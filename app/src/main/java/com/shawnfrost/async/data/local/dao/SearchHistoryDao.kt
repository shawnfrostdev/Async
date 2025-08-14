package com.shawnfrost.async.data.local.dao

import androidx.room.*
import com.shawnfrost.async.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)
    
    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)
    
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getHistoryCount(): Int
    
    @Query("DELETE FROM search_history WHERE timestamp NOT IN (SELECT timestamp FROM search_history ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun limitHistorySize(limit: Int = 10)
} 