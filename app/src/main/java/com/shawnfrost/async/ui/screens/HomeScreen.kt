package com.shawnfrost.async.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawnfrost.async.domain.model.Track
import com.shawnfrost.async.ui.viewmodel.HomeViewModel
import com.shawnfrost.async.ui.viewmodel.MusicPlayerViewModel
import com.shawnfrost.async.ui.viewmodel.LikedSongsViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel(),
    likedSongsViewModel: LikedSongsViewModel = hiltViewModel()
) {
    val trendingTracks by viewModel.trendingTracks.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val likedTrackIds by likedSongsViewModel.likedTrackIds.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome to Async",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = "Discover free music",
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                }
                IconButton(
                    onClick = { viewModel.refreshData() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }

        // Error message
        errorMessage?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Featured Banner
        item {
            FeaturedBanner()
        }

        // New Releases Section
        item {
            MusicSection(
                title = "New Releases",
                subtitle = "Latest tracks from Free Music Archive",
                tracks = newReleases,
                likedTrackIds = likedTrackIds,
                onTrackClick = { track -> 
                    musicPlayerViewModel.playTrack(track)
                },
                onLikeClick = { track ->
                    likedSongsViewModel.toggleLikeTrack(track)
                }
            )
        }

        // Trending Section
        item {
            MusicSection(
                title = "Trending",
                subtitle = "Popular tracks right now",
                tracks = trendingTracks,
                likedTrackIds = likedTrackIds,
                onTrackClick = { track -> 
                    musicPlayerViewModel.playTrack(track)
                },
                onLikeClick = { track ->
                    likedSongsViewModel.toggleLikeTrack(track)
                }
            )
        }
    }
}

@Composable
fun FeaturedBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎵 Featured",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Discover high-quality music from Free Music Archive and Internet Archive. All tracks are legally free to stream and download.",
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
fun MusicSection(
    title: String,
    subtitle: String,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    likedTrackIds: Set<String> = emptySet(),
    onLikeClick: (Track) -> Unit = {}
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (tracks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Text(
                    text = "No tracks available. Pull to refresh to load content.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tracks) { track ->
                    TrackCard(
                        track = track,
                        isLiked = likedTrackIds.contains(track.id),
                        onClick = { onTrackClick(track) },
                        onLikeClick = { onLikeClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackCard(
    track: Track,
    isLiked: Boolean = false,
    onClick: () -> Unit,
    onLikeClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(240.dp),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Album art placeholder with modern design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                elevation = 4.dp,
                shape = MaterialTheme.shapes.small,
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = track.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onSurface
            )
            
            Text(
                text = track.artist,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom row with like button and source badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Source badge
                Surface(
                    color = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = track.source,
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
} 