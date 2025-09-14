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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

/**
 * MaterialTheme extension for accessing Mihon spacing system
 */
val MaterialTheme.padding: Padding
    get() = Padding()

// Dark Color Scheme - Primary theme based on documentation
private val AsyncDarkColorScheme = darkColorScheme(
    // Brand colors
    primary = AsyncColors.Primary,
    onPrimary = AsyncColors.OnPrimary,
    secondary = AsyncColors.Accent,
    onSecondary = AsyncColors.TextPrimary,
    
    // Surface colors
    surface = AsyncColors.Surface,
    onSurface = AsyncColors.OnSurface,
    surfaceVariant = AsyncColors.SurfaceVariant,
    onSurfaceVariant = AsyncColors.OnSurfaceVariant,
    
    // Background colors
    background = AsyncColors.Background,
    onBackground = AsyncColors.OnBackground,
    
    // Container colors
    primaryContainer = AsyncColors.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = AsyncColors.TextPrimary,
    secondaryContainer = AsyncColors.Accent.copy(alpha = 0.2f),
    onSecondaryContainer = AsyncColors.TextPrimary,
    
    // Border/Outline colors
    outline = AsyncColors.Outline,
    outlineVariant = AsyncColors.OutlineVariant,
    
    // Error colors
    error = AsyncColors.Error,
    onError = AsyncColors.TextPrimary
)

// Light Color Scheme - Secondary theme
private val AsyncLightColorScheme = lightColorScheme(
    // Brand colors  
    primary = AsyncColors.Primary,
    onPrimary = Color.White,
    secondary = AsyncColors.Accent,
    onSecondary = Color.White,
    
    // Surface colors
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    
    // Background colors
    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,
    
    // Container colors
    primaryContainer = AsyncColors.Primary.copy(alpha = 0.1f),
    onPrimaryContainer = AsyncColors.Primary,
    secondaryContainer = AsyncColors.Accent.copy(alpha = 0.1f),
    onSecondaryContainer = AsyncColors.Accent,
    
    // Border/Outline colors
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0),
    
    // Error colors
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
