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

/**
 * The TTML below has the same shape as a live 200 response from the
 * BetterLyrics `getLyrics` endpoint: `itunes:timing="Word"`, a `<p>` per line
 * and a `<span begin/end>` per word.
 */
private val LIVE_SHAPED_TTML =
    """
    <tt xmlns="http://www.w3.org/ns/ttml"
        xmlns:itunes="http://music.apple.com/lyric-ttml-internal"
        xmlns:ttm="http://www.w3.org/ns/ttml#metadata"
        itunes:timing="Word" xml:lang="en">
      <head><metadata/></head>
      <body><div>
        <p begin="1.200" end="3.400">
          <span begin="1.200" end="1.700">Hello </span>
          <span begin="1.700" end="2.600">world</span>
        </p>
        <p begin="3.500" end="6.000">
          <span begin="3.500" end="4.200">Second </span>
          <span begin="4.200" end="5.100">line</span>
        </p>
      </div></body>
    </tt>
    """.trimIndent()

class JvmWordTimingDecoderTest {
    @Test
    fun liveShapedTtmlProducesWordTimings() {
        val document = assertNotNull(LyricsParser.parse(LIVE_SHAPED_TTML, "BetterLyrics", JvmWordTimingDecoder))
        assertEquals(LyricsSync.WORD, document.sync)
        assertEquals(2, document.lines.size)
        val first = document.lines[0]
        assertEquals(1_200L, first.startMs)
        assertTrue(first.words.isNotEmpty(), "expected word-level timings from TTML")
        assertEquals(first.text, first.displayText, "word timings must cover the whole line")
        assertEquals(1_200L, first.words.first().startMs)
    }

    @Test
    fun betterLyricsJsonEnvelopeIsUnwrapped() {
        val body = "{\"ttml\":\"<tt xmlns=\\\"http://www.w3.org/ns/ttml\\\"><body><div><p begin=\\\"0s\\\" end=\\\"2s\\\">Hi</p></div></body></tt>\"}"
        val decoded = assertNotNull(BetterLyricsClient.decodePayload(body))
        assertTrue(decoded.startsWith("<tt"), "decoded payload should be raw TTML")
    }
}
