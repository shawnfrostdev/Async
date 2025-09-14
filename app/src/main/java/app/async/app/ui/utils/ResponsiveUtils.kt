package app.async.app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive design utilities for consistent layout across different screen sizes.
 * Follows Android development best practices for supporting multiple screen sizes.
 */

/**
 * Screen size categories based on Android guidelines
 */
enum class ScreenSize {
    COMPACT,    // < 600dp width (phones in portrait)
    MEDIUM,     // 600-839dp width (tablets, phones in landscape)
    EXPANDED    // >= 840dp width (large tablets, desktop)
}

/**
 * Get the current screen size category
 */
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 600 -> ScreenSize.COMPACT
        screenWidth < 840 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

/**
 * Get responsive dimension based on screen size
 */
@Composable
fun responsiveDp(
    compact: Dp,
    medium: Dp = compact,
    expanded: Dp = medium
): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> compact
        ScreenSize.MEDIUM -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

/**
 * Get responsive integer value based on screen size
 */
@Composable
fun responsiveInt(
    compact: Int,
    medium: Int = compact,
    expanded: Int = medium
): Int {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> compact
        ScreenSize.MEDIUM -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

/**
 * Calculate optimal number of columns for a grid based on screen width and minimum item width
 */
@Composable
fun calculateGridColumns(
    minItemWidth: Dp,
    spacing: Dp = 16.dp,
    horizontalPadding: Dp = 16.dp
): Int {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthPx = configuration.screenWidthDp.dp
    val availableWidth = screenWidthPx - (horizontalPadding * 2)
    val itemWidthWithSpacing = minItemWidth + spacing
    
    return (availableWidth / itemWidthWithSpacing).toInt().coerceAtLeast(1)
}

/**
 * Check if the current device is a tablet
 */
@Composable
fun isTablet(): Boolean {
    return getScreenSize() != ScreenSize.COMPACT
}

/**
 * Check if the current device should use tablet UI (following Mihon pattern)
 */
@Composable
fun isTabletUi(): Boolean {
    return isTablet()
}

/**
 * Check if the current device is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Get the appropriate text size scale for different screen sizes
 */
@Composable
fun getTextSizeScale(): Float {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> 1.0f
        ScreenSize.MEDIUM -> 1.1f
        ScreenSize.EXPANDED -> 1.2f
    }
}

/**
 * Format duration with proper localization
 */
fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return when {
        hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
        else -> "%d:%02d".format(minutes, seconds)
    }
}

/**
 * Get responsive padding based on content density
 */
@Composable
fun getContentPadding(): Dp {
    return responsiveDp(
        compact = 16.dp,
        medium = 24.dp,
        expanded = 32.dp
    )
}

/**
 * Get responsive item spacing for lists
 */
@Composable
fun getListItemSpacing(): Dp {
    return responsiveDp(
        compact = 8.dp,
        medium = 12.dp,
        expanded = 16.dp
    )
} 
