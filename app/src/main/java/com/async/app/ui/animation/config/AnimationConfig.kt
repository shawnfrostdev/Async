package com.async.app.ui.animation.config

import android.content.Context
import androidx.compose.animation.core.tween

/**
 * Configuration for app animations
 */
data class AnimationConfig(
    val navigationDuration: Int = 300,
    val fadeDuration: Int = 200,
    val scaleDuration: Int = 150,
    val isReducedMotion: Boolean = false
) {
    companion object {
        fun fromSystemSettings(context: Context): AnimationConfig {
            // For now, return default config
            // In a real app, you'd check system accessibility settings
            return AnimationConfig()
        }
        
        fun default() = AnimationConfig()
    }
    
    fun navigationSpec() = tween<Float>(navigationDuration)
    fun fadeSpec() = tween<Float>(fadeDuration)
    fun scaleSpec() = tween<Float>(scaleDuration)
} 