package app.async.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.media3.common.util.UnstableApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import app.async.app.di.AppModule
import app.async.app.navigation.*
import app.async.app.ui.components.AsyncBottomNavigation
import app.async.app.ui.components.PermissionManager
import app.async.app.ui.components.MiniPlayer
import androidx.lifecycle.viewmodel.compose.viewModel
import app.async.app.ui.vm.PlayerViewModel
import app.async.app.ui.theme.AsyncTheme
import app.async.app.ui.theme.AsyncColors
import app.async.app.ui.screens.player.PlayerScreenContent
import androidx.compose.ui.unit.dp

enum class AppScreen {
    MAIN_TABS, PLAYER
}

@UnstableApi
@Composable
fun AsyncApp() {
    val extensionService = AppModule.getExtensionService()
    var permissionsGranted by remember { mutableStateOf(false) }
    
    // Sync extensions when app starts/resumes
    LaunchedEffect(Unit) {
        extensionService.syncInstalledExtensions()
    }

    AsyncTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AsyncColors.Background
        ) {
            if (!permissionsGranted) {
                // Show permission manager on first launch
                PermissionManager(
                    onPermissionsGranted = {
                        permissionsGranted = true
                    }
                )
            } else {
                // Show main app content
                val playerViewModel: PlayerViewModel = viewModel()
                val playerUiState = playerViewModel.uiState
                var currentScreen by remember { mutableStateOf(AppScreen.MAIN_TABS) }

                // Handle back gesture when player is open
                BackHandler(enabled = currentScreen == AppScreen.PLAYER) {
                    currentScreen = AppScreen.MAIN_TABS
                }

                // Auto-open player when track starts playing
                LaunchedEffect(playerUiState.currentTrack) {
                    if (playerUiState.currentTrack != null) {
                        currentScreen = AppScreen.PLAYER
                    }
                }

                when (currentScreen) {
                    AppScreen.MAIN_TABS -> {
                        TabNavigator(HomeTab) { tabNavigator ->
                            Scaffold(
                                bottomBar = {
                                    Column {
                                        // MiniPlayer above navigation
                                        if (playerUiState.currentTrack != null) {
                                            MiniPlayer(
                                                currentTrack = playerUiState.currentTrack,
                                                isPlaying = playerUiState.isPlaying,
                                                onPlayPause = { playerViewModel.playPause() },
                                                onExpandPlayer = { currentScreen = AppScreen.PLAYER }
                                            )
                                        }
                                        AsyncBottomNavigation(tabNavigator = tabNavigator)
                                    }
                                }
                            ) { paddingValues ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {
                                    tabNavigator.current.Content()
                                }
                            }
                        }
                    }
                    AppScreen.PLAYER -> {
                        PlayerScreenContent(
                            onNavigateBack = { currentScreen = AppScreen.MAIN_TABS },
                            playerViewModel = playerViewModel
                        )
                    }
                }
            }
        }
    }
} 
