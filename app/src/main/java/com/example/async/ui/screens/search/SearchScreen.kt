package com.example.async.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.async.core.model.SearchResult
import com.example.async.ui.components.music.TrackList
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

// Extension status for search
data class ExtensionStatus(
    val name: String,
    val isEnabled: Boolean,
    val isSearching: Boolean,
    val resultCount: Int,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onPlayTrack: (SearchResult) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var extensionStatuses by remember { mutableStateOf<List<ExtensionStatus>>(emptyList()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Sample extension statuses
    val sampleExtensions = remember {
        listOf(
            ExtensionStatus("SoundCloud", true, false, 0),
            ExtensionStatus("YouTube Music", true, false, 0),
            ExtensionStatus("Spotify", false, false, 0), // Disabled
            ExtensionStatus("Last.fm", true, false, 0),
            ExtensionStatus("Bandcamp", true, false, 0)
        )
    }
    
    // Initialize extension statuses
    LaunchedEffect(Unit) {
        extensionStatuses = sampleExtensions
    }
    
    // Simulate search function
    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            extensionStatuses = sampleExtensions
            return
        }
        
        isSearching = true
        extensionStatuses = extensionStatuses.map { 
            if (it.isEnabled) it.copy(isSearching = true, resultCount = 0) else it 
        }
    }
    
    // Handle search results simulation
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && isSearching) {
            kotlinx.coroutines.delay(1500) // Simulate network delay
            
            // Sample search results from different extensions
            val results = listOf(
                SearchResult(
                    id = "1", extensionId = "soundcloud", title = "Blinding Lights",
                    artist = "The Weeknd", album = "After Hours", duration = 200000
                ),
                SearchResult(
                    id = "2", extensionId = "youtube", title = "Blinding Lights (Official Audio)",
                    artist = "The Weeknd", album = "After Hours", duration = 201000
                ),
                SearchResult(
                    id = "3", extensionId = "soundcloud", title = "Shape of You",
                    artist = "Ed Sheeran", album = "Divide", duration = 233000
                ),
                SearchResult(
                    id = "4", extensionId = "bandcamp", title = "indie track",
                    artist = "Indie Artist", album = "Independent Album", duration = 180000
                ),
                SearchResult(
                    id = "5", extensionId = "youtube", title = "Popular Song",
                    artist = "Famous Artist", album = "Hit Album", duration = 195000
                )
            )
            
            searchResults = results
            isSearching = false
            
            // Update extension statuses with results
            extensionStatuses = extensionStatuses.map { status ->
                when (status.name.lowercase().replace(" ", "")) {
                    "soundcloud" -> status.copy(
                        isSearching = false, 
                        resultCount = results.count { it.extensionId == "soundcloud" }
                    )
                    "youtubemusic" -> status.copy(
                        isSearching = false, 
                        resultCount = results.count { it.extensionId == "youtube" }
                    )
                    "bandcamp" -> status.copy(
                        isSearching = false, 
                        resultCount = results.count { it.extensionId == "bandcamp" }
                    )
                    "last.fm" -> status.copy(
                        isSearching = false, 
                        resultCount = 0,
                        hasError = true,
                        errorMessage = "API rate limit exceeded"
                    )
                    else -> status.copy(isSearching = false, resultCount = 0)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header
        HeadlineLarge(
            text = "Search",
            modifier = Modifier.padding(bottom = AppSpacing.m)
        )
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { BodyMedium("Search for music...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        searchResults = emptyList()
                        extensionStatuses = sampleExtensions
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    performSearch(searchQuery)
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.m))
        
        // Extension status indicators
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            item {
                ExtensionStatusSection(
                    extensionStatuses = extensionStatuses,
                    isSearching = isSearching
                )
            }
            
            if (searchResults.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.s))
                    TitleMedium("Search Results")
                    Spacer(modifier = Modifier.height(AppSpacing.s))
                }
                
                item {
                    TrackList(
                        tracks = searchResults,
                        onTrackClick = onTrackClick,
                        onPlayTrack = onPlayTrack,
                        showExtensionSource = true
                    )
                }
            } else if (searchQuery.isNotEmpty() && !isSearching) {
                item {
                    EmptySearchResults(searchQuery)
                }
            } else if (searchQuery.isEmpty()) {
                item {
                    SearchPrompt()
                }
            }
        }
    }
}

@Composable
private fun ExtensionStatusSection(
    extensionStatuses: List<ExtensionStatus>,
    isSearching: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleMedium("Extension Sources")
                if (isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        LabelMedium("Searching...")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.s))
            
            extensionStatuses.forEach { status ->
                ExtensionStatusItem(status = status)
                Spacer(modifier = Modifier.height(AppSpacing.xs))
            }
        }
    }
}

@Composable
private fun ExtensionStatusItem(
    status: ExtensionStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            // Status indicator
            Icon(
                imageVector = when {
                    !status.isEnabled -> Icons.Outlined.DoNotDisturb
                    status.hasError -> Icons.Outlined.Error
                    status.isSearching -> Icons.Outlined.Refresh
                    status.resultCount > 0 -> Icons.Outlined.CheckCircle
                    else -> Icons.Outlined.Extension
                },
                contentDescription = null,
                tint = when {
                    !status.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    status.hasError -> MaterialTheme.colorScheme.error
                    status.isSearching -> MaterialTheme.colorScheme.primary
                    status.resultCount > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )
            
            BodyMedium(
                text = status.name,
                color = if (status.isEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
        
        // Status text
        when {
            !status.isEnabled -> {
                LabelMedium(
                    text = "Disabled",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            status.hasError -> {
                LabelMedium(
                    text = "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }
            status.isSearching -> {
                LabelMedium(
                    text = "Searching...",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            status.resultCount > 0 -> {
                LabelMedium(
                    text = "${status.resultCount} results",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                LabelMedium(
                    text = "Ready",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Error message if any
    if (status.hasError && !status.errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        BodySmall(
            text = status.errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

@Composable
private fun EmptySearchResults(query: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TitleMedium(
                text = "No Results Found",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BodyMedium(
                text = "Try searching with different keywords or check if your extensions are enabled.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SearchPrompt() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            TitleMedium(
                text = "Search for Music",
                color = MaterialTheme.colorScheme.onSurface
            )
            BodyMedium(
                text = "Enter a search query to find music from your enabled extensions.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 