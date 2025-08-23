package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.core.model.SearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search screen
 * Manages search state and operations
 */
class SearchViewModel : ViewModel() {
    
    var uiState by mutableStateOf(SearchUiState())
        private set
    
    fun search(query: String) {
        if (query.isBlank()) {
            uiState = uiState.copy(
                results = emptyList(),
                isLoading = false,
                error = null
            )
            return
        }
        
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            try {
                // Simulate search delay
                delay(1000)
                
                // Create mock search results
                val results = createMockSearchResults(query)
                
                uiState = uiState.copy(
                    isLoading = false,
                    results = results,
                    query = query
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }
    
    private fun createMockSearchResults(query: String): List<SearchResult> {
        return listOf(
            SearchResult(
                id = "search_1",
                extensionId = "mock_extension",
                title = "$query - Result 1",
                artist = "Mock Artist 1",
                album = "Mock Album 1",
                duration = 180000,
                thumbnailUrl = null
            ),
            SearchResult(
                id = "search_2",
                extensionId = "mock_extension",
                title = "$query - Result 2",
                artist = "Mock Artist 2",
                album = "Mock Album 2",
                duration = 210000,
                thumbnailUrl = null
            )
        )
    }
    
    fun onTrackClick(track: SearchResult) {
        // TODO: Handle track click
    }
    
    fun updateQuery(newQuery: String) {
        uiState = uiState.copy(query = newQuery)
    }
    
    fun clearSearch() {
        uiState = SearchUiState()
    }
}

/**
 * UI state for the Search screen
 */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: String? = null
) 
