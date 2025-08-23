package com.async.extensions.storage

import android.content.Context
import android.content.SharedPreferences
import com.async.core.extension.ExtensionInfo
import com.async.core.extension.ExtensionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logcat.logcat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles persistence of extension information and settings.
 * Uses SharedPreferences for lightweight storage of extension metadata.
 */
@Singleton
class ExtensionStorage @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "async_extensions"
        private const val KEY_EXTENSION_PREFIX = "ext_"
        private const val KEY_EXTENSION_CONFIG_PREFIX = "config_"
    }
    
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Save extension information.
     */
    suspend fun saveExtensionInfo(extensionId: String, extensionInfo: ExtensionInfo) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(extensionInfo)
                preferences.edit()
                    .putString(KEY_EXTENSION_PREFIX + extensionId, jsonString)
                    .apply()
                
                logcat { "Saved extension info: $extensionId" }
            } catch (e: Exception) {
                logcat { "Failed to save extension info: $extensionId - ${e.message}" }
            }
        }
    }
    
    /**
     * Load extension information by ID.
     */
    suspend fun getExtensionInfo(extensionId: String): ExtensionInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = preferences.getString(KEY_EXTENSION_PREFIX + extensionId, null)
                    ?: return@withContext null
                
                json.decodeFromString<ExtensionInfo>(jsonString)
            } catch (e: Exception) {
                logcat { "Failed to load extension info: $extensionId - ${e.message}" }
                null
            }
        }
    }
    
    /**
     * Get all saved extension information.
     */
    suspend fun getAllExtensionInfos(): Map<String, ExtensionInfo> {
        return withContext(Dispatchers.IO) {
            val result = mutableMapOf<String, ExtensionInfo>()
            
            try {
                val allPrefs = preferences.all
                
                for ((key, value) in allPrefs) {
                    if (key.startsWith(KEY_EXTENSION_PREFIX) && value is String) {
                        try {
                            val extensionId = key.removePrefix(KEY_EXTENSION_PREFIX)
                            val extensionInfo = json.decodeFromString<ExtensionInfo>(value)
                            result[extensionId] = extensionInfo
                        } catch (e: Exception) {
                            logcat { "Failed to parse extension info for key: $key - ${e.message}" }
                        }
                    }
                }
                
                logcat { "Loaded ${result.size} extension infos" }
            } catch (e: Exception) {
                logcat { "Failed to load extension infos - ${e.message}" }
            }
            
            result
        }
    }
    
    /**
     * Remove extension information.
     */
    suspend fun removeExtensionInfo(extensionId: String) {
        withContext(Dispatchers.IO) {
            try {
                preferences.edit()
                    .remove(KEY_EXTENSION_PREFIX + extensionId)
                    .remove(KEY_EXTENSION_CONFIG_PREFIX + extensionId)
                    .apply()
                
                logcat { "Removed extension info: $extensionId" }
            } catch (e: Exception) {
                logcat { "Failed to remove extension info: $extensionId - ${e.message}" }
            }
        }
    }
    
    /**
     * Save extension configuration.
     */
    suspend fun saveExtensionConfiguration(
        extensionId: String,
        configuration: Map<String, Any>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(configuration)
                preferences.edit()
                    .putString(KEY_EXTENSION_CONFIG_PREFIX + extensionId, jsonString)
                    .apply()
                
                logcat { "Saved extension configuration: $extensionId" }
            } catch (e: Exception) {
                logcat { "Failed to save extension configuration: $extensionId - ${e.message}" }
            }
        }
    }
    
    /**
     * Load extension configuration.
     */
    suspend fun getExtensionConfiguration(extensionId: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = preferences.getString(KEY_EXTENSION_CONFIG_PREFIX + extensionId, null)
                    ?: return@withContext emptyMap()
                
                json.decodeFromString<Map<String, Any>>(jsonString)
            } catch (e: Exception) {
                logcat { "Failed to load extension configuration: $extensionId - ${e.message}" }
                emptyMap()
            }
        }
    }
    
    /**
     * Update extension status only.
     */
    suspend fun updateExtensionStatus(extensionId: String, status: ExtensionStatus) {
        withContext(Dispatchers.IO) {
            try {
                val currentInfo = getExtensionInfo(extensionId) ?: return@withContext
                val updatedInfo = currentInfo.copy(status = status)
                saveExtensionInfo(extensionId, updatedInfo)
            } catch (e: Exception) {
                logcat { "Failed to update extension status: $extensionId - ${e.message}" }
            }
        }
    }
    
    /**
     * Clear all extension data.
     */
    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            try {
                val editor = preferences.edit()
                val allPrefs = preferences.all
                
                for (key in allPrefs.keys) {
                    if (key.startsWith(KEY_EXTENSION_PREFIX) || 
                        key.startsWith(KEY_EXTENSION_CONFIG_PREFIX)) {
                        editor.remove(key)
                    }
                }
                
                editor.apply()
                logcat { "Cleared all extension data" }
            } catch (e: Exception) {
                logcat { "Failed to clear extension data - ${e.message}" }
            }
        }
    }
    
    /**
     * Get extension statistics.
     */
    suspend fun getExtensionStats(): ExtensionStats {
        return withContext(Dispatchers.IO) {
            try {
                val allInfos = getAllExtensionInfos()
                val totalExtensions = allInfos.size
                val enabledExtensions = allInfos.values.count { it.isLoaded }
                val disabledExtensions = allInfos.values.count { 
                    it.status == ExtensionStatus.DISABLED 
                }
                val failedExtensions = allInfos.values.count { 
                    it.status == ExtensionStatus.INSTALL_FAILED || 
                    it.status == ExtensionStatus.UPDATE_FAILED ||
                    it.status == ExtensionStatus.CORRUPTED
                }
                val totalUsage = allInfos.values.sumOf { it.usageCount }
                
                ExtensionStats(
                    totalExtensions = totalExtensions,
                    enabledExtensions = enabledExtensions,
                    disabledExtensions = disabledExtensions,
                    failedExtensions = failedExtensions,
                    totalUsage = totalUsage
                )
            } catch (e: Exception) {
                logcat { "Failed to calculate extension stats - ${e.message}" }
                ExtensionStats()
            }
        }
    }
}

/**
 * Statistics about installed extensions.
 */
data class ExtensionStats(
    val totalExtensions: Int = 0,
    val enabledExtensions: Int = 0,
    val disabledExtensions: Int = 0,
    val failedExtensions: Int = 0,
    val totalUsage: Long = 0
) 