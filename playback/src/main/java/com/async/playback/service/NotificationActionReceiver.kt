package com.async.playback.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import timber.log.Timber

class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val ACTION_PLAY = "com.async.action.PLAY"
        private const val ACTION_PAUSE = "com.async.action.PAUSE"
        private const val ACTION_NEXT = "com.async.action.NEXT"
        private const val ACTION_PREVIOUS = "com.async.action.PREVIOUS"
        private const val ACTION_STOP = "com.async.action.STOP"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val action = intent.action ?: return
        Timber.d("Notification action received: $action")
        
        try {
            // Get the media controller from the PlayerService
            val serviceIntent = Intent(context, PlayerService::class.java)
            val mediaController = getMediaController(context)
            
            when (action) {
                ACTION_PLAY -> {
                    mediaController?.transportControls?.play()
                    Timber.d("Play action executed")
                }
                
                ACTION_PAUSE -> {
                    mediaController?.transportControls?.pause()
                    Timber.d("Pause action executed")
                }
                
                ACTION_NEXT -> {
                    mediaController?.transportControls?.skipToNext()
                    Timber.d("Next action executed")
                }
                
                ACTION_PREVIOUS -> {
                    mediaController?.transportControls?.skipToPrevious()
                    Timber.d("Previous action executed")
                }
                
                ACTION_STOP -> {
                    mediaController?.transportControls?.stop()
                    // Also stop the service
                    context.stopService(serviceIntent)
                    Timber.d("Stop action executed")
                }
                
                else -> {
                    Timber.w("Unknown notification action: $action")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling notification action: $action")
        }
    }
    
    private fun getMediaController(context: Context): MediaControllerCompat? {
        return try {
            // In a real implementation, you would need to get the session token
            // from the PlayerService. For now, this is a simplified approach.
            // You might want to use a singleton pattern or dependency injection
            // to access the MediaSession token.
            null
        } catch (e: Exception) {
            Timber.e(e, "Failed to get MediaController")
            null
        }
    }
} 