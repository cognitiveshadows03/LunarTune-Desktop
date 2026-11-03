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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.app.DesktopAppViewModel
import dev.citali.lunartune.desktop.model.Track

private enum class LibraryTab(val label: String) { SONGS("Songs"), HISTORY("History"), ALBUMS("Albums"), ARTISTS("Artists") }

@Composable
fun LibraryScreen(viewModel: DesktopAppViewModel) {
    val results by viewModel.results.collectAsState()
    val history by viewModel.history.collectAsState()
    val liked by viewModel.liked.collectAsState()
    val playback by viewModel.playback.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)) {
            Text("Library", style = MaterialTheme.typography.headlineLarge)
        }
        PrimaryTabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            LibraryTab.entries.forEachIndexed { index, entry ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(entry.label) },
                )
            }
        }

        when (LibraryTab.entries[tab]) {
            LibraryTab.SONGS -> SongList(
                tracks = results,
                currentId = playback.track?.id,
                onPlay = { idx -> viewModel.playFromList(results, idx) },
                emptyMessage = "Run a search to populate your library.",
            )
            LibraryTab.HISTORY -> SongList(
                tracks = history,
                currentId = playback.track?.id,
                onPlay = { idx -> viewModel.playFromList(history, idx) },
                emptyMessage = "Tracks you play will show up here.",
            )
            LibraryTab.ALBUMS -> AlbumGrid(results, playback.track?.id) { idx -> viewModel.playFromList(results, idx) }
            LibraryTab.ARTISTS -> ArtistGrid(results, playback.track?.id) { idx -> viewModel.playFromList(results, idx) }
        }
    }
}

@Composable
private fun SongList(
    tracks: List<Track>,
    currentId: String?,
    onPlay: (Int) -> Unit,
    emptyMessage: String,
) {
    if (tracks.isEmpty()) {
        EmptyState(emptyMessage)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(tracks, key = { it.id }) { track ->
            val idx = tracks.indexOf(track)
            LibraryRow(track, track.id == currentId) { onPlay(idx) }
        }
    }
}

@Composable
private fun AlbumGrid(tracks: List<Track>, currentId: String?, onPlay: (Int) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyState("No albums yet.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tracks.chunked(4)) { rowTracks ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                rowTracks.forEach { track ->
                    val idx = tracks.indexOf(track)
                    Column(modifier = Modifier.width(140.dp).clickable { onPlay(idx) }) {
                        Artwork(track.title, track.thumbnailUrl, 140.dp, cornerRadius = 12.dp)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            track.album ?: track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (track.id == currentId) FontWeight.Bold else FontWeight.Normal,
                            color = if (track.id == currentId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistGrid(tracks: List<Track>, currentId: String?, onPlay: (Int) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyState("No artists yet.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tracks.chunked(4)) { rowTracks ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                rowTracks.forEach { track ->
                    val idx = tracks.indexOf(track)
                    Column(modifier = Modifier.width(120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onPlay(idx) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Artwork(track.artist, track.thumbnailUrl, 120.dp, cornerRadius = 60.dp)
                        }
                        Spacer(Modifier.size(6.dp))
                        Text(
                            track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (track.id == currentId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(track: Track, playing: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Artwork(track.title, track.thumbnailUrl, 40.dp, cornerRadius = 8.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (track.durationSeconds > 0) {
            Text(formatTime(track.durationSeconds * 1000L), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(LibraryIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
            Spacer(Modifier.size(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
