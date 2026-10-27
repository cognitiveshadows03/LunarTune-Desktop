/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.lyrics

import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.LyricsLine
import dev.citali.lunartune.desktop.model.LyricsSync
import dev.citali.lunartune.desktop.model.WordTiming
import moe.rukamori.archivetune.betterlyrics.QRCParser
import moe.rukamori.archivetune.betterlyrics.TTMLParser

/**
 * Uses the exact TTML/QRC parsers from the shared lyrics library as the Android
 * app, then maps their word timings into the common [LyricsDocument] model.
 */
object JvmWordTimingDecoder : WordTimingDecoder {
    override fun decode(
        raw: String,
        source: String,
    ): LyricsDocument? {
        val text = raw.trim()
        return when {
            QRCParser.isQrc(text) -> fromQrc(QRCParser.parseQrc(text), source)
            text.contains("<tt", ignoreCase = true) || text.startsWith("<?xml") ->
                fromTtml(TTMLParser.parseTTML(text), source)

            else -> null
        }
    }

    private fun fromTtml(
        parsed: List<TTMLParser.ParsedLine>,
        source: String,
    ): LyricsDocument? = build(parsed.map { line ->
        val words =
            line.words.map { word ->
                WordTiming(
                    text = word.text,
                    startMs = (word.startTime * 1_000).toLong(),
                    endMs = (word.endTime * 1_000).toLong(),
                )
            }
        TimedLine(
            startMs = (line.startTime * 1_000).toLong(),
            endMs = (line.endTime * 1_000).toLong(),
            text = line.text,
            words = alignWords(line.text, words),
        )
    }, source)

    private fun fromQrc(
        parsed: List<QRCParser.ParsedLine>,
        source: String,
    ): LyricsDocument? = build(parsed.map { line ->
        val words =
            line.words.map { word ->
                WordTiming(
                    text = word.text,
                    startMs = (word.startTime * 1_000).toLong(),
                    endMs = (word.endTime * 1_000).toLong(),
                )
            }
        TimedLine(
            startMs = (line.startTime * 1_000).toLong(),
            endMs = (line.endTime * 1_000).toLong(),
            text = line.text,
            words = alignWords(line.text, words),
        )
    }, source)

    /**
     * The library returns trimmed word text, while the line text keeps the
     * spaces. Re-attach those separators to the neighbouring word so the word
     * timings still cover exactly the displayed string.
     */
    private fun alignWords(
        fullText: String,
        words: List<WordTiming>,
    ): List<WordTiming> {
        if (words.isEmpty()) return emptyList()
        var cursor = 0
        val aligned = mutableListOf<WordTiming>()
        words.forEach { word ->
            val index = fullText.indexOf(word.text, cursor)
            if (index < 0) {
                aligned += word
                return@forEach
            }
            val gap = fullText.substring(cursor, index)
            if (gap.isNotEmpty()) {
                if (aligned.isEmpty()) {
                    aligned += word.copy(text = gap + word.text)
                } else {
                    val previous = aligned.removeAt(aligned.lastIndex)
                    aligned += previous.copy(text = previous.text + gap)
                    aligned += word
                }
            } else {
                aligned += word
            }
            cursor = index + word.text.length
        }
        if (cursor < fullText.length && aligned.isNotEmpty()) {
            val last = aligned.removeAt(aligned.lastIndex)
            aligned += last.copy(text = last.text + fullText.substring(cursor))
        }
        return aligned
    }

    private data class TimedLine(
        val startMs: Long,
        val endMs: Long,
        val text: String,
        val words: List<WordTiming>,
    )

    private fun build(
        lines: List<TimedLine>,
        source: String,
    ): LyricsDocument? {
        val mapped =
            lines
                .filter { it.text.isNotBlank() }
                .map { line ->
                    LyricsLine(
                        startMs = line.startMs,
                        endMs = line.endMs,
                        text = line.text,
                        words = line.words,
                    )
                }
        if (mapped.isEmpty()) return null
        val sync = if (mapped.any { it.words.isNotEmpty() }) LyricsSync.WORD else LyricsSync.LINE
        return LyricsDocument(lines = mapped, sync = sync, source = source)
    }
}
