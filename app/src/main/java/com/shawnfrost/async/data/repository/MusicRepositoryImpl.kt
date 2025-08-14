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
            // Clean the query first
            val cleanQuery = query.trim().replace("\n", " ")
            
            // Try real music sources only
            val jamendoResults = searchJamendo(cleanQuery).getOrElse { emptyList() }
            val iaResults = searchInternetArchive(cleanQuery).getOrElse { emptyList() }
            
            // Combine real results only
            val finalResults = jamendoResults + iaResults
            
            Result.success(finalResults)
        } catch (e: Exception) {
            // Return empty results on error - no fake data
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
            // Clean the query and add music-specific filters
            val cleanQuery = query.trim().replace("\n", " ")
            // Enhanced music filtering for better results
            val musicQuery = "collection:(opensource_audio OR etree OR community_audio OR freemusicarchive) AND mediatype:audio AND format:(VBR MP3 OR FLAC OR Ogg) AND ($cleanQuery)"
            
            val response = internetArchiveService.searchAudio(musicQuery)
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
            // Return error - no fake data
            Result.failure(e)
        }
    }

    override suspend fun getNewReleases(): Result<List<Track>> {
        return try {
            val response = jamendoService.getNewReleases()
            val tracks = response.results.map { it.toDomainModel() }
            Result.success(tracks)
        } catch (e: Exception) {
            // Return error - no fake data
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
        // Check for various audio formats in Internet Archive
        val hasAudioFormat = format?.any { formatItem ->
            formatItem.contains("MP3", ignoreCase = true) || 
            formatItem.contains("FLAC", ignoreCase = true) ||
            formatItem.contains("VBR", ignoreCase = true) ||
            formatItem.contains("Ogg", ignoreCase = true)
        } ?: false
        
        if (!hasAudioFormat) return null
        
        // Filter out non-music content (podcasts, speeches, etc.)
        val nonMusicKeywords = listOf(
            "podcast", "sermon", "speech", "lecture", "talk", "interview", 
            "homily", "radio", "news", "meditation", "prayer", "audiobook"
        )
        
        val titleLower = title.lowercase()
        val creatorLower = creator?.lowercase() ?: ""
        
        // Skip if title or creator suggests non-music content
        val isNonMusic = nonMusicKeywords.any { keyword ->
            titleLower.contains(keyword) || creatorLower.contains(keyword)
        }
        
        if (isNonMusic) return null
        
        // Clean up the title for better display
        val cleanTitle = when {
            title.contains(" - ") -> title.substringAfter(" - ").trim()
            title.contains(": ") -> title.substringAfter(": ").trim()
            else -> title
        }
        
        return Track(
            id = "ia_$identifier",
            title = cleanTitle,
            artist = creator ?: "Unknown Artist", 
            duration = 0L, // Duration not available in search results
            albumArt = null,
            mp3Url = "https://archive.org/download/$identifier/$identifier.mp3",
            flacUrl = if (format?.any { it.contains("FLAC", ignoreCase = true) } == true) 
                "https://archive.org/download/$identifier/$identifier.flac" else null,
            license = "Creative Commons",
            source = "Internet Archive"
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