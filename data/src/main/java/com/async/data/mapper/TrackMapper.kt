package com.async.data.mapper

import com.async.core.model.SearchResult
import com.async.data.database.entity.TrackEntity
import com.async.domain.model.Track
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between Track domain models and TrackEntity database models
 */
@Singleton
class TrackMapper @Inject constructor() {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Convert TrackEntity to Track domain model
     */
    fun toDomain(entity: TrackEntity): Track {
        return Track(
            id = entity.id,
            externalId = entity.externalId,
            extensionId = entity.extensionId,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            duration = entity.duration,
            thumbnailUrl = entity.thumbnailUrl,
            streamUrl = entity.streamUrl,
            metadata = parseMetadata(entity.metadata),
            dateAdded = entity.dateAdded,
            lastPlayed = entity.lastPlayed,
            playCount = entity.playCount,
            isFavorite = entity.isFavorite,
            isDownloaded = entity.isDownloaded,
            downloadPath = entity.downloadPath
        )
    }
    
    /**
     * Convert Track domain model to TrackEntity
     */
    fun toEntity(track: Track): TrackEntity {
        return TrackEntity(
            id = track.id,
            externalId = track.externalId,
            extensionId = track.extensionId,
            title = track.title,
            artist = track.artist,
            album = track.album,
            duration = track.duration,
            thumbnailUrl = track.thumbnailUrl,
            streamUrl = track.streamUrl,
            metadata = encodeMetadata(track.metadata),
            dateAdded = track.dateAdded,
            lastPlayed = track.lastPlayed,
            playCount = track.playCount,
            isFavorite = track.isFavorite,
            isDownloaded = track.isDownloaded,
            downloadPath = track.downloadPath
        )
    }
    
    /**
     * Convert SearchResult from extension to TrackEntity
     */
    fun fromSearchResult(searchResult: SearchResult): TrackEntity {
        return TrackEntity(
            id = 0, // Will be assigned by database
            externalId = searchResult.id,
            extensionId = searchResult.extensionId,
            title = searchResult.title,
            artist = searchResult.artist,
            album = searchResult.album,
            duration = searchResult.duration,
            thumbnailUrl = searchResult.thumbnailUrl,
            streamUrl = null, // Will be fetched on demand
            metadata = encodeMetadata(searchResult.metadata),
            dateAdded = System.currentTimeMillis(),
            lastPlayed = null,
            playCount = 0,
            isFavorite = false,
            isDownloaded = false,
            downloadPath = null
        )
    }
    
    /**
     * Update existing TrackEntity with data from SearchResult
     */
    fun updateFromSearchResult(entity: TrackEntity, searchResult: SearchResult): TrackEntity {
        return entity.copy(
            title = searchResult.title,
            artist = searchResult.artist,
            album = searchResult.album,
            duration = searchResult.duration,
            thumbnailUrl = searchResult.thumbnailUrl,
            metadata = encodeMetadata(searchResult.metadata),
            // Keep existing values for play history, favorites, etc.
            lastPlayed = entity.lastPlayed,
            playCount = entity.playCount,
            isFavorite = entity.isFavorite,
            isDownloaded = entity.isDownloaded,
            downloadPath = entity.downloadPath
        )
    }
    
    /**
     * Convert multiple entities to domain models
     */
    fun toDomainList(entities: List<TrackEntity>): List<Track> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convert multiple domain models to entities
     */
    fun toEntityList(tracks: List<Track>): List<TrackEntity> {
        return tracks.map { toEntity(it) }
    }
    
    /**
     * Convert SearchResults to TrackEntities
     */
    fun fromSearchResults(searchResults: List<SearchResult>): List<TrackEntity> {
        return searchResults.map { fromSearchResult(it) }
    }
    
    /**
     * Parse metadata JSON string to Map
     */
    private fun parseMetadata(metadataJson: String?): Map<String, Any> {
        return try {
            if (metadataJson.isNullOrBlank()) {
                emptyMap()
            } else {
                json.decodeFromString<Map<String, Any>>(metadataJson)
            }
        } catch (e: Exception) {
            // If parsing fails, return empty map
            emptyMap()
        }
    }
    
    /**
     * Encode metadata Map to JSON string
     */
    private fun encodeMetadata(metadata: Map<String, Any>): String? {
        return try {
            if (metadata.isEmpty()) {
                null
            } else {
                json.encodeToString(metadata)
            }
        } catch (e: Exception) {
            // If encoding fails, return null
            null
        }
    }
    
    // Alias methods for repository compatibility
    fun mapEntityToDomain(entity: TrackEntity): Track = toDomain(entity)
    fun mapDomainToEntity(track: Track): TrackEntity = toEntity(track)
    fun mapEntitiesToDomain(entities: List<TrackEntity>): List<Track> = entities.map { toDomain(it) }
} 