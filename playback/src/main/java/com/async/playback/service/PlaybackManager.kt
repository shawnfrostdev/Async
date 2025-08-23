package com.async.playback.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.extensions.manager.ExtensionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Manages audio playback using ExoPlayer and provides high-level playback controls
 */
class PlaybackManager(
    private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Extension manager for getting stream URLs
    private lateinit var extensionManager: ExtensionManager
    
    // Current playback state
    private var currentQueue: List<SearchResult> = emptyList()
    private var currentIndex: Int = -1
    
    fun initializeExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }
    
    fun setExtensionManager(manager: ExtensionManager) {
        extensionManager = manager
    }
    
    fun play() {
        exoPlayer?.let { player ->
            if (player.mediaItemCount > 0) {
                player.play()
                logcat { "Playback started" }
            } else {
                logcat { "No media items in queue to play" }
            }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun pause() {
        exoPlayer?.let { player ->
            player.pause()
            logcat { "Playback paused" }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun stop() {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            currentQueue = emptyList()
            currentIndex = -1
            logcat { "Playback stopped and queue cleared" }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun skipToNext() {
        exoPlayer?.let { player ->
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                currentIndex++
                logcat { "Skipped to next track" }
            } else {
                logcat { "No next track available" }
            }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun skipToPrevious() {
        exoPlayer?.let { player ->
            if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
                currentIndex--
                logcat { "Skipped to previous track" }
            } else {
                logcat { "No previous track available" }
            }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.let { player ->
            player.seekTo(positionMs)
            logcat { "Seeked to position: ${positionMs}ms" }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun skipToQueueItem(queueIndex: Int) {
        exoPlayer?.let { player ->
            if (queueIndex in 0 until player.mediaItemCount) {
                player.seekTo(queueIndex, 0)
                currentIndex = queueIndex
                logcat { "Skipped to queue item: $queueIndex" }
            } else {
                logcat { "Invalid queue index: $queueIndex" }
            }
        } ?: logcat { "ExoPlayer not initialized" }
    }
    
    fun playFromMediaId(mediaId: String) {
        scope.launch {
            try {
                logcat { "Playing from media ID: $mediaId" }
                
                // Parse media ID to get extension ID and track ID
                val parts = mediaId.split(":")
                if (parts.size >= 2) {
                    val extensionId = parts[0]
                    val trackId = parts[1]
                    
                    // Find the extension
                    val extension = extensionManager.getActiveExtensions()
                        .find { it.id == extensionId }
                    
                    if (extension != null) {
                        // Get stream URL from extension
                        val streamUrlResult = extension.getStreamUrl(trackId)
                        when (streamUrlResult) {
                            is ExtensionResult.Success -> {
                                val streamUrl = streamUrlResult.data
                                val mediaItem = createMediaItem(mediaId, streamUrl)
                                
                                // Play the media item
                                exoPlayer?.let { player ->
                                    player.setMediaItem(mediaItem)
                                    player.prepare()
                                    player.play()
                                }
                            }
                            is ExtensionResult.Error -> {
                                logcat { "Failed to get stream URL: ${streamUrlResult.exception.message}" }
                            }
                            is ExtensionResult.Loading -> {
                                logcat { "Stream URL is loading..." }
                            }
                        }
                    } else {
                        logcat { "Extension not found: $extensionId" }
                    }
                }
            } catch (e: Exception) {
                logcat { "Error playing from media ID: $mediaId" }
            }
        }
    }
    
    fun playFromSearch(query: String) {
        if (query.isBlank()) {
            logcat { "Empty search query" }
            return
        }
        
        scope.launch {
            try {
                logcat { "Playing from search: $query" }
                
                // Perform search across all enabled extensions
                // This is a simplified implementation - in reality you'd collect results from the flow
                val searchResults = mutableListOf<SearchResult>()
                
                if (searchResults.isNotEmpty()) {
                    playSearchResults(searchResults)
                } else {
                    logcat { "No search results found for: $query" }
                }
                
            } catch (e: Exception) {
                logcat { "Error playing from search: $query" }
            }
        }
    }
    
    private suspend fun playSearchResults(searchResults: List<SearchResult>, startIndex: Int = 0) {
        try {
            val mediaItems = mutableListOf<MediaItem>()
            
            for (result in searchResults) {
                // Get stream URL for each result
                val extensionId = result.extensionId
                val extension = extensionManager.getActiveExtensions()
                    .find { it.id == extensionId }
                
                if (extension != null) {
                    when (val streamUrlResult = extension.getStreamUrl(result.id)) {
                        is ExtensionResult.Success -> {
                            val mediaItem = createMediaItemFromSearchResult(result, streamUrlResult.data)
                            mediaItems.add(mediaItem)
                        }
                        is ExtensionResult.Error -> {
                            logcat { "Failed to get stream URL for queue item: ${result.id}" }
                        }
                        is ExtensionResult.Loading -> {
                            logcat { "Stream URL loading for queue item: ${result.id}" }
                        }
                    }
                }
            }
            
            if (mediaItems.isNotEmpty()) {
                exoPlayer?.let { player ->
                    player.setMediaItems(mediaItems, startIndex, 0)
                    player.prepare()
                    player.play()
                    
                    // Update current state
                    currentQueue = searchResults
                    currentIndex = startIndex
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun addToQueue(description: android.support.v4.media.MediaDescriptionCompat) {
        try {
            val mediaId = description.mediaId ?: return
            val mediaItem = createMediaItem(mediaId, description.title.toString())
            
            exoPlayer?.addMediaItem(mediaItem)
            logcat { "Added to queue: ${description.title}" }
            
        } catch (e: Exception) {
            logcat { "Error adding to queue: ${description.title}" }
        }
    }
    
    fun removeFromQueue(description: android.support.v4.media.MediaDescriptionCompat) {
        try {
            // This is a simplified implementation
            // In reality, you'd need to track media items by ID and remove the correct one
            val player = exoPlayer ?: return
            
            // For now, just remove the last item as an example
            if (player.mediaItemCount > 0) {
                val lastIndex = player.mediaItemCount - 1
                player.removeMediaItem(lastIndex)
                logcat { "Removed from queue: ${description.title}" }
            }
            
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun setShuffleMode(enabled: Boolean) {
        exoPlayer?.let { player ->
            player.shuffleModeEnabled = enabled
            logcat { "Shuffle mode: $enabled" }
        }
    }
    
    fun setRepeatMode(mode: Int) {
        exoPlayer?.let { player ->
            player.repeatMode = when (mode) {
                1 -> Player.REPEAT_MODE_ONE
                2 -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
            logcat { "Repeat mode: $mode" }
        }
    }
    
    private suspend fun getStreamUrlForResult(result: SearchResult): String? {
        return try {
            val extensionId = result.extensionId
            val extension = extensionManager.getActiveExtensions()
                .find { it.id == extensionId }
            
            if (extension != null) {
                when (val streamUrlResult = extension.getStreamUrl(result.id)) {
                    is ExtensionResult.Success -> streamUrlResult.data
                    is ExtensionResult.Error -> {
                        logcat { "Failed to get stream URL for: ${result.title}" }
                        null
                    }
                    is ExtensionResult.Loading -> {
                        logcat { "Getting stream URL for: ${result.title}" }
                        null
                    }
                }
            } else {
                logcat { "Extension not found for result: ${result.extensionId}" }
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun playSearchResults(results: List<SearchResult>) {
        try {
            val mediaItems = mutableListOf<MediaItem>()
            
            // Convert search results to MediaItems with stream URLs
            for (result in results) {
                val streamUrl = getStreamUrlForResult(result)
                if (streamUrl != null) {
                    val mediaItem = createMediaItemFromSearchResult(result, streamUrl)
                    mediaItems.add(mediaItem)
                }
            }
            
            // Set media items and start playback
            exoPlayer?.let { player ->
                player.setMediaItems(mediaItems)
                player.prepare()
                player.play()
                
                logcat { "Playing ${mediaItems.size} search results" }
            }
            
        } catch (e: Exception) {
            logcat { "Error playing search results" }
        }
    }
    
    private fun createMediaItem(mediaId: String, streamUrl: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle("Loading...")
            .build()
        
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }
    
    private fun createMediaItemFromSearchResult(result: SearchResult, streamUrl: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(result.title)
            .setArtist(result.artist)
            .setAlbumTitle(result.album)
            .build()
        
        logcat { "Playing media item: ${result.title}" }
        
        return MediaItem.Builder()
            .setMediaId(result.id)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }
    
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0
    
    fun getDuration(): Long = exoPlayer?.duration ?: 0
    
    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false
    
    fun getCurrentMediaItem(): MediaItem? = exoPlayer?.currentMediaItem
    
    fun getPlaybackState(): Int = exoPlayer?.playbackState ?: Player.STATE_IDLE
    
    fun getQueue(): List<SearchResult> = currentQueue
    
    fun getCurrentQueueIndex(): Int = currentIndex
    
    fun setPlaybackStateListener(listener: Player.Listener) {
        exoPlayer?.addListener(listener)
    }
    
    fun removePlaybackStateListener(listener: Player.Listener) {
        exoPlayer?.removeListener(listener)
    }
    
    // Search across extensions (simplified version)
    private suspend fun searchExtensions(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            extensionManager.getActiveExtensions().forEach { extension ->
                try {
                    when (val searchResult = extension.search(query)) {
                        is ExtensionResult.Success -> {
                            results.addAll(searchResult.data)
                        }
                        is ExtensionResult.Error -> {
                            logcat { "Search failed for extension: ${extension.id}" }
                        }
                        is ExtensionResult.Loading -> {
                            logcat { "Search loading for extension: ${extension.id}" }
                        }
                    }
                } catch (e: Exception) {
                    logcat { "Search failed for extension: ${extension.id}" }
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
        
        return results
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
} 