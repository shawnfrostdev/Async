package com.async.playback.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.util.UnstableApi
import com.async.extensions.service.ExtensionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Enhanced media browser service with ExoPlayer integration
 */
@UnstableApi
class PlayerService : MediaBrowserServiceCompat() {
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playbackManager: PlaybackManager
    private lateinit var extensionService: ExtensionService
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        logcat { "PlayerService: onCreate" }
        
        // Initialize extension service
        extensionService = ExtensionService(this)
        
        // Initialize playback manager with extension service
        playbackManager = PlaybackManager(this, extensionService)
        
        // Create media session
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
        
        sessionToken = mediaSession.sessionToken
        
        // Observe playback state changes
        setupStateObservers()
        
        logcat { "PlayerService: Initialized successfully" }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        playbackManager.release()
        logcat { "PlayerService: Destroyed" }
    }
    
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        logcat { "PlayerService: onGetRoot called by $clientPackageName" }
        return BrowserRoot("__ROOT__", null)
    }
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        logcat { "PlayerService: onLoadChildren called for $parentId" }
        
        // Return empty list for now
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
        result.sendResult(mediaItems)
    }
    
    /**
     * Setup state observers for playback manager
     */
    private fun setupStateObservers() {
        // Observe playing state
        playbackManager.isPlaying
            .onEach { isPlaying ->
                val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                updatePlaybackState(state)
            }
            .launchIn(serviceScope)
        
        // Observe current track
        playbackManager.currentTrack
            .onEach { track ->
                if (track != null) {
                    updateMediaMetadata(track)
                }
            }
            .launchIn(serviceScope)
        
        // Observe loading state
        playbackManager.isLoading
            .onEach { isLoading ->
                if (isLoading) {
                    updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                }
            }
            .launchIn(serviceScope)
        
        // Observe errors
        playbackManager.error
            .onEach { error ->
                if (error != null) {
                    updatePlaybackState(PlaybackStateCompat.STATE_ERROR)
                    logcat { "PlayerService: Playback error: $error" }
                }
            }
            .launchIn(serviceScope)
    }
    
    /**
     * Update media metadata for media session
     */
    private fun updateMediaMetadata(track: com.async.core.model.SearchResult) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration ?: 0L)
            .build()
        
        mediaSession.setMetadata(metadata)
        logcat { "PlayerService: Updated metadata for ${track.title}" }
    }
    
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            logcat { "PlayerService: onPlay" }
            playbackManager.resume()
        }
        
        override fun onPause() {
            logcat { "PlayerService: onPause" }
            playbackManager.pause()
        }
        
        override fun onStop() {
            logcat { "PlayerService: onStop" }
            playbackManager.stop()
        }
        
        override fun onSkipToNext() {
            logcat { "PlayerService: onSkipToNext" }
            serviceScope.launch {
                playbackManager.skipToNext()
            }
        }
        
        override fun onSkipToPrevious() {
            logcat { "PlayerService: onSkipToPrevious" }
            serviceScope.launch {
                playbackManager.skipToPrevious()
            }
        }
        
        override fun onSeekTo(pos: Long) {
            logcat { "PlayerService: onSeekTo $pos" }
            playbackManager.seekTo(pos)
        }
        
        override fun onSetRepeatMode(repeatMode: Int) {
            logcat { "PlayerService: onSetRepeatMode $repeatMode" }
            // Convert MediaSession repeat mode to our repeat mode
            when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_NONE -> {
                    // Set to OFF mode
                    playbackManager.toggleRepeat() // This cycles, so we need to get to OFF
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    // Set to ONE mode
                    playbackManager.toggleRepeat()
                }
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    // Set to ALL mode  
                    playbackManager.toggleRepeat()
                }
            }
        }
        
        override fun onSetShuffleMode(shuffleMode: Int) {
            logcat { "PlayerService: onSetShuffleMode $shuffleMode" }
            val shuffleEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
            if (playbackManager.shuffleEnabled.value != shuffleEnabled) {
                playbackManager.toggleShuffle()
            }
        }
        
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            logcat { "PlayerService: onPlayFromMediaId $mediaId" }
            // TODO: Implement play from media ID
        }
        
        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            logcat { "PlayerService: onPlayFromSearch '$query'" }
            // TODO: Implement play from search
        }
    }

    private fun updatePlaybackState(state: Int) {
        val position = playbackManager.position.value
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
        
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, 1.0f)
            .setActions(actions)
            .build()
        
        mediaSession.setPlaybackState(playbackState)
        
        logcat { "PlayerService: Updated playback state: $state, position: $position" }
    }
    
    /**
     * Get the playback manager instance
     */
    fun getPlaybackManager(): PlaybackManager = playbackManager
} 