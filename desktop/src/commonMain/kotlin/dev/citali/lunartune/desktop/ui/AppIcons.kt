/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Compact icon set defined inline so the desktop build never has to ship the
 * ~36 MB material-icons-extended jar. Each glyph follows the Material symbol
 * path (24dp viewport), filled black; Compose tints them via [Icon].
 */
private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

val PauseIcon: ImageVector by lazy {
    icon("Pause") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 5f); lineTo(10f, 5f); lineTo(10f, 19f); lineTo(6f, 19f); close()
            moveTo(14f, 5f); lineTo(18f, 5f); lineTo(18f, 19f); lineTo(14f, 19f); close()
        }
    }
}

val PlayArrowIcon: ImageVector by lazy {
    icon("PlayArrow") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close()
        }
    }
}

val SkipNextIcon: ImageVector by lazy {
    icon("SkipNext") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 5f); lineTo(15f, 12f); lineTo(6f, 19f); close()
            moveTo(17f, 5f); lineTo(19f, 5f); lineTo(19f, 19f); lineTo(17f, 19f); close()
        }
    }
}

val SkipPreviousIcon: ImageVector by lazy {
    icon("SkipPrevious") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18f, 5f); lineTo(9f, 12f); lineTo(18f, 19f); close()
            moveTo(7f, 5f); lineTo(5f, 5f); lineTo(5f, 19f); lineTo(7f, 19f); close()
        }
    }
}

val ShuffleIcon: ImageVector by lazy {
    icon("Shuffle") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(14f, 5f); lineTo(14f, 7f); lineTo(17.6f, 7f); lineTo(5f, 19.6f); lineTo(6.4f, 21f)
            lineTo(19f, 8.4f); lineTo(19f, 12f); lineTo(21f, 12f); lineTo(21f, 5f); close()
            moveTo(5f, 5f); lineTo(5f, 7f); lineTo(9.2f, 7f); lineTo(11.6f, 9.4f); lineTo(13f, 8f)
            lineTo(10f, 5f); close()
            moveTo(19f, 16f); lineTo(19f, 19.6f); lineTo(15.4f, 16f); lineTo(14f, 17.4f)
            lineTo(17f, 20.4f); lineTo(14f, 20.4f); lineTo(14f, 22.4f); lineTo(21f, 22.4f)
            lineTo(21f, 16f); close()
        }
    }
}

val RepeatIcon: ImageVector by lazy {
    icon("Repeat") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 7f); lineTo(17f, 7f); lineTo(17f, 10f); lineTo(21f, 6f); lineTo(17f, 2f)
            lineTo(17f, 5f); lineTo(5f, 5f); lineTo(5f, 11f); lineTo(7f, 11f); close()
            moveTo(17f, 17f); lineTo(7f, 17f); lineTo(7f, 14f); lineTo(3f, 18f); lineTo(7f, 22f)
            lineTo(7f, 19f); lineTo(19f, 19f); lineTo(19f, 13f); lineTo(17f, 13f); close()
        }
    }
}

val VolumeHighIcon: ImageVector by lazy {
    icon("VolumeHigh") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 4f); lineTo(12f, 20f); lineTo(7f, 15f)
            lineTo(3f, 15f); close()
            moveTo(16f, 8f); curveTo(17.5f, 9.5f, 17.5f, 14.5f, 16f, 16f); lineTo(14.8f, 14.8f)
            curveTo(15.7f, 13.9f, 15.7f, 10.1f, 14.8f, 9.2f); close()
            moveTo(19f, 5f); curveTo(22f, 8f, 22f, 16f, 19f, 19f); lineTo(17.8f, 17.8f)
            curveTo(20f, 15.6f, 20f, 8.4f, 17.8f, 6.2f); close()
        }
    }
}

val VolumeMuteIcon: ImageVector by lazy {
    icon("VolumeMute") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 4f); lineTo(12f, 20f); lineTo(7f, 15f)
            lineTo(3f, 15f); close()
            moveTo(16f, 9f); lineTo(22f, 15f); lineTo(20.6f, 16.4f); lineTo(17f, 12.8f)
            lineTo(13.4f, 16.4f); lineTo(12f, 15f); lineTo(15.6f, 11.4f); lineTo(12f, 7.8f)
            lineTo(13.4f, 6.4f); close()
        }
    }
}

