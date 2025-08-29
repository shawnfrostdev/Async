package com.async.app.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.async.extensions.di.ExtensionModule
import com.async.extensions.service.ExtensionService
import com.async.playback.service.PlaybackManager
import com.async.data.database.AsyncDatabase
import com.async.data.repository.PlaylistRepositoryImpl
import com.async.data.repository.TrackRepositoryImpl
import com.async.data.repository.SettingsRepositoryImpl
import com.async.data.mapper.PlaylistMapper
import com.async.data.mapper.TrackMapper
import com.async.domain.repository.PlaylistRepository
import com.async.domain.repository.TrackRepository
import com.async.domain.repository.SettingsRepository

/**
 * Main dependency injection module for the app
 */
@UnstableApi
object AppModule {
    private lateinit var context: Context
    private var playbackManager: PlaybackManager? = null
    private var database: AsyncDatabase? = null
    private var playlistRepository: PlaylistRepository? = null
    private var trackRepository: TrackRepository? = null
    private var settingsRepository: SettingsRepository? = null
    
    /**
     * Initialize the app module with application context
     */
    fun initialize(applicationContext: Context) {
        context = applicationContext
        
        // Initialize extension module
        ExtensionModule.initialize(applicationContext)
        
        // Initialize database
        database = AsyncDatabase.getDatabase(applicationContext)
    }
    
    /**
     * Get the database instance
     */
    fun getDatabase(): AsyncDatabase {
        return database ?: throw IllegalStateException("AppModule not initialized")
    }
    
    /**
     * Get the playlist repository
     */
    fun getPlaylistRepository(): PlaylistRepository {
        if (playlistRepository == null) {
            val db = getDatabase()
            playlistRepository = PlaylistRepositoryImpl(
                playlistDao = db.playlistDao(),
                trackDao = db.trackDao(),
                playlistMapper = PlaylistMapper(),
                trackMapper = TrackMapper()
            )
        }
        return playlistRepository!!
    }
    
    /**
     * Get the track repository
     */
    fun getTrackRepository(): TrackRepository {
        if (trackRepository == null) {
            val db = getDatabase()
            trackRepository = TrackRepositoryImpl(
                trackDao = db.trackDao(),
                trackMapper = TrackMapper()
            )
        }
        return trackRepository!!
    }
    
    /**
     * Get the settings repository
     */
    fun getSettingsRepository(): SettingsRepository {
        if (settingsRepository == null) {
            val db = getDatabase()
            settingsRepository = SettingsRepositoryImpl(
                userSettingsDao = db.userSettingsDao()
            )
        }
        return settingsRepository!!
    }
    
    /**
     * Get the extension service
     */
    fun getExtensionService(): ExtensionService {
        return ExtensionModule.getExtensionService()
    }
    
    /**
     * Get the playback manager (lazy initialization)
     */
    fun getPlaybackManager(): PlaybackManager {
        if (playbackManager == null) {
            playbackManager = PlaybackManager(context, getExtensionService())
        }
        return playbackManager!!
    }
    
    /**
     * Release resources
     */
    fun release() {
        playbackManager?.release()
        playbackManager = null
    }
} 