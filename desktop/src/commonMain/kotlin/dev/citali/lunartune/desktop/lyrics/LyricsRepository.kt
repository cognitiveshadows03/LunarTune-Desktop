/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.lyrics

import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.Track

/**
 * Fetches lyrics for a track. The common code only sees parsed documents, so the
 * platform side is free to use the same JVM provider modules as the Android app.
 */
interface LyricsRepository {
    suspend fun load(track: Track): LyricsDocument?
}

/** Bridge used by the shared parser for word-timed formats that need a DOM/XML parser. */
interface WordTimingDecoder {
    fun decode(raw: String, source: String): LyricsDocument?
}
