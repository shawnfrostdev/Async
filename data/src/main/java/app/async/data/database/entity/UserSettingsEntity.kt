package app.async.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing user settings and preferences
 */
@Entity(
    tableName = "user_settings",
    indices = [
        Index(value = ["category", "key"], unique = true),
        Index(value = ["category"]),
        Index(value = ["last_modified"])
    ]
)
data class UserSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "category")
    val category: String, // e.g., "app", "playback", "extension", "ui"
    
    @ColumnInfo(name = "key")
    val key: String, // setting key
    
    @ColumnInfo(name = "value")
    val value: String, // JSON string value
    
    @ColumnInfo(name = "value_type")
    val valueType: String, // "string", "int", "boolean", "float", "json"
    
    @ColumnInfo(name = "extension_id")
    val extensionId: String? = null, // for extension-specific settings
    
    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean = false, // for sensitive settings
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false, // for cloud sync
    
    @ColumnInfo(name = "date_created")
    val dateCreated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "description")
    val description: String? = null // human-readable description
) 
