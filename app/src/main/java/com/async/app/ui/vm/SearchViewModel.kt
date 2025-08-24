package com.async.app.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.async.app.di.AppModule
import com.async.core.extension.ExtensionStatus
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.extensions.service.ExtensionService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logcat.logcat

/**
 * Enhanced ViewModel for the Search screen with real extension integration
 * Manages search state, history, caching, and advanced search features
 */
class SearchViewModel : ViewModel() {
    
    private val extensionService: ExtensionService = AppModule.getExtensionService()
    private val json = Json { ignoreUnknownKeys = true }
    
    var uiState by mutableStateOf(SearchUiState())
        private set
    
    // Debouncing and search management
    private var searchJob: Job? = null
    private val searchHistory = mutableListOf<String>()
    private val searchCache = mutableMapOf<String, CachedSearchResult>()
    
    // Advanced search filters
    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()
    
    init {
        // Clear old cache since we switched to real search (no more mock data)
        clearSearchCache()
        loadSearchHistory()
        loadSearchCache()
        
        // Monitor extension changes
        viewModelScope.launch {
            extensionService.getInstalledExtensions().collect { extensions ->
                val enabledExtensions = extensions.filter { (_, extension) -> 
                    extension.status == ExtensionStatus.INSTALLED 
                }.keys.toList()
                
                uiState = uiState.copy(
                    availableExtensions = enabledExtensions,
                    hasExtensions = enabledExtensions.isNotEmpty()
                )
            }
        }
    }
    
    /**
     * Perform search with debouncing and caching
     */
    fun search(query: String, forceRefresh: Boolean = false) {
        if (query.isBlank()) {
            uiState = uiState.copy(
                results = emptyList(),
                resultsByExtension = emptyMap(),
                isLoading = false,
                error = null,
                lastSearchQuery = ""
            )
            return
        }
        
        logcat("SearchViewModel") { "Starting search for: '$query'" }
        
        // Cancel previous search
        searchJob?.cancel()
        
        // Skip cache temporarily - always do fresh search for real extensions
        logcat("SearchViewModel") { "Forcing fresh search for: '$query' (bypassing cache)" }
        val cacheKey = createCacheKey(query, _searchFilters.value)
        
        searchJob = viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true, 
                error = null,
                lastSearchQuery = query,
                searchSource = "Extensions"
            )
            
            try {
                // Add debouncing delay
                delay(500)
                
                val installedExtensions = extensionService.getInstalledExtensions().value
                logcat("SearchViewModel") { "Found ${installedExtensions.size} installed extensions: ${installedExtensions.keys}" }
                
                if (installedExtensions.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "No extensions installed. Install music extensions to search for tracks.",
                        hasExtensions = false
                    )
                    return@launch
                }
                
                // Filter extensions based on user selection and enabled status
                val filters = _searchFilters.value
                val enabledExtensions = installedExtensions.filter { (_, extension) -> 
                    extension.status == ExtensionStatus.INSTALLED 
                }.keys.toList()
                
                logcat("SearchViewModel") { "Enabled extensions: $enabledExtensions" }
                
                val extensionsToSearch = if (filters.selectedExtensions.isEmpty()) {
                    enabledExtensions
                } else {
                    filters.selectedExtensions.filter { it in enabledExtensions }
                }
                
