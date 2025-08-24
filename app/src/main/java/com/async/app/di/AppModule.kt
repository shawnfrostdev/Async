package com.async.app.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.async.extensions.di.ExtensionModule
import com.async.extensions.service.ExtensionService
import com.async.playback.service.PlaybackManager

/**
 * Main dependency injection module for the app
 */
@UnstableApi
object AppModule {
    private lateinit var context: Context
    private var playbackManager: PlaybackManager? = null
    
    /**
     * Initialize the app module with application context
     */
    fun initialize(applicationContext: Context) {
        context = applicationContext
        
        // Initialize extension module
        ExtensionModule.initialize(applicationContext)
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