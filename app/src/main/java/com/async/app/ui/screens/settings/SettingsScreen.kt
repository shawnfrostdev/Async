package com.async.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.async.app.navigation.ExtensionManagementScreenNav
import com.async.app.navigation.AboutScreenNav

/**
 * Settings screen with modern design and proper organization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Navigator(SettingsMainScreen) { navigator ->
        navigator.lastItem.Content()
    }
}

object SettingsMainScreen : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = cafe.adriel.voyager.navigator.LocalNavigator.current
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Header section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize your music experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Settings content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SettingsSection(title = "Audio & Playback")
                }
                
                items(audioSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = "Content & Sources")
                }
                
                items(contentSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = "Appearance")
                }
                
                items(appearanceSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = "Privacy & Data")
                }
                
                items(privacySettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = "Support")
                }
                
                items(supportSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    setting: SettingItem,
    onNavigate: (cafe.adriel.voyager.core.screen.Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { setting.destination?.let { onNavigate(it) } }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = setting.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (setting.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = setting.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (setting.destination != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Settings data model
data class SettingItem(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val destination: cafe.adriel.voyager.core.screen.Screen? = null
)

// Settings data
private val audioSettings = listOf(
    SettingItem(
        title = "Audio Quality",
        subtitle = "Bitrate, format, and streaming quality",
        icon = Icons.Outlined.HighQuality
    ),
    SettingItem(
        title = "Playback Settings",
        subtitle = "Crossfade, gapless playback, and audio effects",
        icon = Icons.Outlined.Tune
    ),
    SettingItem(
        title = "Equalizer",
        subtitle = "Audio enhancement and sound presets",
        icon = Icons.Outlined.GraphicEq
    )
)

private val contentSettings = listOf(
    SettingItem(
        title = "Extensions",
        subtitle = "Manage music source extensions",
        icon = Icons.Outlined.Extension,
        destination = ExtensionManagementScreenNav
    ),
    SettingItem(
        title = "Downloads",
        subtitle = "Offline music and caching settings",
        icon = Icons.Outlined.Download
    ),
    SettingItem(
        title = "Library Sync",
        subtitle = "Import and sync your music library",
        icon = Icons.Outlined.Sync
    )
)

private val appearanceSettings = listOf(
    SettingItem(
        title = "Theme",
        subtitle = "Dark, light, or system theme",
        icon = Icons.Outlined.Palette
    ),
    SettingItem(
        title = "Display",
        subtitle = "Text size, animations, and layout",
        icon = Icons.Outlined.DisplaySettings
    ),
    SettingItem(
        title = "Now Playing",
        subtitle = "Player screen appearance and controls",
        icon = Icons.Outlined.MusicNote
    )
)

private val privacySettings = listOf(
    SettingItem(
        title = "Privacy",
        subtitle = "Data collection and usage preferences",
        icon = Icons.Outlined.Security
    ),
    SettingItem(
        title = "Storage",
        subtitle = "Cache management and data usage",
        icon = Icons.Outlined.Storage
    ),
    SettingItem(
        title = "Permissions",
        subtitle = "App permissions and extension security",
        icon = Icons.Outlined.AdminPanelSettings
    )
)

private val supportSettings = listOf(
    SettingItem(
        title = "Help & Feedback",
        subtitle = "Get help and send feedback",
        icon = Icons.Outlined.Help
    ),
    SettingItem(
        title = "About",
        subtitle = "App version, licenses, and credits",
        icon = Icons.Outlined.Info,
        destination = AboutScreenNav
    )
) 