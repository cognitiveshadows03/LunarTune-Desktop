/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.app

import dev.citali.lunartune.desktop.catalog.TrackCatalog
import dev.citali.lunartune.desktop.lyrics.LyricsRepository
import dev.citali.lunartune.desktop.model.LyricsDocument
import dev.citali.lunartune.desktop.model.PlaybackState
import dev.citali.lunartune.desktop.model.Track
import dev.citali.lunartune.desktop.player.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A titled row of tracks shown on the home screen. */
data class HomeSection(
    val title: String,
    val subtitle: String,
    val tracks: List<Track>,
)

/** Integration toggles held in memory; the UI reads/writes through these flows. */
data class IntegrationsState(
    val discordEnabled: Boolean = false,
    val discordApplicationId: String = "",
    val lastfmEnabled: Boolean = false,
    val lastfmSessionKey: String = "",
    val lastfmSendNowPlaying: Boolean = true,
)

/**
 * Shared state holder. Everything in here is common code: the platform supplies a
 * catalog, a lyrics repository and a player, and the UI is the same on every target.
 */
class DesktopAppViewModel(
    private val catalog: TrackCatalog,
    private val lyricsRepository: LyricsRepository,
    private val player: PlayerController,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private val _lyrics = MutableStateFlow<LyricsDocument?>(null)
    val lyrics: StateFlow<LyricsDocument?> = _lyrics.asStateFlow()

    private val _lyricsStatus = MutableStateFlow("Start a song to see lyrics")
    val lyricsStatus: StateFlow<String> = _lyricsStatus.asStateFlow()

    val playback: StateFlow<PlaybackState> = player.state

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _liked = MutableStateFlow<Set<String>>(emptySet())
    val liked: StateFlow<Set<String>> = _liked.asStateFlow()

    private val _history = MutableStateFlow<List<Track>>(emptyList())
    val history: StateFlow<List<Track>> = _history.asStateFlow()

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections: StateFlow<List<HomeSection>> = _homeSections.asStateFlow()

    private val _homeLoading = MutableStateFlow(false)
    val homeLoading: StateFlow<Boolean> = _homeLoading.asStateFlow()

    private val _integrations = MutableStateFlow(IntegrationsState())
    val integrations: StateFlow<IntegrationsState> = _integrations.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null
    private var homeJob: kotlinx.coroutines.Job? = null
    private var wasPlaying = false

    init {
        // Load (and cache) lyrics whenever the playing track changes.
        scope.launch {
            player.state
                .map { it.track }
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { track ->
                    if (track == null) {
                        _lyrics.value = null
                        _lyricsStatus.value = "Start a song to see lyrics"
                        return@collect
                    }
                    _lyrics.value = null
                    _lyricsStatus.value = "Loading lyrics…"
                    val document =
                        withContext(Dispatchers.Default) {
                            runCatching { lyricsRepository.load(track) }.getOrNull()
                        }
                    _lyrics.value = document
                    _lyricsStatus.value =
                        when {
                            document == null -> "No lyrics found"
                            document.sync == dev.citali.lunartune.desktop.model.LyricsSync.WORD ->
                                "Word-synced · ${document.source}"
                            document.sync == dev.citali.lunartune.desktop.model.LyricsSync.LINE ->
                                "Line-synced · ${document.source}"
                            else -> "Plain lyrics · ${document.source}"
                        }
                }
        }

        // Auto-advance the queue when the current track finishes. Lavaplayer does
        // not expose an end-of-track callback through the shared interface, so we
        // watch the playback state for the playing -> stopped edge with the
        // position parked at the end of the track.
        scope.launch {
            player.state.collect { state ->
                val ended =
                    wasPlaying &&
                        !state.isPlaying &&
                        state.track != null &&
                        state.durationMs > 1_000L &&
                        state.positionMs >= (state.durationMs - 400L).coerceAtLeast(0L) &&
                        state.error == null
                wasPlaying = state.isPlaying
                if (ended) playNext()
            }
        }

        loadHome()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun search() {
        val value = _query.value.trim()
        if (value.isBlank()) return
        searchJob?.cancel()
        searchJob =
            scope.launch {
                _isSearching.value = true
                _catalogError.value = null
                val outcome: Result<List<Track>> =
                    withContext(Dispatchers.Default) {
                        try {
                            catalog.search(value)
                        } catch (failure: Throwable) {
                            Result.failure(failure)
                        }
                    }
                outcome
                    .onSuccess {
                        _results.value = it
                        if (it.isNotEmpty()) playFromList(it, 0, autoplay = false)
                    }
                    .onFailure { _catalogError.value = it.message ?: "Search failed" }
                _isSearching.value = false
            }
    }

    /** Play a track as part of a list, setting the queue so Next/Previous work. */
    fun playFromList(list: List<Track>, index: Int, autoplay: Boolean = true) {
        if (index !in list.indices) return
        _queue.value = list
        _queueIndex.value = index
        if (autoplay) play(list[index])
    }

    fun play(track: Track) {
        val current = _queue.value
        val idx = current.indexOfFirst { it.id == track.id }
        if (idx >= 0) {
            _queueIndex.value = idx
        } else if (current.isEmpty()) {
            _queue.value = listOf(track)
            _queueIndex.value = 0
        }
        player.play(track)
        recordHistory(track)
    }

    fun playNext() {
        val list = _queue.value
        val idx = _queueIndex.value + 1
        if (list.isEmpty() || idx !in list.indices) return
        _queueIndex.value = idx
        player.play(list[idx])
        recordHistory(list[idx])
    }

    fun playPrevious() {
        val list = _queue.value
        val idx = _queueIndex.value - 1
        if (list.isEmpty() || idx !in list.indices) return
        _queueIndex.value = idx
        player.play(list[idx])
        recordHistory(list[idx])
    }

    fun togglePlayPause() = player.togglePlayPause()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun toggleLike(track: Track) {
        _liked.value =
            if (track.id in _liked.value) {
                _liked.value - track.id
            } else {
                _liked.value + track.id
            }
    }

    fun isLiked(track: Track): Boolean = track.id in _liked.value

    fun updateIntegrations(transform: (IntegrationsState) -> IntegrationsState) {
        _integrations.value = transform(_integrations.value)
    }

    private fun recordHistory(track: Track) {
        _history.value = (listOf(track) + _history.value.filter { it.id != track.id }).take(100)
    }

    /**
     * Populates the home feed by running a handful of preset searches through the
     * same catalog the search box uses. Real artwork, real tracks, no new API
     * surface - the catalog stays a single-method boundary.
     */
    fun loadHome() {
        homeJob?.cancel()
        homeJob =
            scope.launch {
                _homeLoading.value = true
                val sections =
                    withContext(Dispatchers.Default) {
                        HOME_QUERIES.map { (title, subtitle, query) ->
                            val tracks =
                                runCatching { catalog.search(query) }.getOrNull()?.getOrElse { emptyList() } ?: emptyList()
                            HomeSection(title, subtitle, tracks.take(12))
                        }.filter { it.tracks.isNotEmpty() }
                    }
                _homeSections.value = sections
                _homeLoading.value = false
            }
    }

    fun dispose() {
        searchJob?.cancel()
        homeJob?.cancel()
        scope.cancel()
        player.release()
    }

    private companion object {
        val HOME_QUERIES =
            listOf(
                Triple("Quick picks", "Jump back in where you left off", "top hits"),
                Triple("New releases", "Fresh out this season", "new songs 2025"),
                Triple("Made for you", "Shapes from your listening", "anime openings"),
                Triple("From your library", "Playlists you might like", "lofi hip hop beats"),
                Triple("Listen together", "Crowd favourites right now", "workout mix"),
            )
    }
}
