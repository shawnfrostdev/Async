package app.async.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import app.async.app.ui.components.AppText
import app.async.app.ui.components.AppCards
import app.async.domain.model.Track
import app.async.core.model.SearchResult
import app.async.app.ui.vm.LibraryViewModel

enum class SortOption(val displayName: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    RECENTLY_ADDED("Recently Added")
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

/**
 * Playlist detail screen showing tracks with same design as search results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    playlistDescription: String?,
    playlistIcon: androidx.compose.ui.graphics.vector.ImageVector,
    playlistIconTint: Color,
    tracks: List<Track>,
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onTrackMenuClick: (Track) -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onEditPlaylist: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    var showSortBottomSheet by remember { mutableStateOf(false) }
    var currentSortOption by remember { mutableStateOf(SortOption.RECENTLY_ADDED) }
    var currentSortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }
    
    // Sort tracks based on current selection
    val sortedTracks = remember(tracks, currentSortOption, currentSortDirection) {
        when (currentSortOption) {
            SortOption.TITLE -> {
                if (currentSortDirection == SortDirection.ASCENDING) {
                    tracks.sortedBy { it.title.lowercase() }
                } else {
                    tracks.sortedByDescending { it.title.lowercase() }
                }
            }
            SortOption.ARTIST -> {
                if (currentSortDirection == SortDirection.ASCENDING) {
                    tracks.sortedBy { it.artist?.lowercase() ?: "" }
                } else {
                    tracks.sortedByDescending { it.artist?.lowercase() ?: "" }
                }
            }
            SortOption.ALBUM -> {
                if (currentSortDirection == SortDirection.ASCENDING) {
                    tracks.sortedBy { it.album?.lowercase() ?: "" }
                } else {
                    tracks.sortedByDescending { it.album?.lowercase() ?: "" }
                }
            }
            SortOption.RECENTLY_ADDED -> {
                if (currentSortDirection == SortDirection.ASCENDING) {
                    tracks.sortedBy { it.dateAdded }
                } else {
                    tracks.sortedByDescending { it.dateAdded }
                }
            }
        }
    }

    // Box to overlay floating back button over scrolling content
    Box(modifier = modifier.fillMaxSize()) {
        // Single scrollable container for entire playlist screen
        when {
            isLoading -> {
                LoadingPlaylistContent()
            }
            sortedTracks.isEmpty() -> {
                // For empty state, we still want the header + empty content in one scroll
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 56.dp, bottom = 16.dp) // Top padding for floating back button
                ) {
                    item {
                        PlaylistHeader(
                            name = playlistName,
                            description = playlistDescription,
                            trackCount = sortedTracks.size,
                            icon = playlistIcon,
                            iconTint = playlistIconTint,
                            onShuffleClick = onShuffleClick,
                            onPlayClick = onPlayClick,
                            onSortClick = { showSortBottomSheet = true },
                            onEditClick = onEditPlaylist
                        )
                    }
                    item {
                        EmptyPlaylistContent(
                            message = "No tracks in this playlist yet"
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 56.dp, bottom = 16.dp) // Top padding for floating back button
                ) {
                    // Header as first item
                    item {
                        PlaylistHeader(
                            name = playlistName,
                            description = playlistDescription,
                            trackCount = sortedTracks.size,
                            icon = playlistIcon,
                            iconTint = playlistIconTint,
                            onShuffleClick = onShuffleClick,
                            onPlayClick = onPlayClick,
                            onSortClick = { showSortBottomSheet = true },
                            onEditClick = onEditPlaylist
                        )
                    }
                    
                    // Tracks as subsequent items
                    items(sortedTracks) { track ->
                        PlaylistTrackCard(
                            track = track,
                            onClick = {
                                val searchResult = SearchResult(
                                    id = track.externalId,
                                    title = track.title,
                                    artist = track.artist,
                                    album = track.album,
                                    duration = track.duration,
                                    extensionId = track.extensionId,
                                    thumbnailUrl = track.thumbnailUrl
                                )
                                onTrackClick(searchResult)
                            },
                            onMenuClick = { onTrackMenuClick(track) }
                        )
                        if (track != sortedTracks.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        
        // Floating back button
        FloatingActionButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
    
    // Sort bottom sheet
    if (showSortBottomSheet) {
        SortBottomSheet(
            currentSortOption = currentSortOption,
            currentSortDirection = currentSortDirection,
            onSortOptionSelected = { option, direction ->
                currentSortOption = option
                currentSortDirection = direction
            },
            onDismiss = { showSortBottomSheet = false }
        )
    }
    
    // Edit Playlist Dialog
    if (viewModel.uiState.showEditPlaylistDialog && viewModel.uiState.editingPlaylist != null) {
        EditPlaylistDialog(
            playlist = viewModel.uiState.editingPlaylist!!,
            onUpdatePlaylist = { name, description ->
                viewModel.updatePlaylist(viewModel.uiState.editingPlaylist!!.id, name, description)
                viewModel.hideEditPlaylistDialog()
            },
            onDismiss = {
                viewModel.hideEditPlaylistDialog()
            }
        )
    }
}

@Composable
private fun PlaylistHeader(
    name: String,
    description: String?,
    trackCount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onShuffleClick: () -> Unit,
    onPlayClick: () -> Unit,
    onSortClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Playlist cover image (centered)
        Card(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            colors = CardDefaults.cardColors(
                containerColor = iconTint.copy(alpha = 0.1f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = iconTint
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name and controls row (restored original layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Playlist name and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleLarge(
                    text = name,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Description if available (without "Custom playlist" fallback)
                description?.let { desc ->
                    if (desc.isNotBlank() && desc != "Custom playlist") {
                        AppText.BodyMedium(
                            text = desc,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Combined row with Sort, Edit, Track Count on left and Shuffle, Play on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Sort, Edit, Track Count buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sort button with text and background
                        Card(
                            onClick = onSortClick,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(50) // Full round corners
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sort",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Edit button with text and background
                        Card(
                            onClick = onEditClick,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(50) // Full round corners
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Track count with matching background and styling
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(50) // Full round corners
                        ) {
                            Text(
                                text = "$trackCount ${if (trackCount == 1) "track" else "tracks"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Right side - Shuffle and Play buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Shuffle button
                        IconButton(
                            onClick = onShuffleClick,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shuffle,
                                contentDescription = "Shuffle",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Play button
                        IconButton(
                            onClick = onPlayClick,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistTrackCard(
    track: Track,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track artwork with real album cover
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
                        contentDescription = "Album Art",
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
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            track.artist?.let { artist ->
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            track.album?.let { album ->
                Text(
                    text = album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Duration
        track.duration?.let { duration ->
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        
        // Three dots menu button
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Outlined.MoreVert,
                contentDescription = "Track options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LoadingPlaylistContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppCards.LoadingCard(
            title = "Loading tracks...",
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun EmptyPlaylistContent(
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Use a consistent empty playlist icon - music off icon
        Icon(
            imageVector = Icons.Outlined.MusicOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppText.TitleMedium(
            text = "Empty Playlist",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppText.BodyMedium(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    currentSortOption: SortOption,
    currentSortDirection: SortDirection,
    onSortOptionSelected: (SortOption, SortDirection) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = null,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and done button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText.TitleMedium(
                    text = "Sort by",
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort options
            SortOption.values().forEach { option ->
                SortOptionItem(
                    option = option,
                    isSelected = option == currentSortOption,
                    currentDirection = if (option == currentSortOption) currentSortDirection else SortDirection.ASCENDING,
                    onClick = { direction ->
                        onSortOptionSelected(option, direction)
                    }
                )
                
                if (option != SortOption.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Bottom padding for safe area
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SortOptionItem(
    option: SortOption,
    isSelected: Boolean,
    currentDirection: SortDirection,
    onClick: (SortDirection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (isSelected) {
                // Toggle direction if already selected
                val newDirection = if (currentDirection == SortDirection.ASCENDING) {
                    SortDirection.DESCENDING
                } else {
                    SortDirection.ASCENDING
                }
                onClick(newDirection)
            } else {
                // Select with ascending direction by default
                onClick(SortDirection.ASCENDING)
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    imageVector = if (currentDirection == SortDirection.ASCENDING) {
                        Icons.Outlined.KeyboardArrowUp
                    } else {
                        Icons.Outlined.KeyboardArrowDown
                    },
                    contentDescription = if (currentDirection == SortDirection.ASCENDING) {
                        "Ascending"
                    } else {
                        "Descending"
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 

@Composable
private fun EditPlaylistDialog(
    playlist: app.async.domain.model.Playlist,
    onUpdatePlaylist: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var playlistName by remember { mutableStateOf(playlist.name) }
    var playlistDescription by remember { mutableStateOf(playlist.description ?: "") }
    // Character limits
    val maxNameLength = 50
    val maxDescriptionLength = 200
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText.TitleMedium(text = "Edit Playlist")
        },
        text = {
            Column {
                // Playlist name field
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { if (it.length <= maxNameLength) playlistName = it },
                    label = { Text("Playlist Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "${playlistName.length}/$maxNameLength",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (playlistName.length > maxNameLength * 0.9) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description field (optional)
                OutlinedTextField(
                    value = playlistDescription,
                    onValueChange = { if (it.length <= maxDescriptionLength) playlistDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = {
                        Text(
                            text = "${playlistDescription.length}/$maxDescriptionLength",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (playlistDescription.length > maxDescriptionLength * 0.9) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onUpdatePlaylist(playlistName.trim(), playlistDescription.trim())
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 
