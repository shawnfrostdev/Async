package com.async.data.repository

import com.async.core.result.AsyncResult
import com.async.data.database.dao.PlaylistDao
import com.async.data.database.dao.TrackDao
import com.async.data.mapper.PlaylistMapper
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import com.async.domain.repository.PlaylistRepository
import com.async.domain.repository.PlaylistError
import com.async.domain.repository.PlaylistStats
import com.async.domain.repository.PlaylistDetailStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlaylistRepository
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao,
    private val playlistMapper: PlaylistMapper
) : PlaylistRepository {
    
    // ======== PLAYLIST OPERATIONS ========
    
    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverArtUrl: String?
    ): AsyncResult<Playlist, PlaylistError> {
        return try {
            // Validate playlist name
            if (!Playlist.isValidName(name)) {
                val error = Playlist.getNameValidationError(name)
                return AsyncResult.error(PlaylistError.ValidationError(error ?: "Invalid name"))
            }
            
            // Check if playlist with same name exists
            val existing = playlistDao.getPlaylistByName(name)
            if (existing != null) {
                return AsyncResult.error(PlaylistError.DuplicateName)
            }
            
            // Create playlist
            val playlist = Playlist.createUserPlaylist(name, description).copy(
                coverArtUrl = coverArtUrl
            )
            val entity = playlistMapper.toEntity(playlist)
            val id = playlistDao.insertPlaylist(entity)
            
            val createdPlaylist = playlist.copy(id = id)
            Timber.d("Created playlist: ${createdPlaylist.name}")
            AsyncResult.success(createdPlaylist)
        } catch (e: Exception) {
            Timber.e(e, "Error creating playlist: $name")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun updatePlaylist(playlist: Playlist): AsyncResult<Playlist, PlaylistError> {
        return try {
            if (playlist.isSystemPlaylist) {
                return AsyncResult.error(PlaylistError.SystemPlaylistModification)
            }
            
            val entity = playlistMapper.toEntity(playlist.copy(
                lastModified = System.currentTimeMillis()
            ))
            playlistDao.updatePlaylist(entity)
            
            val updatedPlaylist = playlist.copy(lastModified = System.currentTimeMillis())
            Timber.d("Updated playlist: ${updatedPlaylist.name}")
            AsyncResult.success(updatedPlaylist)
        } catch (e: Exception) {
            Timber.e(e, "Error updating playlist: ${playlist.id}")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun deletePlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            val playlist = playlistDao.getPlaylistById(playlistId)
                ?: return AsyncResult.error(PlaylistError.NotFound)
            
            if (playlistMapper.toDomain(playlist).isSystemPlaylist) {
                return AsyncResult.error(PlaylistError.SystemPlaylistModification)
            }
            
            playlistDao.deletePlaylistById(playlistId)
            Timber.d("Deleted playlist: $playlistId")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting playlist: $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return try {
            playlistDao.getPlaylistById(playlistId)?.let { playlistMapper.toDomain(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error getting playlist by ID: $playlistId")
            null
        }
    }
    
    override fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistByIdFlow(playlistId).map { entity ->
            entity?.let { playlistMapper.toDomain(it) }
        }
    }
    
    override suspend fun getPlaylistByName(name: String): Playlist? {
        return try {
            playlistDao.getPlaylistByName(name)?.let { playlistMapper.toDomain(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error getting playlist by name: $name")
            null
        }
    }
    
    // ======== PLAYLIST COLLECTIONS ========
    
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { playlistMapper.toDomain(it) }
        }
    }
    
    override fun getUserPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getUserPlaylists().map { entities ->
            entities.map { playlistMapper.toDomain(it) }
        }
    }
    
    override fun getSystemPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getSystemPlaylists().map { entities ->
            entities.map { playlistMapper.toDomain(it) }
        }
    }
    
    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylists(query).map { entities ->
            entities.map { playlistMapper.toDomain(it) }
        }
    }
    
    // ======== TRACK MANAGEMENT ========
    
    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            // Check if playlist exists
            val playlist = playlistDao.getPlaylistById(playlistId)
                ?: return AsyncResult.error(PlaylistError.NotFound)
            
            // Check if track exists
            val track = trackDao.getTrackById(trackId)
                ?: return AsyncResult.error(PlaylistError.TrackNotFound)
            
            // Check if track is already in playlist
            if (playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                return AsyncResult.error(PlaylistError.TrackAlreadyInPlaylist)
            }
            
            // Add track to playlist
            playlistDao.addTrackToPlaylist(playlistId, trackId)
            Timber.d("Added track $trackId to playlist $playlistId")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding track $trackId to playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError> {
        return try {
            trackIds.forEach { trackId ->
                val result = addTrackToPlaylist(playlistId, trackId)
                if (result.isError) {
                    // Continue with other tracks even if one fails
                    Timber.w("Failed to add track $trackId to playlist $playlistId")
                }
            }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding tracks to playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            if (!playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                return AsyncResult.error(PlaylistError.TrackNotFound)
            }
            
            playlistDao.removeTrackFromPlaylistAndReorder(playlistId, trackId)
            Timber.d("Removed track $trackId from playlist $playlistId")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing track $trackId from playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError> {
        return try {
            trackIds.forEach { trackId ->
                val result = removeTrackFromPlaylist(playlistId, trackId)
                if (result.isError) {
                    Timber.w("Failed to remove track $trackId from playlist $playlistId")
                }
            }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing tracks from playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun clearPlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            playlistDao.removeAllTracksFromPlaylist(playlistId)
            Timber.d("Cleared playlist $playlistId")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return try {
            playlistDao.isTrackInPlaylist(playlistId, trackId)
        } catch (e: Exception) {
            Timber.e(e, "Error checking if track $trackId is in playlist $playlistId")
            false
        }
    }
    
    // ======== PLAYLIST CONTENT ========
    
    override fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        // TODO: Will need TrackMapper when implementing this
        return playlistDao.getPlaylistTracks(playlistId).map { entities ->
            // For now, create basic Track objects
            // This will be properly implemented when TrackMapper is integrated
            entities.map { entity ->
                Track(
                    id = entity.id,
                    externalId = entity.externalId,
                    extensionId = entity.extensionId,
                    title = entity.title,
                    artist = entity.artist,
                    album = entity.album,
                    duration = entity.duration,
                    thumbnailUrl = entity.thumbnailUrl,
                    streamUrl = entity.streamUrl,
                    dateAdded = entity.dateAdded,
                    lastPlayed = entity.lastPlayed,
                    playCount = entity.playCount,
                    isFavorite = entity.isFavorite,
                    isDownloaded = entity.isDownloaded,
                    downloadPath = entity.downloadPath
                )
            }
        }
    }
    
    override suspend fun getPlaylistTrackCount(playlistId: Long): Int {
        return try {
            playlistDao.getPlaylistTrackCount(playlistId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting track count for playlist $playlistId")
            0
        }
    }
    
    override suspend fun getPlaylistDuration(playlistId: Long): Long {
        return try {
            playlistDao.getPlaylistTotalDuration(playlistId) ?: 0
        } catch (e: Exception) {
            Timber.e(e, "Error getting duration for playlist $playlistId")
            0
        }
    }
    
    // ======== TRACK ORDERING ========
    
    override suspend fun reorderPlaylistTrack(
        playlistId: Long,
        fromPosition: Int,
        toPosition: Int
    ): AsyncResult<Unit, PlaylistError> {
        return try {
            playlistDao.reorderPlaylistTrack(playlistId, fromPosition, toPosition)
            Timber.d("Reordered track in playlist $playlistId from $fromPosition to $toPosition")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error reordering track in playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    override suspend fun moveTrackToPosition(
        playlistId: Long,
        trackId: Long,
        newPosition: Int
    ): AsyncResult<Unit, PlaylistError> {
        return try {
            // This would need proper implementation with position lookup
            // For now, just return success
            Timber.d("Move track position not fully implemented yet")
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error moving track position in playlist $playlistId")
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }
    
    // ======== SYSTEM PLAYLISTS ========
    
    override suspend fun getRecentlyPlayedPlaylist(): Playlist {
        return getSystemPlaylistOrCreate(
            Playlist.RECENTLY_PLAYED,
            "Your recently played tracks"
        )
    }
    
    override suspend fun getFavoritesPlaylist(): Playlist {
        return getSystemPlaylistOrCreate(
            Playlist.FAVORITES,
            "Your favorite tracks"
        )
    }
    
    override suspend fun getMostPlayedPlaylist(): Playlist {
        return getSystemPlaylistOrCreate(
            Playlist.MOST_PLAYED,
            "Your most played tracks"
        )
    }
    
    override suspend fun updateRecentlyPlayed(trackId: Long) {
        try {
            // Implementation would add track to recently played playlist
            Timber.d("Update recently played not fully implemented yet")
        } catch (e: Exception) {
            Timber.e(e, "Error updating recently played")
        }
    }
    
    override suspend fun refreshMostPlayedPlaylist() {
        try {
            // Implementation would refresh most played based on play counts
            Timber.d("Refresh most played not fully implemented yet")
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing most played playlist")
        }
    }
    
    // ======== STATISTICS ========
    
    override suspend fun getPlaylistStats(): PlaylistStats {
        return try {
            val totalPlaylists = playlistDao.getTotalPlaylistCount()
            val userPlaylists = playlistDao.getUserPlaylistCount()
            val systemPlaylists = totalPlaylists - userPlaylists
            val totalTracks = playlistDao.getTotalTracksInPlaylists()
            val averageSize = playlistDao.getAveragePlaylistSize()
            
            PlaylistStats(
                totalPlaylists = totalPlaylists,
                userPlaylists = userPlaylists,
                systemPlaylists = systemPlaylists,
                totalTracks = totalTracks,
                averagePlaylistSize = averageSize,
                largestPlaylistSize = 0, // Would need additional query
                totalDuration = 0 // Would need additional query
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting playlist stats")
            PlaylistStats(0, 0, 0, 0, 0.0, 0, 0)
        }
    }
    
    override suspend fun getPlaylistStats(playlistId: Long): PlaylistDetailStats {
        return try {
            val playlist = playlistDao.getPlaylistById(playlistId)
                ?: return PlaylistDetailStats(playlistId, 0, 0, 0, 0, 0, 0, 0, null)
            
            val trackCount = playlistDao.getPlaylistTrackCount(playlistId)
            val totalDuration = playlistDao.getPlaylistTotalDuration(playlistId) ?: 0
            val averageDuration = if (trackCount > 0) totalDuration / trackCount else 0
            
            PlaylistDetailStats(
                playlistId = playlistId,
                trackCount = trackCount,
                totalDuration = totalDuration,
                averageTrackDuration = averageDuration,
                uniqueArtists = 0, // Would need additional query
                uniqueExtensions = 0, // Would need additional query
                createdDate = playlist.dateCreated,
                lastModified = playlist.lastModified,
                lastPlayed = null // Would need additional tracking
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting playlist detail stats for $playlistId")
            PlaylistDetailStats(playlistId, 0, 0, 0, 0, 0, 0, 0, null)
        }
    }
    
    // ======== MAINTENANCE ========
    
    override suspend fun cleanupEmptyPlaylists() {
        try {
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
            playlistDao.cleanupEmptyUserPlaylists(cutoffTime)
            Timber.d("Cleaned up empty playlists")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up empty playlists")
        }
    }
    
    override suspend fun refreshPlaylistStats() {
        try {
            // Implementation would refresh stats for all playlists
            Timber.d("Refresh playlist stats not fully implemented yet")
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing playlist stats")
        }
    }
    
    override suspend fun validatePlaylists(): List<PlaylistError> {
        return try {
            // Implementation would validate playlist integrity
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error validating playlists")
            listOf(PlaylistError.DatabaseError)
        }
    }
    
    // ======== PRIVATE HELPER METHODS ========
    
    private suspend fun getSystemPlaylistOrCreate(name: String, description: String): Playlist {
        return try {
            val existing = playlistDao.getPlaylistByName(name)
            if (existing != null) {
                playlistMapper.toDomain(existing)
            } else {
                // Create system playlist
                val playlist = Playlist.createSystemPlaylist(name, description)
                val entity = playlistMapper.toEntity(playlist)
                val id = playlistDao.insertPlaylist(entity)
                playlist.copy(id = id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting or creating system playlist: $name")
            Playlist.createSystemPlaylist(name, description)
        }
    }
} 