package com.async.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.async.app.di.AppModule
import com.async.app.navigation.*
import com.async.app.ui.components.AsyncBottomNavigation
import com.async.app.ui.components.PermissionManager
import com.async.app.ui.theme.AsyncTheme

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
                // Show main app content
                TabNavigator(HomeTab) { tabNavigator ->
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            AsyncBottomNavigation(tabNavigator = tabNavigator)
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
        }
    }
} 