val FavoriteIcon: ImageVector by lazy {
    icon("Favorite") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 21f); lineTo(10.6f, 19.7f); curveTo(5.4f, 15f, 2f, 12f, 2f, 8.5f)
            curveTo(2f, 5.4f, 4.4f, 3f, 7.5f, 3f); curveTo(9.2f, 3f, 10.9f, 3.8f, 12f, 5.1f)
            curveTo(13.1f, 3.8f, 14.8f, 3f, 16.5f, 3f); curveTo(19.6f, 3f, 22f, 5.4f, 22f, 8.5f)
            curveTo(22f, 12f, 18.6f, 15f, 13.4f, 19.7f); close()
        }
    }
}

val FavoriteBorderIcon: ImageVector by lazy {
    icon("FavoriteBorder") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16.5f, 4f); curveTo(14.9f, 4f, 13.3f, 4.8f, 12f, 6f); curveTo(10.7f, 4.8f, 9.1f, 4f, 7.5f, 4f)
            curveTo(4.4f, 4f, 2f, 6.4f, 2f, 9.5f); curveTo(2f, 13f, 5.4f, 16f, 10.6f, 20.7f)
            lineTo(12f, 22f); lineTo(13.4f, 20.7f); curveTo(18.6f, 16f, 22f, 13f, 22f, 9.5f)
            curveTo(22f, 6.4f, 19.6f, 4f, 16.5f, 4f); close()
            moveTo(12f, 19f); lineTo(11.1f, 18.2f); curveTo(6.4f, 13.9f, 4f, 11.7f, 4f, 9.5f)
            curveTo(4f, 7.5f, 5.5f, 6f, 7.5f, 6f); curveTo(9f, 6f, 10.5f, 6.8f, 11.3f, 8f)
            lineTo(12.7f, 8f); curveTo(13.5f, 6.8f, 15f, 6f, 16.5f, 6f); curveTo(18.5f, 6f, 20f, 7.5f, 20f, 9.5f)
            curveTo(20f, 11.7f, 17.6f, 13.9f, 12.9f, 18.2f); close()
        }
    }
}

val HomeIcon: ImageVector by lazy {
    icon("Home") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 3f); lineTo(2f, 12f); lineTo(5f, 12f); lineTo(5f, 21f); lineTo(10f, 21f)
            lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 21f); lineTo(19f, 21f); lineTo(19f, 12f)
            lineTo(22f, 12f); close()
        }
    }
}

val SearchIcon: ImageVector by lazy {
    icon("Search") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14f); horizontalLineTo(14.7f); lineTo(14.4f, 13.7f)
            curveTo(15.4f, 12.6f, 16f, 11.1f, 16f, 9.5f); curveTo(16f, 5.9f, 13.1f, 3f, 9.5f, 3f)
            curveTo(5.9f, 3f, 3f, 5.9f, 3f, 9.5f); curveTo(3f, 13.1f, 5.9f, 16f, 9.5f, 16f)
            curveTo(11.1f, 16f, 12.6f, 15.4f, 13.7f, 14.4f); lineTo(14f, 14.7f); verticalLineTo(15.5f)
            lineTo(19f, 20.5f); lineTo(20.5f, 19f); close()
            moveTo(9.5f, 14f); curveTo(7f, 14f, 5f, 12f, 5f, 9.5f); curveTo(5f, 7f, 7f, 5f, 9.5f, 5f)
            curveTo(12f, 5f, 14f, 7f, 14f, 9.5f); curveTo(14f, 12f, 12f, 14f, 9.5f, 14f); close()
        }
    }
}

val LibraryIcon: ImageVector by lazy {
    icon("Library") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 6f); lineTo(5f, 6f); lineTo(5f, 20f); lineTo(3f, 20f); close()
            moveTo(7f, 4f); lineTo(9f, 4f); lineTo(9f, 20f); lineTo(7f, 20f); close()
            moveTo(11f, 8f); lineTo(13f, 8f); lineTo(13f, 20f); lineTo(11f, 20f); close()
            moveTo(16.5f, 7.7f); lineTo(21f, 20f); lineTo(18.9f, 20f); lineTo(15.3f, 9.9f); close()
        }
    }
}

