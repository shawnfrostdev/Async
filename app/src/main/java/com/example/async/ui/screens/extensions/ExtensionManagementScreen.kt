package com.example.async.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

// Data classes for repository and extension management
data class Repository(
    val url: String,
    val name: String = url.substringAfterLast("/").substringBefore("."),
    val extensions: List<Extension> = emptyList(),
    val isExpanded: Boolean = false
)

data class Extension(
    val name: String,
    val version: String,
    val developer: String = "Unknown Developer",
    val description: String = "No description available",
    val isInstalled: Boolean = false,
    val hasSettings: Boolean = false,
    val isEnabled: Boolean = true,
    val permissions: List<String> = emptyList()
)

data class ExtensionSetting(
    val key: String,
    val label: String,
    val value: String,
    val type: SettingType = SettingType.TEXT,
    val options: List<String> = emptyList()
)

enum class SettingType {
    TEXT, SWITCH, DROPDOWN, NUMBER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    var showExtensionSettingsDialog by remember { mutableStateOf<Extension?>(null) }
    var showExtensionInfoDialog by remember { mutableStateOf<Extension?>(null) }
    
    var repositories by remember { 
        mutableStateOf(
            listOf(
                Repository(
                    url = "https://github.com/example/music-extensions",
                    name = "Music Extensions",
                    extensions = listOf(
                        Extension(
                            name = "Spotify Extension", 
                            version = "1.0.0", 
                            developer = "Spotify Inc.",
                            description = "Access Spotify's vast music library with high-quality streaming",
                            isInstalled = false,
                            hasSettings = true,
                            permissions = listOf("Network Access", "Audio Playback")
                        ),
                        Extension(
                            name = "YouTube Music", 
                            version = "2.1.0", 
                            developer = "Google LLC",
                            description = "Stream music from YouTube Music with lyrics support",
                            isInstalled = false,
                            hasSettings = true,
                            permissions = listOf("Network Access", "Audio Playback", "Location")
                        ),
                        Extension(
                            name = "SoundCloud", 
                            version = "1.5.0", 
                            developer = "SoundCloud Ltd.",
                            description = "Discover independent artists and exclusive tracks",
                            isInstalled = true,
                            hasSettings = true,
                            isEnabled = true,
                            permissions = listOf("Network Access", "Audio Playback")
                        )
                    )
                ),
                Repository(
                    url = "https://github.com/community/async-plugins",
                    name = "Community Plugins",
                    extensions = listOf(
                        Extension(
                            name = "Last.fm Scrobbler", 
                            version = "1.2.0", 
                            developer = "Community",
                            description = "Track your listening habits and get music recommendations",
                            isInstalled = true,
                            hasSettings = true,
                            isEnabled = true,
                            permissions = listOf("Network Access", "Usage Analytics")
                        ),
                        Extension(
                            name = "Lyrics Provider", 
                            version = "1.0.1", 
                            developer = "Community",
                            description = "Display synchronized lyrics for currently playing songs",
                            isInstalled = false,
                            hasSettings = false,
                            permissions = listOf("Network Access")
                        ),
                        Extension(
                            name = "Equalizer Plus", 
                            version = "2.0.0", 
                            developer = "Audio Community",
                            description = "Advanced audio equalizer with custom presets",
                            isInstalled = true,
                            hasSettings = true,
                            isEnabled = false,
                            permissions = listOf("Audio Processing")
                        )
                    )
                )
            )
        )
    }
    
