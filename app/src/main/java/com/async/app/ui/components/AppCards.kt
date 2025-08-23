package com.async.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import com.async.core.model.SearchResult

/**
 * Centralized card components for consistent UI
 */
object AppCards {
    
    @Composable
    fun TrackCard(
        track: SearchResult,
        onClick: () -> Unit,
        onPlayClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .width(160.dp)
                .height(200.dp),
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Album art placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Track info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AppText.TitleSmall(
                        text = track.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                    
                    track.artist?.let { artist ->
                        AppText.BodySmall(
                            text = artist,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Play button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun InfoCard(
        title: String,
        subtitle: String? = null,
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        content: (@Composable ColumnScope.() -> Unit)? = null
    ) {
        val cardModifier = if (onClick != null) {
            modifier.then(Modifier)
        } else {
            modifier
        }
        
        Card(
            modifier = cardModifier,
            onClick = onClick ?: {},
            enabled = onClick != null,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AppText.TitleMedium(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText.BodyMedium(
                        text = it,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
                
                content?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    it()
                }
            }
        }
    }
    
    @Composable
    fun ErrorCard(
        title: String,
        message: String,
        onRetry: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        InfoCard(
            title = title,
            subtitle = message,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            onRetry?.let { retry ->
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
    
    @Composable
    fun LoadingCard(
        title: String = "Loading...",
        modifier: Modifier = Modifier
    ) {
        InfoCard(
            title = title,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
} 