package app.async.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import app.async.app.navigation.ExtensionManagementScreenNav
import app.async.app.navigation.AboutScreenNav
import app.async.app.ui.theme.padding
import cafe.adriel.voyager.navigator.LocalNavigator
import app.async.app.navigation.LocalBackPress
import app.async.app.navigation.DefaultNavigatorScreenTransition

/**
 * More tab screen following exact Mihon tabscreens.md guide
 * Complete implementation with sections as specified
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen() {
    Navigator(MoreScreenMain) { navigator ->
        val parentNavigator = LocalNavigator.current
        
        // Nested navigator back handling as per animation guide
        val pop: () -> Unit = {
            if (navigator.canPop) {
                navigator.pop()
            } else {
                parentNavigator?.pop()
            }
        }
        
        CompositionLocalProvider(LocalBackPress provides pop) {
            DefaultNavigatorScreenTransition(navigator = navigator)
        }
    }
}

object MoreScreenMain : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = cafe.adriel.voyager.navigator.LocalNavigator.current
        
        MoreScreen(
            downloadQueueStateProvider = { DownloadQueueState.Stopped }, // TODO: Connect to actual state
            onClickDownloadQueue = { },
            onClickStats = { },
            onClickDataAndStorage = { },
            onClickSettings = { 
                navigator?.push(SettingsMainScreen)
            },
            onClickAbout = { 
                navigator?.push(AboutScreenNav)
            },
            onClickHelp = { },
            onClickDonate = { }
        )
    }
}

// Download Queue State as per Mihon guide
sealed interface DownloadQueueState {
    data object Stopped : DownloadQueueState
    data class Paused(val pending: Int) : DownloadQueueState
    data class Downloading(val pending: Int) : DownloadQueueState
}

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    onClickDownloadQueue: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickHelp: () -> Unit,
    onClickDonate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Following Mihon spacing: 24dp horizontal margins, consistent bottom padding
        contentPadding = PaddingValues(
            start = 24.dp, // Screen-level horizontal padding per Mihon guidelines
            top = MaterialTheme.padding.medium, // 16dp top
            end = 24.dp, // Screen-level horizontal padding per Mihon guidelines
            bottom = 24.dp // Match horizontal padding for clean UI
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.large)
    ) {
        // Logo Header as per updated Mihon guide
        item {
            LogoHeader()
        }
        
        // Download Queue Section
        item {
            DownloadQueueSection(
                downloadQueueStateProvider = downloadQueueStateProvider,
                onClickDownloadQueue = onClickDownloadQueue,
            )
        }

        // Navigation Section
        item {
            NavigationSection(
                onClickStats = onClickStats,
                onClickDataAndStorage = onClickDataAndStorage,
            )
        }

        // Settings Section
        item {
            SettingsSection(
                onClickSettings = onClickSettings,
                onClickAbout = onClickAbout,
                onClickHelp = onClickHelp,
                onClickDonate = onClickDonate,
            )
        }
    }
}

// Logo Header as per updated Mihon guide
@Composable
private fun LogoHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote, // Using music note as app logo placeholder
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(vertical = 56.dp) // Exact specification from updated guide
                .size(64.dp), // Perfect for readability without overwhelming the interface
        )

        HorizontalDivider()
    }
}

// Download Queue Section as per Mihon guide
@Composable
private fun DownloadQueueSection(
    downloadQueueStateProvider: () -> DownloadQueueState,
    onClickDownloadQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val downloadQueueState = downloadQueueStateProvider()

    Column(modifier = modifier) {
        Text(
            text = "Download Queue", // TODO: Use string resource
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.padding.small),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickDownloadQueue() },
            colors = CardDefaults.cardColors(
                containerColor = when (downloadQueueState) {
                    is DownloadQueueState.Downloading -> MaterialTheme.colorScheme.primaryContainer
                    is DownloadQueueState.Paused -> MaterialTheme.colorScheme.errorContainer
                    DownloadQueueState.Stopped -> MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.padding.medium), // 16dp internal card padding
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val (icon, iconColor) = when (downloadQueueState) {
                    is DownloadQueueState.Downloading -> Icons.Outlined.Download to MaterialTheme.colorScheme.onPrimaryContainer
                    is DownloadQueueState.Paused -> Icons.Outlined.Pause to MaterialTheme.colorScheme.onErrorContainer
                    DownloadQueueState.Stopped -> Icons.Outlined.DownloadDone to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp), // Standard Material icon size
                )

                Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))

                Column(modifier = Modifier.weight(1f)) {
                    val (titleText, subtitleText) = when (downloadQueueState) {
                        is DownloadQueueState.Downloading -> {
                            "Downloading" to "${downloadQueueState.pending} pending"
                        }
                        is DownloadQueueState.Paused -> {
                            "Paused" to "${downloadQueueState.pending} pending"
                        }
                        DownloadQueueState.Stopped -> {
                            "Download queue" to "No downloads running"
                        }
                    }

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyLarge, // 16sp per Material 3
                        color = when (downloadQueueState) {
                            is DownloadQueueState.Downloading -> MaterialTheme.colorScheme.onPrimaryContainer
                            is DownloadQueueState.Paused -> MaterialTheme.colorScheme.onErrorContainer
                            DownloadQueueState.Stopped -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium, // 14sp per Material 3
                        color = when (downloadQueueState) {
                            is DownloadQueueState.Downloading -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            is DownloadQueueState.Paused -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            DownloadQueueState.Stopped -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = when (downloadQueueState) {
                        is DownloadQueueState.Downloading -> MaterialTheme.colorScheme.onPrimaryContainer
                        is DownloadQueueState.Paused -> MaterialTheme.colorScheme.onErrorContainer
                        DownloadQueueState.Stopped -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

// Navigation Section as per Mihon guide (removed Categories)
@Composable
private fun NavigationSection(
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Navigation", // TODO: Use string resource
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.padding.small),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(vertical = MaterialTheme.padding.small)) {
                NavigationItem(
                    title = "Statistics",
                    description = "Reading stats and analytics",
                    icon = Icons.Outlined.BarChart,
                    onClick = onClickStats,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                NavigationItem(
                    title = "Data and storage",
                    description = "Manage app data",
                    icon = Icons.Outlined.Storage,
                    onClick = onClickDataAndStorage,
                )
            }
        }
    }
}

// Settings Section as per Mihon guide (added Help and Donate)
@Composable
private fun SettingsSection(
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickHelp: () -> Unit,
    onClickDonate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Settings", // TODO: Use string resource
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.padding.small),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(vertical = MaterialTheme.padding.small)) {
                NavigationItem(
                    title = "Settings",
                    description = "App preferences and configuration",
                    icon = Icons.Outlined.Settings,
                    onClick = onClickSettings,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                NavigationItem(
                    title = "About",
                    description = "App information and credits",
                    icon = Icons.Outlined.Info,
                    onClick = onClickAbout,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                NavigationItem(
                    title = "Help",
                    description = "Get help and support",
                    icon = Icons.AutoMirrored.Outlined.Help,
                    onClick = onClickHelp,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                NavigationItem(
                    title = "Donate",
                    description = "Support the project",
                    icon = Icons.Outlined.AttachMoney,
                    onClick = onClickDonate,
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(MaterialTheme.padding.medium), // 16dp internal item padding
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp), // Standard Material icon size
        )

        Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge, // 16sp per Material 3
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium, // 14sp per Material 3
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
} 
