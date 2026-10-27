/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.model.PlaybackState

@Composable
fun PlayerBar(
    state: PlaybackState,
    showLyrics: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = state.durationMs.coerceAtLeast(1L)
    var dragPosition by remember { mutableFloatStateOf(state.positionMs.toFloat()) }
    val displayedPosition =
        if (dragPosition > 0f && dragPosition != state.positionMs.toFloat()) {
            dragPosition
        } else {
            state.positionMs.toFloat()
        }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            ArtworkTile(title = state.track?.title ?: "LunarTune", size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.width(180.dp)) {
                Text(
                    text = state.track?.title ?: "Nothing playing",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.track?.artist ?: state.error ?: "LunarTune Desktop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(12.dp))
            FilledIconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (state.isPlaying) PauseIcon else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(12.dp))
            Text(formatTime(displayedPosition.toLong()), style = MaterialTheme.typography.labelSmall)
            Slider(
                value = displayedPosition.coerceIn(0f, duration.toFloat()),
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    onSeek(dragPosition.toLong())
                    dragPosition = 0f
                },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            Text(formatTime(duration), style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onToggleLyrics) {
                Text(if (showLyrics) "Hide lyrics" else "Lyrics")
            }
        }
    }
}