    var expandedRepositories by remember { mutableStateOf(setOf<String>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header with back button and add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(end = AppSpacing.s)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            HeadlineLarge(
                text = "Extensions",
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { showAddRepositoryDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Repository"
                )
            }
        }
        
        // Repository list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            items(repositories) { repository ->
                RepositoryCard(
                    repository = repository,
                    isExpanded = expandedRepositories.contains(repository.url),
                    onToggleExpanded = { 
                        expandedRepositories = if (expandedRepositories.contains(repository.url)) {
                            expandedRepositories - repository.url
                        } else {
                            expandedRepositories + repository.url
                        }
                    },
                    onExtensionInstall = { extension ->
                        // TODO: Connect to ExtensionManager
                        repositories = repositories.map { repo ->
                            if (repo.url == repository.url) {
                                repo.copy(
                                    extensions = repo.extensions.map { ext ->
                                        if (ext.name == extension.name) {
                                            ext.copy(isInstalled = !ext.isInstalled)
                                        } else ext
                                    }
                                )
                            } else repo
                        }
                    },
                    onExtensionSettings = { extension ->
                        if (extension.hasSettings && extension.isInstalled) {
                            showExtensionSettingsDialog = extension
                        }
                    },
                    onExtensionInfo = { extension ->
                        showExtensionInfoDialog = extension
                    },
                    onExtensionToggle = { extension ->
                        repositories = repositories.map { repo ->
                            if (repo.url == repository.url) {
                                repo.copy(
                                    extensions = repo.extensions.map { ext ->
                                        if (ext.name == extension.name) {
                                            ext.copy(isEnabled = !ext.isEnabled)
                                        } else ext
                                    }
                                )
                            } else repo
                        }
                    }
                )
            }
        }
    }
    
    // Add repository dialog
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepositoryDialog = false },
            onAdd = { url ->
                // TODO: Connect to ExtensionManager to fetch repository
                repositories = repositories + Repository(url = url, name = "New Repository")
                showAddRepositoryDialog = false
            }
        )
    }
    
    // Extension settings dialog
    showExtensionSettingsDialog?.let { extension ->
        ExtensionSettingsDialog(
            extension = extension,
            onDismiss = { showExtensionSettingsDialog = null },
            onSave = { settings ->
                // TODO: Save settings to ExtensionManager
                showExtensionSettingsDialog = null
            }
        )
    }
    
    // Extension info dialog
    showExtensionInfoDialog?.let { extension ->
        ExtensionInfoDialog(
            extension = extension,
            onDismiss = { showExtensionInfoDialog = null }
        )
    }
}

