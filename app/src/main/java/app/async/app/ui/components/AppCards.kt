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
import androidx.compose.ui.unit.Dp
import app.async.core.model.SearchResult
import app.async.app.ui.theme.AsyncColors
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import app.async.app.ui.theme.padding
import app.async.app.ui.theme.iconSizing
import app.async.app.ui.theme.componentSizing
import app.async.app.ui.theme.ComponentSizing
import app.async.app.ui.theme.IconSizing

/**
 * Centralized card components following Mihon design system specifications
 * 
 * Card Standards as per Mihon UI guide:
 * - Background: Surface colors from theme
 * - Corner radius: 8-12dp for modern look
 * - Elevation: 2-4dp subtle shadow
 * - Padding: 16dp internal consistency (MaterialTheme.padding.medium)
 * - Content: Title → titleMedium, Subtitle → bodyMedium
 * - Icons: 24dp default size (MaterialTheme.iconSizing.default)
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
                .padding(
                    horizontal = MaterialTheme.padding.medium, // 16dp as per Mihon guide
                    vertical = MaterialTheme.padding.medium // 16dp as per Mihon guide
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder - 56dp size for list items
            Card(
                modifier = Modifier.size(ComponentSizing.ListItem.singleLine), // 56dp as per Mihon guide
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(MaterialTheme.padding.small) // 8dp corner radius
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
                            modifier = Modifier.size(IconSizing.default), // 24dp as per Mihon guide
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(MaterialTheme.padding.medium)) // 16dp spacing
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium, // 16sp Medium as per Mihon guide
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                
                track.artist?.let { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyMedium, // 14sp as per Mihon guide
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Play button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(ComponentSizing.Button.large) // 48dp touch target
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(IconSizing.default), // 24dp icon
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    @Composable
    fun PlaylistCard(
        title: String,
        subtitle: String? = null,
        thumbnailUrl: String? = null,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(MaterialTheme.padding.medium) // 16dp corner radius
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.padding.medium) // 16dp internal padding
            ) {
                // Thumbnail area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f), // Standard aspect ratio
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(MaterialTheme.padding.small) // 8dp corner radius
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbnailUrl != null) {
                            AsyncImage(
                                model = thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(IconSizing.large), // 32dp for larger display
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(MaterialTheme.padding.medium)) // 16dp spacing
                
                // Title and subtitle
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // 16sp Medium as per Mihon guide
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(MaterialTheme.padding.extraSmall)) // 4dp spacing
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium, // 14sp as per Mihon guide
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    
    @Composable
    fun SectionCard(
        title: String,
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(MaterialTheme.padding.medium) // 16dp corner radius
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.padding.medium) // 16dp internal padding
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall, // 24sp as per Mihon guide
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = MaterialTheme.padding.small) // 8dp bottom spacing
                )
                
                content()
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
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(MaterialTheme.padding.medium) // 16dp corner radius
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.padding.medium) // 16dp internal padding
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // 16sp Medium as per Mihon guide
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                
                Spacer(modifier = Modifier.height(MaterialTheme.padding.small)) // 8dp spacing
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium, // 14sp as per Mihon guide
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                
                onRetry?.let { retry ->
                    Spacer(modifier = Modifier.height(MaterialTheme.padding.medium)) // 16dp spacing
                    Button(
                        onClick = retry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
    
    @Composable
    fun LoadingCard(
        title: String = "Loading...",
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(MaterialTheme.padding.medium) // 16dp corner radius
        ) {
            Row(
                modifier = Modifier.padding(MaterialTheme.padding.medium), // 16dp internal padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(IconSizing.default), // 24dp as per Mihon guide
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(MaterialTheme.padding.medium)) // 16dp spacing
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // 16sp Medium as per Mihon guide
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
} 
