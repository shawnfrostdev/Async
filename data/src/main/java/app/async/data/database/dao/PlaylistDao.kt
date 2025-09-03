package app.async.data.database.dao

import androidx.room.*
import app.async.data.database.entity.PlaylistEntity
import app.async.data.database.entity.PlaylistTrackEntity
import app.async.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Playlist operations
 */
@Dao
interface PlaylistDao {
    
    // ======== PLAYLIST OPERATIONS ========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>): List<Long>
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistByIdFlow(playlistId: Long): Flow<PlaylistEntity?>
    
    @Query("SELECT * FROM playlists WHERE name = :name")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?
    
    @Query("SELECT * FROM playlists ORDER BY sort_order ASC, date_created DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE is_system_playlist = 0 ORDER BY sort_order ASC, date_created DESC")
    fun getUserPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE is_system_playlist = 1 ORDER BY sort_order ASC")
    fun getSystemPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("UPDATE playlists SET track_count = :count, total_duration = :duration, last_modified = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistStats(playlistId: Long, count: Int, duration: Long, timestamp: Long = System.currentTimeMillis())
    
    // ======== PLAYLIST TRACK OPERATIONS ========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrackEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(playlistTracks: List<PlaylistTrackEntity>): List<Long>
    
    @Delete
    suspend fun deletePlaylistTrack(playlistTrack: PlaylistTrackEntity)
    
    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    
    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun removeAllTracksFromPlaylist(playlistId: Long)
    
    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun getMaxPositionInPlaylist(playlistId: Long): Int?
    
    @Query("UPDATE playlist_tracks SET position = position + 1 WHERE playlist_id = :playlistId AND position >= :position")
    suspend fun shiftPlaylistTracksDown(playlistId: Long, position: Int)
    
    @Query("UPDATE playlist_tracks SET position = position - 1 WHERE playlist_id = :playlistId AND position > :position")
    suspend fun shiftPlaylistTracksUp(playlistId: Long, position: Int)
    
    // ======== PLAYLIST WITH TRACKS QUERIES ========
    
    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.track_id 
        WHERE pt.playlist_id = :playlistId 
        ORDER BY pt.position ASC
    """)
    fun getPlaylistTracks(playlistId: Long): Flow<List<TrackEntity>>
    
    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.track_id 
        WHERE pt.playlist_id = :playlistId 
        ORDER BY pt.position ASC
    """)
    suspend fun getPlaylistTracksSync(playlistId: Long): List<TrackEntity>
    
    @Query("""
        SELECT pt.* FROM playlist_tracks pt 
        WHERE pt.playlist_id = :playlistId 
        ORDER BY pt.position ASC
    """)
    fun getPlaylistTrackEntities(playlistId: Long): Flow<List<PlaylistTrackEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM playlist_tracks 
        WHERE playlist_id = :playlistId
    """)
    suspend fun getPlaylistTrackCount(playlistId: Long): Int
    
    @Query("""
        SELECT SUM(t.duration) FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.track_id 
        WHERE pt.playlist_id = :playlistId AND t.duration IS NOT NULL
    """)
    suspend fun getPlaylistTotalDuration(playlistId: Long): Long?
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM playlist_tracks 
            WHERE playlist_id = :playlistId AND track_id = :trackId
        )
    """)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    
    // ======== SEARCH OPERATIONS ========
    
    @Query("""
        SELECT * FROM playlists 
        WHERE name LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            track_count DESC,
            date_created DESC
    """)
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>
    
    @Query("""
        SELECT DISTINCT p.* FROM playlists p
        INNER JOIN playlist_tracks pt ON p.id = pt.playlist_id
        INNER JOIN tracks t ON pt.track_id = t.id
        WHERE t.title LIKE '%' || :query || '%' 
           OR t.artist LIKE '%' || :query || '%' 
           OR t.album LIKE '%' || :query || '%'
        ORDER BY p.track_count DESC, p.date_created DESC
    """)
    fun searchPlaylistsByTrackContent(query: String): Flow<List<PlaylistEntity>>
    
    // ======== STATISTICS OPERATIONS ========
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getTotalPlaylistCount(): Int
    
    @Query("SELECT COUNT(*) FROM playlists WHERE is_system_playlist = 0")
    suspend fun getUserPlaylistCount(): Int
    
    @Query("SELECT AVG(track_count) FROM playlists WHERE track_count > 0")
    suspend fun getAveragePlaylistSize(): Double
    
    @Query("SELECT SUM(track_count) FROM playlists")
    suspend fun getTotalTracksInPlaylists(): Int
    
    // ======== MAINTENANCE OPERATIONS ========
    
    @Query("DELETE FROM playlists WHERE track_count = 0 AND is_system_playlist = 0 AND date_created < :cutoffTime")
    suspend fun cleanupEmptyUserPlaylists(cutoffTime: Long)
    
    @Transaction
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val maxPosition = getMaxPositionInPlaylist(playlistId) ?: -1
        val playlistTrack = PlaylistTrackEntity(
            playlistId = playlistId,
            trackId = trackId,
            position = maxPosition + 1
        )
        insertPlaylistTrack(playlistTrack)
        
        // Update playlist stats
        val trackCount = getPlaylistTrackCount(playlistId)
        val totalDuration = getPlaylistTotalDuration(playlistId) ?: 0
        updatePlaylistStats(playlistId, trackCount, totalDuration)
    }
    
    @Transaction
    suspend fun removeTrackFromPlaylistAndReorder(playlistId: Long, trackId: Long) {
        // Get the position of the track to be removed
        val playlistTrack = getPlaylistTrackEntities(playlistId).toString() // This would need proper implementation
        removeTrackFromPlaylist(playlistId, trackId)
        
        // Update playlist stats
        val trackCount = getPlaylistTrackCount(playlistId)
        val totalDuration = getPlaylistTotalDuration(playlistId) ?: 0
        updatePlaylistStats(playlistId, trackCount, totalDuration)
    }
    
    @Transaction
    suspend fun reorderPlaylistTrack(playlistId: Long, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        
        if (fromPosition < toPosition) {
            // Moving down: shift items up
            shiftPlaylistTracksUp(playlistId, fromPosition)
            shiftPlaylistTracksDown(playlistId, toPosition - 1)
        } else {
            // Moving up: shift items down
            shiftPlaylistTracksDown(playlistId, toPosition)
            shiftPlaylistTracksUp(playlistId, fromPosition)
        }
    }
} 
