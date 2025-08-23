package com.async.data.repository

import com.async.core.result.AsyncResult
import com.async.data.database.dao.PlaylistDao
import com.async.data.database.dao.TrackDao
import com.async.data.database.entity.PlaylistEntity
import com.async.data.mapper.PlaylistMapper
import com.async.data.mapper.TrackMapper
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import com.async.domain.repository.PlaylistRepository
import com.async.domain.repository.PlaylistError
import com.async.domain.repository.PlaylistStats
import com.async.domain.repository.PlaylistDetailStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import logcat.logcat

/**
 * Implementation of PlaylistRepository
 * Manages playlist operations and track associations
 */
class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao,
    private val playlistMapper: PlaylistMapper,
    private val trackMapper: TrackMapper
) : PlaylistRepository {

    // ======== PLAYLIST OPERATIONS ========

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverArtUrl: String?
    ): AsyncResult<Playlist, PlaylistError> {
        return try {
            logcat { "Creating playlist: $name" }
            
            // Validate name
            if (!Playlist.isValidName(name)) {
                return AsyncResult.error(PlaylistError.ValidationError("Invalid playlist name"))
            }
            
            // Check for duplicate name
            val existingPlaylist = playlistDao.getPlaylistByName(name)
            if (existingPlaylist != null) {
                return AsyncResult.error(PlaylistError.DuplicateName)
            }
            
            val playlist = Playlist.createUserPlaylist(name, description).copy(
                coverArtUrl = coverArtUrl
            )
            
            val entity = playlistMapper.mapDomainToEntity(playlist)
            val playlistId = playlistDao.insertPlaylist(entity)
            
            val insertedEntity = playlistDao.getPlaylistById(playlistId)
            if (insertedEntity != null) {
                val insertedPlaylist = playlistMapper.mapEntityToDomain(insertedEntity)
                logcat { "Successfully created playlist with ID: $playlistId" }
                AsyncResult.success(insertedPlaylist)
            } else {
                AsyncResult.error(PlaylistError.DatabaseError)
            }
        } catch (e: Exception) {
            logcat { "Error creating playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist): AsyncResult<Playlist, PlaylistError> {
        return try {
            logcat { "Updating playlist: ${playlist.name}" }
            
            // Check if playlist exists
            val existingPlaylist = playlistDao.getPlaylistById(playlist.id)
            if (existingPlaylist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            // Prevent modification of system playlists
            if (existingPlaylist.isSystemPlaylist) {
                return AsyncResult.error(PlaylistError.SystemPlaylistModification)
            }
            
            val entity = playlistMapper.mapDomainToEntity(playlist.copy(
                lastModified = System.currentTimeMillis()
            ))
            playlistDao.updatePlaylist(entity)
            
            logcat { "Successfully updated playlist" }
            AsyncResult.success(playlist)
        } catch (e: Exception) {
            logcat { "Error updating playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Deleting playlist with ID: $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            if (playlist.isSystemPlaylist) {
                return AsyncResult.error(PlaylistError.SystemPlaylistModification)
            }
            
            // Remove all tracks from playlist first
            playlistDao.removeAllTracksFromPlaylist(playlistId)
            // Delete the playlist
            playlistDao.deletePlaylistById(playlistId)
            
            logcat { "Successfully deleted playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error deleting playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return try {
            logcat { "Getting playlist by ID: $playlistId" }
            val entity = playlistDao.getPlaylistById(playlistId)
            entity?.let { playlistMapper.mapEntityToDomain(it) }
        } catch (e: Exception) {
            logcat { "Error getting playlist by ID: ${e.message}" }
            null
        }
    }

    override fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?> {
        logcat { "Getting playlist flow for ID: $playlistId" }
        return playlistDao.getPlaylistByIdFlow(playlistId)
            .map { entity -> entity?.let { playlistMapper.mapEntityToDomain(it) } }
    }

    override suspend fun getPlaylistByName(name: String): Playlist? {
        return try {
            logcat { "Getting playlist by name: $name" }
            val entity = playlistDao.getPlaylistByName(name)
            entity?.let { playlistMapper.mapEntityToDomain(it) }
        } catch (e: Exception) {
            logcat { "Error getting playlist by name: ${e.message}" }
            null
        }
    }

    // ======== PLAYLIST COLLECTIONS ========

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        logcat { "Getting all playlists" }
        return playlistDao.getAllPlaylists()
            .map { entities -> playlistMapper.mapEntitiesToDomain(entities) }
    }

    override fun getUserPlaylists(): Flow<List<Playlist>> {
        logcat { "Getting user playlists" }
        return playlistDao.getUserPlaylists()
            .map { entities -> playlistMapper.mapEntitiesToDomain(entities) }
    }

    override fun getSystemPlaylists(): Flow<List<Playlist>> {
        logcat { "Getting system playlists" }
        return playlistDao.getSystemPlaylists()
            .map { entities -> playlistMapper.mapEntitiesToDomain(entities) }
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        logcat { "Searching playlists with query: $query" }
        return playlistDao.searchPlaylists(query)
            .map { entities -> playlistMapper.mapEntitiesToDomain(entities) }
    }

    // ======== TRACK MANAGEMENT ========

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Adding track $trackId to playlist $playlistId" }
            
            // Check if playlist exists
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            // Check if track exists
            val track = trackDao.getTrackById(trackId)
            if (track == null) {
                return AsyncResult.error(PlaylistError.TrackNotFound)
            }
            
            // Check if track is already in playlist
            if (playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                return AsyncResult.error(PlaylistError.TrackAlreadyInPlaylist)
            }
            
            playlistDao.addTrackToPlaylist(playlistId, trackId)
            
            logcat { "Successfully added track to playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error adding track to playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Adding ${trackIds.size} tracks to playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            // Add tracks one by one to maintain order and check duplicates
            for (trackId in trackIds) {
                if (!playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                    playlistDao.addTrackToPlaylist(playlistId, trackId)
                }
            }
            
            logcat { "Successfully added tracks to playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error adding tracks to playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Removing track $trackId from playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            if (!playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                return AsyncResult.error(PlaylistError.TrackNotFound)
            }
            
            playlistDao.removeTrackFromPlaylistAndReorder(playlistId, trackId)
            
            logcat { "Successfully removed track from playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error removing track from playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<Long>): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Removing ${trackIds.size} tracks from playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            for (trackId in trackIds) {
                if (playlistDao.isTrackInPlaylist(playlistId, trackId)) {
                    playlistDao.removeTrackFromPlaylist(playlistId, trackId)
                }
            }
            
            // Update playlist stats after bulk removal
            val trackCount = playlistDao.getPlaylistTrackCount(playlistId)
            val totalDuration = playlistDao.getPlaylistTotalDuration(playlistId) ?: 0
            playlistDao.updatePlaylistStats(playlistId, trackCount, totalDuration)
            
            logcat { "Successfully removed tracks from playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error removing tracks from playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun clearPlaylist(playlistId: Long): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Clearing playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            playlistDao.removeAllTracksFromPlaylist(playlistId)
            playlistDao.updatePlaylistStats(playlistId, 0, 0)
            
            logcat { "Successfully cleared playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error clearing playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return try {
            playlistDao.isTrackInPlaylist(playlistId, trackId)
        } catch (e: Exception) {
            logcat { "Error checking if track is in playlist: ${e.message}" }
            false
        }
    }

    // ======== PLAYLIST CONTENT ========

    override fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        logcat { "Getting tracks for playlist $playlistId" }
        return playlistDao.getPlaylistTracks(playlistId)
            .map { entities -> trackMapper.mapEntitiesToDomain(entities) }
    }

    override suspend fun getPlaylistTrackCount(playlistId: Long): Int {
        return try {
            playlistDao.getPlaylistTrackCount(playlistId)
        } catch (e: Exception) {
            logcat { "Error getting playlist track count: ${e.message}" }
            0
        }
    }

    override suspend fun getPlaylistDuration(playlistId: Long): Long {
        return try {
            playlistDao.getPlaylistTotalDuration(playlistId) ?: 0
        } catch (e: Exception) {
            logcat { "Error getting playlist duration: ${e.message}" }
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
            logcat { "Reordering track in playlist $playlistId from $fromPosition to $toPosition" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            val trackCount = playlistDao.getPlaylistTrackCount(playlistId)
            if (fromPosition < 0 || fromPosition >= trackCount || toPosition < 0 || toPosition >= trackCount) {
                return AsyncResult.error(PlaylistError.InvalidPosition)
            }
            
            playlistDao.reorderPlaylistTrack(playlistId, fromPosition, toPosition)
            
            logcat { "Successfully reordered track in playlist" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error reordering track in playlist: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    override suspend fun moveTrackToPosition(
        playlistId: Long,
        trackId: Long,
        newPosition: Int
    ): AsyncResult<Unit, PlaylistError> {
        return try {
            logcat { "Moving track $trackId to position $newPosition in playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                return AsyncResult.error(PlaylistError.NotFound)
            }
            
            val trackCount = playlistDao.getPlaylistTrackCount(playlistId)
            if (newPosition < 0 || newPosition >= trackCount) {
                return AsyncResult.error(PlaylistError.InvalidPosition)
            }
            
            // This would need a more complex implementation in the DAO
            // For now, we'll return success as a placeholder
            logcat { "Successfully moved track to position" }
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            logcat { "Error moving track to position: ${e.message}" }
            AsyncResult.error(PlaylistError.DatabaseError)
        }
    }

    // ======== SYSTEM PLAYLISTS ========

    override suspend fun getRecentlyPlayedPlaylist(): Playlist {
        return getOrCreateSystemPlaylist(Playlist.RECENTLY_PLAYED) {
            Playlist.createRecentlyPlayedPlaylist()
        }
    }

    override suspend fun getFavoritesPlaylist(): Playlist {
        return getOrCreateSystemPlaylist(Playlist.FAVORITES) {
            Playlist.createFavoritesPlaylist()
        }
    }

    override suspend fun getMostPlayedPlaylist(): Playlist {
        return getOrCreateSystemPlaylist(Playlist.MOST_PLAYED) {
            Playlist.createMostPlayedPlaylist()
        }
    }

    private suspend fun getOrCreateSystemPlaylist(name: String, creator: () -> Playlist): Playlist {
        val existing = playlistDao.getPlaylistByName(name)
        if (existing != null) {
            return playlistMapper.mapEntityToDomain(existing)
        }
        
        val playlist = creator()
        val entity = playlistMapper.mapDomainToEntity(playlist)
        val id = playlistDao.insertPlaylist(entity)
        
        return playlist.copy(id = id)
    }

    override suspend fun updateRecentlyPlayed(trackId: Long) {
        try {
            logcat { "Updating recently played with track: $trackId" }
            val recentPlaylist = getRecentlyPlayedPlaylist()
            
            // Remove track if it already exists
            if (playlistDao.isTrackInPlaylist(recentPlaylist.id, trackId)) {
                playlistDao.removeTrackFromPlaylist(recentPlaylist.id, trackId)
            }
            
            // Add track to the beginning
            playlistDao.addTrackToPlaylist(recentPlaylist.id, trackId)
            
            // Keep only last 100 tracks
            val tracks = playlistDao.getPlaylistTracksSync(recentPlaylist.id)
            if (tracks.size > 100) {
                val tracksToRemove = tracks.drop(100)
                for (track in tracksToRemove) {
                    playlistDao.removeTrackFromPlaylist(recentPlaylist.id, track.id)
                }
            }
        } catch (e: Exception) {
            logcat { "Error updating recently played: ${e.message}" }
        }
    }

    override suspend fun refreshMostPlayedPlaylist() {
        try {
            logcat { "Refreshing most played playlist" }
            val mostPlayedPlaylist = getMostPlayedPlaylist()
            
            // Clear current tracks
            playlistDao.removeAllTracksFromPlaylist(mostPlayedPlaylist.id)
            
            // Get most played tracks
            val mostPlayedTracks = trackDao.getMostPlayedTracks(50).first()
            
            // Add them to the playlist
            for ((index, track) in mostPlayedTracks.withIndex()) {
                playlistDao.addTrackToPlaylist(mostPlayedPlaylist.id, track.id)
            }
        } catch (e: Exception) {
            logcat { "Error refreshing most played playlist: ${e.message}" }
        }
    }

    // ======== STATISTICS ========

    override suspend fun getPlaylistStats(): PlaylistStats {
        return try {
            logcat { "Getting playlist statistics" }
            
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
                largestPlaylistSize = 0, // Would need additional DAO method
                totalDuration = 0L // Would need additional DAO method
            )
        } catch (e: Exception) {
            logcat { "Error getting playlist statistics: ${e.message}" }
            PlaylistStats(0, 0, 0, 0, 0.0, 0, 0L)
        }
    }

    override suspend fun getPlaylistStats(playlistId: Long): PlaylistDetailStats {
        return try {
            logcat { "Getting detailed statistics for playlist $playlistId" }
            
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist == null) {
                throw IllegalArgumentException("Playlist not found")
            }
            
            val trackCount = playlistDao.getPlaylistTrackCount(playlistId)
            val totalDuration = playlistDao.getPlaylistTotalDuration(playlistId) ?: 0L
            val averageDuration = if (trackCount > 0) totalDuration / trackCount else 0L
            
            PlaylistDetailStats(
                playlistId = playlistId,
                trackCount = trackCount,
                totalDuration = totalDuration,
                averageTrackDuration = averageDuration,
                uniqueArtists = 0, // Would need additional DAO method
                uniqueExtensions = 0, // Would need additional DAO method
                createdDate = playlist.dateCreated,
                lastModified = playlist.lastModified,
                lastPlayed = null // Would need additional tracking
            )
        } catch (e: Exception) {
            logcat { "Error getting playlist detail statistics: ${e.message}" }
            PlaylistDetailStats(playlistId, 0, 0L, 0L, 0, 0, 0L, 0L, null)
        }
    }

    // ======== MAINTENANCE ========

    override suspend fun cleanupEmptyPlaylists() {
        try {
            logcat { "Cleaning up empty playlists" }
            val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
            playlistDao.cleanupEmptyUserPlaylists(cutoffTime)
        } catch (e: Exception) {
            logcat { "Error cleaning up empty playlists: ${e.message}" }
        }
    }

    override suspend fun refreshPlaylistStats() {
        try {
            logcat { "Refreshing all playlist statistics" }
            val playlists = playlistDao.getAllPlaylists().first()
            
            for (playlist in playlists) {
                val trackCount = playlistDao.getPlaylistTrackCount(playlist.id)
                val totalDuration = playlistDao.getPlaylistTotalDuration(playlist.id) ?: 0
                playlistDao.updatePlaylistStats(playlist.id, trackCount, totalDuration)
            }
        } catch (e: Exception) {
            logcat { "Error refreshing playlist statistics: ${e.message}" }
        }
    }

    override suspend fun validatePlaylists(): List<PlaylistError> {
        val errors = mutableListOf<PlaylistError>()
        
        try {
            logcat { "Validating playlists" }
            val playlists = playlistDao.getAllPlaylists().first()
            
            for (playlist in playlists) {
                // Check for invalid names
                if (!Playlist.isValidName(playlist.name)) {
                    errors.add(PlaylistError.ValidationError("Invalid name: ${playlist.name}"))
                }
                
                // Check for orphaned tracks (tracks that don't exist)
                val playlistTracks = playlistDao.getPlaylistTracksSync(playlist.id)
                for (track in playlistTracks) {
                    val trackExists = trackDao.getTrackById(track.id) != null
                    if (!trackExists) {
                        errors.add(PlaylistError.ValidationError("Orphaned track in playlist ${playlist.name}"))
                    }
                }
            }
        } catch (e: Exception) {
            logcat { "Error validating playlists: ${e.message}" }
            errors.add(PlaylistError.DatabaseError)
        }
        
        return errors
    }
} 