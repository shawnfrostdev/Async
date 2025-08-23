package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Library screen
 * Manages library state and data loading
 */
class LibraryViewModel : ViewModel() {
    
    var uiState by mutableStateOf(LibraryUiState())
        private set
    
    init {
        loadLibraryData()
    }
    
    private fun loadLibraryData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            
            // Simulate loading delay
            delay(1000)
            
            // Create simple mock data
            val playlists = createMockPlaylists()
            val likedTracks = createMockTracks()
            
            uiState = uiState.copy(
                isLoading = false,
                playlists = playlists,
                likedTracks = likedTracks
            )
        }
    }
    
    private fun createMockPlaylists(): List<Playlist> {
        return listOf(
            Playlist(
                id = 1L,
                name = "Liked Songs",
                description = "Your favorite tracks",
                trackCount = 42,
                totalDuration = 10800000L,
                dateCreated = System.currentTimeMillis() - 7776000000L,
                lastModified = System.currentTimeMillis()
            ),
            Playlist(
                id = 2L,
                name = "Chill Vibes",
                description = "Relaxing music",
                trackCount = 28,
                totalDuration = 6720000L,
                dateCreated = System.currentTimeMillis() - 2592000000L,
                lastModified = System.currentTimeMillis() - 86400000L
            )
        )
    }
    
    private fun createMockTracks(): List<Track> {
        return listOf(
            Track(
                id = 1L,
                externalId = "track_1",
                extensionId = "mock_extension",
                title = "Library Track 1",
                artist = "Mock Artist 1",
                album = "Mock Album 1",
                duration = 180000L,
                thumbnailUrl = null,
                streamUrl = "https://example.com/track1.mp3"
            ),
            Track(
                id = 2L,
                externalId = "track_2",
                extensionId = "mock_extension",
                title = "Library Track 2",
                artist = "Mock Artist 2",
                album = "Mock Album 2",
                duration = 210000L,
                thumbnailUrl = null,
                streamUrl = "https://example.com/track2.mp3"
            )
        )
    }
    
    fun onPlaylistClick(playlist: Playlist) {
        // TODO: Handle playlist click
    }
    
    fun onTrackClick(track: Track) {
        // TODO: Handle track click
    }
    
    fun refresh() {
        loadLibraryData()
    }
    
    fun loadPlaylists() {
        loadLibraryData()
    }
    
    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            // TODO: Implement actual deletion
            val currentPlaylists = uiState.playlists.toMutableList()
            currentPlaylists.removeAll { it.id.toString() == playlistId }
            uiState = uiState.copy(playlists = currentPlaylists)
        }
    }
    
    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            // TODO: Implement actual creation
            val newPlaylist = Playlist(
                id = System.currentTimeMillis(),
                name = name,
                description = description.takeIf { it.isNotBlank() },
                trackCount = 0,
                totalDuration = 0L,
                dateCreated = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )
            val currentPlaylists = uiState.playlists.toMutableList()
            currentPlaylists.add(0, newPlaylist)
            uiState = uiState.copy(playlists = currentPlaylists)
        }
    }
}

/**
 * UI state for the Library screen
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val likedTracks: List<Track> = emptyList(),
    val error: String? = null
) 
