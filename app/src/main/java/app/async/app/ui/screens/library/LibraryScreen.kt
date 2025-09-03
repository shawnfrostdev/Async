package app.async.app.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import app.async.R
import app.async.core.model.SearchResult
import app.async.domain.model.Playlist
import app.async.domain.model.Track
import app.async.app.ui.vm.LibraryViewModel
import app.async.app.ui.vm.LibraryUiState
import app.async.app.ui.components.AppText
import app.async.app.ui.components.AppCards
import app.async.app.ui.components.layout.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val uiState = libraryViewModel.uiState
    
    StandardScreenLayout(
        hasTopBar = false
    ) {
        // Header with title and create playlist button
        StandardScreenHeader(
            title = stringResource(R.string.title_library),
            actions = {
                IconButton(
                    onClick = { 
                        // Show create playlist dialog
                        libraryViewModel.showCreatePlaylistDialog()
                    },
                    modifier = Modifier.size(dimensionResource(R.dimen.min_touch_target))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.library_create_playlist),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                    )
                }
            }
        )
        
        // System playlists content
        SystemPlaylistsContent(
            uiState = uiState,
            onTrackClick = onTrackClick,
            onRemoveLiked = { track ->
                val searchResult = SearchResult(
                    id = track.externalId,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration,
                    extensionId = track.extensionId,
                    thumbnailUrl = track.thumbnailUrl
                )
                libraryViewModel.toggleTrackLiked(searchResult)
            },
            onDeleteDownload = { libraryViewModel.deleteDownloadedTrack(it) },
            onRetry = { libraryViewModel.refresh() },
            onPlaylistClick = onPlaylistClick,
            onEditPlaylist = { playlist -> libraryViewModel.showEditPlaylistDialog(playlist) },
            onDeletePlaylist = { playlist -> libraryViewModel.showDeleteConfirmationDialog(playlist) }
        )
        
        // Create Playlist Dialog
        if (uiState.showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onCreatePlaylist = { name, description ->
                    libraryViewModel.createPlaylist(name, description)
                    libraryViewModel.hideCreatePlaylistDialog()
                },
                onDismiss = {
                    libraryViewModel.hideCreatePlaylistDialog()
                }
            )
        }
        
        // Edit Playlist Dialog
        if (uiState.showEditPlaylistDialog && uiState.editingPlaylist != null) {
            EditPlaylistDialog(
                playlist = uiState.editingPlaylist!!,
                onUpdatePlaylist = { name, description ->
                    libraryViewModel.updatePlaylist(uiState.editingPlaylist!!.id, name, description)
                    libraryViewModel.hideEditPlaylistDialog()
                },
                onDismiss = {
                    libraryViewModel.hideEditPlaylistDialog()
                }
            )
        }
        
        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmationDialog && uiState.playlistToDelete != null) {
            DeletePlaylistConfirmationDialog(
                playlist = uiState.playlistToDelete!!,
                onConfirmDelete = {
                    libraryViewModel.deletePlaylist(uiState.playlistToDelete!!.id)
                    libraryViewModel.hideDeleteConfirmationDialog()
                },
                onDismiss = {
                    libraryViewModel.hideDeleteConfirmationDialog()
                }
            )
        }
    }
}



