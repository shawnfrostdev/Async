package com.async.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.async.app.navigation.*

@Composable
fun AsyncBottomNavigation(
    tabNavigator: TabNavigator,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(HomeTab, SearchTab, LibraryTab, SettingsTab)
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEach { tab ->
            val isSelected = tabNavigator.current == tab
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { tabNavigator.current = tab },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            HomeTab -> Icons.Default.Home
                            SearchTab -> Icons.Default.Search
                            LibraryTab -> Icons.Default.LibraryMusic
                            SettingsTab -> Icons.Default.Settings
                            else -> Icons.Default.Home
                        },
                        contentDescription = tab.options.title
                    )
                },
                label = { Text(tab.options.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
} 
