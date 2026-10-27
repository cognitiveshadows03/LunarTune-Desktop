/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.app

import dev.citali.lunartune.desktop.catalog.TrackCatalog
import dev.citali.lunartune.desktop.lyrics.LyricsRepository
import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.PlaybackState
import dev.citali.lunartune.desktop.model.Track
import dev.citali.lunartune.desktop.player.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared state holder. Everything in here is common code: the platform supplies a
 * catalog, a lyrics repository and a player, and the UI is the same on every target.
 */
class DesktopAppViewModel(
    private val catalog: TrackCatalog,
    private val lyricsRepository: LyricsRepository,
    private val player: PlayerController,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private val _lyrics = MutableStateFlow<LyricsDocument?>(null)
    val lyrics: StateFlow<LyricsDocument?> = _lyrics.asStateFlow()

    private val _lyricsStatus = MutableStateFlow("Start a song to see lyrics")
    val lyricsStatus: StateFlow<String> = _lyricsStatus.asStateFlow()

    val playback: StateFlow<PlaybackState> = player.state

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        // Load (and cache) lyrics whenever the playing track changes.
        scope.launch {
            player.state
                .map { it.track }
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { track ->
                    if (track == null) {
                        _lyrics.value = null
                        _lyricsStatus.value = "Start a song to see lyrics"
                        return@collect
                    }
                    _lyrics.value = null
                    _lyricsStatus.value = "Loading lyrics…"
                    val document =
                        withContext(Dispatchers.Default) {
                            runCatching { lyricsRepository.load(track) }.getOrNull()
                        }
                    _lyrics.value = document
                    _lyricsStatus.value =
                        when {
                            document == null -> "No lyrics found"
                            document.sync == dev.citali.lunartune.desktop.model.LyricsSync.WORD ->
                                "Word-synced · ${document.source}"
                            document.sync == dev.citali.lunartune.desktop.model.LyricsSync.LINE ->
                                "Line-synced · ${document.source}"
                            else -> "Plain lyrics · ${document.source}"
                        }
                }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun search() {
        val value = _query.value.trim()
        if (value.isBlank()) return
        searchJob?.cancel()
        searchJob =
            scope.launch {
                _isSearching.value = true
                _catalogError.value = null
                val outcome: Result<List<Track>> =
                    withContext(Dispatchers.Default) {
                        try {
                            catalog.search(value)
                        } catch (failure: Throwable) {
                            Result.failure(failure)
                        }
                    }
                outcome
                    .onSuccess { _results.value = it }
                    .onFailure { _catalogError.value = it.message ?: "Search failed" }
                _isSearching.value = false
            }
    }

    fun play(track: Track) = player.play(track)

    fun togglePlayPause() = player.togglePlayPause()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun dispose() {
        searchJob?.cancel()
        scope.cancel()
        player.release()
    }
}
