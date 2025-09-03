package app.async.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import app.async.R
import app.async.app.ui.theme.AsyncColors
import app.async.core.model.SearchResult
import app.async.app.ui.vm.SearchViewModel
import app.async.app.ui.vm.LibraryViewModel
import app.async.app.ui.vm.SearchFilters
import app.async.app.ui.vm.SortOption
import app.async.app.ui.components.AppText
import app.async.app.ui.components.layout.*
import logcat.logcat
import app.async.app.ui.components.AppCards
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

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
    
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<SearchResult?>(null) }
    
    StandardScreenLayout(
        hasTopBar = false,
        hasBottomPadding = false
    ) {
        
        // Search Bar with Extension Count Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.spacing_normal)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Search Bar (reduced width)
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { searchViewModel.updateQuery(it) },
                placeholder = { 
                    Text(
                        stringResource(R.string.search_placeholder),
                        color = AsyncColors.TextSecondary
                    ) 
                },
                leadingIcon = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small)),
                            strokeWidth = 2.dp,
                            color = AsyncColors.TextSecondary
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.action_search),
                            tint = AsyncColors.TextSecondary,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                        )
                    }
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { searchViewModel.clearSearch() }) {
                            Icon(
                                Icons.Outlined.Clear, 
                                contentDescription = "Clear",
                                tint = AsyncColors.TextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AsyncColors.Surface,
                    focusedContainerColor = AsyncColors.Surface,
                    focusedTextColor = AsyncColors.TextPrimary,
                    unfocusedTextColor = AsyncColors.TextPrimary,
                    focusedBorderColor = AsyncColors.Primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchViewModel.search(uiState.query, isAutoSearch = false)
                        keyboardController?.hide()
                    }
                )
            )
            
            // Extension Count Indicator Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = AsyncColors.Surface,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val extensionCount = if (searchFilters.selectedExtensions.isEmpty()) {
                    uiState.availableExtensions.size
                } else {
                    searchFilters.selectedExtensions.size
                }
                
                Text(
                    text = extensionCount.toString(),
                    color = AsyncColors.TextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
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
            libraryViewModel = libraryViewModel
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
            },
            libraryViewModel = libraryViewModel
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
    uiState: app.async.app.ui.vm.SearchUiState,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit,
    libraryViewModel: LibraryViewModel
) {
    if (uiState.isLoading) {
        LoadingContent()
    } else if (uiState.results.isNotEmpty()) {
        // Results grouped by extension (standard layout)
        Column {
            // Results header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                onAddToPlaylist = onAddToPlaylist,
                libraryViewModel = libraryViewModel
            )
        }
    } else if (!uiState.hasExtensions && uiState.lastSearchQuery.isNotEmpty()) {
        // User searched but no extensions available
        NoExtensionsContent()
    } else if (uiState.lastSearchQuery.isNotEmpty()) {
        // User searched but no results found
        NoResultsContent(query = uiState.lastSearchQuery)
    } else {
        EmptySearchContent()
    }
}

@Composable
private fun LoadingContent() {
    StandardLoadingState(
        message = stringResource(R.string.search_loading)
    )
}



