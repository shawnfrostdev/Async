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
import logcat.logcat

/**
 * Implementation of TrackRepository
 * Coordinates between local database and extension system
 */
class TrackRepositoryImpl(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper
) : TrackRepository {
    
    // ======== SEARCH OPERATIONS ========
    
    override suspend fun searchTracks(query: String): Flow<List<Track>> {
        logcat { "Searching tracks with query: $query" }
        return trackDao.searchTracks("%$query%")
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    override suspend fun searchTracksFromExtension(query: String, extensionId: String): Flow<List<Track>> {
        logcat { "Searching tracks from extension: $extensionId with query: $query" }
        return trackDao.searchTracksByExtension("%$query%", extensionId)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    override suspend fun getTrendingTracks(limit: Int): Flow<List<Track>> {
        logcat { "Getting trending tracks with limit: $limit" }
        return trackDao.getTrendingTracks(limit)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    // ======== TRACK OPERATIONS ========
    
    override suspend fun getTrackById(trackId: Long): Track? {
        logcat { "Getting track with ID: $trackId" }
        return try {
            val entity = trackDao.getTrackById(trackId)
            entity?.let { trackMapper.mapEntityToDomain(it) }
        } catch (e: Exception) {
            logcat { "Error getting track: ${e.message}" }
            null
        }
    }
    
    override suspend fun getTrackByExternalId(extensionId: String, externalId: String): Track? {
        logcat { "Getting track by external ID: $extensionId:$externalId" }
        return try {
            val entity = trackDao.getTrackByExternalId(extensionId, externalId)
            entity?.let { trackMapper.mapEntityToDomain(it) }
        } catch (e: Exception) {
            logcat { "Error getting track by external ID: ${e.message}" }
            null
        }
    }
    
    override fun getTrackByIdFlow(trackId: Long): Flow<Track?> {
        logcat { "Getting track flow for ID: $trackId" }
        return trackDao.getTrackByIdFlow(trackId)
            .map { entity -> entity?.let { trackMapper.mapEntityToDomain(it) } }
    }
    
    override fun getAllTracks(): Flow<List<Track>> {
        logcat { "Getting all tracks" }
        return trackDao.getAllTracks()
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    override fun getTracksByExtension(extensionId: String): Flow<List<Track>> {
        logcat { "Getting tracks by extension: $extensionId" }
        return trackDao.getTracksByExtension(extensionId)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    // ======== FAVORITES OPERATIONS ========
    
    override suspend fun addToFavorites(trackId: Long) {
        try {
            logcat { "Adding track to favorites: $trackId" }
            trackDao.updateFavoriteStatus(trackId, true)
        } catch (e: Exception) {
            logcat { "Error adding to favorites: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun removeFromFavorites(trackId: Long) {
        try {
            logcat { "Removing track from favorites: $trackId" }
            trackDao.updateFavoriteStatus(trackId, false)
        } catch (e: Exception) {
            logcat { "Error removing from favorites: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun isFavorite(trackId: Long): Boolean {
        return try {
            logcat { "Checking if track is favorite: $trackId" }
            trackDao.isFavorite(trackId)
        } catch (e: Exception) {
            logcat { "Error checking favorite status: ${e.message}" }
            false
        }
    }
    
    override fun getFavoriteTracks(): Flow<List<Track>> {
        logcat { "Getting favorite tracks" }
        return trackDao.getFavoriteTracks()
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    // ======== PLAYBACK OPERATIONS ========
    
    override suspend fun getStreamUrl(track: Track): AsyncResult<String, TrackError> {
        return try {
            logcat { "Getting stream URL for track: ${track.title}" }
            if (!track.streamUrl.isNullOrBlank()) {
                AsyncResult.success(track.streamUrl!!)
            } else {
                AsyncResult.error(TrackError.InvalidUrl("Stream URL is empty"))
            }
        } catch (e: Exception) {
            logcat { "Error getting stream URL: ${e.message}" }
            AsyncResult.error(TrackError.NetworkError)
        }
    }
    
    override suspend fun recordPlayback(trackId: Long) {
        try {
            logcat { "Recording playback for track: $trackId" }
            trackDao.incrementPlayCount(trackId)
            trackDao.updateLastPlayed(trackId, System.currentTimeMillis())
        } catch (e: Exception) {
            logcat { "Error recording playback: ${e.message}" }
            throw e
        }
    }
    
    override fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> {
        logcat { "Getting recently played tracks with limit: $limit" }
        return trackDao.getRecentTracks(limit)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    override fun getMostPlayedTracks(limit: Int): Flow<List<Track>> {
        logcat { "Getting most played tracks with limit: $limit" }
        return trackDao.getMostPlayedTracks(limit)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }
    
    // ======== CACHE OPERATIONS ========
    
    override suspend fun cacheTrack(searchResult: SearchResult): Track {
        try {
            logcat { "Caching track: ${searchResult.title}" }
            
            // Convert SearchResult to Track
            val track = Track(
                id = 0, // Will be assigned by database
                externalId = searchResult.id,
                extensionId = searchResult.extensionId,
                title = searchResult.title,
                artist = searchResult.artist,
                album = searchResult.album,
                duration = searchResult.duration,
                thumbnailUrl = searchResult.thumbnailUrl,
                streamUrl = null, // Will be fetched when needed
                metadata = searchResult.metadata.map { it.key to it.value as Any }.toMap(),
                dateAdded = System.currentTimeMillis(),
                lastPlayed = null,
                playCount = 0,
                isFavorite = false,
                isDownloaded = false,
                downloadPath = null
            )
            
            // Check if track already exists
            val existingTrack = getTrackByExternalId(searchResult.extensionId, searchResult.id)
            if (existingTrack != null) {
                return existingTrack
            }
            
            val entity = trackMapper.mapDomainToEntity(track)
            val trackId = trackDao.insertTrack(entity)
            
            val insertedEntity = trackDao.getTrackById(trackId)
            return trackMapper.mapEntityToDomain(insertedEntity!!)
        } catch (e: Exception) {
            logcat { "Error caching track: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun cacheTracks(searchResults: List<SearchResult>): List<Track> {
        return try {
            logcat { "Caching ${searchResults.size} tracks" }
            searchResults.map { cacheTrack(it) }
        } catch (e: Exception) {
            logcat { "Error caching tracks: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun updateTrack(track: Track) {
        try {
            logcat { "Updating track with ID: ${track.id}" }
            val entity = trackMapper.mapDomainToEntity(track)
            trackDao.updateTrack(entity)
        } catch (e: Exception) {
            logcat { "Error updating track: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun removeTrack(trackId: Long) {
        try {
            logcat { "Removing track with ID: $trackId" }
            trackDao.deleteTrack(trackId)
        } catch (e: Exception) {
            logcat { "Error removing track: ${e.message}" }
            throw e
        }
    }
    
    // ======== STATISTICS OPERATIONS ========
    
    override suspend fun getTotalTrackCount(): Int {
        return try {
            logcat { "Getting total track count" }
            trackDao.getTotalTrackCount()
        } catch (e: Exception) {
            logcat { "Error getting total track count: ${e.message}" }
            0
        }
    }
    
    override suspend fun getTrackStats(): TrackStats {
        return try {
            logcat { "Getting track statistics" }
            
            val totalTracks = trackDao.getTotalTrackCount()
            val favoriteTracks = trackDao.getFavoriteTrackCount()
            val totalPlayCount = trackDao.getTotalPlayCount()
            val totalDuration = trackDao.getTotalDuration()
            val uniqueExtensions = trackDao.getUniqueExtensionCount()
            
            TrackStats(
                totalTracks = totalTracks,
                totalFavorites = favoriteTracks,
                totalPlayTime = totalDuration,
                averagePlayCount = if (totalTracks > 0) totalPlayCount.toDouble() / totalTracks else 0.0,
                uniqueExtensions = uniqueExtensions
            )
        } catch (e: Exception) {
            logcat { "Error getting track statistics: ${e.message}" }
            TrackStats(0, 0, 0, 0.0, 0)
        }
    }
    
    // ======== MAINTENANCE OPERATIONS ========
    
    override suspend fun cleanupOldTracks(olderThanDays: Int) {
        try {
            logcat { "Cleaning up tracks older than $olderThanDays days" }
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            trackDao.deleteOldTracks(cutoffTime)
        } catch (e: Exception) {
            logcat { "Error cleaning up old tracks: ${e.message}" }
            throw e
        }
    }
    
    override suspend fun refreshTrackMetadata(trackId: Long): AsyncResult<Track, TrackError> {
        return try {
            logcat { "Refreshing metadata for track: $trackId" }
            val track = getTrackById(trackId)
            if (track != null) {
                // In a real implementation, this would fetch fresh metadata from the extension
                AsyncResult.success(track)
            } else {
                AsyncResult.error(TrackError.NotFound)
            }
        } catch (e: Exception) {
            logcat { "Error refreshing track metadata: ${e.message}" }
            AsyncResult.error(TrackError.DatabaseError)
        }
    }
} 