package com.example.async.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.async.R
import com.example.async.navigation.AsyncDestinations

/**
 * Bottom navigation bar for the main app navigation
 */
@Composable
fun AsyncBottomNavigation(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(
        BottomNavItem(
            route = AsyncDestinations.HOME,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            labelResId = R.string.nav_home
        ),
        BottomNavItem(
            route = AsyncDestinations.SEARCH,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            labelResId = R.string.nav_search
        ),
        BottomNavItem(
            route = AsyncDestinations.LIBRARY,
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Outlined.List,
            labelResId = R.string.nav_library
        ),

        BottomNavItem(
            route = AsyncDestinations.SETTINGS,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            labelResId = R.string.nav_settings
        )
    )
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelResId)
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentDestination != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid large back stack
                            popUpTo(AsyncDestinations.HOME) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

/**
 * Data class representing a bottom navigation item
 */
private data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelResId: Int
) 