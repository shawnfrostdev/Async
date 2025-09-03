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
import androidx.compose.ui.Alignment
import cafe.adriel.voyager.navigator.tab.TabNavigator
import app.async.R
import app.async.app.navigation.*
import app.async.app.ui.theme.AsyncColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun AsyncBottomNavigation(
    tabNavigator: TabNavigator,
    modifier: Modifier = Modifier
) {
    val tabs = remember { listOf(HomeTab, SearchTab, LibraryTab, SettingsTab) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.bottom_nav_height))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            tabs.forEach { tab ->
                val isSelected = tabNavigator.current == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { tabNavigator.current = tab }
                ) {
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
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                        tint = if (isSelected) AsyncColors.Primary else AsyncColors.TextSecondary
                    )
                    
                    Text(
                        text = when (tab) {
                            HomeTab -> stringResource(R.string.nav_home)
                            SearchTab -> stringResource(R.string.nav_search)
                            LibraryTab -> stringResource(R.string.nav_library)
                            SettingsTab -> stringResource(R.string.nav_settings)
                            else -> stringResource(R.string.nav_home)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) AsyncColors.Primary else AsyncColors.TextSecondary
                    )
                }
            }
        }
    }
} 
