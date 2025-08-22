package com.example.async.ui.animation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavBackStackEntry
import com.example.async.navigation.AsyncDestinations
import com.example.async.ui.animation.config.AnimationConfig
import com.example.async.ui.animation.config.AppAnimationSpecs

/**
 * CompositionLocal for accessing current animation configuration
 */
val LocalAnimationConfig = compositionLocalOf { AnimationConfig() }

/**
 * Transition types available for navigation animations in the Async music player.
 */
enum class TransitionType {
    /**
     * Horizontal slide transition - used for main navigation flow
     */
    SHARED_AXIS_X,
    
    /**
     * Vertical slide transition - used for modal presentations
     */
    SHARED_AXIS_Y,
    
    /**
     * Fade through transition - used for tab navigation
     */
    FADE_THROUGH,
    
    /**
     * Container transform - used for detailed views
     */
    CONTAINER_TRANSFORM,
    
    /**
     * Simple slide - basic slide without fade
     */
    SLIDE,
    
    /**
     * No animation
     */
    NONE
}

/**
 * Direction of navigation for determining animation direction
 */
enum class NavigationDirection {
    FORWARD,
    BACKWARD,
    TAB_SWITCH,
    MODAL_UP,
    MODAL_DOWN
}

/**
 * Enhanced navigation transition system for the Async music player.
 * Provides flexible animations based on navigation context and destination types.
 */
object NavigationTransitions {
    
    /**
     * Tab destinations that use fade-through transitions
     */
    private val TAB_DESTINATIONS = setOf(
        AsyncDestinations.HOME,
        AsyncDestinations.SEARCH,
        AsyncDestinations.LIBRARY,
        AsyncDestinations.SETTINGS
    )
    
    /**
     * Modal destinations that slide up from bottom
     */
    private val MODAL_DESTINATIONS = setOf(
        AsyncDestinations.PLAYER,
        AsyncDestinations.EXTENSIONS
    )
    
    /**
     * Determines the navigation direction based on source and target destinations
     */
    fun getNavigationDirection(
        initialRoute: String?,
        targetRoute: String?,
        isPopping: Boolean = false
    ): NavigationDirection {
        return when {
            isPopping -> NavigationDirection.BACKWARD
            initialRoute in TAB_DESTINATIONS && targetRoute in TAB_DESTINATIONS -> NavigationDirection.TAB_SWITCH
            targetRoute in MODAL_DESTINATIONS -> NavigationDirection.MODAL_UP
            initialRoute in MODAL_DESTINATIONS -> NavigationDirection.MODAL_DOWN
            else -> NavigationDirection.FORWARD
        }
    }
    
    /**
     * Creates content transform based on transition type and navigation direction
     */
    fun createContentTransform(
        transitionType: TransitionType,
        direction: NavigationDirection,
        config: AnimationConfig
    ): ContentTransform {
        return when (transitionType) {
            TransitionType.SHARED_AXIS_X -> createSharedAxisX(direction, config)
            TransitionType.SHARED_AXIS_Y -> createSharedAxisY(direction, config)
            TransitionType.FADE_THROUGH -> createFadeThrough(config)
            TransitionType.CONTAINER_TRANSFORM -> createContainerTransform(config)
            TransitionType.SLIDE -> createSlide(direction, config)
            TransitionType.NONE -> createNone()
        }
    }
    
    /**
     * Creates horizontal shared axis transition (slide + fade)
     */
    private fun createSharedAxisX(
        direction: NavigationDirection,
        config: AnimationConfig
    ): ContentTransform {
        val slideDistance = 30 // Percentage of screen width
        
        val (enterSlide, exitSlide) = when (direction) {
            NavigationDirection.FORWARD -> Pair(slideDistance, -slideDistance)
            NavigationDirection.BACKWARD -> Pair(-slideDistance, slideDistance)
            else -> Pair(slideDistance, -slideDistance)
        }
        
        return slideInHorizontally(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            initialOffsetX = { fullWidth -> (fullWidth * enterSlide) / 100 }
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        ) togetherWith slideOutHorizontally(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            targetOffsetX = { fullWidth -> (fullWidth * exitSlide) / 100 }
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        )
    }
    
