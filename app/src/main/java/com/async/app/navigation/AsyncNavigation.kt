package com.async.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.async.app.ui.screens.home.HomeScreen
import com.async.app.ui.screens.search.SearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.app.ui.vm.PlayerViewModel
import logcat.logcat
import com.async.app.ui.screens.library.LibraryScreen
import com.async.app.ui.screens.player.PlayerScreen
import com.async.app.ui.screens.playlists.PlaylistsScreen
import com.async.app.ui.screens.settings.SettingsScreen
import com.async.app.ui.screens.settings.AboutScreen
import com.async.app.ui.screens.extensions.ExtensionManagementScreen

/**
 * Main navigation setup using Voyager
 */
@Composable
fun AsyncNavigation() {
    Navigator(HomeTab) { navigator ->
        SlideTransition(navigator)
    }
}

/**
 * Navigation tabs for bottom navigation
 */
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home"
        )

    @Composable
    override fun Content() {
        HomeScreen()
    }
}

object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Search"
        )

    @Composable
    override fun Content() {
        val playerViewModel: PlayerViewModel = viewModel()
        SearchScreen(
            onPlayTrack = { track -> 
                logcat("Navigation") { "onPlayTrack called for: ${track.title}" }
                playerViewModel.playTrack(track) 
            }
        )
    }
}

object LibraryTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Library"
        )

    @Composable
    override fun Content() {
        LibraryScreen()
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = "Settings"
        )

    @Composable
    override fun Content() {
        Navigator(SettingsMainScreen) { navigator ->
            navigator.lastItem.Content()
        }
    }
}

object SettingsMainScreen : Screen {
    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

/**
 * Individual screens that can be navigated to
 */
object PlayerScreenNav : Screen {
    @Composable
    override fun Content() {
        com.async.app.ui.screens.player.PlayerScreenContent()
    }
}

object ExtensionManagementScreenNav : Screen {
    @Composable
    override fun Content() {
        ExtensionManagementScreen().Content()
    }
}

object AboutScreenNav : Screen {
    @Composable
    override fun Content() {
        AboutScreen()
    }
}

/**
 * Navigation destinations
 */
sealed class NavigationDestination(val route: String) {
    object Home : NavigationDestination("home")
    object Search : NavigationDestination("search")
    object Library : NavigationDestination("library")
    object Playlists : NavigationDestination("playlists")
    object Player : NavigationDestination("player")
    object Settings : NavigationDestination("settings")
    object Extensions : NavigationDestination("extensions")
} 
