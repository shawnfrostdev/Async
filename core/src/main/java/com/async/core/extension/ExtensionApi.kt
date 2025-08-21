package com.async.core.extension

/**
 * Constants and utilities for the Extension API system
 */
object ExtensionApi {
    
    /**
     * Current API version that the core app supports.
     * Extensions can check this to ensure compatibility.
     */
    const val CURRENT_API_VERSION = 1
    
    /**
     * Minimum API version supported by the core app.
     * Extensions requiring older API versions will not be loaded.
     */
    const val MIN_SUPPORTED_API_VERSION = 1
    
    /**
     * Maximum API version supported by the core app.
     * Extensions requiring newer API versions will not be loaded.
     */
    const val MAX_SUPPORTED_API_VERSION = 1
    
    /**
     * Standard file extensions for extension packages
     */
    object FileExtensions {
        const val APK = ".apk"
        const val JAR = ".jar"
        const val ZIP = ".zip"
    }
    
    /**
     * Standard permissions that extensions can request
     */
    object Permissions {
        const val NETWORK_ACCESS = "android.permission.INTERNET"
        const val STORAGE_ACCESS = "android.permission.READ_EXTERNAL_STORAGE"
        const val WRITE_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"
    }
    
    /**
     * Standard configuration keys that extensions might use
     */
    object ConfigKeys {
        const val API_KEY = "api_key"
        const val BASE_URL = "base_url"
        const val USER_AGENT = "user_agent"
        const val TIMEOUT = "timeout"
        const val RATE_LIMIT = "rate_limit"
        const val QUALITY_PREFERENCE = "quality_preference"
        const val LANGUAGE = "language"
        const val REGION = "region"
    }
    
    /**
     * Standard metadata keys for additional information
     */
    object MetadataKeys {
        const val GENRE = "genre"
        const val RELEASE_DATE = "release_date"
        const val TRACK_NUMBER = "track_number"
        const val DISC_NUMBER = "disc_number"
        const val BITRATE = "bitrate"
        const val SAMPLE_RATE = "sample_rate"
        const val FILE_FORMAT = "file_format"
        const val FILE_SIZE = "file_size"
        const val LYRICS = "lyrics"
        const val COMPOSER = "composer"
        const val PRODUCER = "producer"
        const val RECORD_LABEL = "record_label"
    }
    
    /**
     * Check if an extension is compatible with the current API version
     */
    fun isCompatible(extension: MusicExtension): Boolean {
        return extension.minApiLevel <= CURRENT_API_VERSION && 
               extension.maxApiLevel >= CURRENT_API_VERSION
    }
    
    /**
     * Check if an extension metadata is compatible with the current API version
     */
    fun isCompatible(metadata: ExtensionMetadata): Boolean {
        // For metadata, we assume compatibility since we don't store API levels there
        return true
    }
    
    /**
     * Get compatibility message for an incompatible extension
     */
    fun getCompatibilityMessage(extension: MusicExtension): String? {
        return when {
            extension.minApiLevel > CURRENT_API_VERSION -> {
                "Extension requires API level ${extension.minApiLevel} or higher. Current API level is $CURRENT_API_VERSION."
            }
            extension.maxApiLevel < CURRENT_API_VERSION -> {
                "Extension supports API level ${extension.maxApiLevel} or lower. Current API level is $CURRENT_API_VERSION."
            }
            else -> null
        }
    }
    
    /**
     * Validate extension ID format
     */
    fun isValidExtensionId(id: String): Boolean {
        // Extension IDs should follow reverse domain naming convention
        val regex = Regex("^[a-z][a-z0-9]*+(\\.[a-z][a-z0-9]*+)*+$")
        return id.matches(regex) && id.length >= 3 && id.length <= 100
    }
    
    /**
     * Sanitize extension ID to make it valid
     */
    fun sanitizeExtensionId(id: String): String {
        return id.lowercase()
            .replace(Regex("[^a-z0-9.]"), "")
            .replace(Regex("\\.+"), ".")
            .removePrefix(".")
            .removeSuffix(".")
            .take(100)
    }
} 