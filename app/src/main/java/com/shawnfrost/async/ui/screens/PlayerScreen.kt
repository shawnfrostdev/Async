package com.shawnfrost.async.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawnfrost.async.ui.viewmodel.MusicPlayerViewModel
import com.shawnfrost.async.ui.viewmodel.LikedSongsViewModel

@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel(),
    likedSongsViewModel: LikedSongsViewModel = hiltViewModel()
) {
    val currentTrack by musicPlayerViewModel.currentTrack.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val playbackPosition by musicPlayerViewModel.playbackPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()
    val likedTrackIds by likedSongsViewModel.likedTrackIds.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.primary.copy(alpha = 0.3f),
                        MaterialTheme.colors.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
                
                Text(
                    text = "Now Playing",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.onBackground
                )
                
                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            currentTrack?.let { track ->
                // Album art
                Card(
                    modifier = Modifier.size(320.dp),
                    elevation = 16.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Album Art",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Track info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = track.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = track.artist,
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause
                    Card(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        IconButton(
                            onClick = { musicPlayerViewModel.togglePlayPause() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                    
                    // Like button
                    IconButton(
                        onClick = { 
                            likedSongsViewModel.toggleLikeTrack(track)
                        }
                    ) {
                        Icon(
                            imageVector = if (likedTrackIds.contains(track.id)) 
                                Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (likedTrackIds.contains(track.id)) "Unlike" else "Like",
                            tint = if (likedTrackIds.contains(track.id)) 
                                MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            } ?: run {
                // No track playing
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicOff,
                            contentDescription = "No Music",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No track playing",
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
} 