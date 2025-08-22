package com.example.async.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.LabelLarge
import com.example.async.ui.theme.AppSpacing

/**
 * Comprehensive button system for the Async music player.
 * Provides various button types following Material Design 3 guidelines.
 */

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = AppSpacing.buttonPadding,
        colors = ButtonDefaults.buttonColors()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                leadingIcon?.invoke()
            }
            
            content()
            
            if (!loading) {
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = AppSpacing.buttonPadding,
        colors = ButtonDefaults.filledTonalButtonColors()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                leadingIcon?.invoke()
            }
            
            content()
            
            if (!loading) {
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
fun TertiaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = AppSpacing.buttonPadding,
        colors = ButtonDefaults.elevatedButtonColors()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                leadingIcon?.invoke()
            }
            
            content()
            
            if (!loading) {
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = AppSpacing.buttonPadding,
        colors = ButtonDefaults.outlinedButtonColors()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                leadingIcon?.invoke()
            }
            
            content()
            
            if (!loading) {
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = AppSpacing.buttonPadding,
        colors = ButtonDefaults.textButtonColors()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                leadingIcon?.invoke()
            }
            
            content()
            
            if (!loading) {
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: androidx.compose.material3.IconButtonColors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        content = content
    )
}

@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}

// Music player specific buttons
@Composable
fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PrimaryButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        LabelLarge("Play")
    }
}

@Composable
fun PauseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PrimaryButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        LabelLarge("Pause")
    }
}

@Composable
fun ShuffleButton(
    isShuffled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (isShuffled) {
        PrimaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            LabelLarge("Shuffle On")
        }
    } else {
        SecondaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            LabelLarge("Shuffle")
        }
    }
}

@Composable
fun RepeatButton(
    repeatMode: RepeatMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    when (repeatMode) {
        RepeatMode.OFF -> SecondaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            LabelLarge("Repeat")
        }
        RepeatMode.ALL -> PrimaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            LabelLarge("Repeat All")
        }
        RepeatMode.ONE -> PrimaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            LabelLarge("Repeat One")
        }
    }
}

@Composable
fun AddToPlaylistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AppOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        LabelLarge("Add to Playlist")
    }
}

@Composable
fun DownloadButton(
    isDownloaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    if (isDownloaded) {
        SecondaryButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            loading = loading
        ) {
            LabelLarge("Downloaded")
        }
    } else {
        AppOutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            loading = loading
        ) {
            LabelLarge("Download")
        }
    }
}

enum class RepeatMode {
    OFF, ALL, ONE
} 