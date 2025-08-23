package com.async.app.ui.screens.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.core.model.SearchResult
import com.async.app.ui.vm.PlayerViewModel
import com.async.app.ui.vm.RepeatMode
import com.async.app.ui.components.AppText

enum class PlayerTab {
    NOW_PLAYING, QUEUE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit = {},
    playerViewModel: PlayerViewModel = viewModel()
) {
    val uiState = playerViewModel.uiState
    var selectedTab by remember { mutableStateOf(PlayerTab.NOW_PLAYING) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
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
            // Player content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Album Art and Track Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Album Art Placeholder
                    Card(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(MaterialTheme.shapes.large),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Track Info
                    val currentTrackData = uiState.currentTrack
                    AppText.TitleLarge(
                        text = currentTrackData?.title ?: "Unknown Title",
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText.BodyMedium(
                        text = currentTrackData?.artist ?: "Unknown Artist",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentTrackData?.album != null) {
                        AppText.BodySmall(
                            text = currentTrackData.album!!,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Bar
                Column {
                    Slider(
                        value = if (uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration.toFloat() else 0f,
                        onValueChange = { progress ->
                            val newPosition = (progress * uiState.duration).toLong()
                            playerViewModel.seekTo(newPosition)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppText.LabelMedium(
                            text = formatTime(uiState.currentPosition),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        AppText.LabelMedium(
                            text = formatTime(uiState.duration),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { playerViewModel.toggleShuffle() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (uiState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Previous
                    IconButton(
                        onClick = { playerViewModel.skipPrevious() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Outlined.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Play/Pause
                    FilledIconButton(
                        onClick = { 
                            playerViewModel.playPause()
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    // Next
                    IconButton(
                        onClick = { playerViewModel.skipNext() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Outlined.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Repeat
                    IconButton(
                        onClick = { playerViewModel.toggleRepeat() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            when (uiState.repeatMode) {
                                RepeatMode.ALL -> Icons.Outlined.Repeat
                                RepeatMode.ONE -> Icons.Outlined.RepeatOne
                                else -> Icons.Outlined.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (uiState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* TODO: Add to playlist */ }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.PlaylistAdd,
                            contentDescription = "Add to playlist"
                        )
                    }
                    IconButton(onClick = { /* TODO: Download */ }) {
                        Icon(
                            Icons.Outlined.Download,
                            contentDescription = "Download"
                        )
                    }
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == PlayerTab.NOW_PLAYING,
                        onClick = { selectedTab = PlayerTab.NOW_PLAYING },
                        text = { Text("Now Playing") }
                    )
                    Tab(
                        selected = selectedTab == PlayerTab.QUEUE,
                        onClick = { selectedTab = PlayerTab.QUEUE },
                        text = { Text("Queue (${uiState.queue.size})") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tab Content
                when (selectedTab) {
                    PlayerTab.NOW_PLAYING -> {
                        uiState.currentTrack?.let { track ->
                            NowPlayingContent(
                                track = track,
                                onPlayTrack = { playerViewModel.playTrack(it) }
                            )
                        }
                    }
                    PlayerTab.QUEUE -> {
                        QueueContent(
                            queue = uiState.queue,
                            currentIndex = uiState.currentIndex,
                            onQueueTrackClick = { track -> playerViewModel.playTrack(track) },
                            onRemoveFromQueue = { index -> playerViewModel.removeFromQueue(index) }
                        )
                    }
                }
            }
        }
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

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
} 
