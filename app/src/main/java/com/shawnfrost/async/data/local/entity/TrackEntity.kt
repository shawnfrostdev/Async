package com.shawnfrost.async.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArt: String?,
    val mp3Url: String,
    val flacUrl: String?,
    val license: String,
    val source: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null
) 