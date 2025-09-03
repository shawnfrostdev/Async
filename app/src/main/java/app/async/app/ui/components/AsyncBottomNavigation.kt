package app.async.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabNavigator
import app.async.R
import app.async.app.navigation.*
import app.async.app.ui.theme.AsyncColors

@Composable
fun AsyncBottomNavigation(
    tabNavigator: TabNavigator,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = AsyncColors.Primary,
        tonalElevation = 0.dp
    ) {
        val tabs = listOf(HomeTab, SearchTab, LibraryTab, SettingsTab)
        
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
                        contentDescription = when (tab) {
                            HomeTab -> stringResource(R.string.nav_home)
                            SearchTab -> stringResource(R.string.nav_search)
                            LibraryTab -> stringResource(R.string.nav_library)
                            SettingsTab -> stringResource(R.string.nav_settings)
                            else -> stringResource(R.string.nav_home)
                        },
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
                    )
                },
                label = {
                    Text(
                        text = when (tab) {
                            HomeTab -> stringResource(R.string.nav_home)
                            SearchTab -> stringResource(R.string.nav_search)
                            LibraryTab -> stringResource(R.string.nav_library)
                            SettingsTab -> stringResource(R.string.nav_settings)
                            else -> stringResource(R.string.nav_home)
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AsyncColors.Primary,
                    selectedTextColor = AsyncColors.Primary,
                    unselectedIconColor = AsyncColors.TextSecondary,
                    unselectedTextColor = AsyncColors.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
} 
