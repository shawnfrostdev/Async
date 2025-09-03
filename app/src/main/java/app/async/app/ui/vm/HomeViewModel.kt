package app.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.async.core.model.SearchResult
import app.async.domain.model.Playlist
import app.async.domain.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen
 * Manages home screen state and data loading
 */
class HomeViewModel : ViewModel() {
    
    var uiState by mutableStateOf(HomeUiState())
        private set
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            
            // TODO: Load real data from extensions
            // For now, just show empty state
            
            uiState = uiState.copy(
                isLoading = false,
                trendingTracks = emptyList(),
                recentlyPlayed = emptyList(),
                recommendations = emptyList(),
                error = "Home screen data not yet connected to extensions. Install and enable music extensions to see content."
            )
        }
    }
    
    fun onTrackClick(track: SearchResult) {
        // TODO: Handle track click
    }
    
    fun onPlaylistClick(playlist: Playlist) {
        // TODO: Handle playlist click
    }
    
    fun refresh() {
        loadHomeData()
    }
}

/**
 * UI state for the Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val trendingTracks: List<SearchResult> = emptyList(),
    val recentlyPlayed: List<SearchResult> = emptyList(),
    val recommendations: List<SearchResult> = emptyList(),
    val recentPlaylists: List<Playlist> = emptyList(),
    val error: String? = null
) 
