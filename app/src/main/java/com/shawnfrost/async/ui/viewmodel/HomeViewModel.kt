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
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _trendingTracks = MutableStateFlow<List<Track>>(emptyList())
    val trendingTracks: StateFlow<List<Track>> = _trendingTracks.asStateFlow()

    private val _newReleases = MutableStateFlow<List<Track>>(emptyList())
    val newReleases: StateFlow<List<Track>> = _newReleases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load trending tracks
                musicRepository.getTrendingTracks()
                    .onSuccess { tracks ->
                        _trendingTracks.value = tracks
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Failed to load trending tracks: ${error.message}"
                    }

                // Load new releases
                musicRepository.getNewReleases()
                    .onSuccess { tracks ->
                        _newReleases.value = tracks
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Failed to load new releases: ${error.message}"
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }
} 