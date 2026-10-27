/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.app.DesktopAppViewModel
import dev.citali.lunartune.desktop.model.Track

private enum class Destination { HOME, SEARCH, LIBRARY, SETTINGS }

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
    val results by viewModel.results.collectAsState()
    val query by viewModel.query.collectAsState()
    val searching by viewModel.isSearching.collectAsState()
    val error by viewModel.catalogError.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val lyricsStatus by viewModel.lyricsStatus.collectAsState()

    var destination by remember { mutableStateOf(Destination.SEARCH) }
    var showLyrics by remember { mutableStateOf(true) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    val entries =
                        listOf(
                            Triple(Destination.HOME, "Home", Icons.Rounded.Home),
                            Triple(Destination.SEARCH, "Search", Icons.Rounded.Search),
                            Triple(Destination.LIBRARY, "Library", Icons.Rounded.LibraryMusic),
                            Triple(Destination.SETTINGS, "Settings", Icons.Rounded.Settings),
                        )
                    entries.forEach { (target, label, icon) ->
                        NavigationRailItem(
                            selected = destination == target,
                            onClick = { destination = target },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            alwaysShowLabel = false,
                        )
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                ) {
                    Text(
                        text =
                            when (destination) {
                                Destination.HOME -> "Home"
                                Destination.SEARCH -> "Search"
                                Destination.LIBRARY -> "Library"
                                Destination.SETTINGS -> "Settings"
                            },
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        singleLine = true,
                        placeholder = { Text("Search songs, artists, albums…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(16.dp))

                    if (searching) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Searching…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(results, key = { it.id }) { track ->
                            TrackRow(track = track, onClick = { viewModel.play(track) })
                        }
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
                onToggleLyrics = { showLyrics = !showLyrics },
            )
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        ArtworkTile(title = track.title, size = 48.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (track.durationSeconds > 0) {
            Text(
                text = formatTime(track.durationSeconds * 1000L),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ArtworkTile(
    title: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val seed = (title.hashCode() and 0x00FFFFFF) or 0xFF000000.toInt()
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(seed).copy(alpha = 0.75f)),
    ) {
        Text(
            text = title.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}
