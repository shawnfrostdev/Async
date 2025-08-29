package com.async.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.async.core.model.SearchResult
import com.async.app.ui.vm.SearchViewModel
import com.async.app.ui.vm.LibraryViewModel
import com.async.app.ui.vm.SearchFilters
import com.async.app.ui.vm.SortOption
import com.async.app.ui.components.AppText
import logcat.logcat
import com.async.app.ui.components.AppCards
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onPlayTrack: (SearchResult) -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val uiState = searchViewModel.uiState
    val libraryUiState = libraryViewModel.uiState
    val searchFilters by searchViewModel.searchFilters.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    
    var showFilters by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<SearchResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with extension status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText.TitleLarge(text = "Search")
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Extension status indicator
                ExtensionStatusIndicator(
                    hasExtensions = uiState.hasExtensions,
                    extensionCount = uiState.availableExtensions.size
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Filters button
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Search Filters",
                        tint = if (searchFilters != SearchFilters()) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // History button
                IconButton(onClick = { showHistory = !showHistory }) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = "Search History"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Input with suggestions
        SearchInputSection(
            query = uiState.query,
            isLoading = uiState.isLoading,
            showHistory = showHistory,
            searchHistory = uiState.searchHistory,
            searchViewModel = searchViewModel,
            keyboardController = keyboardController,
            onHistoryDismiss = { showHistory = false }
        )
        
        // Search Filters (expandable)
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SearchFiltersCard(
                filters = searchFilters,
                availableExtensions = uiState.availableExtensions,
                onFiltersUpdate = { searchViewModel.updateFilters(it) },
                onDismiss = { showFilters = false }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Results Section
        SearchResultsSection(
            uiState = uiState,
            onTrackClick = onTrackClick,
            onPlayTrack = onPlayTrack,
            onAddToPlaylist = { track ->
                selectedTrackForPlaylist = track
                showAddToPlaylistDialog = true
            },
            onRetrySearch = { searchViewModel.search(uiState.lastSearchQuery, forceRefresh = true) }
        )
    }
    
    // Add to Playlist Dialog
    if (showAddToPlaylistDialog && selectedTrackForPlaylist != null) {
        AddToPlaylistDialog(
            track = selectedTrackForPlaylist!!,
            playlists = libraryUiState.customPlaylists,
            onPlaylistSelected = { playlist ->
                libraryViewModel.addTrackToPlaylist(playlist.id, selectedTrackForPlaylist!!)
                showAddToPlaylistDialog = false
                selectedTrackForPlaylist = null
            },
            onCreateNewPlaylist = {
                libraryViewModel.showCreatePlaylistDialog()
                showAddToPlaylistDialog = false
                selectedTrackForPlaylist = null
            },
            onDismiss = {
                showAddToPlaylistDialog = false
                selectedTrackForPlaylist = null
            }
        )
    }
}

