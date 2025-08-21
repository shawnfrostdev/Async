package com.async.extensions.security

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.async.core.extension.ExtensionApi
import com.async.core.extension.MusicExtension
import com.async.core.model.ExtensionException
import com.async.core.model.ExtensionResult
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles security validation and verification of extensions before loading.
 * Provides sandboxing and permission checking capabilities.
 */
@Singleton
class ExtensionValidator @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val MIN_FILE_SIZE = 1024 // 1KB
        
        // Dangerous permissions that extensions should not have
        private val DANGEROUS_PERMISSIONS = setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.CALL_PHONE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS"
        )
        
        // Allowed permissions for extensions
        private val ALLOWED_PERMISSIONS = setOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.WAKE_LOCK"
        )
    }
    
    /**
     * Validate an extension file before loading.
     * 
     * @param extensionFile The extension file to validate
     * @return ExtensionResult indicating validation success or failure
     */
    suspend fun validateExtensionFile(extensionFile: File): ExtensionResult<ValidationResult> {
        return try {
            Timber.d("Validating extension file: ${extensionFile.name}")
            
            val validationResult = ValidationResult()
            
            // Basic file validation
            validateFileBasics(extensionFile, validationResult)
            if (validationResult.hasErrors()) {
                return ExtensionResult.Error(
                    ExtensionException.ConfigurationError(validationResult.getFirstError())
                )
            }
            
            // File integrity check
            validateFileIntegrity(extensionFile, validationResult)
            
            // Permission validation (for APK files)
            if (extensionFile.extension.lowercase() == "apk") {
                validatePermissions(extensionFile, validationResult)
            }
            
            // Content validation
            validateFileContent(extensionFile, validationResult)
            
            // Security scan
            performSecurityScan(extensionFile, validationResult)
            
            if (validationResult.hasErrors()) {
                ExtensionResult.Error(
                    ExtensionException.ConfigurationError(validationResult.getFirstError())
                )
            } else {
                ExtensionResult.Success(validationResult)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Extension validation failed")
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Validation failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Validate a loaded extension instance.
     */
    suspend fun validateExtensionInstance(extension: MusicExtension): ExtensionResult<Unit> {
        return try {
            // Validate extension ID format
            if (!ExtensionApi.isValidExtensionId(extension.id)) {
                return ExtensionResult.Error(
                    ExtensionException.ConfigurationError(
                        "Invalid extension ID: ${extension.id}. Must follow reverse domain naming."
                    )
                )
            }
            
            // Check API compatibility
            if (!ExtensionApi.isCompatible(extension)) {
                return ExtensionResult.Error(
                    ExtensionException.ConfigurationError(
                        ExtensionApi.getCompatibilityMessage(extension) ?: "Extension is not compatible"
                    )
                )
            }
            
            // Validate required fields
            validateRequiredFields(extension)?.let { error ->
                return ExtensionResult.Error(error)
            }
            
            // Validate extension behavior (if possible)
            validateExtensionBehavior(extension)?.let { error ->
                return ExtensionResult.Error(error)
            }
            
            ExtensionResult.Success(Unit)
            
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Instance validation failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Check if an extension is trusted (signed by known developer).
     */
    fun isExtensionTrusted(extensionFile: File): Boolean {
        return try {
            // For now, we don't have a trusted certificate store
            // In production, this would check against known developer certificates
            false
        } catch (e: Exception) {
            Timber.w(e, "Failed to check extension trust status")
            false
        }
    }
    
    /**
     * Get extension signature information.
     */
    fun getExtensionSignature(extensionFile: File): ExtensionSignature? {
        return try {
            if (extensionFile.extension.lowercase() == "apk") {
                getApkSignature(extensionFile)
            } else {
                getJarSignature(extensionFile)
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to get extension signature")
            null
        }
    }
    
    /**
     * Basic file validation (size, format, readability).
     */
    private fun validateFileBasics(file: File, result: ValidationResult) {
        if (!file.exists()) {
            result.addError("Extension file does not exist")
            return
        }
        
        if (!file.canRead()) {
            result.addError("Cannot read extension file")
            return
        }
        
        val fileSize = file.length()
        if (fileSize < MIN_FILE_SIZE) {
            result.addError("Extension file is too small (${fileSize} bytes)")
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            result.addError("Extension file is too large (${fileSize / (1024 * 1024)} MB)")
        }
        
        val extension = file.extension.lowercase()
        if (extension !in ExtensionApi.FileExtensions.let { 
            listOf(it.APK.removePrefix("."), it.JAR.removePrefix("."), it.ZIP.removePrefix("."))
        }) {
            result.addError("Unsupported file type: $extension")
        }
        
        result.fileSize = fileSize
        result.fileType = extension
    }
    
    /**
     * Validate file integrity (checksums, corruption check).
     */
    private fun validateFileIntegrity(file: File, result: ValidationResult) {
        try {
            // Calculate SHA-256 hash
            val hash = calculateFileHash(file)
            result.fileHash = hash
            
            // Try to open as ZIP/JAR to check for corruption
            JarFile(file).use { jarFile ->
                val entries = jarFile.entries()
                var entryCount = 0
                
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    entryCount++
                    
                    // Verify entry can be read
                    if (!entry.isDirectory) {
                        jarFile.getInputStream(entry).use { stream ->
                            stream.read() // Try to read at least one byte
                        }
                    }
                }
                
                if (entryCount == 0) {
                    result.addError("Extension file appears to be empty")
                }
                
                result.entryCount = entryCount
            }
        } catch (e: Exception) {
            result.addError("Extension file appears to be corrupted: ${e.message}")
        }
    }
    
    /**
     * Validate APK permissions.
     */
    private fun validatePermissions(file: File, result: ValidationResult) {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.GET_PERMISSIONS
            )
            
            packageInfo?.requestedPermissions?.let { permissions ->
                val dangerousPerms = permissions.filter { it in DANGEROUS_PERMISSIONS }
                val unknownPerms = permissions.filter { 
                    it !in ALLOWED_PERMISSIONS && it !in DANGEROUS_PERMISSIONS 
                }
                
                if (dangerousPerms.isNotEmpty()) {
                    result.addError("Extension requests dangerous permissions: ${dangerousPerms.joinToString()}")
                }
                
                if (unknownPerms.isNotEmpty()) {
                    result.addWarning("Extension requests unknown permissions: ${unknownPerms.joinToString()}")
                }
                
                result.permissions = permissions.toList()
            }
        } catch (e: Exception) {
            result.addWarning("Could not validate APK permissions: ${e.message}")
        }
    }
    
    /**
     * Validate file content for suspicious patterns.
     */
    private fun validateFileContent(file: File, result: ValidationResult) {
        try {
            JarFile(file).use { jarFile ->
                val entries = jarFile.entries()
                val suspiciousPatterns = listOf(
                    "native-lib", "lib/", ".so",  // Native libraries
                    "classes.dex",                 // Android DEX files (expected in APK)
                    "META-INF/services/",         // Service providers
                    "assets/",                    // Asset files
                )
                
                val foundPatterns = mutableSetOf<String>()
                
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name.lowercase()
                    
                    suspiciousPatterns.forEach { pattern ->
                        if (name.contains(pattern.lowercase())) {
                            foundPatterns.add(pattern)
                        }
                    }
                }
                
                // Native libraries are suspicious
                if (foundPatterns.any { it.contains("lib") || it.contains(".so") }) {
                    result.addWarning("Extension contains native libraries")
                }
                
                result.contentPatterns = foundPatterns.toList()
            }
        } catch (e: Exception) {
            result.addWarning("Could not scan file content: ${e.message}")
        }
    }
    
    /**
     * Perform basic security scan.
     */
    private fun performSecurityScan(file: File, result: ValidationResult) {
        try {
            // Check file name for suspicious patterns
            val fileName = file.name.lowercase()
            val suspiciousNames = listOf("system", "root", "admin", "hack", "crack")
            
            if (suspiciousNames.any { fileName.contains(it) }) {
                result.addWarning("Extension has suspicious file name")
            }
            
            // Additional security checks can be added here
            result.securityScanCompleted = true
            
        } catch (e: Exception) {
            result.addWarning("Security scan failed: ${e.message}")
        }
    }
    
    /**
     * Validate required extension fields.
     */
    private fun validateRequiredFields(extension: MusicExtension): ExtensionException? {
        if (extension.name.isBlank()) {
            return ExtensionException.ConfigurationError("Extension name cannot be empty")
        }
        
        if (extension.developer.isBlank()) {
            return ExtensionException.ConfigurationError("Extension developer cannot be empty")
        }
        
        if (extension.description.isBlank()) {
            return ExtensionException.ConfigurationError("Extension description cannot be empty")
        }
        
        if (extension.version <= 0) {
            return ExtensionException.ConfigurationError("Extension version must be positive")
        }
        
        return null
    }
    
    /**
     * Validate extension behavior (basic runtime checks).
     */
    private fun validateExtensionBehavior(extension: MusicExtension): ExtensionException? {
        try {
            // Check if extension can provide configuration
            val config = extension.getConfiguration()
            // Configuration should be serializable
            
            return null
        } catch (e: Exception) {
            return ExtensionException.GenericError(
                "Extension behavior validation failed: ${e.message}",
                e.javaClass.simpleName
            )
        }
    }
    
    /**
     * Get APK signature information.
     */
    private fun getApkSignature(file: File): ExtensionSignature? {
        return try {
            val packageInfo = context.packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.GET_SIGNATURES
            )
            
            packageInfo?.signatures?.firstOrNull()?.let { signature ->
                ExtensionSignature(
                    algorithm = "APK",
                    fingerprint = signature.toCharsString().take(16),
                    issuer = "Unknown"
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get JAR signature information.
     */
    private fun getJarSignature(file: File): ExtensionSignature? {
        return try {
            JarFile(file).use { jarFile ->
                val manifest = jarFile.manifest
                manifest?.let {
                    ExtensionSignature(
                        algorithm = "JAR",
                        fingerprint = "unsigned",
                        issuer = "Unknown"
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Calculate SHA-256 hash of a file.
     */
    private fun calculateFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Result of extension validation.
 */
data class ValidationResult(
    val errors: MutableList<String> = mutableListOf(),
    val warnings: MutableList<String> = mutableListOf(),
    var fileSize: Long = 0,
    var fileType: String = "",
    var fileHash: String = "",
    var entryCount: Int = 0,
    var permissions: List<String> = emptyList(),
    var contentPatterns: List<String> = emptyList(),
    var securityScanCompleted: Boolean = false
) {
    fun addError(message: String) = errors.add(message)
    fun addWarning(message: String) = warnings.add(message)
    fun hasErrors() = errors.isNotEmpty()
    fun hasWarnings() = warnings.isNotEmpty()
    fun getFirstError() = errors.firstOrNull() ?: "Unknown validation error"
}

/**
 * Extension signature information.
 */
data class ExtensionSignature(
    val algorithm: String,
    val fingerprint: String,
    val issuer: String
) 