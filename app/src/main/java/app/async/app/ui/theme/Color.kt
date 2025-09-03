package app.async.app.ui.theme

import androidx.compose.ui.graphics.Color

// Core Color Palette - Based on documentation
object AsyncColors {
    // Background Colors
    val Background = Color(0xFF111111)
    val Surface = Color(0xFF202020)
    
    // Brand Colors
    val Primary = Color(0xFFF54B5D)
    val Accent = Color(0xFF03DAC5)
    
    // Text Colors
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF898989)
    val Disabled = Color(0xFF555555)
    
    // Status Colors
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF4CAF50)
    
    // Overlay
    val Overlay = Color(0x99000000) // rgba(0,0,0,0.6)
    
    // Semantic Colors for UI Elements
    val OnPrimary = TextPrimary
    val OnSurface = TextPrimary
    val OnBackground = TextPrimary
    
    // Additional semantic colors
    val SurfaceVariant = Color(0xFF2A2A2A)
    val OnSurfaceVariant = TextSecondary
    val Outline = Color(0xFF404040)
    val OutlineVariant = Color(0xFF2A2A2A)
}

// Legacy color support for gradual migration
val Purple80 = AsyncColors.Primary
val PurpleGrey80 = AsyncColors.TextSecondary
val Pink80 = AsyncColors.Accent

val Purple40 = AsyncColors.Primary
val PurpleGrey40 = AsyncColors.TextSecondary
val Pink40 = AsyncColors.Accent 
