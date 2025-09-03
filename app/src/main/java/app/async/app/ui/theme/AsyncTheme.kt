package app.async.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Color Scheme - Primary theme based on documentation
private val AsyncDarkColorScheme = darkColorScheme(
    // Brand colors
    primary = AsyncColors.Primary,
    secondary = AsyncColors.Accent,
    tertiary = AsyncColors.Accent,
    
    // Background colors
    background = AsyncColors.Background,
    surface = AsyncColors.Surface,
    surfaceVariant = AsyncColors.SurfaceVariant,
    
    // Text colors
    onPrimary = AsyncColors.OnPrimary,
    onSecondary = AsyncColors.TextPrimary,
    onTertiary = AsyncColors.TextPrimary,
    onBackground = AsyncColors.OnBackground,
    onSurface = AsyncColors.OnSurface,
    onSurfaceVariant = AsyncColors.OnSurfaceVariant,
    
    // Status colors
    error = AsyncColors.Error,
    onError = AsyncColors.TextPrimary,
    
    // Outline colors
    outline = AsyncColors.Outline,
    outlineVariant = AsyncColors.OutlineVariant,
    
    // Container colors
    primaryContainer = AsyncColors.Primary,
    onPrimaryContainer = AsyncColors.TextPrimary,
    secondaryContainer = AsyncColors.Surface,
    onSecondaryContainer = AsyncColors.TextPrimary,
    
    // Surface tint
    surfaceTint = AsyncColors.Primary,
    
    // Inverse colors for accessibility
    inverseSurface = AsyncColors.TextPrimary,
    inverseOnSurface = AsyncColors.Background,
    inversePrimary = AsyncColors.Background
)

// Light Color Scheme - Fallback (though app is designed for dark theme)
private val AsyncLightColorScheme = lightColorScheme(
    primary = AsyncColors.Primary,
    secondary = AsyncColors.Accent,
    tertiary = AsyncColors.Accent,
    background = AsyncColors.TextPrimary,
    surface = AsyncColors.Surface,
    onPrimary = AsyncColors.TextPrimary,
    onSecondary = AsyncColors.TextPrimary,
    onTertiary = AsyncColors.TextPrimary,
    onBackground = AsyncColors.Background,
    onSurface = AsyncColors.Background,
    error = AsyncColors.Error,
    onError = AsyncColors.TextPrimary
)

@Composable
fun AsyncTheme(
    darkTheme: Boolean = true, // Default to dark theme as per design
    content: @Composable () -> Unit
) {
    // Always use our custom color scheme (no dynamic colors for consistency)
    val colorScheme = if (darkTheme) AsyncDarkColorScheme else AsyncLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Configure system bars for dark theme
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val systemBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.statusBarColor = systemBarColor
            @Suppress("DEPRECATION")
            window.navigationBarColor = systemBarColor
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AsyncTypography,
        content = content
    )
} 
