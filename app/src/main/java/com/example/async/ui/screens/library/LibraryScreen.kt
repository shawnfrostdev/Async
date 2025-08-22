package com.example.async.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.async.core.model.SearchResult
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import com.example.async.ui.components.cards.BaseCard
import com.example.async.ui.components.music.CompactTrackItem
import com.example.async.ui.components.music.EmptyTrackList
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

enum class LibraryTab {
    RECENT, FAVORITES, PLAYLISTS, DOWNLOADS
}

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onPlayTrack: (SearchResult) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.RECENT) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header
        HeadlineLarge(
            text = "Library",
            modifier = Modifier.padding(bottom = AppSpacing.m)
        )
        
        // Tab row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            LibraryTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        TitleMedium(
                            text = when (tab) {
                                LibraryTab.RECENT -> "Recent"
                                LibraryTab.FAVORITES -> "Favorites"
                                LibraryTab.PLAYLISTS -> "Playlists"
                                LibraryTab.DOWNLOADS -> "Downloads"
                            }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.m))
        
        // Tab content
        when (selectedTab) {
            LibraryTab.RECENT -> RecentContent(
                onTrackClick = onTrackClick,
                onPlayTrack = onPlayTrack
            )
            LibraryTab.FAVORITES -> FavoritesContent(
                onTrackClick = onTrackClick,
                onPlayTrack = onPlayTrack
            )
            LibraryTab.PLAYLISTS -> PlaylistsContent(
                onPlaylistClick = onPlaylistClick,
                onNavigateToPlaylists = onNavigateToPlaylists
            )
            LibraryTab.DOWNLOADS -> DownloadsContent(
                onTrackClick = onTrackClick,
                onPlayTrack = onPlayTrack
            )
        }
    }
}

@Composable
private fun RecentContent(
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit
) {
    // Sample recent tracks - in real implementation this would come from ViewModel
    val recentTracks = remember {
        listOf(
            SearchResult(
                id = "1",
                extensionId = "spotify",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                duration = 200000,
                thumbnailUrl = null
            ),
            SearchResult(
                id = "2",
                extensionId = "youtube",
                title = "Shape of You",
                artist = "Ed Sheeran",
                album = "Divide",
                duration = 233000,
                thumbnailUrl = null
            ),
            SearchResult(
                id = "3",
                extensionId = "soundcloud",
                title = "Bad Guy",
                artist = "Billie Eilish",
                album = "When We All Fall Asleep, Where Do We Go?",
                duration = 194000,
                thumbnailUrl = null
            )
        )
    }
    
    if (recentTracks.isEmpty()) {
        EmptyTrackList(
            message = "No recently played tracks. Start listening to music to see your history here."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(recentTracks) { track ->
                CompactTrackItem(
                    track = track,
                    onTrackClick = { onTrackClick(track) },
                    onPlayTrack = { onPlayTrack(track) },
                    showExtensionSource = true
                )
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit
) {
    // Sample favorite tracks - in real implementation this would come from ViewModel
    val favoriteTracks = remember {
        listOf(
            SearchResult(
                id = "fav1",
                extensionId = "spotify",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera",
                duration = 354000,
                thumbnailUrl = null
            ),
            SearchResult(
                id = "fav2",
                extensionId = "youtube",
                title = "Stairway to Heaven",
                artist = "Led Zeppelin",
                album = "Led Zeppelin IV",
                duration = 482000,
                thumbnailUrl = null
            )
        )
    }
    
    if (favoriteTracks.isEmpty()) {
        EmptyTrackList(
            message = "No favorite tracks yet. Tap the heart icon on any track to add it to your favorites."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(favoriteTracks) { track ->
                CompactTrackItem(
                    track = track,
                    onTrackClick = { onTrackClick(track) },
                    onPlayTrack = { onPlayTrack(track) },
                    showExtensionSource = true
                )
            }
        }
    }
}

@Composable
private fun PlaylistsContent(
    onPlaylistClick: (Playlist) -> Unit,
    onNavigateToPlaylists: () -> Unit
) {
    // Sample playlists - in real implementation this would come from ViewModel
    val playlists = remember {
        listOf(
            Playlist.createFavoritesPlaylist().copy(trackCount = 25, totalDuration = 6480000),
            Playlist.createUserPlaylist("My Chill Mix", "Perfect for relaxing").copy(
                id = 4,
                trackCount = 15,
                totalDuration = 3600000
            ),
            Playlist.createUserPlaylist("Workout Hits", "High energy tracks for gym").copy(
                id = 5,
                trackCount = 20,
                totalDuration = 4800000
            )
        )
    }
    
    Column {
        // View all playlists button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleMedium("Your Playlists")
            
            TextButton(onClick = onNavigateToPlaylists) {
                LabelMedium("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.s))
        
        if (playlists.isEmpty()) {
            EmptyTrackList(
                message = "No playlists yet. Create your first playlist to organize your music."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                items(playlists.take(5)) { playlist -> // Show only first 5
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
                .padding(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            // Playlist artwork placeholder
            Card(
                modifier = Modifier.size(48.dp),
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TitleMedium(
                    text = playlist.getDisplayName(),
                    maxLines = 1
                )
                
                LabelMedium(
                    text = "${playlist.getTrackCountText()} • ${playlist.getFormattedDuration()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
private fun DownloadsContent(
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit
) {
    // Sample downloaded tracks - in real implementation this would come from ViewModel
    val downloadedTracks = remember { emptyList<SearchResult>() }
    
    if (downloadedTracks.isEmpty()) {
        EmptyTrackList(
            message = "No downloaded tracks. Download music for offline listening."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(downloadedTracks) { track ->
                CompactTrackItem(
                    track = track,
                    onTrackClick = { onTrackClick(track) },
                    onPlayTrack = { onPlayTrack(track) },
                    showExtensionSource = true
                )
            }
        }
    }
} 