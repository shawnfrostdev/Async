package com.example.async.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.AlbumName
import com.example.async.ui.components.text.ArtistName
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.Duration
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.components.text.TrackTitle
import com.example.async.ui.theme.AppSpacing

/**
 * Comprehensive card system for the Async music player.
 * Provides base cards and music-specific card components.
 */

@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        content = content
    )
}

@Composable
fun ContentCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    TitleMedium(title)
                    subtitle?.let { 
                        BodySmall(
                            text = it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                trailingIcon?.invoke()
            }
            
            content?.invoke(this)
        }
    }
}

@Composable
fun ImageCard(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    aspectRatio: Float = 1f,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column {
            // Placeholder for image - would use AsyncImage in real implementation
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                BodyMedium(
                    text = "Image",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                modifier = Modifier.padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                TitleMedium(title)
                subtitle?.let { 
                    BodySmall(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                content?.invoke(this)
            }
        }
    }
}

// Music-specific cards
@Composable
fun TrackCard(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
    album: String? = null,
    duration: String? = null,
    isPlaying: Boolean = false,
    onClick: (() -> Unit)? = null,
    onPlayPause: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        colors = if (isPlaying) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                LabelMedium(
                    text = "♪",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                TrackTitle(
                    text = title,
                    color = if (isPlaying) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                ArtistName(
                    text = artist,
                    color = if (isPlaying) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                album?.let { 
                    AlbumName(
                        text = it,
                        color = if (isPlaying) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                duration?.let { 
                    Duration(
                        text = it,
                        color = if (isPlaying) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumCard(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
    year: String? = null,
    trackCount: Int? = null,
    onClick: (() -> Unit)? = null
) {
    ImageCard(
        imageUrl = "", // Placeholder
        title = title,
        subtitle = artist,
        modifier = modifier,
        aspectRatio = 1f,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            year?.let { 
                BodySmall(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trackCount?.let { 
                BodySmall(
                    text = "$it tracks",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PlaylistCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    trackCount: Int? = null,
    duration: String? = null,
    onClick: (() -> Unit)? = null
) {
    ImageCard(
        imageUrl = "", // Placeholder
        title = title,
        subtitle = description,
        modifier = modifier,
        aspectRatio = 1f,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trackCount?.let { 
                BodySmall(
                    text = "$it songs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            duration?.let { 
                BodySmall(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    name: String,
    modifier: Modifier = Modifier,
    genre: String? = null,
    albumCount: Int? = null,
    onClick: (() -> Unit)? = null
) {
    ImageCard(
        imageUrl = "", // Placeholder
        title = name,
        subtitle = genre,
        modifier = modifier,
        aspectRatio = 1f,
        onClick = onClick
    ) {
        albumCount?.let { 
            BodySmall(
                text = "$it albums",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NowPlayingCard(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                TitleMedium(
                    text = "♪",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                TrackTitle(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                ArtistName(
                    text = artist,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                // Control buttons would go here
                LabelMedium(
                    text = if (isPlaying) "⏸" else "▶",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
} 