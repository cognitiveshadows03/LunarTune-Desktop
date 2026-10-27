/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.catalog

import dev.citali.lunartune.desktop.model.Track
import dev.citali.lunartune.desktop.platform.YouTubeSession
import moe.rukamori.archivetune.innertube.YouTube
import moe.rukamori.archivetune.innertube.models.SongItem

/** Search powered by the shared InnerTube client (same API as the Android app). */
class InnerTubeCatalog : TrackCatalog {
    override suspend fun search(query: String): Result<List<Track>> =
        runCatching {
            YouTubeSession.ensure()
            val page =
                YouTube
                    .search(
                        query = query,
                        filter = YouTube.SearchFilter.FILTER_SONG,
                        useAccountContext = false,
                    ).getOrThrow()
            page.items.filterIsInstance<SongItem>().map { it.toTrack() }
        }

    private fun SongItem.toTrack(): Track =
        Track(
            id = id,
            title = title,
            artist = artists.joinToString(", ") { it.name }.ifBlank { "Unknown artist" },
            album = album?.name,
            durationSeconds = duration ?: -1,
            thumbnailUrl = thumbnail,
        )
}
