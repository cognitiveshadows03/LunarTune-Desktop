/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.lyrics

import dev.citali.lunartune.desktop.model.LyricsSync
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LyricsParserTest {
    @Test
    fun standardLrcIsLineSynced() {
        val raw = "[00:00.00]First line\n[00:03.50]Second line\n[00:07.00]Third line"
        val document = assertNotNull(LyricsParser.parse(raw, "test"))
        assertEquals(LyricsSync.LINE, document.sync)
        assertEquals(3, document.lines.size)
        assertEquals(3_500L, document.lines[1].startMs)
        assertEquals("Third line", document.lines[2].text)
        assertTrue(document.lines[1].words.isEmpty())
    }

    @Test
    fun enhancedLrcHasWordTimings() {
        val raw = "[00:12.00]<00:12.00>Hello <00:12.80>world <00:13.40>again"
        val document = assertNotNull(LyricsParser.parse(raw, "test"))
        assertEquals(LyricsSync.WORD, document.sync)
        assertEquals(3, document.lines[0].words.size)
        assertEquals(12_000L, document.lines[0].words[0].startMs)
        assertEquals(12_800L, document.lines[0].words[1].startMs)
        assertEquals("Hello world again", document.lines[0].displayText)
    }

    @Test
    fun textWithoutTagsFallsBackToPlain() {
        val document = assertNotNull(LyricsParser.parse("Just words\nOn two lines", "test"))
        assertEquals(LyricsSync.PLAIN, document.sync)
        assertEquals(2, document.lines.size)
    }

    @Test
    fun activeLineFollowsPosition() {
        val document = assertNotNull(LyricsParser.parse("[00:00.00]One\n[00:05.00]Two\n[00:10.00]Three", "test"))
        assertEquals(-1, document.activeLineAt(-1L))
        assertEquals(1, document.activeLineAt(5_000L))
        assertEquals(2, document.activeLineAt(99_000L))
    }

    @Test
    fun ttmlNeedsThePlatformDecoder() {
        val ttml = "<tt xmlns=\"http://www.w3.org/ns/ttml\"><body><div><p begin=\"0s\" end=\"2s\">Hi</p></div></body></tt>"
        assertEquals(null, LyricsParser.parse(ttml, "test", wordDecoder = null))
    }
}
