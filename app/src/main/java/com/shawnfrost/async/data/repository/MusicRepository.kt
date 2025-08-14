package com.shawnfrost.async.data.repository

import com.shawnfrost.async.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun searchTracks(query: String): Flow<List<Track>>
    suspend fun getTrendingTracks(): Flow<List<Track>>
    suspend fun getNewReleases(): Flow<List<Track>>
    suspend fun getRecommendedTracks(): Flow<List<Track>>
    suspend fun getFeaturedTracks(): Flow<List<Track>>
} 