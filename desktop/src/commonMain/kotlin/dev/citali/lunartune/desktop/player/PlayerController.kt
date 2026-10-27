/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.player

import dev.citali.lunartune.desktop.model.PlaybackState
import dev.citali.lunartune.desktop.model.Track
import kotlinx.coroutines.flow.StateFlow

/** The playback boundary shared by every target. */
interface PlayerController {
    val state: StateFlow<PlaybackState>

    fun play(track: Track)

    fun togglePlayPause()

    fun seekTo(positionMs: Long)

    fun stop()

    fun release()
}