val SettingsIcon: ImageVector by lazy {
    icon("Settings") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.1f, 13f); curveTo(19.2f, 12.5f, 19.3f, 12f, 19.3f, 11.5f)
            curveTo(19.3f, 11f, 19.2f, 10.5f, 19.1f, 10f); lineTo(20.8f, 8.7f); lineTo(19.1f, 5.8f)
            lineTo(17.1f, 6.6f); curveTo(16.3f, 6f, 15.5f, 5.5f, 14.6f, 5.2f); lineTo(14.3f, 3f)
            lineTo(10.7f, 3f); lineTo(10.4f, 5.2f); curveTo(9.5f, 5.5f, 8.7f, 6f, 7.9f, 6.6f)
            lineTo(5.9f, 5.8f); lineTo(4.2f, 8.7f); lineTo(5.9f, 10f); curveTo(5.8f, 10.5f, 5.7f, 11f, 5.7f, 11.5f)
            curveTo(5.7f, 12f, 5.8f, 12.5f, 5.9f, 13f); lineTo(4.2f, 14.3f); lineTo(5.9f, 17.2f)
            lineTo(7.9f, 16.4f); curveTo(8.7f, 17f, 9.5f, 17.5f, 10.4f, 17.8f); lineTo(10.7f, 20f)
            lineTo(14.3f, 20f); lineTo(14.6f, 17.8f); curveTo(15.5f, 17.5f, 16.3f, 17f, 17.1f, 16.4f)
            lineTo(19.1f, 17.2f); lineTo(20.8f, 14.3f); close()
            moveTo(12.5f, 15f); curveTo(10.6f, 15f, 9f, 13.4f, 9f, 11.5f)
            curveTo(9f, 9.6f, 10.6f, 8f, 12.5f, 8f); curveTo(14.4f, 8f, 16f, 9.6f, 16f, 11.5f)
            curveTo(16f, 13.4f, 14.4f, 15f, 12.5f, 15f); close()
        }
    }
}

val RadioIcon: ImageVector by lazy {
    icon("Radio") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3.2f, 7f); lineTo(17.2f, 4.6f); lineTo(17.6f, 6.6f); lineTo(3.6f, 9f); close()
            moveTo(7f, 6.4f); lineTo(16f, 4.7f); lineTo(15.7f, 3.5f); lineTo(13.7f, 3.9f); close()
            moveTo(20f, 10f); verticalLineTo(20f); curveTo(20f, 20.6f, 19.6f, 21f, 19f, 21f)
            horizontalLineTo(5f); curveTo(4.4f, 21f, 4f, 20.6f, 4f, 20f); verticalLineTo(10f)
            curveTo(4f, 9.4f, 4.4f, 9f, 5f, 9f); horizontalLineTo(19f); curveTo(19.6f, 9f, 20f, 9.4f, 20f, 10f); close()
            moveTo(11f, 12f); curveTo(11f, 11.4f, 10.6f, 11f, 10f, 11f); horizontalLineTo(6f)
            curveTo(5.4f, 11f, 5f, 11.4f, 5f, 12f); verticalLineTo(13f); curveTo(5f, 13.6f, 5.4f, 14f, 6f, 14f)
            horizontalLineTo(10f); curveTo(10.6f, 14f, 11f, 13.6f, 11f, 13f); close()
            moveTo(16.5f, 14.5f); curveTo(15.7f, 14.5f, 15f, 15.2f, 15f, 16f)
            curveTo(15f, 16.8f, 15.7f, 17.5f, 16.5f, 17.5f); curveTo(17.3f, 17.5f, 18f, 16.8f, 18f, 16f)
            curveTo(18f, 15.2f, 17.3f, 14.5f, 16.5f, 14.5f); close()
        }
    }
}

val NewReleasesIcon: ImageVector by lazy {
    icon("NewReleases") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f); lineTo(13.5f, 4f); lineTo(16f, 3f); lineTo(16f, 5.5f); lineTo(18.5f, 6f)
            lineTo(17.5f, 8.3f); lineTo(19.8f, 9.8f); lineTo(18f, 11.7f); lineTo(19f, 14f); lineTo(16.5f, 14.5f)
            lineTo(16.5f, 17f); lineTo(14f, 16.5f); lineTo(12.7f, 18.6f); lineTo(11f, 17f); lineTo(8.5f, 18.5f)
            lineTo(8f, 16f); lineTo(5.5f, 15.5f); lineTo(6.5f, 13.2f); lineTo(4.5f, 11.5f); lineTo(6f, 9.6f)
            lineTo(5f, 7f); lineTo(7.5f, 6.5f); lineTo(7.5f, 4f); lineTo(10f, 5f); close()
            moveTo(11f, 7f); verticalLineTo(13f); horizontalLineTo(13f); verticalLineTo(7f); close()
            moveTo(11f, 14f); verticalLineTo(16f); horizontalLineTo(13f); verticalLineTo(14f); close()
        }
    }
}

