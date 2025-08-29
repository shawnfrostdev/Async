package com.async.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.async.data.database.dao.*
import com.async.data.database.entity.*

/**
 * Main Room database for the Async Music Player
 */
@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        PlayHistoryEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AsyncDatabase : RoomDatabase() {
    
    // ======== DAO ACCESSORS ========
    
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun userSettingsDao(): UserSettingsDao
    
    companion object {
        const val DATABASE_NAME = "async_database"
        
        @Volatile
        private var INSTANCE: AsyncDatabase? = null
        
        fun getDatabase(context: Context): AsyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AsyncDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*getAllMigrations())
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // Remove in production
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Get all database migrations
         */
        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                MIGRATION_1_2
            )
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add is_liked column to tracks table
                database.execSQL("ALTER TABLE tracks ADD COLUMN is_liked INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        /**
         * Clear the database instance (for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
    
    /**
     * Database callback for initialization
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Initialize default data when database is created
            initializeDefaultData(db)
        }
        
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON")
        }
        
        /**
         * Initialize default data
         */
        private fun initializeDefaultData(db: SupportSQLiteDatabase) {
            try {
                // Create default system playlists
                createDefaultPlaylists(db)
                
                // Create default app settings
                createDefaultSettings(db)
                
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
        
        /**
         * Create default system playlists
         */
        private fun createDefaultPlaylists(db: SupportSQLiteDatabase) {
            val currentTime = System.currentTimeMillis()
            
            // Recently Played playlist
            db.execSQL("""
                INSERT INTO playlists (name, description, is_system_playlist, sort_order, date_created, last_modified)
                VALUES ('Recently Played', 'Your recently played tracks', 1, 0, $currentTime, $currentTime)
            """)
            
            // Favorites playlist
            db.execSQL("""
                INSERT INTO playlists (name, description, is_system_playlist, sort_order, date_created, last_modified)
                VALUES ('Favorites', 'Your favorite tracks', 1, 1, $currentTime, $currentTime)
            """)
            
            // Most Played playlist
            db.execSQL("""
                INSERT INTO playlists (name, description, is_system_playlist, sort_order, date_created, last_modified)
                VALUES ('Most Played', 'Your most played tracks', 1, 2, $currentTime, $currentTime)
            """)
        }
        
        /**
         * Create default app settings
         */
        private fun createDefaultSettings(db: SupportSQLiteDatabase) {
            val currentTime = System.currentTimeMillis()
            
            // App settings
            val appSettings = listOf(
                Triple("theme", "dark", "App theme (light, dark, system)"),
                Triple("language", "en", "App language"),
                Triple("first_launch", "true", "Whether this is the first app launch"),
                Triple("analytics_enabled", "true", "Whether analytics are enabled"),
                Triple("crash_reporting_enabled", "true", "Whether crash reporting is enabled")
            )
            
            appSettings.forEach { (key, value, description) ->
                db.execSQL("""
                    INSERT INTO user_settings (category, key, value, value_type, description, date_created, last_modified)
                    VALUES ('app', '$key', '$value', 'string', '$description', $currentTime, $currentTime)
                """)
            }
            
            // Playback settings
            val playbackSettings = listOf(
                Triple("repeat_mode", "off", "Repeat mode (off, one, all)"),
                Triple("shuffle_enabled", "false", "Whether shuffle is enabled"),
                Triple("crossfade_duration", "0", "Crossfade duration in seconds"),
                Triple("replay_gain_enabled", "false", "Whether replay gain is enabled"),
                Triple("audio_quality", "high", "Audio quality preference"),
                Triple("buffer_size", "default", "Audio buffer size"),
                Triple("skip_silence", "false", "Whether to skip silence"),
                Triple("volume_normalization", "false", "Whether to normalize volume")
            )
            
            playbackSettings.forEach { (key, value, description) ->
                val valueType = when (key) {
                    "crossfade_duration" -> "int"
                    "replay_gain_enabled", "shuffle_enabled", "skip_silence", "volume_normalization" -> "boolean"
                    else -> "string"
                }
                
                db.execSQL("""
                    INSERT INTO user_settings (category, key, value, value_type, description, date_created, last_modified)
                    VALUES ('playback', '$key', '$value', '$valueType', '$description', $currentTime, $currentTime)
                """)
            }
            
            // UI settings
            val uiSettings = listOf(
                Triple("show_album_art", "true", "Whether to show album art"),
                Triple("compact_player", "false", "Whether to use compact player UI"),
                Triple("show_visualizer", "false", "Whether to show audio visualizer"),
                Triple("grid_columns", "2", "Number of columns in grid views"),
                Triple("list_item_size", "medium", "Size of list items (small, medium, large)")
            )
            
            uiSettings.forEach { (key, value, description) ->
                val valueType = when (key) {
                    "grid_columns" -> "int"
                    "show_album_art", "compact_player", "show_visualizer" -> "boolean"
                    else -> "string"
                }
                
                db.execSQL("""
                    INSERT INTO user_settings (category, key, value, value_type, description, date_created, last_modified)
                    VALUES ('ui', '$key', '$value', '$valueType', '$description', $currentTime, $currentTime)
                """)
            }
        }
    }
}

/**
 * Database migration examples (add as needed)
 */
/*
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column example
        database.execSQL("ALTER TABLE tracks ADD COLUMN new_column TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new table example
        database.execSQL("""
            CREATE TABLE new_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """)
    }
}
*/ 