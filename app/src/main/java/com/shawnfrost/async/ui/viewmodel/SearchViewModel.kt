package com.shawnfrost.async.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawnfrost.async.data.repository.MusicRepository
import com.shawnfrost.async.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    init {
        // Auto-search when query changes with debounce
        searchQuery
            .debounce(500) // Wait 500ms after user stops typing
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _searchResults.value = emptyList()
                    _errorMessage.value = null
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _errorMessage.value = null
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Add to search history
            addToSearchHistory(query)
            
            try {
                musicRepository.searchTracks(query)
                    .onSuccess { tracks ->
                        _searchResults.value = tracks
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Search failed: ${error.message}"
                        _searchResults.value = emptyList()
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addToSearchHistory(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        // Remove if already exists to avoid duplicates
        currentHistory.remove(query)
        // Add to the beginning
        currentHistory.add(0, query)
        // Keep only last 10 searches
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        _searchHistory.value = currentHistory
    }

    fun selectFromHistory(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun retrySearch() {
        if (_searchQuery.value.isNotBlank()) {
            performSearch(_searchQuery.value)
        }
    }
} 