@Composable
private fun RepositoryCard(
    repository: Repository,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onExtensionInstall: (Extension) -> Unit,
    onExtensionSettings: (Extension) -> Unit,
    onExtensionInfo: (Extension) -> Unit,
    onExtensionToggle: (Extension) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.m)
        ) {
            // Repository header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TitleMedium(repository.name)
                    BodySmall(
                        text = repository.url,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LabelMedium(
                        text = "${repository.extensions.size} extensions • ${repository.extensions.count { it.isInstalled }} installed",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
            
            // Extensions list (when expanded)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(AppSpacing.s))
                Divider()
                
                repository.extensions.forEach { extension ->
                    ExtensionItem(
                        extension = extension,
                        onInstall = { onExtensionInstall(extension) },
                        onSettings = { onExtensionSettings(extension) },
                        onInfo = { onExtensionInfo(extension) },
                        onToggle = { onExtensionToggle(extension) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtensionItem(
    extension: Extension,
    onInstall: () -> Unit,
    onSettings: () -> Unit,
    onInfo: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Extension info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                TitleMedium(extension.name)
                if (extension.isInstalled && !extension.isEnabled) {
                    LabelMedium(
                        text = "Disabled",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            BodySmall(
                text = "v${extension.version} • ${extension.developer}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Extension actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            // Info button
            IconButton(
                onClick = onInfo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Extension Info",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Settings button (only if installed and has settings)
            if (extension.isInstalled && extension.hasSettings) {
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Extension Settings",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Enable/Disable switch (only if installed)
            if (extension.isInstalled) {
                Switch(
                    checked = extension.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.height(32.dp)
                )
            }
            
            // Install/Uninstall button
            IconButton(
                onClick = onInstall,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (extension.isInstalled) Icons.Outlined.Delete else Icons.Outlined.Download,
                    contentDescription = if (extension.isInstalled) "Uninstall" else "Install",
                    tint = if (extension.isInstalled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExtensionSettingsDialog(
    extension: Extension,
    onDismiss: () -> Unit,
    onSave: (List<ExtensionSetting>) -> Unit
) {
    // Sample settings for the extension
    var settings by remember {
        mutableStateOf(
            listOf(
                ExtensionSetting("quality", "Audio Quality", "High", SettingType.DROPDOWN, listOf("Low", "Medium", "High", "Lossless")),
                ExtensionSetting("autoplay", "Auto-play", "true", SettingType.SWITCH),
                ExtensionSetting("cache_size", "Cache Size (MB)", "500", SettingType.NUMBER),
                ExtensionSetting("region", "Region", "US", SettingType.DROPDOWN, listOf("US", "UK", "CA", "AU", "DE")),
                ExtensionSetting("api_key", "API Key", "", SettingType.TEXT)
            )
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMedium("${extension.name} Settings") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                items(settings) { setting ->
                    when (setting.type) {
                        SettingType.TEXT -> {
                            OutlinedTextField(
                                value = setting.value,
                                onValueChange = { newValue ->
                                    settings = settings.map { 
                                        if (it.key == setting.key) it.copy(value = newValue) else it 
                                    }
                                },
                                label = { BodyMedium(setting.label) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        SettingType.SWITCH -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyMedium(setting.label)
                                Switch(
                                    checked = setting.value.toBoolean(),
                                    onCheckedChange = { checked ->
                                        settings = settings.map { 
                                            if (it.key == setting.key) it.copy(value = checked.toString()) else it 
                                        }
                                    }
                                )
                            }
                        }
                        SettingType.DROPDOWN -> {
                            var expanded by remember { mutableStateOf(false) }
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = setting.value,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { BodyMedium(setting.label) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    setting.options.forEach { option ->
                                        DropdownMenuItem(
                                            text = { BodyMedium(option) },
                                            onClick = {
                                                settings = settings.map { 
                                                    if (it.key == setting.key) it.copy(value = option) else it 
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        SettingType.NUMBER -> {
                            OutlinedTextField(
                                value = setting.value,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        settings = settings.map { 
                                            if (it.key == setting.key) it.copy(value = newValue) else it 
                                        }
                                    }
                                },
                                label = { BodyMedium(setting.label) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(settings) }) {
                TitleMedium("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                TitleMedium("Cancel")
            }
        }
    )
}

@Composable
private fun ExtensionInfoDialog(
    extension: Extension,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMedium(extension.name) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                InfoRow("Version", extension.version)
                InfoRow("Developer", extension.developer)
                InfoRow("Status", if (extension.isInstalled) "Installed" else "Not Installed")
                
                if (extension.isInstalled) {
                    InfoRow("Enabled", if (extension.isEnabled) "Yes" else "No")
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                
                TitleMedium("Description")
                BodyMedium(
                    text = extension.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                
                TitleMedium("Permissions")
                extension.permissions.forEach { permission ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Extension,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        BodySmall(permission)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                TitleMedium("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BodyMedium(label)
        BodyMedium(
            text = value,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMedium("Add Repository") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                BodyMedium("Enter the URL of the extension repository:")
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        isValidUrl = it.isNotBlank() && (it.startsWith("https://") || it.startsWith("http://"))
                    },
                    label = { BodyMedium("Repository URL") },
                    placeholder = { BodyMedium("https://github.com/user/repo") },
                    isError = !isValidUrl && url.isNotBlank(),
                    supportingText = if (!isValidUrl && url.isNotBlank()) {
                        { BodySmall("Please enter a valid URL") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(url) },
                enabled = isValidUrl && url.isNotBlank()
            ) {
                TitleMedium("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                TitleMedium("Cancel")
            }
        }
    )
} 