package com.example.async.ui.animation.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.async.ui.animation.config.AnimationConfig
import com.example.async.ui.animation.config.AppAnimationSpecs
import com.example.async.ui.animation.navigation.LocalAnimationConfig
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated circular progress indicator with smooth progress transitions.
 * Optimized for music player track progress display.
 */
@Composable
fun AnimatedCircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    if (indeterminate) {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = strokeCap
        )
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress().coerceIn(0f, 1f),
            animationSpec = AppAnimationSpecs.playerControl(animationConfig),
            label = "circular_progress"
        )
        
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = strokeCap
        )
    }
}

/**
 * Animated linear progress indicator with smooth transitions.
 * Perfect for track seeking and loading states.
 */
@Composable
fun AnimatedLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    if (indeterminate) {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap
        )
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress().coerceIn(0f, 1f),
            animationSpec = AppAnimationSpecs.playerControl(animationConfig),
            label = "linear_progress"
        )
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap
        )
    }
}

/**
 * Music player specific progress indicator with custom styling.
 * Features enhanced visuals for audio track progress.
 */
@Composable
fun MusicProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    bufferedProgress: Float = 0f,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    bufferedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    strokeWidth: Dp = 4.dp,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress().coerceIn(0f, 1f),
        animationSpec = AppAnimationSpecs.trackTransition(animationConfig),
        label = "music_progress"
    )
    
    val animatedBuffered by animateFloatAsState(
        targetValue = bufferedProgress.coerceIn(0f, 1f),
        animationSpec = AppAnimationSpecs.contentTransition(animationConfig),
        label = "buffered_progress"
    )
    
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    
    Canvas(
        modifier = modifier
            .progressSemantics(animatedProgress)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2
        
        // Draw track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(canvasWidth, centerY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        
        // Draw buffered progress
        if (animatedBuffered > 0f) {
            drawLine(
                color = bufferedColor,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth * animatedBuffered, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }
        
        // Draw actual progress
        if (animatedProgress > 0f) {
            drawLine(
                color = color,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth * animatedProgress, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Waveform-style progress indicator for music visualization.
 * Features animated bars that respond to progress changes.
 */
@Composable
fun WaveformProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    color: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress().coerceIn(0f, 1f),
        animationSpec = AppAnimationSpecs.trackTransition(animationConfig),
        label = "waveform_progress"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_animation")
    val animationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationConfig.scaleDuration(2000),
                easing = androidx.compose.animation.core.LinearEasing
            )
        ),
        label = "waveform_offset"
    )
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / barCount
        val maxHeight = size.height
        
        repeat(barCount) { index ->
            val x = index * barWidth + barWidth / 2
            val normalizedIndex = index.toFloat() / barCount
            
            // Calculate bar height based on sine wave
            val waveHeight = (sin((normalizedIndex * 4f + animationOffset) * Math.PI.toFloat()) + 1f) / 2f
            val barHeight = maxHeight * 0.3f + (maxHeight * 0.7f * waveHeight)
            
            val isActive = normalizedIndex <= animatedProgress
            val barColor = if (isActive) color else inactiveColor
            
            drawRect(
                color = barColor,
                topLeft = Offset(x - barWidth / 4, size.height - barHeight),
                size = Size(barWidth / 2, barHeight)
            )
        }
    }
}

/**
 * Circular loading indicator with pulse animation.
 * Perfect for loading states in the music player.
 */
@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loading")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = animationConfig.scaleDuration(1000)
                0.8f at 0
                1.2f at 500
                0.8f at 1000
            }
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = animationConfig.scaleDuration(1000)
                1f at 0
                0.3f at 500
                1f at 1000
            }
        ),
        label = "pulse_alpha"
    )
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val radius = (size.toPx() / 2f) * scale
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = center
        )
    }
}

/**
 * Download progress indicator with segmented style.
 * Ideal for showing extension or track download progress.
 */
@Composable
fun SegmentedProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    segmentCount: Int = 10,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    gap: Dp = 2.dp,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress().coerceIn(0f, 1f),
        animationSpec = AppAnimationSpecs.contentTransition(animationConfig),
        label = "segmented_progress"
    )
    
    val density = LocalDensity.current
    val gapPx = with(density) { gap.toPx() }
    
    Canvas(modifier = modifier) {
        val totalGaps = (segmentCount - 1) * gapPx
        val segmentWidth = (size.width - totalGaps) / segmentCount
        val segmentHeight = size.height
        
        val filledSegments = (animatedProgress * segmentCount).toInt()
        val partialSegment = (animatedProgress * segmentCount) % 1f
        
        repeat(segmentCount) { index ->
            val x = index * (segmentWidth + gapPx)
            
            val segmentColor = when {
                index < filledSegments -> color
                index == filledSegments && partialSegment > 0f -> color
                else -> trackColor
            }
            
            val actualWidth = if (index == filledSegments && partialSegment > 0f) {
                segmentWidth * partialSegment
            } else {
                segmentWidth
            }
            
            drawRect(
                color = segmentColor,
                topLeft = Offset(x, 0f),
                size = Size(actualWidth, segmentHeight)
            )
        }
    }
} 