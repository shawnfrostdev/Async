package com.example.async.ui.animation.config

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Comprehensive animation configuration system for the Async music player.
 * Integrates with Android system settings and user preferences for optimal performance.
 */
@Stable
data class AnimationConfig(
    val durationMultiplier: Float = 1f,
    val enableTransitions: Boolean = true,
    val enableMicroInteractions: Boolean = true,
    val enableLargeMotions: Boolean = true,
    val reducedMotion: Boolean = false,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val enablePlayerAnimations: Boolean = true,
    val enablePlaylistAnimations: Boolean = true,
    val enableSearchAnimations: Boolean = true,
    val enableLibraryAnimations: Boolean = true
) {
    companion object {
        /**
         * Creates animation config from Android system settings.
         * Respects user accessibility preferences and performance settings.
         */
        fun fromSystemSettings(context: Context): AnimationConfig {
            val animatorScale = try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f
                )
            } catch (e: Exception) {
                1f
            }
            
            val transitionScale = try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    1f
                )
            } catch (e: Exception) {
                1f
            }
            
            val windowScale = try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.WINDOW_ANIMATION_SCALE,
                    1f
                )
            } catch (e: Exception) {
                1f
            }
            
            // Check for reduced motion accessibility setting
            val reducedMotion = try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f
                ) == 0f
            } catch (e: Exception) {
                false
            }
            
            val averageScale = (animatorScale + transitionScale + windowScale) / 3f
            
            return AnimationConfig(
                durationMultiplier = averageScale.coerceIn(0f, 2f),
                enableTransitions = transitionScale > 0f,
                enableMicroInteractions = animatorScale > 0f,
                enableLargeMotions = windowScale > 0f,
                reducedMotion = reducedMotion,
                performanceMode = when {
                    averageScale == 0f -> PerformanceMode.BATTERY_SAVER
                    averageScale > 1f -> PerformanceMode.HIGH_PERFORMANCE
                    else -> PerformanceMode.BALANCED
                }
            )
        }
        
        /**
         * Returns a completely disabled animation config for maximum performance.
         */
        fun disabled(): AnimationConfig = AnimationConfig(
            durationMultiplier = 0f,
            enableTransitions = false,
            enableMicroInteractions = false,
            enableLargeMotions = false,
            reducedMotion = true,
            performanceMode = PerformanceMode.BATTERY_SAVER,
            enablePlayerAnimations = false,
            enablePlaylistAnimations = false,
            enableSearchAnimations = false,
            enableLibraryAnimations = false
        )
        
        /**
         * Returns a high-performance animation config with enhanced animations.
         */
        fun highPerformance(): AnimationConfig = AnimationConfig(
            durationMultiplier = 1.2f,
            enableTransitions = true,
            enableMicroInteractions = true,
            enableLargeMotions = true,
            reducedMotion = false,
            performanceMode = PerformanceMode.HIGH_PERFORMANCE,
            enablePlayerAnimations = true,
            enablePlaylistAnimations = true,
            enableSearchAnimations = true,
            enableLibraryAnimations = true
        )
        
        /**
         * Returns a config optimized for music player interactions.
         */
        fun musicPlayerOptimized(): AnimationConfig = AnimationConfig(
            durationMultiplier = 0.8f,
            enableTransitions = true,
            enableMicroInteractions = true,
            enableLargeMotions = true,
            reducedMotion = false,
            performanceMode = PerformanceMode.BALANCED,
            enablePlayerAnimations = true,
            enablePlaylistAnimations = true,
            enableSearchAnimations = false, // Keep search fast
            enableLibraryAnimations = true
        )
    }
    
    /**
     * Applies duration multiplier to a base duration.
     */
    fun scaleDuration(baseDuration: Int): Int {
        return if (reducedMotion || !enableTransitions) {
            0
        } else {
            (baseDuration * durationMultiplier).toInt().coerceAtLeast(0)
        }
    }
    
    /**
     * Checks if micro interactions should be animated.
     */
    fun shouldAnimateMicroInteractions(): Boolean {
        return enableMicroInteractions && !reducedMotion && durationMultiplier > 0f
    }
    
    /**
     * Checks if large motions should be animated.
     */
    fun shouldAnimateLargeMotions(): Boolean {
        return enableLargeMotions && !reducedMotion && durationMultiplier > 0f
    }
    
    /**
     * Checks if transitions should be animated.
     */
    fun shouldAnimateTransitions(): Boolean {
        return enableTransitions && !reducedMotion && durationMultiplier > 0f
    }
}

/**
 * Performance modes for different device capabilities and user preferences.
 */
enum class PerformanceMode {
    /**
     * High-end devices with enhanced animations and effects.
     */
    HIGH_PERFORMANCE,
    
    /**
     * Standard animations for most devices.
     */
    BALANCED,
    
    /**
     * Minimal animations to save battery and improve performance.
     */
    BATTERY_SAVER
}

/**
 * Provides and manages animation configuration with reactive updates.
 */
class AnimationConfigProvider(context: Context) {
    private val _config = MutableStateFlow(AnimationConfig.fromSystemSettings(context))
    val config: StateFlow<AnimationConfig> = _config.asStateFlow()
    
    private var systemConfig by mutableStateOf(AnimationConfig.fromSystemSettings(context))
    private var userOverrides by mutableStateOf<AnimationConfig?>(null)
    
    /**
     * Updates the animation configuration.
     */
    fun updateConfig(newConfig: AnimationConfig) {
        userOverrides = newConfig
        _config.value = newConfig
    }
    
    /**
     * Applies user overrides to the system configuration.
     */
    fun applyUserOverrides(overrides: AnimationConfig.() -> AnimationConfig) {
        val updated = systemConfig.overrides()
        userOverrides = updated
        _config.value = updated
    }
    
    /**
     * Resets to system settings, removing user overrides.
     */
    fun resetToSystemSettings(context: Context) {
        systemConfig = AnimationConfig.fromSystemSettings(context)
        userOverrides = null
        _config.value = systemConfig
    }
    
    /**
     * Refreshes system settings and applies them if no user overrides exist.
     */
    fun refreshSystemSettings(context: Context) {
        systemConfig = AnimationConfig.fromSystemSettings(context)
        if (userOverrides == null) {
            _config.value = systemConfig
        }
    }
    
    /**
     * Gets the current effective configuration.
     */
    fun getCurrentConfig(): AnimationConfig = _config.value
    
    /**
     * Checks if the current config has user overrides.
     */
    fun hasUserOverrides(): Boolean = userOverrides != null
} 