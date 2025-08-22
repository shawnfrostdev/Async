package com.example.async.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.BodySmall
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
    val description: String,
    val isInstalled: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    var repositories by remember { 
        mutableStateOf(
            listOf(
                Repository(
                    url = "https://github.com/example/music-extensions",
                    name = "Music Extensions",
                    extensions = listOf(
                        Extension("Spotify Extension", "1.0.0", "Connect to Spotify for streaming"),
                        Extension("YouTube Music", "2.1.0", "Access YouTube Music library"),
                        Extension("SoundCloud", "1.5.0", "Stream from SoundCloud")
                    )
                ),
                Repository(
                    url = "https://github.com/community/async-plugins",
                    name = "Community Plugins",
                    extensions = listOf(
                        Extension("Last.fm Scrobbler", "1.2.0", "Scrobble tracks to Last.fm"),
                        Extension("Lyrics Provider", "1.0.1", "Fetch lyrics for current track")
                    )
                )
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header with back button and add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.l),
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
            TitleMedium(
                text = "Extensions",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddRepositoryDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Repository"
                )
            }
        }
        
        if (repositories.isEmpty()) {
            // Show empty state for first-time users
            EmptyRepositoryState(
                modifier = Modifier.fillMaxWidth(),
                onAddRepository = { showAddRepositoryDialog = true }
            )
        } else {
            // Show repository list with extensions
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                items(repositories) { repository ->
                    RepositoryItem(
                        repository = repository,
                        onToggleExpanded = { url ->
                            repositories = repositories.map { repo ->
                                if (repo.url == url) {
                                    repo.copy(isExpanded = !repo.isExpanded)
                                } else {
                                    repo
                                }
                            }
                        },
                        onInstallExtension = { extensionName ->
                            // Future: Handle extension installation
                        },
                        onUninstallExtension = { extensionName ->
                            // Future: Handle extension uninstallation
                        }
                    )
                }
            }
        }
    }
    
    // Add Repository Dialog
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepositoryDialog = false },
            onConfirm = { url ->
                // Add new repository to the list
                val newRepository = Repository(
                    url = url,
                    extensions = listOf(
                        Extension("Sample Extension", "1.0.0", "Example extension from $url")
                    )
                )
                repositories = repositories + newRepository
                showAddRepositoryDialog = false
            }
        )
    }
}

@Composable
private fun RepositoryItem(
    repository: Repository,
    onToggleExpanded: (String) -> Unit,
    onInstallExtension: (String) -> Unit,
    onUninstallExtension: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Repository header with toggle
            Surface(
                onClick = { onToggleExpanded(repository.url) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        TitleMedium(
                            text = repository.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BodySmall(
                            text = repository.url,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        LabelMedium(
                            text = "${repository.extensions.size} extensions",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Icon(
                        imageVector = if (repository.isExpanded) {
                            Icons.Outlined.ExpandLess
                        } else {
                            Icons.Outlined.ExpandMore
                        },
                        contentDescription = if (repository.isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Extensions list (shown when expanded)
            if (repository.isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = AppSpacing.m,
                        end = AppSpacing.m,
                        bottom = AppSpacing.m
                    )
                ) {
                    repository.extensions.forEach { extension ->
                        ExtensionItem(
                            extension = extension,
                            onInstall = { onInstallExtension(extension.name) },
                            onUninstall = { onUninstallExtension(extension.name) },
                            modifier = Modifier.padding(vertical = AppSpacing.xs)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtensionItem(
    extension: Extension,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            // Extension icon
            Icon(
                imageVector = Icons.Outlined.Extension,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            // Extension info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TitleMedium(
                    text = extension.name,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BodySmall(
                    text = "v${extension.version}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BodySmall(
                    text = extension.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // Install/Uninstall button
            if (extension.isInstalled) {
                OutlinedButton(
                    onClick = onUninstall,
                    contentPadding = AppSpacing.smallButtonPadding
                ) {
                    LabelMedium("Uninstall")
                }
            } else {
                Button(
                    onClick = onInstall,
                    contentPadding = AppSpacing.smallButtonPadding
                ) {
                    LabelMedium("Install")
                }
            }
        }
    }
}

@Composable
private fun EmptyRepositoryState(
    modifier: Modifier = Modifier,
    onAddRepository: () -> Unit
) {
    Column(
        modifier = modifier.padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleMedium(
            text = "No Extensions Yet",
            modifier = Modifier.padding(bottom = AppSpacing.m)
        )
        
        BodyMedium(
            text = "Add extension repositories to discover music sources and enhance your listening experience.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AppSpacing.xl)
        )
    }
}

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var isUrlValid by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMedium("Add Repository") },
        text = {
            Column {
                BodyMedium(
                    text = "Enter the URL of the extension repository:",
                    modifier = Modifier.padding(bottom = AppSpacing.m)
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        isUrlValid = isValidUrl(it)
                    },
                    label = { LabelMedium("Repository URL") },
                    placeholder = { BodyMedium("https://github.com/user/repo") },
                    isError = !isUrlValid && url.isNotEmpty(),
                    supportingText = if (!isUrlValid && url.isNotEmpty()) {
                        { BodySmall("Please enter a valid URL") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url) },
                enabled = url.isNotEmpty() && isUrlValid
            ) {
                LabelMedium("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                LabelMedium("Cancel")
            }
        }
    )
}

private fun isValidUrl(url: String): Boolean {
    if (url.isEmpty()) return true
    return try {
        val pattern = Regex("^https?://.*")
        pattern.matches(url)
    } catch (e: Exception) {
        false
    }
} 