package com.async.extensions.loader

import android.content.Context
import com.async.core.extension.ExtensionApi
import com.async.core.extension.MusicExtension
import com.async.core.model.ExtensionException
import com.async.core.model.ExtensionResult
import dalvik.system.DexClassLoader
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.jar.JarFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles dynamic loading of music extensions from APK/JAR files.
 * 
 * This class provides secure loading of extensions with validation,
 * error handling, and proper cleanup.
 */
@Singleton
class ExtensionLoader @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "ExtensionLoader"
        private const val EXTENSION_CLASS_SUFFIX = "Extension"
        private const val MUSIC_EXTENSION_INTERFACE = "com.async.core.extension.MusicExtension"
    }
    
    private val loadedClassLoaders = mutableMapOf<String, DexClassLoader>()
    private val extensionCache = mutableMapOf<String, MusicExtension>()
    
    /**
     * Load an extension from a file (APK or JAR).
     * 
     * @param extensionFile The extension file to load
     * @param expectedClassName Optional specific class name to load
     * @return ExtensionResult containing the loaded MusicExtension or error
     */
    suspend fun loadExtension(
        extensionFile: File,
        expectedClassName: String? = null
    ): ExtensionResult<MusicExtension> {
        return try {
            Timber.d("Loading extension from: ${extensionFile.absolutePath}")
            
            // Validate file
            val validationResult = validateExtensionFile(extensionFile)
            if (validationResult.isError) {
                return validationResult as ExtensionResult<MusicExtension>
            }
            
            // Check cache first
            val fileHash = calculateFileHash(extensionFile)
            extensionCache[fileHash]?.let { cachedExtension ->
                Timber.d("Returning cached extension: ${cachedExtension.id}")
                return ExtensionResult.Success(cachedExtension)
            }
            
            // Create class loader
            val classLoader = createClassLoader(extensionFile)
            
            // Find and load extension class
            val extensionClass = findExtensionClass(classLoader, extensionFile, expectedClassName)
                ?: return ExtensionResult.Error(
                    ExtensionException.ParseError(
                        "No valid MusicExtension implementation found in ${extensionFile.name}"
                    )
                )
            
            // Create extension instance
            val extension = createExtensionInstance(extensionClass)
            
            // Validate extension
            val validationError = validateExtension(extension)
            if (validationError != null) {
                return ExtensionResult.Error(validationError)
            }
            
            // Cache the extension
            extensionCache[fileHash] = extension
            loadedClassLoaders[fileHash] = classLoader
            
            Timber.i("Successfully loaded extension: ${extension.id} v${extension.version}")
            ExtensionResult.Success(extension)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load extension from ${extensionFile.name}")
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Failed to load extension: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Unload an extension and clean up resources.
     * 
     * @param extensionId The ID of the extension to unload
     */
    fun unloadExtension(extensionId: String) {
        try {
            val fileHash = extensionCache.entries
                .find { it.value.id == extensionId }
                ?.key
            
            if (fileHash != null) {
                extensionCache.remove(fileHash)
                loadedClassLoaders.remove(fileHash)
                Timber.i("Unloaded extension: $extensionId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error unloading extension: $extensionId")
        }
    }
    
    /**
     * Get all currently loaded extensions.
     */
    fun getLoadedExtensions(): List<MusicExtension> {
        return extensionCache.values.toList()
    }
    
    /**
     * Clear all loaded extensions and clean up resources.
     */
    fun clearAll() {
        extensionCache.clear()
        loadedClassLoaders.clear()
        Timber.i("Cleared all loaded extensions")
    }
    
    /**
     * Validate an extension file before loading.
     */
    private fun validateExtensionFile(file: File): ExtensionResult<Unit> {
        if (!file.exists()) {
            return ExtensionResult.Error(
                ExtensionException.NotFoundError("Extension file not found: ${file.name}")
            )
        }
        
        if (!file.canRead()) {
            return ExtensionResult.Error(
                ExtensionException.ConfigurationError("Cannot read extension file: ${file.name}")
            )
        }
        
        val extension = file.extension.lowercase()
        if (extension !in listOf("apk", "jar", "zip")) {
            return ExtensionResult.Error(
                ExtensionException.ConfigurationError(
                    "Unsupported file type: $extension. Supported types: APK, JAR, ZIP"
                )
            )
        }
        
        // Basic file size check (max 50MB)
        if (file.length() > 50 * 1024 * 1024) {
            return ExtensionResult.Error(
                ExtensionException.ConfigurationError(
                    "Extension file too large: ${file.length() / (1024 * 1024)}MB. Maximum allowed: 50MB"
                )
            )
        }
        
        return ExtensionResult.Success(Unit)
    }
    
    /**
     * Create a DexClassLoader for the extension file.
     */
    private fun createClassLoader(extensionFile: File): DexClassLoader {
        val dexOutputDir = File(context.codeCacheDir, "extensions")
        if (!dexOutputDir.exists()) {
            dexOutputDir.mkdirs()
        }
        
        return DexClassLoader(
            extensionFile.absolutePath,
            dexOutputDir.absolutePath,
            null,
            context.classLoader
        )
    }
    
    /**
     * Find the MusicExtension implementation class in the loaded file.
     */
    private fun findExtensionClass(
        classLoader: DexClassLoader,
        extensionFile: File,
        expectedClassName: String?
    ): Class<*>? {
        try {
            // If specific class name is provided, try to load it directly
            expectedClassName?.let { className ->
                return try {
                    val clazz = classLoader.loadClass(className)
                    if (isMusicExtension(clazz)) clazz else null
                } catch (e: ClassNotFoundException) {
                    Timber.w("Expected class not found: $className")
                    null
                }
            }
            
            // Search for MusicExtension implementations
            return findExtensionClassInJar(classLoader, extensionFile)
            
        } catch (e: Exception) {
            Timber.e(e, "Error finding extension class")
            return null
        }
    }
    
    /**
     * Search for MusicExtension implementations in a JAR/APK file.
     */
    private fun findExtensionClassInJar(
        classLoader: DexClassLoader,
        extensionFile: File
    ): Class<*>? {
        try {
            JarFile(extensionFile).use { jarFile ->
                val entries = jarFile.entries()
                
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    
                    // Look for .class files
                    if (name.endsWith(".class") && !name.contains("$")) {
                        val className = name.replace("/", ".")
                            .removeSuffix(".class")
                        
                        try {
                            val clazz = classLoader.loadClass(className)
                            if (isMusicExtension(clazz)) {
                                Timber.d("Found MusicExtension implementation: $className")
                                return clazz
                            }
                        } catch (e: Exception) {
                            // Ignore classes that can't be loaded
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching for extension class in JAR")
        }
        
        return null
    }
    
    /**
     * Check if a class implements MusicExtension.
     */
    private fun isMusicExtension(clazz: Class<*>): Boolean {
        return try {
            MusicExtension::class.java.isAssignableFrom(clazz) &&
                    !clazz.isInterface &&
                    !java.lang.reflect.Modifier.isAbstract(clazz.modifiers)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create an instance of the extension class.
     */
    private fun createExtensionInstance(extensionClass: Class<*>): MusicExtension {
        return try {
            val constructor = extensionClass.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance() as MusicExtension
        } catch (e: Exception) {
            throw ExtensionException.GenericError(
                "Failed to create extension instance: ${e.message}",
                e.javaClass.simpleName
            )
        }
    }
    
    /**
     * Validate a loaded extension.
     */
    private fun validateExtension(extension: MusicExtension): ExtensionException? {
        // Validate extension ID
        if (!ExtensionApi.isValidExtensionId(extension.id)) {
            return ExtensionException.ConfigurationError(
                "Invalid extension ID: ${extension.id}. Must follow reverse domain naming convention."
            )
        }
        
        // Check API compatibility
        if (!ExtensionApi.isCompatible(extension)) {
            return ExtensionException.ConfigurationError(
                ExtensionApi.getCompatibilityMessage(extension) ?: "Extension is not compatible"
            )
        }
        
        // Validate required fields
        if (extension.name.isBlank()) {
            return ExtensionException.ConfigurationError("Extension name cannot be empty")
        }
        
        if (extension.developer.isBlank()) {
            return ExtensionException.ConfigurationError("Extension developer cannot be empty")
        }
        
        if (extension.version <= 0) {
            return ExtensionException.ConfigurationError("Extension version must be positive")
        }
        
        return null
    }
    
    /**
     * Calculate SHA-256 hash of a file for caching and integrity checking.
     */
    private fun calculateFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating file hash")
            file.absolutePath.hashCode().toString()
        }
    }
} 