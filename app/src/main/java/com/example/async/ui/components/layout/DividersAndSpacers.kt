package com.example.async.ui.components.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.async.ui.theme.AppSpacing

/**
 * Layout dividers and spacing components for the Async music player.
 * Provides consistent spacing and visual separation throughout the app.
 */

@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Composable
fun AppVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

// Vertical spacers
@Composable
fun VerticalSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HorizontalSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

// Semantic spacing - Vertical
@Composable
fun XSmallSpacer() = VerticalSpacer(AppSpacing.xs)

@Composable
fun SmallSpacer() = VerticalSpacer(AppSpacing.s)

@Composable
fun MediumSpacer() = VerticalSpacer(AppSpacing.m)

@Composable
fun LargeSpacer() = VerticalSpacer(AppSpacing.l)

@Composable
fun XLargeSpacer() = VerticalSpacer(AppSpacing.xl)

@Composable
fun XXLargeSpacer() = VerticalSpacer(AppSpacing.xxl)

// Semantic spacing - Horizontal
@Composable
fun XSmallHorizontalSpacer() = HorizontalSpacer(AppSpacing.xs)

@Composable
fun SmallHorizontalSpacer() = HorizontalSpacer(AppSpacing.s)

@Composable
fun MediumHorizontalSpacer() = HorizontalSpacer(AppSpacing.m)

@Composable
fun LargeHorizontalSpacer() = HorizontalSpacer(AppSpacing.l)

@Composable
fun XLargeHorizontalSpacer() = HorizontalSpacer(AppSpacing.xl)

@Composable
fun XXLargeHorizontalSpacer() = HorizontalSpacer(AppSpacing.xxl)

// Music player specific spacers
@Composable
fun TrackSpacing() = VerticalSpacer(AppSpacing.itemSpacing)

@Composable
fun SectionSpacing() = VerticalSpacer(AppSpacing.sectionSpacing)

@Composable
fun PlayerControlSpacing() = VerticalSpacer(AppSpacing.playerControlsPadding)

@Composable
fun PlaylistItemSpacing() = VerticalSpacer(AppSpacing.playlistItemSpacing)

@Composable
fun QueueItemSpacing() = VerticalSpacer(AppSpacing.queueItemSpacing)

// Content dividers for music sections
@Composable
fun SectionDivider(
    modifier: Modifier = Modifier
) {
    AppDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

@Composable
fun PlaylistDivider(
    modifier: Modifier = Modifier
) {
    AppDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

@Composable
fun TrackDivider(
    modifier: Modifier = Modifier
) {
    AppDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )
} 