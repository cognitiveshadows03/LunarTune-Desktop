/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.player

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.citali.lunartune.desktop.model.PlaybackState
import dev.citali.lunartune.desktop.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36"

/**
 * JVM playback engine. LavaPlayer decodes the stream into PCM frames and a Java
 * Sound [SourceDataLine] plays them; the position ticker feeds the shared
 * [PlaybackState] that drives the word-synced lyrics view.
 */
class LavaplayerController(
    private val resolver: YouTubeStreamResolver,
) : PlayerController {
    private val manager: AudioPlayerManager =
        DefaultAudioPlayerManager().apply {
            configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_LE
            AudioSourceManagers.registerRemoteSources(this)
            // googlevideo URLs are tied to a User-Agent; set it on the HTTP source.
            runCatching {
                source(HttpAudioSourceManager::class.java)?.configureBuilder { builder ->
                    builder.setUserAgent(USER_AGENT)
                    Unit
                }
            }
        }

    private val player = manager.createPlayer()
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loadJob: Job? = null

    @Volatile
    private var running = true
    private var audioThread: Thread? = null
    private var line: SourceDataLine? = null

    init {
        startAudioThread()
        scope.launch {
            while (isActive) {
                val track = player.playingTrack
                _state.update { current ->
                    current.copy(
                        isPlaying = track != null && !player.isPaused,
                        positionMs = track?.position ?: current.positionMs,
                        durationMs = track?.duration?.takeIf { it > 0 } ?: current.durationMs,
                    )
                }
                delay(100L)
            }
        }
    }

    override fun play(track: Track) {
        loadJob?.cancel()
        _state.update {
            it.copy(
                track = track,
                buffering = true,
                error = null,
                isPlaying = false,
                positionMs = 0L,
                durationMs = (track.durationSeconds.coerceAtLeast(0) * 1_000L),
            )
        }
        loadJob =
            scope.launch {
                val stream =
                    if (!track.streamUrl.isNullOrBlank()) {
                        YouTubeStreamResolver.Resolved(track.streamUrl, USER_AGENT, _state.value.durationMs)
                    } else {
                        resolver.resolve(track.id).getOrElse { failure ->
                            _state.update { it.copy(buffering = false, error = failure.message) }
                            return@launch
                        }
                    }
                stream.durationMs?.let { ms -> _state.update { it.copy(durationMs = ms) } }
                load(stream.url, track.title)
            }
    }

    private fun load(
        url: String,
        title: String,
    ) {
        manager.loadItem(
            url,
            object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    player.playTrack(track)
                    _state.update {
                        it.copy(
                            isPlaying = true,
                            buffering = false,
                            error = null,
                            durationMs = track.duration.takeIf { d -> d > 0 } ?: it.durationMs,
                        )
                    }
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    val first = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                    if (first != null) trackLoaded(first) else fail("No playable track in playlist")
                }

                override fun noMatches() = fail("No playable stream at this URL")

                override fun loadFailed(exception: FriendlyException) = fail(exception.message ?: "Playback failed")
            },
        )
    }

    override fun togglePlayPause() {
        if (player.playingTrack == null) {
            _state.value.track?.let { play(it) }
            return
        }
        player.isPaused = !player.isPaused
        _state.update { it.copy(isPlaying = !player.isPaused) }
    }

    override fun seekTo(positionMs: Long) {
        player.playingTrack?.position = positionMs.coerceAtLeast(0L)
        _state.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    override fun stop() {
        loadJob?.cancel()
        player.stopTrack()
        _state.update { it.copy(isPlaying = false, buffering = false) }
    }

    override fun release() {
        running = false
        loadJob?.cancel()
        player.stopTrack()
        player.destroy()
        audioThread?.interrupt()
        scope.cancel()
        runCatching { manager.shutdown() }
    }

    private fun fail(message: String) {
        _state.update { it.copy(buffering = false, isPlaying = false, error = message) }
    }

    private fun startAudioThread() {
        // COMMON_PCM_S16_LE: 44.1 kHz, 16-bit signed little endian, stereo.
        val audioFormat =
            AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44_100f,
                16,
                2,
                4,
                44_100f,
                false,
            )
        val audioRunnable =
            Runnable {
                val output: SourceDataLine =
                    runCatching { AudioSystem.getSourceDataLine(audioFormat) }.getOrNull() ?: return@Runnable
                val opened = runCatching { output.open(audioFormat, 44_100 * 4 / 2) }.isSuccess
                if (!opened) return@Runnable
                output.start()
                line = output
                try {
                    while (running) {
                        val frame =
                            try {
                                player.provide(200, TimeUnit.MILLISECONDS)
                            } catch (_: TimeoutException) {
                                continue
                            } catch (_: InterruptedException) {
                                break
                            } ?: continue
                        val bytes = frame.data
                        val length = frame.dataLength
                        if (bytes != null && length > 0) {
                            output.write(bytes, 0, length.coerceAtMost(bytes.size))
                        }
                    }
                } finally {
                    runCatching { output.drain() }
                    output.stop()
                    output.close()
                    line = null
                }
            }
        audioThread =
            Thread(audioRunnable, "lunartune-audio").apply {
                isDaemon = true
                start()
            }
    }
}
