package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.core.model.SearchResult
import com.async.domain.model.Playlist
import com.async.domain.model.Track
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
            
            // Simulate loading delay
            delay(1000)
            
            // Create simple mock data that compiles
            val trendingTracks = createMockTracks("Trending")
            val recentlyPlayed = createMockTracks("Recent")
            val recommendations = createMockTracks("Recommended")
            
            uiState = uiState.copy(
                isLoading = false,
                trendingTracks = trendingTracks,
                recentlyPlayed = recentlyPlayed,
                recommendations = recommendations
            )
        }
    }
    
    private fun createMockTracks(prefix: String): List<SearchResult> {
        return listOf(
            SearchResult(
                id = "${prefix.lowercase()}_1",
                extensionId = "mock_extension",
                title = "$prefix Track 1",
                artist = "Mock Artist 1",
                album = "Mock Album",
                duration = 180000,
                thumbnailUrl = null
            ),
            SearchResult(
                id = "${prefix.lowercase()}_2",
                extensionId = "mock_extension",
                title = "$prefix Track 2",
                artist = "Mock Artist 2",
                album = "Mock Album",
                duration = 210000,
                thumbnailUrl = null
            )
        )
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