                if (extensionsToSearch.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "No valid extensions selected for search."
                    )
                    return@launch
                }
                
                logcat("SearchViewModel") { "Searching across ${extensionsToSearch.size} extensions" }
                
                // Search across all selected extensions in parallel
                val allResults = mutableListOf<SearchResult>()
                val resultsByExtension = mutableMapOf<String, List<SearchResult>>()
                var searchErrors = mutableListOf<String>()
                
                // Create deferred searches for parallel execution
                val searches = extensionsToSearch.map { extensionId ->
                    async {
                        try {
                            logcat("SearchViewModel") { "Searching extension: $extensionId" }
                            val extension = installedExtensions[extensionId]
                            
                            if (extension != null) {
                                // Call real extension search
                                logcat("SearchViewModel") { "Found extension $extensionId, calling ExtensionService.searchInExtension for query: '$query'" }
                                val searchResults = extensionService.searchInExtension(extensionId, query, filters.maxResultsPerExtension)
                                
                                logcat("SearchViewModel") { "ExtensionService.searchInExtension returned ${searchResults.size} results for $extensionId" }
                                
                                if (searchResults.isNotEmpty()) {
                                    resultsByExtension[extensionId] = searchResults
                                    allResults.addAll(searchResults)
                                    logcat("SearchViewModel") { "Successfully added ${searchResults.size} results from $extensionId" }
                                } else {
                                    logcat("SearchViewModel") { "No results from extension $extensionId" }
                                }
                            } else {
                                logcat("SearchViewModel") { "Extension $extensionId not found in installedExtensions map" }
                            }
                        } catch (e: Exception) {
                            logcat("SearchViewModel") { "Error searching extension $extensionId: ${e.message}" }
                            searchErrors.add("$extensionId: ${e.message}")
                        }
                    }
                }
                
                // Wait for all searches to complete
                searches.awaitAll()
                
                // Apply additional filters
                val filteredResults = applyAdvancedFilters(allResults, filters)
                
                // Sort results
                val sortedResults = sortResults(filteredResults, filters.sortBy)
                
                // Cache the results
                cacheSearchResults(cacheKey, sortedResults, resultsByExtension)
                
                // Add to search history
                addToSearchHistory(query)
                
                uiState = uiState.copy(
                    isLoading = false,
                    results = sortedResults,
                    resultsByExtension = resultsByExtension,
                    error = if (searchErrors.isNotEmpty()) "Some extensions failed: ${searchErrors.joinToString(", ")}" else null,
                    totalResults = sortedResults.size
                )
                
                logcat("SearchViewModel") { "Search completed. Total results: ${sortedResults.size}" }
                
            } catch (e: Exception) {
                logcat("SearchViewModel") { "Search failed: ${e.message}" }
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }
    

    
    /**
     * Apply advanced filters to search results
     */
    private fun applyAdvancedFilters(results: List<SearchResult>, filters: SearchFilters): List<SearchResult> {
        return results.filter { result ->
            // Duration filter
            val meetsMinDuration = filters.minDuration?.let { result.duration?.let { duration -> duration >= it } ?: true } ?: true
            val meetsMaxDuration = filters.maxDuration?.let { result.duration?.let { duration -> duration <= it } ?: true } ?: true
            
            // Quality filter
            val meetsQuality = filters.minQuality?.let { minQuality ->
                result.metadata["quality"]?.let { quality ->
                    extractBitrate(quality) >= minQuality
                } ?: true
            } ?: true
            
            // Artist filter
            val meetsArtist = filters.artistFilter?.let { artistFilter ->
                result.artist?.contains(artistFilter, ignoreCase = true) ?: false
            } ?: true
            
            // Album filter
            val meetsAlbum = filters.albumFilter?.let { albumFilter ->
                result.album?.contains(albumFilter, ignoreCase = true) ?: false
            } ?: true
            
            meetsMinDuration && meetsMaxDuration && meetsQuality && meetsArtist && meetsAlbum
        }
    }
    
    /**
     * Sort search results based on criteria
     */
    private fun sortResults(results: List<SearchResult>, sortBy: SortOption): List<SearchResult> {
        return when (sortBy) {
            SortOption.RELEVANCE -> results // Already in relevance order
            SortOption.TITLE -> results.sortedBy { it.title }
            SortOption.ARTIST -> results.sortedBy { it.artist ?: "" }
            SortOption.DURATION -> results.sortedBy { it.duration ?: 0 }
            SortOption.EXTENSION -> results.sortedBy { it.extensionId }
        }
    }
    
    /**
     * Extract bitrate from quality string
     */
    private fun extractBitrate(quality: String): Int {
        return quality.filter { it.isDigit() }.toIntOrNull() ?: 0
    }
    
    /**
     * Update search query with debouncing
     */
    fun updateQuery(newQuery: String) {
        uiState = uiState.copy(query = newQuery)
        
        // Auto-search with debouncing
        if (newQuery.length >= 3) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(800) // Debounce delay
                search(newQuery)
            }
        } else if (newQuery.isEmpty()) {
            clearSearch()
        }
    }
    
    /**
     * Clear search and reset state
     */
    fun clearSearch() {
        searchJob?.cancel()
        uiState = SearchUiState(
            availableExtensions = uiState.availableExtensions,
            hasExtensions = uiState.hasExtensions,
            searchHistory = uiState.searchHistory
        )
    }
    
    /**
     * Handle track click for playback
     */
    fun onTrackClick(track: SearchResult) {
        logcat("SearchViewModel") { "Track clicked: ${track.title} from ${track.extensionId}" }
        // TODO: Integrate with PlayerViewModel
    }
    
    /**
     * Update search filters
     */
    fun updateFilters(filters: SearchFilters) {
        _searchFilters.value = filters
        
        // Re-search if we have a query
        if (uiState.lastSearchQuery.isNotEmpty()) {
            search(uiState.lastSearchQuery, forceRefresh = true)
        }
    }
    
    /**
     * Get search suggestions based on history
     */
    fun getSearchSuggestions(query: String): List<String> {
        return if (query.isEmpty()) {
            searchHistory.take(5)
        } else {
            searchHistory.filter { it.contains(query, ignoreCase = true) }.take(5)
        }
    }
    
    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        searchHistory.clear()
        saveSearchHistory()
        uiState = uiState.copy(searchHistory = emptyList())
    }
    
    /**
     * Remove item from search history
     */
    fun removeFromHistory(query: String) {
        searchHistory.remove(query)
        saveSearchHistory()
        uiState = uiState.copy(searchHistory = searchHistory.toList())
    }
    
    // Cache and history management
    private fun createCacheKey(query: String, filters: SearchFilters): String {
        return "${query}_${filters.hashCode()}"
    }
    
    private fun cacheSearchResults(key: String, results: List<SearchResult>, resultsByExtension: Map<String, List<SearchResult>>) {
        searchCache[key] = CachedSearchResult(
            results = results,
            resultsByExtension = resultsByExtension,
            timestamp = System.currentTimeMillis()
        )
        
        // Limit cache size
        if (searchCache.size > 50) {
            val oldestKey = searchCache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { searchCache.remove(it) }
        }
    }
    
    private fun addToSearchHistory(query: String) {
        searchHistory.remove(query) // Remove if exists
        searchHistory.add(0, query) // Add to beginning
        
        // Limit history size
        if (searchHistory.size > 20) {
            searchHistory.removeLastOrNull()
        }
        
        saveSearchHistory()
        uiState = uiState.copy(searchHistory = searchHistory.toList())
    }
    
    private fun loadSearchHistory() {
        // TODO: Load from SharedPreferences or Room database
        // For now, add some sample history
        searchHistory.addAll(listOf("Daft Punk", "Bohemian Rhapsody", "Jazz", "Chill Music"))
        uiState = uiState.copy(searchHistory = searchHistory.toList())
    }
    
    private fun saveSearchHistory() {
        // TODO: Save to SharedPreferences or Room database
    }
    
    private fun loadSearchCache() {
        // TODO: Load cached results from storage if needed
    }
    
    /**
     * Clear all cached search results
     */
    private fun clearSearchCache() {
        searchCache.clear()
        logcat("SearchViewModel") { "Search cache cleared" }
    }
}

