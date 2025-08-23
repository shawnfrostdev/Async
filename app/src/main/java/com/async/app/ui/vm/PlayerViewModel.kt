package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.core.model.SearchResult
import com.async.domain.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Player screen
 * Manages playback state and controls
 */
class PlayerViewModel : ViewModel() {
    
    var uiState by mutableStateOf(PlayerUiState())
        private set
    
    fun playTrack(track: SearchResult) {
        uiState = uiState.copy(
            currentTrack = track,
            isPlaying = true
        )
    }
    
    fun playPause() {
        uiState = uiState.copy(isPlaying = !uiState.isPlaying)
    }
    
    fun skipNext() {
        // TODO: Implement skip next
    }
    
    fun skipPrevious() {
        // TODO: Implement skip previous
    }
    
    fun seekTo(positionMs: Long) {
        uiState = uiState.copy(currentPosition = positionMs)
    }
    
    fun toggleShuffle() {
        uiState = uiState.copy(isShuffleEnabled = !uiState.isShuffleEnabled)
    }
    
    fun toggleRepeat() {
        val newMode = when (uiState.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        uiState = uiState.copy(repeatMode = newMode)
    }
    
    fun setQueue(tracks: List<SearchResult>, startIndex: Int = 0) {
        uiState = uiState.copy(
            queue = tracks,
            currentTrack = tracks.getOrNull(startIndex)
        )
    }
    
    fun removeFromQueue(index: Int) {
        val newQueue = uiState.queue.toMutableList()
        if (index in newQueue.indices) {
            newQueue.removeAt(index)
            uiState = uiState.copy(queue = newQueue)
        }
    }
}

/**
 * UI state for the Player screen
 */
data class PlayerUiState(
    val currentTrack: SearchResult? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<SearchResult> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
)

/**
 * Repeat mode enumeration
 */
enum class RepeatMode {
    OFF, ONE, ALL
} 
