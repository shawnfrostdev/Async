package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.async.core.model.SearchResult
import com.async.domain.model.Track
import com.async.app.di.AppModule
import com.async.playback.service.PlaybackManager
import com.async.app.ui.vm.RepeatMode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * ViewModel for the Player screen with real PlaybackManager integration
 */
@UnstableApi
class PlayerViewModel : ViewModel() {
    
    private val playbackManager: PlaybackManager = AppModule.getPlaybackManager()
    
    var uiState by mutableStateOf(PlayerUiState())
        private set
    
    init {
        // Observe playback manager state changes
        setupPlaybackObservers()
    }
    
    /**
     * Setup observers for playback manager state
     */
    private fun setupPlaybackObservers() {
        // Observe current track
        playbackManager.currentTrack
            .onEach { track ->
                uiState = uiState.copy(currentTrack = track)
                logcat("PlayerViewModel") { "Current track updated: ${track?.title}" }
            }
            .launchIn(viewModelScope)
        
        // Observe playing state
        playbackManager.isPlaying
            .onEach { isPlaying ->
                uiState = uiState.copy(isPlaying = isPlaying)
                logcat("PlayerViewModel") { "Playing state: $isPlaying" }
            }
            .launchIn(viewModelScope)
        
        // Observe position
        playbackManager.position
            .onEach { position ->
                uiState = uiState.copy(currentPosition = position)
            }
            .launchIn(viewModelScope)
        
        // Observe duration
        playbackManager.duration
            .onEach { duration ->
                uiState = uiState.copy(duration = duration)
                logcat("PlayerViewModel") { "Duration updated: ${duration}ms" }
            }
            .launchIn(viewModelScope)
        
        // Observe loading state
        playbackManager.isLoading
            .onEach { isLoading ->
                uiState = uiState.copy(isLoading = isLoading)
                logcat("PlayerViewModel") { "Loading state: $isLoading" }
            }
            .launchIn(viewModelScope)
        
        // Observe errors
        playbackManager.error
            .onEach { error ->
                uiState = uiState.copy(error = error)
                if (error != null) {
                    logcat("PlayerViewModel") { "Playback error: $error" }
                }
            }
            .launchIn(viewModelScope)
        
        // Observe queue
        playbackManager.queue
            .onEach { queue ->
                uiState = uiState.copy(queue = queue)
                logcat("PlayerViewModel") { "Queue updated: ${queue.size} tracks" }
            }
            .launchIn(viewModelScope)
        
        // Observe current index
        playbackManager.currentIndex
            .onEach { index ->
                uiState = uiState.copy(currentIndex = index)
            }
            .launchIn(viewModelScope)
        
        // Observe shuffle state
        playbackManager.shuffleEnabled
            .onEach { shuffleEnabled ->
                uiState = uiState.copy(isShuffleEnabled = shuffleEnabled)
                logcat("PlayerViewModel") { "Shuffle: $shuffleEnabled" }
            }
            .launchIn(viewModelScope)
        
        // Observe repeat mode
        playbackManager.repeatMode
            .onEach { repeatMode ->
                val uiRepeatMode = when (repeatMode) {
                    com.async.playback.service.RepeatMode.OFF -> RepeatMode.OFF
                    com.async.playback.service.RepeatMode.ALL -> RepeatMode.ALL
                    com.async.playback.service.RepeatMode.ONE -> RepeatMode.ONE
                }
                uiState = uiState.copy(repeatMode = uiRepeatMode)
                logcat("PlayerViewModel") { "Repeat mode: $repeatMode" }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Play a track
     */
    fun playTrack(track: SearchResult) {
        logcat("PlayerViewModel") { "Playing track: ${track.title} by ${track.artist}" }
        
        viewModelScope.launch {
            try {
                playbackManager.playTrack(track)
            } catch (e: Exception) {
                logcat("PlayerViewModel") { "Error playing track: ${e.message}" }
                uiState = uiState.copy(
                    error = "Error playing track: ${e.message}",
                    isPlaying = false
                )
            }
        }
    }
    
    /**
     * Play/pause toggle
     */
    fun playPause() {
        if (uiState.isPlaying) {
            playbackManager.pause()
        } else {
            playbackManager.resume()
        }
        logcat("PlayerViewModel") { "Play/pause toggled" }
    }
    
    /**
     * Skip to next track
     */
    fun skipNext() {
        viewModelScope.launch {
            playbackManager.skipToNext()
            logcat("PlayerViewModel") { "Skipped to next track" }
        }
    }
    
    /**
     * Skip to previous track
     */
    fun skipPrevious() {
        viewModelScope.launch {
            playbackManager.skipToPrevious()
            logcat("PlayerViewModel") { "Skipped to previous track" }
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
        logcat("PlayerViewModel") { "Seeked to ${positionMs}ms" }
    }
    
    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        playbackManager.toggleShuffle()
        logcat("PlayerViewModel") { "Shuffle toggled" }
    }
    
    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        playbackManager.toggleRepeat()
        logcat("PlayerViewModel") { "Repeat mode toggled" }
    }
    
    /**
     * Set queue and play from index
     */
    fun setQueue(tracks: List<SearchResult>, startIndex: Int = 0) {
        logcat("PlayerViewModel") { "Setting queue with ${tracks.size} tracks, starting at $startIndex" }
        viewModelScope.launch {
            playbackManager.setQueue(tracks, startIndex)
        }
    }
    
    /**
     * Add track to queue
     */
    fun addToQueue(track: SearchResult) {
        playbackManager.addToQueue(track)
        logcat("PlayerViewModel") { "Added ${track.title} to queue" }
    }
    
    /**
     * Remove track from queue
     */
    fun removeFromQueue(index: Int) {
        if (index in uiState.queue.indices) {
            playbackManager.removeFromQueue(index)
            logcat("PlayerViewModel") { "Removed track at index $index from queue" }
        }
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        playbackManager.stop()
        logcat("PlayerViewModel") { "Playback stopped" }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}

/**
 * UI state for the Player screen
 */
data class PlayerUiState(
    val currentTrack: SearchResult? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<SearchResult> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
)

/**
 * Repeat mode enumeration (UI layer)
 */
enum class RepeatMode {
    OFF,    // No repeat
    ALL,    // Repeat all tracks in queue  
    ONE     // Repeat current track
} 
