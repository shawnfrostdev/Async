package com.async.app.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.async.app.di.AppModule
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Extension Management Screen with tabs for Installed, Browse, and Repositories
 */
class ExtensionManagementScreen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles = listOf("Installed", "Browse", "Repositories")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Extensions") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> InstalledExtensionsContent()
                    1 -> BrowseExtensionsContent()
                    2 -> RepositoriesContent()
                }
            }
        }
    }
}

@Composable
private fun InstalledExtensionsContent() {
    val extensionService = AppModule.getExtensionService()
    val installedExtensions by extensionService.getInstalledExtensions().collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (installedExtensions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Extensions Installed",
                    description = "Install extensions from repositories to get started",
                    icon = Icons.Default.Extension
                )
            }
        } else {
            items(installedExtensions.values.toList()) { extension ->
                ExtensionCard(
                    title = extension.metadata.name,
                    subtitle = "v${extension.metadata.version} • ${extension.metadata.description}",
                    icon = Icons.Default.Extension,
                    isEnabled = true, // Simplified for now
                    onToggle = { enabled ->
                        logcat { "Toggle extension ${extension.metadata.name}: $enabled" }
                    },
                    onUninstall = {
                        logcat { "Uninstall extension ${extension.metadata.name}" }
                    }
                )
            }
        }
    }
}

@Composable
private fun BrowseExtensionsContent() {
    val extensionService = AppModule.getExtensionService()
    val remoteExtensions by extensionService.remoteExtensions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (remoteExtensions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Extensions Available",
                    description = "Add a repository to browse extensions",
                    icon = Icons.Default.Extension
                )
            }
        } else {
            remoteExtensions.forEach { (repoUrl, extensions) ->
                if (extensions.isNotEmpty()) {
                    val repoName = extensions.first().repositoryName
                    logcat { "BrowseExtensionsContent: Repository $repoUrl -> displaying name: '$repoName'" }
                    
                    // Repository header
                    item {
                        Text(
                            text = repoName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Extensions in this repository
                    items(extensions) { extensionWithRepo ->
                        RemoteExtensionCard(
                            title = extensionWithRepo.extension.name,
                            subtitle = "v${extensionWithRepo.extension.version} • ${extensionWithRepo.extension.description}",
                            developer = extensionWithRepo.extension.developer,
                            onDownloadClick = { 
                                logcat { "Download clicked for ${extensionWithRepo.extension.name}" }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoriesContent() {
    val extensionService = AppModule.getExtensionService()
    val repositories by extensionService.repositories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newRepoUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = { showAddDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add Repository",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                onClick = { 
                    scope.launch {
                        extensionService.refreshAllRepositories()
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Refresh All Repositories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (repositories.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Repositories",
                    description = "Add extension repositories to browse and install extensions",
                    icon = Icons.Default.CloudOff
                )
            }
        } else {
            items(repositories) { repoUrl ->
                RepositoryCard(
                    repositoryUrl = repoUrl,
                    repositoryName = extractRepoName(repoUrl),
                    onRemove = {
                        scope.launch {
                            extensionService.removeRepository(repoUrl)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newRepoUrl = ""
            },
            title = { Text("Add Repository") },
            text = {
                Column {
                    Text(
                        text = "Enter the repository URL:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newRepoUrl,
                        onValueChange = { newRepoUrl = it },
                        label = { Text("Repository URL") },
                        placeholder = { Text("https://raw.githubusercontent.com/user/repo/main/manifest.json") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newRepoUrl.isNotBlank()) {
                            scope.launch {
                                extensionService.addRepository(newRepoUrl.trim())
                                showAddDialog = false
                                newRepoUrl = ""
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newRepoUrl = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExtensionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onUninstall: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onUninstall) {
                    Text("Uninstall")
                }
            }
        }
    }
}

@Composable
private fun RemoteExtensionCard(
    title: String,
    subtitle: String,
    developer: String,
    onDownloadClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Extension,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "by $developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onDownloadClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download")
            }
        }
    }
}

@Composable
private fun RepositoryCard(
    repositoryUrl: String,
    repositoryName: String,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repositoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = repositoryUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove repository",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper function to extract repo name from GitHub URL
private fun extractRepoName(repositoryUrl: String): String {
    return try {
        val parts = repositoryUrl.split("/")
        if (parts.any { it.contains("github") }) {
            val githubIndex = parts.indexOfFirst { it.contains("github") }
            val userIndex = githubIndex + 1
            if (userIndex < parts.size && userIndex + 1 < parts.size) {
                val user = parts[userIndex]
                val repo = parts[userIndex + 1]
                return "$user/$repo"
            }
        }
        repositoryUrl.substringAfterLast("/").substringBefore(".")
    } catch (e: Exception) {
        repositoryUrl.substringAfterLast("/").substringBefore(".")
    }
} 