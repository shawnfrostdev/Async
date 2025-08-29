package com.async.app.ui.screens.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.AsyncImage
import com.async.core.model.SearchResult
import com.async.app.ui.vm.PlayerViewModel
import com.async.app.ui.vm.LibraryViewModel
import com.async.app.ui.vm.RepeatMode
import com.async.app.ui.components.AppText
import logcat.logcat
import androidx.compose.ui.text.font.FontWeight

enum class PlayerTab {
    NOW_PLAYING, QUEUE
}

// Voyager Screen wrapper for navigation
class PlayerScreen(private val playerViewModel: PlayerViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        
        // Safe back navigation with error handling
        val safeNavigateBack = remember {
            {
                try {
                    if (navigator?.canPop == true) {
                        navigator.pop()
                    }
                } catch (e: Exception) {
                    // If navigation fails, we'll just do nothing
                    // This prevents crashes while maintaining user experience
                    logcat("PlayerScreen") { "Navigation error: ${e.message}" }
                }
            }
        }
        
        PlayerScreenContent(
            onNavigateBack = safeNavigateBack,
            playerViewModel = playerViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreenContent(
    onNavigateBack: () -> Unit = {},
    playerViewModel: PlayerViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val uiState = playerViewModel.uiState
    val libraryUiState = libraryViewModel.uiState
    var selectedTab by remember { mutableStateOf(PlayerTab.NOW_PLAYING) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (uiState.currentTrack == null) {
            // No track playing state
            EmptyPlayerContent(onNavigateBack = onNavigateBack)
        } else {
            // Player content with scrollable layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Album Art (centered)
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .size(320.dp)
                                .clip(MaterialTheme.shapes.large),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.currentTrack?.thumbnailUrl != null) {
                                    AsyncImage(
                                        model = uiState.currentTrack.thumbnailUrl,
                                        contentDescription = "Album Art",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Track Info (left aligned) with Add to Playlist and Queue Icons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Song name (reduced size)
                            Text(
                                text = uiState.currentTrack?.title ?: "Unknown Title",
                                style = MaterialTheme.typography.titleLarge, // Changed from headlineSmall
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Artist - Album
                            val artistAlbum = buildString {
                                append(uiState.currentTrack?.artist ?: "Unknown Artist")
                                uiState.currentTrack?.album?.let { album ->
                                    append(" - ")
                                    append(album)
                                }
                            }
                            Text(
                                text = artistAlbum,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Add to Playlist Icon
                        IconButton(
                            onClick = { 
                                if (uiState.currentTrack != null) {
                                    val isInAnyPlaylist = libraryViewModel.isTrackInAnyPlaylist(uiState.currentTrack!!)
                                    if (isInAnyPlaylist) {
                                        // Track is in some playlist, open dialog for advanced management
                                        showAddToPlaylistDialog = true
                                    } else {
                                        // First click: Add to liked playlist automatically
                                        libraryViewModel.toggleTrackLiked(uiState.currentTrack!!)
                                    }
                                }
                            }
                        ) {
                            if (uiState.currentTrack != null) {
                                val isInAnyPlaylist = libraryViewModel.isTrackInAnyPlaylist(uiState.currentTrack!!)
                                if (isInAnyPlaylist) {
                                    // Show filled circle with checkmark for tracks in any playlist
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "In playlist - tap to manage playlists",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    // Show add icon for tracks not in any playlist
                                    Icon(
                                        Icons.Outlined.Add,
                                        contentDescription = "Add to liked playlist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Outlined.PlaylistAdd,
                                    contentDescription = "Add to playlist",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Queue Icon (moved next to add to playlist)
                        IconButton(onClick = { /* TODO: Queue */ }) {
                            Icon(
                                Icons.Outlined.QueueMusic,
                                contentDescription = "Queue",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Progress Bar
                item {
                    Column {
                        Slider(
                            value = if (uiState.duration > 0) {
                                uiState.currentPosition.toFloat() / uiState.duration.toFloat()
                            } else 0f,
                            onValueChange = { progress ->
                                if (uiState.duration > 0) {
                                    val newPosition = (progress * uiState.duration).toLong()
                                    playerViewModel.seekTo(newPosition)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Time labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(uiState.currentPosition),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(uiState.duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Control Buttons Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = { playerViewModel.toggleShuffle() }
                        ) {
                            Icon(
                                Icons.Outlined.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (uiState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Previous
                        IconButton(
                            onClick = { playerViewModel.skipPrevious() }
                        ) {
                            Icon(
                                Icons.Outlined.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Play/Pause (larger)
                        IconButton(
                            onClick = { playerViewModel.playPause() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Next
                        IconButton(
                            onClick = { playerViewModel.skipNext() }
                        ) {
                            Icon(
                                Icons.Outlined.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Repeat
                        IconButton(
                            onClick = { playerViewModel.toggleRepeat() }
                        ) {
                            Icon(
                                when (uiState.repeatMode) {
                                    RepeatMode.ALL -> Icons.Outlined.Repeat
                                    RepeatMode.ONE -> Icons.Outlined.RepeatOne
                                    else -> Icons.Outlined.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = if (uiState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Lyrics Card (placeholder) - now scrollable
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Increased height for better scrolling experience
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Outlined.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Lyrics",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scroll to see more content below",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Additional content to demonstrate scrolling
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Track Information",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Additional details and metadata will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Add to Playlist Dialog
    if (showAddToPlaylistDialog && uiState.currentTrack != null) {
        AddToPlaylistDialog(
            track = uiState.currentTrack!!,
            playlists = libraryUiState.customPlaylists,
            onPlaylistSelected = { playlist ->
                libraryViewModel.addTrackToPlaylist(playlist.id, uiState.currentTrack!!)
                showAddToPlaylistDialog = false
            },
            onCreateNewPlaylist = {
                libraryViewModel.showCreatePlaylistDialog()
                showAddToPlaylistDialog = false
            },
            onDismiss = {
                showAddToPlaylistDialog = false
            },
            libraryViewModel = libraryViewModel
        )
    }
}

@Composable
private fun EmptyPlayerContent(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "No Track Playing",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Select a track to start playing music",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateBack) {
                Text("Browse Music")
            }
        }
    }
}

@Composable
private fun NowPlayingContent(
    track: SearchResult,
    onPlayTrack: (SearchResult) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AppText.TitleMedium(text = "Track Details")
                Spacer(modifier = Modifier.height(8.dp))
                
                if (track.album != null) {
                    DetailRow("Album", track.album!!)
                }
                if (track.duration != null) {
                    DetailRow("Duration", formatTime(track.duration!!))
                }
                DetailRow("Source", track.extensionId.replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
private fun QueueContent(
    queue: List<SearchResult>,
    currentIndex: Int,
    onQueueTrackClick: (SearchResult) -> Unit,
    onRemoveFromQueue: (Int) -> Unit
) {
    if (queue.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.PlaylistAdd,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppText.TitleMedium(
                    text = "No Queue",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                AppText.BodyMedium(
                    text = "Add tracks to start playing",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(queue.withIndex().toList()) { (index, track) ->
                QueueTrackItem(
                    track = track,
                    isCurrentTrack = index == currentIndex,
                    onClick = { onQueueTrackClick(track) },
                    onRemove = { onRemoveFromQueue(index) }
                )
            }
        }
    }
}

@Composable
private fun QueueTrackItem(
    track: SearchResult,
    isCurrentTrack: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleMedium(
                    text = track.title ?: "Unknown Title",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                AppText.BodySmall(
                    text = track.artist ?: "Unknown Artist",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (track.duration != null) {
                AppText.LabelMedium(
                    text = formatTime(track.duration!!),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Remove from queue",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText.BodyMedium(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        AppText.BodyMedium(
            text = value,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper function to format time
private fun formatTime(timeMs: Long): String {
    val seconds = (timeMs / 1000) % 60
    val minutes = (timeMs / (1000 * 60)) % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun AddToPlaylistDialog(
    track: SearchResult,
    playlists: List<com.async.domain.model.Playlist>,
    onPlaylistSelected: (com.async.domain.model.Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit,
    libraryViewModel: LibraryViewModel
) {
    val isLiked = libraryViewModel.isTrackLiked(track.id ?: "")
    val playlistMembership = libraryViewModel.getTrackPlaylistMembership(track)
    
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { 
                            libraryViewModel.toggleTrackLiked(track)
                            // Update the cached state immediately for UI responsiveness
                            libraryViewModel.updateTrackPlaylistState(
                                track.id ?: "", 
                                -1L, 
                                !isLiked
                            )
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLiked) 
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
                                if (isLiked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (isLiked) "Remove from liked" else "Add to liked",
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isLiked) FontWeight.Medium else FontWeight.Normal
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
                        // Get membership status from the map or cached state
                        val isInPlaylist = playlistMembership[playlist.id] ?: 
                                          libraryViewModel.getTrackPlaylistState(track.id ?: "", playlist.id)
                        
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
