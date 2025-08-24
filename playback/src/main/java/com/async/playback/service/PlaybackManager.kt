package com.async.playback.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultDataSource
import com.async.core.model.SearchResult
import com.async.extensions.service.ExtensionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import logcat.logcat

/**
 * ExoPlayer-based playback manager with extension integration
 */
@UnstableApi
class PlaybackManager(
    private val context: Context,
    private val extensionService: ExtensionService
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // ExoPlayer instance
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(
                    DefaultDataSource.Factory(
                        context,
                        DefaultHttpDataSource.Factory()
                            .setUserAgent("Async-Music-Player/1.0")
                            .setConnectTimeoutMs(30000)
                            .setReadTimeoutMs(30000)
                    )
                )
            )
            .build()
            .also { player ->
                player.addListener(playerListener)
            }
    }
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<SearchResult?>(null)
    val currentTrack: StateFlow<SearchResult?> = _currentTrack.asStateFlow()
    
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Queue management
    private val _queue = MutableStateFlow<List<SearchResult>>(emptyList())
    val queue: StateFlow<List<SearchResult>> = _queue.asStateFlow()
    
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    // Position tracking
    private var positionUpdateJob: kotlinx.coroutines.Job? = null
    
    init {
        startPositionUpdates()
    }
    
    /**
     * Play a track with extension integration
     */
    suspend fun playTrack(track: SearchResult) {
        logcat { "PlaybackManager: Playing track ${track.title} by ${track.artist}" }
        
        try {
            _isLoading.value = true
            _error.value = null
            _currentTrack.value = track
            
            // Get stream URL from extension
            val streamUrl = getStreamUrlFromExtension(track)
            
            if (streamUrl != null) {
                logcat { "PlaybackManager: Got stream URL: $streamUrl" }
                playStreamUrl(streamUrl, track)
            } else {
                logcat { "PlaybackManager: Failed to get stream URL for ${track.title}" }
                _error.value = "Unable to get stream URL for this track"
                _isLoading.value = false
            }
            
        } catch (e: Exception) {
            logcat { "PlaybackManager: Error playing track: ${e.message}" }
            _error.value = "Error playing track: ${e.message}"
            _isLoading.value = false
        }
    }
    
    /**
     * Get stream URL from extension
     */
    private suspend fun getStreamUrlFromExtension(track: SearchResult): String? {
        return try {
            // Find the extension that provided this track
            val extensionId = track.extensionId
            logcat { "PlaybackManager: Getting stream URL from extension: $extensionId" }
            
            val streamUrl = extensionService.getStreamUrl(extensionId, track.id)
            if (streamUrl != null) {
                logcat { "PlaybackManager: Successfully got stream URL from extension" }
                streamUrl
            } else {
                logcat { "PlaybackManager: Extension returned null stream URL" }
                null
            }
        } catch (e: Exception) {
            logcat { "PlaybackManager: Failed to get stream URL from extension: ${e.message}" }
            null
        }
    }
    
    /**
     * Play stream URL with ExoPlayer
     */
    private fun playStreamUrl(streamUrl: String, track: SearchResult) {
        try {
            logcat { "PlaybackManager: Setting up ExoPlayer with URL: ${streamUrl.take(100)}..." }
            
            val mediaItem = MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaId(track.id)
                .build()
            
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            
            logcat { "PlaybackManager: ExoPlayer setup complete, starting playback" }
            
        } catch (e: Exception) {
            logcat { "PlaybackManager: Error setting up ExoPlayer: ${e.message}" }
            _error.value = "Error setting up playback: ${e.message}"
            _isLoading.value = false
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        logcat { "PlaybackManager: Pausing playback" }
        exoPlayer.pause()
    }
    
    /**
     * Resume playback
     */
    fun resume() {
        logcat { "PlaybackManager: Resuming playback" }
        exoPlayer.play()
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        logcat { "PlaybackManager: Stopping playback" }
        exoPlayer.stop()
        _currentTrack.value = null
        _position.value = 0L
        _duration.value = 0L
        _error.value = null
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        logcat { "PlaybackManager: Seeking to $positionMs ms" }
        exoPlayer.seekTo(positionMs)
    }
    
    /**
     * Skip to next track
     */
    suspend fun skipToNext() {
        val currentQueue = _queue.value
        val currentIdx = _currentIndex.value
        
        if (currentQueue.isNotEmpty() && currentIdx < currentQueue.size - 1) {
            val nextIndex = currentIdx + 1
            _currentIndex.value = nextIndex
            playTrack(currentQueue[nextIndex])
        } else if (_repeatMode.value == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            _currentIndex.value = 0
            playTrack(currentQueue[0])
        }
    }
    
    /**
     * Skip to previous track
     */
    suspend fun skipToPrevious() {
        val currentQueue = _queue.value
        val currentIdx = _currentIndex.value
        
        if (currentQueue.isNotEmpty() && currentIdx > 0) {
            val prevIndex = currentIdx - 1
            _currentIndex.value = prevIndex
            playTrack(currentQueue[prevIndex])
        } else if (_repeatMode.value == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            val lastIndex = currentQueue.size - 1
            _currentIndex.value = lastIndex
            playTrack(currentQueue[lastIndex])
        }
    }
    
    /**
     * Set queue and play from index
     */
    suspend fun setQueue(tracks: List<SearchResult>, startIndex: Int = 0) {
        logcat { "PlaybackManager: Setting queue with ${tracks.size} tracks, starting at index $startIndex" }
        _queue.value = tracks
        _currentIndex.value = startIndex
        
        if (tracks.isNotEmpty() && startIndex < tracks.size) {
            playTrack(tracks[startIndex])
        }
    }
    
    /**
     * Add track to queue
     */
    fun addToQueue(track: SearchResult) {
        val currentQueue = _queue.value.toMutableList()
        currentQueue.add(track)
        _queue.value = currentQueue
        logcat { "PlaybackManager: Added ${track.title} to queue. Queue size: ${currentQueue.size}" }
    }
    
    /**
     * Remove track from queue
     */
    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index in currentQueue.indices) {
            val removedTrack = currentQueue.removeAt(index)
            _queue.value = currentQueue
            
            // Adjust current index if needed
            val currentIdx = _currentIndex.value
            if (index < currentIdx) {
                _currentIndex.value = currentIdx - 1
            } else if (index == currentIdx && currentQueue.isNotEmpty()) {
                // If we removed the currently playing track, play the next one
                scope.launch {
                    val newIndex = if (currentIdx < currentQueue.size) currentIdx else 0
                    _currentIndex.value = newIndex
                    playTrack(currentQueue[newIndex])
                }
            }
            
            logcat { "PlaybackManager: Removed ${removedTrack.title} from queue" }
        }
    }
    
    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        logcat { "PlaybackManager: Shuffle ${if (_shuffleEnabled.value) "enabled" else "disabled"}" }
    }
    
    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        logcat { "PlaybackManager: Repeat mode: ${_repeatMode.value}" }
    }
    
    /**
     * ExoPlayer listener for state changes
     */
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            logcat { "PlaybackManager: onIsPlayingChanged: $isPlaying" }
            _isPlaying.value = isPlaying
            _isLoading.value = false
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            logcat { "PlaybackManager: onPlaybackStateChanged: $playbackState" }
            when (playbackState) {
                Player.STATE_READY -> {
                    _isLoading.value = false
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                    logcat { "PlaybackManager: Track ready, duration: ${_duration.value}ms" }
                }
                Player.STATE_BUFFERING -> {
                    _isLoading.value = true
                    logcat { "PlaybackManager: Buffering..." }
                }
                Player.STATE_ENDED -> {
                    logcat { "PlaybackManager: Track ended" }
                    scope.launch {
                        when (_repeatMode.value) {
                            RepeatMode.ONE -> {
                                // Repeat current track
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                            }
                            RepeatMode.ALL, RepeatMode.OFF -> {
                                // Skip to next track
                                skipToNext()
                            }
                        }
                    }
                }
                Player.STATE_IDLE -> {
                    _isLoading.value = false
                }
            }
        }
        
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            logcat { "PlaybackManager: Player error: ${error.message}" }
            _error.value = "Playback error: ${error.message}"
            _isLoading.value = false
            _isPlaying.value = false
        }
    }
    
    /**
     * Start position updates
     */
    private fun startPositionUpdates() {
        positionUpdateJob = scope.launch {
            while (true) {
                if (_isPlaying.value && !_isLoading.value) {
                    _position.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                }
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        logcat { "PlaybackManager: Releasing resources" }
        positionUpdateJob?.cancel()
        exoPlayer.release()
    }
}

/**
 * Repeat mode enumeration
 */
enum class RepeatMode {
    OFF,    // No repeat
    ALL,    // Repeat all tracks in queue
    ONE     // Repeat current track
} 