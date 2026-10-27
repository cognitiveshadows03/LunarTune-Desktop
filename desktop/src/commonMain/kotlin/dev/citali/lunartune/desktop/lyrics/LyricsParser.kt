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

/**
 * Pure-Kotlin lyrics parsing shared by every target.
 *
 * Handles plain text, standard LRC and enhanced (word-timed) LRC. TTML and QRC -
 * the formats BetterLyrics returns - are handed to a [WordTimingDecoder] on the
 * platform side, which uses the exact same parser classes as the Android app.
 */
object LyricsParser {
    private val lrcTag = Regex("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]")
    private val lrcWordTag = Regex("<(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?>")
    private val lrcLine = Regex("((?:\\[\\d{1,3}:\\d{1,2}(?:[.:]\\d{1,3})?])+)(.*)")
    private val anyTag = Regex("<[^>]*>")

    fun parse(
        raw: String,
        source: String,
        wordDecoder: WordTimingDecoder? = null,
    ): LyricsDocument? {
        val text = raw.trim().removePrefix("\uFEFF")
        if (text.isEmpty()) return null

        val looksLikeMarkup =
            text.startsWith("<") || text.contains("QrcInfos", ignoreCase = true)
        if (looksLikeMarkup) {
            wordDecoder?.decode(text, source)?.let { return it }
            // A word-timed document we could not decode is not useful as plain text.
            return null
        }

        return parseLrc(text, source) ?: plain(text, source)
    }

    /** Standard LRC plus the `<mm:ss.xx>` enhanced-LRC word tags. */
    fun parseLrc(
        text: String,
        source: String,
    ): LyricsDocument? {
        val timed = mutableListOf<Pair<Long, String>>()
        text.lineSequence().forEach { rawLine ->
            val match = lrcLine.find(rawLine.trim()) ?: return@forEach
            val stamps = lrcTag.findAll(match.groupValues[1])
            val content = match.groupValues[2].trim()
            stamps.forEach { tag ->
                timed += stamp(tag.groupValues[1], tag.groupValues[2], tag.groupValues[3]) to content
            }
        }
        if (timed.isEmpty()) return null

        timed.sortBy { it.first }
        val lines =
            timed.mapIndexed { index, (start, content) ->
                val end = timed.getOrNull(index + 1)?.first?.minus(1)?.coerceAtLeast(start)
                val words = parseEnhancedWords(content, start, end ?: (start + 4_000L))
                val display = if (words.isEmpty()) content.replace(anyTag, "") else words.joinToString("") { it.text }
                LyricsLine(
                    startMs = start,
                    endMs = end,
                    text = display,
                    words = words,
                )
            }.filter { it.text.isNotBlank() }

        if (lines.isEmpty()) return null
        val sync = if (lines.any { it.words.isNotEmpty() }) LyricsSync.WORD else LyricsSync.LINE
        return LyricsDocument(lines = lines, sync = sync, source = source)
    }

    fun plain(
        text: String,
        source: String,
    ): LyricsDocument {
        val lines =
            text.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { LyricsLine(startMs = 0L, text = it) }
                .toList()
        return LyricsDocument(lines = lines, sync = LyricsSync.PLAIN, source = source)
    }

    private fun parseEnhancedWords(
        content: String,
        lineStartMs: Long,
        lineEndMs: Long,
    ): List<WordTiming> {
        val tags = lrcWordTag.findAll(content).toList()
        if (tags.isEmpty()) return emptyList()
        val words = mutableListOf<WordTiming>()
        tags.forEachIndexed { index, tag ->
            val start = stamp(tag.groupValues[1], tag.groupValues[2], tag.groupValues[3])
            val next = tags.getOrNull(index + 1)
            val end = next?.let { stamp(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
                ?: maxOf(lineEndMs, start + 1)
            val from = tag.range.last + 1
            val to = next?.range?.first ?: content.length
            if (to > from) {
                words += WordTiming(
                    text = content.substring(from, to),
                    startMs = start,
                    endMs = maxOf(start + 1, end),
                )
            }
        }
        return words
    }

    private fun stamp(
        minutes: String,
        seconds: String,
        fraction: String?,
    ): Long {
        val value = fraction.orEmpty()
        val millis =
            when (value.length) {
                0 -> 0L
                1 -> value.toLong() * 100L
                2 -> value.toLong() * 10L
                else -> value.take(3).padEnd(3, '0').toLong()
            }
        return minutes.toLong() * 60_000L + seconds.toLong() * 1_000L + millis
    }
}
