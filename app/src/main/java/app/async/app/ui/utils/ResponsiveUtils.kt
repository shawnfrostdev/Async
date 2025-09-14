package app.async.app.ui.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

// Mihon Window Size Classes as per UI guide
val MediumWidthWindowSize = 600.dp
val ExpandedWidthWindowSize = 840.dp

// Mihon Tablet UI Constants as per UI guide
private const val TABLET_UI_REQUIRED_SCREEN_WIDTH_DP = 720
private const val TABLET_UI_MIN_SCREEN_WIDTH_PORTRAIT_DP = 700
private const val TABLET_UI_MIN_SCREEN_WIDTH_LANDSCAPE_DP = 600

/**
 * Window Size Classes as per Mihon UI guide
 */
@Composable
@ReadOnlyComposable
fun isMediumWidthWindow(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > MediumWidthWindowSize.value
}

@Composable
@ReadOnlyComposable
fun isExpandedWidthWindow(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > ExpandedWidthWindowSize.value
}

/**
 * Tablet UI Detection as per Mihon UI guide
 */
fun Configuration.isTabletUi(): Boolean {
    return smallestScreenWidthDp >= TABLET_UI_REQUIRED_SCREEN_WIDTH_DP
}

@Composable
fun isTabletUi(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.isTabletUi()
}

/**
 * Breakpoint System as per Mihon UI guide:
 * - Phone: < 600dp width
 * - Medium Width: 600dp - 840dp width  
 * - Expanded Width: > 840dp width
 * - Tablet UI: > 720dp smallest width
 */
@Composable
fun isPhoneWidth(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp < 600
}

@Composable
fun isMediumWidth(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp in 600..839
}

@Composable
fun isExpandedWidth(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 840
} 
