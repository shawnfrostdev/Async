package app.async.app.ui.components

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
import app.async.core.model.SearchResult
import app.async.app.ui.theme.AsyncColors
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Centralized card components following design system specifications
 * 
 * Card Standards:
 * - Background: #202020 (Surface)
 * - Corner radius: 8-12dp for modern look
 * - Elevation: 2-4dp subtle shadow
 * - Padding: 16dp internal consistency
 * - Content: Title → Text Primary, Subtitle → Text Secondary
 */
object AppCards {
    
    @Composable
    fun TrackCard(
        track: SearchResult,
        onClick: () -> Unit,
        onPlayClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AsyncColors.SurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.thumbnailUrl != null) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = AsyncColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.Title(
                    text = track.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                
                track.artist?.let { artist ->
                    AppText.SecondaryText(
                        text = artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Play button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = AsyncColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    @Composable
    fun InfoCard(
        title: String,
        subtitle: String? = null,
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        containerColor: Color = AsyncColors.Surface,
        contentColor: Color = AsyncColors.TextPrimary,
        content: (@Composable ColumnScope.() -> Unit)? = null
    ) {
        Card(
            modifier = modifier,
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
                AppText.Title(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText.SecondaryText(
                        text = it,
                        color = if (contentColor == AsyncColors.TextPrimary) 
                            AsyncColors.TextSecondary 
                        else 
                            contentColor.copy(alpha = 0.8f)
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
            containerColor = AsyncColors.Surface,
            contentColor = AsyncColors.Error
        ) {
            onRetry?.let { retry ->
                Spacer(modifier = Modifier.height(8.dp))
                AppButtons.Primary(
                    text = "Retry",
                    onClick = retry
                )
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
            containerColor = AsyncColors.Surface,
            contentColor = AsyncColors.TextPrimary
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = AsyncColors.Primary
            )
        }
    }
    
    @Composable
    fun SuccessCard(
        title: String,
        message: String,
        onDismiss: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        InfoCard(
            title = title,
            subtitle = message,
            modifier = modifier,
            containerColor = AsyncColors.Surface,
            contentColor = AsyncColors.Success
        ) {
            onDismiss?.let { dismiss ->
                Spacer(modifier = Modifier.height(8.dp))
                AppButtons.Text(
                    text = "Dismiss",
                    onClick = dismiss,
                    color = AsyncColors.Success
                )
            }
        }
    }
} 
