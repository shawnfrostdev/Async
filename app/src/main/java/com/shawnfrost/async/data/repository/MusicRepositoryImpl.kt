package com.shawnfrost.async.data.repository

import com.shawnfrost.async.data.api.FMAService
import com.shawnfrost.async.data.api.InternetArchiveService
import com.shawnfrost.async.data.api.JamendoService
import com.shawnfrost.async.data.local.dao.TrackDao
import com.shawnfrost.async.data.local.entity.TrackEntity
import com.shawnfrost.async.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val fmaService: FMAService,
    private val internetArchiveService: InternetArchiveService,
    private val jamendoService: JamendoService,
    private val trackDao: TrackDao
) : MusicRepository {

    override suspend fun searchTracks(query: String): Result<List<Track>> {
        return try {
            // Use Jamendo as primary source since FMA API is broken
            val jamendoResults = searchJamendo(query).getOrElse { emptyList() }
            val iaResults = searchInternetArchive(query).getOrElse { emptyList() }
            Result.success(jamendoResults + iaResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFMA(query: String): Result<List<Track>> {
        return try {
            val response = fmaService.searchTracks(query)
            val tracks = response.tracks.map { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            // FMA API is broken, return empty list
            Result.success(emptyList())
        }
    }

    override suspend fun searchInternetArchive(query: String): Result<List<Track>> {
        return try {
            val response = internetArchiveService.searchAudio(query)
            val tracks = response.response.docs.mapNotNull { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun searchJamendo(query: String): Result<List<Track>> {
        return try {
            val response = jamendoService.searchTracks(query = query)
            val tracks = response.results.map { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingTracks(): Result<List<Track>> {
        return try {
            val response = jamendoService.getTrendingTracks()
            val tracks = response.results.map { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNewReleases(): Result<List<Track>> {
        return try {
            val response = jamendoService.getNewReleases()
            val tracks = response.results.map { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLikedTracks(): Flow<List<Track>> {
        return trackDao.getLikedTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getTrackById(trackId: String): Track? {
        return trackDao.getTrackById(trackId)?.toDomainModel()
    }

    override suspend fun saveTrack(track: Track) {
        trackDao.insertTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track.toEntity())
    }

    override suspend fun toggleLikeTrack(trackId: String) {
        val track = trackDao.getTrackById(trackId)
        if (track != null) {
            trackDao.updateTrackLikeStatus(trackId, !track.isLiked)
        }
    }

    override suspend fun incrementPlayCount(trackId: String) {
        trackDao.incrementPlayCount(trackId)
    }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Track>> {
        return trackDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getMostPlayed(limit: Int): Flow<List<Track>> {
        return trackDao.getMostPlayed(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Extension functions for mapping
    private fun FMAService.Track.toDomainModel(): Track {
        return Track(
            id = track_id,
            title = track_title,
            artist = artist_name,
            duration = track_duration.toLongOrNull() ?: 0L,
            albumArt = track_image_file,
            mp3Url = track_url,
            flacUrl = null,
            license = license_title,
            source = "FMA"
        )
    }

    private fun InternetArchiveService.SearchResponse.Response.Document.toDomainModel(): Track? {
        if (format == null || (!format.contains("MP3") && !format.contains("FLAC"))) return null
        
        return Track(
            id = identifier,
            title = title,
            artist = creator ?: "Unknown Artist",
            duration = 0L, // Duration not available in search results
            albumArt = null,
            mp3Url = "https://archive.org/download/$identifier/$identifier.mp3",
            flacUrl = if (format.contains("FLAC")) 
                "https://archive.org/download/$identifier/$identifier.flac" else null,
            license = "Creative Commons",
            source = "IA"
        )
    }

    private fun TrackEntity.toDomainModel(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            duration = duration,
            albumArt = albumArt,
            mp3Url = mp3Url,
            flacUrl = flacUrl,
            license = license,
            source = source
        )
    }

    private fun Track.toEntity(): TrackEntity {
        return TrackEntity(
            id = id,
            title = title,
            artist = artist,
            duration = duration,
            albumArt = albumArt,
            mp3Url = mp3Url,
            flacUrl = flacUrl,
            license = license,
            source = source
        )
    }

    private fun JamendoService.Track.toDomainModel(): Track {
        return Track(
            id = "jamendo_$id",
            title = name,
            artist = artist_name,
            duration = duration.toLong() * 1000, // Convert seconds to milliseconds
            albumArt = album_image ?: track_image,
            mp3Url = audio,
            flacUrl = null, // Jamendo doesn't provide FLAC in free tier
            license = "Creative Commons",
            source = "Jamendo"
        )
    }
} 