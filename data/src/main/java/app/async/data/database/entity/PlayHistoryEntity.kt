package app.async.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing playback history entries
 */
@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["track_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["completion_percentage"]),
        Index(value = ["timestamp", "track_id"])
    ]
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "duration_played")
    val durationPlayed: Long, // in milliseconds
    
    @ColumnInfo(name = "completion_percentage")
    val completionPercentage: Float, // 0.0 to 1.0
    
    @ColumnInfo(name = "source")
    val source: String? = null, // e.g., "playlist", "search", "recommendation"
    
    @ColumnInfo(name = "source_id")
    val sourceId: String? = null, // ID of the source (playlist ID, search query, etc.)
    
    @ColumnInfo(name = "session_id")
    val sessionId: String? = null, // for grouping related plays
    
    @ColumnInfo(name = "device_info")
    val deviceInfo: String? = null // JSON string with device information
) 
