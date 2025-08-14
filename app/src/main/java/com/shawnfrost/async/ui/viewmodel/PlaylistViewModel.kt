package com.shawnfrost.async.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawnfrost.async.data.local.dao.PlaylistDao
import com.shawnfrost.async.data.local.entity.PlaylistEntity
import com.shawnfrost.async.data.local.entity.PlaylistTrackCrossRef
import com.shawnfrost.async.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val playlists: StateFlow<List<PlaylistEntity>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                playlistDao.getAllPlaylists().collect { playlists ->
                    _playlists.value = playlists
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load playlists: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            try {
                val playlist = PlaylistEntity(
                    name = name,
                    description = description,
                    coverArt = null
                )
                playlistDao.insertPlaylist(playlist)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create playlist: ${e.message}"
            }
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            try {
                playlistDao.deletePlaylist(playlist)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            try {
                val currentCount = playlistDao.getPlaylistTrackCount(playlistId)
                val crossRef = PlaylistTrackCrossRef(
                    playlistId = playlistId,
                    trackId = track.id,
                    position = currentCount
                )
                playlistDao.addTrackToPlaylist(crossRef)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add track to playlist: ${e.message}"
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            try {
                val crossRef = PlaylistTrackCrossRef(
                    playlistId = playlistId,
                    trackId = trackId,
                    position = 0 // Position doesn't matter for deletion
                )
                playlistDao.removeTrackFromPlaylist(crossRef)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove track from playlist: ${e.message}"
            }
        }
    }

    fun getPlaylistTracks(playlistId: Long) = playlistDao.getPlaylistTracks(playlistId)

    fun clearError() {
        _errorMessage.value = null
    }
} 