package app.async.app.ui.components.lyrics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable
data class LrcLine(
    val time: Long,
    val text: String
)

@Serializable
data class LrcLyrics(
    val syncType: Int,
    val lines: List<LrcLine>,
    val metadata: Map<String, String> = emptyMap()
)

class LyricsManager(
    private val scope: CoroutineScope,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val _currentLyrics = MutableStateFlow<LrcLyrics?>(null)
    val currentLyrics: StateFlow<LrcLyrics?> = _currentLyrics.asStateFlow()

    private val _currentLine = MutableStateFlow<LrcLine?>(null)
    val currentLine: StateFlow<LrcLine?> = _currentLine.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchLyrics(artist: String, title: String) {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
                val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                
                val request = Request.Builder()
                    .url("https://lrclib.net/api/search?artist_name=$encodedArtist&track_name=$encodedTitle")
                    .build()

                withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val json = response.body?.string()
                        if (json != null) {
                            val lyrics = Json.decodeFromString<LrcLyrics>(json)
                            _currentLyrics.value = lyrics
                        } else {
                            _error.value = "No lyrics found"
                        }
                    } else {
                        _error.value = "Failed to fetch lyrics: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error fetching lyrics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCurrentLine(currentTimeMs: Long) {
        val lyrics = _currentLyrics.value ?: return
        
        // Find the line that should be displayed at the current time
        val currentLine = lyrics.lines.lastOrNull { line -> 
            line.time <= currentTimeMs
        }
        
        if (currentLine != _currentLine.value) {
            _currentLine.value = currentLine
        }
    }

    fun clear() {
        _currentLyrics.value = null
        _currentLine.value = null
        _error.value = null
    }
} 
