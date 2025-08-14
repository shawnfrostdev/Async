package com.shawnfrost.async.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Music-themed color palette following Material Design guidelines
private val MusicPrimary = Color(0xFF1DB954) // Spotify-like green for music theme
private val MusicPrimaryVariant = Color(0xFF1AA34A)
private val MusicSecondary = Color(0xFFFF6B35) // Vibrant orange accent
private val MusicSecondaryVariant = Color(0xFFE55A2B)

// Dark theme optimized for music apps and OLED displays
private val DarkColorPalette = darkColors(
    primary = MusicPrimary,
    primaryVariant = MusicPrimaryVariant,
    secondary = MusicSecondary,
    secondaryVariant = MusicSecondaryVariant,
    background = Color(0xFF121212), // True black for OLED efficiency
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFCF6679)
)

// Light theme with music-friendly colors
private val LightColorPalette = lightColors(
    primary = MusicPrimary,
    primaryVariant = MusicPrimaryVariant,
    secondary = MusicSecondary,
    secondaryVariant = MusicSecondaryVariant,
    background = Color(0xFFFAFAFA), // Slightly off-white for better readability
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    error = Color(0xFFB00020)
)

@Composable
fun AsyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
} 