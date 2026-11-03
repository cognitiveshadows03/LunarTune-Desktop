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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.app.DesktopAppViewModel
import dev.citali.lunartune.desktop.app.HomeSection
import dev.citali.lunartune.desktop.model.Track

@Composable
fun HomeScreen(viewModel: DesktopAppViewModel) {
    val sections by viewModel.homeSections.collectAsState()
    val loading by viewModel.homeLoading.collectAsState()
    val playback by viewModel.playback.collectAsState()
    val catalogError by viewModel.catalogError.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            Column {
                Text(
                    text = greeting(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "What would you like to play today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { MoodChips() }

        if (loading && sections.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                }
            }
        }

        catalogError?.let {
            item {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // First section gets a hero row: one large card plus three side cards.
        sections.firstOrNull()?.let { section ->
            item {
                SectionHeader(section.title, section.subtitle)
                Spacer(Modifier.height(14.dp))
                QuickPicksRow(
                    section = section,
                    currentId = playback.track?.id,
                    onPlay = { idx -> viewModel.playFromList(section.tracks, idx) },
                )
            }
        }

        // "Made for you" renders as gradient mood cards.
        sections.drop(1).firstOrNull { it.title == "Made for you" }?.let { section ->
            item {
                SectionHeader(section.title, section.subtitle)
                Spacer(Modifier.height(14.dp))
                MadeForYouRow(
                    section = section,
                    currentId = playback.track?.id,
                    onPlay = { idx -> viewModel.playFromList(section.tracks, idx) },
                )
            }
        }

        // Remaining sections as standard artwork rows.
        sections
            .drop(1)
            .filter { it.title != "Made for you" }
            .forEach { section ->
                item {
                    SectionHeader(section.title, section.subtitle)
                    Spacer(Modifier.height(14.dp))
                    TrackCardRow(
                        section = section,
                        currentId = playback.track?.id,
                        onPlay = { idx -> viewModel.playFromList(section.tracks, idx) },
                    )
                }
            }
    }
}

@Composable
private fun MoodChips() {
    val moods =
        listOf(
            "Relax" to MoodPalette.relax,
            "Workout" to MoodPalette.workout,
            "Party" to MoodPalette.party,
            "Focus" to MoodPalette.focus,
            "Sleep" to MoodPalette.sleep,
        )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(moods) { (label, color) ->
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text(label) },
                leadingIcon = {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color),
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickPicksRow(
    section: HomeSection,
    currentId: String?,
    onPlay: (Int) -> Unit,
) {
    val tracks = section.tracks
    if (tracks.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Hero card spans two rows.
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1.1f),
        ) {
            LargeTrackCard(
                track = tracks.first(),
                playing = tracks.first().id == currentId,
                onClick = { onPlay(0) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (tracks.size >= 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    CompactTrackCard(tracks[1], tracks[1].id == currentId, { onPlay(1) }, Modifier.weight(1f))
                    CompactTrackCard(tracks[2], tracks[2].id == currentId, { onPlay(2) }, Modifier.weight(1f))
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            tracks.drop(3).take(4).forEachIndexed { index, track ->
                SidePickRow(
                    track = track,
                    playing = track.id == currentId,
                    onClick = { onPlay(index + 3) },
                )
            }
        }
    }
}

@Composable
private fun LargeTrackCard(
    track: Track,
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.aspectRatio(16f / 9f).clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Artwork(
                title = track.title,
                thumbnailUrl = track.thumbnailUrl,
                size = 0.dp,
                cornerRadius = 16.dp,
                fill = true,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playing) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Now playing",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactTrackCard(
    track: Track,
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize()) {
            Artwork(
                title = track.title,
                thumbnailUrl = track.thumbnailUrl,
                size = 0.dp,
                cornerRadius = 12.dp,
                fill = true,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = track.title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (playing) FontWeight.Bold else FontWeight.Medium,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SidePickRow(
    track: Track,
    playing: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
    ) {
        Artwork(
            title = track.title,
            thumbnailUrl = track.thumbnailUrl,
            size = 44.dp,
            cornerRadius = 8.dp,
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (playing) FontWeight.Bold else FontWeight.Normal,
                color = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
    }
}

@Composable
private fun TrackCardRow(
    section: HomeSection,
    currentId: String?,
    onPlay: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(section.tracks) { track ->
            val index = section.tracks.indexOf(track)
            SquareTrackCard(
                track = track,
                playing = track.id == currentId,
                onClick = { onPlay(index) },
            )
        }
    }
}

@Composable
private fun SquareTrackCard(
    track: Track,
    playing: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(150.dp).clickable(onClick = onClick),
    ) {
        Artwork(
            title = track.title,
            thumbnailUrl = track.thumbnailUrl,
            size = 150.dp,
            cornerRadius = 12.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (playing) FontWeight.Bold else FontWeight.Normal,
            color = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
}

@Composable
private fun MadeForYouRow(
    section: HomeSection,
    currentId: String?,
    onPlay: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(section.tracks.take(8)) { track ->
            val index = section.tracks.indexOf(track)
            MoodCard(
                track = track,
                playing = track.id == currentId,
                onClick = { onPlay(index) },
            )
        }
    }
}

@Composable
private fun MoodCard(
    track: Track,
    playing: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(width = 200.dp, height = 110.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(gradientFor(track.title))
                .clickable(onClick = onClick)
                .padding(14.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playing) {
                Text(
                    text = "Now playing",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun greeting(): String {
    val hour = currentHourOfDay()
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..22 -> "Good evening"
        else -> "Still up?"
    }
}
