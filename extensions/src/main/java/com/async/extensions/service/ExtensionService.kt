package com.async.extensions.service

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionMetadata
import com.async.core.extension.ExtensionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logcat.logcat
import java.net.URL

/**
 * Extension service that fetches real data from repositories
 */
class ExtensionService(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    // Service initialization state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Real installed extensions (empty by default)
    private val _installedExtensions = MutableStateFlow<Map<String, ExtensionInfo>>(emptyMap())
    
    // Real repositories
    private val _repositories = MutableStateFlow<List<String>>(listOf())
    val repositories: StateFlow<List<String>> = _repositories.asStateFlow()
    
    // Remote extensions from repositories
    private val _remoteExtensions = MutableStateFlow<Map<String, List<RemoteExtensionWithRepo>>>(emptyMap())
    val remoteExtensions: StateFlow<Map<String, List<RemoteExtensionWithRepo>>> = _remoteExtensions
    
    // Installation states
    private val _installationState = MutableStateFlow<Map<String, InstallationStatus>>(emptyMap())
    val installationState: StateFlow<Map<String, InstallationStatus>> = _installationState.asStateFlow()
    
    /**
     * Initialize the extension service
     */
    suspend fun initialize() {
        if (_isInitialized.value) return
        
        try {
            logcat { "ExtensionService: Starting initialization..." }
            
            // Simulate initialization delay
            delay(1000)
            
            // Load saved repositories and extensions
            loadSavedData()
            
            _isInitialized.value = true
            logcat { "ExtensionService: Initialization completed" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Initialization failed: ${e.message}" }
        }
    }
    
    /**
     * Get installed extensions
     */
    fun getInstalledExtensions(): StateFlow<Map<String, ExtensionInfo>> {
        return _installedExtensions.asStateFlow()
    }
    
    /**
     * Enable an extension
     */
    suspend fun enableExtension(extensionId: String) {
        logcat { "ExtensionService: Enabling extension $extensionId" }
        val current = _installedExtensions.value.toMutableMap()
        current[extensionId]?.let { info ->
            current[extensionId] = info.copy(status = ExtensionStatus.INSTALLED)
        }
        _installedExtensions.value = current
    }
    
    /**
     * Disable an extension
     */
    suspend fun disableExtension(extensionId: String) {
        logcat { "ExtensionService: Disabling extension $extensionId" }
        val current = _installedExtensions.value.toMutableMap()
        current[extensionId]?.let { info ->
            current[extensionId] = info.copy(status = ExtensionStatus.DISABLED)
        }
        _installedExtensions.value = current
    }
    
    /**
     * Uninstall an extension
     */
    suspend fun uninstallExtension(extensionId: String) {
        logcat { "ExtensionService: Uninstalling extension $extensionId" }
        val current = _installedExtensions.value.toMutableMap()
        current.remove(extensionId)
        _installedExtensions.value = current
    }
    
    /**
     * Add a repository
     */
    suspend fun addRepository(repositoryUrl: String) {
        logcat { "ExtensionService: Adding repository $repositoryUrl" }
        
        val currentRepos = _repositories.value.toMutableList()
        if (!currentRepos.contains(repositoryUrl)) {
            currentRepos.add(repositoryUrl)
            _repositories.value = currentRepos
            
            // Fetch real extensions from this repository
            fetchExtensionsFromRepository(repositoryUrl)
            
            // Save to preferences
            saveRepositories(currentRepos)
        }
    }
    
    /**
     * Remove a repository
     */
    suspend fun removeRepository(repositoryUrl: String) {
        logcat { "ExtensionService: Removing repository $repositoryUrl" }
        
        val currentRepos = _repositories.value.toMutableList()
        currentRepos.remove(repositoryUrl)
        _repositories.value = currentRepos
        
        // Clear the remote extensions for this repository
        val currentRemote = _remoteExtensions.value.toMutableMap()
        currentRemote.remove(repositoryUrl)
        _remoteExtensions.value = currentRemote
        
        saveRepositories(currentRepos)
    }
    
    /**
     * Install extension from repository
     */
    suspend fun installExtensionFromRepository(repositoryUrl: String, extension: RemoteExtensionInfo) {
        logcat { "ExtensionService: Installing ${extension.name}" }
        
        // Update installation state
        updateInstallationStatus(extension.id, InstallationStatus.INSTALLING)
        
        // Simulate installation
        delay(3000)
        
        // Create extension info
        val extensionInfo = ExtensionInfo(
            metadata = ExtensionMetadata(
                id = extension.id,
                version = extension.version.toIntOrNull() ?: 1,
                name = extension.name,
                developer = extension.developer, // Changed from extension.developer to extension.author
                description = extension.description
            ),
            status = ExtensionStatus.INSTALLED,
            isLoaded = true
        )
        
        // Add to installed extensions
        val current = _installedExtensions.value.toMutableMap()
        current[extension.id] = extensionInfo
        _installedExtensions.value = current
        
        updateInstallationStatus(extension.id, InstallationStatus.COMPLETED)
        
        logcat { "ExtensionService: Successfully installed ${extension.name}" }
    }
    
    /**
     * Fetch extensions from a repository URL
     */
    private suspend fun fetchExtensionsFromRepository(repositoryUrl: String) {
        try {
            logcat { "ExtensionService: Fetching extensions from $repositoryUrl" }
            
            val manifest = withContext(Dispatchers.IO) {
                val manifestUrl = if (repositoryUrl.endsWith(".json")) {
                    repositoryUrl
                } else {
                    "$repositoryUrl/manifest.json"
                }
                
                logcat { "ExtensionService: Final manifest URL: $manifestUrl" }
                
                try {
                    val content = URL(manifestUrl).readText()
                    logcat { "ExtensionService: Successfully downloaded content: ${content.take(200)}..." }
                    
                    // Fix version field if it's a number instead of string
                    val fixedContent = content.replace(
                        Regex("\"version\":\\s*(\\d+)"), 
                        "\"version\":\"$1\""
                    )
                    
                    val parsedManifest = json.decodeFromString<RepositoryManifest>(fixedContent)
                    logcat { "ExtensionService: Successfully parsed manifest with ${parsedManifest.extensions.size} extensions" }
                    
                    parsedManifest
                } catch (e: Exception) {
                    logcat { "ExtensionService: Error during download/parsing: ${e.message}" }
                    logcat { "ExtensionService: Full exception: $e" }
                    throw e
                }
            }
            
            val currentRemote = _remoteExtensions.value.toMutableMap()
            currentRemote[repositoryUrl] = manifest.extensions.map { ext ->
                val repoName = extractRepoName(repositoryUrl)
                logcat { "ExtensionService: Creating RemoteExtensionWithRepo for ${ext.name} with repo name: $repoName" }
                RemoteExtensionWithRepo(
                    extension = ext,
                    repositoryName = repoName,
                    repositoryUrl = repositoryUrl,
                    manifestInfo = manifest
                )
            }
            _remoteExtensions.value = currentRemote
            
            logcat { "ExtensionService: Successfully fetched ${manifest.extensions.size} extensions from $repositoryUrl" }
            manifest.extensions.forEach { ext ->
                logcat { "ExtensionService: Extension: ${ext.name} (${ext.id}) v${ext.version}" }
                logcat { "ExtensionService: - Developer: ${ext.developer}" }
                logcat { "ExtensionService: - Description: ${ext.description}" }
                logcat { "ExtensionService: - DownloadPath: ${ext.downloadPath}" }
                logcat { "ExtensionService: - IconUrl: ${ext.iconUrl}" }
                logcat { "ExtensionService: - SourceUrl: ${ext.sourceUrl}" }
                logcat { "ExtensionService: - Features: ${ext.features?.size ?: 0} features" }
                logcat { "ExtensionService: - Permissions: ${ext.permissions?.joinToString(", ") ?: "none"}" }
            }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to fetch extensions from $repositoryUrl: ${e.message}" }
            logcat { "ExtensionService: Exception type: ${e.javaClass.simpleName}" }
            e.printStackTrace()
        }
    }
    
    /**
     * Force refresh all repositories (useful after fixing repository name extraction)
     */
    suspend fun refreshAllRepositories() {
        logcat { "ExtensionService: Force refreshing all repositories" }
        
        // Clear all remote extensions
        _remoteExtensions.value = emptyMap()
        
        // Re-fetch all repositories
        val currentRepos = _repositories.value
        currentRepos.forEach { repoUrl ->
            fetchExtensionsFromRepository(repoUrl)
        }
    }
    
    private fun loadSavedData() {
        // Load saved repositories
        val sharedPrefs = context.getSharedPreferences("extension_repos", Context.MODE_PRIVATE)
        val savedRepos = sharedPrefs.getStringSet("repositories", emptySet())?.toList() ?: emptyList()
        _repositories.value = savedRepos
        
        // No more mock installed extensions - start with empty
        _installedExtensions.value = emptyMap()
        
        logcat { "ExtensionService: Loaded ${savedRepos.size} repositories" }
        
        // Refresh extensions from saved repositories in the background
        if (savedRepos.isNotEmpty()) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
                savedRepos.forEach { repoUrl ->
                    fetchExtensionsFromRepository(repoUrl)
                }
            }
        }
    }
    
    private fun saveRepositories(repositories: List<String>) {
        val sharedPrefs = context.getSharedPreferences("extension_repos", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putStringSet("repositories", repositories.toSet())
            .apply()
    }
    
    private fun updateInstallationStatus(extensionId: String, status: InstallationStatus) {
        val current = _installationState.value.toMutableMap()
        current[extensionId] = status
        _installationState.value = current
    }

    /**
     * Extract repository name from GitHub URL
     */
    private fun extractRepoName(repositoryUrl: String): String {
        return try {
            logcat { "ExtensionService: Extracting repo name from: $repositoryUrl" }
            // Handle GitHub URLs like:
            // https://raw.githubusercontent.com/shawnfrostdev/Async-Extensions/main/async-extension/manifest.json
            // https://github.com/shawnfrostdev/Async-Extensions
            val parts = repositoryUrl.split("/")
            logcat { "ExtensionService: URL parts: ${parts.joinToString(", ")}" }
            
            // Debug each part
            parts.forEachIndexed { index, part ->
                val containsGithub = part.contains("github")
                logcat { "ExtensionService: Part[$index] = '$part', contains github: $containsGithub" }
            }
            
            val hasGithub = parts.any { it.contains("github") }
            logcat { "ExtensionService: Has GitHub: $hasGithub" }
            
            if (hasGithub) {
                val githubIndex = parts.indexOfFirst { it.contains("github") }
                val userIndex = githubIndex + 1
                logcat { "ExtensionService: GitHub index: $githubIndex, User index: $userIndex, parts size: ${parts.size}" }
                
                if (userIndex < parts.size && userIndex + 1 < parts.size) {
                    val user = parts[userIndex]
                    val repo = parts[userIndex + 1]
                    val repoName = "$user/$repo"
                    logcat { "ExtensionService: Extracted repo name: $repoName" }
                    return repoName
                }
            }
            // Fallback to last part of URL without extension
            val fallback = repositoryUrl.substringAfterLast("/").substringBefore(".")
            logcat { "ExtensionService: Using fallback name: $fallback" }
            fallback
        } catch (e: Exception) {
            val fallback = repositoryUrl.substringAfterLast("/").substringBefore(".")
            logcat { "ExtensionService: Exception occurred, using fallback: $fallback" }
            fallback
        }
    }
}

/**
 * Repository manifest structure
 */
@Serializable
data class RepositoryManifest(
    val name: String,
    val version: String,
    val extensions: List<RemoteExtensionInfo>
)

/**
 * Remote extension information from repository
 */
@Serializable
data class RemoteExtensionInfo(
    val id: String,
    val name: String,
    val version: String,
    val developer: String,
    val description: String,
    val downloadPath: String? = null,
    val iconUrl: String? = null,
    val sourceUrl: String? = null,
    val permissions: List<String>? = null,
    val minAppVersion: Int? = null,
    val features: List<String>? = null
)

/**
 * Remote extension information with repository context
 */
data class RemoteExtensionWithRepo(
    val extension: RemoteExtensionInfo,
    val repositoryName: String,
    val repositoryUrl: String,
    val manifestInfo: RepositoryManifest
)

/**
 * Installation status
 */
enum class InstallationStatus {
    IDLE,
    INSTALLING,
    COMPLETED,
    ERROR
} 