val HistoryIcon: ImageVector by lazy {
    icon("History") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(13f, 3f); curveTo(8f, 3f, 4f, 7f, 4f, 12f); lineTo(2f, 12f); lineTo(5f, 15f)
            lineTo(5.1f, 15.1f); lineTo(8f, 12f); lineTo(6f, 12f); curveTo(6f, 8.1f, 9.1f, 5f, 13f, 5f)
            curveTo(16.9f, 5f, 20f, 8.1f, 20f, 12f); curveTo(20f, 15.9f, 16.9f, 19f, 13f, 19f)
            curveTo(11.1f, 19f, 9.3f, 18.2f, 8.1f, 16.9f); lineTo(6.7f, 18.3f); curveTo(8.3f, 19.9f, 10.5f, 21f, 13f, 21f)
            curveTo(18f, 21f, 22f, 17f, 22f, 12f); curveTo(22f, 7f, 18f, 3f, 13f, 3f); close()
            moveTo(12f, 8f); verticalLineTo(13f); lineTo(16.3f, 15.5f); lineTo(17f, 14.2f); lineTo(13.7f, 12.3f)
            verticalLineTo(8f); close()
        }
    }
}

val AlbumIcon: ImageVector by lazy {
    icon("Album") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 3f); curveTo(7f, 3f, 3f, 7f, 3f, 12f); curveTo(3f, 17f, 7f, 21f, 12f, 21f)
            curveTo(17f, 21f, 21f, 17f, 21f, 12f); curveTo(21f, 7f, 17f, 3f, 12f, 3f); close()
            moveTo(12f, 16f); curveTo(9.8f, 16f, 8f, 14.2f, 8f, 12f); curveTo(8f, 9.8f, 9.8f, 8f, 12f, 8f)
            curveTo(14.2f, 8f, 16f, 9.8f, 16f, 12f); curveTo(16f, 14.2f, 14.2f, 16f, 12f, 16f); close()
            moveTo(12f, 11f); curveTo(11.4f, 11f, 11f, 11.4f, 11f, 12f); curveTo(11f, 12.6f, 11.4f, 13f, 12f, 13f)
            curveTo(12.6f, 13f, 13f, 12.6f, 13f, 12f); curveTo(13f, 11.4f, 12.6f, 11f, 12f, 11f); close()
        }
    }
}

val ArtistIcon: ImageVector by lazy {
    icon("Artist") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 12f); curveTo(14.2f, 12f, 16f, 10.2f, 16f, 8f); curveTo(16f, 5.8f, 14.2f, 4f, 12f, 4f)
            curveTo(9.8f, 4f, 8f, 5.8f, 8f, 8f); curveTo(8f, 10.2f, 9.8f, 12f, 12f, 12f); close()
            moveTo(12f, 14f); curveTo(9.3f, 14f, 4f, 15.3f, 4f, 18f); verticalLineTo(20f)
            horizontalLineTo(20f); verticalLineTo(18f); curveTo(20f, 15.3f, 14.7f, 14f, 12f, 14f); close()
        }
    }
}

val QueueIcon: ImageVector by lazy {
    icon("Queue") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 5f); lineTo(15f, 5f); lineTo(15f, 7f); lineTo(3f, 7f); close()
            moveTo(3f, 11f); lineTo(15f, 11f); lineTo(15f, 13f); lineTo(3f, 13f); close()
            moveTo(3f, 17f); lineTo(11f, 17f); lineTo(11f, 19f); lineTo(3f, 19f); close()
            moveTo(17f, 12f); lineTo(22f, 15f); lineTo(17f, 18f); close()
        }
    }
}

