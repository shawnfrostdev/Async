package com.async.extensions.installer

import android.content.Context
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionStatus
import com.async.core.model.ExtensionResult
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.repository.RemoteExtensionInfo
import com.async.extensions.security.ExtensionValidator
import com.async.extensions.storage.ExtensionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.logcat
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles extension installation from APK files with validation
 */
@Singleton
class ExtensionInstaller @Inject constructor(
    private val context: Context,
    private val extensionLoader: ExtensionLoader,
    private val extensionValidator: ExtensionValidator,
    private val extensionStorage: ExtensionStorage
) {
    
    /**
     * Install extension from downloaded APK file
     */
    suspend fun installExtension(
        apkFile: File,
        remoteInfo: RemoteExtensionInfo? = null
    ): ExtensionResult<ExtensionInfo> {
        return withContext(Dispatchers.IO) {
            try {
                logcat { "Installing extension from: ${apkFile.absolutePath}" }
                
                // Validate APK file
                val validationResult = extensionValidator.validateExtensionFile(apkFile)
                if (validationResult.isError) {
                    return@withContext validationResult as ExtensionResult<ExtensionInfo>
                }
                
                // Load extension to get metadata
                val loadResult = extensionLoader.loadExtension(apkFile)
                if (loadResult.isError) {
                    return@withContext loadResult as ExtensionResult<ExtensionInfo>
                }
                
                val extension = loadResult.getOrThrow()
                
                // Check if extension already exists
                val existingInfo = extensionStorage.getExtensionInfo(extension.id)
                if (existingInfo != null) {
                    // Check version for update
                    if (remoteInfo != null) {
                        val remoteVersionInt = try {
                            remoteInfo.version.toIntOrNull() ?: 1
                        } catch (e: Exception) {
                            1
                        }
                        if (remoteVersionInt > existingInfo.metadata.version) {
                            return@withContext updateExtension(apkFile, extension.id, remoteInfo)
                        } else {
                            return@withContext ExtensionResult.Error(
                                com.async.core.model.ExtensionException.ConfigurationError(
                                    "Extension ${extension.id} is already installed"
                                )
                            )
                        }
                    } else {
                        return@withContext ExtensionResult.Error(
                            com.async.core.model.ExtensionException.ConfigurationError(
                                "Extension ${extension.id} is already installed"
                            )
                        )
                    }
                }
                
                // Create extensions directory
                val extensionsDir = context.getDir("extensions", Context.MODE_PRIVATE)
                extensionsDir.mkdirs()
                
                // Copy APK to extensions directory
                val targetFile = File(extensionsDir, "${extension.id}.apk")
                apkFile.copyTo(targetFile, overwrite = true)
                
                // Create extension metadata
                val metadata = com.async.core.extension.ExtensionMetadata.fromExtension(
                    extension, 
                    targetFile.absolutePath
                )
                
                // Test initialization
                val initResult = extension.initialize()
                val status = if (initResult.isSuccess) {
                    ExtensionStatus.INSTALLED
                } else {
                    ExtensionStatus.INSTALL_FAILED
                }
                
                val extensionInfo = ExtensionInfo(
                    metadata = metadata,
                    status = status,
                    errorMessage = if (initResult.isError) {
                        (initResult as ExtensionResult.Error).exception.message
                    } else null,
                    isLoaded = false
                )
                
                // Save extension info
                extensionStorage.saveExtensionInfo(extension.id, extensionInfo)
                
                logcat { "Successfully installed extension: ${extension.id}" }
                ExtensionResult.Success(extensionInfo)
                
            } catch (e: Exception) {
                logcat { "Failed to install extension: ${e.message}" }
                ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError(
                        "Installation failed: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Update existing extension
     */
    private suspend fun updateExtension(
        apkFile: File,
        extensionId: String,
        remoteInfo: RemoteExtensionInfo
    ): ExtensionResult<ExtensionInfo> {
        return try {
            logcat { "Updating extension: $extensionId" }
            
            // Remove old extension
            uninstallExtension(extensionId)
            
            // Install new version
            installExtension(apkFile, remoteInfo)
            
        } catch (e: Exception) {
            logcat { "Failed to update extension $extensionId: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Update failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Uninstall extension
     */
    suspend fun uninstallExtension(extensionId: String): ExtensionResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                logcat { "Uninstalling extension: $extensionId" }
                
                val extensionInfo = extensionStorage.getExtensionInfo(extensionId)
                    ?: return@withContext ExtensionResult.Error(
                        com.async.core.model.ExtensionException.ExtensionNotFound(
                            "Extension not found: $extensionId"
                        )
                    )
                
                // Delete APK file
                val extensionFile = File(extensionInfo.metadata.filePath)
                if (extensionFile.exists()) {
                    extensionFile.delete()
                    logcat { "Deleted extension file: ${extensionFile.absolutePath}" }
                }
                
                // Remove from storage
                extensionStorage.removeExtensionInfo(extensionId)
                
                logcat { "Successfully uninstalled extension: $extensionId" }
                ExtensionResult.Success(Unit)
                
            } catch (e: Exception) {
                logcat { "Failed to uninstall extension $extensionId: ${e.message}" }
                ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError(
                        "Uninstallation failed: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
    
    /**
     * Install extension from remote repository
     */
    suspend fun installFromRepository(
        repositoryUrl: String,
        remoteInfo: RemoteExtensionInfo,
        downloadedFile: File
    ): ExtensionResult<ExtensionInfo> {
        return try {
            logcat { "Installing extension ${remoteInfo.id} from repository: $repositoryUrl" }
            
            // Verify downloaded file matches expected extension
            val loadResult = extensionLoader.loadExtension(downloadedFile)
            if (loadResult.isError) {
                return loadResult as ExtensionResult<ExtensionInfo>
            }
            
            val extension = loadResult.getOrThrow()
            if (extension.id != remoteInfo.id) {
                return ExtensionResult.Error(
                    com.async.core.model.ExtensionException.ConfigurationError(
                        "Extension ID mismatch: expected ${remoteInfo.id}, got ${extension.id}"
                    )
                )
            }
            
            // Compare versions properly
            val remoteVersionInt = try {
                remoteInfo.version.toIntOrNull() ?: 1
            } catch (e: Exception) {
                1
            }
            
            if (extension.version != remoteVersionInt) {
                return ExtensionResult.Error(
                    com.async.core.model.ExtensionException.ConfigurationError(
                        "Extension version mismatch: expected ${remoteInfo.version}, got ${extension.version}"
                    )
                )
            }
            
            // Install the extension
            installExtension(downloadedFile, remoteInfo)
            
        } catch (e: Exception) {
            logcat { "Failed to install extension from repository: ${e.message}" }
            ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(
                    "Repository installation failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Validate extension before installation
     */
    suspend fun validateExtensionForInstallation(apkFile: File): ExtensionResult<ValidationInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate file
                val validationResult = extensionValidator.validateExtensionFile(apkFile)
                if (validationResult.isError) {
                    return@withContext validationResult as ExtensionResult<ValidationInfo>
                }
                
                // Load extension metadata
                val loadResult = extensionLoader.loadExtension(apkFile)
                if (loadResult.isError) {
                    return@withContext loadResult as ExtensionResult<ValidationInfo>
                }
                
                val extension = loadResult.getOrThrow()
                
                val validationInfo = ValidationInfo(
                    extensionId = extension.id,
                    name = extension.name,
                    version = extension.version,
                    developer = extension.developer,
                    isValid = true,
                    existingVersion = extensionStorage.getExtensionInfo(extension.id)?.metadata?.version,
                    permissions = emptyList() // TODO: Extract from APK
                )
                
                ExtensionResult.Success(validationInfo)
                
            } catch (e: Exception) {
                ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError(
                        "Validation failed: ${e.message}",
                        e.javaClass.simpleName
                    )
                )
            }
        }
    }
}

/**
 * Extension validation information
 */
data class ValidationInfo(
    val extensionId: String,
    val name: String,
    val version: Int, // Change to Int to match ExtensionMetadata
    val developer: String,
    val isValid: Boolean,
    val existingVersion: Int? = null, // Change to Int to match ExtensionMetadata
    val permissions: List<String> = emptyList(),
    val fileSize: Long = 0,
    val checksum: String? = null
) 