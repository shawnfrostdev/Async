package com.async.data.database.dao

import androidx.room.*
import com.async.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Track operations
 */
@Dao
interface TrackDao {
    
    // ======== INSERT OPERATIONS ========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>): List<Long>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackIgnoreConflict(track: TrackEntity): Long
    
    // ======== UPDATE OPERATIONS ========
    
    @Update
    suspend fun updateTrack(track: TrackEntity)
    
    @Update
    suspend fun updateTracks(tracks: List<TrackEntity>)
    
    @Query("UPDATE tracks SET play_count = play_count + 1, last_played = :timestamp WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE tracks SET last_played = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: Long, timestamp: Long)
    
    @Query("UPDATE tracks SET is_favorite = :isFavorite WHERE id = :trackId")
    suspend fun updateFavoriteStatus(trackId: Long, isFavorite: Boolean)
    
    @Query("UPDATE tracks SET stream_url = :streamUrl WHERE id = :trackId")
    suspend fun updateStreamUrl(trackId: Long, streamUrl: String?)
    
    // ======== DELETE OPERATIONS ========
    
    @Delete
    suspend fun deleteTrack(track: TrackEntity)
    
    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: Long)
    
    @Query("DELETE FROM tracks WHERE id IN (:trackIds)")
    suspend fun deleteTracks(trackIds: List<Long>): Int
    
    @Query("DELETE FROM tracks WHERE extension_id = :extensionId")
    suspend fun deleteTracksByExtension(extensionId: String): Int
    
    @Query("DELETE FROM tracks WHERE date_added < :cutoffTime")
    suspend fun deleteOldTracks(cutoffTime: Long)
    
    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
    
    // ======== QUERY OPERATIONS ========
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: Long): TrackEntity?
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackByIdFlow(trackId: Long): Flow<TrackEntity?>
    
    @Query("SELECT * FROM tracks WHERE id IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<Long>): List<TrackEntity>
    
    @Query("SELECT * FROM tracks WHERE extension_id = :extensionId AND external_id = :externalId")
    suspend fun getTrackByExternalId(extensionId: String, externalId: String): TrackEntity?
    
    @Query("SELECT * FROM tracks WHERE extension_id = :extensionId AND external_id = :externalId")
    fun getTrackByExternalIdFlow(extensionId: String, externalId: String): Flow<TrackEntity?>
    
    @Query("SELECT * FROM tracks ORDER BY date_added DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks WHERE extension_id = :extensionId ORDER BY date_added DESC")
    fun getTracksByExtension(extensionId: String): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks WHERE is_favorite = 1 ORDER BY date_added DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks WHERE last_played IS NOT NULL ORDER BY last_played DESC LIMIT :limit")
    fun getRecentTracks(limit: Int = 50): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks WHERE last_played IS NOT NULL ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks ORDER BY play_count DESC, date_added DESC LIMIT :limit")
    fun getTrendingTracks(limit: Int = 50): Flow<List<TrackEntity>>
    
    @Query("SELECT is_favorite FROM tracks WHERE id = :trackId")
    suspend fun isFavorite(trackId: Long): Boolean
    
    // ======== SEARCH OPERATIONS ========
    
    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN artist LIKE :query || '%' THEN 2
                WHEN album LIKE :query || '%' THEN 3
                ELSE 4
            END,
            play_count DESC,
            date_added DESC
    """)
    fun searchTracks(query: String): Flow<List<TrackEntity>>
    
    @Query("""
        SELECT * FROM tracks 
        WHERE (title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%')
           AND extension_id = :extensionId
        ORDER BY play_count DESC, date_added DESC
    """)
    fun searchTracksByExtension(query: String, extensionId: String): Flow<List<TrackEntity>>
    
    // ======== STATISTICS OPERATIONS ========
    
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTotalTrackCount(): Int
    
    @Query("SELECT COUNT(*) FROM tracks WHERE extension_id = :extensionId")
    suspend fun getTrackCountByExtension(extensionId: String): Int
    
    @Query("SELECT COUNT(*) FROM tracks WHERE is_favorite = 1")
    suspend fun getFavoriteTrackCount(): Int
    
    @Query("SELECT SUM(play_count) FROM tracks")
    suspend fun getTotalPlayCount(): Int
    
    @Query("SELECT SUM(duration) FROM tracks WHERE duration IS NOT NULL")
    suspend fun getTotalDuration(): Long
    
    @Query("SELECT AVG(play_count) FROM tracks WHERE play_count > 0")
    suspend fun getAveragePlayCount(): Double
    
    @Query("SELECT COUNT(DISTINCT extension_id) FROM tracks")
    suspend fun getUniqueExtensionCount(): Int
    
    // ======== MAINTENANCE OPERATIONS ========
    
    @Query("DELETE FROM tracks WHERE last_played < :cutoffTime AND play_count = 0 AND is_favorite = 0")
    suspend fun cleanupOldUnplayedTracks(cutoffTime: Long)
    
    @Query("SELECT DISTINCT extension_id FROM tracks")
    suspend fun getUsedExtensionIds(): List<String>
    
    @Query("""
        UPDATE tracks SET stream_url = NULL 
        WHERE stream_url IS NOT NULL 
        AND last_played < :cutoffTime
    """)
    suspend fun clearOldStreamUrls(cutoffTime: Long)
} 