package app.async.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import soup.compose.material.motion.MaterialMotion
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

/**
 * Material Shared Axis screen transitions for Async navigation
 * Based on Mihon animation guide specifications
 */

/**
 * Default navigator screen transition with Material Shared Axis X animation
 * Implements back gesture animations as per animation guide
 */
@Composable
fun DefaultNavigatorScreenTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val slideDistance = rememberSlideDistance()
    val isForward = navigator.lastEvent != StackEvent.Pop
    
    // Use MaterialMotion as per the official guide
    MaterialMotion(
        targetState = navigator.lastItem,
        transitionSpec = {
            materialSharedAxisX(forward = isForward, slideDistance = slideDistance)
        },
        modifier = modifier,
        pop = !isForward
    ) { screen ->
        navigator.saveableState("transition", screen) {
            screen.Content()
        }
    }
}

// ScreenTransition function removed - using MaterialMotion directly as per the guide

/**
 * Nested navigator back handling with proper delegation
 * For screens that contain their own Navigator (like Settings)
 */
@Composable
fun createNestedBackHandler(
    navigator: Navigator,
    parentNavigator: Navigator? = null
): () -> Unit {
    return {
        if (navigator.canPop) {
            navigator.pop()
        } else {
            parentNavigator?.pop()
        }
    }
}

/**
 * Sheet transitions for modal content
 * Used for adaptive sheets and dialogs
 */
@OptIn(ExperimentalAnimationApi::class)
fun sheetTransition(): ContentTransform {
    return fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
            fadeOut(animationSpec = tween(90))
}

/**
 * Custom easing curves for Material Design animations
 */
private val EasingLinearCubicBezier = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
private val EasingEmphasizedCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/**
 * Animation timing constants based on Material Design
 */
object AnimationTiming {
    const val DURATION_SHORT = 150   // Quick state changes
    const val DURATION_MEDIUM = 300  // Standard UI transitions  
    const val DURATION_LONG = 500    // Complex transformations
    
    // Sheet-specific durations
    const val SHEET_FADE_DURATION = 220
    const val SHEET_FADE_DELAY = 90
    const val SHEET_EXIT_DURATION = 90
}

/**
 * LocalBackPress CompositionLocal for nested navigation
 * Allows child screens to access parent navigation back handling
 */
val LocalBackPress = compositionLocalOf<() -> Unit> {
    error("No back handler provided")
} 