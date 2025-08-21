package com.async.domain.repository

import com.async.core.result.AsyncResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for settings and preferences management
 */
interface SettingsRepository {
    
    // ======== BASIC SETTINGS OPERATIONS ========
    
    /**
     * Get a string setting value
     */
    suspend fun getString(category: String, key: String, defaultValue: String = ""): String
    
    /**
     * Set a string setting value
     */
    suspend fun setString(category: String, key: String, value: String)
    
    /**
     * Get an integer setting value
     */
    suspend fun getInt(category: String, key: String, defaultValue: Int = 0): Int
    
    /**
     * Set an integer setting value
     */
    suspend fun setInt(category: String, key: String, value: Int)
    
    /**
     * Get a boolean setting value
     */
    suspend fun getBoolean(category: String, key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Set a boolean setting value
     */
    suspend fun setBoolean(category: String, key: String, value: Boolean)
    
    /**
     * Get a float setting value
     */
    suspend fun getFloat(category: String, key: String, defaultValue: Float = 0f): Float
    
    /**
     * Set a float setting value
     */
    suspend fun setFloat(category: String, key: String, value: Float)
    
    // ======== REACTIVE SETTINGS ========
    
    /**
     * Observe a setting value as Flow
     */
    fun observeString(category: String, key: String, defaultValue: String = ""): Flow<String>
    
    /**
     * Observe an integer setting as Flow
     */
    fun observeInt(category: String, key: String, defaultValue: Int = 0): Flow<Int>
    
    /**
     * Observe a boolean setting as Flow
     */
    fun observeBoolean(category: String, key: String, defaultValue: Boolean = false): Flow<Boolean>
    
    /**
     * Observe a float setting as Flow
     */
    fun observeFloat(category: String, key: String, defaultValue: Float = 0f): Flow<Float>
    
    // ======== APP SETTINGS ========
    
    /**
     * Get app theme setting
     */
    suspend fun getTheme(): AppTheme
    
    /**
     * Set app theme
     */
    suspend fun setTheme(theme: AppTheme)
    
    /**
     * Observe theme changes
     */
    fun observeTheme(): Flow<AppTheme>
    
    /**
     * Get app language
     */
    suspend fun getLanguage(): String
    
    /**
     * Set app language
     */
    suspend fun setLanguage(language: String)
    
    /**
     * Check if it's first app launch
     */
    suspend fun isFirstLaunch(): Boolean
    
    /**
     * Mark first launch as completed
     */
    suspend fun setFirstLaunchCompleted()
    
    // ======== PLAYBACK SETTINGS ========
    
    /**
     * Get repeat mode
     */
    suspend fun getRepeatMode(): RepeatMode
    
    /**
     * Set repeat mode
     */
    suspend fun setRepeatMode(mode: RepeatMode)
    
    /**
     * Observe repeat mode changes
     */
    fun observeRepeatMode(): Flow<RepeatMode>
    
    /**
     * Check if shuffle is enabled
     */
    suspend fun isShuffleEnabled(): Boolean
    
    /**
     * Set shuffle enabled state
     */
    suspend fun setShuffleEnabled(enabled: Boolean)
    
    /**
     * Observe shuffle state changes
     */
    fun observeShuffleEnabled(): Flow<Boolean>
    
    /**
     * Get crossfade duration in seconds
     */
    suspend fun getCrossfadeDuration(): Int
    
    /**
     * Set crossfade duration
     */
    suspend fun setCrossfadeDuration(seconds: Int)
    
    /**
     * Get audio quality preference
     */
    suspend fun getAudioQuality(): AudioQuality
    
    /**
     * Set audio quality preference
     */
    suspend fun setAudioQuality(quality: AudioQuality)
    
    // ======== UI SETTINGS ========
    
    /**
     * Check if album art should be shown
     */
    suspend fun shouldShowAlbumArt(): Boolean
    
    /**
     * Set album art visibility
     */
    suspend fun setShowAlbumArt(show: Boolean)
    
    /**
     * Check if compact player UI is enabled
     */
    suspend fun isCompactPlayerEnabled(): Boolean
    
    /**
     * Set compact player state
     */
    suspend fun setCompactPlayerEnabled(enabled: Boolean)
    
    /**
     * Get grid columns count
     */
    suspend fun getGridColumns(): Int
    
    /**
     * Set grid columns count
     */
    suspend fun setGridColumns(columns: Int)
    
    // ======== EXTENSION SETTINGS ========
    
    /**
     * Get extension-specific setting
     */
    suspend fun getExtensionSetting(
        extensionId: String, 
        key: String, 
        defaultValue: String = ""
    ): String
    
    /**
     * Set extension-specific setting
     */
    suspend fun setExtensionSetting(extensionId: String, key: String, value: String)
    
    /**
     * Get all settings for an extension
     */
    suspend fun getExtensionSettings(extensionId: String): Map<String, String>
    
    /**
     * Remove all settings for an extension
     */
    suspend fun clearExtensionSettings(extensionId: String)
    
    /**
     * Observe extension setting changes
     */
    fun observeExtensionSetting(
        extensionId: String, 
        key: String, 
        defaultValue: String = ""
    ): Flow<String>
    
    // ======== CATEGORY OPERATIONS ========
    
    /**
     * Get all settings in a category
     */
    suspend fun getCategorySettings(category: String): Map<String, String>
    
    /**
     * Clear all settings in a category
     */
    suspend fun clearCategory(category: String)
    
    /**
     * Get all available categories
     */
    suspend fun getAllCategories(): List<String>
    
    // ======== BACKUP & RESTORE ========
    
    /**
     * Export all settings as JSON
     */
    suspend fun exportSettings(): AsyncResult<String, SettingsError>
    
    /**
     * Import settings from JSON
     */
    suspend fun importSettings(jsonData: String): AsyncResult<Unit, SettingsError>
    
    /**
     * Reset all settings to defaults
     */
    suspend fun resetToDefaults(): AsyncResult<Unit, SettingsError>
    
    /**
     * Reset category to defaults
     */
    suspend fun resetCategoryToDefaults(category: String): AsyncResult<Unit, SettingsError>
    
    // ======== VALIDATION & MAINTENANCE ========
    
    /**
     * Validate settings integrity
     */
    suspend fun validateSettings(): List<SettingsError>
    
    /**
     * Cleanup orphaned settings
     */
    suspend fun cleanupOrphanedSettings()
    
    /**
     * Get settings statistics
     */
    suspend fun getSettingsStats(): SettingsStats
}

/**
 * App theme enumeration
 */
enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

/**
 * Repeat mode enumeration
 */
enum class RepeatMode {
    OFF, ONE, ALL
}

/**
 * Audio quality enumeration
 */
enum class AudioQuality {
    LOW, MEDIUM, HIGH, LOSSLESS
}

/**
 * Settings error types
 */
sealed class SettingsError {
    object DatabaseError : SettingsError()
    object ExportError : SettingsError()
    object ImportError : SettingsError()
    object ValidationError : SettingsError()
    data class InvalidValue(val key: String, val value: String) : SettingsError()
    data class CategoryNotFound(val category: String) : SettingsError()
    data class CorruptedData(val message: String) : SettingsError()
}

/**
 * Settings statistics
 */
data class SettingsStats(
    val totalSettings: Int,
    val categoriesCount: Int,
    val extensionSettings: Int,
    val lastModified: Long,
    val storageSize: Long // in bytes
)

/**
 * Settings constants for common categories and keys
 */
object SettingsConstants {
    
