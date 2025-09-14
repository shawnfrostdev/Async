package app.async.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import app.async.R
import app.async.app.navigation.ExtensionManagementScreenNav
import app.async.app.ui.theme.AsyncColors
import app.async.app.ui.components.layout.*
import app.async.app.navigation.AboutScreenNav

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
            StandardScreenHeader(
                title = stringResource(R.string.title_settings),
                subtitle = stringResource(R.string.settings_subtitle),
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.screen_margin_horizontal))
            )
            
            // Settings content
            StandardLazyColumn(
                hasTopBar = true,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                item {
                    SettingsSection(title = stringResource(R.string.settings_audio_playback))
                }
                
                items(audioSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                    SettingsSection(title = stringResource(R.string.settings_content_sources))
                }
                
                items(contentSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                    SettingsSection(title = stringResource(R.string.settings_appearance))
                }
                
                items(appearanceSettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                    SettingsSection(title = stringResource(R.string.settings_privacy_data))
                }
                
                items(privacySettings) { setting ->
                    SettingsItem(
                        setting = setting,
                        onNavigate = { navigator?.push(it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                    SettingsSection(title = stringResource(R.string.settings_support))
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
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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

// Settings data - Note: In a real app, these would be generated with @Composable functions
// to access string resources, but for simplicity we'll keep static data here
private val audioSettings = listOf(
    SettingItem(
        title = "Audio Quality", // Would use stringResource(R.string.settings_audio_quality)
        subtitle = "Bitrate, format, and streaming quality", // Would use stringResource(R.string.settings_audio_quality_desc)
        icon = Icons.Outlined.HighQuality
    ),
    SettingItem(
        title = "Playback Settings", // Would use stringResource(R.string.settings_playback_settings)
        subtitle = "Crossfade, gapless playback, and audio effects", // Would use stringResource(R.string.settings_playback_settings_desc)
        icon = Icons.Outlined.Tune
    ),
    SettingItem(
        title = "Equalizer", // Would use stringResource(R.string.settings_equalizer)
        subtitle = "Audio enhancement and sound presets", // Would use stringResource(R.string.settings_equalizer_desc)
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
        icon = Icons.AutoMirrored.Outlined.Help
    ),
    SettingItem(
        title = "About",
        subtitle = "App version, licenses, and credits",
        icon = Icons.Outlined.Info,
        destination = AboutScreenNav
    )
) 
