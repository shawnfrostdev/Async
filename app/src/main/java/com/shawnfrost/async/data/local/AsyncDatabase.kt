package com.shawnfrost.async.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shawnfrost.async.data.local.dao.PlaylistDao
import com.shawnfrost.async.data.local.dao.SearchHistoryDao
import com.shawnfrost.async.data.local.dao.TrackDao
import com.shawnfrost.async.data.local.entity.PlaylistEntity
import com.shawnfrost.async.data.local.entity.PlaylistTrackCrossRef
import com.shawnfrost.async.data.local.entity.SearchHistoryEntity
import com.shawnfrost.async.data.local.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        SearchHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AsyncDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val DATABASE_NAME = "async_db"
    }
} 