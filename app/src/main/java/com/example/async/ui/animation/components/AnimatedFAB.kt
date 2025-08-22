package com.example.async.ui.animation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.async.ui.animation.config.AnimationConfig
import com.example.async.ui.animation.config.AppAnimationSpecs
import com.example.async.ui.animation.navigation.LocalAnimationConfig

/**
 * Animated extended floating action button optimized for music player interactions.
 * Features smooth expand/collapse animations with configurable timing.
 */
@Composable
fun AnimatedExtendedFAB(
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shape: Shape = FloatingActionButtonDefaults.extendedFabShape,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.95f,
        animationSpec = AppAnimationSpecs.microInteraction(animationConfig),
        label = "fab_scale"
    )
    
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .animateContentSize(),
        expanded = expanded,
        icon = icon,
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape
    )
}

/**
 * Animated floating action button with show/hide functionality.
 * Ideal for music player controls that need to appear/disappear.
 */
@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    icon: @Composable () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shape: Shape = FloatingActionButtonDefaults.shape,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = AppAnimationSpecs.contentTransition(animationConfig),
            initialScale = 0.8f
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig)
        ),
        exit = scaleOut(
            animationSpec = AppAnimationSpecs.contentTransition(animationConfig),
            targetScale = 0.8f
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig)
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = shape
        ) {
            icon()
        }
    }
}

/**
 * Music player specific FAB that morphs between play and pause states.
 * Features smooth icon transitions and pulse animations.
 */
@Composable
fun MusicPlayerFAB(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    playIcon: @Composable () -> Unit,
    pauseIcon: @Composable () -> Unit,
    visible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = AppAnimationSpecs.playerControl(animationConfig),
        label = "music_fab_scale"
    )
    
    AnimatedFAB(
        onClick = onPlayPause,
        visible = visible,
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        animationConfig = animationConfig,
        icon = {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    fadeIn(
                        animationSpec = AppAnimationSpecs.playerControl(animationConfig)
                    ) togetherWith fadeOut(
                        animationSpec = AppAnimationSpecs.playerControl(animationConfig)
                    )
                },
                label = "play_pause_icon"
            ) { playing ->
                if (playing) {
                    pauseIcon()
                } else {
                    playIcon()
                }
            }
        }
    )
}

/**
 * Queue action FAB for adding songs to the queue.
 * Features staggered animation and visual feedback.
 */
@Composable
fun QueueActionFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: String? = null,
    expanded: Boolean = false,
    visible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = AppAnimationSpecs.queueItemChange(animationConfig),
            initialScale = 0.7f
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.contentTransition(animationConfig)
        ),
        exit = scaleOut(
            animationSpec = AppAnimationSpecs.queueItemChange(animationConfig),
            targetScale = 0.7f
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.contentTransition(animationConfig)
        ),
        modifier = modifier
    ) {
        if (text != null && expanded) {
            AnimatedExtendedFAB(
                onClick = onClick,
                expanded = expanded,
                text = { Text(text) },
                icon = icon,
                containerColor = containerColor,
                contentColor = contentColor,
                animationConfig = animationConfig
            )
        } else {
            FloatingActionButton(
                onClick = onClick,
                containerColor = containerColor,
                contentColor = contentColor
            ) {
                icon()
            }
        }
    }
}

/**
 * Search FAB that expands to show search functionality.
 * Optimized for search screen interactions.
 */
@Composable
fun SearchFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    expanded: Boolean = false,
    visible: Boolean = true,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val searchScale by animateFloatAsState(
        targetValue = if (expanded) 1.05f else 1f,
        animationSpec = AppAnimationSpecs.searchResults(animationConfig),
        label = "search_fab_scale"
    )
    
    AnimatedFAB(
        onClick = onClick,
        visible = visible,
        modifier = modifier.scale(searchScale),
        icon = icon,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        animationConfig = animationConfig
    )
}

/**
 * Library organization FAB for sorting and filtering.
 * Features subtle animations for library interactions.
 */
@Composable
fun LibraryActionFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    isActive: Boolean = false,
    visible: Boolean = true,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val activeScale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = AppAnimationSpecs.contentTransition(animationConfig),
        label = "library_fab_scale"
    )
    
    AnimatedFAB(
        onClick = onClick,
        visible = visible,
        modifier = modifier.scale(activeScale),
        icon = icon,
        containerColor = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (isActive) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationConfig = animationConfig
    )
}

/**
 * Mini FAB for compact spaces in the music player interface.
 */
@Composable
fun MiniFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    visible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig),
            initialScale = 0.6f
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig)
        ),
        exit = scaleOut(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig),
            targetScale = 0.6f
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.microInteraction(animationConfig)
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            icon()
        }
    }
} 