package com.shawnfrost.async.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawnfrost.async.data.repository.MusicRepository
import com.shawnfrost.async.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _likedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val likedTrackIds: StateFlow<Set<String>> = _likedTrackIds.asStateFlow()

    init {
        loadLikedTracks()
    }

    private fun loadLikedTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                musicRepository.getLikedTracks().collect { tracks ->
                    _likedTracks.value = tracks
                    _likedTrackIds.value = tracks.map { it.id }.toSet()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLikeTrack(track: Track) {
        viewModelScope.launch {
            try {
                if (isTrackLiked(track.id)) {
                    // Unlike the track
                    musicRepository.deleteTrack(track)
                } else {
                    // Like the track
                    musicRepository.saveTrack(track)
                }
                musicRepository.toggleLikeTrack(track.id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun isTrackLiked(trackId: String): Boolean {
        return _likedTrackIds.value.contains(trackId)
    }

    fun refreshLikedTracks() {
        loadLikedTracks()
    }
} 