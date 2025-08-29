package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.core.model.SearchResult
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import com.async.domain.repository.PlaylistRepository
import com.async.domain.repository.TrackRepository
import com.async.app.di.AppModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import logcat.logcat

/**
 * ViewModel for the Library screen with advanced features
 * Manages library state, playlists, liked tracks, and downloads
 */
class LibraryViewModel : ViewModel() {
    
    private val playlistRepository: PlaylistRepository = AppModule.getPlaylistRepository()
    private val trackRepository: TrackRepository = AppModule.getTrackRepository()
    
    var uiState by mutableStateOf(LibraryUiState())
        private set
    
    private var hasInitialized = false

    
    init {
        loadLibraryDataIfNeeded()
    }
    
    /**
     * Load library data only if it hasn't been initialized yet
     */
    private fun loadLibraryDataIfNeeded() {
        if (!hasInitialized) {
            hasInitialized = true
            loadLibraryData()
        }
    }
    
    fun loadLibraryData() {
        viewModelScope.launch {
            // Only show loading if we don't have any data yet
            val hasData = uiState.likedTracks.isNotEmpty() || 
                         uiState.downloadedTracks.isNotEmpty() || 
                         uiState.customPlaylists.isNotEmpty()
            
            if (!hasData) {
                uiState = uiState.copy(isLoading = true)
            }
            
            try {
                // Load all library data
                loadLikedTracks()
                loadDownloadedTracks()
                loadCustomPlaylists()
                
                uiState = uiState.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error loading library data: ${e.message}" }
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Failed to load library data: ${e.message}"
                )
            }
        }
    }
    

    
    private fun loadLikedTracks() {
        viewModelScope.launch {
            try {
                val likedTracks = trackRepository.getLikedTracks().firstOrNull() ?: emptyList()
                uiState = uiState.copy(likedTracks = likedTracks)
                logcat("LibraryViewModel") { "Loaded ${likedTracks.size} liked tracks from database" }
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error loading liked tracks: ${e.message}" }
                // Create sample liked tracks for testing
                val sampleLikedTracks = listOf(
                    createSampleTrack("Shape of You", "Ed Sheeran", "÷ (Divide)", System.currentTimeMillis() - 3600000L),
                    createSampleTrack("Blinding Lights", "The Weeknd", "After Hours", System.currentTimeMillis() - 7200000L),
                    createSampleTrack("Watermelon Sugar", "Harry Styles", "Fine Line", System.currentTimeMillis() - 10800000L),
                    createSampleTrack("Levitating", "Dua Lipa", "Future Nostalgia", System.currentTimeMillis() - 14400000L),
                    createSampleTrack("Good 4 U", "Olivia Rodrigo", "SOUR", System.currentTimeMillis() - 18000000L)
                )
                uiState = uiState.copy(likedTracks = sampleLikedTracks)
            }
        }
    }
    
    private fun loadDownloadedTracks() {
        viewModelScope.launch {
            try {
                val downloadedTracks = trackRepository.getDownloadedTracks().firstOrNull() ?: emptyList()
                uiState = uiState.copy(downloadedTracks = downloadedTracks)
                logcat("LibraryViewModel") { "Loaded ${downloadedTracks.size} downloaded tracks from database" }
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error loading downloaded tracks: ${e.message}" }
                // Create sample downloaded tracks for testing
                val sampleDownloadedTracks = listOf(
                    createSampleTrack("As It Was", "Harry Styles", "Harry's House"),
                    createSampleTrack("Heat Waves", "Glass Animals", "Dreamland"),
                    createSampleTrack("Bad Habit", "Steve Lacy", "Gemini Rights")
                )
                uiState = uiState.copy(downloadedTracks = sampleDownloadedTracks)
            }
        }
    }
    
    private fun createSampleTrack(title: String, artist: String, album: String, addedAt: Long = System.currentTimeMillis()): Track {
        return Track(
            id = title.hashCode().toLong(),
            externalId = title.hashCode().toString(),
            extensionId = "sample",
            title = title,
            artist = artist,
            album = album,
            duration = (180000L..300000L).random(),
            thumbnailUrl = null,
            streamUrl = null,
            dateAdded = addedAt
        )
    }
    

    
    // Liked tracks management
    fun toggleTrackLiked(track: SearchResult) {
        viewModelScope.launch {
            try {
                val currentLiked = uiState.likedTracks.toMutableList()
                val existingTrack = currentLiked.find { it.externalId == track.id }
                
                if (existingTrack != null) {
                    trackRepository.removeLikedTrack(existingTrack.id)
                    currentLiked.remove(existingTrack)
                    logcat("LibraryViewModel") { "Removed track from liked: ${track.title}" }
                } else {
                    val newTrack = Track(
                        id = track.id.toLongOrNull() ?: track.hashCode().toLong(),
                        externalId = track.id,
                        extensionId = track.extensionId ?: "unknown",
                        title = track.title,
                        artist = track.artist ?: "Unknown Artist",
                        album = track.album ?: "Unknown Album",
                        duration = track.duration ?: 180000L,
                        thumbnailUrl = track.thumbnailUrl,
                        streamUrl = null,
                        dateAdded = System.currentTimeMillis()
                    )
                    trackRepository.addLikedTrack(newTrack)
                    currentLiked.add(0, newTrack)
                    logcat("LibraryViewModel") { "Added track to liked: ${track.title}" }
                }
                
                uiState = uiState.copy(likedTracks = currentLiked)
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error toggling liked track: ${e.message}" }
            }
        }
    }
    
    fun isTrackLiked(trackId: String): Boolean {
        return uiState.likedTracks.any { it.externalId == trackId }
    }
    
    // Downloaded tracks management
    fun deleteDownloadedTrack(trackId: String) {
        viewModelScope.launch {
            try {
                trackRepository.deleteDownloadedTrack(trackId.toLong())
                loadDownloadedTracks()
                logcat("LibraryViewModel") { "Deleted downloaded track: $trackId" }
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error deleting downloaded track: ${e.message}" }
                // Fallback to in-memory deletion
                val currentDownloaded = uiState.downloadedTracks.toMutableList()
                currentDownloaded.removeAll { it.id.toString() == trackId }
                uiState = uiState.copy(downloadedTracks = currentDownloaded)
            }
        }
    }
    
    // Background sync functionality
    fun syncLibraryData() {
        viewModelScope.launch {
            uiState = uiState.copy(isSyncing = true)
            
            try {
                delay(2000) // Simulate sync
                loadLibraryData()
                
                uiState = uiState.copy(
                    isSyncing = false,
                    lastSyncTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            
            try {
                // Load all library data
                loadLikedTracks()
                loadDownloadedTracks()
                loadCustomPlaylists()
                
                uiState = uiState.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                logcat("LibraryViewModel") { "Error refreshing library data: ${e.message}" }
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Failed to refresh library data: ${e.message}"
                )
            }
        }
    }
    
    // ======== CUSTOM PLAYLIST MANAGEMENT ========
    
    /**
     * Load custom playlists from repository
     */
    private suspend fun loadCustomPlaylists() {
        try {
            val playlists = playlistRepository.getUserPlaylists().firstOrNull() ?: emptyList()
            uiState = uiState.copy(customPlaylists = playlists)
        } catch (e: Exception) {
            logcat("LibraryViewModel") { "Error loading custom playlists: ${e.message}" }
        }
    }
    
    /**
     * Create a new custom playlist
     */
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                val result = playlistRepository.createPlaylist(name, description)
                
                when (result) {
                    is com.async.core.result.AsyncResult.Success -> {
                        // Reload playlists to get the updated list
                        loadCustomPlaylists()
                        logcat("LibraryViewModel") { "Created playlist: $name" }
                    }
                    is com.async.core.result.AsyncResult.Error -> {
                        uiState = uiState.copy(error = "Failed to create playlist: ${result.getErrorOrNull()}")
                        logcat("LibraryViewModel") { "Error creating playlist: ${result.getErrorOrNull()}" }
                    }
                    is com.async.core.result.AsyncResult.Loading -> {
                        // Handle loading state if needed
                        logcat("LibraryViewModel") { "Creating playlist..." }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Failed to create playlist: ${e.message}")
                logcat("LibraryViewModel") { "Error creating playlist: ${e.message}" }
            }
        }
    }
    
    /**
     * Delete a custom playlist
     */
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val result = playlistRepository.deletePlaylist(playlistId)
                
                when (result) {
                    is com.async.core.result.AsyncResult.Success -> {
                        loadCustomPlaylists()
                        logcat("LibraryViewModel") { "Deleted playlist: $playlistId" }
                    }
                    is com.async.core.result.AsyncResult.Error -> {
                        uiState = uiState.copy(error = "Failed to delete playlist: ${result.getErrorOrNull()}")
                        logcat("LibraryViewModel") { "Error deleting playlist: ${result.getErrorOrNull()}" }
                    }
                    is com.async.core.result.AsyncResult.Loading -> {
                        // Handle loading state if needed
                        logcat("LibraryViewModel") { "Deleting playlist..." }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Failed to delete playlist: ${e.message}")
                logcat("LibraryViewModel") { "Error deleting playlist: ${e.message}" }
            }
        }
    }
    
    /**
     * Update an existing playlist
     */
    fun updatePlaylist(playlistId: Long, name: String, description: String = "") {
        viewModelScope.launch {
            try {
                // Find the existing playlist to preserve other properties
                val existingPlaylist = uiState.customPlaylists.find { it.id == playlistId }
                if (existingPlaylist == null) {
                    uiState = uiState.copy(error = "Playlist not found")
                    return@launch
                }
                
                // Create updated playlist object
                val updatedPlaylist = existingPlaylist.copy(
                    name = name.trim(),
                    description = description.trim().ifEmpty { null }
                )
                
                val result = playlistRepository.updatePlaylist(updatedPlaylist)
                
                when (result) {
                    is com.async.core.result.AsyncResult.Success -> {
                        loadCustomPlaylists()
                        logcat("LibraryViewModel") { "Updated playlist: $playlistId" }
                    }
                    is com.async.core.result.AsyncResult.Error -> {
                        uiState = uiState.copy(error = "Failed to update playlist: ${result.getErrorOrNull()}")
                        logcat("LibraryViewModel") { "Error updating playlist: ${result.getErrorOrNull()}" }
                    }
                    is com.async.core.result.AsyncResult.Loading -> {
                        // Handle loading state if needed
                        logcat("LibraryViewModel") { "Updating playlist..." }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Failed to update playlist: ${e.message}")
                logcat("LibraryViewModel") { "Error updating playlist: ${e.message}" }
            }
        }
    }
    
    /**
     * Show the create playlist dialog
     */
    fun showCreatePlaylistDialog() {
        uiState = uiState.copy(showCreatePlaylistDialog = true)
    }
    
    /**
     * Hide the create playlist dialog
     */
    fun hideCreatePlaylistDialog() {
        uiState = uiState.copy(showCreatePlaylistDialog = false)
    }
    
    /**
     * Show the edit playlist dialog
     */
    fun showEditPlaylistDialog(playlist: Playlist) {
        uiState = uiState.copy(
            showEditPlaylistDialog = true,
            editingPlaylist = playlist
        )
    }
    
    /**
     * Hide the edit playlist dialog
     */
    fun hideEditPlaylistDialog() {
        uiState = uiState.copy(
            showEditPlaylistDialog = false,
            editingPlaylist = null
        )
    }
    
    /**
     * Show the delete confirmation dialog
     */
    fun showDeleteConfirmationDialog(playlist: Playlist) {
        uiState = uiState.copy(
            showDeleteConfirmationDialog = true,
            playlistToDelete = playlist
        )
    }
    
    /**
     * Hide the delete confirmation dialog
     */
    fun hideDeleteConfirmationDialog() {
        uiState = uiState.copy(
            showDeleteConfirmationDialog = false,
            playlistToDelete = null
        )
    }
    
    /**
     * Add track to playlist
     */
    fun addTrackToPlaylist(playlistId: Long, searchResult: SearchResult) {
        viewModelScope.launch {
            try {
                // First cache the track in the database
                val track = trackRepository.cacheTrack(searchResult)
                
                // Then add it to the playlist
                val result = playlistRepository.addTrackToPlaylist(playlistId, track.id)
                
                when (result) {
                    is com.async.core.result.AsyncResult.Success -> {
                        // Reload playlists to update track counts
                        loadCustomPlaylists()
                        logcat("LibraryViewModel") { "Added track to playlist: ${searchResult.title}" }
                    }
                    is com.async.core.result.AsyncResult.Error -> {
                        uiState = uiState.copy(error = "Failed to add track to playlist: ${result.getErrorOrNull()}")
                        logcat("LibraryViewModel") { "Error adding track to playlist: ${result.getErrorOrNull()}" }
                    }
                    is com.async.core.result.AsyncResult.Loading -> {
                        logcat("LibraryViewModel") { "Adding track to playlist..." }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Failed to add track to playlist: ${e.message}")
                logcat("LibraryViewModel") { "Error adding track to playlist: ${e.message}" }
            }
        }
    }
    
    /**
     * Smart add to playlist - adds to liked first, then opens dialog on second click
     */
    fun smartAddToPlaylist(searchResult: SearchResult): Boolean {
        val currentlyLiked = isTrackLiked(searchResult.id ?: "")
        
        if (!currentlyLiked) {
            // First click: Add to liked playlist automatically
            viewModelScope.launch {
                try {
                    val track = trackRepository.cacheTrack(searchResult)
                    trackRepository.addLikedTrack(track)
                    loadLikedTracks()
                    logcat("LibraryViewModel") { "Added track to liked: ${searchResult.title}" }
                } catch (e: Exception) {
                    uiState = uiState.copy(error = "Failed to add track: ${e.message}")
                    logcat("LibraryViewModel") { "Error in smart add: ${e.message}" }
                }
            }
        }
        
        // Return true if track should open dialog (already liked), false if just added to liked
        return currentlyLiked
    }
    
    /**
     * Check if track is in a specific playlist
     * For now, returns false - this should be implemented with proper state management
     */
    fun isTrackInPlaylist(playlistId: Long, searchResult: SearchResult): Boolean {
        // TODO: Implement proper playlist membership checking
        // This would require caching track states or using suspend functions properly
        return false
    }
    
    /**
     * Toggle track in playlist (add if not present, remove if present)
     */
    fun toggleTrackInPlaylist(playlistId: Long, searchResult: SearchResult) {
        viewModelScope.launch {
            try {
                val track = trackRepository.cacheTrack(searchResult)
                val isInPlaylist = playlistRepository.isTrackInPlaylist(playlistId, track.id)
                
                if (isInPlaylist) {
                    // Remove from playlist
                    val result = playlistRepository.removeTrackFromPlaylist(playlistId, track.id)
                    when (result) {
                        is com.async.core.result.AsyncResult.Success -> {
                            loadCustomPlaylists()
                            if (playlistId == -1L) { // Liked playlist (using -1 as liked playlist ID)
                                loadLikedTracks()
                            }
                            logcat("LibraryViewModel") { "Removed track from playlist: ${searchResult.title}" }
                        }
                        else -> {
                            uiState = uiState.copy(error = "Failed to remove track from playlist")
                        }
                    }
                } else {
                    // Add to playlist
                    val result = playlistRepository.addTrackToPlaylist(playlistId, track.id)
                    when (result) {
                        is com.async.core.result.AsyncResult.Success -> {
                            loadCustomPlaylists()
                            if (playlistId == -1L) { // Liked playlist
                                loadLikedTracks()
                            }
                            logcat("LibraryViewModel") { "Added track to playlist: ${searchResult.title}" }
                        }
                        else -> {
                            uiState = uiState.copy(error = "Failed to add track to playlist")
                        }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Failed to toggle track: ${e.message}")
                logcat("LibraryViewModel") { "Error toggling track: ${e.message}" }
            }
        }
    }
}

/**
 * UI state for the Library screen
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val likedTracks: List<Track> = emptyList(),
    val downloadedTracks: List<Track> = emptyList(),
    val customPlaylists: List<Playlist> = emptyList(),
    val error: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val showCreatePlaylistDialog: Boolean = false,
    val showEditPlaylistDialog: Boolean = false,
    val editingPlaylist: Playlist? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val playlistToDelete: Playlist? = null
)

 