    // Categories
    const val CATEGORY_APP = "app"
    const val CATEGORY_PLAYBACK = "playback"
    const val CATEGORY_UI = "ui"
    const val CATEGORY_EXTENSION = "extension"
    
    // App settings keys
    const val KEY_THEME = "theme"
    const val KEY_LANGUAGE = "language"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    const val KEY_CRASH_REPORTING = "crash_reporting_enabled"
    
    // Playback settings keys
    const val KEY_REPEAT_MODE = "repeat_mode"
    const val KEY_SHUFFLE_ENABLED = "shuffle_enabled"
    const val KEY_CROSSFADE_DURATION = "crossfade_duration"
    const val KEY_REPLAY_GAIN = "replay_gain_enabled"
    const val KEY_AUDIO_QUALITY = "audio_quality"
    const val KEY_BUFFER_SIZE = "buffer_size"
    const val KEY_SKIP_SILENCE = "skip_silence"
    const val KEY_VOLUME_NORMALIZATION = "volume_normalization"
    
    // UI settings keys
    const val KEY_SHOW_ALBUM_ART = "show_album_art"
    const val KEY_COMPACT_PLAYER = "compact_player"
    const val KEY_SHOW_VISUALIZER = "show_visualizer"
    const val KEY_GRID_COLUMNS = "grid_columns"
    const val KEY_LIST_ITEM_SIZE = "list_item_size"
    
    // Default values
    const val DEFAULT_THEME = "dark"
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_REPEAT_MODE = "off"
    const val DEFAULT_CROSSFADE_DURATION = 0
    const val DEFAULT_AUDIO_QUALITY = "high"
    const val DEFAULT_GRID_COLUMNS = 2
} 