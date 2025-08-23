package com.async.app.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.async.app.di.AppModule
import com.async.extensions.service.RemoteExtensionInfo
import com.async.extensions.service.InstallationStatus
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Extension management screen - simplified version
 */
class ExtensionManagementScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val extensionService = AppModule.getExtensionService()
        val isInitialized by extensionService.isInitialized.collectAsState()
        var selectedTab by remember { mutableStateOf(0) }
        
        // Debug logging
        LaunchedEffect(Unit) {
            logcat { "ExtensionManagementScreen: Mounting screen" }
            logcat { "ExtensionManagementScreen: isInitialized = $isInitialized" }
        }
        
        // Track initialization state changes
        LaunchedEffect(isInitialized) {
            logcat { "ExtensionManagementScreen: isInitialized changed to $isInitialized" }
        }
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Extensions") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!isInitialized) {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Initializing extension system...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    // Tab row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Installed") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Browse") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Repositories") }
                        )
                    }
                    
                    // Tab content
                    when (selectedTab) {
                        0 -> {
                            logcat { "ExtensionManagementScreen: Showing InstalledExtensionsContent" }
                            InstalledExtensionsContent()
                        }
                        1 -> {
                            logcat { "ExtensionManagementScreen: Showing BrowseExtensionsContent" }
                            BrowseExtensionsContent()
                        }
                        2 -> {
                            logcat { "ExtensionManagementScreen: Showing RepositoriesContent" }
                            RepositoriesContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledExtensionsContent() {
    val extensionService = AppModule.getExtensionService()
    val installedExtensions by extensionService.getInstalledExtensions().collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        logcat { "InstalledExtensionsContent: Loading ${installedExtensions.size} extensions" }
        installedExtensions.forEach { (id, extension) ->
            logcat { "InstalledExtensionsContent: Extension $id = ${extension.metadata.name}" }
        }
    }
    
    // Add debug logging for state changes
    LaunchedEffect(installedExtensions) {
        logcat { "InstalledExtensionsContent: State changed - ${installedExtensions.size} extensions" }
        installedExtensions.forEach { (id, extension) ->
            logcat { "InstalledExtensionsContent: Extension $id = ${extension.metadata.name}, status = ${extension.status}" }
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        logcat { "InstalledExtensionsContent: Rendering LazyColumn with ${installedExtensions.size} items" }
        
        if (installedExtensions.isEmpty()) {
            logcat { "InstalledExtensionsContent: Showing empty state" }
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Extension,
                    title = "No Extensions Installed",
                    subtitle = "Browse and install extensions to add new music sources",
                    actionText = "Browse Extensions",
                    onAction = { /* Switch to browse tab */ }
                )
            }
        } else {
            logcat { "InstalledExtensionsContent: Showing ${installedExtensions.size} extension items" }
            items(installedExtensions.values.toList()) { extension ->
                logcat { "InstalledExtensionsContent: Rendering extension ${extension.metadata.name}" }
                ExtensionCard(
                    title = extension.metadata.name,
                    subtitle = "v${extension.metadata.version} • ${extension.metadata.description}",
                    icon = Icons.Outlined.Extension,
                    isEnabled = extension.status != com.async.core.extension.ExtensionStatus.DISABLED,
                    onToggle = { enabled ->
                        scope.launch {
                            if (enabled) {
                                extensionService.enableExtension(extension.metadata.id)
                            } else {
                                extensionService.disableExtension(extension.metadata.id)
                            }
                        }
                    },
                    onUninstall = {
                        scope.launch {
                            extensionService.uninstallExtension(extension.metadata.id)
                        }
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
    val installationState by extensionService.installationState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (remoteExtensions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.CloudOff,
                    title = "No Extensions Available",
                    subtitle = "Add repositories to browse and install extensions",
                    actionText = "Add Repository",
                    onAction = { /* Switch to repositories tab */ }
                )
            }
        } else {
            remoteExtensions.forEach { (repoUrl, extensions) ->
                item {
                    Text(
                        text = "Repository: ${repoUrl.substringAfterLast('/')}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(extensions) { extension ->
                    RemoteExtensionCard(
                        title = extension.name,
                        subtitle = "v${extension.version} • ${extension.description}",
                        icon = Icons.Outlined.Download,
                        isInstalling = installationState[extension.id] == InstallationStatus.INSTALLING,
                        onInstall = {
                            scope.launch {
                                extensionService.installExtensionFromRepository(repoUrl, extension)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepositoriesContent() {
    val extensionService = AppModule.getExtensionService()
    val repositories by extensionService.repositories.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add repository card
        item {
            AddRepositoryCard(
                onAddClicked = { showAddDialog = true }
            )
        }
        
        if (repositories.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Source,
                    title = "No Repositories Added",
                    subtitle = "Add extension repositories to discover new music sources",
                    actionText = null,
                    onAction = null
                )
            }
        } else {
            items(repositories) { repoUrl ->
                RepositoryCard(
                    url = repoUrl,
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
        AddRepositoryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { url ->
                scope.launch {
                    extensionService.addRepository(url)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String?,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
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
                OutlinedButton(
                    onClick = onUninstall,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
    icon: ImageVector,
    isInstalling: Boolean,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onInstall,
                enabled = !isInstalling,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isInstalling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Installing...")
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Install")
                }
            }
        }
    }
}

@Composable
private fun AddRepositoryCard(
    onAddClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onAddClicked
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Add Repository",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add a new extension repository",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RepositoryCard(
    url: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Source,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = url.substringAfterLast('/').substringBeforeLast('.'),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repository") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        error = null
                    },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://example.com/extensions") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isBlank()) {
                        error = "URL cannot be empty"
                        return@Button
                    }
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        error = "URL must start with http:// or https://"
                        return@Button
                    }
                    onAdd(url)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 