package com.async.playback.service

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.async.core.extension.MusicExtension
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.extensions.manager.ExtensionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extensionManager: ExtensionManager
) {
    
    private var exoPlayer: ExoPlayer? = null
    private val playbackScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Current queue
    private val _currentQueue = MutableStateFlow<List<QueueItem>>(emptyList())
    val currentQueue: StateFlow<List<QueueItem>> = _currentQueue.asStateFlow()
    
    // Current queue index
    private val _currentQueueIndex = MutableStateFlow(-1)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()
    
    // Shuffle and repeat modes
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    fun setExoPlayer(player: ExoPlayer) {
        this.exoPlayer = player
    }
    
    /**
     * Play the current media item
     */
    fun play() {
        exoPlayer?.let { player ->
            if (player.mediaItemCount > 0) {
                player.play()
                Timber.d("Playback started")
            } else {
                Timber.w("No media items in queue to play")
            }
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        exoPlayer?.let { player ->
            player.pause()
            Timber.d("Playback paused")
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Stop playback and clear queue
     */
    fun stop() {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            _currentQueue.value = emptyList()
            _currentQueueIndex.value = -1
            Timber.d("Playback stopped and queue cleared")
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Skip to next track
     */
    fun skipToNext() {
        exoPlayer?.let { player ->
            if (player.hasNextMediaItem()) {
                player.seekToNext()
                updateQueueIndex(player.currentMediaItemIndex)
                Timber.d("Skipped to next track")
            } else {
                Timber.d("No next track available")
            }
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        exoPlayer?.let { player ->
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious()
                updateQueueIndex(player.currentMediaItemIndex)
                Timber.d("Skipped to previous track")
            } else {
                Timber.d("No previous track available")
            }
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.let { player ->
            player.seekTo(positionMs)
            Timber.d("Seeked to position: ${positionMs}ms")
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Skip to specific queue item
     */
    fun skipToQueueItem(queueIndex: Int) {
        exoPlayer?.let { player ->
            if (queueIndex >= 0 && queueIndex < player.mediaItemCount) {
                player.seekTo(queueIndex, 0)
                updateQueueIndex(queueIndex)
                Timber.d("Skipped to queue item: $queueIndex")
            } else {
                Timber.w("Invalid queue index: $queueIndex")
            }
        } ?: Timber.e("ExoPlayer not initialized")
    }
    
    /**
     * Play from media ID (typically from extension search results)
     */
    fun playFromMediaId(mediaId: String, extras: Bundle?) {
        playbackScope.launch {
            try {
                Timber.d("Playing from media ID: $mediaId")
                
                // Parse the media ID to determine extension and track info
                val (extensionId, trackId) = parseMediaId(mediaId)
                
                // Get the extension
                val extension = extensionManager.getExtension(extensionId)
                if (extension == null) {
                    Timber.e("Extension not found: $extensionId")
                    return@launch
                }
                
                // Get stream URL from extension
                val streamUrlResult = extension.getStreamUrl(trackId)
                if (streamUrlResult.isError) {
                    Timber.e("Failed to get stream URL: ${(streamUrlResult as ExtensionResult.Error).exception.message}")
                    return@launch
                }
                
                val streamUrl = streamUrlResult.getOrThrow()
                
                // Create media item and play
                val mediaItem = createMediaItem(mediaId, streamUrl, extras)
                playMediaItem(mediaItem)
                
                // Record extension usage
                extensionManager.recordExtensionUsage(extensionId)
                
            } catch (e: Exception) {
                Timber.e(e, "Error playing from media ID: $mediaId")
            }
        }
    }
    
    /**
     * Play from search query using extensions
     */
    fun playFromSearch(query: String?, extras: Bundle?) {
        if (query.isNullOrBlank()) {
            Timber.w("Empty search query")
            return
        }
        
        playbackScope.launch {
            try {
                Timber.d("Playing from search: $query")
                
                // Search across all active extensions
                val searchResults = searchAcrossExtensions(query)
                
                if (searchResults.isEmpty()) {
                    Timber.w("No search results found for: $query")
                    return@launch
                }
                
                // Play the first result
                val firstResult = searchResults.first()
                val mediaId = createMediaId(firstResult.extensionId, firstResult.id)
                playFromMediaId(mediaId, extras)
                
            } catch (e: Exception) {
                Timber.e(e, "Error playing from search: $query")
            }
        }
    }
    
    /**
     * Add item to queue
     */
    fun addToQueue(description: MediaDescriptionCompat) {
        playbackScope.launch {
            try {
                val mediaId = description.mediaId ?: return@launch
                val (extensionId, trackId) = parseMediaId(mediaId)
                
                val extension = extensionManager.getExtension(extensionId) ?: return@launch
                val streamUrlResult = extension.getStreamUrl(trackId)
                
                if (streamUrlResult.isError) {
                    Timber.e("Failed to get stream URL for queue item: $mediaId")
                    return@launch
                }
                
                val streamUrl = streamUrlResult.getOrThrow()
                val mediaItem = createMediaItem(mediaId, streamUrl, null)
                
                exoPlayer?.addMediaItem(mediaItem)
                
                // Update queue state
                val queueItem = QueueItem(
                    mediaId = mediaId,
                    title = description.title?.toString() ?: "Unknown",
                    artist = description.subtitle?.toString() ?: "Unknown",
                    extensionId = extensionId
                )
                
                val currentQueue = _currentQueue.value.toMutableList()
                currentQueue.add(queueItem)
                _currentQueue.value = currentQueue
                
                Timber.d("Added to queue: ${description.title}")
                
            } catch (e: Exception) {
                Timber.e(e, "Error adding to queue: ${description.title}")
            }
        }
    }
    
    /**
     * Remove item from queue
     */
    fun removeFromQueue(description: MediaDescriptionCompat) {
        val mediaId = description.mediaId ?: return
        
        exoPlayer?.let { player ->
            for (i in 0 until player.mediaItemCount) {
                if (player.getMediaItemAt(i).mediaId == mediaId) {
                    player.removeMediaItem(i)
                    
                    // Update queue state
                    val currentQueue = _currentQueue.value.toMutableList()
                    currentQueue.removeAt(i)
                    _currentQueue.value = currentQueue
                    
                    // Update current index if needed
                    if (i <= _currentQueueIndex.value) {
                        _currentQueueIndex.value = maxOf(0, _currentQueueIndex.value - 1)
                    }
                    
                    Timber.d("Removed from queue: ${description.title}")
                    break
                }
            }
        }
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleMode(enabled: Boolean) {
        exoPlayer?.shuffleModeEnabled = enabled
        _shuffleMode.value = enabled
        Timber.d("Shuffle mode: $enabled")
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatMode(mode: RepeatMode) {
        val exoRepeatMode = when (mode) {
            RepeatMode.OFF -> androidx.media3.common.Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> androidx.media3.common.Player.REPEAT_MODE_ALL
        }
        
        exoPlayer?.repeatMode = exoRepeatMode
        _repeatMode.value = mode
        Timber.d("Repeat mode: $mode")
    }
    
    /**
     * Play a list of search results
     */
    fun playSearchResults(results: List<SearchResult>, startIndex: Int = 0) {
        playbackScope.launch {
            try {
                val mediaItems = mutableListOf<MediaItem>()
                val queueItems = mutableListOf<QueueItem>()
                
                for (result in results) {
                    val extension = extensionManager.getExtension(result.extensionId)
                    if (extension == null) {
                        Timber.w("Extension not found for result: ${result.extensionId}")
                        continue
                    }
                    
                    val streamUrlResult = extension.getStreamUrl(result.id)
                    if (streamUrlResult.isError) {
                        Timber.w("Failed to get stream URL for: ${result.title}")
                        continue
                    }
                    
                    val streamUrl = streamUrlResult.getOrThrow()
                    val mediaId = createMediaId(result.extensionId, result.id)
                    val mediaItem = createMediaItemFromSearchResult(result, streamUrl)
                    
                    mediaItems.add(mediaItem)
                    queueItems.add(
                        QueueItem(
                            mediaId = mediaId,
                            title = result.title,
                            artist = result.artist ?: "Unknown Artist",
                            extensionId = result.extensionId
                        )
                    )
                }
                
                if (mediaItems.isNotEmpty()) {
                    exoPlayer?.let { player ->
                        player.setMediaItems(mediaItems, startIndex, 0)
                        player.prepare()
                        player.play()
                        
                        _currentQueue.value = queueItems
                        _currentQueueIndex.value = startIndex
                        
                        Timber.d("Playing ${mediaItems.size} search results starting at index $startIndex")
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error playing search results")
            }
        }
    }
    
    private fun playMediaItem(mediaItem: MediaItem) {
        exoPlayer?.let { player ->
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            // Update queue with single item
            val queueItem = QueueItem(
                mediaId = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                extensionId = parseMediaId(mediaItem.mediaId).first
            )
            
            _currentQueue.value = listOf(queueItem)
            _currentQueueIndex.value = 0
            
            Timber.d("Playing media item: ${mediaItem.mediaMetadata.title}")
        }
    }
    
    private fun createMediaItem(mediaId: String, streamUrl: String, extras: Bundle?): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(extras?.getString("title"))
            .setArtist(extras?.getString("artist"))
            .setAlbumTitle(extras?.getString("album"))
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
        
        return MediaItem.Builder()
            .setMediaId(createMediaId(result.extensionId, result.id))
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }
    
    private suspend fun searchAcrossExtensions(query: String): List<SearchResult> {
        val allResults = mutableListOf<SearchResult>()
        val activeExtensions = extensionManager.getActiveExtensions()
        
        // Search in parallel across all extensions
        val searchJobs = activeExtensions.map { extension ->
            playbackScope.async {
                try {
                    val result = extension.search(query)
                    if (result.isSuccess) {
                        result.getOrThrow()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Search failed for extension: ${extension.id}")
                    emptyList()
                }
            }
        }
        
        // Collect all results
        searchJobs.forEach { job ->
            allResults.addAll(job.await())
        }
        
        return allResults.take(50) // Limit to 50 results
    }
    
    private fun parseMediaId(mediaId: String): Pair<String, String> {
        val parts = mediaId.split(":", limit = 2)
        return if (parts.size == 2) {
            parts[0] to parts[1]
        } else {
            "" to mediaId
        }
    }
    
    private fun createMediaId(extensionId: String, trackId: String): String {
        return "$extensionId:$trackId"
    }
    
    private fun updateQueueIndex(newIndex: Int) {
        _currentQueueIndex.value = newIndex
    }
    
    fun cleanup() {
        playbackScope.cancel()
    }
}

/**
 * Represents an item in the playback queue
 */
data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val extensionId: String
)

/**
 * Repeat mode options
 */
enum class RepeatMode {
    OFF, ONE, ALL
} 