package app.async.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import app.async.core.model.SearchResult
import app.async.app.di.AppModule
import app.async.playback.service.PlaybackManager
import app.async.app.ui.components.lyrics.LyricsManager
import app.async.app.ui.components.lyrics.LrcLyrics
import app.async.app.ui.components.lyrics.LrcLine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import logcat.logcat

data class PlayerUiState(
    val currentTrack: SearchResult? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<SearchResult> = emptyList(),
    val currentIndex: Int = -1,
    // Lyrics state
    val lyrics: LrcLyrics? = null,
    val currentLyricLine: LrcLine? = null,
    val isLoadingLyrics: Boolean = false,
    val lyricsError: String? = null
)

enum class RepeatMode {
    OFF, ONE, ALL
}

@UnstableApi
class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: PlayerUiState get() = _uiState.value

    private val playbackManager: PlaybackManager = AppModule.getPlaybackManager()
    private val lyricsManager = LyricsManager(viewModelScope)

    init {
        // Observe playback manager state changes
        setupPlaybackObservers()

        // Collect lyrics states
        viewModelScope.launch {
            lyricsManager.currentLyrics.collect { lyrics ->
                _uiState.update { it.copy(lyrics = lyrics) }
            }
        }
        viewModelScope.launch {
            lyricsManager.currentLine.collect { line ->
                _uiState.update { it.copy(currentLyricLine = line) }
            }
        }
        viewModelScope.launch {
            lyricsManager.isLoading.collect { isLoading ->
                _uiState.update { it.copy(isLoadingLyrics = isLoading) }
            }
        }
        viewModelScope.launch {
            lyricsManager.error.collect { error ->
                _uiState.update { it.copy(lyricsError = error) }
            }
        }
    }

    private fun setupPlaybackObservers() {
        // Observe current track
        playbackManager.currentTrack
            .onEach { track ->
                _uiState.update { it.copy(currentTrack = track) }
                // Fetch lyrics when track changes
                track?.artist?.let { artist ->
                    lyricsManager.fetchLyrics(artist, track.title)
                }
                logcat("PlayerViewModel") { "Current track updated: ${track?.title}" }
            }
            .launchIn(viewModelScope)

        // Observe playing state
        playbackManager.isPlaying
            .onEach { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
                logcat("PlayerViewModel") { "Playing state: $isPlaying" }
            }
            .launchIn(viewModelScope)

        // Observe position
        playbackManager.position
            .onEach { position ->
                _uiState.update { it.copy(currentPosition = position) }
                // Update current lyric line
                lyricsManager.updateCurrentLine(position)
            }
            .launchIn(viewModelScope)

        // Observe duration
        playbackManager.duration
            .onEach { duration ->
                _uiState.update { it.copy(duration = duration) }
                logcat("PlayerViewModel") { "Duration updated: ${duration}ms" }
            }
            .launchIn(viewModelScope)

        // Observe queue
        playbackManager.queue
            .onEach { queue ->
                _uiState.update { it.copy(queue = queue) }
                logcat("PlayerViewModel") { "Queue updated: ${queue.size} tracks" }
            }
            .launchIn(viewModelScope)

        // Observe current index
        playbackManager.currentIndex
            .onEach { index ->
                _uiState.update { it.copy(currentIndex = index) }
            }
            .launchIn(viewModelScope)

        // Observe shuffle state
        playbackManager.shuffleEnabled
            .onEach { shuffleEnabled ->
                _uiState.update { it.copy(isShuffleEnabled = shuffleEnabled) }
                logcat("PlayerViewModel") { "Shuffle: $shuffleEnabled" }
            }
            .launchIn(viewModelScope)

        // Observe repeat mode
        playbackManager.repeatMode
            .onEach { repeatMode ->
                val uiRepeatMode = when (repeatMode) {
                    app.async.playback.service.RepeatMode.OFF -> RepeatMode.OFF
                    app.async.playback.service.RepeatMode.ALL -> RepeatMode.ALL
                    app.async.playback.service.RepeatMode.ONE -> RepeatMode.ONE
                }
                _uiState.update { it.copy(repeatMode = uiRepeatMode) }
                logcat("PlayerViewModel") { "Repeat mode: $repeatMode" }
            }
            .launchIn(viewModelScope)
    }

    fun playTrack(track: SearchResult) {
        logcat("PlayerViewModel") { "Playing track: ${track.title} by ${track.artist}" }
        viewModelScope.launch {
            try {
                playbackManager.playTrack(track)
            } catch (e: Exception) {
                logcat("PlayerViewModel") { "Error playing track: ${e.message}" }
                _uiState.update {
                    it.copy(lyricsError = "Error playing track: ${e.message}")
                }
            }
        }
    }

    fun playPause() {
        if (uiState.isPlaying) {
            playbackManager.pause()
        } else {
            playbackManager.resume()
        }
        logcat("PlayerViewModel") { "Play/pause toggled" }
    }

    fun seekTo(position: Long) {
        playbackManager.seekTo(position)
        logcat("PlayerViewModel") { "Seeked to ${position}ms" }
    }

    fun skipNext() {
        viewModelScope.launch {
            playbackManager.skipToNext()
            logcat("PlayerViewModel") { "Skipped to next track" }
        }
    }

    fun skipPrevious() {
        viewModelScope.launch {
            playbackManager.skipToPrevious()
            logcat("PlayerViewModel") { "Skipped to previous track" }
        }
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
        logcat("PlayerViewModel") { "Shuffle toggled" }
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
        logcat("PlayerViewModel") { "Repeat mode toggled" }
    }

    fun updateQueue(tracks: List<SearchResult>, startIndex: Int = 0) {
        logcat("PlayerViewModel") { "Setting queue with ${tracks.size} tracks, starting at $startIndex" }
        viewModelScope.launch {
            playbackManager.setQueue(tracks, startIndex)
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in uiState.queue.indices) {
            playbackManager.removeFromQueue(index)
            logcat("PlayerViewModel") { "Removed track at index $index from queue" }
        }
    }

    fun stop() {
        playbackManager.stop()
        logcat("PlayerViewModel") { "Playback stopped" }
    }
} 

