package com.example.async.ui.screens.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.async.domain.model.Playlist
import com.example.async.ui.components.cards.BaseCard
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    onEditPlaylist: (Playlist) -> Unit = {},
    onDeletePlaylist: (Playlist) -> Unit = {}
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Playlist?>(null) }
    
    // Sample data - in real implementation this would come from ViewModel
    val samplePlaylists = remember {
        listOf(
            Playlist.createFavoritesPlaylist().copy(trackCount = 25, totalDuration = 6480000),
            Playlist.createRecentlyPlayedPlaylist().copy(trackCount = 50, totalDuration = 12750000),
            Playlist.createMostPlayedPlaylist().copy(trackCount = 30, totalDuration = 7920000),
            Playlist.createUserPlaylist("My Chill Mix", "Perfect for relaxing").copy(
                id = 4,
                trackCount = 15,
                totalDuration = 3600000
            ),
            Playlist.createUserPlaylist("Workout Hits", "High energy tracks for gym").copy(
                id = 5,
                trackCount = 20,
                totalDuration = 4800000
            ),
            Playlist.createUserPlaylist("Study Focus", "Instrumental tracks for concentration").copy(
                id = 6,
                trackCount = 12,
                totalDuration = 2880000
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header with create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeadlineLarge(
                text = "Playlists",
                modifier = Modifier.weight(1f)
            )
            
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Create Playlist"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.m))
        
        // Playlists list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            items(samplePlaylists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    onPlay = { onPlaylistClick(playlist) },
                    onEdit = if (playlist.isModifiable()) { { onEditPlaylist(playlist) } } else null,
                    onDelete = if (playlist.isModifiable()) { { showDeleteDialog = playlist } } else null
                )
            }
        }
    }
    
    // Create playlist dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                showCreateDialog = false
                onCreatePlaylist()
                // TODO: Pass name and description to create function
            }
        )
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { playlist ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { TitleMedium("Delete Playlist") },
            text = { BodyMedium("Are you sure you want to delete \"${playlist.getDisplayName()}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(playlist)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    TitleMedium("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    TitleMedium("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    BaseCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            // Playlist artwork placeholder
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                TitleMedium(
                    text = playlist.getDisplayName(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!playlist.getDisplayDescription().equals("No description", ignoreCase = true)) {
                    BodySmall(
                        text = playlist.getDisplayDescription(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelMedium(
                        text = playlist.getTrackCountText(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (playlist.totalDuration > 0) {
                        LabelMedium(
                            text = " • ${playlist.getFormattedDuration()}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (playlist.isSystemPlaylist) {
                        LabelMedium(
                            text = " • System",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Play button
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play Playlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // More options menu
            if (onEdit != null || onDelete != null) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        onEdit?.let {
                            DropdownMenuItem(
                                text = { BodyMedium("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Edit, contentDescription = null)
                                }
                            )
                        }
                        
                        onDelete?.let {
                            DropdownMenuItem(
                                text = { 
                                    BodyMedium(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
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
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMedium("Create Playlist") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = Playlist.getNameValidationError(it)
                    },
                    label = { BodyMedium("Playlist Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { BodySmall(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { BodyMedium("Description (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = nameError == null && name.isNotBlank()
            ) {
                TitleMedium("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                TitleMedium("Cancel")
            }
        }
    )
} 