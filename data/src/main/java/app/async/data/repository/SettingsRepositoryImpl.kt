package app.async.data.repository

import app.async.core.result.AsyncResult
import app.async.data.database.dao.UserSettingsDao
import app.async.data.database.entity.UserSettingsEntity
import app.async.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import logcat.logcat

/**
 * Implementation of SettingsRepository
 * Manages user settings and app configuration
 */
class SettingsRepositoryImpl(
    private val userSettingsDao: UserSettingsDao
) : SettingsRepository {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    // ======== BASIC SETTINGS OPERATIONS ========

    override suspend fun getString(category: String, key: String, defaultValue: String): String {
        return try {
            userSettingsDao.getStringValue(category, key, defaultValue)
        } catch (e: Exception) {
            logcat { "Error getting string setting $category:$key - ${e.message}" }
            defaultValue
        }
    }

    override suspend fun setString(category: String, key: String, value: String) {
        try {
            userSettingsDao.setStringValue(category, key, value)
            logcat { "Successfully set string setting $category:$key" }
        } catch (e: Exception) {
            logcat { "Error setting string $category:$key - ${e.message}" }
        }
    }

    override suspend fun getInt(category: String, key: String, defaultValue: Int): Int {
        return try {
            userSettingsDao.getIntValue(category, key, defaultValue)
        } catch (e: Exception) {
            logcat { "Error getting int setting $category:$key - ${e.message}" }
            defaultValue
        }
    }

    override suspend fun setInt(category: String, key: String, value: Int) {
        try {
            userSettingsDao.setIntValue(category, key, value)
        } catch (e: Exception) {
            logcat { "Error setting int $category:$key - ${e.message}" }
        }
    }

    override suspend fun getBoolean(category: String, key: String, defaultValue: Boolean): Boolean {
        return try {
            userSettingsDao.getBooleanValue(category, key, defaultValue)
        } catch (e: Exception) {
            logcat { "Error getting boolean setting $category:$key - ${e.message}" }
            defaultValue
        }
    }

    override suspend fun setBoolean(category: String, key: String, value: Boolean) {
        try {
            userSettingsDao.setBooleanValue(category, key, value)
        } catch (e: Exception) {
            logcat { "Error setting boolean $category:$key - ${e.message}" }
        }
    }

    override suspend fun getFloat(category: String, key: String, defaultValue: Float): Float {
        return try {
            userSettingsDao.getFloatValue(category, key, defaultValue)
        } catch (e: Exception) {
            logcat { "Error getting float setting $category:$key - ${e.message}" }
            defaultValue
        }
    }

    override suspend fun setFloat(category: String, key: String, value: Float) {
        try {
            userSettingsDao.setFloatValue(category, key, value)
        } catch (e: Exception) {
            logcat { "Error setting float $category:$key - ${e.message}" }
        }
    }

    // ======== REACTIVE SETTINGS ========

    override fun observeString(category: String, key: String, defaultValue: String): Flow<String> {
        return userSettingsDao.getSettingValueFlow(category, key)
            .map { value -> value ?: defaultValue }
    }

    override fun observeInt(category: String, key: String, defaultValue: Int): Flow<Int> {
        return userSettingsDao.getSettingValueFlow(category, key)
            .map { value -> value?.toIntOrNull() ?: defaultValue }
    }

    override fun observeBoolean(category: String, key: String, defaultValue: Boolean): Flow<Boolean> {
        return userSettingsDao.getSettingValueFlow(category, key)
            .map { value -> value?.toBooleanStrictOrNull() ?: defaultValue }
    }

    override fun observeFloat(category: String, key: String, defaultValue: Float): Flow<Float> {
        return userSettingsDao.getSettingValueFlow(category, key)
            .map { value -> value?.toFloatOrNull() ?: defaultValue }
    }

    // ======== APP SETTINGS ========

    override suspend fun getTheme(): AppTheme {
        val themeString = getString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_THEME, SettingsConstants.DEFAULT_THEME)
        return when (themeString.lowercase()) {
            "light" -> AppTheme.LIGHT
            "dark" -> AppTheme.DARK
            "system" -> AppTheme.SYSTEM
            else -> AppTheme.DARK
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        setString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_THEME, theme.name.lowercase())
    }

    override fun observeTheme(): Flow<AppTheme> {
        return observeString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_THEME, SettingsConstants.DEFAULT_THEME)
            .map { themeString ->
                when (themeString.lowercase()) {
                    "light" -> AppTheme.LIGHT
                    "dark" -> AppTheme.DARK
                    "system" -> AppTheme.SYSTEM
                    else -> AppTheme.DARK
                }
            }
    }

    override suspend fun getLanguage(): String {
        return getString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_LANGUAGE, SettingsConstants.DEFAULT_LANGUAGE)
    }

    override suspend fun setLanguage(language: String) {
        setString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_LANGUAGE, language)
    }

    override suspend fun isFirstLaunch(): Boolean {
        return getBoolean(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_FIRST_LAUNCH, true)
    }

    override suspend fun setFirstLaunchCompleted() {
        setBoolean(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_FIRST_LAUNCH, false)
    }

    // ======== PLAYBACK SETTINGS ========

    override suspend fun getRepeatMode(): RepeatMode {
        val modeString = getString(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_REPEAT_MODE, SettingsConstants.DEFAULT_REPEAT_MODE)
        return when (modeString.lowercase()) {
            "off" -> RepeatMode.OFF
            "one" -> RepeatMode.ONE
            "all" -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        setString(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_REPEAT_MODE, mode.name.lowercase())
    }

    override fun observeRepeatMode(): Flow<RepeatMode> {
        return observeString(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_REPEAT_MODE, SettingsConstants.DEFAULT_REPEAT_MODE)
            .map { modeString ->
                when (modeString.lowercase()) {
                    "off" -> RepeatMode.OFF
                    "one" -> RepeatMode.ONE
                    "all" -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            }
    }

    override suspend fun isShuffleEnabled(): Boolean {
        return getBoolean(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_SHUFFLE_ENABLED, false)
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        setBoolean(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_SHUFFLE_ENABLED, enabled)
    }

    override fun observeShuffleEnabled(): Flow<Boolean> {
        return observeBoolean(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_SHUFFLE_ENABLED, false)
    }

    override suspend fun getCrossfadeDuration(): Int {
        return getInt(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_CROSSFADE_DURATION, SettingsConstants.DEFAULT_CROSSFADE_DURATION)
    }

    override suspend fun setCrossfadeDuration(seconds: Int) {
        setInt(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_CROSSFADE_DURATION, seconds)
    }

    override suspend fun getAudioQuality(): AudioQuality {
        val qualityString = getString(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_AUDIO_QUALITY, SettingsConstants.DEFAULT_AUDIO_QUALITY)
        return when (qualityString.lowercase()) {
            "low" -> AudioQuality.LOW
            "medium" -> AudioQuality.MEDIUM
            "high" -> AudioQuality.HIGH
            "lossless" -> AudioQuality.LOSSLESS
            else -> AudioQuality.HIGH
        }
    }

    override suspend fun setAudioQuality(quality: AudioQuality) {
        setString(SettingsConstants.CATEGORY_PLAYBACK, SettingsConstants.KEY_AUDIO_QUALITY, quality.name.lowercase())
    }

    // ======== UI SETTINGS ========

    override suspend fun shouldShowAlbumArt(): Boolean {
        return getBoolean(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_SHOW_ALBUM_ART, true)
    }

    override suspend fun setShowAlbumArt(show: Boolean) {
        setBoolean(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_SHOW_ALBUM_ART, show)
    }

    override suspend fun isCompactPlayerEnabled(): Boolean {
        return getBoolean(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_COMPACT_PLAYER, false)
    }

    override suspend fun setCompactPlayerEnabled(enabled: Boolean) {
        setBoolean(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_COMPACT_PLAYER, enabled)
    }

    override suspend fun getGridColumns(): Int {
        return getInt(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_GRID_COLUMNS, SettingsConstants.DEFAULT_GRID_COLUMNS)
    }

    override suspend fun setGridColumns(columns: Int) {
        setInt(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_GRID_COLUMNS, columns)
    }

    // ======== EXTENSION SETTINGS ========

    override suspend fun getExtensionSetting(extensionId: String, key: String, defaultValue: String): String {
        return try {
            val entity = userSettingsDao.getSetting("extension", key)
            if (entity?.extensionId == extensionId) {
                entity.value
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            logcat { "Error getting extension setting $extensionId:$key - ${e.message}" }
            defaultValue
        }
    }

    override suspend fun setExtensionSetting(extensionId: String, key: String, value: String) {
        try {
            userSettingsDao.setExtensionSetting(extensionId, key, value)
        } catch (e: Exception) {
            logcat { "Error setting extension setting $extensionId:$key - ${e.message}" }
        }
    }

    override suspend fun getExtensionSettings(extensionId: String): Map<String, String> {
        return try {
            val entities = userSettingsDao.getExtensionSettings(extensionId).first()
            entities.associate { it.key to it.value }
        } catch (e: Exception) {
            logcat { "Error getting extension settings for $extensionId - ${e.message}" }
            emptyMap()
        }
    }

    override suspend fun clearExtensionSettings(extensionId: String) {
        try {
            userSettingsDao.deleteExtensionSettings(extensionId)
            logcat { "Successfully cleared extension settings for: $extensionId" }
        } catch (e: Exception) {
            logcat { "Error clearing extension settings for $extensionId - ${e.message}" }
        }
    }

    override fun observeExtensionSetting(extensionId: String, key: String, defaultValue: String): Flow<String> {
        return userSettingsDao.getExtensionSettings(extensionId)
            .map { entities ->
                entities.find { it.key == key }?.value ?: defaultValue
            }
    }

    // ======== CATEGORY OPERATIONS ========

    override suspend fun getCategorySettings(category: String): Map<String, String> {
        return try {
            val entities = userSettingsDao.getSettingsByCategory(category).first()
            entities.associate { it.key to it.value }
        } catch (e: Exception) {
            logcat { "Error getting category settings for $category - ${e.message}" }
            emptyMap()
        }
    }

    override suspend fun clearCategory(category: String) {
        try {
            userSettingsDao.deleteSettingsByCategory(category)
            logcat { "Successfully cleared category: $category" }
        } catch (e: Exception) {
            logcat { "Error clearing category $category - ${e.message}" }
        }
    }

    override suspend fun getAllCategories(): List<String> {
        return try {
            userSettingsDao.getAllCategories()
        } catch (e: Exception) {
            logcat { "Error getting all categories - ${e.message}" }
            emptyList()
        }
    }

    // ======== BACKUP & RESTORE ========

    override suspend fun exportSettings(): AsyncResult<String, SettingsError> {
        return try {
            val allSettings = userSettingsDao.getSettingsForBackup()
            val settingsMap = allSettings.groupBy { it.category }
                .mapValues { (_, entities) ->
                    entities.associate { it.key to it.value }
                }
            
            val jsonString = json.encodeToString(settingsMap)
            logcat { "Successfully exported settings" }
            AsyncResult.success(jsonString)
        } catch (e: Exception) {
            logcat { "Error exporting settings - ${e.message}" }
            AsyncResult.error(SettingsError.ExportError)
        }
    }

    override suspend fun importSettings(jsonData: String): AsyncResult<Unit, SettingsError> {
        return try {
            val settings = json.decodeFromString<Map<String, Map<String, String>>>(jsonData)
            
            for ((category, categorySettings) in settings) {
                for ((key, value) in categorySettings) {
                    setString(category, key, value)
                }
            }
            
            logcat { "Successfully imported settings" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error importing settings - ${e.message}" }
            AsyncResult.error(SettingsError.ImportError)
        }
    }

    override suspend fun resetToDefaults(): AsyncResult<Unit, SettingsError> {
        return try {
            userSettingsDao.deleteAllSettings()
            
            // Set default values
            setTheme(AppTheme.DARK)
            setLanguage(SettingsConstants.DEFAULT_LANGUAGE)
            setRepeatMode(RepeatMode.OFF)
            setCrossfadeDuration(SettingsConstants.DEFAULT_CROSSFADE_DURATION)
            setAudioQuality(AudioQuality.HIGH)
            setGridColumns(SettingsConstants.DEFAULT_GRID_COLUMNS)
            
            logcat { "Successfully reset all settings to defaults" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error resetting to defaults - ${e.message}" }
            AsyncResult.error(SettingsError.DatabaseError)
        }
    }

    override suspend fun resetCategoryToDefaults(category: String): AsyncResult<Unit, SettingsError> {
        return try {
            clearCategory(category)
            
            // Set category-specific defaults
            when (category) {
                SettingsConstants.CATEGORY_APP -> {
                    setTheme(AppTheme.DARK)
                    setLanguage(SettingsConstants.DEFAULT_LANGUAGE)
                }
                SettingsConstants.CATEGORY_PLAYBACK -> {
                    setRepeatMode(RepeatMode.OFF)
                    setCrossfadeDuration(SettingsConstants.DEFAULT_CROSSFADE_DURATION)
                    setAudioQuality(AudioQuality.HIGH)
                }
                SettingsConstants.CATEGORY_UI -> {
                    setGridColumns(SettingsConstants.DEFAULT_GRID_COLUMNS)
                    setShowAlbumArt(true)
                    setCompactPlayerEnabled(false)
                }
            }
            
            logcat { "Successfully reset category $category to defaults" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error resetting category $category to defaults - ${e.message}" }
            AsyncResult.error(SettingsError.DatabaseError)
        }
    }

    // ======== VALIDATION & MAINTENANCE ========

    override suspend fun validateSettings(): List<SettingsError> {
        val errors = mutableListOf<SettingsError>()
        
        try {
            // Validate theme setting
            val theme = getString(SettingsConstants.CATEGORY_APP, SettingsConstants.KEY_THEME, "")
            if (theme.isNotEmpty() && !listOf("light", "dark", "system").contains(theme.lowercase())) {
                errors.add(SettingsError.InvalidValue(SettingsConstants.KEY_THEME, theme))
            }
            
            // Validate grid columns
            val gridColumns = getInt(SettingsConstants.CATEGORY_UI, SettingsConstants.KEY_GRID_COLUMNS, 2)
            if (gridColumns < 1 || gridColumns > 10) {
                errors.add(SettingsError.InvalidValue(SettingsConstants.KEY_GRID_COLUMNS, gridColumns.toString()))
            }
            
            // Validate crossfade duration
            val crossfade = getCrossfadeDuration()
            if (crossfade < 0 || crossfade > 30) {
                errors.add(SettingsError.InvalidValue(SettingsConstants.KEY_CROSSFADE_DURATION, crossfade.toString()))
            }
            
        } catch (e: Exception) {
            logcat { "Error validating settings - ${e.message}" }
            errors.add(SettingsError.ValidationError)
        }
        
        return errors
    }

    override suspend fun cleanupOrphanedSettings() {
        try {
            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            userSettingsDao.cleanupOldNonCriticalSettings(oneWeekAgo)
            logcat { "Cleanup orphaned settings completed" }
        } catch (e: Exception) {
            logcat { "Error cleaning up orphaned settings - ${e.message}" }
        }
    }

    override suspend fun getSettingsStats(): SettingsStats {
        return try {
            val totalSettings = userSettingsDao.getTotalSettingsCount()
            val categories = userSettingsDao.getAllCategories()
            val extensionSettings = userSettingsDao.getExtensionSettingsCount()
            
            SettingsStats(
                totalSettings = totalSettings,
                categoriesCount = categories.size,
                extensionSettings = extensionSettings,
                lastModified = System.currentTimeMillis(),
                storageSize = 0L // Would need proper calculation
            )
        } catch (e: Exception) {
            logcat { "Error getting settings stats - ${e.message}" }
            SettingsStats(0, 0, 0, 0L, 0L)
        }
    }
} 
