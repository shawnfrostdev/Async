package com.async.core.extension

import com.async.core.model.Album
import com.async.core.model.Artist
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult

/**
 * Core interface that all music extensions must implement.
 * This defines the contract between the Async app and external music sources.
 * 
 * Extensions implementing this interface can be dynamically loaded by the app
 * to provide music search and streaming capabilities from various sources.
 */
interface MusicExtension {
    
    /**
     * Unique identifier for this extension.
     * Must be unique across all extensions and follow reverse domain naming (e.g., "com.example.myextension")
     */
    val id: String
    
    /**
     * Version of this extension for migration and compatibility purposes.
     * Should be incremented when making breaking changes.
     */
    val version: Int
    
    /**
     * Human-readable name of this extension displayed in the UI
     */
    val name: String
    
    /**
     * Developer or organization name
     */
    val developer: String
    
    /**
     * Extension description shown to users
     */
    val description: String
    
    /**
     * URL to extension's icon/logo (optional)
     */
    val iconUrl: String?
        get() = null
    
    /**
     * Website or source code URL (optional)
     */
    val websiteUrl: String?
        get() = null
    
    /**
     * Minimum API level this extension requires
     */
    val minApiLevel: Int
        get() = 1
    
    /**
     * Maximum API level this extension supports
     */
    val maxApiLevel: Int
        get() = Int.MAX_VALUE
    
    /**
     * Whether this extension requires network access
     */
    val requiresNetwork: Boolean
        get() = true
    
    /**
     * Search for music tracks based on a query string.
     * 
     * @param query Search query (artist, song title, album, etc.)
     * @param limit Maximum number of results to return (optional)
     * @param offset Pagination offset (optional)
     * @return ExtensionResult containing list of SearchResult objects
     */
    suspend fun search(
        query: String,
        limit: Int = 50,
        offset: Int = 0
    ): ExtensionResult<List<SearchResult>>
    
    /**
     * Get the streaming URL for a specific track.
     * This is called when the user wants to play a track.
     * 
     * @param mediaId The ID from SearchResult.id
     * @return ExtensionResult containing the direct streaming URL
     */
    suspend fun getStreamUrl(mediaId: String): ExtensionResult<String>
    
    /**
     * Download album artwork or track thumbnail.
     * 
     * @param url The URL from SearchResult.thumbnailUrl or Album.artworkUrl
     * @return ExtensionResult containing the image data as ByteArray
     */
    suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray>
    
    /**
     * Get detailed information about an artist (optional).
     * Extensions may choose not to implement this if artist data is not available.
     * 
     * @param artistId Unique identifier for the artist
     * @return ExtensionResult containing Artist information
     */
    suspend fun getArtist(artistId: String): ExtensionResult<Artist> {
        return ExtensionResult.Error(
            com.async.core.model.ExtensionException.GenericError("Artist lookup not supported by this extension")
        )
    }
    
    /**
     * Get detailed information about an album (optional).
     * Extensions may choose not to implement this if album data is not available.
     * 
     * @param albumId Unique identifier for the album
     * @return ExtensionResult containing Album information
     */
    suspend fun getAlbum(albumId: String): ExtensionResult<Album> {
        return ExtensionResult.Error(
            com.async.core.model.ExtensionException.GenericError("Album lookup not supported by this extension")
        )
    }
    
    /**
     * Get tracks from a specific album (optional).
     * 
     * @param albumId Unique identifier for the album
     * @return ExtensionResult containing list of tracks in the album
     */
    suspend fun getAlbumTracks(albumId: String): ExtensionResult<List<SearchResult>> {
        return ExtensionResult.Error(
            com.async.core.model.ExtensionException.GenericError("Album tracks not supported by this extension")
        )
    }
    
    /**
     * Get tracks by a specific artist (optional).
     * 
     * @param artistId Unique identifier for the artist
     * @param limit Maximum number of results to return
     * @param offset Pagination offset
     * @return ExtensionResult containing list of tracks by the artist
     */
    suspend fun getArtistTracks(
        artistId: String,
        limit: Int = 50,
        offset: Int = 0
    ): ExtensionResult<List<SearchResult>> {
        return ExtensionResult.Error(
            com.async.core.model.ExtensionException.GenericError("Artist tracks not supported by this extension")
        )
    }
    
    /**
     * Initialize the extension. Called when the extension is loaded.
     * Extensions can perform setup tasks here.
     * 
     * @return ExtensionResult indicating success or failure
     */
    suspend fun initialize(): ExtensionResult<Unit> {
        return ExtensionResult.Success(Unit)
    }
    
    /**
     * Clean up resources when the extension is unloaded.
     * Extensions should cancel any ongoing operations and clean up resources.
     */
    suspend fun cleanup() {
        // Default implementation does nothing
    }
    
    /**
     * Get extension-specific configuration options (optional).
     * This can be used to provide settings that users can configure.
     * 
     * @return Map of configuration keys to their current values
     */
    fun getConfiguration(): Map<String, Any> {
        return emptyMap()
    }
    
    /**
     * Update extension configuration (optional).
     * 
     * @param config Map of configuration keys to new values
     * @return ExtensionResult indicating success or failure
     */
    suspend fun updateConfiguration(config: Map<String, Any>): ExtensionResult<Unit> {
        return ExtensionResult.Success(Unit)
    }
} 