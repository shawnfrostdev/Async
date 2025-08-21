package com.async.data.repository

import com.async.core.model.SearchResult
import com.async.core.result.AsyncResult
import com.async.data.database.dao.TrackDao
import com.async.data.mapper.TrackMapper
import com.async.domain.model.Track
import com.async.domain.repository.TrackRepository
import com.async.domain.repository.TrackError
import com.async.domain.repository.TrackStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TrackRepository
 * Coordinates between local database and extension system
 */
@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper
) : TrackRepository {
    
    // ======== SEARCH OPERATIONS ========
    
    override suspend fun searchTracks(query: String): Flow<List<Track>> {
        return try {
            // Search local database for now
            // Extension search will be added when extension system is integrated
            trackDao.searchTracks(query).map { entities ->
                entities.map { trackMapper.toDomain(it) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching tracks for query: $query")
            flowOf(emptyList())
        }
    }
    
    override suspend fun searchTracksFromExtension(query: String, extensionId: String): Flow<List<Track>> {
        return try {
            // Extension search will be implemented when extension system is integrated
            Timber.d("Extension search not yet implemented for $extensionId")
            flowOf(emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Error searching extension $extensionId")
            flowOf(emptyList())
        }
    }
    
    override suspend fun getTrendingTracks(limit: Int): Flow<List<Track>> {
        return try {
            // For now, return most played tracks as trending
            // In the future, this could integrate with extension trending APIs
            getMostPlayedTracks(limit)
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending tracks")
            flowOf(emptyList())
        }
    }
    
    // ======== TRACK OPERATIONS ========
    
    override suspend fun getTrackById(trackId: Long): Track? {
        return try {
            trackDao.getTrackById(trackId)?.let { trackMapper.toDomain(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error getting track by ID: $trackId")
            null
        }
    }
    
    override suspend fun getTrackByExternalId(extensionId: String, externalId: String): Track? {
        return try {
            trackDao.getTrackByExternalId(extensionId, externalId)?.let { 
                trackMapper.toDomain(it) 
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting track by external ID: $extensionId:$externalId")
            null
        }
    }
    
    override fun getTrackByIdFlow(trackId: Long): Flow<Track?> {
        return trackDao.getTrackByIdFlow(trackId).map { entity ->
            entity?.let { trackMapper.toDomain(it) }
        }
    }
    
    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { trackMapper.toDomain(it) }
        }
    }
    
    override fun getTracksByExtension(extensionId: String): Flow<List<Track>> {
        return trackDao.getTracksByExtension(extensionId).map { entities ->
            entities.map { trackMapper.toDomain(it) }
        }
    }
    
    // ======== FAVORITES OPERATIONS ========
    
    override suspend fun addToFavorites(trackId: Long) {
        try {
            trackDao.updateFavoriteStatus(trackId, true)
            Timber.d("Added track $trackId to favorites")
        } catch (e: Exception) {
            Timber.e(e, "Error adding track $trackId to favorites")
        }
    }
    
    override suspend fun removeFromFavorites(trackId: Long) {
        try {
            trackDao.updateFavoriteStatus(trackId, false)
            Timber.d("Removed track $trackId from favorites")
        } catch (e: Exception) {
            Timber.e(e, "Error removing track $trackId from favorites")
        }
    }
    
    override suspend fun isFavorite(trackId: Long): Boolean {
        return try {
            getTrackById(trackId)?.isFavorite ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error checking favorite status for track $trackId")
            false
        }
    }
    
    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavoriteTracks().map { entities ->
            entities.map { trackMapper.toDomain(it) }
        }
    }
    
    // ======== PLAYBACK OPERATIONS ========
    
    override suspend fun getStreamUrl(track: Track): AsyncResult<String, TrackError> {
        return try {
            // If we have a cached stream URL, return it
            if (!track.streamUrl.isNullOrBlank()) {
                return AsyncResult.success(track.streamUrl!!)
            }
            
            // Extension integration will be added later
            // For now, return error if no cached URL
            Timber.w("No cached stream URL for track ${track.id}, extension integration pending")
            AsyncResult.error(TrackError.ExtensionNotAvailable(track.extensionId))
        } catch (e: Exception) {
            Timber.e(e, "Error getting stream URL for track ${track.id}")
            AsyncResult.error(TrackError.ExtensionError)
        }
    }
    
    override suspend fun recordPlayback(trackId: Long) {
        try {
            trackDao.incrementPlayCount(trackId)
            Timber.d("Recorded playback for track $trackId")
        } catch (e: Exception) {
            Timber.e(e, "Error recording playback for track $trackId")
        }
    }
    
    override fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> {
        return trackDao.getRecentlyPlayedTracks(limit).map { entities ->
            entities.map { trackMapper.toDomain(it) }
        }
    }
    
    override fun getMostPlayedTracks(limit: Int): Flow<List<Track>> {
        return trackDao.getMostPlayedTracks(limit).map { entities ->
            entities.map { trackMapper.toDomain(it) }
        }
    }
    
    // ======== CACHE OPERATIONS ========
    
    override suspend fun cacheTrack(searchResult: SearchResult): Track {
        return try {
            val existingTrack = trackDao.getTrackByExternalId(
                searchResult.extensionId, 
                searchResult.id
            )
            
            if (existingTrack != null) {
                // Update existing track
                val updatedEntity = trackMapper.updateFromSearchResult(existingTrack, searchResult)
                trackDao.updateTrack(updatedEntity)
                trackMapper.toDomain(updatedEntity)
            } else {
                // Create new track
                val newEntity = trackMapper.fromSearchResult(searchResult)
                val id = trackDao.insertTrack(newEntity)
                trackMapper.toDomain(newEntity.copy(id = id))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error caching track: ${searchResult.id}")
            throw e
        }
    }
    
    override suspend fun cacheTracks(searchResults: List<SearchResult>): List<Track> {
        return try {
            searchResults.map { cacheTrack(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error caching multiple tracks")
            emptyList()
        }
    }
    
    override suspend fun updateTrack(track: Track) {
        try {
            val entity = trackMapper.toEntity(track)
            trackDao.updateTrack(entity)
            Timber.d("Updated track ${track.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error updating track ${track.id}")
        }
    }
    
    override suspend fun removeTrack(trackId: Long) {
        try {
            trackDao.deleteTrackById(trackId)
            Timber.d("Removed track $trackId")
        } catch (e: Exception) {
            Timber.e(e, "Error removing track $trackId")
        }
    }
    
    // ======== STATISTICS OPERATIONS ========
    
    override suspend fun getTotalTrackCount(): Int {
        return try {
            trackDao.getTotalTrackCount()
        } catch (e: Exception) {
            Timber.e(e, "Error getting total track count")
            0
        }
    }
    
    override suspend fun getTrackStats(): TrackStats {
        return try {
            val totalTracks = trackDao.getTotalTrackCount()
            val totalFavorites = trackDao.getFavoriteTrackCount()
            val totalPlayTime = trackDao.getTotalDuration()
            val averagePlayCount = trackDao.getAveragePlayCount()
            val uniqueExtensions = trackDao.getUsedExtensionIds().size
            
            TrackStats(
                totalTracks = totalTracks,
                totalFavorites = totalFavorites,
                totalPlayTime = totalPlayTime,
                averagePlayCount = averagePlayCount,
                uniqueExtensions = uniqueExtensions
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting track stats")
            TrackStats(0, 0, 0, 0.0, 0)
        }
    }
    
    // ======== MAINTENANCE OPERATIONS ========
    
    override suspend fun cleanupOldTracks(olderThanDays: Int) {
        try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            trackDao.cleanupOldUnplayedTracks(cutoffTime)
            trackDao.clearOldStreamUrls(cutoffTime)
            Timber.d("Cleaned up tracks older than $olderThanDays days")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up old tracks")
        }
    }
    
    override suspend fun refreshTrackMetadata(trackId: Long): AsyncResult<Track, TrackError> {
        return try {
            val track = getTrackById(trackId) ?: return AsyncResult.error(TrackError.NotFound)
            
            // Extension integration will be added later
            // For now, just return the existing track
            Timber.d("Track metadata refresh not yet implemented, returning cached data")
            AsyncResult.success(track)
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing track metadata for $trackId")
            AsyncResult.error(TrackError.ExtensionError)
        }
    }
} 