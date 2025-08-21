package com.example.async.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
 * Main navigation graph for the Async music player
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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