package com.shawnfrost.async.data.repository

import com.shawnfrost.async.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Search
    suspend fun searchTracks(query: String): Result<List<Track>>
    suspend fun searchFMA(query: String): Result<List<Track>>
    suspend fun searchInternetArchive(query: String): Result<List<Track>>
    
    // Trending & New Releases
    suspend fun getTrendingTracks(): Result<List<Track>>
    suspend fun getNewReleases(): Result<List<Track>>
    
    // Local Database
    fun getAllTracks(): Flow<List<Track>>
    fun getLikedTracks(): Flow<List<Track>>
    suspend fun getTrackById(trackId: String): Track?
    suspend fun saveTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    suspend fun toggleLikeTrack(trackId: String)
    suspend fun incrementPlayCount(trackId: String)
    
    // Recently Played & Most Played
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Track>>
    fun getMostPlayed(limit: Int = 20): Flow<List<Track>>
} 