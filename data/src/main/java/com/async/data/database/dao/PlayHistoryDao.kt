package com.async.data.database.dao

import androidx.room.*
import com.async.data.database.entity.PlayHistoryEntity
import com.async.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Play History operations
 */
@Dao
interface PlayHistoryDao {
    
    // ======== INSERT OPERATIONS ========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(playHistory: PlayHistoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistories(playHistories: List<PlayHistoryEntity>): List<Long>
    
    // ======== UPDATE OPERATIONS ========
    
    @Update
    suspend fun updatePlayHistory(playHistory: PlayHistoryEntity)
    
    // ======== DELETE OPERATIONS ========
    
    @Delete
    suspend fun deletePlayHistory(playHistory: PlayHistoryEntity)
    
    @Query("DELETE FROM play_history WHERE id = :historyId")
    suspend fun deletePlayHistoryById(historyId: Long)
    
    @Query("DELETE FROM play_history WHERE track_id = :trackId")
    suspend fun deletePlayHistoryForTrack(trackId: Long)
    
    @Query("DELETE FROM play_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldPlayHistory(cutoffTime: Long)
    
    @Query("DELETE FROM play_history")
    suspend fun deleteAllPlayHistory()
    
    // ======== QUERY OPERATIONS ========
    
    @Query("SELECT * FROM play_history WHERE id = :historyId")
    suspend fun getPlayHistoryById(historyId: Long): PlayHistoryEntity?
    
    @Query("SELECT * FROM play_history ORDER BY timestamp DESC")
    fun getAllPlayHistory(): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPlayHistory(limit: Int = 100): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history WHERE track_id = :trackId ORDER BY timestamp DESC")
    fun getPlayHistoryForTrack(trackId: Long): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getPlayHistoryForSession(sessionId: String): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history WHERE source = :source ORDER BY timestamp DESC")
    fun getPlayHistoryBySource(source: String): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getPlayHistoryInTimeRange(startTime: Long, endTime: Long): Flow<List<PlayHistoryEntity>>
    
    // ======== PLAY HISTORY WITH TRACKS ========
    
    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN play_history ph ON t.id = ph.track_id 
        ORDER BY ph.timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<TrackEntity>>
    
    @Query("""
        SELECT DISTINCT t.* FROM tracks t 
        INNER JOIN play_history ph ON t.id = ph.track_id 
        WHERE ph.timestamp >= :since
        ORDER BY ph.timestamp DESC
    """)
    fun getRecentlyPlayedTracksSince(since: Long): Flow<List<TrackEntity>>
    
    @Query("""
        SELECT t.*, COUNT(ph.id) as play_count FROM tracks t 
        INNER JOIN play_history ph ON t.id = ph.track_id 
        GROUP BY t.id 
        ORDER BY play_count DESC, MAX(ph.timestamp) DESC 
        LIMIT :limit
    """)
    fun getMostPlayedTracksFromHistory(limit: Int = 50): Flow<List<TrackEntity>>
    
    // ======== STATISTICS OPERATIONS ========
    
    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getTotalPlayHistoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM play_history WHERE track_id = :trackId")
    suspend fun getPlayCountForTrack(trackId: Long): Int
    
    @Query("SELECT COUNT(DISTINCT track_id) FROM play_history")
    suspend fun getUniqueTracksPlayedCount(): Int
    
    @Query("SELECT COUNT(DISTINCT session_id) FROM play_history WHERE session_id IS NOT NULL")
    suspend fun getUniqueSessionCount(): Int
    
    @Query("SELECT SUM(duration_played) FROM play_history")
    suspend fun getTotalListeningTime(): Long
    
    @Query("SELECT AVG(completion_percentage) FROM play_history WHERE completion_percentage > 0")
    suspend fun getAverageCompletionPercentage(): Double
    
    @Query("SELECT SUM(duration_played) FROM play_history WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getListeningTimeInRange(startTime: Long, endTime: Long): Long
    
    @Query("SELECT COUNT(*) FROM play_history WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getPlayCountInRange(startTime: Long, endTime: Long): Int
    
    // ======== ADVANCED ANALYTICS ========
    
    @Query("""
        SELECT track_id, COUNT(*) as play_count, SUM(duration_played) as total_time
        FROM play_history 
        WHERE timestamp >= :since
        GROUP BY track_id 
        ORDER BY play_count DESC 
        LIMIT :limit
    """)
    suspend fun getTopTracksStats(since: Long, limit: Int = 10): List<TrackPlayStats>
    
    @Query("""
        SELECT source, COUNT(*) as play_count 
        FROM play_history 
        WHERE source IS NOT NULL AND timestamp >= :since
        GROUP BY source 
        ORDER BY play_count DESC
    """)
    suspend fun getPlaySourceStats(since: Long): List<PlaySourceStats>
    
    @Query("""
        SELECT 
            strftime('%H', datetime(timestamp/1000, 'unixepoch', 'localtime')) as hour,
            COUNT(*) as play_count
        FROM play_history 
        WHERE timestamp >= :since
        GROUP BY hour 
        ORDER BY hour
    """)
    suspend fun getHourlyPlayStats(since: Long): List<HourlyPlayStats>
    
    @Query("""
        SELECT 
            date(timestamp/1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as play_count,
            SUM(duration_played) as total_time
        FROM play_history 
        WHERE timestamp >= :since
        GROUP BY date 
        ORDER BY date DESC
    """)
    suspend fun getDailyPlayStats(since: Long): List<DailyPlayStats>
    
    // ======== LISTENING STREAKS ========
    
    @Query("""
        SELECT DISTINCT date(timestamp/1000, 'unixepoch', 'localtime') as date
        FROM play_history 
        WHERE timestamp >= :since
        ORDER BY date DESC
    """)
    suspend fun getListeningDates(since: Long): List<String>
    
    @Query("""
        SELECT COUNT(DISTINCT date(timestamp/1000, 'unixepoch', 'localtime'))
        FROM play_history 
        WHERE timestamp >= :since
    """)
    suspend fun getListeningDaysCount(since: Long): Int
    
    // ======== MAINTENANCE OPERATIONS ========
    
    @Query("""
        DELETE FROM play_history 
        WHERE timestamp < :cutoffTime 
        AND completion_percentage < :minCompletion
    """)
    suspend fun cleanupIncompleteOldPlays(cutoffTime: Long, minCompletion: Float = 0.1f)
    
    @Query("""
        DELETE FROM play_history 
        WHERE id NOT IN (
            SELECT id FROM play_history 
            ORDER BY timestamp DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun keepOnlyRecentHistory(keepCount: Int = 10000)
}

/**
 * Data classes for statistics results
 */
data class TrackPlayStats(
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "play_count") val playCount: Int,
    @ColumnInfo(name = "total_time") val totalTime: Long
)

data class PlaySourceStats(
    val source: String,
    @ColumnInfo(name = "play_count") val playCount: Int
)

data class HourlyPlayStats(
    val hour: String,
    @ColumnInfo(name = "play_count") val playCount: Int
)

data class DailyPlayStats(
    val date: String,
    @ColumnInfo(name = "play_count") val playCount: Int,
    @ColumnInfo(name = "total_time") val totalTime: Long
) 