/**
 * Enhanced UI state for the Search screen
 */
data class SearchUiState(
    val query: String = "",
    val lastSearchQuery: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val resultsByExtension: Map<String, List<SearchResult>> = emptyMap(),
    val error: String? = null,
    val totalResults: Int = 0,
    val searchHistory: List<String> = emptyList(),
    val availableExtensions: List<String> = emptyList(),
    val hasExtensions: Boolean = false,
    val searchSource: String = ""
)

/**
 * Advanced search filters
 */
@Serializable
data class SearchFilters(
    val selectedExtensions: List<String> = emptyList(), // Empty means all
    val maxResultsPerExtension: Int = 10,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val minDuration: Long? = null, // in milliseconds
    val maxDuration: Long? = null, // in milliseconds
    val minQuality: Int? = null, // minimum bitrate
    val artistFilter: String? = null,
    val albumFilter: String? = null,
    val genreFilter: String? = null
)

/**
 * Sort options for search results
 */
enum class SortOption {
    RELEVANCE, TITLE, ARTIST, DURATION, EXTENSION
}

/**
 * Cached search result with expiration
 */
private data class CachedSearchResult(
    val results: List<SearchResult>,
    val resultsByExtension: Map<String, List<SearchResult>>,
    val timestamp: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > 30 * 60 * 1000 // 30 minutes
    }
} 
