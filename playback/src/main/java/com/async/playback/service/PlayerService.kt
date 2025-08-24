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
import com.async.playback.service.PlaybackManager
import logcat.logcat

/**
 * Simplified media browser service
 */
class PlayerService : MediaBrowserServiceCompat() {
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playbackManager: PlaybackManager
    
    override fun onCreate() {
        super.onCreate()
        
        logcat { "PlayerService: onCreate" }
        
        // Initialize playback manager
        playbackManager = PlaybackManager(this)
        
        // Create media session
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
        
        sessionToken = mediaSession.sessionToken
        
        logcat { "PlayerService: Initialized successfully" }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
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
    
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            logcat { "PlayerService: onPlay" }
            playbackManager.resume()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
        
        override fun onPause() {
            logcat { "PlayerService: onPause" }
            playbackManager.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
        
        override fun onStop() {
            logcat { "PlayerService: onStop" }
            playbackManager.stop()
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }
        
        override fun onSkipToNext() {
            logcat { "PlayerService: onSkipToNext" }
            // TODO: Implement skip to next
        }
        
        override fun onSkipToPrevious() {
            logcat { "PlayerService: onSkipToPrevious" }
            // TODO: Implement skip to previous
        }
        
        override fun onSeekTo(pos: Long) {
            logcat { "PlayerService: onSeekTo $pos" }
            playbackManager.seekTo(pos)
        }
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, 0L, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .build()
        
        mediaSession.setPlaybackState(playbackState)
    }
} 