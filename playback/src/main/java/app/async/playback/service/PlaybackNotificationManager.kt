package app.async.playback.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media3.common.util.UnstableApi
// Glide imports temporarily disabled
// import com.bumptech.glide.Glide
// import com.bumptech.glide.request.target.CustomTarget
// import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import logcat.logcat

@UnstableApi
class PlaybackNotificationManager(
    private val context: Context
) {
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "async_playback_channel"
        private const val CHANNEL_NAME = "Music Playback"
        private const val CHANNEL_DESCRIPTION = "Controls for music playback"
        
        // Notification actions
        private const val ACTION_PLAY = "app.async.action.PLAY"
private const val ACTION_PAUSE = "app.async.action.PAUSE"
private const val ACTION_NEXT = "app.async.action.NEXT"
private const val ACTION_PREVIOUS = "app.async.action.PREVIOUS"
private const val ACTION_STOP = "app.async.action.STOP"
    }
    
    private var mediaSession: MediaSessionCompat? = null
    private var notificationListener: NotificationListener? = null
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    var isForegroundService = false
        private set
    
    fun initialize(
        mediaSession: MediaSessionCompat,
        notificationListener: NotificationListener
    ) {
        this.mediaSession = mediaSession
        this.notificationListener = notificationListener
        
        createNotificationChannel()
        logcat { "PlaybackNotificationManager initialized" }
    }
    
    fun updateNotification(
        metadata: MediaMetadataCompat?,
        playbackState: PlaybackStateCompat?
    ) {
        if (mediaSession == null) {
            logcat { "MediaSession not initialized" }
            return
        }
        
        notificationScope.launch {
            try {
                val notification = buildNotification(metadata, playbackState)
                val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
                
                @Suppress("MissingPermission")
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
                
                notificationListener?.onNotificationPosted(
                    NOTIFICATION_ID,
                    notification,
                    isPlaying
                )
                
                isForegroundService = isPlaying
                
            } catch (e: Exception) {
                logcat { "Error updating notification" }
            }
        }
    }
    
    fun cancelNotification() {
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        notificationListener?.onNotificationCancelled(NOTIFICATION_ID, false)
        isForegroundService = false
        logcat { "Notification cancelled" }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            logcat { "Notification channel created" }
        }
    }
    
    private suspend fun buildNotification(
        metadata: MediaMetadataCompat?,
        playbackState: PlaybackStateCompat?
    ): Notification {
        return withContext(Dispatchers.IO) {
            val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
            val title = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "Unknown Title"
            val artist = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM) ?: ""
            
            // Create main content intent
            val contentIntent = createContentIntent()
            
            // Build notification using MediaStyle
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play) // TODO: Replace with app icon
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(album)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(createStopIntent())
                .setOngoing(isPlaying)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(
                    MediaNotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession?.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2) // prev, play/pause, next
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(createStopIntent())
                )
            
            // Add action buttons
            addNotificationActions(builder, playbackState)
            
            // Load and set large icon (album art) asynchronously
            loadAlbumArt(metadata) { bitmap ->
                bitmap?.let { builder.setLargeIcon(it) }
            }
            
            builder.build()
        }
    }
    
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        playbackState: PlaybackStateCompat?
    ) {
        val actions = playbackState?.actions ?: 0L
        
        // Previous button
        if (actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
            builder.addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                createActionIntent(ACTION_PREVIOUS)
            )
        }
        
        // Play/Pause button
        val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
        if (isPlaying) {
            if (actions and PlaybackStateCompat.ACTION_PAUSE != 0L) {
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    createActionIntent(ACTION_PAUSE)
                )
            }
        } else {
            if (actions and PlaybackStateCompat.ACTION_PLAY != 0L) {
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    "Play",
                    createActionIntent(ACTION_PLAY)
                )
            }
        }
        
        // Next button
        if (actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
            builder.addAction(
                android.R.drawable.ic_media_next,
                "Next",
                createActionIntent(ACTION_NEXT)
            )
        }
        
        // Stop button (always available)
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createActionIntent(ACTION_STOP)
        )
    }
    
    private fun createContentIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createStopIntent(): PendingIntent {
        return createActionIntent(ACTION_STOP)
    }
    
    private fun loadAlbumArt(
        metadata: MediaMetadataCompat?,
        callback: (Bitmap?) -> Unit
    ) {
        val albumArtUri = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        
        if (albumArtUri.isNullOrBlank()) {
            callback(null)
            return
        }
        
        try {
            // Glide functionality temporarily disabled
            // TODO: Implement album art loading with Coil or another image loader
            callback(null)
        } catch (e: Exception) {
            logcat { "Failed to load album art" }
            callback(null)
        }
    }
    
    fun cleanup() {
        notificationScope.cancel()
        cancelNotification()
        mediaSession = null
        notificationListener = null
    }
    
    /**
     * Interface for notification events
     */
    interface NotificationListener {
        fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        )
        
        fun onNotificationCancelled(
            notificationId: Int,
            dismissedByUser: Boolean
        )
    }
} 
