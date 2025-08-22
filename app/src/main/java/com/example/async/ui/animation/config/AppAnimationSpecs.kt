package com.example.async.ui.animation.config

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Centralized animation specifications for the Async music player.
 * All specifications follow Material Design 3 guidelines and are optimized for music app interactions.
 */
object AppAnimationSpecs {
    
    /**
     * Standard durations following Material Design 3 guidelines.
     * All durations are in milliseconds and respect the animation configuration.
     */
    object Duration {
        const val MICRO = 100      // Button press, ripple effects, small state changes
        const val SHORT = 200      // Medium state changes, fab extend/collapse  
        const val MEDIUM = 300     // Screen transitions, large component changes
        const val LONG = 500       // Complex animations, dramatic effects
        const val EXTRA_LONG = 1000 // Special effects, major state transformations
        
        // Music player specific durations
        const val PLAYER_CONTROLS = 150   // Play/pause button, seek bar
        const val TRACK_TRANSITION = 250  // Track change animations
        const val ALBUM_ART_FADE = 300    // Album art crossfade
        const val QUEUE_ITEM = 200        // Queue item add/remove
        const val SEARCH_RESULTS = 180    // Search result appearance
        const val PLAYLIST_ITEM = 220     // Playlist item interactions
    }
    
    /**
     * Material Design 3 easing curves optimized for different interaction types.
     */
    object AppEasing {
        // Standard Material Design 3 easing curves
        val Linear: Easing = LinearEasing
        val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        
        // Custom easing for music player interactions
        val PlayerControl: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
        val TrackTransition: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val AlbumArtCrossfade: Easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
        val QueueItemChange: Easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
        val SearchResponse: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    }
    
    /**
     * Creates an animation spec for micro interactions (button presses, ripples).
     */
    fun microInteraction(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.shouldAnimateMicroInteractions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.MICRO),
                easing = AppEasing.FastOutSlowIn
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates an animation spec for content transitions and state changes.
     */
    fun contentTransition(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.SHORT),
                easing = when (config.performanceMode) {
                    PerformanceMode.HIGH_PERFORMANCE -> AppEasing.Emphasized
                    PerformanceMode.BALANCED -> AppEasing.FastOutSlowIn
                    PerformanceMode.BATTERY_SAVER -> AppEasing.Linear
                }
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates an animation spec for large motions and screen transitions.
     */
    fun largeMotion(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.shouldAnimateLargeMotions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.MEDIUM),
                easing = when (config.performanceMode) {
                    PerformanceMode.HIGH_PERFORMANCE -> AppEasing.Emphasized
                    PerformanceMode.BALANCED -> AppEasing.EmphasizedDecelerate
                    PerformanceMode.BATTERY_SAVER -> AppEasing.FastOutSlowIn
                }
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates optimized animation specs for player controls.
     */
    fun playerControl(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enablePlayerAnimations && config.shouldAnimateMicroInteractions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.PLAYER_CONTROLS),
                easing = AppEasing.PlayerControl
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates animation specs for track transitions.
     */
    fun trackTransition(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enablePlayerAnimations && config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.TRACK_TRANSITION),
                easing = AppEasing.TrackTransition
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates animation specs for album art crossfade.
     */
    fun albumArtCrossfade(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enablePlayerAnimations && config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.ALBUM_ART_FADE),
                easing = AppEasing.AlbumArtCrossfade
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates animation specs for queue item changes.
     */
    fun queueItemChange(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enablePlaylistAnimations && config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.QUEUE_ITEM),
                easing = AppEasing.QueueItemChange
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates animation specs for search result animations.
     */
    fun searchResults(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enableSearchAnimations && config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.SEARCH_RESULTS),
                easing = AppEasing.SearchResponse
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates animation specs for playlist item interactions.
     */
    fun playlistItem(config: AnimationConfig): FiniteAnimationSpec<Float> {
        return if (config.enablePlaylistAnimations && config.shouldAnimateTransitions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.PLAYLIST_ITEM),
                easing = AppEasing.FastOutSlowIn
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Creates enter transition for screen navigation.
     */
    fun enter(config: AnimationConfig): EnterTransition {
        return if (config.shouldAnimateTransitions()) {
            slideInHorizontally(
                animationSpec = largeMotionOffset(config),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(
                animationSpec = contentTransition(config)
            )
        } else {
            EnterTransition.None
        }
    }
    
    /**
     * Creates exit transition for screen navigation.
     */
    fun exit(config: AnimationConfig): ExitTransition {
        return if (config.shouldAnimateTransitions()) {
            slideOutHorizontally(
                animationSpec = largeMotionOffset(config),
                targetOffsetX = { fullWidth -> -fullWidth }
            ) + fadeOut(
                animationSpec = contentTransition(config)
            )
        } else {
            ExitTransition.None
        }
    }
    
    /**
     * Creates fade-only enter transition (used for tab navigation).
     */
    fun fadeEnter(config: AnimationConfig): EnterTransition {
        return if (config.shouldAnimateTransitions()) {
            fadeIn(
                animationSpec = tween(
                    durationMillis = config.scaleDuration(Duration.SHORT / 2),
                    delayMillis = config.scaleDuration(Duration.SHORT / 2),
                    easing = AppEasing.FastOutSlowIn
                )
            )
        } else {
            EnterTransition.None
        }
    }
    
    /**
     * Creates fade-only exit transition (used for tab navigation).
     */
    fun fadeExit(config: AnimationConfig): ExitTransition {
        return if (config.shouldAnimateTransitions()) {
            fadeOut(
                animationSpec = tween(
                    durationMillis = config.scaleDuration(Duration.SHORT / 2),
                    easing = AppEasing.FastOutSlowIn
                )
            )
        } else {
            ExitTransition.None
        }
    }
    
    /**
     * Creates animation spec for IntOffset (slide animations).
     */
    fun largeMotionOffset(config: AnimationConfig): FiniteAnimationSpec<IntOffset> {
        return if (config.shouldAnimateLargeMotions()) {
            tween(
                durationMillis = config.scaleDuration(Duration.MEDIUM),
                easing = when (config.performanceMode) {
                    PerformanceMode.HIGH_PERFORMANCE -> AppEasing.Emphasized
                    PerformanceMode.BALANCED -> AppEasing.EmphasizedDecelerate
                    PerformanceMode.BATTERY_SAVER -> AppEasing.FastOutSlowIn
                }
            )
        } else {
            tween(durationMillis = 0)
        }
    }
    
    /**
     * Utility function to scale duration based on animation config.
     */
    fun scaledDuration(baseDuration: Int, config: AnimationConfig): Int {
        return config.scaleDuration(baseDuration)
    }
    
    /**
     * Creates a tween animation spec with scaled duration.
     */
    fun scaledTween(
        baseDuration: Int,
        config: AnimationConfig,
        easing: Easing = AppEasing.FastOutSlowIn,
        delayMillis: Int = 0
    ): FiniteAnimationSpec<Float> {
        return tween(
            durationMillis = config.scaleDuration(baseDuration),
            delayMillis = config.scaleDuration(delayMillis),
            easing = easing
        )
    }
} 