@Composable
private fun SystemPlaylistsContent(
    uiState: LibraryUiState,
    onTrackClick: (SearchResult) -> Unit,
    onRemoveLiked: (Track) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onRetry: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onEditPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    when {
        uiState.isLoading -> {
            StandardLoadingState(
                message = stringResource(R.string.loading_library)
            )
        }
        uiState.error != null -> {
            StandardErrorState(
                title = stringResource(R.string.error_loading_library),
                message = uiState.error,
                onRetryClick = onRetry
            )
        }
        else -> {
            StandardLazyColumn(
                hasTopBar = false,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                // Liked Songs System Playlist
                item {
                    SystemPlaylistCard(
                        title = "Liked Songs",
                        description = "Your favorite tracks",
                        trackCount = uiState.likedTracks.size,
                        icon = Icons.Outlined.Favorite,
                        iconTint = MaterialTheme.colorScheme.error,
                        tracks = uiState.likedTracks,
                        onTrackClick = onTrackClick,
                        onRemoveTrack = onRemoveLiked,
                        onPlaylistClick = { onPlaylistClick(Playlist(id = -1L, name = "Liked Songs", description = "Your favorite tracks")) }
                    )
                }
                
                // Downloads System Playlist
                item {
                    SystemPlaylistCard(
                        title = "Downloads",
                        description = "Your downloaded music",
                        trackCount = uiState.downloadedTracks.size,
                        icon = Icons.Outlined.Download,
                        iconTint = MaterialTheme.colorScheme.primary,
                        tracks = uiState.downloadedTracks,
                        onTrackClick = onTrackClick,
                        onRemoveTrack = { track ->
                            onDeleteDownload(track.id.toString())
                        },
                        onPlaylistClick = { onPlaylistClick(Playlist(id = -2L, name = "Downloads", description = "Your downloaded music")) }
                    )
                }
                
                // Custom Playlists Section
                if (uiState.customPlaylists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        AppText.TitleMedium(
                            text = "Your Playlists",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(uiState.customPlaylists) { playlist ->
                        CustomPlaylistCard(
                            playlist = playlist,
                            onPlaylistClick = { onPlaylistClick(playlist) },
                            onEditPlaylist = { onEditPlaylist(playlist) },
                            onDeletePlaylist = { onDeletePlaylist(playlist) }
                        )
                    }
                }
            }
        }
    }
}









// Enhanced Playlist Card with selection support
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            // Playlist icon
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
                    Icon(
                        Icons.AutoMirrored.Outlined.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${playlist.trackCount} tracks • ${formatDuration(playlist.totalDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                playlist.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Menu button (only when not in selection mode)
            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun EmptyPlaylistsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Your Playlists are Empty",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Create your first playlist to get started",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { /* TODO: Navigate to create playlist */ }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Playlist")
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Favorite,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Your Favorites are Empty",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Add tracks to your favorites to see them here",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { /* TODO: Navigate to player */ }) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play Music")
            }
        }
    }
}

@Composable
private fun EmptyRecentContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Your Recent History is Empty",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Play some music to see it here",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { /* TODO: Navigate to player */ }) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play Music")
            }
        }
    }
}

@Composable
private fun EmptyArtistsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Your Favorite Artists are Empty",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Add artists to your favorites to see them here",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { /* TODO: Navigate to artists */ }) {
                Icon(Icons.Outlined.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Browse Artists")
            }
        }
    }
}





@Composable
private fun FavoriteTrackCard(
    track: Track,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = false, // No selection for tracks
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist icon
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
                    Icon(
                        Icons.AutoMirrored.Outlined.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                
                Text(
                    text = track.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
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
            
            // Remove favorite button
            IconButton(onClick = onRemoveFavorite) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}



private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return when {
        hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
        else -> "%d:%02d".format(minutes, seconds)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Library tab enumeration
 */




@Composable
private fun SystemPlaylistCard(
    title: String,
    description: String,
    trackCount: Int,
    icon: ImageVector,
    iconTint: Color,
    tracks: List<Track>,
    onTrackClick: (SearchResult) -> Unit,
    onRemoveTrack: (Track) -> Unit,
    onPlaylistClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onPlaylistClick
    ) {
        // Playlist header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist icon
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = iconTint.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleMedium(text = title)
                
                Spacer(modifier = Modifier.height(2.dp))
                
                AppText.BodySmall(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                AppText.BodySmall(
                    text = "$trackCount ${if (trackCount == 1) "track" else "tracks"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Arrow icon to indicate it's clickable
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Open playlist",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
private fun TrackCard(
    track: Track,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
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
            
            Text(
                text = track.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
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
        
        // Actions
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
        } else {
            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Remove from liked",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DownloadedTrackCard(
    track: Track,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    Icons.Outlined.Download,
                    contentDescription = "Downloaded",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = track.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
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
        
        // Actions
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
        } else {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ======== CUSTOM PLAYLIST COMPONENTS ========

@Composable
private fun CustomPlaylistCard(
    playlist: Playlist,
    onPlaylistClick: () -> Unit,
    onEditPlaylist: () -> Unit,
    onDeletePlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlaylistClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist icon
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleMedium(
                    text = playlist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                AppText.BodySmall(
                    text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Menu button with dropdown
            Box {
            IconButton(
                    onClick = { showMenu = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "Playlist options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEditPlaylist()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDeletePlaylist()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onCreatePlaylist: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    
    // Character limits
    val maxNameLength = 50
    val maxDescriptionLength = 200
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText.TitleMedium(text = "Create Playlist")
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
                        onCreatePlaylist(playlistName.trim(), playlistDescription.trim())
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 

@Composable
private fun EditPlaylistDialog(
    playlist: Playlist,
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

@Composable
private fun DeletePlaylistConfirmationDialog(
    playlist: Playlist,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText.TitleMedium(text = "Delete Playlist")
        },
        text = {
            Column {
                AppText.BodyMedium(
                    text = "Are you sure you want to delete \"${playlist.name}\"?"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppText.BodySmall(
                    text = "This action cannot be undone. All tracks in this playlist will be removed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 
