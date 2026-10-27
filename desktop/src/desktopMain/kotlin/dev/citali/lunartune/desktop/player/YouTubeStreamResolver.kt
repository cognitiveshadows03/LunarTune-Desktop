/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.player

import dev.citali.lunartune.desktop.platform.YouTubeSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.innertube.NewPipeUtils
import moe.rukamori.archivetune.innertube.YouTube
import moe.rukamori.archivetune.innertube.models.YouTubeClient
import moe.rukamori.archivetune.innertube.models.response.PlayerResponse

/**
 * Resolves a directly playable audio URL for a YouTube video id.
 *
 * Built on the exact same InnerTube/NewPipe libraries the Android app uses.
 * Web clients that require a PoToken are excluded: a desktop app has no WebView
 * to mint one, so their URLs are guaranteed to fail. Each remaining client is
 * tried in turn and the `n`/signature parameters are deobfuscated by
 * [NewPipeUtils] before the URL is handed to the audio engine.
 */
object YouTubeStreamResolver {
    data class Resolved(
        val url: String,
        val userAgent: String,
        val durationMs: Long?,
    )

    private val clients: List<YouTubeClient> =
        listOf(
            YouTubeClient.VISIONOS,
            YouTubeClient.ANDROID_VR_1_65_10,
            YouTubeClient.ANDROID_VR_1_43_32,
            YouTubeClient.IOS,
            YouTubeClient.TVHTML5,
            YouTubeClient.MWEB,
        ).filterNot { it.useWebPoTokens || it.loginRequired }

    suspend fun resolve(videoId: String): Result<Resolved> =
        withContext(Dispatchers.IO) {
            YouTubeSession.ensure()
            val failures = mutableListOf<String>()

            for (client in clients) {
                val response =
                    YouTube
                        .player(
                            videoId = videoId,
                            client = client,
                            signatureTimestamp = null,
                            poToken = null,
                            setLogin = false,
                        ).getOrElse { failure ->
                            failures += "${client.clientName}: ${failure.message ?: "request failed"}"
                            continue
                        }

                if (response.playabilityStatus.status != "OK") {
                    failures += "${client.clientName}: ${response.playabilityStatus.reason ?: response.playabilityStatus.status}"
                    continue
                }

                val durationMs = response.videoDetails?.lengthSeconds?.toLongOrNull()?.times(1_000L)
                val audioFormats =
                    response.streamingData
                        ?.adaptiveFormats
                        .orEmpty()
                        .filter(::isPlayableAudio)
                        .sortedByDescending { it.bitrate }

                for (format in audioFormats.take(4)) {
                    val url =
                        NewPipeUtils
                            .getStreamUrl(format = format, videoId = videoId, client = client)
                            .getOrNull()
                    if (!url.isNullOrBlank()) {
                        return@withContext Result.success(
                            Resolved(url = url, userAgent = client.userAgent, durationMs = durationMs),
                        )
                    }
                }
                failures += "${client.clientName}: no playable audio format"
            }

            Result.failure(
                IllegalStateException("Could not resolve an audio stream (${failures.joinToString("; ")})"),
            )
        }

    private fun isPlayableAudio(format: PlayerResponse.StreamingData.Format): Boolean =
        format.isAudio &&
            (format.url != null || format.signatureCipher != null || format.cipher != null)
}
