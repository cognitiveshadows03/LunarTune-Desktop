/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.catalog

import dev.citali.lunartune.desktop.model.Track

/** Search/browse boundary; implemented on the platform with the shared InnerTube client. */
interface TrackCatalog {
    suspend fun search(query: String): Result<List<Track>>
}
