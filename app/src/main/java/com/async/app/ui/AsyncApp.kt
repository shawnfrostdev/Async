package com.async.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.async.app.di.AppModule
import com.async.app.navigation.*
import com.async.app.ui.components.AsyncBottomNavigation
import com.async.app.ui.components.PermissionManager
import com.async.app.ui.components.MiniPlayer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.app.ui.vm.PlayerViewModel
import com.async.app.ui.theme.AsyncTheme
import com.async.app.ui.screens.player.PlayerScreenContent

enum class AppScreen {
    MAIN_TABS, PLAYER
}

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
            color = MaterialTheme.colorScheme.background
        ) {
            if (!permissionsGranted) {
                // Show permission manager on first launch
                PermissionManager(
                    onPermissionsGranted = {
                        permissionsGranted = true
                    }
                )
            } else {
                // Show main app content with animated navigation
                val playerViewModel: PlayerViewModel = viewModel()
                val playerUiState = playerViewModel.uiState
                var currentScreen by remember { mutableStateOf(AppScreen.MAIN_TABS) }
                
                // Track the current tab to return to the right place
                var currentTabState by remember { mutableStateOf<Tab>(HomeTab) }
                
                // Handle back gesture when player is open
                BackHandler(enabled = currentScreen == AppScreen.PLAYER) {
                    currentScreen = AppScreen.MAIN_TABS
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Main tabs screen (always present)
                    MainTabsScreen(
                        playerViewModel = playerViewModel,
                        initialTab = currentTabState,
                        onTabChanged = { newTab -> currentTabState = newTab },
                        onOpenPlayer = { currentScreen = AppScreen.PLAYER }
                    )
                    
                    // Player screen with slide-up animation
                    AnimatedVisibility(
                        visible = currentScreen == AppScreen.PLAYER,
                        enter = slideInVertically(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            ),
                            initialOffsetY = { fullHeight -> fullHeight }
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 100
                            )
                        ),
                        exit = slideOutVertically(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutLinearInEasing
                            ),
                            targetOffsetY = { fullHeight -> fullHeight }
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 250
                            )
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Full screen player overlay
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
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
}

@Composable
private fun MainTabsScreen(
    playerViewModel: PlayerViewModel,
    initialTab: Tab,
    onTabChanged: (Tab) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val playerUiState = playerViewModel.uiState
    
    TabNavigator(initialTab) { tabNavigator ->
        // Track tab changes
        LaunchedEffect(tabNavigator.current) {
            onTabChanged(tabNavigator.current)
        }
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column {
                    // MiniPlayer above navigation
                    if (playerUiState.currentTrack != null) {
                        MiniPlayer(
                            currentTrack = playerUiState.currentTrack,
                            isPlaying = playerUiState.isPlaying,
                            onPlayPause = { playerViewModel.playPause() },
                            onExpandPlayer = onOpenPlayer
                        )
                    }
                    AsyncBottomNavigation(tabNavigator = tabNavigator)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
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
