package app.async.app.ui.animation.navigation

import androidx.compose.runtime.compositionLocalOf
import app.async.app.ui.animation.config.AnimationConfig

/**
 * Composition local for animation configuration
 * Provides animation settings throughout the UI tree
 */
val LocalAnimationConfig = compositionLocalOf<AnimationConfig> {
    error("AnimationConfig not provided")
} 
