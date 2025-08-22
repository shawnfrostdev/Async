package com.example.async.ui.screens.library

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
import com.async.domain.model.Playlist
import com.example.async.ui.components.cards.BaseCard
import com.example.async.ui.components.music.EmptyTrackList
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onCreatePlaylist: () -> Unit = {}
) {
    // Sample playlists - in real implementation this would come from ViewModel
    val playlists = remember {
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
            ),
            Playlist.createUserPlaylist("Road Trip Vibes", "Songs for long drives").copy(
                id = 7,
                trackCount = 35,
                totalDuration = 8400000
            ),
            Playlist.createUserPlaylist("Throwback Thursday", "Nostalgic hits from the past").copy(
                id = 8,
                trackCount = 40,
                totalDuration = 9600000
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
                text = "Library",
                modifier = Modifier.weight(1f)
            )
            
            FloatingActionButton(
                onClick = onCreatePlaylist,
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
        if (playlists.isEmpty()) {
            EmptyTrackList(
                message = "No playlists yet. Create your first playlist to organize your music."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                items(playlists) { playlist ->
                    PlaylistLibraryCard(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistLibraryCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        imageVector = if (playlist.isSystemPlaylist) {
                            when (playlist.name) {
                                Playlist.FAVORITES -> Icons.Outlined.Favorite
                                Playlist.RECENTLY_PLAYED -> Icons.Outlined.History
                                Playlist.MOST_PLAYED -> Icons.AutoMirrored.Outlined.TrendingUp
                                else -> Icons.Outlined.PlayArrow
                            }
                        } else {
                            Icons.AutoMirrored.Outlined.PlaylistPlay
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
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
                    BodyMedium(
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
            
            // Forward arrow
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 