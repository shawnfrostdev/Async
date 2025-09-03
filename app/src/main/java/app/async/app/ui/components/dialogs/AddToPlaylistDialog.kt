package app.async.app.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.async.core.model.SearchResult
import app.async.app.ui.vm.LibraryViewModel

@Composable
fun AddToPlaylistDialog(
    track: SearchResult,
    playlists: List<app.async.domain.model.Playlist>,
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