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
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaBrowserServiceCompat() {
    
    companion object {
        private const val MEDIA_ROOT_ID = "media_root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val RECENT_ROOT_ID = "recent_root_id"
        private const val QUEUE_ROOT_ID = "queue_root_id"
    }
    
    @Inject
    lateinit var playbackManager: PlaybackManager
    
    @Inject
    lateinit var notificationManager: PlaybackNotificationManager
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: ExoPlayer
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Playback state
    private val _playbackState = MutableStateFlow(
        PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build()
    )
    val playbackState: StateFlow<PlaybackStateCompat> = _playbackState.asStateFlow()
    
    // Current media metadata
    private val _currentMedia = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentMedia: StateFlow<MediaMetadataCompat?> = _currentMedia.asStateFlow()
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("PlayerService created")
        
        initializePlayer()
        initializeMediaSession()
        initializeNotificationManager()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("PlayerService destroyed")
        
        serviceScope.cancel()
        cleanupPlayer()
        cleanupMediaSession()
    }
    
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Timber.d("onGetRoot called by $clientPackageName")
        
        // Verify the client is allowed to browse media
        return if (isClientAllowed(clientPackageName, clientUid)) {
            BrowserRoot(MEDIA_ROOT_ID, null)
        } else {
            // Return an empty root for unauthorized clients
            BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
        }
    }
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Timber.d("onLoadChildren called with parentId: $parentId")
        
        when (parentId) {
            MEDIA_ROOT_ID -> {
                result.sendResult(buildMediaRoot())
            }
            RECENT_ROOT_ID -> {
                result.detach()
                loadRecentMedia(result)
            }
            QUEUE_ROOT_ID -> {
                result.detach()
                loadCurrentQueue(result)
            }
            EMPTY_MEDIA_ROOT_ID -> {
                result.sendResult(mutableListOf())
            }
            else -> {
                result.sendResult(null)
            }
        }
    }
    
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        
        // Connect ExoPlayer to PlaybackManager
        playbackManager.setExoPlayer(exoPlayer)
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                updatePlaybackState()
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                Timber.e(error, "ExoPlayer error occurred")
                handlePlaybackError(error)
            }
            
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                super.onMediaItemTransition(mediaItem, reason)
                updateCurrentMedia(mediaItem)
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                updatePlaybackState()
            }
        })
        
        Timber.d("ExoPlayer initialized")
    }
    
    private fun initializeMediaSession() {
        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
            setCallback(MediaSessionCallback())
            setPlaybackState(_playbackState.value)
        }
        
        sessionToken = mediaSession.sessionToken
        Timber.d("MediaSession initialized")
    }
    
    private fun initializeNotificationManager() {
        notificationManager.initialize(
            mediaSession = mediaSession,
            notificationListener = object : PlaybackNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    if (dismissedByUser) {
                        stopSelf()
                    }
                }
                
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: android.app.Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing && !isForegroundService) {
                        startForeground(notificationId, notification)
                    }
                }
            }
        )
        
        Timber.d("Notification manager initialized")
    }
    
    private fun updatePlaybackState() {
        val state = when {
            exoPlayer.playbackState == Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
            exoPlayer.playWhenReady && exoPlayer.playbackState == Player.STATE_READY -> PlaybackStateCompat.STATE_PLAYING
            exoPlayer.playbackState == Player.STATE_READY -> PlaybackStateCompat.STATE_PAUSED
            exoPlayer.playbackState == Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_NONE
        }
        
        val playbackStateCompat = PlaybackStateCompat.Builder()
            .setState(state, exoPlayer.currentPosition, exoPlayer.playbackParameters.speed)
            .setActions(getAvailableActions())
            .build()
        
        _playbackState.value = playbackStateCompat
        mediaSession.setPlaybackState(playbackStateCompat)
        
        Timber.v("Playback state updated: $state")
    }
    
    private fun updateCurrentMedia(mediaItem: MediaItem?) {
        val metadata = mediaItem?.let { item ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, item.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.mediaMetadata.title?.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.mediaMetadata.artist?.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.mediaMetadata.albumTitle?.toString())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer.duration)
                .build()
        }
        
        _currentMedia.value = metadata
        mediaSession.setMetadata(metadata)
        
        Timber.d("Current media updated: ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)}")
    }
    
    private fun getAvailableActions(): Long {
        var actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
        
        if (exoPlayer.playbackState != Player.STATE_ENDED) {
            actions = actions or PlaybackStateCompat.ACTION_SEEK_TO
        }
        
        when {
            exoPlayer.playWhenReady -> {
                actions = actions or PlaybackStateCompat.ACTION_PAUSE
            }
            else -> {
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
        }
        
        if (exoPlayer.hasPreviousMediaItem()) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        
        if (exoPlayer.hasNextMediaItem()) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }
        
        return actions
    }
    
    private fun handlePlaybackError(error: androidx.media3.common.PlaybackException) {
        val errorState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_ERROR,
                exoPlayer.currentPosition,
                0f
            )
            .setErrorMessage(
                PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR,
                error.localizedMessage ?: "Unknown playback error"
            )
            .build()
        
        _playbackState.value = errorState
        mediaSession.setPlaybackState(errorState)
    }
    
    private fun isClientAllowed(clientPackageName: String, clientUid: Int): Boolean {
        // For now, allow all clients. In production, you might want to verify
        // the client's signature or maintain a whitelist
        return true
    }
    
    private fun buildMediaRoot(): MutableList<MediaBrowserCompat.MediaItem> {
        return mutableListOf(
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(RECENT_ROOT_ID)
                    .setTitle("Recently Played")
                    .setSubtitle("Your recent music")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(QUEUE_ROOT_ID)
                    .setTitle("Current Queue")
                    .setSubtitle("Now playing queue")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        )
    }
    
    private fun loadRecentMedia(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        serviceScope.launch {
            try {
                // TODO: Load recent media from repository
                val recentItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                result.sendResult(recentItems)
            } catch (e: Exception) {
                Timber.e(e, "Error loading recent media")
                result.sendResult(mutableListOf())
            }
        }
    }
    
    private fun loadCurrentQueue(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        serviceScope.launch {
            try {
                val queueItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                
                for (i in 0 until exoPlayer.mediaItemCount) {
                    val mediaItem = exoPlayer.getMediaItemAt(i)
                    val browserItem = MediaBrowserCompat.MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId(mediaItem.mediaId)
                            .setTitle(mediaItem.mediaMetadata.title)
                            .setSubtitle(mediaItem.mediaMetadata.artist)
                            .build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                    queueItems.add(browserItem)
                }
                
                result.sendResult(queueItems)
            } catch (e: Exception) {
                Timber.e(e, "Error loading current queue")
                result.sendResult(mutableListOf())
            }
        }
    }
    
    private fun cleanupPlayer() {
        exoPlayer.release()
    }
    
    private fun cleanupMediaSession() {
        mediaSession.isActive = false
        mediaSession.release()
    }
    
    private val isForegroundService: Boolean
        get() = notificationManager.isForegroundService
    
    /**
     * MediaSession callback to handle playback commands
     */
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            Timber.d("MediaSession: onPlay")
            playbackManager.play()
        }
        
        override fun onPause() {
            Timber.d("MediaSession: onPause")
            playbackManager.pause()
        }
        
        override fun onStop() {
            Timber.d("MediaSession: onStop")
            playbackManager.stop()
        }
        
        override fun onSkipToNext() {
            Timber.d("MediaSession: onSkipToNext")
            playbackManager.skipToNext()
        }
        
        override fun onSkipToPrevious() {
            Timber.d("MediaSession: onSkipToPrevious")
            playbackManager.skipToPrevious()
        }
        
        override fun onSeekTo(pos: Long) {
            Timber.d("MediaSession: onSeekTo $pos")
            playbackManager.seekTo(pos)
        }
        
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Timber.d("MediaSession: onPlayFromMediaId $mediaId")
            mediaId?.let { playbackManager.playFromMediaId(it, extras) }
        }
        
        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            Timber.d("MediaSession: onPlayFromSearch $query")
            playbackManager.playFromSearch(query, extras)
        }
        
        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            Timber.d("MediaSession: onAddQueueItem ${description?.title}")
            description?.let { playbackManager.addToQueue(it) }
        }
        
        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            Timber.d("MediaSession: onRemoveQueueItem ${description?.title}")
            description?.let { playbackManager.removeFromQueue(it) }
        }
        
        override fun onSkipToQueueItem(id: Long) {
            Timber.d("MediaSession: onSkipToQueueItem $id")
            playbackManager.skipToQueueItem(id.toInt())
        }
    }
} 