package app.async.domain.repository

import app.async.core.model.SearchResult
import app.async.core.result.AsyncResult
import app.async.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for track-related operations
 */
interface TrackRepository {
    
    // ======== SEARCH OPERATIONS ========
    
    /**
     * Search tracks from local database and active extensions
     */
    suspend fun searchTracks(query: String): Flow<List<Track>>
    
    /**
     * Search tracks from a specific extension
     */
    suspend fun searchTracksFromExtension(query: String, extensionId: String): Flow<List<Track>>
    
    /**
     * Get trending or popular tracks from extensions
     */
    suspend fun getTrendingTracks(limit: Int = 50): Flow<List<Track>>
    
    // ======== TRACK OPERATIONS ========
    
    /**
     * Get track by internal database ID
     */
    suspend fun getTrackById(trackId: Long): Track?
    
    /**
     * Get track by external ID and extension
     */
    suspend fun getTrackByExternalId(extensionId: String, externalId: String): Track?
    
    /**
     * Get track by internal ID as Flow
     */
    fun getTrackByIdFlow(trackId: Long): Flow<Track?>
    
    /**
     * Get all tracks from local database
     */
    fun getAllTracks(): Flow<List<Track>>
    
    /**
     * Get tracks from specific extension
     */
    fun getTracksByExtension(extensionId: String): Flow<List<Track>>
    
    // ======== FAVORITES OPERATIONS ========
    
    /**
     * Add track to favorites
     */
    suspend fun addToFavorites(trackId: Long)
    
    /**
     * Remove track from favorites
     */
    suspend fun removeFromFavorites(trackId: Long)
    
    /**
     * Check if track is in favorites
     */
    suspend fun isFavorite(trackId: Long): Boolean
    
    /**
     * Get all favorite tracks
     */
    fun getFavoriteTracks(): Flow<List<Track>>
    
    // ======== LIKED TRACKS OPERATIONS ========
    
    /**
     * Add track to liked collection
     */
    suspend fun addLikedTrack(track: Track)
    
    /**
     * Remove track from liked collection
     */
    suspend fun removeLikedTrack(trackId: Long)
    
    /**
     * Check if track is liked
     */
    suspend fun isLiked(trackId: Long): Boolean
    
    /**
     * Get all liked tracks
     */
    fun getLikedTracks(): Flow<List<Track>>
    
    // ======== DOWNLOADS OPERATIONS ========
    
    /**
     * Add track to downloads
     */
    suspend fun addDownloadedTrack(track: Track, filePath: String)
    
    /**
     * Remove downloaded track
     */
    suspend fun deleteDownloadedTrack(trackId: Long)
    
    /**
     * Check if track is downloaded
     */
    suspend fun isDownloaded(trackId: Long): Boolean
    
    /**
     * Get all downloaded tracks
     */
    fun getDownloadedTracks(): Flow<List<Track>>
    
    // ======== PLAYBACK OPERATIONS ========
    
    /**
     * Get stream URL for a track
     */
    suspend fun getStreamUrl(track: Track): AsyncResult<String, TrackError>
    
    /**
     * Update play count and last played timestamp
     */
    suspend fun recordPlayback(trackId: Long)
    
    /**
     * Get recently played tracks
     */
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    /**
     * Get most played tracks
     */
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    // ======== CACHE OPERATIONS ========
    
    /**
     * Cache track from extension search result
     */
    suspend fun cacheTrack(searchResult: SearchResult): Track
    
    /**
     * Cache multiple tracks
     */
    suspend fun cacheTracks(searchResults: List<SearchResult>): List<Track>
    
    /**
     * Update cached track information
     */
    suspend fun updateTrack(track: Track)
    
    /**
     * Remove track from cache
     */
    suspend fun removeTrack(trackId: Long)
    
    // ======== STATISTICS OPERATIONS ========
    
    /**
     * Get total number of cached tracks
     */
    suspend fun getTotalTrackCount(): Int
    
    /**
     * Get track statistics
     */
    suspend fun getTrackStats(): TrackStats
    
    // ======== MAINTENANCE OPERATIONS ========
    
    /**
     * Cleanup old cached tracks
     */
    suspend fun cleanupOldTracks(olderThanDays: Int = 30)
    
    /**
     * Refresh track metadata from extensions
     */
    suspend fun refreshTrackMetadata(trackId: Long): AsyncResult<Track, TrackError>
}

/**
 * Error types for track operations
 */
sealed class TrackError {
    object NotFound : TrackError()
    object NetworkError : TrackError()
    object ExtensionError : TrackError()
    object DatabaseError : TrackError()
    data class InvalidUrl(val message: String) : TrackError()
    data class ExtensionNotAvailable(val extensionId: String) : TrackError()
}

/**
 * Track statistics data class
 */
data class TrackStats(
    val totalTracks: Int,
    val totalFavorites: Int,
    val totalPlayTime: Long,
    val averagePlayCount: Double,
    val uniqueExtensions: Int
) 
