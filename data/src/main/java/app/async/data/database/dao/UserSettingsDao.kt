package app.async.data.database.dao

import androidx.room.*
import app.async.data.database.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Settings operations
 */
@Dao
interface UserSettingsDao {
    
    // ======== INSERT OPERATIONS ========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: UserSettingsEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<UserSettingsEntity>): List<Long>
    
    // ======== UPDATE OPERATIONS ========
    
    @Update
    suspend fun updateSetting(setting: UserSettingsEntity)
    
    @Query("UPDATE user_settings SET value = :value, last_modified = :timestamp WHERE category = :category AND key = :key")
    suspend fun updateSettingValue(category: String, key: String, value: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_settings SET is_synced = :synced WHERE category = :category AND key = :key")
    suspend fun updateSyncStatus(category: String, key: String, synced: Boolean)
    
    // ======== DELETE OPERATIONS ========
    
    @Delete
    suspend fun deleteSetting(setting: UserSettingsEntity)
    
    @Query("DELETE FROM user_settings WHERE id = :settingId")
    suspend fun deleteSettingById(settingId: Long)
    
    @Query("DELETE FROM user_settings WHERE category = :category AND key = :key")
    suspend fun deleteSetting(category: String, key: String)
    
    @Query("DELETE FROM user_settings WHERE category = :category")
    suspend fun deleteSettingsByCategory(category: String)
    
    @Query("DELETE FROM user_settings WHERE extension_id = :extensionId")
    suspend fun deleteExtensionSettings(extensionId: String)
    
    @Query("DELETE FROM user_settings")
    suspend fun deleteAllSettings()
    
    // ======== QUERY OPERATIONS ========
    
    @Query("SELECT * FROM user_settings WHERE id = :settingId")
    suspend fun getSettingById(settingId: Long): UserSettingsEntity?
    
    @Query("SELECT * FROM user_settings WHERE category = :category AND key = :key")
    suspend fun getSetting(category: String, key: String): UserSettingsEntity?
    
    @Query("SELECT * FROM user_settings WHERE category = :category AND key = :key")
    fun getSettingFlow(category: String, key: String): Flow<UserSettingsEntity?>
    
    @Query("SELECT value FROM user_settings WHERE category = :category AND key = :key")
    suspend fun getSettingValue(category: String, key: String): String?
    
    @Query("SELECT value FROM user_settings WHERE category = :category AND key = :key")
    fun getSettingValueFlow(category: String, key: String): Flow<String?>
    
    @Query("SELECT * FROM user_settings WHERE category = :category ORDER BY key ASC")
    fun getSettingsByCategory(category: String): Flow<List<UserSettingsEntity>>
    
    @Query("SELECT * FROM user_settings WHERE extension_id = :extensionId ORDER BY category ASC, key ASC")
    fun getExtensionSettings(extensionId: String): Flow<List<UserSettingsEntity>>
    
    @Query("SELECT * FROM user_settings ORDER BY category ASC, key ASC")
    fun getAllSettings(): Flow<List<UserSettingsEntity>>
    
    @Query("SELECT * FROM user_settings WHERE is_synced = 0 ORDER BY last_modified DESC")
    fun getUnsyncedSettings(): Flow<List<UserSettingsEntity>>
    
    @Query("SELECT * FROM user_settings WHERE is_encrypted = 1 ORDER BY category ASC, key ASC")
    fun getEncryptedSettings(): Flow<List<UserSettingsEntity>>
    
    // ======== CONVENIENCE METHODS ========
    
    /**
     * Get a string setting value with default
     */
    suspend fun getStringValue(category: String, key: String, defaultValue: String = ""): String {
        return getSettingValue(category, key) ?: defaultValue
    }
    
    /**
     * Get an integer setting value with default
     */
    suspend fun getIntValue(category: String, key: String, defaultValue: Int = 0): Int {
        return getSettingValue(category, key)?.toIntOrNull() ?: defaultValue
    }
    
    /**
     * Get a boolean setting value with default
     */
    suspend fun getBooleanValue(category: String, key: String, defaultValue: Boolean = false): Boolean {
        return getSettingValue(category, key)?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    /**
     * Get a float setting value with default
     */
    suspend fun getFloatValue(category: String, key: String, defaultValue: Float = 0f): Float {
        return getSettingValue(category, key)?.toFloatOrNull() ?: defaultValue
    }
    
    /**
     * Set a string setting value
     */
    suspend fun setStringValue(category: String, key: String, value: String, description: String? = null) {
        val setting = UserSettingsEntity(
            category = category,
            key = key,
            value = value,
            valueType = "string",
            description = description
        )
        insertSetting(setting)
    }
    
    /**
     * Set an integer setting value
     */
    suspend fun setIntValue(category: String, key: String, value: Int, description: String? = null) {
        val setting = UserSettingsEntity(
            category = category,
            key = key,
            value = value.toString(),
            valueType = "int",
            description = description
        )
        insertSetting(setting)
    }
    
    /**
     * Set a boolean setting value
     */
    suspend fun setBooleanValue(category: String, key: String, value: Boolean, description: String? = null) {
        val setting = UserSettingsEntity(
            category = category,
            key = key,
            value = value.toString(),
            valueType = "boolean",
            description = description
        )
        insertSetting(setting)
    }
    
    /**
     * Set a float setting value
     */
    suspend fun setFloatValue(category: String, key: String, value: Float, description: String? = null) {
        val setting = UserSettingsEntity(
            category = category,
            key = key,
            value = value.toString(),
            valueType = "float",
            description = description
        )
        insertSetting(setting)
    }
    
    /**
     * Set an extension setting value
     */
    suspend fun setExtensionSetting(
        extensionId: String,
        key: String,
        value: String,
        valueType: String = "string",
        description: String? = null
    ) {
        val setting = UserSettingsEntity(
            category = "extension",
            key = key,
            value = value,
            valueType = valueType,
            extensionId = extensionId,
            description = description
        )
        insertSetting(setting)
    }
    
    // ======== SEARCH OPERATIONS ========
    
    @Query("""
        SELECT * FROM user_settings 
        WHERE key LIKE '%' || :query || '%' 
           OR value LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY category ASC, key ASC
    """)
    fun searchSettings(query: String): Flow<List<UserSettingsEntity>>
    
    // ======== STATISTICS OPERATIONS ========
    
    @Query("SELECT COUNT(*) FROM user_settings")
    suspend fun getTotalSettingsCount(): Int
    
    @Query("SELECT COUNT(*) FROM user_settings WHERE category = :category")
    suspend fun getSettingsCountByCategory(category: String): Int
    
    @Query("SELECT COUNT(*) FROM user_settings WHERE extension_id IS NOT NULL")
    suspend fun getExtensionSettingsCount(): Int
    
    @Query("SELECT COUNT(*) FROM user_settings WHERE is_encrypted = 1")
    suspend fun getEncryptedSettingsCount(): Int
    
    @Query("SELECT DISTINCT category FROM user_settings ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT DISTINCT extension_id FROM user_settings WHERE extension_id IS NOT NULL ORDER BY extension_id ASC")
    suspend fun getExtensionsWithSettings(): List<String>
    
    // ======== MAINTENANCE OPERATIONS ========
    
    @Query("DELETE FROM user_settings WHERE last_modified < :cutoffTime AND category NOT IN ('app', 'playback')")
    suspend fun cleanupOldNonCriticalSettings(cutoffTime: Long)
    
    @Query("UPDATE user_settings SET is_synced = 0 WHERE category = :category")
    suspend fun markCategoryAsUnsynced(category: String)
    
    @Query("UPDATE user_settings SET is_synced = 1 WHERE is_synced = 0")
    suspend fun markAllAsSynced()
    
    // ======== BACKUP/RESTORE OPERATIONS ========
    
    @Query("SELECT * FROM user_settings WHERE category NOT LIKE 'temp_%' ORDER BY category ASC, key ASC")
    suspend fun getSettingsForBackup(): List<UserSettingsEntity>
    
    @Transaction
    suspend fun restoreSettings(settings: List<UserSettingsEntity>) {
        // Clear existing non-critical settings
        val criticalCategories = listOf("app", "playback")
        for (category in criticalCategories) {
            // Keep critical settings, only update if they exist in backup
        }
        
        // Insert restored settings
        insertSettings(settings)
    }
} 
