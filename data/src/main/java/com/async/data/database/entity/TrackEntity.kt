package com.async.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a music track in the database
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["extension_id", "external_id"], unique = true),
        Index(value = ["title", "artist"]),
        Index(value = ["extension_id"]),
        Index(value = ["date_added"])
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "external_id")
    val externalId: String,
    
    @ColumnInfo(name = "extension_id")
    val extensionId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist")
    val artist: String?,
    
    @ColumnInfo(name = "album")
    val album: String?,
    
    @ColumnInfo(name = "duration")
    val duration: Long?, // in milliseconds
    
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    
    @ColumnInfo(name = "stream_url")
    val streamUrl: String?,
    
    @ColumnInfo(name = "metadata")
    val metadata: String?, // JSON string of additional metadata
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean = false,
    
    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,
    
    @ColumnInfo(name = "download_path")
    val downloadPath: String? = null
) 