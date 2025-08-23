package com.async.extensions.runtime

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionStatus
import com.async.core.extension.MusicExtension
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.manager.ExtensionManager
import com.async.extensions.storage.ExtensionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime extension system that handles discovery, loading, and search integration
 */
@Singleton
class ExtensionRuntime @Inject constructor(
    private val context: Context,
    private val extensionManager: ExtensionManager,
    private val extensionLoader: ExtensionLoader,
    private val extensionStorage: ExtensionStorage
) {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Active loaded extensions
    private val activeExtensions = mutableMapOf<String, MusicExtension>()
    
    // Extension loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Available extensions
    private val _availableExtensions = MutableStateFlow<Map<String, ExtensionInfo>>(emptyMap())
    val availableExtensions: StateFlow<Map<String, ExtensionInfo>> = _availableExtensions.asStateFlow()
    
    /**
     * Initialize the extension runtime and discover installed extensions
     */
    suspend fun initialize() {
        logcat { "Initializing extension runtime" }
        _isLoading.value = true
        
        try {
            // Discover installed extensions
            discoverInstalledExtensions()
            
            // Load enabled extensions
            loadEnabledExtensions()
            
            logcat { "Extension runtime initialized with ${activeExtensions.size} active extensions" }
        } catch (e: Exception) {
            logcat { "Failed to initialize extension runtime: ${e.message}" }
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Discover extensions installed in the extensions directory
     */
    private suspend fun discoverInstalledExtensions() {
        val extensionsDir = context.getDir("extensions", Context.MODE_PRIVATE)
        val extensionFiles = extensionsDir.listFiles { file -> 
            file.isFile && file.extension.lowercase() in listOf("apk", "jar")
        } ?: emptyArray()
        
        logcat { "Found ${extensionFiles.size} extension files" }
        
        val discoveredExtensions = mutableMapOf<String, ExtensionInfo>()
        
        extensionFiles.forEach { file ->
            try {
                val loadResult = extensionLoader.loadExtension(file)
                if (loadResult.isSuccess) {
                    val extension = loadResult.getOrThrow()
                    val extensionInfo = ExtensionInfo(
                        metadata = com.async.core.extension.ExtensionMetadata.fromExtension(
                            extension, 
                            file.absolutePath
                        ),
                        status = ExtensionStatus.INSTALLED,
                        isLoaded = false
                    )
                    
                    // Save to storage
                    extensionStorage.saveExtensionInfo(extension.id, extensionInfo)
                    discoveredExtensions[extension.id] = extensionInfo
                    
                    logcat { "Discovered extension: ${extension.id}" }
                }
            } catch (e: Exception) {
                logcat { "Failed to discover extension from ${file.name}: ${e.message}" }
            }
        }
        
        _availableExtensions.value = discoveredExtensions
    }
    
    /**
     * Load all enabled extensions
     */
    private suspend fun loadEnabledExtensions() {
        val enabledExtensions = _availableExtensions.value.filter { (_, info) ->
            info.status == ExtensionStatus.INSTALLED
        }
        
        enabledExtensions.forEach { (extensionId, extensionInfo) ->
            try {
                val file = java.io.File(extensionInfo.metadata.filePath)
                val loadResult = extensionLoader.loadExtension(file)
                
                if (loadResult.isSuccess) {
                    val extension = loadResult.getOrThrow()
                    val initResult = extension.initialize()
                    
                    if (initResult.isSuccess) {
                        activeExtensions[extensionId] = extension
                        
                        // Update status
                        val updatedInfo = extensionInfo.copy(
                            status = ExtensionStatus.INSTALLED,
                            isLoaded = true
                        )
                        extensionStorage.saveExtensionInfo(extensionId, updatedInfo)
                        
                        logcat { "Loaded extension: $extensionId" }
                    } else {
                        logcat { "Failed to initialize extension $extensionId: ${initResult}" }
                    }
                }
            } catch (e: Exception) {
                logcat { "Failed to load extension $extensionId: ${e.message}" }
            }
        }
        
        // Update available extensions with loading status
        _availableExtensions.value = extensionStorage.getAllExtensionInfos()
    }
    
    /**
     * Search across all active extensions
     */
    suspend fun searchAll(query: String): ExtensionResult<List<SearchResult>> {
        if (activeExtensions.isEmpty()) {
            return ExtensionResult.Success(emptyList())
        }
        
        return try {
            logcat { "Searching across ${activeExtensions.size} extensions for: $query" }
            
            val searchResults = activeExtensions.values.map { extension ->
                coroutineScope.async {
                    try {
                        val result = extension.search(query)
                        if (result.isSuccess) {
                            result.getOrThrow()
                        } else {
                            logcat { "Search failed for ${extension.id}: ${result}" }
                            emptyList()
                        }
                    } catch (e: Exception) {
                        logcat { "Search error for ${extension.id}: ${e.message}" }
                        emptyList()
                    }
                }
            }.awaitAll()
            
            val allResults = searchResults.flatten()
            logcat { "Found ${allResults.size} total results across all extensions" }
            
            ExtensionResult.Success(allResults)
            
        } catch (e: Exception) {
            logcat { "Multi-extension search failed: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Search failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Search using a specific extension
     */
    suspend fun searchWithExtension(extensionId: String, query: String): ExtensionResult<List<SearchResult>> {
        val extension = activeExtensions[extensionId]
            ?: return ExtensionResult.Error(
                com.async.core.model.ExtensionException.ExtensionNotFound(
                    "Extension not found or not loaded: $extensionId"
                )
            )
        
        return try {
            logcat { "Searching with extension $extensionId for: $query" }
            extension.search(query)
        } catch (e: Exception) {
            logcat { "Search failed with extension $extensionId: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Search failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Get stream URL from extension
     */
    suspend fun getStreamUrl(extensionId: String, mediaId: String): ExtensionResult<String> {
        val extension = activeExtensions[extensionId]
            ?: return ExtensionResult.Error(
                com.async.core.model.ExtensionException.ExtensionNotFound(
                    "Extension not found or not loaded: $extensionId"
                )
            )
        
        return try {
            logcat { "Getting stream URL from extension $extensionId for media: $mediaId" }
            extension.getStreamUrl(mediaId)
        } catch (e: Exception) {
            logcat { "Failed to get stream URL from extension $extensionId: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Failed to get stream URL: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Get album art from extension
     */
    suspend fun getAlbumArt(extensionId: String, artworkUrl: String): ExtensionResult<ByteArray> {
        val extension = activeExtensions[extensionId]
            ?: return ExtensionResult.Error(
                com.async.core.model.ExtensionException.ExtensionNotFound(
                    "Extension not found or not loaded: $extensionId"
                )
            )
        
        return try {
            logcat { "Getting album art from extension $extensionId for URL: $artworkUrl" }
            extension.getAlbumArt(artworkUrl)
        } catch (e: Exception) {
            logcat { "Failed to get album art from extension $extensionId: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Failed to get album art: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Enable an extension
     */
    suspend fun enableExtension(extensionId: String): ExtensionResult<Unit> {
        return extensionManager.enableExtension(extensionId).also {
            if (it.isSuccess) {
                // Reload extensions
                loadEnabledExtensions()
            }
        }
    }
    
    /**
     * Disable an extension
     */
    suspend fun disableExtension(extensionId: String): ExtensionResult<Unit> {
        return extensionManager.disableExtension(extensionId).also {
            if (it.isSuccess) {
                // Remove from active extensions
                activeExtensions.remove(extensionId)
                // Update available extensions
                _availableExtensions.value = extensionStorage.getAllExtensionInfos()
            }
        }
    }
    
    /**
     * Get list of active extension IDs
     */
    fun getActiveExtensionIds(): List<String> = activeExtensions.keys.toList()
    
    /**
     * Check if extension is loaded and active
     */
    fun isExtensionActive(extensionId: String): Boolean = activeExtensions.containsKey(extensionId)
} 