val DiscordIcon: ImageVector by lazy {
    icon("Discord") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.3f, 5.3f); curveTo(17.9f, 4.7f, 16.5f, 4.3f, 15f, 4f); curveTo(14.8f, 4.4f, 14.6f, 4.8f, 14.5f, 5.1f)
            curveTo(13f, 4.8f, 11.4f, 4.8f, 9.9f, 5.1f); curveTo(9.7f, 4.8f, 9.5f, 4.4f, 9.3f, 4f)
            curveTo(7.8f, 4.3f, 6.4f, 4.7f, 5f, 5.3f); curveTo(2.4f, 9.1f, 1.7f, 12.8f, 2f, 16.5f)
            curveTo(3.6f, 17.7f, 5.2f, 18.4f, 6.8f, 18.9f); curveTo(7.2f, 18.3f, 7.6f, 17.7f, 7.9f, 17.1f)
            curveTo(7.3f, 16.9f, 6.7f, 16.6f, 6.2f, 16.3f); curveTo(6.4f, 16.2f, 6.5f, 16.1f, 6.6f, 15.9f)
            curveTo(9.8f, 17.4f, 13.5f, 17.4f, 16.7f, 15.9f); curveTo(16.8f, 16.1f, 17f, 16.2f, 17.1f, 16.3f)
            curveTo(16.6f, 16.6f, 16f, 16.9f, 15.4f, 17.1f); curveTo(15.7f, 17.7f, 16.1f, 18.3f, 16.5f, 18.9f)
            curveTo(18.1f, 18.4f, 19.7f, 17.7f, 21.3f, 16.5f); curveTo(21.6f, 12.2f, 20.5f, 8.5f, 19.3f, 5.3f); close()
            moveTo(9f, 14.2f); curveTo(8.1f, 14.2f, 7.3f, 13.3f, 7.3f, 12.2f); curveTo(7.3f, 11.1f, 8f, 10.2f, 9f, 10.2f)
            curveTo(10f, 10.2f, 10.7f, 11.1f, 10.7f, 12.2f); curveTo(10.7f, 13.3f, 10f, 14.2f, 9f, 14.2f); close()
            moveTo(15.4f, 14.2f); curveTo(14.5f, 14.2f, 13.7f, 13.3f, 13.7f, 12.2f)
            curveTo(13.7f, 11.1f, 14.4f, 10.2f, 15.4f, 10.2f); curveTo(16.4f, 10.2f, 17.1f, 11.1f, 17.1f, 12.2f)
            curveTo(17.1f, 13.3f, 16.4f, 14.2f, 15.4f, 14.2f); close()
        }
    }
}

val LastFmIcon: ImageVector by lazy {
    icon("LastFm") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 8.4f); curveTo(20f, 6.5f, 18.5f, 5f, 16.6f, 5f); curveTo(14.9f, 5f, 13.5f, 6.2f, 13.2f, 7.8f)
            lineTo(11f, 13.2f); curveTo(10.7f, 14f, 10f, 14.4f, 9.2f, 14.4f); curveTo(8f, 14.4f, 7f, 13.4f, 7f, 12.2f)
            curveTo(7f, 11.6f, 7.2f, 11.1f, 7.6f, 10.7f); lineTo(10f, 8f); curveTo(10.3f, 7.6f, 10.5f, 7.1f, 10.5f, 6.6f)
            curveTo(10.5f, 5.5f, 9.6f, 4.6f, 8.5f, 4.6f); curveTo(7.4f, 4.6f, 6.5f, 5.5f, 6.5f, 6.6f)
            lineTo(4f, 17f); lineTo(6.5f, 17f); lineTo(8.4f, 10.9f); curveTo(8f, 11.6f, 7.8f, 12.4f, 7.8f, 13.2f)
            curveTo(7.8f, 15.4f, 9.6f, 17.2f, 11.8f, 17.2f); curveTo(13.5f, 17.2f, 15f, 16.1f, 15.6f, 14.5f)
            lineTo(17.6f, 9.4f); verticalLineTo(17f); lineTo(20f, 17f); close()
        }
    }
}

val InfoIcon: ImageVector by lazy {
    icon("Info") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f); curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f); curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
            curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f); curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f); close()
            moveTo(13f, 17f); horizontalLineTo(11f); verticalLineTo(11f); horizontalLineTo(13f); close()
            moveTo(13f, 9f); horizontalLineTo(11f); verticalLineTo(7f); horizontalLineTo(13f); close()
        }
    }
}
