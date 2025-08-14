package com.shawnfrost.async.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawnfrost.async.domain.model.Track
import com.shawnfrost.async.service.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var audioPlayerService: AudioPlayerService? = null
    private var isServiceBound = false

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.AudioPlayerBinder
            audioPlayerService = binder.getService()
            isServiceBound = true
            _isServiceConnected.value = true
            
            // Observe service state
            viewModelScope.launch {
                audioPlayerService?.currentTrack?.collect { track ->
                    _currentTrack.value = track
                }
            }
            
            viewModelScope.launch {
                audioPlayerService?.isPlaying?.collect { playing ->
                    _isPlaying.value = playing
                }
            }
            
            viewModelScope.launch {
                audioPlayerService?.playbackPosition?.collect { position ->
                    _playbackPosition.value = position
                }
            }
            
            viewModelScope.launch {
                audioPlayerService?.duration?.collect { duration ->
                    _duration.value = duration
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioPlayerService = null
            isServiceBound = false
            _isServiceConnected.value = false
        }
    }

    init {
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent(context, AudioPlayerService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun playTrack(track: Track) {
        audioPlayerService?.playTrack(track)
    }

    fun pausePlayback() {
        audioPlayerService?.pausePlayback()
    }

    fun resumePlayback() {
        audioPlayerService?.resumePlayback()
    }

    fun stopPlayback() {
        audioPlayerService?.stopPlayback()
    }

    fun seekTo(position: Long) {
        audioPlayerService?.seekTo(position)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    fun getCurrentPosition(): Long {
        return audioPlayerService?.getCurrentPosition() ?: 0L
    }

    fun getDuration(): Long {
        return audioPlayerService?.getDuration() ?: 0L
    }

    override fun onCleared() {
        super.onCleared()
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
    }
} 