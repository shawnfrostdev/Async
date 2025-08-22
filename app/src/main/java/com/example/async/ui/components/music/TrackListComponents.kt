package com.example.async.ui.components.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.async.core.model.SearchResult
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

@Composable
fun TrackList(
    tracks: List<SearchResult>,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    showExtensionSource: Boolean = true
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        itemsIndexed(tracks) { index, track ->
            TrackItem(
                track = track,
                onTrackClick = { onTrackClick(track) },
                onPlayTrack = { onPlayTrack(track) },
                showExtensionSource = showExtensionSource,
                trackNumber = index + 1
            )
        }
    }
}

@Composable
fun TrackItem(
    track: SearchResult,
    onTrackClick: () -> Unit,
    onPlayTrack: () -> Unit,
    modifier: Modifier = Modifier,
    showExtensionSource: Boolean = true,
    trackNumber: Int? = null
) {
    Card(
        onClick = onTrackClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            // Track number or play button
            if (trackNumber != null) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LabelMedium(
                        text = trackNumber.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TitleMedium(
                    text = track.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BodySmall(
                        text = track.artist ?: "Unknown Artist",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (showExtensionSource) {
                        LabelMedium(
                            text = track.extensionId,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = AppSpacing.xs, vertical = 2.dp)
                        )
                    }
                }
                
                track.duration?.let { duration ->
                    BodySmall(
                        text = formatDuration(duration),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Play button
            IconButton(
                onClick = onPlayTrack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // More options button
            IconButton(
                onClick = { /* TODO: Show track options menu */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CompactTrackItem(
    track: SearchResult,
    onTrackClick: () -> Unit,
    onPlayTrack: () -> Unit,
    modifier: Modifier = Modifier,
    showExtensionSource: Boolean = false
) {
    Surface(
        onClick = onTrackClick,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TitleMedium(
                    text = track.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    BodySmall(
                        text = track.artist ?: "Unknown Artist",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (showExtensionSource) {
                        LabelMedium(
                            text = " • ${track.extensionId}",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Play button
            IconButton(
                onClick = onPlayTrack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyTrackList(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            TitleMedium(
                text = "No Tracks Found",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BodySmall(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
} 