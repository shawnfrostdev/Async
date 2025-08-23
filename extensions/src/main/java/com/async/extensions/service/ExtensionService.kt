package com.async.extensions.service

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionMetadata
import com.async.core.extension.ExtensionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat

/**
 * Simple extension service that provides mock data for the UI
 */
class ExtensionService(private val context: Context) {
    
    // Service initialization state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Mock installed extensions
    private val _installedExtensions = MutableStateFlow<Map<String, ExtensionInfo>>(emptyMap())
    
    // Mock repositories
    private val _repositories = MutableStateFlow<List<String>>(listOf())
    val repositories: StateFlow<List<String>> = _repositories.asStateFlow()
    
    // Mock remote extensions
    private val _remoteExtensions = MutableStateFlow<Map<String, List<RemoteExtensionInfo>>>(emptyMap())
    val remoteExtensions: StateFlow<Map<String, List<RemoteExtensionInfo>>> = _remoteExtensions.asStateFlow()
    
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
            delay(2000)
            
            // Load some mock data
            loadMockData()
            
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
            
            // Add mock extensions for this repository
            val mockExtensions = listOf(
                RemoteExtensionInfo(
                    id = "youtube.music.extension",
                    name = "YouTube Music",
                    version = "1.0.0",
                    description = "Stream music from YouTube Music",
                    downloadUrl = "$repositoryUrl/youtube.apk"
                ),
                RemoteExtensionInfo(
                    id = "spotify.extension",
                    name = "Spotify Extension",
                    version = "2.1.0", 
                    description = "Access Spotify content",
                    downloadUrl = "$repositoryUrl/spotify.apk"
                ),
                RemoteExtensionInfo(
                    id = "soundcloud.extension",
                    name = "SoundCloud",
                    version = "1.5.0",
                    description = "Stream from SoundCloud",
                    downloadUrl = "$repositoryUrl/soundcloud.apk"
                )
            )
            
            val currentRemote = _remoteExtensions.value.toMutableMap()
            currentRemote[repositoryUrl] = mockExtensions
            _remoteExtensions.value = currentRemote
            
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
                version = 1,
                name = extension.name,
                developer = "Extension Developer",
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
    
    private fun loadMockData() {
        // Load saved repositories
        val sharedPrefs = context.getSharedPreferences("extension_repos", Context.MODE_PRIVATE)
        val savedRepos = sharedPrefs.getStringSet("repositories", emptySet())?.toList() ?: emptyList()
        _repositories.value = savedRepos
        
        // Add some mock installed extensions
        val mockInstalled = mapOf(
            "demo.extension" to ExtensionInfo(
                metadata = ExtensionMetadata(
                    id = "demo.extension",
                    version = 1,
                    name = "Demo Extension",
                    developer = "Async Team",
                    description = "A demonstration extension"
                ),
                status = ExtensionStatus.INSTALLED,
                isLoaded = true
            )
        )
        _installedExtensions.value = mockInstalled
        
        logcat { "ExtensionService: Loaded ${savedRepos.size} repositories and ${mockInstalled.size} extensions" }
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
}

/**
 * Remote extension info
 */
data class RemoteExtensionInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val downloadUrl: String
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