    /**
     * Creates vertical shared axis transition
     */
    private fun createSharedAxisY(
        direction: NavigationDirection,
        config: AnimationConfig
    ): ContentTransform {
        val slideDistance = 30 // Percentage of screen height
        
        val (enterSlide, exitSlide) = when (direction) {
            NavigationDirection.MODAL_UP -> Pair(slideDistance, -slideDistance)
            NavigationDirection.MODAL_DOWN -> Pair(-slideDistance, slideDistance)
            else -> Pair(slideDistance, -slideDistance)
        }
        
        return slideInVertically(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            initialOffsetY = { fullHeight -> (fullHeight * enterSlide) / 100 }
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        ) togetherWith slideOutVertically(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            targetOffsetY = { fullHeight -> (fullHeight * exitSlide) / 100 }
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        )
    }
    
    /**
     * Creates fade through transition (fade to black, then fade in)
     */
    private fun createFadeThrough(config: AnimationConfig): ContentTransform {
        val fadeDuration = AppAnimationSpecs.scaledDuration(
            AppAnimationSpecs.Duration.SHORT / 2, 
            config
        )
        
        return fadeIn(
            animationSpec = tween(
                durationMillis = fadeDuration,
                delayMillis = fadeDuration
            )
        ) togetherWith fadeOut(
            animationSpec = tween(durationMillis = fadeDuration)
        )
    }
    
    /**
     * Creates container transform transition (scale + fade)
     */
    private fun createContainerTransform(config: AnimationConfig): ContentTransform {
        return scaleIn(
            animationSpec = AppAnimationSpecs.largeMotion(config),
            initialScale = 0.8f
        ) + fadeIn(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        ) togetherWith scaleOut(
            animationSpec = AppAnimationSpecs.largeMotion(config),
            targetScale = 1.1f
        ) + fadeOut(
            animationSpec = AppAnimationSpecs.contentTransition(config)
        )
    }
    
    /**
     * Creates simple slide transition without fade
     */
    private fun createSlide(
        direction: NavigationDirection,
        config: AnimationConfig
    ): ContentTransform {
        val (enterOffset, exitOffset) = when (direction) {
            NavigationDirection.FORWARD -> Pair(
                { width: Int -> width },
                { width: Int -> -width }
            )
            NavigationDirection.BACKWARD -> Pair(
                { width: Int -> -width },
                { width: Int -> width }
            )
            else -> Pair(
                { width: Int -> width },
                { width: Int -> -width }
            )
        }
        
        return slideInHorizontally(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            initialOffsetX = enterOffset
        ) togetherWith slideOutHorizontally(
            animationSpec = AppAnimationSpecs.largeMotionOffset(config),
            targetOffsetX = exitOffset
        )
    }
    
    /**
     * Creates no animation transition
     */
    private fun createNone(): ContentTransform {
        return EnterTransition.None togetherWith ExitTransition.None
    }
}

/**
 * Extension function to get appropriate transition type for a destination
 */
fun String?.getTransitionType(): TransitionType {
    return when (this) {
        AsyncDestinations.HOME, 
        AsyncDestinations.SEARCH, 
        AsyncDestinations.LIBRARY, 
        AsyncDestinations.SETTINGS -> TransitionType.FADE_THROUGH
        
        AsyncDestinations.PLAYER -> TransitionType.SHARED_AXIS_Y
        AsyncDestinations.EXTENSIONS -> TransitionType.SHARED_AXIS_Y
        AsyncDestinations.PLAYLISTS -> TransitionType.SHARED_AXIS_X
        
        else -> TransitionType.SHARED_AXIS_X
    }
}

/**
 * Enhanced transition scope for AnimatedContentTransitionScope
 */
fun <S> AnimatedContentTransitionScope<S>.musicPlayerTransition(
    config: AnimationConfig = AnimationConfig()
): ContentTransform where S : Any {
    val initialRoute = initialState.toString()
    val targetRoute = targetState.toString()
    
    val direction = NavigationTransitions.getNavigationDirection(
        initialRoute = initialRoute,
        targetRoute = targetRoute,
        isPopping = false // This would need to be determined from navigation state
    )
    
    val transitionType = targetRoute.getTransitionType()
    
    return NavigationTransitions.createContentTransform(
        transitionType = transitionType,
        direction = direction,
        config = config
    )
} 