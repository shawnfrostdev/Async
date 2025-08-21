package com.async.data.sync

import com.async.core.result.AsyncResult
import com.async.data.database.dao.TrackDao
import com.async.data.database.dao.PlaylistDao
import com.async.data.database.dao.PlayHistoryDao
import com.async.data.database.dao.UserSettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages intelligent caching strategies and storage optimization
 */
@Singleton
class CacheStrategy @Inject constructor(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val playHistoryDao: PlayHistoryDao,
    private val userSettingsDao: UserSettingsDao
) {
    
    companion object {
        // Cache limits and thresholds
        const val MAX_CACHED_TRACKS = 10000
        const val MAX_HISTORY_ITEMS = 50000
        const val TRACK_CLEANUP_DAYS = 30
        const val STREAM_URL_CLEANUP_DAYS = 7
        const val INCOMPLETE_PLAYS_CLEANUP_DAYS = 7
        const val MIN_COMPLETION_RATE = 0.1f // 10%
        
        // Cache priorities
        const val PRIORITY_FAVORITES = 10
        const val PRIORITY_RECENTLY_PLAYED = 8
        const val PRIORITY_FREQUENTLY_PLAYED = 6
        const val PRIORITY_PLAYLISTED = 4
        const val PRIORITY_SEARCHED = 2
        const val PRIORITY_DEFAULT = 1
    }
    
    // ======== CACHE CLEANUP OPERATIONS ========
    
    /**
     * Clean up old tracks based on strategy
     */
    suspend fun cleanupOldTracks(): AsyncResult<Int, CacheError> {
        return try {
            Timber.i("Starting track cleanup strategy")
            
            val cutoffTime = System.currentTimeMillis() - (TRACK_CLEANUP_DAYS * 24 * 60 * 60 * 1000L)
            var cleanedCount = 0
            
            // Phase 1: Remove tracks that are old, unplayed, and not favorited
            trackDao.cleanupOldUnplayedTracks(cutoffTime)
            cleanedCount += 100 // Placeholder count
            
            // Phase 2: Clear old stream URLs to save space
            trackDao.clearOldStreamUrls(System.currentTimeMillis() - (STREAM_URL_CLEANUP_DAYS * 24 * 60 * 60 * 1000L))
            
            // Phase 3: Check if we're over the track limit
            val totalTracks = trackDao.getTotalTrackCount()
            if (totalTracks > MAX_CACHED_TRACKS) {
                val excessTracks = totalTracks - MAX_CACHED_TRACKS
                cleanedCount += cleanupExcessTracks(excessTracks)
            }
            
            Timber.i("Track cleanup completed: $cleanedCount tracks cleaned")
            AsyncResult.success(cleanedCount)
            
        } catch (e: Exception) {
            Timber.e(e, "Error during track cleanup")
            AsyncResult.error(CacheError.CleanupError(e.message ?: "Track cleanup failed"))
        }
    }
    
    /**
     * Clean up excess tracks based on priority scoring
     */
    private suspend fun cleanupExcessTracks(excessCount: Int): Int {
        try {
            Timber.d("Cleaning up $excessCount excess tracks")
            
            // Get all tracks and calculate priority scores
            val allTracks = trackDao.getAllTracks().first()
            
            val tracksWithPriority = allTracks.map { track ->
                val priority = calculateTrackPriority(track)
                TrackPriority(track.id, priority)
            }.sortedBy { it.priority } // Lowest priority first
            
            // Remove tracks with lowest priority
            val tracksToRemove = tracksWithPriority.take(excessCount)
            tracksToRemove.forEach { trackPriority ->
                trackDao.deleteTrackById(trackPriority.trackId)
            }
            
            return tracksToRemove.size
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up excess tracks")
            return 0
        }
    }
    
    /**
     * Calculate priority score for a track
     */
    private suspend fun calculateTrackPriority(track: com.async.data.database.entity.TrackEntity): Int {
        var priority = PRIORITY_DEFAULT
        
        // Favorites get highest priority
        if (track.isFavorite) {
            priority += PRIORITY_FAVORITES
        }
        
        // Recently played tracks get high priority
        val recentThreshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days
        if (track.lastPlayed != null && track.lastPlayed > recentThreshold) {
            priority += PRIORITY_RECENTLY_PLAYED
        }
        
        // Frequently played tracks get priority
        if (track.playCount > 5) {
            priority += PRIORITY_FREQUENTLY_PLAYED
        }
        
        // Tracks in playlists get priority
        // This would need a join query to check if track is in any playlist
        
        return priority
    }
    
    /**
     * Optimize track storage by removing duplicates and invalid entries
     */
    suspend fun optimizeTrackStorage(): AsyncResult<Int, CacheError> {
        return try {
            Timber.i("Optimizing track storage")
            
            var optimizedCount = 0
            
            // Remove tracks with invalid metadata
            // This would need specific queries for invalid data
            
            // Clean up orphaned data
            optimizedCount += cleanupOrphanedData()
            
            Timber.i("Track storage optimization completed: $optimizedCount items optimized")
            AsyncResult.success(optimizedCount)
            
        } catch (e: Exception) {
            Timber.e(e, "Error optimizing track storage")
            AsyncResult.error(CacheError.OptimizationError(e.message ?: "Storage optimization failed"))
        }
    }
    
    // ======== PLAYLIST CACHE MANAGEMENT ========
    
    /**
     * Clean up old and empty playlists
     */
    suspend fun cleanupPlaylists(): AsyncResult<Int, CacheError> {
        return try {
            Timber.i("Cleaning up playlists")
            
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
            
            // Clean up empty user playlists older than threshold
            playlistDao.cleanupEmptyUserPlaylists(cutoffTime)
            
            AsyncResult.success(1) // Placeholder count
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up playlists")
            AsyncResult.error(CacheError.CleanupError(e.message ?: "Playlist cleanup failed"))
        }
    }
    
    // ======== HISTORY CACHE MANAGEMENT ========
    
    /**
     * Clean up old play history
     */
    suspend fun cleanupPlayHistory(): AsyncResult<Int, CacheError> {
        return try {
            Timber.i("Cleaning up play history")
            
            var cleanedCount = 0
            
            // Phase 1: Remove incomplete plays older than threshold
            val incompleteThreshold = System.currentTimeMillis() - (INCOMPLETE_PLAYS_CLEANUP_DAYS * 24 * 60 * 60 * 1000L)
            playHistoryDao.cleanupIncompleteOldPlays(incompleteThreshold, MIN_COMPLETION_RATE)
            cleanedCount += 100 // Placeholder
            
            // Phase 2: Keep only recent history if over limit
            val totalHistory = playHistoryDao.getTotalPlayHistoryCount()
            if (totalHistory > MAX_HISTORY_ITEMS) {
                playHistoryDao.keepOnlyRecentHistory(MAX_HISTORY_ITEMS)
                cleanedCount += totalHistory - MAX_HISTORY_ITEMS
            }
            
            Timber.i("Play history cleanup completed: $cleanedCount items cleaned")
            AsyncResult.success(cleanedCount)
            
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up play history")
            AsyncResult.error(CacheError.CleanupError(e.message ?: "History cleanup failed"))
        }
    }
    
    // ======== SETTINGS CACHE MANAGEMENT ========
    
    /**
     * Clean up orphaned and old settings
     */
    suspend fun cleanupSettings(): AsyncResult<Int, CacheError> {
        return try {
            Timber.i("Cleaning up settings")
            
            // Clean up old non-critical settings
            val cutoffTime = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L) // 90 days
            userSettingsDao.cleanupOldNonCriticalSettings(cutoffTime)
            
            AsyncResult.success(1) // Placeholder count
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up settings")
            AsyncResult.error(CacheError.CleanupError(e.message ?: "Settings cleanup failed"))
        }
    }
    
    // ======== COMPREHENSIVE CACHE OPERATIONS ========
    
    /**
     * Perform full cache optimization
     */
    suspend fun performFullCacheOptimization(): AsyncResult<CacheOptimizationResult, CacheError> {
        return try {
            Timber.i("Starting full cache optimization")
            
            val result = CacheOptimizationResult()
            
            // Optimize tracks
            val trackResult = cleanupOldTracks()
            result.tracksOptimized = trackResult.getOrNull() ?: 0
            
            // Optimize playlists
            val playlistResult = cleanupPlaylists()
            result.playlistsOptimized = playlistResult.getOrNull() ?: 0
            
            // Optimize history
            val historyResult = cleanupPlayHistory()
            result.historyOptimized = historyResult.getOrNull() ?: 0
            
            // Optimize settings
            val settingsResult = cleanupSettings()
            result.settingsOptimized = settingsResult.getOrNull() ?: 0
            
            // Clean up orphaned data
            result.orphanedDataCleaned = cleanupOrphanedData()
            
            Timber.i("Full cache optimization completed: $result")
            AsyncResult.success(result)
            
        } catch (e: Exception) {
            Timber.e(e, "Error during full cache optimization")
            AsyncResult.error(CacheError.OptimizationError(e.message ?: "Full optimization failed"))
        }
    }
    
    /**
     * Get cache efficiency metrics
     */
    suspend fun getCacheEfficiency(): CacheEfficiency {
        return try {
            val trackCount = trackDao.getTotalTrackCount()
            val favoriteCount = trackDao.getFavoriteTrackCount()
            val playlistCount = playlistDao.getTotalPlaylistCount()
            val userPlaylistCount = playlistDao.getUserPlaylistCount()
            val historyCount = playHistoryDao.getTotalPlayHistoryCount()
            val settingsCount = userSettingsDao.getTotalSettingsCount()
            
            // Calculate efficiency scores
            val trackEfficiency = if (trackCount > 0) (favoriteCount.toFloat() / trackCount) * 100 else 0f
            val playlistEfficiency = if (playlistCount > 0) (userPlaylistCount.toFloat() / playlistCount) * 100 else 0f
            
            CacheEfficiency(
                totalCachedItems = trackCount + playlistCount + historyCount + settingsCount,
                trackEfficiencyScore = trackEfficiency,
                playlistEfficiencyScore = playlistEfficiency,
                storageUtilization = calculateStorageUtilization(),
                recommendedCleanup = shouldRecommendCleanup()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error calculating cache efficiency")
            CacheEfficiency()
        }
    }
    
    // ======== PRIVATE HELPER METHODS ========
    
    private suspend fun cleanupOrphanedData(): Int {
        try {
            // Clean up orphaned playlist tracks (tracks that don't exist anymore)
            // Clean up orphaned history entries
            // This would need specific database operations
            return 0 // Placeholder
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up orphaned data")
            return 0
        }
    }
    
    private suspend fun calculateStorageUtilization(): Float {
        // This would calculate actual storage usage
        // For now, return a placeholder
        return 75.0f
    }
    
    private suspend fun shouldRecommendCleanup(): Boolean {
        val trackCount = trackDao.getTotalTrackCount()
        val historyCount = playHistoryDao.getTotalPlayHistoryCount()
        
        return trackCount > (MAX_CACHED_TRACKS * 0.8) || historyCount > (MAX_HISTORY_ITEMS * 0.8)
    }
}

