/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.LyricsLine
import dev.citali.lunartune.desktop.model.LyricsSync

/**
 * Karaoke-style lyrics. Word-timed documents colour each word character by
 * character as the playback position crosses its start/end timestamps; line-timed
 * documents simply highlight the active line.
 */
@Composable
fun WordSyncedLyrics(
    document: LyricsDocument?,
    positionMs: Long,
    status: String,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val activeIndex = document?.activeLineAt(positionMs) ?: -1

    LaunchedEffect(activeIndex, document) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    if (document == null || document.lines.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val sung = MaterialTheme.colorScheme.primary
    val unsung = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
    ) {
        itemsIndexed(document.lines, key = { index, _ -> index }) { index, line ->
            val isActive = index == activeIndex
            Text(
                text = line.annotated(positionMs, document.sync, isActive, sung, unsung),
                style =
                    if (isActive) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Start,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = document.sync != LyricsSync.PLAIN) { onSeek(line.startMs) }
                        .padding(vertical = 4.dp),
            )
        }
    }
}

private fun LyricsLine.annotated(
    positionMs: Long,
    sync: LyricsSync,
    isActive: Boolean,
    sung: Color,
    unsung: Color,
): AnnotatedString {
    val value = displayText
    return buildAnnotatedString {
        append(value)
        if (value.isEmpty()) return@buildAnnotatedString
        if (sync != LyricsSync.WORD || words.isEmpty() || !isActive) {
            addStyle(
                SpanStyle(color = if (isActive && sync != LyricsSync.WORD) sung else unsung),
                0,
                value.length,
            )
            return@buildAnnotatedString
        }
        var cursor = 0
        words.forEach { word ->
            val length = word.text.length
            if (length == 0) return@forEach
            val end = (cursor + length).coerceAtMost(value.length)
            val span = (word.endMs - word.startMs).coerceAtLeast(1L)
            val progress = ((positionMs - word.startMs).toFloat() / span).coerceIn(0f, 1f)
            val split = (cursor + length * progress).toInt().coerceIn(cursor, end)
            if (split > cursor) addStyle(SpanStyle(color = sung), cursor, split)
            if (end > split) addStyle(SpanStyle(color = unsung), split, end)
            cursor = end
        }
        if (cursor < value.length) addStyle(SpanStyle(color = unsung), cursor, value.length)
    }
}
