/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.platform

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.rukamori.archivetune.innertube.YouTube
import moe.rukamori.archivetune.innertube.models.YouTubeLocale

/** One-time, anonymous setup for the shared InnerTube client. */
object YouTubeSession {
    private val mutex = Mutex()

    @Volatile
    private var ready = false

    suspend fun ensure() {
        if (ready) return
        mutex.withLock {
            if (ready) return
            YouTube.locale = YouTubeLocale(gl = "US", hl = "en")
            // A visitor-data token is required by some player clients; failing to
            // fetch one must not stop search or playback from being attempted.
            runCatching {
                YouTube.visitorData().getOrNull()?.let { YouTube.visitorData = it }
            }
            ready = true
        }
    }
}
