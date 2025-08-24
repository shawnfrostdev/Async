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
            
            logcat { "ExtensionService: Uninstall process started for $extensionId" }
            
        } catch (e: Exception) {
            logcat { "ExtensionService: Uninstall failed for $extensionId: ${e.message}" }
            
            // Still remove from our list even if system uninstall fails
            val current = _installedExtensions.value.toMutableMap()
            current.remove(extensionId)
            _installedExtensions.value = current
        }
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
                    logcat { "ExtensionService: ✅ ${extension.name} is correctly tracked as installed" }
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
        
        installedExtensions.forEach { (extensionId, installedExt) ->
            val remoteExt = remoteExtensions.find { it.extension.id == extensionId }?.extension
            
            if (remoteExt != null) {
                val hasUpdate = checkVersionUpdate(installedExt, remoteExt)
                
                if (hasUpdate.hasUpdate) {
                    currentUpdateStatus[extensionId] = hasUpdate
                    updateCount++
                    logcat { "ExtensionService: Update available for ${installedExt.metadata.name}: ${installedExt.metadata.version} -> ${remoteExt.version}" }
                } else {
                    currentUpdateStatus.remove(extensionId)
                }
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
            // Download and install the new version
            downloadExtension(remoteExt.repositoryUrl, remoteExt.extension)
            delay(1000) // Wait for download
            installDownloadedExtension(extensionId)
            
            // Clear update status after updating
            val currentStatus = _updateStatus.value.toMutableMap()
            currentStatus.remove(extensionId)
            _updateStatus.value = currentStatus
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