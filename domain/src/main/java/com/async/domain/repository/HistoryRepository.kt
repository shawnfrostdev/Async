package com.async.domain.repository

import com.async.core.result.AsyncResult
import com.async.domain.model.PlayHistoryItem
import com.async.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playback history and analytics
 */
interface HistoryRepository {
    
    // ======== RECORD PLAYBACK ========
    
    /**
     * Record a playback event
     */
    suspend fun recordPlayback(
        trackId: Long,
        durationPlayed: Long,
        completionPercentage: Float,
        source: String? = null,
        sourceId: String? = null,
        sessionId: String? = null
    ): AsyncResult<Unit, HistoryError>
    
    /**
     * Start a new listening session
     */
    suspend fun startListeningSession(): String // Returns session ID
    
    /**
     * End current listening session
     */
    suspend fun endListeningSession(sessionId: String)
    
    // ======== HISTORY RETRIEVAL ========
    
    /**
     * Get recent playback history
     */
    fun getRecentHistory(limit: Int = 100): Flow<List<PlayHistoryItem>>
    
    /**
     * Get playback history for specific track
     */
    fun getTrackHistory(trackId: Long): Flow<List<PlayHistoryItem>>
    
    /**
     * Get playback history for date range
     */
    fun getHistoryInRange(startTime: Long, endTime: Long): Flow<List<PlayHistoryItem>>
    
    /**
     * Get playback history for specific session
     */
    fun getSessionHistory(sessionId: String): Flow<List<PlayHistoryItem>>
    
    // ======== RECENTLY PLAYED ========
    
    /**
     * Get recently played tracks (deduplicated)
     */
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    /**
     * Get recently played tracks since timestamp
     */
    fun getRecentlyPlayedSince(timestamp: Long): Flow<List<Track>>
    
    /**
     * Check if track was played recently
     */
    suspend fun wasPlayedRecently(trackId: Long, withinHours: Int = 24): Boolean
    
    // ======== MOST PLAYED ========
    
    /**
     * Get most played tracks overall
     */
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    /**
     * Get most played tracks in time period
     */
    fun getMostPlayedInPeriod(
        startTime: Long, 
        endTime: Long, 
        limit: Int = 50
    ): Flow<List<Track>>
    
    /**
     * Get play count for specific track
     */
    suspend fun getTrackPlayCount(trackId: Long): Int
    
    // ======== LISTENING STATISTICS ========
    
    /**
     * Get overall listening statistics
     */
    suspend fun getListeningStats(): ListeningStats
    
    /**
     * Get listening statistics for time period
     */
    suspend fun getListeningStatsForPeriod(startTime: Long, endTime: Long): PeriodListeningStats
    
    /**
     * Get daily listening statistics
     */
    suspend fun getDailyStats(days: Int = 30): List<DailyStats>
    
    /**
     * Get hourly listening patterns
     */
    suspend fun getHourlyPatterns(days: Int = 7): List<HourlyStats>
    
    /**
     * Get listening streak (consecutive days)
     */
    suspend fun getCurrentListeningStreak(): Int
    
    /**
     * Get longest listening streak
     */
    suspend fun getLongestListeningStreak(): Int
    
    // ======== TOP TRACKS & INSIGHTS ========
    
    /**
     * Get top tracks by play count
     */
    suspend fun getTopTracksByPlayCount(
        limit: Int = 10,
        sinceTimestamp: Long? = null
    ): List<TrackPlayStats>
    
    /**
     * Get listening insights (genres, artists, etc.)
     */
    suspend fun getListeningInsights(days: Int = 30): ListeningInsights
    
    /**
     * Get track discovery rate (new vs repeated tracks)
     */
    suspend fun getDiscoveryRate(days: Int = 30): DiscoveryStats
    
    // ======== SEARCH & FILTERING ========
    
    /**
     * Search playback history
     */
    fun searchHistory(query: String): Flow<List<PlayHistoryItem>>
    
    /**
     * Get history by playback source
     */
    fun getHistoryBySource(source: String): Flow<List<PlayHistoryItem>>
    
    /**
     * Get incomplete playbacks (skipped songs)
     */
    fun getIncompletePlaybacks(maxCompletion: Float = 0.3f): Flow<List<PlayHistoryItem>>
    
