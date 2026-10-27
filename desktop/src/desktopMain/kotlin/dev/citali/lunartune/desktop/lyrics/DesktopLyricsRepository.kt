/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.lyrics

import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.kugou.KuGou
import moe.rukamori.archivetune.lrclib.LrcLib
import moe.rukamori.archivetune.paxsenix.PaxsenixLyrics
import moe.rukamori.archivetune.simpmusic.SimpMusicLyrics
import moe.rukamori.archivetune.unison.Unison
import moe.rukamori.archivetune.youlyplus.YouLyPlus

/**
 * Ordered provider chain. Every provider except the BetterLyrics app-layer
 * client is a module from the very same lyrics library the Android app ships.
 */
class DesktopLyricsRepository(
    private val decoder: WordTimingDecoder = JvmWordTimingDecoder,
) : LyricsRepository {
    override suspend fun load(track: Track): LyricsDocument? =
        withContext(Dispatchers.IO) {
            val title = track.title.trim()
            val artist = track.artist.trim()
            val duration = track.durationSeconds

            val sources: List<Pair<String, suspend () -> List<String>>> =
                listOf(
                    "BetterLyrics" to {
                        BetterLyricsClient.fetchAll(title, artist, duration)
                    },
                    "LrcLib" to {
                        listOfNotNull(
                            runCatching { LrcLib.getLyrics(title, artist, duration, track.album).getOrNull() }.getOrNull(),
                        )
                    },
                    "KuGou" to {
                        listOfNotNull(runCatching { KuGou.getLyrics(title, artist, duration).getOrNull() }.getOrNull())
                    },
                    "Unison" to {
                        listOfNotNull(
                            runCatching {
                                Unison
                                    .getLyrics(
                                        videoId = track.id,
                                        title = title,
                                        artist = artist,
                                        album = track.album,
                                        durationSeconds = duration,
                                    ).getOrNull()
                            }.getOrNull(),
                        )
                    },
                    "SimpMusic" to {
                        listOfNotNull(runCatching { SimpMusicLyrics.getLyrics(track.id, duration.coerceAtLeast(0)).getOrNull() }.getOrNull())
                    },
                    "Paxsenix" to {
                        listOfNotNull(runCatching { PaxsenixLyrics.getLyrics(title, artist, duration).getOrNull() }.getOrNull())
                    },
                    "YouLyPlus" to {
                        listOfNotNull(
                            runCatching {
                                YouLyPlus.getLyrics(title = title, artist = artist, album = track.album, durationSeconds = duration).getOrNull()
                            }.getOrNull(),
                        )
                    },
                )

            for ((name, fetch) in sources) {
                val payloads = runCatching { fetch() }.getOrDefault(emptyList())
                for (payload in payloads) {
                    val document = runCatching { LyricsParser.parse(payload, name, decoder) }.getOrNull()
                    if (document != null && document.lines.isNotEmpty()) return@withContext document
                }
            }
            null
        }
}
