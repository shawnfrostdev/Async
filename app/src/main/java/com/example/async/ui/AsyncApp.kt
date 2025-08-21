package com.example.async.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.async.navigation.AsyncDestinations
import com.example.async.navigation.AsyncNavigation
import com.example.async.ui.components.AsyncBottomNavigation
import com.example.async.ui.components.MiniPlayer
import com.example.async.ui.theme.AsyncTheme

/**
 * Main app composable that sets up the overall UI structure
 */
@Composable
fun AsyncApp() {
    AsyncTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Show bottom navigation for main screens, hide for player and settings screens
        val showBottomNav = currentRoute != AsyncDestinations.PLAYER && 
                           currentRoute != AsyncDestinations.EXTENSIONS &&
                           currentRoute != AsyncDestinations.SETTINGS
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomNav) {
                    Column {
                        // Mini player above bottom navigation
                        MiniPlayer(
                            onExpandPlayer = {
                                navController.navigate(AsyncDestinations.PLAYER)
                            },
                            trackTitle = "", // Empty until playback is implemented
                            trackArtist = ""  // Empty until playback is implemented
                        )
                        AsyncBottomNavigation(navController = navController)
                    }
                }
            },
            content = { paddingValues ->
                AsyncNavigation(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        )
    }
} 