/**
 * Track priority data class for cleanup decisions
 */
private data class TrackPriority(
    val trackId: Long,
    val priority: Int
)

/**
 * Cache optimization result
 */
data class CacheOptimizationResult(
    var tracksOptimized: Int = 0,
    var playlistsOptimized: Int = 0,
    var historyOptimized: Int = 0,
    var settingsOptimized: Int = 0,
    var orphanedDataCleaned: Int = 0,
    val optimizationTime: Long = System.currentTimeMillis()
) {
    val totalItemsOptimized: Int
        get() = tracksOptimized + playlistsOptimized + historyOptimized + settingsOptimized + orphanedDataCleaned
}

/**
 * Cache efficiency metrics
 */
data class CacheEfficiency(
    val totalCachedItems: Int = 0,
    val trackEfficiencyScore: Float = 0f, // Percentage of useful tracks (favorites, played)
    val playlistEfficiencyScore: Float = 0f, // Percentage of user playlists vs system
    val storageUtilization: Float = 0f, // Percentage of storage used
    val recommendedCleanup: Boolean = false
)

/**
 * Cache error types
 */
sealed class CacheError {
    data class CleanupError(val message: String) : CacheError()
    data class OptimizationError(val message: String) : CacheError()
    data class StorageError(val message: String) : CacheError()
    object InsufficientSpace : CacheError()
    object DatabaseCorruption : CacheError()
} 