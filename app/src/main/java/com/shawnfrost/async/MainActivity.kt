package com.shawnfrost.async

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import dagger.hilt.android.AndroidEntryPoint
import com.shawnfrost.async.navigation.Screen
import com.shawnfrost.async.ui.components.BottomNavBar
import com.shawnfrost.async.ui.components.MiniPlayer
import com.shawnfrost.async.ui.screens.HomeScreen
import com.shawnfrost.async.ui.screens.SearchScreen
import com.shawnfrost.async.ui.screens.LibraryScreen
import com.shawnfrost.async.ui.screens.SettingsScreen
import com.shawnfrost.async.ui.theme.AsyncTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AsyncApp()
                }
            }
        }
    }
}

@Composable
fun AsyncApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            Column {
                MiniPlayer(
                    onExpandClick = {
                        // TODO: Navigate to full player screen
                    }
                )
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) {
                HomeScreen()
            }
            composable(
                route = Screen.Search.route,
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) {
                SearchScreen()
            }
            composable(
                route = Screen.Library.route,
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) {
                LibraryScreen()
            }
            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) {
                SettingsScreen()
            }
        }
    }
} 