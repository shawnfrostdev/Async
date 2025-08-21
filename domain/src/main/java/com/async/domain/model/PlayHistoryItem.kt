package com.async.domain.model

/**
 * Domain model representing a playback history entry
 */
data class PlayHistoryItem(
    val id: Long = 0,
    val trackId: Long,
    val track: Track? = null, // Optional embedded track data
    val timestamp: Long = System.currentTimeMillis(),
    val durationPlayed: Long, // in milliseconds
    val completionPercentage: Float, // 0.0 to 1.0
    val source: String? = null, // e.g., "playlist", "search", "recommendation"
    val sourceId: String? = null, // ID of the source (playlist ID, search query, etc.)
    val sessionId: String? = null, // for grouping related plays
    val deviceInfo: Map<String, Any> = emptyMap() // device information
) {
    
    /**
     * Get formatted timestamp
     */
    fun getFormattedTimestamp(): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    /**
     * Get formatted duration played
     */
    fun getFormattedDurationPlayed(): String {
        return Track.formatDuration(durationPlayed)
    }
    
    /**
     * Get completion percentage as string
     */
    fun getCompletionText(): String {
        return "${(completionPercentage * 100).toInt()}%"
    }
    
    /**
     * Check if the track was completed (played > 80%)
     */
    fun isCompleted(): Boolean = completionPercentage >= 0.8f
    
    /**
     * Check if the track was skipped (played < 30%)
     */
    fun wasSkipped(): Boolean = completionPercentage < 0.3f
    
    /**
     * Get playback quality description
     */
    fun getPlaybackQuality(): PlaybackQuality {
        return when {
            wasSkipped() -> PlaybackQuality.SKIPPED
            completionPercentage < 0.5f -> PlaybackQuality.PARTIAL
            completionPercentage < 0.8f -> PlaybackQuality.MOSTLY_PLAYED
            else -> PlaybackQuality.COMPLETED
        }
    }
    
    /**
     * Get source display text
     */
    fun getSourceDisplayText(): String {
        return when (source) {
            "playlist" -> "From playlist"
            "search" -> "From search"
            "recommendation" -> "Recommended"
            "album" -> "From album"
            "artist" -> "From artist"
            "radio" -> "From radio"
            "shuffle" -> "Shuffle mode"
            else -> source?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }
    }
    
    /**
     * Get time since played
     */
    fun getTimeSincePlayed(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> "More than a week ago"
        }
    }
    
    /**
     * Check if this is from today
     */
    fun isFromToday(): Boolean {
        val today = java.util.Calendar.getInstance()
        val playedDate = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(java.util.Calendar.YEAR) == playedDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == playedDate.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if this is from this week
     */
    fun isFromThisWeek(): Boolean {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return timestamp >= weekAgo
    }
    
    companion object {
        
        /**
         * Create a history item for a completed track
         */
        fun completed(
            trackId: Long,
            durationPlayed: Long,
            source: String? = null,
            sourceId: String? = null,
            sessionId: String? = null
        ) = PlayHistoryItem(
            trackId = trackId,
            durationPlayed = durationPlayed,
            completionPercentage = 1.0f,
            source = source,
            sourceId = sourceId,
            sessionId = sessionId
        )
        
        /**
         * Create a history item for a partially played track
         */
        fun partial(
            trackId: Long,
            durationPlayed: Long,
            totalDuration: Long,
            source: String? = null,
            sourceId: String? = null,
            sessionId: String? = null
        ) = PlayHistoryItem(
            trackId = trackId,
            durationPlayed = durationPlayed,
            completionPercentage = if (totalDuration > 0) durationPlayed.toFloat() / totalDuration else 0f,
            source = source,
            sourceId = sourceId,
            sessionId = sessionId
        )
        
        /**
         * Create a history item for a skipped track
         */
        fun skipped(
            trackId: Long,
            durationPlayed: Long = 0,
            source: String? = null,
            sourceId: String? = null,
            sessionId: String? = null
        ) = PlayHistoryItem(
            trackId = trackId,
            durationPlayed = durationPlayed,
            completionPercentage = 0f,
            source = source,
            sourceId = sourceId,
            sessionId = sessionId
        )
    }
}

/**
 * Enumeration for playback quality/completion
 */
enum class PlaybackQuality {
    SKIPPED,       // < 30%
    PARTIAL,       // 30% - 50%
    MOSTLY_PLAYED, // 50% - 80%
    COMPLETED      // >= 80%
} 