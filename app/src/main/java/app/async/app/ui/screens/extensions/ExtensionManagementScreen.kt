package app.async.app.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import app.async.app.di.AppModule
import app.async.core.extension.ExtensionStatus
import app.async.extensions.service.InstallationStatus
import app.async.app.ui.components.AppBar
import app.async.app.navigation.AsyncScaffold
import app.async.app.ui.theme.padding
import app.async.app.ui.theme.IconSizing
import app.async.app.ui.theme.ComponentSizing
import kotlinx.coroutines.delay
import logcat.logcat

/**
 * Extension Management Screen following Mihon UI design system
 */
object ExtensionManagementScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles = listOf("Installed", "Browse", "Repositories")
        var showDropdownMenu by remember { mutableStateOf(false) }
        val extensionService = AppModule.getExtensionService()
        val updateStatus by extensionService.updateStatus.collectAsState()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        
        val updateCount = updateStatus.size

        AsyncScaffold(
            topBar = {
                AppBar(
                    title = "Extensions",
                    navigateUp = { navigator.pop() },
                    actions = {
                        // Show dropdown menu in Browse and Repositories tabs
                        if (selectedTab == 1 || selectedTab == 2) {
                            Box {
                                IconButton(onClick = { showDropdownMenu = true }) {
                                    Box {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            modifier = Modifier.size(IconSizing.default) // 24dp
                                        )
                                        if (updateCount > 0 && selectedTab == 1) {
                                            Badge(
                                                modifier = Modifier.offset(x = MaterialTheme.padding.small, y = (-MaterialTheme.padding.small))
                                            ) {
                                                Text(
                                                    text = updateCount.toString(),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = showDropdownMenu,
                                    onDismissRequest = { showDropdownMenu = false }
                                ) {
                                    // Browse tab menu items
                                    if (selectedTab == 1) {
                                        DropdownMenuItem(
                                            text = { Text("Check for Updates") },
                                            onClick = {
                                                showDropdownMenu = false
                                                scope.launch {
                                                    val updateCount = extensionService.checkForUpdates(force = true)
                                                    val message = if (updateCount > 0) {
                                                        "$updateCount Updates found"
                                                    } else {
                                                        "No Updates found"
                                                    }
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Update,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(IconSizing.default)
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Clear Extension Cache") },
                                            onClick = {
                                                showDropdownMenu = false
                                                scope.launch {
                                                    extensionService.clearExtensionCache()
                                                    snackbarHostState.showSnackbar("Extension cache cleared - extensions will reload on next use")
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(IconSizing.default)
                                                )
                                            }
                                        )
                                    }
                                    
                                    // Repositories tab menu items
                                    if (selectedTab == 2) {
                                        DropdownMenuItem(
                                            text = { Text("Refresh All Repositories") },
                                            onClick = {
                                                showDropdownMenu = false
                                                scope.launch {
                                                    extensionService.refreshAllRepositories()
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(IconSizing.default)
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sync Installed Extensions") },
                                            onClick = {
                                                showDropdownMenu = false
                                                scope.launch {
                                                    extensionService.syncInstalledExtensions()
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(IconSizing.default)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge // 14sp Medium as per Mihon guide
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> InstalledExtensionsContent(snackbarHostState)
                    1 -> BrowseExtensionsContent(snackbarHostState)
                    2 -> RepositoriesContent(snackbarHostState)
                }
            }
        }
    }
}

@Composable
private fun InstalledExtensionsContent(snackbarHostState: SnackbarHostState) {
    val extensionService = AppModule.getExtensionService()
    val installedExtensions by extensionService.getInstalledExtensions().collectAsState()
    val remoteExtensions by extensionService.remoteExtensions.collectAsState()
    val updateStatus by extensionService.updateStatus.collectAsState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.padding.medium), // 16dp as per Mihon guide
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small) // 8dp between items
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
                // Find the corresponding remote extension to get icon URL
                val remoteExtension = remoteExtensions.values.flatten()
                    .find { it.extension.id == extension.metadata.id }?.extension
                val hasUpdate = updateStatus[extension.metadata.id]?.hasUpdate == true
                val availableVersion = updateStatus[extension.metadata.id]?.availableVersion
                
                ExtensionCard(
                    title = extension.metadata.name,
                    subtitle = if (hasUpdate && availableVersion != null) {
                        "v${extension.metadata.versionString ?: extension.metadata.version} → v$availableVersion"
                    } else {
                        "v${extension.metadata.versionString ?: extension.metadata.version}"
                    },
                    icon = Icons.Default.Extension,
                    iconUrl = remoteExtension?.iconUrl,
                    packageName = extension.metadata.id,
                    hasUpdate = hasUpdate,
                    isEnabled = extension.status == ExtensionStatus.INSTALLED,
                    onToggle = { enabled ->
                        logcat { "Toggle extension ${extension.metadata.name}: $enabled" }
                        scope.launch {
                            if (enabled) {
                                extensionService.enableExtension(extension.metadata.id)
                            } else {
                                extensionService.disableExtension(extension.metadata.id)
                            }
                        }
                    },
                    onUninstall = {
                        logcat { "Uninstall extension ${extension.metadata.name}" }
                        scope.launch {
                            extensionService.uninstallExtension(extension.metadata.id)
                            // Trigger a sync to refresh the UI
                            extensionService.syncInstalledExtensions()
                        }
                    },
                    onUpdate = if (hasUpdate) {
                        {
                            scope.launch {
                                extensionService.updateExtension(extension.metadata.id)
                                // Trigger a sync after update to refresh the UI
                                delay(2000)
                                extensionService.syncInstalledExtensions()
                            }
                        }
                    } else null,
                    onForceReload = {
                        scope.launch {
                            extensionService.forceReloadExtension(extension.metadata.id)
                            snackbarHostState.showSnackbar("Extension ${extension.metadata.name} force reloaded")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BrowseExtensionsContent(snackbarHostState: SnackbarHostState) {
    val extensionService = AppModule.getExtensionService()
    val remoteExtensions by extensionService.remoteExtensions.collectAsState()
    val installationStates by extensionService.installationState.collectAsState()
    val installedExtensions by extensionService.getInstalledExtensions().collectAsState()
    val downloadedApks by extensionService.downloadedApks.collectAsState()
    val updateStatus by extensionService.updateStatus.collectAsState()
    val scope = rememberCoroutineScope()
    
    val updateCount = updateStatus.size
    
    // Trigger sync when entering this screen to ensure accurate installation states
    LaunchedEffect(Unit) {
        extensionService.syncInstalledExtensions()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Update All button
        if (updateCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.padding.medium,
                        vertical = MaterialTheme.padding.small
                    ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            extensionService.updateAllExtensions()
                        }
                    },
                    modifier = Modifier.heightIn(min = ComponentSizing.Button.standard) // 40dp height
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.extraSmall) // 16dp for button icon
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.padding.extraSmall))
                    Text(
                        text = "Update All",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.padding.extraSmall))
                    Badge {
                        Text(
                            text = updateCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.padding.medium), // 16dp as per Mihon guide
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small) // 8dp between items
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
                                style = MaterialTheme.typography.titleMedium, // 16sp Medium as per Mihon guide
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = MaterialTheme.padding.small)
                            )
                        }
                        
                        // Extensions in this repository
                        items(extensions) { extensionWithRepo ->
                            val installationStatus = installationStates[extensionWithRepo.extension.id]
                            val isInstalled = installedExtensions.containsKey(extensionWithRepo.extension.id)
                            val hasDownloadedApk = downloadedApks[extensionWithRepo.extension.id]?.exists() == true
                            val hasUpdate = updateStatus[extensionWithRepo.extension.id]?.hasUpdate == true
                            
                            RemoteExtensionCard(
                                title = extensionWithRepo.extension.name,
                                subtitle = "v${extensionWithRepo.extension.version}",
                                developer = extensionWithRepo.extension.developer,
                                iconUrl = extensionWithRepo.extension.iconUrl,
                                installationStatus = installationStatus,
                                isInstalled = isInstalled,
                                hasDownloadedApk = hasDownloadedApk,
                                hasUpdate = hasUpdate,
                                onDownloadClick = { 
                                    logcat { "Download clicked for ${extensionWithRepo.extension.name}" }
                                    scope.launch {
                                        try {
                                            extensionService.downloadExtension(repoUrl, extensionWithRepo.extension)
                                            logcat { "Successfully started download for ${extensionWithRepo.extension.name}" }
                                        } catch (e: Exception) {
                                            logcat { "Failed to download ${extensionWithRepo.extension.name}: ${e.message}" }
                                        }
                                    }
                                },
                                onInstallClick = {
                                    logcat { "Install clicked for ${extensionWithRepo.extension.name}" }
                                    scope.launch {
                                        try {
                                            extensionService.installDownloadedExtension(extensionWithRepo.extension.id)
                                            logcat { "Successfully started installation for ${extensionWithRepo.extension.name}" }
                                        } catch (e: Exception) {
                                            logcat { "Failed to install ${extensionWithRepo.extension.name}: ${e.message}" }
                                        }
                                    }
                                },
                                onUninstallClick = {
                                    scope.launch {
                                        extensionService.uninstallExtension(extensionWithRepo.extension.id)
                                        logcat { "Uninstalled ${extensionWithRepo.extension.name}" }
                                    }
                                },
                                onUpdateClick = {
                                    logcat { "Update clicked for ${extensionWithRepo.extension.name}" }
                                    scope.launch {
                                        try {
                                            extensionService.updateExtension(extensionWithRepo.extension.id)
                                            logcat { "Successfully started update for ${extensionWithRepo.extension.name}" }
                                        } catch (e: Exception) {
                                            logcat { "Failed to update ${extensionWithRepo.extension.name}: ${e.message}" }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoriesContent(snackbarHostState: SnackbarHostState) {
    val extensionService = AppModule.getExtensionService()
    val repositories by extensionService.repositories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newRepoUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.padding.medium), // 16dp as per Mihon guide
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small) // 8dp between items
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
                        .padding(MaterialTheme.padding.medium), // 16dp internal padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.default), // 24dp
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
                    Text(
                        text = "Add Repository",
                        style = MaterialTheme.typography.bodyLarge, // 16sp like other buttons
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
            title = { 
                Text(
                    text = "Add Repository",
                    style = MaterialTheme.typography.headlineSmall // 24sp as per Mihon guide
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the repository URL:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.padding.small))
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
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newRepoUrl = ""
                }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
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
    iconUrl: String? = null,
    packageName: String? = null,
    hasUpdate: Boolean = false,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onUninstall: () -> Unit,
    onUpdate: (() -> Unit)? = null,
    onForceReload: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.padding.medium)) { // 16dp internal padding
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Extension icon
                if (!iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.default) // 24dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.default), // 24dp
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
                
                // Extension name and version
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge, // 16sp like other items
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium, // 14sp
                        color = if (hasUpdate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasUpdate) {
                        Text(
                            text = "Update available",
                            style = MaterialTheme.typography.bodySmall, // 12sp
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Update button (if update available)
                if (hasUpdate && onUpdate != null) {
                    IconButton(
                        onClick = onUpdate,
                        modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Update,
                            contentDescription = "Update",
                            modifier = Modifier.size(IconSizing.medium) // 20dp
                        )
                    }
                }
                
                // Force reload button (debug option)
                if (onForceReload != null) {
                    IconButton(
                        onClick = onForceReload,
                        modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Force Reload",
                            modifier = Modifier.size(IconSizing.medium) // 20dp
                        )
                    }
                }
                
                // Uninstall icon button
                IconButton(
                    onClick = onUninstall,
                    modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Uninstall",
                        modifier = Modifier.size(IconSizing.medium) // 20dp
                    )
                }
                
                // Enable/disable toggle
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun RemoteExtensionCard(
    title: String,
    subtitle: String,
    developer: String,
    iconUrl: String? = null,
    installationStatus: InstallationStatus? = null,
    isInstalled: Boolean = false,
    hasDownloadedApk: Boolean = false,
    hasUpdate: Boolean = false,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit = {},
    onUninstallClick: () -> Unit = {},
    onUpdateClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.padding.medium)) { // 16dp internal padding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Extension icon
                if (!iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.default) // 24dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizing.default), // 24dp
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
                
                // Extension info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge, // 16sp like other items
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium, // 14sp
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "by $developer",
                        style = MaterialTheme.typography.bodySmall, // 12sp
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action button/icon
                when {
                    isInstalled && hasUpdate -> {
                        IconButton(
                            onClick = onUpdateClick,
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Update,
                                contentDescription = "Update",
                                modifier = Modifier.size(IconSizing.medium) // 20dp
                            )
                        }
                    }
                    isInstalled -> {
                        IconButton(
                            onClick = onUninstallClick,
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Uninstall",
                                modifier = Modifier.size(IconSizing.medium) // 20dp
                            )
                        }
                    }
                    installationStatus == InstallationStatus.DOWNLOADING -> {
                        Box(
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(IconSizing.medium), // 20dp
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    installationStatus == InstallationStatus.DOWNLOADED || hasDownloadedApk -> {
                        IconButton(
                            onClick = onInstallClick,
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.InstallMobile,
                                contentDescription = "Install",
                                modifier = Modifier.size(IconSizing.medium) // 20dp
                            )
                        }
                    }
                    installationStatus == InstallationStatus.INSTALLING -> {
                        Box(
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(IconSizing.medium), // 20dp
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    installationStatus == InstallationStatus.COMPLETED -> {
                        IconButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.size(ComponentSizing.Button.large), // 48dp touch target
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Installed",
                                modifier = Modifier.size(IconSizing.medium) // 20dp
                            )
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(ComponentSizing.Button.large) // 48dp touch target
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(IconSizing.medium) // 20dp
                            )
                        }
                    }
                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.padding.medium), // 16dp internal padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(IconSizing.default), // 24dp
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repositoryName,
                    style = MaterialTheme.typography.bodyLarge, // 16sp like other items
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = repositoryUrl,
                    style = MaterialTheme.typography.bodySmall, // 12sp
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(ComponentSizing.Button.large) // 48dp touch target
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove repository",
                    modifier = Modifier.size(IconSizing.default), // 24dp
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
                .padding(MaterialTheme.padding.large), // 24dp for empty state
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(IconSizing.huge), // 48dp for empty state
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MaterialTheme.padding.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium, // 16sp Medium
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MaterialTheme.padding.small))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium, // 14sp
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