@Composable
private fun ExtensionStatusIndicator(
    hasExtensions: Boolean,
    extensionCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasExtensions) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (hasExtensions) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (hasExtensions) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (hasExtensions) "$extensionCount Extensions" else "No Extensions",
                style = MaterialTheme.typography.labelSmall,
                color = if (hasExtensions) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputSection(
    query: String,
    isLoading: Boolean,
    showHistory: Boolean,
    searchHistory: List<String>,
    searchViewModel: SearchViewModel,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    onHistoryDismiss: () -> Unit
) {
    Column {
        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { searchViewModel.updateQuery(it) },
            label = { Text("Search for music...") },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.clearSearch() }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    searchViewModel.search(query)
                    keyboardController?.hide()
                    onHistoryDismiss()
                }
            )
        )
        
        // Search History Dropdown
        AnimatedVisibility(
            visible = showHistory && searchHistory.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        TextButton(onClick = { searchViewModel.clearSearchHistory() }) {
                            Text("Clear All")
                        }
                    }
                    
                    searchHistory.take(5).forEach { historyItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    searchViewModel.updateQuery(historyItem)
                                    searchViewModel.search(historyItem)
                                    onHistoryDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = historyItem,
                                        modifier = Modifier.weight(1f),
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { searchViewModel.removeFromHistory(historyItem) }
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFiltersCard(
    filters: SearchFilters,
    availableExtensions: List<String>,
    onFiltersUpdate: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
        
        Spacer(modifier = Modifier.height(16.dp))
        
            // Extension Selection
            if (availableExtensions.isNotEmpty()) {
                Text(
                    text = "Extensions",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(availableExtensions) { extension ->
                        val isSelected = filters.selectedExtensions.isEmpty() || 
                                        extension in filters.selectedExtensions
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newSelection = if (filters.selectedExtensions.isEmpty()) {
                                    // If all were selected, now select only this one
                                    listOf(extension)
                                } else if (extension in filters.selectedExtensions) {
                                    // Remove this extension
                                    val newList = filters.selectedExtensions - extension
                                    if (newList.isEmpty()) emptyList() else newList // Empty means all
                                } else {
                                    // Add this extension
                                    filters.selectedExtensions + extension
                                }
                                onFiltersUpdate(filters.copy(selectedExtensions = newSelection))
                            },
                            label = {
                                Text(
                                    text = extension.substringAfterLast('.'),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Sort Options
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(SortOption.values()) { sortOption ->
                    FilterChip(
                        selected = filters.sortBy == sortOption,
                        onClick = {
                            onFiltersUpdate(filters.copy(sortBy = sortOption))
                        },
                        label = {
                            Text(
                                text = when (sortOption) {
                                    SortOption.RELEVANCE -> "Relevance"
                                    SortOption.TITLE -> "Title"
                                    SortOption.ARTIST -> "Artist"
                                    SortOption.DURATION -> "Duration"
                                    SortOption.EXTENSION -> "Source"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
            
            // Results per extension
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Results per extension: ${filters.maxResultsPerExtension}",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Slider(
                    value = filters.maxResultsPerExtension.toFloat(),
                    onValueChange = { 
                        onFiltersUpdate(filters.copy(maxResultsPerExtension = it.toInt()))
                    },
                    valueRange = 5f..50f,
                    steps = 8,
                    modifier = Modifier.width(120.dp)
                )
            }
            
            // Reset filters button
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onFiltersUpdate(SearchFilters()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Filters")
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    uiState: com.async.app.ui.vm.SearchUiState,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit,
    onRetrySearch: () -> Unit
) {
        if (uiState.isLoading) {
        LoadingContent()
    } else if (uiState.error != null) {
        ErrorContent(
            error = uiState.error!!,
            onRetry = onRetrySearch
        )
    } else if (uiState.results.isNotEmpty()) {
        // Results grouped by extension (standard layout)
        Column {
            // Results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.totalResults} results found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (uiState.searchSource.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = uiState.searchSource,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results grouped by extension - standard music platform layout
            ResultsByExtensionStandard(
                resultsByExtension = uiState.resultsByExtension,
                onTrackClick = onTrackClick,
                onPlayTrack = onPlayTrack,
                onAddToPlaylist = onAddToPlaylist
            )
        }
    } else if (uiState.lastSearchQuery.isNotEmpty()) {
        NoResultsContent(query = uiState.lastSearchQuery)
    } else if (!uiState.hasExtensions) {
        NoExtensionsContent()
    } else {
        EmptySearchContent()
    }
}

@Composable
private fun LoadingContent() {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Searching across extensions...",
                style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = "Search Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
            
            Spacer(modifier = Modifier.height(16.dp))
            
                    Button(
                onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Search")
                    }
                }
            }
}

@Composable
private fun ResultsByExtensionStandard(
    resultsByExtension: Map<String, List<SearchResult>>,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit
) {
            LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
        resultsByExtension.forEach { (extensionId, results) ->
                item {
                ExtensionResultsSectionStandard(
                    extensionId = extensionId,
                    results = results,
                    onTrackClick = onTrackClick,
                    onPlayTrack = onPlayTrack,
                    onAddToPlaylist = onAddToPlaylist
                )
            }
        }
    }
}

@Composable
private fun ExtensionResultsSectionStandard(
    extensionId: String,
    results: List<SearchResult>,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit
) {
    Column {
        // Extension header - clean, standard style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
            Text(
                text = getExtensionDisplayName(extensionId),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${results.size} results",
                style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
        
        Spacer(modifier = Modifier.height(12.dp))
                
        // Results for this extension - clean list
        results.forEach { track ->
            StandardSearchResultItem(
                        track = track,
                        onTrackClick = onTrackClick,
                        onPlayClick = onPlayTrack,
                        onAddToPlaylist = onAddToPlaylist
                    )
            if (track != results.last()) {
                Spacer(modifier = Modifier.height(1.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}

// Helper function to get display name for extensions
private fun getExtensionDisplayName(extensionId: String): String {
    return when {
        extensionId.contains("dabyeet", ignoreCase = true) -> "DabYeet"
        extensionId.contains("spotify", ignoreCase = true) -> "Spotify"
        extensionId.contains("youtube", ignoreCase = true) -> "YouTube Music"
        extensionId.contains("soundcloud", ignoreCase = true) -> "SoundCloud"
        else -> extensionId.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    }
}

@Composable
private fun StandardSearchResultItem(
    track: SearchResult,
    onTrackClick: (SearchResult) -> Unit,
    onPlayClick: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { 
            logcat("SearchScreen") { "Song card clicked: ${track.title}" }
            onPlayClick(track)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track artwork - sized to match both text lines
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                    if (track.thumbnailUrl != null) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Outlined.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
                Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Song name - larger and prominent
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Artist name and type - smaller, secondary color
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type indicator (Song/Album)
                    val trackType = track.metadata["type"] ?: "Song"
                    Text(
                        text = trackType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (track.artist != null) {
                        Text(
                            text = " • ${track.artist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Duration
                    if (track.duration != null) {
                        Text(
                            text = " • ${formatDuration(track.duration!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Add to playlist button
            IconButton(
                onClick = { 
                    onAddToPlaylist(track)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add to playlist",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedSearchResultItem(
    track: SearchResult,
    onTrackClick: (SearchResult) -> Unit,
    onPlayClick: (SearchResult) -> Unit,
    showExtensionBadge: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onTrackClick(track) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.thumbnailUrl != null) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                    Icon(
                        Icons.Outlined.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (track.artist != null) {
                    Text(
                        text = track.artist!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (track.duration != null) {
                        Text(
                            text = formatDuration(track.duration!!),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Quality indicator
                    track.metadata["quality"]?.let { quality ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = quality,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Extension badge
                    if (showExtensionBadge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = track.extensionId.substringAfterLast('.'),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Actions
            Row {
                IconButton(onClick = { onPlayClick(track) }) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { /* TODO: Add to playlist */ }) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add to playlist"
                    )
                }
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share"
                    )
                }
            }
        }
    }
}

@Composable
private fun NoResultsContent(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "No tracks found for \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try different keywords or check your extensions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun NoExtensionsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Extension,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Extensions Installed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Install music extensions to search for tracks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EmptySearchContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search for Music",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Find songs, artists, and albums from various sources",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            

        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
} 

@Composable
private fun AddToPlaylistDialog(
    track: SearchResult,
    playlists: List<com.async.domain.model.Playlist>,
    onPlaylistSelected: (com.async.domain.model.Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.title ?: "Unknown Track",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Create new playlist option
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCreateNewPlaylist,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Create new playlist",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Create New Playlist",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Existing playlists
                if (playlists.isNotEmpty()) {
                    items(playlists) { playlist ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPlaylistSelected(playlist) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.PlaylistAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.PlaylistAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No playlists yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 
