/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.lyrics

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Direct client for the BetterLyrics API, mirroring the Android app's app-layer
 * client (the bundled SDK still points at the legacy host and cannot see a 401).
 *
 * The API is cache-first: a cached query is free, an uncached one answers 401
 * until an API key is issued. Album is deliberately not sent (it turns cache
 * hits into misses) and duration is sent whenever known.
 *
 * The 401 backoff is **per endpoint**: one uncached endpoint must not silence
 * the other two, which may well have a cached entry for the same song.
 */
internal object BetterLyricsClient {
    private const val BASE_URL = "https://api.betterlyrics.org/"
    private val endpoints = listOf("getLyrics", "kugou/getLyrics", "qq/getLyrics")
    private const val AUTH_BACKOFF_MS = 5 * 60 * 1000L

    private val blockedUntil = ConcurrentHashMap<String, Long>()
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 20_000
            }
        }
    }

    suspend fun fetchAll(
        title: String,
        artist: String,
        durationSeconds: Int,
    ): List<String> {
        val cleanTitle = title.trim()
        val cleanArtist = artist.trim()
        if (cleanTitle.isEmpty() || cleanArtist.isEmpty()) return emptyList()
        return endpoints.mapNotNull { endpoint ->
            fetch(endpoint, cleanTitle, cleanArtist, durationSeconds)
        }
    }

    private suspend fun fetch(
        endpoint: String,
        title: String,
        artist: String,
        durationSeconds: Int,
    ): String? {
        if ((blockedUntil[endpoint] ?: 0L) > System.currentTimeMillis()) return null
        return try {
            val response =
                client.get(BASE_URL + endpoint) {
                    parameter("s", title)
                    parameter("a", artist)
                    // Album omitted on purpose - see the class comment.
                    if (durationSeconds > 0) parameter("d", durationSeconds)
                }
            when (val status = response.status.value) {
                401 -> {
                    blockedUntil[endpoint] = System.currentTimeMillis() + AUTH_BACKOFF_MS
                    null
                }

                in 200..299 -> decode(response.bodyAsText())
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Unwrap raw TTML/QRC/LRC or a JSON envelope that nests it under a known key. */
    internal fun decodePayload(body: String): String? = decode(body)

    private fun decode(body: String): String? {
        val raw = body.removePrefix("\uFEFF").trim()
        if (isLyricPayload(raw)) return raw
        val root = runCatching { json.parseToJsonElement(raw) }.getOrNull() ?: return null
        return unwrap(root, depth = 0)
    }

    private fun unwrap(
        element: JsonElement,
        depth: Int,
    ): String? {
        if (depth > 2) return null
        return when (element) {
            is JsonObject -> {
                val payload =
                    element["ttml"]
                        ?: element["lyrics"]
                        ?: element["data"]
                        ?: element["result"]
                        ?: element["response"]
                        ?: return null
                unwrap(payload, depth + 1)
            }

            is JsonPrimitive -> {
                val content = element.contentOrNull ?: return null
                if (isLyricPayload(content)) {
                    content
                } else {
                    val nested = runCatching { json.parseToJsonElement(content) }.getOrNull() ?: return null
                    unwrap(nested, depth + 1)
                }
            }

            else -> null
        }
    }

    private fun isLyricPayload(text: String): Boolean {
        val trimmed = text.trimStart()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith("<")) {
            return trimmed.startsWith("<?xml") ||
                trimmed.contains("<tt", ignoreCase = true) ||
                trimmed.contains("QrcInfos", ignoreCase = true)
        }
        return trimmed.startsWith("[") && (trimmed.contains("]") || trimmed.contains("("))
    }
}
