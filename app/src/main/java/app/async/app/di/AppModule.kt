package app.async.app.di


import android.content.Context
import androidx.media3.common.util.UnstableApi
import app.async.extensions.di.ExtensionModule
import app.async.extensions.service.ExtensionService
import app.async.playback.service.PlaybackManager
import app.async.data.database.AsyncDatabase
import app.async.data.repository.PlaylistRepositoryImpl
import app.async.data.repository.TrackRepositoryImpl
import app.async.data.repository.SettingsRepositoryImpl
import app.async.data.mapper.PlaylistMapper
import app.async.data.mapper.TrackMapper
import app.async.domain.repository.PlaylistRepository
import app.async.domain.repository.TrackRepository
import app.async.domain.repository.SettingsRepository

/**
 * Main dependency injection module for the app
 */
@UnstableApi
object AppModule {
    private var applicationContext: Context? = null
    private var playbackManager: PlaybackManager? = null
    private var database: AsyncDatabase? = null
    private var playlistRepository: PlaylistRepository? = null
    private var trackRepository: TrackRepository? = null
    private var settingsRepository: SettingsRepository? = null
    
    /**
     * Initialize the app module with application context
     */
    fun initialize(context: Context) {
        // Ensure we only store Application context to prevent memory leaks
        applicationContext = context.applicationContext
        
        // Initialize extension module
        ExtensionModule.initialize(context)
        
        // Initialize database
        database = AsyncDatabase.getDatabase(context)
    }
    
    private fun requireContext(): Context {
        return requireNotNull(applicationContext) { "AppModule not initialized" }
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
            playbackManager = PlaybackManager(requireContext(), getExtensionService())
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
