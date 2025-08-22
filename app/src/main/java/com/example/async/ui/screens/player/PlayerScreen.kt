package com.example.async.ui.screens.player

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
import com.async.core.model.SearchResult
import com.example.async.ui.components.music.CompactTrackItem
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleLarge
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

enum class PlayerTab {
    NOW_PLAYING, QUEUE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit = {},
    currentTrack: SearchResult,
    isPlaying: Boolean = false,
    currentPosition: Long = 0,
    duration: Long = 0,
    queue: List<SearchResult> = emptyList(),
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onShuffleToggle: () -> Unit = {},
    onRepeatToggle: () -> Unit = {},
    onQueueTrackClick: (SearchResult) -> Unit = {},
    onFavoriteToggle: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onShare: () -> Unit = {},
    shuffleEnabled: Boolean = false,
    repeatMode: RepeatMode = RepeatMode.OFF,
    isFavorite: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(PlayerTab.NOW_PLAYING) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(end = AppSpacing.s)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
            HeadlineLarge(
                text = "Now Playing",
                modifier = Modifier.weight(1f)
            )
            
            // More options menu
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More Options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { BodyMedium("Add to Playlist") },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.PlaylistAdd, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { BodyMedium("Share") },
                        onClick = {
                            showMenu = false
                            onShare()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                        }
                    )
                }
            }
        }
        
        // Tab row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            PlayerTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        TitleMedium(
                            text = when (tab) {
                                PlayerTab.NOW_PLAYING -> "Player"
                                PlayerTab.QUEUE -> "Queue (${queue.size})"
                            }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.m))
        
        // Tab content
        when (selectedTab) {
            PlayerTab.NOW_PLAYING -> {
                NowPlayingContent(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    onPlay = onPlay,
                    onPause = onPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onFavoriteToggle = onFavoriteToggle,
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    isFavorite = isFavorite
                )
            }
            PlayerTab.QUEUE -> {
                QueueContent(
                    queue = queue,
                    onTrackClick = onQueueTrackClick
                )
            }
        }
    }
}

@Composable
private fun NowPlayingContent(
    track: SearchResult,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.l)
    ) {
        // Album Art
        AlbumArtDisplay(
            albumArtUrl = track.thumbnailUrl,
            modifier = Modifier.size(280.dp)
        )
        
        // Track Info
        TrackInfoDisplay(
            track = track,
            isFavorite = isFavorite,
            onFavoriteToggle = onFavoriteToggle,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Progress Bar
        ProgressBarWithSeek(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Player Controls
        PlayerControls(
            isPlaying = isPlaying,
            onPlay = onPlay,
            onPause = onPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onShuffleToggle = onShuffleToggle,
            onRepeatToggle = onRepeatToggle,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AlbumArtDisplay(
    albumArtUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtUrl != null) {
                // TODO: Use AsyncImage when image loading is implemented
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = "Album Art",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = "No Album Art",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrackInfoDisplay(
    track: SearchResult,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        TitleLarge(
            text = track.title,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                TitleMedium(
                    text = track.artist ?: "Unknown Artist",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                track.album?.takeIf { it.isNotBlank() }?.let { album ->
                    BodyMedium(
                        text = album,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Extension source
                LabelMedium(
                    text = track.extensionId,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Favorite button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressBarWithSeek(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        // Progress slider
        Slider(
            value = if (isDragging) dragPosition else if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { 
                isDragging = true
                dragPosition = it
            },
            onValueChangeFinished = {
                isDragging = false
                if (duration > 0) {
                    onSeek((dragPosition * duration).toLong())
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LabelMedium(
                text = formatTime(currentPosition),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabelMedium(
                text = formatTime(duration),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
    ) {
        // Secondary controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = onShuffleToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Repeat
            IconButton(
                onClick = onRepeatToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Outlined.Repeat
                        RepeatMode.ALL -> Icons.Outlined.Repeat
                        RepeatMode.ONE -> Icons.Outlined.RepeatOne
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Main controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Play/Pause
            FloatingActionButton(
                onClick = if (isPlaying) onPause else onPlay,
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Next
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun QueueContent(
    queue: List<SearchResult>,
    onTrackClick: (SearchResult) -> Unit
) {
    if (queue.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                TitleMedium(
                    text = "Queue is Empty",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BodyMedium(
                    text = "Add songs to your queue to see them here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(queue) { track ->
                CompactTrackItem(
                    track = track,
                    onTrackClick = { onTrackClick(track) },
                    onPlayTrack = { onTrackClick(track) },
                    showExtensionSource = true
                )
            }
        }
    }
}

enum class RepeatMode {
    OFF, ALL, ONE
}

private fun formatTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
} 