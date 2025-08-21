package com.async.extensions.manager

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionMetadata
import com.async.core.extension.ExtensionStatus
import com.async.core.extension.MusicExtension
import com.async.core.model.ExtensionException
import com.async.core.model.ExtensionResult
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.storage.ExtensionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the lifecycle of music extensions including loading, unloading,
 * enabling/disabling, and state management.
 */
@Singleton
class ExtensionManager @Inject constructor(
    private val context: Context,
    private val extensionLoader: ExtensionLoader,
    private val extensionStorage: ExtensionStorage
) {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val extensionMutex = Mutex()
    
    // Active extensions (loaded and enabled)
    private val activeExtensions = mutableMapOf<String, MusicExtension>()
    
    // All extension information (including disabled ones)
    private val _extensionInfos = MutableStateFlow<Map<String, ExtensionInfo>>(emptyMap())
    val extensionInfos: StateFlow<Map<String, ExtensionInfo>> = _extensionInfos.asStateFlow()
    
    // Currently active extensions
    private val _activeExtensionIds = MutableStateFlow<Set<String>>(emptySet())
    val activeExtensionIds: StateFlow<Set<String>> = _activeExtensionIds.asStateFlow()
    
    init {
        // Load saved extension information on startup
        coroutineScope.launch {
            loadSavedExtensions()
        }
    }
    
    /**
     * Install a new extension from file.
     * 
     * @param extensionFile The extension file to install
     * @param enable Whether to enable the extension after installation
     * @return ExtensionResult containing installation result
     */
    suspend fun installExtension(
        extensionFile: File,
        enable: Boolean = true
    ): ExtensionResult<ExtensionInfo> {
        return extensionMutex.withLock {
            try {
                Timber.i("Installing extension from: ${extensionFile.name}")
                
                // Update status to installing
                val tempInfo = ExtensionInfo(
                    metadata = ExtensionMetadata(
                        id = "temp_${System.currentTimeMillis()}",
                        version = 1,
                        name = "Installing...",
                        developer = "Unknown",
                        description = "Extension being installed"
                    ),
                    status = ExtensionStatus.INSTALLING
                )
                
                // Load the extension
                val loadResult = extensionLoader.loadExtension(extensionFile)
                if (loadResult.isError) {
                    return ExtensionResult.Error(
                        (loadResult as ExtensionResult.Error).exception
                    )
                }
                
                val extension = loadResult.getOrThrow()
                
                // Check if extension already exists
                if (activeExtensions.containsKey(extension.id) || 
                    _extensionInfos.value.containsKey(extension.id)) {
                    return ExtensionResult.Error(
                        ExtensionException.ConfigurationError(
                            "Extension ${extension.id} is already installed"
                        )
                    )
                }
                
                // Copy file to extensions directory
                val targetFile = copyExtensionFile(extensionFile, extension.id)
                
                // Create extension metadata
                val metadata = ExtensionMetadata.fromExtension(extension, targetFile.absolutePath)
                
                // Initialize extension
                val initResult = extension.initialize()
                val status = if (initResult.isSuccess && enable) {
                    ExtensionStatus.INSTALLED
                } else if (initResult.isError) {
                    ExtensionStatus.INSTALL_FAILED
                } else {
                    ExtensionStatus.DISABLED
                }
                
                val extensionInfo = ExtensionInfo(
                    metadata = metadata,
                    status = status,
                    errorMessage = if (initResult.isError) {
                        (initResult as ExtensionResult.Error).exception.message
                    } else null,
                    isLoaded = enable && initResult.isSuccess
                )
                
                // Save extension info
                extensionStorage.saveExtensionInfo(extension.id, extensionInfo)
                
                // Add to active extensions if enabled and initialized successfully
                if (enable && initResult.isSuccess) {
                    activeExtensions[extension.id] = extension
                    updateActiveExtensionIds()
                }
                
                // Update state
                updateExtensionInfos()
                
                Timber.i("Successfully installed extension: ${extension.id}")
                ExtensionResult.Success(extensionInfo)
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to install extension")
                ExtensionResult.Error(
                    ExtensionException.GenericError(
                        "Installation failed: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Uninstall an extension.
     * 
     * @param extensionId The ID of the extension to uninstall
     * @return ExtensionResult indicating success or failure
     */
    suspend fun uninstallExtension(extensionId: String): ExtensionResult<Unit> {
        return extensionMutex.withLock {
            try {
                Timber.i("Uninstalling extension: $extensionId")
                
                val extensionInfo = _extensionInfos.value[extensionId]
                    ?: return ExtensionResult.Error(
                        ExtensionException.NotFoundError("Extension not found: $extensionId")
                    )
                
                // Update status to uninstalling
                updateExtensionStatus(extensionId, ExtensionStatus.UNINSTALLING)
                
                // Disable if active
                if (activeExtensions.containsKey(extensionId)) {
                    disableExtension(extensionId)
                }
                
                // Cleanup extension
                activeExtensions[extensionId]?.cleanup()
                
                // Unload from loader
                extensionLoader.unloadExtension(extensionId)
                
                // Delete extension file
                extensionInfo.metadata.filePath?.let { filePath ->
                    val file = File(filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                
                // Remove from storage
                extensionStorage.removeExtensionInfo(extensionId)
                
                // Update state
                updateExtensionInfos()
                
                Timber.i("Successfully uninstalled extension: $extensionId")
                ExtensionResult.Success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to uninstall extension: $extensionId")
                ExtensionResult.Error(
                    ExtensionException.GenericError(
                        "Uninstallation failed: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Enable an installed extension.
     */
    suspend fun enableExtension(extensionId: String): ExtensionResult<Unit> {
        return extensionMutex.withLock {
            try {
                val extensionInfo = _extensionInfos.value[extensionId]
                    ?: return ExtensionResult.Error(
                        ExtensionException.NotFoundError("Extension not found: $extensionId")
                    )
                
                if (extensionInfo.isLoaded) {
                    return ExtensionResult.Success(Unit) // Already enabled
                }
                
                // Load extension if not already loaded
                val filePath = extensionInfo.metadata.filePath
                    ?: return ExtensionResult.Error(
                        ExtensionException.ConfigurationError("Extension file path not found")
                    )
                
                val loadResult = extensionLoader.loadExtension(File(filePath))
                if (loadResult.isError) {
                    updateExtensionStatus(extensionId, ExtensionStatus.INSTALL_FAILED)
                    return ExtensionResult.Error((loadResult as ExtensionResult.Error).exception)
                }
                
                val extension = loadResult.getOrThrow()
                
                // Initialize extension
                val initResult = extension.initialize()
                if (initResult.isError) {
                    updateExtensionStatus(extensionId, ExtensionStatus.INSTALL_FAILED)
                    return ExtensionResult.Error((initResult as ExtensionResult.Error).exception)
                }
                
                // Add to active extensions
                activeExtensions[extensionId] = extension
                updateExtensionStatus(extensionId, ExtensionStatus.INSTALLED, isLoaded = true)
                updateActiveExtensionIds()
                
                Timber.i("Enabled extension: $extensionId")
                ExtensionResult.Success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to enable extension: $extensionId")
                ExtensionResult.Error(
                    ExtensionException.GenericError(
                        "Failed to enable extension: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Disable an active extension.
     */
    suspend fun disableExtension(extensionId: String): ExtensionResult<Unit> {
        return extensionMutex.withLock {
            try {
                val extension = activeExtensions[extensionId]
                if (extension != null) {
                    extension.cleanup()
                    activeExtensions.remove(extensionId)
                    updateActiveExtensionIds()
                }
                
                updateExtensionStatus(extensionId, ExtensionStatus.DISABLED, isLoaded = false)
                
                Timber.i("Disabled extension: $extensionId")
                ExtensionResult.Success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to disable extension: $extensionId")
                ExtensionResult.Error(
                    ExtensionException.GenericError(
                        "Failed to disable extension: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Get an active extension by ID.
     */
    fun getExtension(extensionId: String): MusicExtension? {
        return activeExtensions[extensionId]
    }
    
    /**
     * Get all active extensions.
     */
    fun getActiveExtensions(): List<MusicExtension> {
        return activeExtensions.values.toList()
    }
    
    /**
     * Get extension info by ID.
     */
    fun getExtensionInfo(extensionId: String): ExtensionInfo? {
        return _extensionInfos.value[extensionId]
    }
    
    /**
     * Update extension configuration.
     */
    suspend fun updateExtensionConfiguration(
        extensionId: String,
        config: Map<String, Any>
    ): ExtensionResult<Unit> {
        val extension = activeExtensions[extensionId]
            ?: return ExtensionResult.Error(
                ExtensionException.NotFoundError("Extension not found or not active: $extensionId")
            )
        
        return extension.updateConfiguration(config)
    }
    
    /**
     * Record extension usage for statistics.
     */
    fun recordExtensionUsage(extensionId: String) {
        coroutineScope.launch {
            try {
                val currentInfo = _extensionInfos.value[extensionId] ?: return@launch
                val updatedInfo = currentInfo.copy(
                    lastUsed = System.currentTimeMillis(),
                    usageCount = currentInfo.usageCount + 1
                )
                
                extensionStorage.saveExtensionInfo(extensionId, updatedInfo)
                updateExtensionInfos()
            } catch (e: Exception) {
                Timber.e(e, "Failed to record usage for extension: $extensionId")
            }
        }
    }
    
    /**
     * Load saved extension information from storage.
     */
    private suspend fun loadSavedExtensions() {
        try {
            val savedInfos = extensionStorage.getAllExtensionInfos()
            _extensionInfos.value = savedInfos
            
            // Load enabled extensions
            savedInfos.values.forEach { info ->
                if (info.status == ExtensionStatus.INSTALLED && info.isLoaded) {
                    enableExtension(info.metadata.id)
                }
            }
            
            Timber.i("Loaded ${savedInfos.size} saved extensions")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load saved extensions")
        }
    }
    
    /**
     * Copy extension file to the extensions directory.
     */
    private fun copyExtensionFile(sourceFile: File, extensionId: String): File {
        val extensionsDir = File(context.filesDir, "extensions")
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs()
        }
        
        val targetFile = File(extensionsDir, "$extensionId.${sourceFile.extension}")
        sourceFile.copyTo(targetFile, overwrite = true)
        
        return targetFile
    }
    
    /**
     * Update extension status.
     */
    private suspend fun updateExtensionStatus(
        extensionId: String,
        status: ExtensionStatus,
        errorMessage: String? = null,
        isLoaded: Boolean? = null
    ) {
        val currentInfo = _extensionInfos.value[extensionId] ?: return
        val updatedInfo = currentInfo.copy(
            status = status,
            errorMessage = errorMessage,
            isLoaded = isLoaded ?: currentInfo.isLoaded
        )
        
        extensionStorage.saveExtensionInfo(extensionId, updatedInfo)
        updateExtensionInfos()
    }
    
    /**
     * Update extension infos from storage.
     */
    private suspend fun updateExtensionInfos() {
        _extensionInfos.value = extensionStorage.getAllExtensionInfos()
    }
    
    /**
     * Update active extension IDs.
     */
    private fun updateActiveExtensionIds() {
        _activeExtensionIds.value = activeExtensions.keys.toSet()
    }
} 