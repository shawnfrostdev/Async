package com.async.extensions.service

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.core.model.ExtensionException
import com.async.extensions.installer.ExtensionInstaller
import com.async.extensions.installer.ValidationInfo
import com.async.extensions.manager.ExtensionManager
import com.async.extensions.repository.ExtensionRepository
import com.async.extensions.repository.ExtensionUpdate
import com.async.extensions.repository.RemoteExtensionInfo
import com.async.extensions.repository.RepositoryManifest
import com.async.extensions.runtime.ExtensionRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import logcat.logcat
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Installation status for extensions
 */
enum class InstallationStatus {
    IDLE,
    INSTALLING,
    COMPLETED,
    ERROR
}

/**
 * High-level service that provides all extension functionality for UI integration
 */
@Singleton
class ExtensionService @Inject constructor(
    private val context: Context,
    private val extensionRuntime: ExtensionRuntime,
    private val extensionManager: ExtensionManager,
    private val extensionRepository: ExtensionRepository,
    private val extensionInstaller: ExtensionInstaller
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Service state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Repository management
    private val _repositories = MutableStateFlow<List<String>>(emptyList())
    val repositories: StateFlow<List<String>> = _repositories.asStateFlow()
    
    // Available extensions from repositories
    private val _remoteExtensions = MutableStateFlow<Map<String, List<RemoteExtensionInfo>>>(emptyMap())
    val remoteExtensions: StateFlow<Map<String, List<RemoteExtensionInfo>>> = _remoteExtensions.asStateFlow()
    
    // Installation state
    private val _installationState = MutableStateFlow<Map<String, InstallationStatus>>(emptyMap())
    val installationState: StateFlow<Map<String, InstallationStatus>> = _installationState.asStateFlow()
    
    /**
     * Initialize the extension service
     */
    suspend fun initialize() {
        if (_isInitialized.value) return
        
        try {
            logcat { "ExtensionService: Starting initialization..." }
            
            // Simulate initialization time to see loading state
            delay(2000)
            
            // Initialize extension runtime
            extensionRuntime.initialize()
            
            // Load saved repositories
            loadSavedRepositories()
            
            _isInitialized.value = true
            logcat { "ExtensionService: Initialization completed successfully" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Initialization failed: ${e.message}" }
            e.printStackTrace()
        }
    }
    
    /**
     * Get all installed extensions
     */
    fun getInstalledExtensions(): StateFlow<Map<String, ExtensionInfo>> {
        return extensionRuntime.availableExtensions
    }
    
    /**
     * Get active extension IDs
     */
    fun getActiveExtensionIds(): List<String> {
        return extensionRuntime.getActiveExtensionIds()
    }
    
    /**
     * Enable an extension
     */
    suspend fun enableExtension(extensionId: String): ExtensionResult<Unit> {
        logcat { "ExtensionService: Enabling extension $extensionId" }
        return extensionRuntime.enableExtension(extensionId)
    }
    
    /**
     * Disable an extension
     */
    suspend fun disableExtension(extensionId: String): ExtensionResult<Unit> {
        logcat { "ExtensionService: Disabling extension $extensionId" }
        return extensionRuntime.disableExtension(extensionId)
    }
    
    /**
     * Uninstall an extension
     */
    suspend fun uninstallExtension(extensionId: String): ExtensionResult<Unit> {
        logcat { "ExtensionService: Uninstalling extension $extensionId" }
        return try {
            extensionManager.uninstallExtension(extensionId)
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to uninstall extension $extensionId: ${e.message}" }
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "UNINSTALL_FAILED"
                )
            )
        }
    }
    
    /**
     * Add a repository
     */
    suspend fun addRepository(repositoryUrl: String): ExtensionResult<Unit> {
        return try {
            logcat { "ExtensionService: Adding repository $repositoryUrl" }
            
            val currentRepositories = _repositories.value.toMutableList()
            if (!currentRepositories.contains(repositoryUrl)) {
                currentRepositories.add(repositoryUrl)
                _repositories.value = currentRepositories
                
                // Save to persistent storage
                saveRepositories(currentRepositories)
                
                // Refresh remote extensions
                refreshRemoteExtensions()
            }
            
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to add repository: ${e.message}" }
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "ADD_REPOSITORY_FAILED"
                )
            )
        }
    }
    
    /**
     * Remove a repository
     */
    suspend fun removeRepository(repositoryUrl: String): ExtensionResult<Unit> {
        return try {
            logcat { "ExtensionService: Removing repository $repositoryUrl" }
            
            val currentRepositories = _repositories.value.toMutableList()
            currentRepositories.remove(repositoryUrl)
            _repositories.value = currentRepositories
            
            // Save to persistent storage
            saveRepositories(currentRepositories)
            
            // Refresh remote extensions
            refreshRemoteExtensions()
            
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to remove repository: ${e.message}" }
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "REMOVE_REPOSITORY_FAILED"
                )
            )
        }
    }
    
    /**
     * Install extension from repository
     */
    suspend fun installExtensionFromRepository(
        repositoryUrl: String, 
        extension: RemoteExtensionInfo
    ): ExtensionResult<ExtensionInfo> {
        return try {
            logcat { "ExtensionService: Installing extension ${extension.id} from repository" }
            
            updateInstallationStatus(extension.id, InstallationStatus.INSTALLING)
            
            // Simulate installation process
            delay(3000)
            
            // Here you would implement actual installation logic
            // For now, just simulate success
            updateInstallationStatus(extension.id, InstallationStatus.COMPLETED)
            
            // Create dummy extension info - fix the version type
            val versionInt = try {
                extension.version.toIntOrNull() ?: 1
            } catch (e: Exception) {
                1
            }
            
            val extensionInfo = ExtensionInfo(
                metadata = com.async.core.extension.ExtensionMetadata(
                    id = extension.id,
                    version = versionInt, // Use Int instead of String
                    name = extension.name,
                    developer = "Unknown", // Use developer instead of author
                    description = extension.description,
                    filePath = ""
                ),
                status = com.async.core.extension.ExtensionStatus.INSTALLED,
                isLoaded = false
            )
            
            ExtensionResult.Success(extensionInfo)
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to install extension: ${e.message}" }
            updateInstallationStatus(extension.id, InstallationStatus.ERROR)
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "INSTALL_FAILED"
                )
            )
        }
    }
    
    /**
     * Search across all active extensions
     */
    suspend fun searchAll(query: String): ExtensionResult<List<SearchResult>> {
        return extensionRuntime.searchAll(query)
    }
    
    /**
     * Search using a specific extension
     */
    suspend fun searchWithExtension(extensionId: String, query: String): ExtensionResult<List<SearchResult>> {
        return extensionRuntime.searchWithExtension(extensionId, query)
    }
    
    /**
     * Get stream URL for a track
     */
    suspend fun getStreamUrl(extensionId: String, mediaId: String): ExtensionResult<String> {
        return extensionRuntime.getStreamUrl(extensionId, mediaId)
    }
    
    /**
     * Get album art for a track
     */
    suspend fun getAlbumArt(extensionId: String, artworkUrl: String): ExtensionResult<ByteArray> {
        return extensionRuntime.getAlbumArt(extensionId, artworkUrl)
    }
    
    /**
     * Install extension from local APK file
     */
    suspend fun installExtensionFromFile(apkFile: File): ExtensionResult<ExtensionInfo> {
        return try {
            logcat { "ExtensionService: Installing extension from file ${apkFile.name}" }
            
        // Validate first
        val validationResult = extensionInstaller.validateExtensionForInstallation(apkFile)
        if (validationResult.isError) {
            return validationResult as ExtensionResult<ExtensionInfo>
        }
        
        val validationInfo = validationResult.getOrThrow()
        updateInstallationStatus(validationInfo.extensionId, InstallationStatus.INSTALLING)
        
        // Install
        val installResult = extensionInstaller.installExtension(apkFile)
        
        if (installResult.isSuccess) {
            updateInstallationStatus(validationInfo.extensionId, InstallationStatus.COMPLETED)
            // Re-initialize runtime to pick up new extension
            extensionRuntime.initialize()
        } else {
            updateInstallationStatus(validationInfo.extensionId, InstallationStatus.ERROR)
        }
        
            installResult
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to install extension from file: ${e.message}" }
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "INSTALL_FROM_FILE_FAILED"
                )
            )
        }
    }
    
    /**
     * Validate extension file before installation
     */
    suspend fun validateExtension(apkFile: File): ExtensionResult<ValidationInfo> {
        return extensionInstaller.validateExtensionForInstallation(apkFile)
    }
    
    /**
     * Check for extension updates
     */
    suspend fun checkForUpdates(): ExtensionResult<List<ExtensionUpdate>> {
        return try {
        val installedExtensions = extensionRuntime.availableExtensions.value
        val allUpdates = mutableListOf<ExtensionUpdate>()
        
        _repositories.value.forEach { repositoryUrl ->
            val result = extensionRepository.checkForUpdates(repositoryUrl, installedExtensions)
            if (result.isSuccess) {
                allUpdates.addAll(result.getOrThrow())
            }
        }
        
            ExtensionResult.Success(allUpdates)
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to check for updates: ${e.message}" }
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    e.message ?: "Unknown error",
                    "CHECK_UPDATES_FAILED"
                )
            )
        }
    }
    
    // Private helper methods
    
    private suspend fun loadSavedRepositories() {
        try {
            // Load repositories from SharedPreferences or database
            val sharedPrefs = context.getSharedPreferences("extension_repos", Context.MODE_PRIVATE)
            val savedRepos = sharedPrefs.getStringSet("repositories", emptySet())?.toList() ?: emptyList()
            
            _repositories.value = savedRepos
            logcat { "ExtensionService: Loaded ${savedRepos.size} saved repositories" }
            
            // Refresh remote extensions if we have repositories
            if (savedRepos.isNotEmpty()) {
                refreshRemoteExtensions()
            }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to load saved repositories: ${e.message}" }
        }
    }
    
    private suspend fun saveRepositories(repositories: List<String>) {
        try {
            val sharedPrefs = context.getSharedPreferences("extension_repos", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putStringSet("repositories", repositories.toSet())
                .apply()
            
            logcat { "ExtensionService: Saved ${repositories.size} repositories" }
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to save repositories: ${e.message}" }
        }
    }
    
    private suspend fun refreshRemoteExtensions() {
        try {
            val remoteExtensionsMap = mutableMapOf<String, List<RemoteExtensionInfo>>()
            
            _repositories.value.forEach { repositoryUrl ->
                // For demo purposes, create some sample extensions
                val sampleExtensions = listOf(
                    RemoteExtensionInfo(
                        id = "sample.music.extension",
                        name = "Sample Music Extension",
                        version = "1",
                        description = "A sample music streaming extension",
                        downloadUrl = "$repositoryUrl/sample.apk",
                        iconUrl = "$repositoryUrl/sample.png",
                        minAppVersion = "1"
                    ),
                    RemoteExtensionInfo(
                        id = "demo.audio.source",
                        name = "Demo Audio Source",
                        version = "2",
                        description = "Demo extension for testing purposes",
                        downloadUrl = "$repositoryUrl/demo.apk",
                        iconUrl = "$repositoryUrl/demo.png",
                        minAppVersion = "1"
                    )
                )
                
                remoteExtensionsMap[repositoryUrl] = sampleExtensions
            }
            
            _remoteExtensions.value = remoteExtensionsMap
            logcat { "ExtensionService: Refreshed remote extensions for ${_repositories.value.size} repositories" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to refresh remote extensions: ${e.message}" }
        }
    }
    
    private fun updateInstallationStatus(extensionId: String, status: InstallationStatus) {
        val currentState = _installationState.value.toMutableMap()
        currentState[extensionId] = status
        _installationState.value = currentState
        
        logcat { "ExtensionService: Updated installation status for $extensionId: $status" }
    }
} 