/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.abs

/**
 * Album-art tile. Loads the track's thumbnail through Coil3 when one is
 * available; otherwise renders a deterministic two-stop gradient seeded from
 * the title so every fallback tile is distinct.
 *
 * Pass [fill] = true (and ignore [size]) to make the artwork fill its parent -
 * used by the hero card and the now-playing cover. Otherwise [size] pins the
 * tile to a square dimension.
 */
@Composable
fun Artwork(
    title: String,
    thumbnailUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    fill: Boolean = false,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val sizing = if (fill) Modifier.fillMaxSize() else Modifier.size(size)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.then(sizing).clip(shape),
    ) {
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(gradientFor(title), shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Deterministic gradient used both for artwork fallbacks and the home "Made for
 * you" cards. Two hues derived from the title hash, blended diagonally.
 */
fun gradientFor(seed: String): Brush {
    val h = abs(seed.hashCode())
    val hueA = (h and 0xFF) / 255f
    val hueB = ((h shr 8) and 0xFF) / 255f
    val base = Color.hsv(hueA * 360f, 0.55f, 0.42f)
    val accent = Color.hsv(hueB * 360f, 0.60f, 0.56f)
    return Brush.linearGradient(colors = listOf(base, accent))
}