@Composable
private fun ResultsByExtensionStandard(
    resultsByExtension: Map<String, List<SearchResult>>,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    onAddToPlaylist: (SearchResult) -> Unit,
    libraryViewModel: LibraryViewModel
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
                    onAddToPlaylist = onAddToPlaylist,
                    libraryViewModel = libraryViewModel
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
    onAddToPlaylist: (SearchResult) -> Unit,
    libraryViewModel: LibraryViewModel
) {
    Column {
        // Extension header - clean, standard style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                onAddToPlaylist = onAddToPlaylist,
                libraryViewModel = libraryViewModel
            )
            if (track != results.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
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
    onAddToPlaylist: (SearchResult) -> Unit,
    libraryViewModel: LibraryViewModel
) {
    val isLiked = libraryViewModel.isTrackLiked(track.id ?: "")
    val isInAnyPlaylist = libraryViewModel.isTrackInAnyPlaylist(track)
    val observableStates = libraryViewModel.getObservableTrackPlaylistStates(track.id ?: "")
    
    // Check if track is in any playlist (including from observable state)
    val currentIsInAnyPlaylist = observableStates.values.any { it } || isInAnyPlaylist
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                logcat("SearchScreen") { "Song card clicked: ${track.title}" }
                onPlayClick(track)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                if (currentIsInAnyPlaylist) {
                    // Track is in some playlist, open dialog for advanced management
                    onAddToPlaylist(track)
                } else {
                    // First click: Add to liked playlist automatically
                    libraryViewModel.toggleTrackLiked(track)
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            if (currentIsInAnyPlaylist) {
                // Show filled circle with checkmark for tracks in any playlist
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "In playlist - tap to manage playlists",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Show add icon for tracks not in any playlist
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add to liked playlist",
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick(track) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

@Composable
private fun NoResultsContent(query: String) {
    StandardEmptyState(
        icon = {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_xl)),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.search_no_results_title),
        subtitle = "${stringResource(R.string.search_no_results_subtitle).format(query)}\n${stringResource(R.string.search_no_results_hint)}"
    )
}

@Composable
private fun NoExtensionsContent() {
    StandardEmptyState(
        icon = {
            Icon(
                Icons.Outlined.Extension,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_xl)),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = stringResource(R.string.search_no_extensions_title),
        subtitle = stringResource(R.string.search_no_extensions_subtitle)
    )
}

@Composable
private fun EmptySearchContent() {
    StandardEmptyState(
        icon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_xl)),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.search_empty_title),
        subtitle = stringResource(R.string.search_empty_subtitle)
    )
}

private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
} 

@Composable
private fun AddToPlaylistDialog(
    track: SearchResult,
    playlists: List<app.async.domain.model.Playlist>,
    onPlaylistSelected: (app.async.domain.model.Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit,
    libraryViewModel: LibraryViewModel
) {
    val isLiked = libraryViewModel.isTrackLiked(track.id ?: "")
    val playlistMembership = libraryViewModel.getTrackPlaylistMembership(track)
    val observableStates = libraryViewModel.getObservableTrackPlaylistStates(track.id ?: "")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Manage Playlists",
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
                // Liked playlist (always first)
                item {
                    // Get current liked state (can be from observable state or current state)
                    val currentLikedState = observableStates[-1L] ?: isLiked
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { 
                            libraryViewModel.toggleTrackLiked(track)
                            // Update the cached state immediately for UI responsiveness
                            libraryViewModel.updateTrackPlaylistState(
                                track.id ?: "", 
                                -1L, 
                                !currentLikedState
                            )
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLikedState) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (currentLikedState) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (currentLikedState) "Remove from liked" else "Add to liked",
                                tint = if (currentLikedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (currentLikedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (currentLikedState) FontWeight.Medium else FontWeight.Normal
                                )
                                Text(
                                    text = "Your favorite tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Divider
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                
                // Create new playlist option
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCreateNewPlaylist,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Create New Playlist",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Custom playlists
                if (playlists.isNotEmpty()) {
                    items(playlists) { playlist ->
                        // Get membership status from observable state first, then fallback to initial state
                        val isInPlaylist = observableStates[playlist.id] 
                            ?: playlistMembership[playlist.id] 
                            ?: libraryViewModel.getTrackPlaylistState(track.id ?: "", playlist.id)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                libraryViewModel.toggleTrackInPlaylist(playlist.id, track)
                                // Update the cached state immediately for UI responsiveness
                                libraryViewModel.updateTrackPlaylistState(
                                    track.id ?: "", 
                                    playlist.id, 
                                    !isInPlaylist
                                )
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isInPlaylist) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isInPlaylist) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = if (isInPlaylist) "Remove from playlist" else "Add to playlist",
                                    tint = if (isInPlaylist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isInPlaylist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isInPlaylist) FontWeight.Medium else FontWeight.Normal,
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
                                        text = "No custom playlists yet",
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
                Text("Done")
            }
        }
    )
} 
