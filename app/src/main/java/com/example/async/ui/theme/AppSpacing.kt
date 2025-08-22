package com.example.async.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Consistent spacing system for the Async music player.
 * Follows Material Design guidelines with semantic naming.
 */
object AppSpacing {
    // Base spacing scale (8dp grid system)
    val xs = 4.dp      // Extra small spacing - tight elements
    val s = 8.dp       // Small spacing - close related elements
    val m = 16.dp      // Medium spacing - default spacing
    val l = 24.dp      // Large spacing - section separation
    val xl = 32.dp     // Extra large spacing - major sections
    val xxl = 48.dp    // Extra extra large spacing - screen sections
    
    // Component specific spacing
    val cardPadding = m                    // Standard card internal padding
    val listItemPadding = m               // List item internal padding
    val screenPadding = m                 // Default screen edge padding
    val playerControlsPadding = l         // Player controls spacing
    val albumArtPadding = s              // Album art container padding
    val trackInfoPadding = s             // Track info spacing
    
    // Button padding values
    val buttonPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    val smallButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    val chipPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    val fabPadding = PaddingValues(16.dp)
    
    // Layout spacing
    val sectionSpacing = l                // Between major sections
    val componentSpacing = s              // Between related components
    val contentSpacing = m                // Between content blocks
    val itemSpacing = s                   // Between list items
    
    // Music player specific spacing
    val miniPlayerPadding = m             // Mini player internal padding
    val playerScreenPadding = l           // Full player screen padding
    val queueItemSpacing = s              // Queue list item spacing
    val playlistItemSpacing = s           // Playlist item spacing
    val searchResultSpacing = s           // Search result item spacing
    
    // Navigation spacing
    val bottomNavPadding = s              // Bottom navigation padding
    val topBarPadding = m                 // Top app bar padding
    val tabPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    
    // Dialog and sheet spacing
    val dialogPadding = l                 // Dialog internal padding
    val sheetPadding = l                  // Bottom sheet padding
    val modalPadding = xl                 // Modal content padding
} 