/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.app.DesktopAppViewModel

enum class Destination { HOME, SEARCH, LIBRARY, SETTINGS }

/**
 * Three-pane desktop shell: navigation rail, main content, and a now-playing
 * panel with word-synced lyrics, with a persistent player bar along the bottom.
 */
@Composable
fun LunarTuneApp(
    viewModel: DesktopAppViewModel,
    modifier: Modifier = Modifier,
) {
    val playback by viewModel.playback.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val lyricsStatus by viewModel.lyricsStatus.collectAsState()

    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var showLyrics by rememberSaveable { mutableStateOf(true) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                SideRail(
                    destination = destination,
                    onSelect = { destination = it },
                    modifier = Modifier.fillMaxHeight(),
                )

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    when (destination) {
                        Destination.HOME -> HomeScreen(viewModel)
                        Destination.SEARCH -> SearchScreen(viewModel)
                        Destination.LIBRARY -> LibraryScreen(viewModel)
                        Destination.SETTINGS -> SettingsScreen(viewModel)
                    }
                }

                if (showLyrics) {
                    NowPlayingPanel(
                        track = playback.track,
                        positionMs = playback.positionMs,
                        document = lyrics,
                        status = lyricsStatus,
                        onSeek = viewModel::seekTo,
                        modifier = Modifier.width(360.dp).fillMaxHeight().padding(12.dp),
                    )
                }
            }

            PlayerBar(
                state = playback,
                showLyrics = showLyrics,
                onTogglePlay = viewModel::togglePlayPause,
                onSeek = viewModel::seekTo,
                onNext = viewModel::playNext,
                onPrevious = viewModel::playPrevious,
                onToggleLyrics = { showLyrics = !showLyrics },
                onLike = { playback.track?.let { viewModel.toggleLike(it) } },
                isLiked = playback.track?.let { viewModel.isLiked(it) } ?: false,
            )
        }
    }
}

@Composable
private fun SideRail(
    destination: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier.padding(vertical = 12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        val entries =
            listOf(
                Triple(Destination.HOME, "Home", HomeIcon),
                Triple(Destination.SEARCH, "Search", SearchIcon),
                Triple(Destination.LIBRARY, "Library", LibraryIcon),
                Triple(Destination.SETTINGS, "Settings", SettingsIcon),
            )
        entries.forEach { (target, label, icon) ->
            NavigationRailItem(
                selected = destination == target,
                onClick = { onSelect(target) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                alwaysShowLabel = false,
            )
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp, horizontal = 4.dp))
        Text(
            text = "LunarTune",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp),
        )
    }
}
