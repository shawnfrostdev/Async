package app.async.data.mapper

import app.async.data.database.entity.PlaylistEntity
import app.async.domain.model.Playlist
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between Playlist domain models and PlaylistEntity database models
 */
@Singleton
class PlaylistMapper @Inject constructor() {
    
    /**
     * Convert PlaylistEntity to Playlist domain model
     */
    fun toDomain(entity: PlaylistEntity): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverArtUrl = entity.coverArtUrl,
            coverArtPath = entity.coverArtPath,
            trackCount = entity.trackCount,
            totalDuration = entity.totalDuration,
            dateCreated = entity.dateCreated,
            lastModified = entity.lastModified,
            isSystemPlaylist = entity.isSystemPlaylist,
            sortOrder = entity.sortOrder
        )
    }
    
    /**
     * Convert Playlist domain model to PlaylistEntity
     */
    fun toEntity(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverArtUrl = playlist.coverArtUrl,
            coverArtPath = playlist.coverArtPath,
            trackCount = playlist.trackCount,
            totalDuration = playlist.totalDuration,
            dateCreated = playlist.dateCreated,
            lastModified = playlist.lastModified,
            isSystemPlaylist = playlist.isSystemPlaylist,
            sortOrder = playlist.sortOrder
        )
    }
    
    /**
     * Convert multiple entities to domain models
     */
    fun toDomainList(entities: List<PlaylistEntity>): List<Playlist> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convert multiple domain models to entities
     */
    fun toEntityList(playlists: List<Playlist>): List<PlaylistEntity> {
        return playlists.map { toEntity(it) }
    }
    
    // Alias methods for repository compatibility
    fun mapEntityToDomain(entity: PlaylistEntity): Playlist = toDomain(entity)
    fun mapDomainToEntity(playlist: Playlist): PlaylistEntity = toEntity(playlist)
    fun mapEntitiesToDomain(entities: List<PlaylistEntity>): List<Playlist> = toDomainList(entities)
} 
