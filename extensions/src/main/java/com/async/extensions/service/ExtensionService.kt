package com.async.extensions.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.lang.reflect.Method
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

/**
 * Universal adapter to bridge any third-party extensions to our MusicExtension interface
 */
private class UniversalExtensionAdapter(
    private val thirdPartyInstance: Any,
    private val extensionId: String
) : com.async.core.extension.MusicExtension {
    
    override val id: String = extensionId
    override val version: Int = getPropertyOrDefault("version", 1) as Int
    override val name: String = getPropertyOrDefault("name", "Unknown Extension") as String
    override val developer: String = getPropertyOrDefault("developer", "Unknown Developer") as String
    override val description: String = getPropertyOrDefault("description", "Third-party music extension") as String
    
    // Cache reflection methods for performance
    private val initializeMethod: Method? by lazy {
        findMethod("initialize")
    }
    
    private val searchMethod: Method? by lazy {
        findSearchMethod()
    }
    
    /**
     * Find a method by name with flexible parameter matching
     */
    private fun findMethod(methodName: String): Method? {
        return try {
            val methods = thirdPartyInstance.javaClass.declaredMethods
            methods.find { it.name == methodName }?.also { it.isAccessible = true }
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Failed to find $methodName method: ${e.message}" }
            null
        }
    }
    
    /**
     * Find getStreamUrl method
     */
    private fun findGetStreamUrlMethod(): Method? {
        return try {
            val clazz = thirdPartyInstance.javaClass
            val methods = clazz.declaredMethods.filter { method ->
                method.name == "getStreamUrl"
            }
            
            if (methods.isNotEmpty()) {
                logcat("UniversalAdapter") { "Found ${methods.size} getStreamUrl methods" }
                methods.forEach { method ->
                    logcat("UniversalAdapter") { "getStreamUrl method: ${method.name}(${method.parameterTypes.joinToString { it.simpleName }})" }
                }
                methods.first()
            } else {
                logcat("UniversalAdapter") { "No getStreamUrl method found" }
                null
            }
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Error finding getStreamUrl method: ${e.message}" }
            null
        }
    }
    
    /**
     * Find search method with various possible signatures
     */
    private fun findSearchMethod(): Method? {
        try {
            val clazz = thirdPartyInstance.javaClass
            logcat("UniversalAdapter") { "Searching for methods in class: ${clazz.name}" }
            
            // First, let's see all available methods
            val allMethods = clazz.declaredMethods
            logcat("UniversalAdapter") { "Available methods: ${allMethods.map { "${it.name}(${it.parameterTypes.contentToString()})" }}" }
            
            // Look for any method named "search"
            val searchMethods = allMethods.filter { it.name == "search" }
            logcat("UniversalAdapter") { "Found ${searchMethods.size} search methods" }
            
            for (method in searchMethods) {
                logcat("UniversalAdapter") { "Search method: ${method.name}(${method.parameterTypes.contentToString()})" }
                method.isAccessible = true
                return method
            }
            
            // If no "search" method found, try common alternatives
            val alternativeNames = listOf("find", "query", "lookup", "searchTracks", "searchMusic")
            for (altName in alternativeNames) {
                val altMethods = allMethods.filter { it.name == altName }
                if (altMethods.isNotEmpty()) {
                    logcat("UniversalAdapter") { "Found alternative method: ${altName}" }
                    val method = altMethods.first()
                    method.isAccessible = true
                    return method
                }
            }
            
            logcat("UniversalAdapter") { "No compatible search method found among ${allMethods.size} total methods" }
            return null
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Failed to find search method: ${e.message}" }
            return null
        }
    }
    
    override suspend fun search(query: String, limit: Int, offset: Int): com.async.core.model.ExtensionResult<List<com.async.core.model.SearchResult>> {
        return try {
            logcat("UniversalAdapter") { "Searching extension $extensionId with query: '$query', limit: $limit" }
            
            if (searchMethod == null) {
                logcat("UniversalAdapter") { "No search method available" }
                return com.async.core.model.ExtensionResult.Success(emptyList())
            }
            
            // Call search method with appropriate parameters based on its signature
            val result = when (searchMethod!!.parameterCount) {
                3 -> searchMethod!!.invoke(thirdPartyInstance, query, limit, offset)
                2 -> {
                    // Check if second parameter is Continuation (suspend function)
                    val paramTypes = searchMethod!!.parameterTypes
                    if (paramTypes.size == 2 && paramTypes[1].name.contains("Continuation")) {
                        // This is a Kotlin suspend function - we need to call it as a coroutine
                        logcat("UniversalAdapter") { "Detected Kotlin suspend function, calling with runBlocking" }
                        kotlinx.coroutines.runBlocking {
                            try {
                                // Convert Java Method to Kotlin function and call it as suspend
                                val kotlinFunction = searchMethod!!.kotlinFunction
                                if (kotlinFunction != null) {
                                    kotlinFunction.callSuspend(thirdPartyInstance, query)
                                } else {
                                    logcat("UniversalAdapter") { "Could not convert to Kotlin function" }
                                    null
                                }
                            } catch (e: Exception) {
                                logcat("UniversalAdapter") { "Error calling suspend function: ${e.message}" }
                                null
                            }
                        }
                    } else {
                        searchMethod!!.invoke(thirdPartyInstance, query, limit)
                    }
                }
                1 -> searchMethod!!.invoke(thirdPartyInstance, query)
                0 -> searchMethod!!.invoke(thirdPartyInstance)
                else -> {
                    logcat("UniversalAdapter") { "Unsupported search method parameter count: ${searchMethod!!.parameterCount}" }
                    return com.async.core.model.ExtensionResult.Success(emptyList())
                }
            }
            
            if (result == null) {
                logcat("UniversalAdapter") { "Search method returned null" }
                return com.async.core.model.ExtensionResult.Success(emptyList())
            }
            
            logcat("UniversalAdapter") { "Search method returned: ${result.javaClass.name}" }
            
            // Handle different result wrapper types
            val actualResults = unwrapExtensionResult(result)
            
            if (actualResults == null) {
                logcat("UniversalAdapter") { "Failed to unwrap extension result" }
                return com.async.core.model.ExtensionResult.Success(emptyList())
            }
            
            // Convert third-party search results to our format
            val searchResults = convertThirdPartyResults(actualResults, extensionId)
            logcat("UniversalAdapter") { "Extension search returned ${searchResults.size} results" }
            
            com.async.core.model.ExtensionResult.Success(searchResults)
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Extension search failed: ${e.message}" }
            com.async.core.model.ExtensionResult.Error(com.async.core.model.ExtensionException.GenericError(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun getStreamUrl(mediaId: String): com.async.core.model.ExtensionResult<String> {
        return try {
            logcat("UniversalAdapter") { "Getting stream URL for media: $mediaId" }
            
            // Find getStreamUrl method
            val getStreamUrlMethod = findGetStreamUrlMethod()
            if (getStreamUrlMethod == null) {
                logcat("UniversalAdapter") { "No getStreamUrl method found" }
                return com.async.core.model.ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError("getStreamUrl method not found")
                )
            }
            
            // Call the method
            val result = when (getStreamUrlMethod.parameterCount) {
                2 -> {
                    // Check if second parameter is Continuation (suspend function)
                    val paramTypes = getStreamUrlMethod.parameterTypes
                    if (paramTypes.size == 2 && paramTypes[1].name.contains("Continuation")) {
                        // This is a Kotlin suspend function
                        logcat("UniversalAdapter") { "Detected Kotlin suspend getStreamUrl function" }
                        kotlinx.coroutines.runBlocking {
                            try {
                                val kotlinFunction = getStreamUrlMethod.kotlinFunction
                                if (kotlinFunction != null) {
                                    kotlinFunction.callSuspend(thirdPartyInstance, mediaId)
                                } else {
                                    logcat("UniversalAdapter") { "Could not convert getStreamUrl to Kotlin function" }
                                    null
                                }
                            } catch (e: Exception) {
                                logcat("UniversalAdapter") { "Error calling suspend getStreamUrl: ${e.message}" }
                                null
                            }
                        }
                    } else {
                        getStreamUrlMethod.invoke(thirdPartyInstance, mediaId)
                    }
                }
                1 -> getStreamUrlMethod.invoke(thirdPartyInstance, mediaId)
                else -> {
                    logcat("UniversalAdapter") { "Unsupported getStreamUrl parameter count: ${getStreamUrlMethod.parameterCount}" }
                    return com.async.core.model.ExtensionResult.Error(
                        com.async.core.model.ExtensionException.GenericError("Unsupported getStreamUrl method signature")
                    )
                }
            }
            
            if (result == null) {
                logcat("UniversalAdapter") { "getStreamUrl returned null" }
                return com.async.core.model.ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError("getStreamUrl returned null")
                )
            }
            
            logcat("UniversalAdapter") { "getStreamUrl returned: ${result.javaClass.name}" }
            
            // Handle different result wrapper types
            val actualResult = unwrapExtensionResult(result)
            
            // Check if the original result was an Error and extract the error message
            if (result.javaClass.simpleName.contains("Error")) {
                val errorMessage = try {
                    // Try to get the error message from the Error result
                    val messageField = result.javaClass.getDeclaredField("message")
                    messageField.isAccessible = true
                    val message = messageField.get(result)?.toString()
                    message ?: "Unknown error from extension"
                } catch (e: Exception) {
                    try {
                        // Try alternative field names
                        val exceptionField = result.javaClass.getDeclaredField("exception")
                        exceptionField.isAccessible = true
                        val exception = exceptionField.get(result)
                        exception?.toString() ?: "Unknown error from extension"
                    } catch (e2: Exception) {
                        "Failed to extract error message: ${e2.message}"
                    }
                }
                logcat("UniversalAdapter") { "DabYeet extension returned error: $errorMessage" }
                return com.async.core.model.ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError("DabYeet error: $errorMessage")
                )
            }
            
            if (actualResult is String) {
                logcat("UniversalAdapter") { "Successfully got stream URL: $actualResult" }
                com.async.core.model.ExtensionResult.Success(actualResult)
            } else {
                logcat("UniversalAdapter") { "getStreamUrl result is not a String: ${actualResult?.javaClass?.name}" }
                com.async.core.model.ExtensionResult.Error(
                    com.async.core.model.ExtensionException.GenericError("Invalid stream URL format")
                )
            }
            
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "getStreamUrl failed: ${e.message}" }
            com.async.core.model.ExtensionResult.Error(
                com.async.core.model.ExtensionException.GenericError(e.message ?: "Unknown error")
            )
        }
    }
    
    override suspend fun getAlbumArt(url: String): com.async.core.model.ExtensionResult<ByteArray> {
        return com.async.core.model.ExtensionResult.Error(
            com.async.core.model.ExtensionException.GenericError("Album art not supported by universal adapter yet")
        )
    }
    
    /**
     * Get a property from the third-party extension with a default fallback
     */
    private fun getPropertyOrDefault(propertyName: String, defaultValue: Any): Any {
        return getProperty(thirdPartyInstance, propertyName) ?: defaultValue
    }
    
    /**
     * Unwrap third-party ExtensionResult wrappers to get the actual data
     */
    private fun unwrapExtensionResult(result: Any): Any? {
        try {
            val resultClass = result.javaClass
            logcat("UniversalAdapter") { "Unwrapping result of type: ${resultClass.name}" }
            
            // Check if it's a Success wrapper (DabYeet style)
            if (resultClass.name.contains("ExtensionResult") || resultClass.name.contains("Success")) {
                // Try to get the data property
                val dataProperty = getProperty(result, "data") 
                    ?: getProperty(result, "value")
                    ?: getProperty(result, "result")
                
                if (dataProperty != null) {
                    logcat("UniversalAdapter") { "Found wrapped data: ${dataProperty.javaClass.name}" }
                    return dataProperty
                }
                
                // Try to call a method to get the data
                try {
                    val getDataMethod = resultClass.getDeclaredMethod("getData")
                        ?: resultClass.getDeclaredMethod("getValue")
                        ?: resultClass.getDeclaredMethod("getResult")
                    
                    if (getDataMethod != null) {
                        getDataMethod.isAccessible = true
                        val data = getDataMethod.invoke(result)
                        logcat("UniversalAdapter") { "Got data via method: ${data?.javaClass?.name}" }
                        return data
                    }
                } catch (e: Exception) {
                    logcat("UniversalAdapter") { "Failed to call data getter method: ${e.message}" }
                }
            }
            
            // If no wrapper detected, return the result as-is
            logcat("UniversalAdapter") { "No wrapper detected, using result directly" }
            return result
            
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Error unwrapping result: ${e.message}" }
            return result
        }
    }
    
    private fun convertThirdPartyResults(thirdPartyResults: Any, extensionId: String): List<com.async.core.model.SearchResult> {
        try {
            // Handle various result formats from third-party extensions
            // We'll use reflection to extract the data
            if (thirdPartyResults is List<*>) {
                return thirdPartyResults.mapNotNull { item ->
                    item?.let { convertThirdPartyResult(it, extensionId) }
                }
            }
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Failed to convert third-party results: ${e.message}" }
        }
        return emptyList()
    }
    
    private fun convertThirdPartyResult(thirdPartyResult: Any, extensionId: String): com.async.core.model.SearchResult? {
        try {
            // Use reflection to get common properties from any third-party result
            val title = getProperty(thirdPartyResult, "title") 
                ?: getProperty(thirdPartyResult, "name") 
                ?: getProperty(thirdPartyResult, "trackName")
                ?: "Unknown Title"
                
            val artist = getProperty(thirdPartyResult, "artist") 
                ?: getProperty(thirdPartyResult, "artistName")
                ?: getProperty(thirdPartyResult, "author")
                ?: "Unknown Artist"
                
            val album = getProperty(thirdPartyResult, "album") 
                ?: getProperty(thirdPartyResult, "albumName")
                ?: getProperty(thirdPartyResult, "albumTitle") // DabYeet specific
                
            // Handle duration - DabYeet returns seconds, we need milliseconds
            val duration = getProperty(thirdPartyResult, "duration")?.let { dur ->
                val durValue = dur.toString().toLongOrNull() ?: 0L
                // If it's a reasonable duration in seconds (< 10000), convert to milliseconds
                if (durValue > 0 && durValue < 10000) durValue * 1000 else durValue
            } ?: getProperty(thirdPartyResult, "length")?.toString()?.toLongOrNull()
                
            val artwork = getProperty(thirdPartyResult, "thumbnailUrl") // DabYeet specific
                ?: getProperty(thirdPartyResult, "artwork") 
                ?: getProperty(thirdPartyResult, "thumbnail")
                ?: getProperty(thirdPartyResult, "image")
                ?: getProperty(thirdPartyResult, "cover")
                ?: getProperty(thirdPartyResult, "albumCover") // DabYeet specific
            
            // Get the actual track ID from the third-party result
            val trackId = getProperty(thirdPartyResult, "id")?.toString() 
                ?: thirdPartyResult.hashCode().toString()
            
            return com.async.core.model.SearchResult(
                id = trackId,
                title = title.toString(),
                artist = artist?.toString() ?: "Unknown Artist",
                album = album?.toString(),
                duration = duration,
                thumbnailUrl = artwork?.toString(),
                extensionId = extensionId
            )
        } catch (e: Exception) {
            logcat("UniversalAdapter") { "Failed to convert individual result: ${e.message}" }
            return null
        }
    }
    
    private fun getProperty(obj: Any, propertyName: String): Any? {
        return try {
            val field = obj.javaClass.getDeclaredField(propertyName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            try {
                val method = obj.javaClass.getDeclaredMethod("get${propertyName.replaceFirstChar { it.uppercase() }}")
                method.invoke(obj)
            } catch (e2: Exception) {
                null
            }
        }
    }
}

/**
 * Extension service that fetches real data from repositories
 */
class ExtensionService(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    // Service scope for background operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Service initialization state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Cache for loaded extension instances
    private val extensionInstanceCache = mutableMapOf<String, com.async.core.extension.MusicExtension>()
    
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
    
    // Update states
    private val _updateStatus = MutableStateFlow<Map<String, UpdateStatus>>(emptyMap())
    val updateStatus: StateFlow<Map<String, UpdateStatus>> = _updateStatus.asStateFlow()
    
    // Downloaded APK files
    private val _downloadedApks = MutableStateFlow<Map<String, File>>(emptyMap())
    val downloadedApks: StateFlow<Map<String, File>> = _downloadedApks.asStateFlow()
    
    // Update checking
    private val updateCheckInterval = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    
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
        
        try {
            // Try to uninstall the actual APK package
            val packageName = extensionId // Assuming extensionId is the package name
            
            val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            logcat { "ExtensionService: Starting uninstall for package: $packageName" }
            context.startActivity(uninstallIntent)
            
            // Start monitoring for uninstall completion
            startUninstallMonitoring(extensionId)
            
            logcat { "ExtensionService: Uninstall process started for $extensionId" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Uninstall failed for $extensionId: ${e.message}" }
            
            // Still clean up our records even if system uninstall fails
            cleanupAfterUninstall(extensionId)
        }
    }
    
    /**
     * Start monitoring for uninstall completion
     */
    private fun startUninstallMonitoring(extensionId: String) {
        serviceScope.launch {
            var attempts = 0
            val maxAttempts = 30 // Monitor for up to 30 seconds
            
            while (attempts < maxAttempts) {
                delay(1000) // Check every second
                attempts++
                
                if (!isPackageInstalled(extensionId)) {
                    logcat { "ExtensionService: Package $extensionId successfully uninstalled" }
                    cleanupAfterUninstall(extensionId)
                    break
                }
                
                if (attempts >= maxAttempts) {
                    logcat { "ExtensionService: Uninstall monitoring timeout for $extensionId" }
                    // User might have cancelled the uninstall - don't clean up
                }
            }
        }
    }
    
    /**
     * Clean up after successful uninstall
     */
    private suspend fun cleanupAfterUninstall(extensionId: String) {
        // Remove from our installed extensions list
        val current = _installedExtensions.value.toMutableMap()
        current.remove(extensionId)
        _installedExtensions.value = current
        
        // Clean up downloaded APK file
        val apkFile = _downloadedApks.value[extensionId]
        if (apkFile?.exists() == true) {
            apkFile.delete()
            logcat { "ExtensionService: Deleted downloaded APK for $extensionId" }
        }
        
        // Remove from downloaded APKs
        val currentApks = _downloadedApks.value.toMutableMap()
        currentApks.remove(extensionId)
        _downloadedApks.value = currentApks
        
        // Reset installation status
        updateInstallationStatus(extensionId, InstallationStatus.IDLE)
        
        // Clear from extension cache
        extensionInstanceCache.remove(extensionId)
        
        // Clear update status
        val currentUpdateStatus = _updateStatus.value.toMutableMap()
        currentUpdateStatus.remove(extensionId)
        _updateStatus.value = currentUpdateStatus
        
        logcat { "ExtensionService: Cleanup completed for $extensionId" }
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
     * Download extension from repository (step 1)
     */
    suspend fun downloadExtension(repositoryUrl: String, extension: RemoteExtensionInfo) {
        logcat { "ExtensionService: Downloading ${extension.name}" }
        
        // Update download state
        updateInstallationStatus(extension.id, InstallationStatus.DOWNLOADING)
        
        try {
            val apkFile = withContext(Dispatchers.IO) {
                // Create downloads directory
                val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                // Build download URL
                val baseUrl = repositoryUrl.substringBeforeLast("/")
                val downloadUrl = if (extension.downloadPath?.startsWith("http") == true) {
                    extension.downloadPath
                } else {
                    "$baseUrl/${extension.downloadPath}"
                }
                
                logcat { "ExtensionService: Downloading APK from: $downloadUrl" }
                
                // Download APK file
                val apkFile = File(downloadsDir, "${extension.id}.apk")
                val url = URL(downloadUrl)
                val connection = url.openConnection()
                connection.connect()
                
                val inputStream = connection.getInputStream()
                val outputStream = FileOutputStream(apkFile)
                
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                outputStream.close()
                inputStream.close()
                
                logcat { "ExtensionService: APK downloaded to: ${apkFile.absolutePath}" }
                apkFile
            }
            
            // Store downloaded APK
            val currentApks = _downloadedApks.value.toMutableMap()
            currentApks[extension.id] = apkFile
            _downloadedApks.value = currentApks
            
            updateInstallationStatus(extension.id, InstallationStatus.DOWNLOADED)
            logcat { "ExtensionService: Successfully downloaded ${extension.name}" }
            logcat { "ExtensionService: APK saved to: ${apkFile.absolutePath}" }
            logcat { "ExtensionService: APK file size: ${apkFile.length()} bytes" }
            logcat { "ExtensionService: Now tracking ${_downloadedApks.value.size} downloaded APKs" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Download failed for ${extension.name}: ${e.message}" }
            updateInstallationStatus(extension.id, InstallationStatus.ERROR)
        }
    }
    
    /**
     * Install downloaded extension (step 2)
     */
    suspend fun installDownloadedExtension(extensionId: String) {
        logcat { "ExtensionService: Installing downloaded extension $extensionId" }
        
        // Find the downloaded APK file
        val apkFile = _downloadedApks.value[extensionId]
        if (apkFile == null || !apkFile.exists()) {
            logcat { "ExtensionService: APK file not found for $extensionId" }
            updateInstallationStatus(extensionId, InstallationStatus.ERROR)
            return
        }
        
        // Find the extension info from remote extensions
        val extension = _remoteExtensions.value.values.flatten()
            .find { it.extension.id == extensionId }?.extension
            
        if (extension == null) {
            logcat { "ExtensionService: Extension $extensionId not found" }
            updateInstallationStatus(extensionId, InstallationStatus.ERROR)
            return
        }
        
        // Update installation state
        updateInstallationStatus(extensionId, InstallationStatus.INSTALLING)
        
        try {
            // Install APK using system installer
            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }
            
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            
            logcat { "ExtensionService: Starting APK installation for ${extension.name}" }
            context.startActivity(installIntent)
            
            logcat { "ExtensionService: APK installer launched for ${extension.name}" }
            
            // Start a background job to periodically check for installation completion
            startInstallationMonitoring(extensionId)
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Installation failed for ${extension.name}: ${e.message}" }
            updateInstallationStatus(extensionId, InstallationStatus.ERROR)
        }
    }
    
    /**
     * Monitor installation progress and update status when complete
     */
    private fun startInstallationMonitoring(extensionId: String) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var attempts = 0
            val maxAttempts = 60 // 60 seconds maximum (installations can take time)
            
            while (attempts < maxAttempts) {
                delay(1000) // Check every second
                attempts++
                
                if (isPackageInstalled(extensionId)) {
                    logcat { "ExtensionService: ✅ Installation detected for $extensionId after $attempts seconds" }
                    
                    // Update status and sync
                    updateInstallationStatus(extensionId, InstallationStatus.COMPLETED)
                    syncInstalledExtensions()
                    return@launch
                }
            }
            
            // If we reach here, installation might have failed or been cancelled
            logcat { "ExtensionService: ⚠️ Installation monitoring timeout for $extensionId after $maxAttempts seconds" }
            // Reset to downloaded state since we still have the APK
            updateInstallationStatus(extensionId, InstallationStatus.DOWNLOADED)
            logcat { "ExtensionService: Reset $extensionId status to DOWNLOADED - user can try installing again" }
        }
    }
    
    /**
     * Install extension from repository (legacy - for backward compatibility)
     */
    suspend fun installExtensionFromRepository(repositoryUrl: String, extension: RemoteExtensionInfo) {
        // For backward compatibility, do both steps automatically
        downloadExtension(repositoryUrl, extension)
        delay(100) // Small delay between steps
        installDownloadedExtension(extension.id)
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
            
            // Sync with actually installed packages
            syncInstalledExtensions()
            
            // Check for updates after fetching new data
            checkForUpdates(false)
            
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
     * Force check a specific package name and update UI accordingly
     */
    suspend fun forceCheckPackage(packageName: String) {
        logcat { "ExtensionService: Force checking package: $packageName" }
        
        // Try multiple detection methods
        logcat { "ExtensionService: Method 1 - Direct getPackageInfo" }
        val method1Result = try {
            context.packageManager.getPackageInfo(packageName, 0)
            logcat { "ExtensionService: Method 1 SUCCESS" }
            true
        } catch (e: Exception) {
            logcat { "ExtensionService: Method 1 FAILED: ${e.message}" }
            false
        }
        
        logcat { "ExtensionService: Method 2 - Search installed packages list" }
        val method2Result = try {
            val allPackages = context.packageManager.getInstalledPackages(0)
            val found = allPackages.any { it.packageName == packageName }
            logcat { "ExtensionService: Method 2 ${if (found) "SUCCESS" else "FAILED"}" }
            found
        } catch (e: Exception) {
            logcat { "ExtensionService: Method 2 ERROR: ${e.message}" }
            false
        }
        
        logcat { "ExtensionService: Method 3 - Try launching the app" }
        val method3Result = try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            val found = intent != null
            logcat { "ExtensionService: Method 3 ${if (found) "SUCCESS" else "FAILED"}" }
            found
        } catch (e: Exception) {
            logcat { "ExtensionService: Method 3 ERROR: ${e.message}" }
            false
        }
        
        // Method 4 - Search for ANY app that could be our extension
        logcat { "ExtensionService: Method 4 - Comprehensive search for potential matches" }
        try {
            val allPackages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            logcat { "ExtensionService: Searching through ${allPackages.size} packages for potential matches..." }
            
            val potentialMatches = allPackages.filter { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: ""
                val packageName = pkg.packageName
                
                // Look for any app that could be our extension
                packageName.contains("dabyeet", ignoreCase = true) ||
                packageName.contains("dab", ignoreCase = true) ||
                packageName.contains("yeet", ignoreCase = true) ||
                appName.contains("dab", ignoreCase = true) ||
                appName.contains("yeet", ignoreCase = true) ||
                appName.contains("Async:", ignoreCase = true) ||
                appName.contains("Dab.yeet", ignoreCase = true)
            }
            
            logcat { "ExtensionService: Found ${potentialMatches.size} potential extension matches:" }
            potentialMatches.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                val versionName = pkg.versionName ?: "Unknown"
                val isEnabled = pkg.applicationInfo?.enabled ?: false
                val installTime = try {
                    context.packageManager.getPackageInfo(pkg.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L }
                
                logcat { "ExtensionService: 🔍 POTENTIAL MATCH: ${pkg.packageName}" }
                logcat { "ExtensionService:    - App Name: $appName" }
                logcat { "ExtensionService:    - Version: $versionName" }
                logcat { "ExtensionService:    - Enabled: $isEnabled" }
                logcat { "ExtensionService:    - Installed: ${java.util.Date(installTime)}" }
            }
            
            // Also search for apps installed in last 24 hours
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val recentApps = allPackages.filter { pkg ->
                val installTime = try {
                    context.packageManager.getPackageInfo(pkg.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L }
                installTime > oneDayAgo && (pkg.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0
            }
            
            logcat { "ExtensionService: Found ${recentApps.size} apps installed in last 24 hours:" }
            recentApps.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                val installTime = try {
                    context.packageManager.getPackageInfo(pkg.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L }
                logcat { "ExtensionService: 📅 RECENT: ${pkg.packageName} (${appName}) - ${java.util.Date(installTime)}" }
            }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Method 4 ERROR: ${e.message}" }
        }
        
        logcat { "ExtensionService: RESULTS for $packageName:" }
        logcat { "ExtensionService: - Method 1 (direct): $method1Result" }
        logcat { "ExtensionService: - Method 2 (list): $method2Result" }
        logcat { "ExtensionService: - Method 3 (intent): $method3Result" }
        
        val isActuallyInstalled = method1Result || method2Result || method3Result
        
        if (isActuallyInstalled) {
            logcat { "ExtensionService: ✅ Package $packageName IS INSTALLED - updating state" }
            checkExtensionInstallationStatus(packageName)
        } else {
            logcat { "ExtensionService: ❌ Package $packageName is NOT INSTALLED" }
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
        
        // Final sync after all repositories are loaded
        syncInstalledExtensions()
        
        // Check for updates after refreshing all repositories
        checkForUpdates(force = true)
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
     * Check if a package is installed on the device
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            // First try the direct approach
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            logcat { "ExtensionService: ✅ Package $packageName found! Version: ${packageInfo.versionName}" }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            logcat { "ExtensionService: ❌ Direct lookup failed for $packageName: ${e.message}" }
            
            // Try alternative method - search through all installed packages
            try {
                val allPackages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                val foundPackage = allPackages.find { it.packageName == packageName }
                
                if (foundPackage != null) {
                    logcat { "ExtensionService: ✅ Package $packageName found via list search! Version: ${foundPackage.versionName}" }
                    return true
                } else {
                    logcat { "ExtensionService: ❌ Package $packageName not found in installed packages list" }
                    
                    // Debug: log all packages that contain our target string
                    val similarPackages = allPackages.filter { 
                        it.packageName.contains("dabyeet", ignoreCase = true) ||
                        it.packageName.contains("async", ignoreCase = true) ||
                        it.packageName.contains("extension", ignoreCase = true)
                    }
                    logcat { "ExtensionService: Found ${similarPackages.size} similar packages:" }
                    similarPackages.forEach { pkg ->
                        logcat { "ExtensionService: - ${pkg.packageName} (${pkg.applicationInfo?.loadLabel(context.packageManager)})" }
                    }
                    return false
                }
            } catch (e2: Exception) {
                logcat { "ExtensionService: ⚠️ Error during list search for $packageName: ${e2.message}" }
                return false
            }
        } catch (e: Exception) {
            logcat { "ExtensionService: ⚠️ Error checking package $packageName: ${e.message}" }
            false
        }
    }
    
    /**
     * Get the actual package name from an APK file
     */
    private fun getPackageNameFromApk(apkFile: File): String? {
        return try {
            val packageInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            packageInfo?.packageName
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to get package name from APK: ${e.message}" }
            null
        }
    }
    
    /**
     * List all installed packages for debugging
     */
    private fun listInstalledPackages() {
        try {
            val packages = context.packageManager.getInstalledPackages(0)
            logcat { "ExtensionService: Found ${packages.size} installed packages" }
            
            // Log packages that might be relevant
            val relevantPackages = packages.filter { 
                it.packageName.contains("dabyeet", ignoreCase = true) || 
                it.packageName.contains("async", ignoreCase = true) ||
                it.packageName.contains("extension", ignoreCase = true)
            }
            
            logcat { "ExtensionService: Relevant packages (${relevantPackages.size}):" }
            relevantPackages.forEach { pkg ->
                logcat { "ExtensionService: - ${pkg.packageName}" }
            }
            
            // Also log first 10 packages to see what's installed
            logcat { "ExtensionService: Sample of all packages:" }
            packages.take(10).forEach { pkg ->
                logcat { "ExtensionService: - ${pkg.packageName}" }
            }
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to list packages: ${e.message}" }
        }
    }
    
    /**
     * Manually check if a specific extension is installed and update its status
     */
    suspend fun checkExtensionInstallationStatus(extensionId: String) {
        logcat { "ExtensionService: Manually checking installation status for $extensionId" }
        
        val isActuallyInstalled = isPackageInstalled(extensionId)
        val currentInstalled = _installedExtensions.value.toMutableMap()
        val isTrackedAsInstalled = currentInstalled.containsKey(extensionId)
        
        logcat { "ExtensionService: $extensionId - Actually installed: $isActuallyInstalled, Tracked: $isTrackedAsInstalled" }
        
        when {
            isActuallyInstalled && !isTrackedAsInstalled -> {
                // Package is installed but we're not tracking it - add it
                val extension = _remoteExtensions.value.values.flatten()
                    .find { it.extension.id == extensionId }?.extension
                
                if (extension != null) {
                    val extensionInfo = ExtensionInfo(
                        metadata = ExtensionMetadata(
                            id = extension.id,
                            version = extension.version.toIntOrNull() ?: 1,
                            name = extension.name,
                            developer = extension.developer,
                            description = extension.description
                        ),
                        status = ExtensionStatus.INSTALLED,
                        isLoaded = true
                    )
                    currentInstalled[extension.id] = extensionInfo
                    updateInstallationStatus(extension.id, InstallationStatus.COMPLETED)
                    logcat { "ExtensionService: ✅ Added newly detected installation: ${extension.name}" }
                }
            }
            !isActuallyInstalled && isTrackedAsInstalled -> {
                // Package is not installed but we're tracking it - remove it
                currentInstalled.remove(extensionId)
                val hasDownloadedApk = _downloadedApks.value[extensionId]?.exists() == true
                updateInstallationStatus(extensionId, if (hasDownloadedApk) InstallationStatus.DOWNLOADED else InstallationStatus.IDLE)
                logcat { "ExtensionService: ❌ Removed uninstalled package: $extensionId" }
            }
        }
        
        _installedExtensions.value = currentInstalled
    }
    
    /**
     * Sync installed extensions with actual device packages
     */
    suspend fun syncInstalledExtensions() {
        logcat { "ExtensionService: Syncing installed extensions with device packages" }
        
        val currentInstalled = _installedExtensions.value.toMutableMap()
        val allRemoteExtensions = _remoteExtensions.value.values.flatten()
        
        logcat { "ExtensionService: Found ${allRemoteExtensions.size} remote extensions to check" }
        logcat { "ExtensionService: Currently tracking ${currentInstalled.size} installed extensions" }
        
        // Debug: List installed packages
        listInstalledPackages()
        
        // Debug: Search for any package that might be the extension
        logcat { "ExtensionService: Searching for extension packages manually..." }
        try {
            val allPackages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES)
            val possibleExtensions = allPackages.filter { pkg ->
                pkg.packageName.contains("dabyeet", ignoreCase = true) ||
                pkg.packageName.contains("yeet", ignoreCase = true) ||
                pkg.packageName.contains("async", ignoreCase = true) ||
                pkg.applicationInfo?.loadLabel(context.packageManager)?.toString()?.contains("dab", ignoreCase = true) == true ||
                pkg.applicationInfo?.loadLabel(context.packageManager)?.toString()?.contains("yeet", ignoreCase = true) == true ||
                pkg.applicationInfo?.loadLabel(context.packageManager)?.toString()?.contains("async", ignoreCase = true) == true
            }
            
            logcat { "ExtensionService: Found ${possibleExtensions.size} possible extension packages:" }
            possibleExtensions.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                val isEnabled = pkg.applicationInfo?.enabled ?: false
                logcat { "ExtensionService: - ${pkg.packageName} (${appName}) [enabled: $isEnabled]" }
            }
            
            // Also search broadly for any newly installed packages
            logcat { "ExtensionService: Searching for any user-installed packages..." }
            val userApps = allPackages.filter { pkg ->
                (pkg.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0
            }
            
            logcat { "ExtensionService: Found ${userApps.size} user-installed apps:" }
            userApps.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                logcat { "ExtensionService: USER APP: ${pkg.packageName} (${appName})" }
                
                // Check if this could be our missing extension
                if (appName.contains("Async:", ignoreCase = true) || 
                    appName.contains("Dab.yeet", ignoreCase = true) ||
                    pkg.packageName.contains("dabyeet", ignoreCase = true)) {
                    logcat { "ExtensionService: 🔍 POTENTIAL EXTENSION FOUND: ${pkg.packageName} (${appName})" }
                }
            }
            
            // Also look for any apps with "dab", "yeet", or "async" in the app name
            logcat { "ExtensionService: Searching for apps with relevant names..." }
            allPackages.filter { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: ""
                appName.contains("dab", ignoreCase = true) || 
                appName.contains("yeet", ignoreCase = true) ||
                appName.contains("async", ignoreCase = true)
            }.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                logcat { "ExtensionService: RELEVANT NAME: ${pkg.packageName} (${appName})" }
            }
            
            // Also search for packages that might contain our target extension
            logcat { "ExtensionService: Searching for packages containing 'dabyeet'..." }
            allPackages.filter { pkg ->
                pkg.packageName.contains("dabyeet", ignoreCase = true) ||
                pkg.packageName.contains("dab", ignoreCase = true) ||
                pkg.packageName.contains("yeet", ignoreCase = true)
            }.forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                logcat { "ExtensionService: DABYEET PACKAGE: ${pkg.packageName} (${appName})" }
            }
            
            // Check recently installed packages (last 10 user apps)
            logcat { "ExtensionService: Recently installed user apps:" }
            userApps.takeLast(10).forEach { pkg ->
                val appName = pkg.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown"
                val installTime = try {
                    context.packageManager.getPackageInfo(pkg.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L }
                logcat { "ExtensionService: RECENT USER APP: ${pkg.packageName} (${appName}) - installed: ${java.util.Date(installTime)}" }
            }
        } catch (e: Exception) {
            logcat { "ExtensionService: Error searching for extensions: ${e.message}" }
        }
        
        // Check each remote extension to see if it's actually installed
        allRemoteExtensions.forEach { extensionWithRepo ->
            val extension = extensionWithRepo.extension
            
            // Check if we have the downloaded APK to verify package name
            val downloadedApk = _downloadedApks.value[extension.id]
            logcat { "ExtensionService: Downloaded APK for ${extension.id}: ${downloadedApk?.absolutePath}" }
            logcat { "ExtensionService: APK exists: ${downloadedApk?.exists()}" }
            
            if (downloadedApk?.exists() == true) {
                val actualPackageName = getPackageNameFromApk(downloadedApk)
                logcat { "ExtensionService: APK file package name: $actualPackageName" }
                logcat { "ExtensionService: Manifest package name: ${extension.id}" }
                
                if (actualPackageName != null && actualPackageName != extension.id) {
                    logcat { "ExtensionService: ⚠️ Package name mismatch! APK: $actualPackageName, Manifest: ${extension.id}" }
                } else if (actualPackageName == null) {
                    logcat { "ExtensionService: ⚠️ Could not read package name from APK file" }
                }
            } else {
                logcat { "ExtensionService: ⚠️ No downloaded APK found for ${extension.id}" }
            }
            
            val isActuallyInstalled = isPackageInstalled(extension.id)
            val isTrackedAsInstalled = currentInstalled.containsKey(extension.id)
            
            logcat { "ExtensionService: Checking ${extension.name} (${extension.id})" }
            logcat { "ExtensionService: - Actually installed: $isActuallyInstalled" }
            logcat { "ExtensionService: - Tracked as installed: $isTrackedAsInstalled" }
            
            when {
                isActuallyInstalled && !isTrackedAsInstalled -> {
                    // Package is installed but we're not tracking it - add it
                    val extensionInfo = ExtensionInfo(
                        metadata = ExtensionMetadata(
                            id = extension.id,
                            version = parseVersionToInt(extension.version),
                            name = extension.name,
                            developer = extension.developer,
                            description = extension.description,
                            versionString = extension.version
                        ),
                        status = ExtensionStatus.INSTALLED,
                        isLoaded = true
                    )
                    currentInstalled[extension.id] = extensionInfo
                    updateInstallationStatus(extension.id, InstallationStatus.COMPLETED)
                    logcat { "ExtensionService: ✅ Added installed package: ${extension.name}" }
                }
                !isActuallyInstalled && isTrackedAsInstalled -> {
                    // Package is not installed but we're tracking it - remove it
                    currentInstalled.remove(extension.id)
                    // If we have the APK downloaded, set status to DOWNLOADED, otherwise IDLE
                    val hasDownloadedApk = _downloadedApks.value[extension.id]?.exists() == true
                    updateInstallationStatus(extension.id, if (hasDownloadedApk) InstallationStatus.DOWNLOADED else InstallationStatus.IDLE)
                    logcat { "ExtensionService: ❌ Removed uninstalled package: ${extension.name}" }
                }
                isActuallyInstalled && isTrackedAsInstalled -> {
                    // Update the extension info with the latest remote data (version might have changed)
                    val currentInfo = currentInstalled[extension.id]
                    val remoteVersion = extension.version
                    val currentVersion = currentInfo?.metadata?.versionString ?: currentInfo?.metadata?.version?.toString() ?: "1"
                    
                    if (currentVersion != remoteVersion) {
                        logcat { "ExtensionService: Updating ${extension.name} version from $currentVersion to $remoteVersion" }
                        val updatedInfo = ExtensionInfo(
                            metadata = ExtensionMetadata(
                                id = extension.id,
                                version = parseVersionToInt(remoteVersion),
                                name = extension.name,
                                developer = extension.developer,
                                description = extension.description,
                                versionString = remoteVersion
                            ),
                            status = currentInfo?.status ?: ExtensionStatus.INSTALLED,
                            isLoaded = currentInfo?.isLoaded ?: true
                        )
                        currentInstalled[extension.id] = updatedInfo
                    } else {
                        logcat { "ExtensionService: ✅ ${extension.name} is correctly tracked as installed" }
                    }
                }
                !isActuallyInstalled && !isTrackedAsInstalled -> {
                    logcat { "ExtensionService: ⏸️ ${extension.name} is correctly tracked as not installed" }
                }
            }
        }
        
        _installedExtensions.value = currentInstalled
        logcat { "ExtensionService: Sync complete. Now tracking ${currentInstalled.size} installed extensions" }
    }

    /**
     * Check for extension updates
     */
    suspend fun checkForUpdates(force: Boolean = false): Int {
        val sharedPrefs = context.getSharedPreferences("extension_updates", Context.MODE_PRIVATE)
        val lastCheckTime = sharedPrefs.getLong("last_update_check", 0L)
        val currentTime = System.currentTimeMillis()
        
        // Check if 24 hours have passed or if forced
        if (!force && (currentTime - lastCheckTime) < updateCheckInterval) {
            logcat { "ExtensionService: Skipping update check - last check was ${(currentTime - lastCheckTime) / (60 * 60 * 1000)} hours ago" }
            return _updateStatus.value.size
        }
        
        logcat { "ExtensionService: Checking for extension updates..." }
        
        val installedExtensions = _installedExtensions.value
        val remoteExtensions = _remoteExtensions.value.values.flatten()
        val currentUpdateStatus = _updateStatus.value.toMutableMap()
        
        var updateCount = 0
        
        logcat { "ExtensionService: Checking updates for ${installedExtensions.size} installed extensions" }
        logcat { "ExtensionService: Available remote extensions: ${remoteExtensions.size}" }
        
        installedExtensions.forEach { (extensionId, installedExt) ->
            logcat { "ExtensionService: Checking updates for extension: $extensionId" }
            val remoteExt = remoteExtensions.find { it.extension.id == extensionId }?.extension
            
            if (remoteExt != null) {
                logcat { "ExtensionService: Found remote version for $extensionId" }
                val hasUpdate = checkVersionUpdate(installedExt, remoteExt)
                
                if (hasUpdate.hasUpdate) {
                    currentUpdateStatus[extensionId] = hasUpdate
                    updateCount++
                    logcat { "ExtensionService: ✅ Update available for ${installedExt.metadata.name}: ${installedExt.metadata.version} -> ${remoteExt.version}" }
                } else {
                    currentUpdateStatus.remove(extensionId)
                    logcat { "ExtensionService: ❌ No update needed for ${installedExt.metadata.name}" }
                }
            } else {
                logcat { "ExtensionService: ⚠️ No remote version found for $extensionId" }
            }
        }
        
        _updateStatus.value = currentUpdateStatus
        
        // Save last check time
        sharedPrefs.edit().putLong("last_update_check", currentTime).apply()
        sharedPrefs.edit().putInt("extension_updates_count", updateCount).apply()
        
        if (updateCount > 0) {
            showUpdateNotification(updateCount)
        }
        
        logcat { "ExtensionService: Update check complete. Found $updateCount updates" }
        return updateCount
    }
    
    /**
     * Check if extension has update available
     */
    private fun checkVersionUpdate(installed: ExtensionInfo, remote: RemoteExtensionInfo): UpdateStatus {
        val installedVersionCode = installed.metadata.version
        val remoteVersionCode = remote.version.toIntOrNull() ?: 0
        
        logcat { "ExtensionService: Version comparison for ${installed.metadata.name}:" }
        logcat { "ExtensionService: - Installed version: $installedVersionCode" }
        logcat { "ExtensionService: - Remote version: ${remote.version} -> $remoteVersionCode" }
        logcat { "ExtensionService: - Has update: ${remoteVersionCode > installedVersionCode}" }
        
        // Compare version codes
        val hasVersionUpdate = remoteVersionCode > installedVersionCode
        
        // For now, we don't have library version in the data structure
        // This can be extended when library version tracking is added
        val libVersionUpdate = false
        
        return UpdateStatus(
            hasUpdate = hasVersionUpdate || libVersionUpdate,
            availableVersion = remote.version,
            availableVersionCode = remoteVersionCode,
            libVersionUpdate = libVersionUpdate
        )
    }
    
    /**
     * Show system notification for available updates
     */
    private fun showUpdateNotification(updateCount: Int) {
        logcat { "ExtensionService: Showing update notification for $updateCount extensions" }
        // TODO: Implement notification system
        // This would use NotificationManager to show system notifications
    }
    
    /**
     * Update specific extension
     */
    suspend fun updateExtension(extensionId: String) {
        logcat { "ExtensionService: Starting update for extension $extensionId" }
        
        val remoteExt = _remoteExtensions.value.values.flatten()
            .find { it.extension.id == extensionId }
        
        if (remoteExt != null) {
            try {
                // Download and install the new version
                downloadExtension(remoteExt.repositoryUrl, remoteExt.extension)
                delay(2000) // Wait for download
                installDownloadedExtension(extensionId)
                
                // Wait for installation to complete and then sync
                delay(3000)
                syncInstalledExtensions()
                
                // Clear extension cache to force reload of new version
                clearExtensionCache(extensionId)
                
                // Clear update status after successful update
                val currentStatus = _updateStatus.value.toMutableMap()
                currentStatus.remove(extensionId)
                _updateStatus.value = currentStatus
                
                logcat { "ExtensionService: Successfully updated extension $extensionId" }
                
            } catch (e: Exception) {
                logcat { "ExtensionService: Failed to update extension $extensionId: ${e.message}" }
                // Don't clear update status on failure so user can retry
            }
        } else {
            logcat { "ExtensionService: No remote extension found for $extensionId" }
        }
    }
    
    /**
     * Update all extensions that have updates available
     */
    suspend fun updateAllExtensions() {
        val extensionsWithUpdates = _updateStatus.value.keys.toList()
        logcat { "ExtensionService: Updating ${extensionsWithUpdates.size} extensions" }
        
        extensionsWithUpdates.forEach { extensionId ->
            updateExtension(extensionId)
            delay(2000) // Stagger updates to avoid overwhelming the system
        }
    }
    
    /**
     * Get count of available updates
     */
    fun getUpdateCount(): Int {
        return _updateStatus.value.size
    }
    
    /**
     * Clear extension instance cache to force reload of updated extensions
     */
    suspend fun clearExtensionCache(extensionId: String? = null) {
        if (extensionId != null) {
            logcat { "ExtensionService: Clearing cache for extension: $extensionId" }
            extensionInstanceCache.remove(extensionId)
        } else {
            logcat { "ExtensionService: Clearing all extension caches" }
            extensionInstanceCache.clear()
        }
    }
    
    /**
     * Force reload a specific extension by clearing its cache and re-syncing
     */
    suspend fun forceReloadExtension(extensionId: String) {
        logcat { "ExtensionService: Force reloading extension: $extensionId" }
        
        // Clear the specific extension from cache
        clearExtensionCache(extensionId)
        
        // Trigger a sync to refresh the extension data
        syncInstalledExtensions()
        
        logcat { "ExtensionService: Extension $extensionId force reload completed" }
    }
    
    /**
     * Parse version string to integer for comparison
     * Converts semantic versions like "1.0.1" to integers like 10001
     */
    private fun parseVersionToInt(versionString: String): Int {
        return try {
            // If it's already an integer, use it
            versionString.toIntOrNull()?.let { return it }
            
            // Parse semantic version (e.g., "1.0.1" -> 10001)
            val parts = versionString.split(".")
            when (parts.size) {
                1 -> parts[0].toIntOrNull() ?: 1
                2 -> {
                    val major = parts[0].toIntOrNull() ?: 0
                    val minor = parts[1].toIntOrNull() ?: 0
                    major * 100 + minor
                }
                3 -> {
                    val major = parts[0].toIntOrNull() ?: 0
                    val minor = parts[1].toIntOrNull() ?: 0
                    val patch = parts[2].toIntOrNull() ?: 0
                    major * 10000 + minor * 100 + patch
                }
                else -> 1
            }
        } catch (e: Exception) {
            logcat { "ExtensionService: Failed to parse version '$versionString': ${e.message}" }
            1
        }
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
    
    /**
     * Search within a specific extension
     */
    suspend fun searchInExtension(
        extensionId: String, 
        query: String, 
        limit: Int = 50
    ): List<com.async.core.model.SearchResult> = withContext(Dispatchers.IO) {
        try {
            logcat("ExtensionService") { "Searching in extension: $extensionId for query: $query" }
            
            // Get the installed extension
            val extensionInfo = _installedExtensions.value[extensionId]
            if (extensionInfo == null) {
                logcat("ExtensionService") { "Extension $extensionId not found in installed extensions" }
                return@withContext emptyList()
            }
            
            // Load the extension instance
            val extensionInstance = loadExtensionInstance(extensionId, extensionInfo)
            if (extensionInstance == null) {
                logcat("ExtensionService") { "Failed to load extension instance for $extensionId" }
                return@withContext emptyList()
            }
            
            // Call the extension's search method
            val searchResult = extensionInstance.search(query, limit)
            
            return@withContext when (searchResult) {
                is com.async.core.model.ExtensionResult.Success -> {
                    logcat("ExtensionService") { "Extension $extensionId returned ${searchResult.data.size} results" }
                    // Ensure all results have the correct extensionId
                    searchResult.data.map { result ->
                        if (result.extensionId.isEmpty()) {
                            result.copy(extensionId = extensionId)
                        } else {
                            result
                        }
                    }
                }
                is com.async.core.model.ExtensionResult.Error -> {
                    logcat("ExtensionService") { "Extension $extensionId search failed: ${searchResult.exception.message}" }
                    emptyList()
                }
                is com.async.core.model.ExtensionResult.Loading -> {
                    logcat("ExtensionService") { "Extension $extensionId search still loading" }
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logcat("ExtensionService") { "Error during extension search: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Get stream URL from an extension
     */
    suspend fun getStreamUrl(extensionId: String, mediaId: String): String? = withContext(Dispatchers.IO) {
        try {
            logcat("ExtensionService") { "Getting stream URL from extension: $extensionId for media: $mediaId" }
            
            // Get the installed extension
            val extensionInfo = _installedExtensions.value[extensionId]
            if (extensionInfo == null) {
                logcat("ExtensionService") { "Extension $extensionId not found in installed extensions" }
                return@withContext null
            }
            
            // Load the extension instance
            val extensionInstance = loadExtensionInstance(extensionId, extensionInfo)
            if (extensionInstance == null) {
                logcat("ExtensionService") { "Failed to load extension instance for $extensionId" }
                return@withContext null
            }
            
            // Call the extension's getStreamUrl method
            val streamResult = extensionInstance.getStreamUrl(mediaId)
            
            return@withContext when (streamResult) {
                is com.async.core.model.ExtensionResult.Success -> {
                    logcat("ExtensionService") { "Extension $extensionId returned stream URL: ${streamResult.data}" }
                    streamResult.data
                }
                is com.async.core.model.ExtensionResult.Error -> {
                    logcat("ExtensionService") { "Extension $extensionId getStreamUrl failed: ${streamResult.exception.message}" }
                    null
                }
                is com.async.core.model.ExtensionResult.Loading -> {
                    logcat("ExtensionService") { "Extension $extensionId getStreamUrl still loading" }
                    null
                }
            }
        } catch (e: Exception) {
            logcat("ExtensionService") { "Failed to get stream URL: ${e.message}" }
            null
        }
    }
    
    /**
     * Load and instantiate an extension from its APK
     */
    private suspend fun loadExtensionInstance(
        extensionId: String,
        extensionInfo: ExtensionInfo
    ): com.async.core.extension.MusicExtension? = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            extensionInstanceCache[extensionId]?.let { cached ->
                logcat("ExtensionService") { "Using cached extension instance: $extensionId" }
                return@withContext cached
            }
            
            logcat("ExtensionService") { "Loading extension instance: $extensionId" }
            
            // Get the package info for the extension
            val packageManager = context.packageManager
            val packageInfo = try {
                packageManager.getPackageInfo(extensionId, PackageManager.GET_META_DATA)
            } catch (e: Exception) {
                logcat("ExtensionService") { "Failed to get package info for $extensionId: ${e.message}" }
                return@withContext null
            }
            
            // Create a DexClassLoader to load the extension
            val apkPath = packageInfo.applicationInfo?.sourceDir
            if (apkPath == null) {
                logcat("ExtensionService") { "Could not get APK path for $extensionId" }
                return@withContext null
            }
            val dexOutputDir = File(context.filesDir, "extensions_dex")
            if (!dexOutputDir.exists()) {
                dexOutputDir.mkdirs()
            }
            
            val classLoader = dalvik.system.DexClassLoader(
                apkPath,
                dexOutputDir.absolutePath,
                null,
                context.classLoader
            )
            
            // Find the extension class
            // Most extensions will have a main class that implements MusicExtension
            val extensionClassName = findExtensionMainClass(classLoader, extensionId)
            if (extensionClassName == null) {
                logcat("ExtensionService") { "Could not find main extension class in $extensionId" }
                return@withContext null
            }
            
            // Load and instantiate the extension
            val extensionClass = classLoader.loadClass(extensionClassName)
            
            // Check if this extension needs an adapter (uses different interface)
            val needsAdapter = try {
                !com.async.core.extension.MusicExtension::class.java.isAssignableFrom(extensionClass)
            } catch (e: Exception) { true }
            
            val extensionInstance: com.async.core.extension.MusicExtension = if (needsAdapter) {
                logcat("ExtensionService") { "Creating universal extension adapter for $extensionId" }
                // Create adapter for any non-standard extension
                val thirdPartyInstance = extensionClass.getDeclaredConstructor().newInstance()
                UniversalExtensionAdapter(thirdPartyInstance, extensionId)
            } else {
                // Standard extension that implements our interface directly
                extensionClass.getDeclaredConstructor().newInstance() as com.async.core.extension.MusicExtension
            }
            
            // Initialize the extension
            val initResult = extensionInstance.initialize()
            when (initResult) {
                is com.async.core.model.ExtensionResult.Success -> {
                    logcat("ExtensionService") { "Extension $extensionId initialized successfully" }
                    // Cache the successfully loaded extension
                    extensionInstanceCache[extensionId] = extensionInstance
                    return@withContext extensionInstance
                }
                is com.async.core.model.ExtensionResult.Error -> {
                    logcat("ExtensionService") { "Extension $extensionId initialization failed: ${initResult.exception.message}" }
                    return@withContext null
                }
                is com.async.core.model.ExtensionResult.Loading -> {
                    logcat("ExtensionService") { "Extension $extensionId initialization still loading" }
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            logcat("ExtensionService") { "Error loading extension instance: ${e.message}" }
            return@withContext null
        }
    }
    
    /**
     * Find the main extension class in an APK
     */
    private fun findExtensionMainClass(classLoader: ClassLoader, extensionId: String): String? {
        try {
            logcat("ExtensionService") { "Looking for extension class in $extensionId" }
            
            // Get package metadata to find the extension class name
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(extensionId, PackageManager.GET_META_DATA)
            val metaData = packageInfo.applicationInfo?.metaData
            
            // Check if the extension specifies its main class in metadata
            val specifiedClassName = metaData?.getString("extension.main.class")
            if (specifiedClassName != null) {
                try {
                    val clazz = classLoader.loadClass(specifiedClassName)
                    if (com.async.core.extension.MusicExtension::class.java.isAssignableFrom(clazz)) {
                        logcat("ExtensionService") { "Found extension class from metadata: $specifiedClassName" }
                        return specifiedClassName
                    }
                } catch (e: ClassNotFoundException) {
                    logcat("ExtensionService") { "Specified class $specifiedClassName not found" }
                }
            }
            
            // Try to find classes by scanning - more comprehensive approach
            return findExtensionClassByScanning(classLoader, extensionId)
            
        } catch (e: Exception) {
            logcat("ExtensionService") { "Error finding extension class: ${e.message}" }
            return null
        }
    }
    
    /**
     * Find extension class by trying comprehensive naming patterns
     */
    private fun findExtensionClassByScanning(classLoader: ClassLoader, extensionId: String): String? {
        val packageName = extensionId
        val shortName = packageName.substringAfterLast('.')
        val capitalizedShortName = shortName.replaceFirstChar { it.uppercase() }
        
        // Expanded list of possible class names
        val possibleClassNames = listOf(
            // Standard patterns
            "$packageName.Extension",
            "$packageName.Main", 
            "$packageName.MusicExtension",
            "$packageName.${capitalizedShortName}Extension",
            "$packageName.$capitalizedShortName",
            
            // Sub-package patterns
            "$packageName.main.Extension",
            "$packageName.main.Main",
            "$packageName.main.MusicExtension",
            "$packageName.extension.Extension",
            "$packageName.extension.Main", 
            "$packageName.extension.MusicExtension",
            "$packageName.src.Extension",
            "$packageName.src.Main",
            
            // Simple class names
            "$packageName.$shortName",
            "$packageName.${shortName}Extension",
            "$packageName.${shortName}Main",
            
            // Common extension naming patterns
            "$packageName.${capitalizedShortName}MusicExtension",
            "$packageName.AsyncExtension",
            "$packageName.MusicProvider",
            "$packageName.Provider",
            "$packageName.ExtensionImpl",
            "$packageName.${capitalizedShortName}Provider",
            
            // Try without package prefix (sometimes classes are in default package)
            "Extension",
            "Main",
            "MusicExtension",
            capitalizedShortName,
            "${capitalizedShortName}Extension",
            
            // DabYeet specific patterns (based on package name)
            "$packageName.DabYeetExtension",
            "$packageName.Dabyeet",
            "$packageName.DabYeet",
            "$packageName.extension.DabYeet",
            "$packageName.extension.Dabyeet"
        )
        
        logcat("ExtensionService") { "Trying ${possibleClassNames.size} possible class names for $extensionId" }
        
        for ((index, className) in possibleClassNames.withIndex()) {
            try {
                logcat("ExtensionService") { "Trying class [$index]: $className" }
                val clazz = classLoader.loadClass(className)
                
                if (com.async.core.extension.MusicExtension::class.java.isAssignableFrom(clazz)) {
                    logcat("ExtensionService") { "✅ Found extension class: $className" }
                    return className
                }
                
                // Log what interfaces/superclasses this class actually has
                val interfaces = clazz.interfaces.map { it.name }
                val superclass = clazz.superclass?.name ?: "None"
                logcat("ExtensionService") { "Class $className exists but doesn't implement MusicExtension" }
                logcat("ExtensionService") { "  - Superclass: $superclass" }
                logcat("ExtensionService") { "  - Interfaces: ${interfaces.joinToString(", ")}" }
                
                // Check if it implements any known extension interface (different API)
                val knownExtensionInterfaces = listOf(
                    "com.dabyeet.async.extension.core.MusicExtension",
                    // Add more known extension interfaces here
                    "com.example.musicextension.MusicProvider",
                    "com.musicapp.extension.ExtensionInterface",
                    // Generic patterns that might be used
                    "MusicExtension",
                    "ExtensionInterface"
                )
                
                val matchingInterface = interfaces.find { interfaceName ->
                    knownExtensionInterfaces.any { known ->
                        interfaceName.contains(known) || interfaceName.endsWith(".MusicExtension") || interfaceName.endsWith(".ExtensionInterface")
                    }
                }
                
                if (matchingInterface != null) {
                    logcat("ExtensionService") { "  - ✅ Found compatible extension interface: $matchingInterface - creating adapter!" }
                    return className // Return this class for adapter creation
                }
                
                // Check if it implements any other extension interface we can recognize
                val hasSearchMethod = try {
                    clazz.getDeclaredMethod("search", String::class.java) != null ||
                    clazz.getDeclaredMethod("search", String::class.java, Int::class.java) != null
                } catch (e: Exception) { false }
                
                if (hasSearchMethod) {
                    logcat("ExtensionService") { "  - Has search method - might be compatible!" }
                } else {
                    logcat("ExtensionService") { "  - No search method found" }
                }
                
            } catch (e: ClassNotFoundException) {
                // Continue trying other class names
                logcat("ExtensionService") { "Class not found: $className" }
            } catch (e: Exception) {
                logcat("ExtensionService") { "Error loading class $className: ${e.message}" }
            }
        }
        
        logcat("ExtensionService") { "❌ No extension class found for $extensionId among ${possibleClassNames.size} candidates" }
        
        // Try to get more info about what classes are actually in the APK
        tryToListAvailableClasses(classLoader, extensionId)
        
        return null
    }
    
    /**
     * Attempt to discover what classes are available in the extension APK
     */
    private fun tryToListAvailableClasses(classLoader: ClassLoader, extensionId: String) {
        try {
            logcat("ExtensionService") { "Attempting to discover available classes in $extensionId..." }
            
            // Get the APK path
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(extensionId, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir
            
            if (apkPath != null) {
                logcat("ExtensionService") { "APK path: $apkPath" }
                // Note: Full class enumeration would require ZipFile parsing
                // For now, just log the APK path for debugging
            }
            
        } catch (e: Exception) {
            logcat("ExtensionService") { "Could not enumerate classes: ${e.message}" }
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
    DOWNLOADING,
    DOWNLOADED,
    INSTALLING,
    COMPLETED,
    ERROR
}

/**
 * Update status for extensions
 */
data class UpdateStatus(
    val hasUpdate: Boolean = false,
    val availableVersion: String? = null,
    val availableVersionCode: Int? = null,
    val libVersionUpdate: Boolean = false
) 