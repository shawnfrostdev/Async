package app.async.domain.model

/**
 * Domain model representing a music track
 */
data class Track(
    val id: Long = 0,
    val externalId: String,
    val extensionId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long?, // in milliseconds
    val thumbnailUrl: String?,
    val streamUrl: String?,
    val metadata: Map<String, Any> = emptyMap(),
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayed: Long? = null,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null
) {
    
    /**
     * Get display title for UI
     */
    fun getDisplayTitle(): String = title.takeIf { it.isNotBlank() } ?: "Unknown Title"
    
    /**
     * Get display artist for UI
     */
    fun getDisplayArtist(): String = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
    
    /**
     * Get display album for UI
     */
    fun getDisplayAlbum(): String = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
    
    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        return duration?.let { formatDuration(it) } ?: "--:--"
    }
    
    /**
     * Check if track has valid metadata
     */
    fun hasValidMetadata(): Boolean {
        return title.isNotBlank() && !artist.isNullOrBlank()
    }
    
    /**
     * Check if track is playable
     */
    fun isPlayable(): Boolean {
        return !streamUrl.isNullOrBlank() || isDownloaded
    }
    
    /**
     * Get unique identifier for this track
     */
    fun getUniqueId(): String = "$extensionId:$externalId"
    
    companion object {
        /**
         * Format duration from milliseconds to MM:SS or HH:MM:SS
         */
        fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }
        
        /**
         * Create empty track for placeholders
         */
        fun empty() = Track(
            externalId = "",
            extensionId = "",
            title = "",
            artist = null,
            album = null,
            duration = null,
            thumbnailUrl = null,
            streamUrl = null
        )
    }
} 
