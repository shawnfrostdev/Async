package com.async.app.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.domain.model.Playlist
import com.async.app.ui.vm.LibraryViewModel
import com.async.app.ui.components.AppText
import com.async.app.ui.components.AppCards

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val uiState = libraryViewModel.uiState
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        libraryViewModel.loadPlaylists()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText.TitleLarge(text = "Your Library")
            
            IconButton(onClick = { showCreatePlaylistDialog = true }) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Create playlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            // Loading state
            AppCards.LoadingCard(
                title = "Loading Library...",
                modifier = Modifier.fillMaxSize()
            )
        } else if (uiState.error != null) {
            // Error state
            AppCards.ErrorCard(
                title = "Error Loading Library",
                message = uiState.error ?: "Unknown error occurred",
                onRetry = { libraryViewModel.loadPlaylists() },
                modifier = Modifier.fillMaxWidth()
            )
        } else if (uiState.playlists.isEmpty()) {
            // Empty state
            EmptyLibraryContent(
                onCreatePlaylist = { showCreatePlaylistDialog = true }
            )
        } else {
            // Content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText.TitleMedium(text = "Playlists")
                        AppText.LabelMedium(
                            text = "${uiState.playlists.size} playlists",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                items(uiState.playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        onDeleteClick = { libraryViewModel.deletePlaylist(playlist.id.toString()) },
                        onEditClick = { /* TODO: Edit playlist */ }
                    )
                }
            }
        }
    }
    
    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, description ->
                libraryViewModel.createPlaylist(name, description)
                showCreatePlaylistDialog = false
                onCreatePlaylist()
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleMedium(
                    text = playlist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (playlist.description?.isNotEmpty() == true) {
                    AppText.BodyMedium(
                        text = playlist.description!!,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AppText.LabelMedium(
                    text = "${playlist.trackCount} tracks • ${formatDuration(playlist.totalDuration)}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // Menu button
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
                        text = { Text("Edit") },
                        onClick = {
                            onEditClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                        }
                    )
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

@Composable
private fun EmptyLibraryContent(
    onCreatePlaylist: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Your Library is Empty",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Create your first playlist to get started",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onCreatePlaylist) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Playlist")
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
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

private fun formatDuration(durationMs: Long): String {
    val hours = (durationMs / 1000) / 3600
    val minutes = ((durationMs / 1000) % 3600) / 60
    
    return if (hours > 0) {
        String.format("%dh %dm", hours, minutes)
    } else {
        String.format("%dm", minutes)
    }
} 
