package com.shawnfrost.async.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawnfrost.async.domain.model.Track
import com.shawnfrost.async.ui.viewmodel.MusicPlayerViewModel

@Composable
fun MiniPlayer(
    musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel(),
    onExpandClick: () -> Unit = {}
) {
    val currentTrack by musicPlayerViewModel.currentTrack.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val playbackPosition by musicPlayerViewModel.playbackPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()

    // Only show mini player if there's a current track
    currentTrack?.let { track ->
        Column {
            // Progress bar
            if (duration > 0) {
                LinearProgressIndicator(
                    progress = (playbackPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colors.primary,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.3f)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() },
                color = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art placeholder
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Album Art",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = track.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Play/Pause button
                    IconButton(
                        onClick = { musicPlayerViewModel.togglePlayPause() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next button
                    IconButton(
                        onClick = { 
                            // TODO: Implement next track functionality
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayerSpacer() {
    // Spacer to account for mini player height when it's visible
    Spacer(modifier = Modifier.height(72.dp))
} 