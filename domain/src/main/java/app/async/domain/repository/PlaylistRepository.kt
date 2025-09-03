package app.async.domain.repository

import app.async.core.result.AsyncResult
import app.async.domain.model.Playlist
import app.async.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playlist-related operations
 */
interface PlaylistRepository {
    
    // ======== PLAYLIST OPERATIONS ========
    
    /**
     * Create a new user playlist
     */
    suspend fun createPlaylist(
        name: String,
        description: String? = null,
        coverArtUrl: String? = null
    ): AsyncResult<Playlist, PlaylistError>
    
    /**
     * Update playlist information
     */
    suspend fun updatePlaylist(playlist: Playlist): AsyncResult<Playlist, PlaylistError>
    
    /**
     * Delete a playlist
     */
    suspend fun deletePlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError>
    
    /**
     * Get playlist by ID
     */
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    
    /**
     * Get playlist by ID as Flow
     */
    fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?>
    
    /**
     * Get playlist by name
     */
    suspend fun getPlaylistByName(name: String): Playlist?
    
    // ======== PLAYLIST COLLECTIONS ========
    
    /**
     * Get all playlists (user + system)
     */
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    /**
     * Get user-created playlists only
     */
    fun getUserPlaylists(): Flow<List<Playlist>>
    
    /**
     * Get system playlists (Recently Played, Favorites, etc.)
     */
    fun getSystemPlaylists(): Flow<List<Playlist>>
    
    /**
     * Search playlists by name or description
     */
    fun searchPlaylists(query: String): Flow<List<Playlist>>
    
    // ======== TRACK MANAGEMENT ========
    
    /**
     * Add track to playlist
     */
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError>
    
    /**
     * Add multiple tracks to playlist
     */
    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError>
    
    /**
     * Remove track from playlist
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError>
    
    /**
     * Remove multiple tracks from playlist
     */
    suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError>
    
    /**
     * Clear all tracks from playlist
     */
    suspend fun clearPlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError>
    
    /**
     * Check if track is in playlist
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    
    // ======== PLAYLIST CONTENT ========
    
    /**
     * Get tracks in a playlist (ordered)
     */
    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>>
    
    /**
     * Get playlist tracks count
     */
    suspend fun getPlaylistTrackCount(playlistId: Long): Int
    
    /**
     * Get playlist total duration
     */
    suspend fun getPlaylistDuration(playlistId: Long): Long
    
    // ======== TRACK ORDERING ========
    
    /**
     * Reorder track in playlist
     */
    suspend fun reorderPlaylistTrack(
        playlistId: Long, 
        fromPosition: Int, 
        toPosition: Int
    ): AsyncResult<Unit, PlaylistError>
    
    /**
     * Move track to specific position
     */
    suspend fun moveTrackToPosition(
        playlistId: Long, 
        trackId: Long, 
        newPosition: Int
    ): AsyncResult<Unit, PlaylistError>
    
    // ======== SYSTEM PLAYLISTS ========
    
    /**
     * Get or create "Recently Played" playlist
     */
    suspend fun getRecentlyPlayedPlaylist(): Playlist
    
    /**
     * Get or create "Favorites" playlist  
     */
    suspend fun getFavoritesPlaylist(): Playlist
    
    /**
     * Get or create "Most Played" playlist
     */
    suspend fun getMostPlayedPlaylist(): Playlist
    
    /**
     * Update recently played playlist with new track
     */
    suspend fun updateRecentlyPlayed(trackId: Long)
    
    /**
     * Update most played playlist based on play counts
     */
    suspend fun refreshMostPlayedPlaylist()
    
    // ======== STATISTICS ========
    
    /**
     * Get playlist statistics
     */
    suspend fun getPlaylistStats(): PlaylistStats
    
    /**
     * Get statistics for specific playlist
     */
    suspend fun getPlaylistStats(playlistId: Long): PlaylistDetailStats
    
    // ======== MAINTENANCE ========
    
    /**
     * Cleanup empty user playlists
     */
    suspend fun cleanupEmptyPlaylists()
    
    /**
     * Refresh all playlist statistics
     */
    suspend fun refreshPlaylistStats()
    
    /**
     * Validate playlist integrity
     */
    suspend fun validatePlaylists(): List<PlaylistError>
}

/**
 * Error types for playlist operations
 */
sealed class PlaylistError {
    object NotFound : PlaylistError()
    object DatabaseError : PlaylistError()
    object DuplicateName : PlaylistError()
    object SystemPlaylistModification : PlaylistError()
    object TrackNotFound : PlaylistError()
    object TrackAlreadyInPlaylist : PlaylistError()
    object InvalidPosition : PlaylistError()
    data class ValidationError(val message: String) : PlaylistError()
    data class NameTooLong(val maxLength: Int) : PlaylistError()
}

/**
 * Overall playlist statistics
 */
data class PlaylistStats(
    val totalPlaylists: Int,
    val userPlaylists: Int,
    val systemPlaylists: Int,
    val totalTracks: Int,
    val averagePlaylistSize: Double,
    val largestPlaylistSize: Int,
    val totalDuration: Long
)

/**
 * Detailed statistics for a specific playlist
 */
data class PlaylistDetailStats(
    val playlistId: Long,
    val trackCount: Int,
    val totalDuration: Long,
    val averageTrackDuration: Long,
    val uniqueArtists: Int,
    val uniqueExtensions: Int,
    val createdDate: Long,
    val lastModified: Long,
    val lastPlayed: Long?
) 
