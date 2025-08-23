package com.async.app.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.async.data.database.AsyncDatabase
import com.async.data.mapper.PlaylistMapper
import com.async.data.mapper.TrackMapper
import com.async.data.repository.PlaylistRepositoryImpl
import com.async.data.repository.SettingsRepositoryImpl
import com.async.data.repository.TrackRepositoryImpl
import com.async.domain.repository.PlaylistRepository
import com.async.domain.repository.SettingsRepository
import com.async.domain.repository.TrackRepository
import com.async.extensions.di.ExtensionModule
import com.async.extensions.installer.ExtensionInstaller
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.manager.ExtensionManager
import com.async.extensions.repository.ExtensionRepository
import com.async.extensions.runtime.ExtensionRuntime
import com.async.extensions.security.ExtensionValidator
import com.async.extensions.storage.ExtensionStorage
import com.async.playback.service.PlaybackManager

object AppModule {
    private lateinit var application: Application
    
    // Lazy initialized dependencies
    private val _database: AsyncDatabase by lazy {
        Room.databaseBuilder(
            application,
            AsyncDatabase::class.java,
            "async_database"
        ).build()
    }
    
    // Mappers
    private val _playlistMapper: PlaylistMapper by lazy {
        PlaylistMapper()
    }
    
    private val _trackMapper: TrackMapper by lazy {
        TrackMapper()
    }
    
    // Playback system
    private val _playbackManager: PlaybackManager by lazy {
        PlaybackManager(application)
    }
    
    // Repository implementations
    private val _playlistRepository: PlaylistRepository by lazy {
        PlaylistRepositoryImpl(
            _database.playlistDao(),
            _database.trackDao(),
            _playlistMapper,
            _trackMapper
        )
    }
    
    private val _trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(
            _database.trackDao(),
            _trackMapper
        )
    }
    
    private val _settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(_database.userSettingsDao())
    }
    
    fun initialize(app: Application) {
        application = app
        
        // Initialize extension system
        ExtensionModule.initialize(app)
    }
    
    // Public accessors for core components
    fun getContext(): Context = application
    fun getDatabase(): AsyncDatabase = _database
    fun getPlaybackManager(): PlaybackManager = _playbackManager
    fun getPlaylistRepository(): PlaylistRepository = _playlistRepository
    fun getTrackRepository(): TrackRepository = _trackRepository
    fun getSettingsRepository(): SettingsRepository = _settingsRepository
    fun getPlaylistMapper(): PlaylistMapper = _playlistMapper
    fun getTrackMapper(): TrackMapper = _trackMapper
    
    // Extension system accessors
    fun getExtensionStorage(): ExtensionStorage = ExtensionModule.getExtensionStorage()
    fun getExtensionValidator(): ExtensionValidator = ExtensionModule.getExtensionValidator()
    fun getExtensionLoader(): ExtensionLoader = ExtensionModule.getExtensionLoader()
    fun getExtensionRepository(): ExtensionRepository = ExtensionModule.getExtensionRepository()
    fun getExtensionInstaller(): ExtensionInstaller = ExtensionModule.getExtensionInstaller()
    fun getExtensionManager(): ExtensionManager = ExtensionModule.getExtensionManager()
    fun getExtensionRuntime(): ExtensionRuntime = ExtensionModule.getExtensionRuntime()
    fun getExtensionService(): com.async.extensions.service.ExtensionService = ExtensionModule.getExtensionService()
} 