    // ======== DATA MANAGEMENT ========
    
    /**
     * Clear all playback history
     */
    suspend fun clearAllHistory(): AsyncResult<Unit, HistoryError>
    
    /**
     * Clear history older than specified days
     */
    suspend fun clearOldHistory(olderThanDays: Int): AsyncResult<Unit, HistoryError>
    
    /**
     * Clear history for specific track
     */
    suspend fun clearTrackHistory(trackId: Long): AsyncResult<Unit, HistoryError>
    
    /**
     * Export history data
     */
    suspend fun exportHistory(): AsyncResult<String, HistoryError> // Returns JSON string
    
    /**
     * Import history data
     */
    suspend fun importHistory(jsonData: String): AsyncResult<Unit, HistoryError>
    
    // ======== MAINTENANCE ========
    
    /**
     * Cleanup incomplete playbacks older than specified time
     */
    suspend fun cleanupIncompletePlaybacks(olderThanDays: Int = 7)
    
    /**
     * Optimize history database (remove duplicates, etc.)
     */
    suspend fun optimizeHistoryDatabase()
    
    /**
     * Validate history integrity
     */
    suspend fun validateHistoryIntegrity(): List<HistoryError>
}

/**
 * Error types for history operations
 */
sealed class HistoryError {
    object DatabaseError : HistoryError()
    object TrackNotFound : HistoryError()
    object InvalidSession : HistoryError()
    object ExportError : HistoryError()
    object ImportError : HistoryError()
    data class ValidationError(val message: String) : HistoryError()
    data class InvalidTimeRange(val message: String) : HistoryError()
}

/**
 * Overall listening statistics
 */
data class ListeningStats(
    val totalPlaybackTime: Long, // in milliseconds
    val totalPlaybacks: Int,
    val uniqueTracksPlayed: Int,
    val averageSessionLength: Long,
    val averageCompletionRate: Float,
    val totalSessions: Int,
    val firstPlaybackTime: Long?,
    val lastPlaybackTime: Long?
)

/**
 * Listening statistics for a specific period
 */
data class PeriodListeningStats(
    val startTime: Long,
    val endTime: Long,
    val totalPlaybackTime: Long,
    val totalPlaybacks: Int,
    val uniqueTracksPlayed: Int,
    val averageCompletionRate: Float,
    val topGenre: String?,
    val topArtist: String?,
    val peakListeningHour: Int?
)

/**
 * Daily listening statistics
 */
data class DailyStats(
    val date: String, // YYYY-MM-DD format
    val totalPlaybackTime: Long,
    val playbackCount: Int,
    val uniqueTracks: Int,
    val sessionsCount: Int
)

/**
 * Hourly listening statistics
 */
data class HourlyStats(
    val hour: Int, // 0-23
    val playbackCount: Int,
    val averagePlaybackTime: Long
)

/**
 * Track play statistics
 */
data class TrackPlayStats(
    val trackId: Long,
    val playCount: Int,
    val totalPlayTime: Long,
    val averageCompletion: Float,
    val lastPlayed: Long
)

/**
 * Listening insights and patterns
 */
data class ListeningInsights(
    val topGenres: List<GenreStats>,
    val topArtists: List<ArtistStats>,
    val listeningPatterns: Map<String, Any>, // Flexible data for various insights
    val peakListeningTimes: List<TimeStats>,
    val skipRate: Float,
    val discoveryTrend: String // "increasing", "stable", "decreasing"
)

/**
 * Genre statistics
 */
data class GenreStats(
    val genre: String,
    val playCount: Int,
    val totalTime: Long,
    val percentage: Float
)

/**
 * Artist statistics
 */
data class ArtistStats(
    val artist: String,
    val playCount: Int,
    val totalTime: Long,
    val uniqueTracks: Int,
    val percentage: Float
)

/**
 * Time-based statistics
 */
data class TimeStats(
    val timeRange: String, // e.g., "14:00-15:00"
    val playCount: Int,
    val percentage: Float
)

/**
 * Track discovery statistics
 */
data class DiscoveryStats(
    val newTracksPlayed: Int,
    val repeatedTracksPlayed: Int,
    val discoveryRate: Float, // percentage of new tracks
    val explorationScore: Float // how diverse the listening is
) 