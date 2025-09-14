package app.async.app.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/**
 * Mihon UI Design System Constants
 * Based on the official Mihon UI sizing and usage guide
 */

/**
 * Spacing System - 4dp-based spacing scale
 */
class Padding {
    val extraLarge = 32.dp  // Major sections, large spacing
    val large = 24.dp       // Section headers, significant spacing  
    val medium = 16.dp      // Standard content padding
    val small = 8.dp        // Minor spacing, list items
    val extraSmall = 4.dp   // Minimal spacing, tight layouts
}

/**
 * Icon Sizing Standards
 */
object IconSizing {
    val extraSmall = 16.dp  // Small inline icons, dense UI
    val small = 18.dp       // Compact interface elements
    val medium = 20.dp      // Standard small icons
    val default = 24.dp     // Default icon size, most common
    val large = 32.dp       // Large action buttons, prominent features
    val extraLarge = 40.dp  // Large icons, avatars, prominent elements
    val huge = 48.dp        // Extra large icons, touch targets
    val hero = 56.dp        // Hero icons, major visual elements
    val logo = 64.dp        // Logo, brand elements, major graphics
}

/**
 * Component Sizing Standards
 */
object ComponentSizing {
    // Button Heights
    object Button {
        val small = 32.dp
        val standard = 40.dp
        val large = 48.dp
        val fab = 56.dp
    }
    
    // Button Padding Standards
    object ButtonPadding {
        val standard = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        val large = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        val compact = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    }
    
    // List Item Heights
    object ListItem {
        val singleLine = 56.dp
        val twoLine = 72.dp
        val threeLine = 88.dp
    }
    
    // Card Standards
    object Card {
        val outerMargin = 16.dp        // Card to screen edge
        val innerPadding = 16.dp       // Inner card content padding
        val headerPadding = 8.dp       // Card header padding
        val sectionSpacing = 8.dp      // Card internal sections
        val betweenCards = 8.dp        // Between cards
    }
}

/**
 * Touch Target Guidelines
 */
object TouchTarget {
    val minimum = 48.dp              // Minimum touch target size
    val spacing = 8.dp               // Minimum spacing between targets
    val edgeMargin = 16.dp           // Minimum margin from screen edges
}

/**
 * Layout Patterns
 */
object LayoutPadding {
    // Screen Layout Structure
    val screenHorizontal = 16.dp     // Standard horizontal screen padding
    val screenVertical = 16.dp       // Standard vertical screen padding
    
    // Section Spacing
    val majorSection = 24.dp         // Between major sections
    val minorSection = 16.dp         // Between minor sections
    val relatedItems = 8.dp          // Between related items
    
    // Form Layout
    val inputFieldSpacing = 16.dp    // Between input fields
    val buttonGroupSpacing = 8.dp    // Between related buttons
}

/**
 * Screen-Specific Sizing
 */
object ScreenSpecific {
    // Logo Header (More Tab)
    val logoVerticalPadding = 56.dp  // Large vertical breathing room
    val logoSize = 64.dp             // Large logo size
    
    // List Screens
    val listHorizontalPadding = 16.dp // Sides padding
    val listVerticalPadding = 8.dp    // Top/bottom padding
    val listItemVerticalSpacing = 4.dp // Between list items
    
    // Card Grid Layouts
    val gridMinCardWidth = 120.dp     // Minimum card width
    val gridHorizontalSpacing = 8.dp  // Between grid columns
    val gridVerticalSpacing = 8.dp    // Between grid rows
}

/**
 * Alpha Values for Transparency
 */
const val DISABLED_ALPHA = .38f
const val SECONDARY_ALPHA = .78f

/**
 * MaterialTheme Extensions for accessing Mihon design system
 */
val MaterialTheme.iconSizing: IconSizing
    @Composable
    @ReadOnlyComposable
    get() = IconSizing

val MaterialTheme.componentSizing: ComponentSizing
    @Composable
    @ReadOnlyComposable
    get() = ComponentSizing

val MaterialTheme.touchTarget: TouchTarget
    @Composable
    @ReadOnlyComposable
    get() = TouchTarget

val MaterialTheme.layoutPadding: LayoutPadding
    @Composable
    @ReadOnlyComposable
    get() = LayoutPadding

val MaterialTheme.screenSpecific: ScreenSpecific
    @Composable
    @ReadOnlyComposable
    get() = ScreenSpecific 