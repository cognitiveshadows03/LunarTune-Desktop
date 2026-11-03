/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LunarTuneSeed = Color(0xFFED5564)

/**
 * One seed colour, its shades everywhere - the same idea as the Material You
 * tonal palettes used by the Android app, but expressed with plain Compose lerps
 * so it works unchanged in common code.
 */
@Composable
fun LunarTuneDesktopTheme(
    seed: Color = LunarTuneSeed,
    content: @Composable () -> Unit,
) {
    val scheme =
        remember(seed) {
            val primary = shade(seed, Color.White, 0.18f)
            val onSurface = shade(seed, Color.White, 0.86f)
            darkColorScheme(
                primary = primary,
                onPrimary = Color.Black,
                primaryContainer = shade(seed, Color.Black, 0.38f),
                onPrimaryContainer = shade(seed, Color.White, 0.55f),
                secondary = shade(seed, Color.White, 0.05f),
                onSecondary = Color.Black,
                secondaryContainer = shade(seed, Color.Black, 0.52f),
                onSecondaryContainer = shade(seed, Color.White, 0.72f),
                tertiary = shade(seed, Color.White, 0.28f),
                onTertiary = Color.Black,
                background = shade(seed, Color.Black, 0.93f),
                onBackground = onSurface,
                surface = shade(seed, Color.Black, 0.90f),
                onSurface = onSurface,
                surfaceVariant = shade(seed, Color.Black, 0.78f),
                onSurfaceVariant = shade(seed, Color.White, 0.60f),
                outline = shade(seed, Color.White, 0.18f),
            )
        }
    val typography =
        remember {
            Typography(
                headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
                headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
                titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
                titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
                bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
                bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
                bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium),
            )
        }
    MaterialTheme(colorScheme = scheme, typography = typography, content = content)
}

private fun shade(
    seed: Color,
    towards: Color,
    amount: Float,
): Color = lerp(seed, towards, amount)

/** Mood-chip accent colours used by the home screen's Relax / Workout / Party row. */
object MoodPalette {
    val relax = Color(0xFF6B8E9E)
    val workout = Color(0xFFE0564E)
    val party = Color(0xFFC962B4)
    val focus = Color(0xFF6F7BC4)
    val sleep = Color(0xFF4A5C7A)
}
