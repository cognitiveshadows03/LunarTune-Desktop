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

fun main(args: Array<String>) {
    val launchCheck = args.firstOrNull { it.startsWith("--launch-check") }
    if (launchCheck != null) {
        runLaunchCheck(launchCheck.substringAfter('=', ""))
        return
    }

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
}

/**
 * Non-visual smoke test for the packaged jlink runtime. CI runs the packaged
 * launcher with `--launch-check=<file>`: if a JDK module is missing, the JVM or
 * one of these classes fails to load and the check never reports success, instead
 * of the launcher's opaque "Failed to launch JVM" dialog on a user's machine.
 */
private fun runLaunchCheck(markerPath: String) {
    val required =
        listOf(
            "sun.misc.Unsafe", // jdk.unsupported - Skiko native interop
            "java.awt.Window", // java.desktop
            "javax.xml.parsers.DocumentBuilderFactory", // java.xml - TTML/QRC parsing
            "java.sql.DriverManager", // java.sql
            "javax.naming.InitialContext", // java.naming
            "java.lang.management.ManagementFactory", // java.management
            "java.net.http.HttpClient", // java.net.http
            "java.util.prefs.Preferences", // java.prefs
        )
    val missing =
        required.filter { name ->
            runCatching { Class.forName(name, false, ClassLoader.getSystemClassLoader()) }.isFailure
        }
    val javaVersion = System.getProperty("java.version")
    val suffix = if (missing.isEmpty()) "" else " missing=${missing.joinToString(",")}"
    val report = "LUNARTUNE_LAUNCH_CHECK_OK java=$javaVersion$suffix"
    println(report)
    if (markerPath.isNotBlank()) {
        runCatching { java.io.File(markerPath).writeText(report) }
    }
    if (missing.isNotEmpty()) {
        kotlin.system.exitProcess(1)
    }
}

private fun createViewModel(): DesktopAppViewModel =
    DesktopAppViewModel(
        catalog = InnerTubeCatalog(),
        lyricsRepository = DesktopLyricsRepository(),
        player = LavaplayerController(YouTubeStreamResolver),
    )
