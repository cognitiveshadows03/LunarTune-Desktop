/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.citali.lunartune.desktop.app.DesktopAppViewModel
import dev.citali.lunartune.desktop.catalog.InnerTubeCatalog
import dev.citali.lunartune.desktop.lyrics.DesktopLyricsRepository
import dev.citali.lunartune.desktop.player.LavaplayerController
import dev.citali.lunartune.desktop.player.YouTubeStreamResolver
import dev.citali.lunartune.desktop.ui.LunarTuneApp
import dev.citali.lunartune.desktop.ui.LunarTuneDesktopTheme

fun main() =
    application {
        val viewModel = remember { createViewModel() }
        Window(
            onCloseRequest = {
                viewModel.dispose()
                exitApplication()
            },
            title = "LunarTune",
            state = rememberWindowState(width = 1280.dp, height = 820.dp),
            onKeyEvent = { event ->
                if (event.key == Key.Spacebar && event.type == KeyEventType.KeyDown) {
                    viewModel.togglePlayPause()
                    true
                } else {
                    false
                }
            },
        ) {
            LunarTuneDesktopTheme {
                LunarTuneApp(viewModel)
            }
        }
    }

private fun createViewModel(): DesktopAppViewModel =
    DesktopAppViewModel(
        catalog = InnerTubeCatalog(),
        lyricsRepository = DesktopLyricsRepository(),
        player = LavaplayerController(YouTubeStreamResolver),
    )
