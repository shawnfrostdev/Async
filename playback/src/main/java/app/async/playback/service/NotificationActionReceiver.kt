package app.async.playback.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.util.UnstableApi
import logcat.logcat

@UnstableApi
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val ACTION_PLAY = "app.async.action.PLAY"
private const val ACTION_PAUSE = "app.async.action.PAUSE"
private const val ACTION_NEXT = "app.async.action.NEXT"
private const val ACTION_PREVIOUS = "app.async.action.PREVIOUS"
private const val ACTION_STOP = "app.async.action.STOP"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val action = intent.action ?: return
        logcat { "Notification action received: $action" }
        
        try {
            // Get the media controller from the PlayerService
            val serviceIntent = Intent(context, PlayerService::class.java)
            val mediaController = getMediaController(context)
            
            when (action) {
                ACTION_PLAY -> {
                    mediaController?.transportControls?.play()
                    logcat { "Play action executed" }
                }
                
                ACTION_PAUSE -> {
                    mediaController?.transportControls?.pause()
                    logcat { "Pause action executed" }
                }
                
                ACTION_NEXT -> {
                    mediaController?.transportControls?.skipToNext()
                    logcat { "Next action executed" }
                }
                
                ACTION_PREVIOUS -> {
                    mediaController?.transportControls?.skipToPrevious()
                    logcat { "Previous action executed" }
                }
                
                ACTION_STOP -> {
                    mediaController?.transportControls?.stop()
                    // Also stop the service
                    context.stopService(serviceIntent)
                    logcat { "Stop action executed" }
                }
                
                else -> {
                    logcat { "Unknown notification action: $action" }
                }
            }
        } catch (e: Exception) {
            logcat { "Error handling notification action: $action" }
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
            logcat { "Failed to get MediaController" }
            null
        }
    }
} 
