package com.async.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a playlist in the database
 */
@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["date_created"]),
        Index(value = ["last_modified"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "cover_art_url")
    val coverArtUrl: String? = null,
    
    @ColumnInfo(name = "cover_art_path")
    val coverArtPath: String? = null,
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0,
    
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long = 0, // in milliseconds
    
    @ColumnInfo(name = "date_created")
    val dateCreated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_system_playlist")
    val isSystemPlaylist: Boolean = false, // e.g., "Recently Played", "Favorites"
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0 // for custom ordering
) 