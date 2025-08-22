package com.example.async.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Async app colors from colortheme guide - using Color.kt definitions
import com.example.async.ui.theme.AsyncPrimaryGreen
import com.example.async.ui.theme.AsyncLighterGreen
import com.example.async.ui.theme.AsyncBlack
import com.example.async.ui.theme.AsyncWhite
import com.example.async.ui.theme.AsyncGray
import com.example.async.ui.theme.AsyncBlueAccent
import com.example.async.ui.theme.AsyncSurfaceDark
import com.example.async.ui.theme.AsyncSurfaceVariant
import com.example.async.ui.theme.AsyncOnSurfaceVariant
import com.example.async.ui.theme.AsyncError

private val DarkColorScheme = darkColorScheme(
    primary = AsyncPrimaryGreen,                        // Main buttons, branding
    onPrimary = AsyncWhite,                            // Text on primary buttons
    primaryContainer = AsyncLighterGreen,               // Primary container backgrounds
    onPrimaryContainer = AsyncBlack,                    // Text on primary containers
    
    secondary = AsyncBlueAccent,                        // Secondary actions
    onSecondary = AsyncWhite,                          // Text on secondary buttons
    secondaryContainer = AsyncGray,                     // Secondary container backgrounds
    onSecondaryContainer = AsyncWhite,                  // Text on secondary containers
    
    tertiary = AsyncLighterGreen,                       // Tertiary actions
    onTertiary = AsyncBlack,                           // Text on tertiary elements
    
    background = AsyncBlack,                            // App background
    onBackground = AsyncWhite,                          // Primary text
    
    surface = AsyncSurfaceDark,                         // Cards, dialogs
    onSurface = AsyncWhite,                            // Text on surfaces
    surfaceVariant = AsyncSurfaceVariant,               // Input fields, settings
    onSurfaceVariant = AsyncOnSurfaceVariant,           // Secondary/tertiary text
    
    surfaceTint = AsyncPrimaryGreen,                    // Surface tinting
    inverseSurface = AsyncWhite,                        // Inverse surface
    inverseOnSurface = AsyncBlack,                      // Text on inverse surface
    
    error = AsyncError,                                 // Error states
    onError = AsyncWhite,                              // Text on error
    errorContainer = Color(0xFF93000A),                // Error container
    onErrorContainer = Color(0xFFFFDAD6),              // Text on error container
    
    outline = AsyncGray,                               // Borders, dividers
    outlineVariant = Color(0xFF404040),                // Subtle borders
    
    scrim = Color(0xFF000000),                         // Modal scrim
    
    surfaceBright = AsyncSurfaceVariant,               // Bright surface variant
    surfaceDim = AsyncSurfaceDark,                     // Dim surface variant  
    surfaceContainer = AsyncSurfaceDark,               // Container surfaces
    surfaceContainerHigh = AsyncSurfaceVariant,        // High emphasis containers
    surfaceContainerHighest = Color(0xFF353535),       // Highest emphasis containers
    surfaceContainerLow = Color(0xFF1A1A1A),          // Low emphasis containers
    surfaceContainerLowest = Color(0xFF0F0F0F),       // Lowest emphasis containers
)

@Composable
fun AsyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we'll use our custom colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else DarkColorScheme
        }
        else -> DarkColorScheme  // Always use dark theme as per colortheme guide
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AsyncBlack.toArgb()
            window.navigationBarColor = AsyncBlack.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AsyncTypography,
        content = content
    )
} 