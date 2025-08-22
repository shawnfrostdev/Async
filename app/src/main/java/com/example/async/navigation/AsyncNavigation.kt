package com.example.async.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.async.ui.screens.home.HomeScreen
import com.example.async.ui.screens.search.SearchScreen
import com.example.async.ui.screens.library.LibraryScreen
import com.example.async.ui.screens.player.PlayerScreen
import com.example.async.ui.screens.playlists.PlaylistsScreen
import com.example.async.ui.screens.settings.SettingsScreen
import com.example.async.ui.screens.extensions.ExtensionManagementScreen

/**
 * Animation constants following Android design guidelines for optimal performance
 */
private object AnimationConstants {
    const val ANIMATION_DURATION = 300 // 0.3 seconds as requested
    const val FADE_DURATION = 150 // Shorter fade for smoother black transition
    
    // Material Design easing for natural motion
    val MOTION_EASING = FastOutSlowInEasing
}

/**
 * Main navigation graph for the Async music player with optimized animations
 * Implements black fade transitions with slide animations:
 * - Entering: slide from right to left + fade in from black
 * - Exiting: slide left to right + fade out to black
 * - Duration: 0.3s for all transitions
 * - Optimized for 60fps performance
 */
@Composable
fun AsyncNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AsyncDestinations.HOME,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            // Entering: slide from right + fade in from black
            slideInHorizontally(
                animationSpec = tween(
                    durationMillis = AnimationConstants.ANIMATION_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                ),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.FADE_DURATION,
                    delayMillis = AnimationConstants.FADE_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                )
            )
        },
        exitTransition = {
            // Exiting: slide to left + fade out to black
            slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = AnimationConstants.ANIMATION_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                ),
                targetOffsetX = { fullWidth -> -fullWidth }
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AnimationConstants.FADE_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                )
            )
        },
        popEnterTransition = {
            // Pop entering: slide from left + fade in from black
            slideInHorizontally(
                animationSpec = tween(
                    durationMillis = AnimationConstants.ANIMATION_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                ),
                initialOffsetX = { fullWidth -> -fullWidth }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.FADE_DURATION,
                    delayMillis = AnimationConstants.FADE_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                )
            )
        },
        popExitTransition = {
            // Pop exiting: slide to right + fade out to black
            slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = AnimationConstants.ANIMATION_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                ),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AnimationConstants.FADE_DURATION,
                    easing = AnimationConstants.MOTION_EASING
                )
            )
        }
    ) {
        // Home screen - main entry point
        composable(AsyncDestinations.HOME) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(AsyncDestinations.SEARCH) },
                onNavigateToLibrary = { navController.navigate(AsyncDestinations.LIBRARY) },
                onNavigateToPlayer = { navController.navigate(AsyncDestinations.PLAYER) },
                onNavigateToPlaylists = { navController.navigate(AsyncDestinations.PLAYLISTS) }
            )
        }
        
        // Search screen
        composable(AsyncDestinations.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(AsyncDestinations.PLAYER) }
            )
        }
        
        // Library screen
        composable(AsyncDestinations.LIBRARY) {
            LibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlaylists = { navController.navigate(AsyncDestinations.PLAYLISTS) },
                onNavigateToPlayer = { navController.navigate(AsyncDestinations.PLAYER) }
            )
        }
        
        // Player screen
        composable(AsyncDestinations.PLAYER) {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Playlists screen
        composable(AsyncDestinations.PLAYLISTS) {
            PlaylistsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(AsyncDestinations.PLAYER) }
            )
        }
        
        // Settings screen
        composable(AsyncDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExtensions = { navController.navigate(AsyncDestinations.EXTENSIONS) }
            )
        }
        
        // Extension management screen
        composable(AsyncDestinations.EXTENSIONS) {
            ExtensionManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Navigation destinations for the app
 */
object AsyncDestinations {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val PLAYER = "player"
    const val PLAYLISTS = "playlists"
    const val SETTINGS = "settings"
    const val EXTENSIONS = "extensions"
} 