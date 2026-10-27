/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.model

/** A playable song. [id] is the YouTube video id; [streamUrl] is filled in by the platform resolver. */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationSeconds: Int = -1,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
)

enum class LyricsSync {
    /** No timing information at all. */
    PLAIN,

    /** One timestamp per line (standard LRC). */
    LINE,

    /** A timestamp for every word/syllable (enhanced LRC, TTML, QRC). */
    WORD,
}

data class WordTiming(
    val text: String,
    val startMs: Long,
    val endMs: Long,
)

data class LyricsLine(
    val startMs: Long,
    val endMs: Long? = null,
    val text: String,
    val words: List<WordTiming> = emptyList(),
) {
    /** Text to display, including the whitespace between enhanced-LRC words. */
    val displayText: String
        get() = if (words.isEmpty()) text else words.joinToString("") { it.text }
}

data class LyricsDocument(
    val lines: List<LyricsLine>,
    val sync: LyricsSync,
    val source: String,
) {
    fun activeLineAt(positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var low = 0
        var high = lines.lastIndex
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (lines[mid].startMs <= positionMs) low = mid else high = mid - 1
        }
        return if (lines[0].startMs > positionMs) -1 else low
    }
}

data class PlaybackState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val buffering: Boolean = false,
    val error: String? = null,
)
