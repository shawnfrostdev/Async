package com.example.extension

import com.async.core.extension.MusicExtension
import com.async.core.model.Album
import com.async.core.model.Artist
import com.async.core.model.ExtensionResult
import com.async.core.model.SearchResult
import com.async.core.model.ExtensionException

/**
 * Template for creating a new music extension.
 * 
 * Replace "Template" with your extension name and implement the required methods.
 * This template provides a basic structure and common patterns for extension development.
 * 
 * TODO: Update all the properties below with your extension's information
 */
class TemplateMusicExtension : MusicExtension {
    
    // TODO: Replace with your unique extension ID (use reverse domain notation)
    override val id = "com.example.templateextension"
    
    // TODO: Set your extension version (increment for updates)
    override val version = 1
    
    // TODO: Set your extension's display name
    override val name = "Template Music Extension"
    
    // TODO: Set your name or organization
    override val developer = "Your Name"
    
    // TODO: Describe what your extension does
    override val description = "Template extension for accessing music from [Your Music Service]"
    
    // TODO: Optional - Add your extension's icon URL
    override val iconUrl: String? = null
    
    // TODO: Optional - Add your website or source code URL
    override val websiteUrl: String? = null
    
    // TODO: Set API compatibility (usually keep these defaults)
    override val minApiLevel = 1
    override val maxApiLevel = Int.MAX_VALUE
    
    // TODO: Set to false if your extension doesn't need network access
    override val requiresNetwork = true
    
    // TODO: Add any HTTP client or other resources your extension needs
    // private val httpClient = HttpClient()
    // private val baseUrl = "https://api.yourmusicservice.com"
    
    /**
     * Search for music tracks.
     * This is the main method users will interact with.
     * 
     * TODO: Implement your search logic here
     */
    override suspend fun search(
        query: String,
        limit: Int,
        offset: Int
    ): ExtensionResult<List<SearchResult>> {
        return try {
            // TODO: Implement your search logic
            // Example structure:
            // 1. Make HTTP request to your music service's search API
            // 2. Parse the response
            // 3. Convert to SearchResult objects
            // 4. Return ExtensionResult.Success(results)
            
            // For now, return an error indicating this needs implementation
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Search not implemented yet. Please implement the search method."
                )
            )
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Search failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Get the streaming URL for a specific track.
     * Called when the user wants to play a track.
     * 
     * TODO: Implement your stream URL retrieval logic here
     */
    override suspend fun getStreamUrl(mediaId: String): ExtensionResult<String> {
        return try {
            // TODO: Implement your stream URL logic
            // Example structure:
            // 1. Make HTTP request to get track details
            // 2. Extract the direct streaming URL
            // 3. Return ExtensionResult.Success(streamUrl)
            
            // For now, return an error indicating this needs implementation
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Stream URL retrieval not implemented yet. Please implement the getStreamUrl method."
                )
            )
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Failed to get stream URL: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * Download album artwork or track thumbnail.
     * 
     * TODO: Implement your image download logic here
     */
    override suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray> {
        return try {
            // TODO: Implement your image download logic
            // Example structure:
            // 1. Make HTTP request to download the image
            // 2. Return the image data as ByteArray
            // 3. Return ExtensionResult.Success(imageData)
            
            // For now, return an error indicating this needs implementation
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Album art download not implemented yet. Please implement the getAlbumArt method."
                )
            )
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.NetworkError(
                    "Failed to download album art: ${e.message}"
                )
            )
        }
    }
    
    /**
     * Optional: Get detailed artist information.
     * Only implement this if your music service provides artist data.
     */
    override suspend fun getArtist(artistId: String): ExtensionResult<Artist> {
        // TODO: Implement if your service supports artist lookup
        return ExtensionResult.Error(
            ExtensionException.GenericError("Artist lookup not supported by this extension")
        )
    }
    
    /**
     * Optional: Get detailed album information.
     * Only implement this if your music service provides album data.
     */
    override suspend fun getAlbum(albumId: String): ExtensionResult<Album> {
        // TODO: Implement if your service supports album lookup
        return ExtensionResult.Error(
            ExtensionException.GenericError("Album lookup not supported by this extension")
        )
    }
    
    /**
     * Optional: Get all tracks from a specific album.
     * Only implement this if your music service provides album track listings.
     */
    override suspend fun getAlbumTracks(albumId: String): ExtensionResult<List<SearchResult>> {
        // TODO: Implement if your service supports album track listings
        return ExtensionResult.Error(
            ExtensionException.GenericError("Album tracks not supported by this extension")
        )
    }
    
    /**
     * Optional: Get tracks by a specific artist.
     * Only implement this if your music service provides artist track listings.
     */
    override suspend fun getArtistTracks(
        artistId: String,
        limit: Int,
        offset: Int
    ): ExtensionResult<List<SearchResult>> {
        // TODO: Implement if your service supports artist track listings
        return ExtensionResult.Error(
            ExtensionException.GenericError("Artist tracks not supported by this extension")
        )
    }
    
    /**
     * Optional: Initialize the extension.
     * Called when the extension is first loaded.
     * Use this to set up any resources your extension needs.
     */
    override suspend fun initialize(): ExtensionResult<Unit> {
        return try {
            // TODO: Add any initialization logic here
            // Example: authenticate with your service, load configuration, etc.
            
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.ConfigurationError(
                    "Failed to initialize extension: ${e.message}"
                )
            )
        }
    }
    
    /**
     * Optional: Clean up resources when the extension is unloaded.
     * Use this to close HTTP clients, cancel coroutines, etc.
     */
    override suspend fun cleanup() {
        // TODO: Add cleanup logic here
        // Example: httpClient.close()
    }
    
    /**
     * Optional: Get extension configuration options.
     * Return a map of configuration keys to their current values.
     */
    override fun getConfiguration(): Map<String, Any> {
        // TODO: Return your extension's configuration options
        // Example:
        return mapOf(
            "api_key" to "",
            "quality" to "high",
            "language" to "en",
            "timeout" to 30
        )
    }
    
    /**
     * Optional: Update extension configuration.
     * Handle configuration changes from the user.
     */
    override suspend fun updateConfiguration(config: Map<String, Any>): ExtensionResult<Unit> {
        return try {
            // TODO: Handle configuration updates
            // Example: validate and save new configuration values
            
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.ConfigurationError(
                    "Failed to update configuration: ${e.message}"
                )
            )
        }
    }
    
    // TODO: Add your private helper methods here
    
    /**
     * Example helper method for parsing search results.
     * Replace this with your actual parsing logic.
     */
    private fun parseSearchResults(responseData: String): List<SearchResult> {
        // TODO: Parse your API response and convert to SearchResult objects
        // Example structure:
        /*
        val jsonResponse = Json.decodeFromString<YourApiResponse>(responseData)
        return jsonResponse.tracks.map { track ->
            SearchResult(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                duration = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                metadata = mapOf(
                    "genre" to track.genre,
                    "year" to track.year.toString()
                )
            )
        }
        */
        return emptyList()
    }
    
    /**
     * Example helper method for making HTTP requests.
     * Replace this with your actual HTTP client logic.
     */
    private suspend fun makeHttpRequest(url: String): String {
        // TODO: Implement your HTTP request logic
        // Example with Ktor:
        /*
        val response = httpClient.get(url)
        return response.bodyAsText()
        */
        throw NotImplementedError("HTTP request logic not implemented")
    }
}

/*
 * TODO: Implementation Checklist
 * 
 * [ ] Update all the TODO comments above
 * [ ] Implement the search() method
 * [ ] Implement the getStreamUrl() method
 * [ ] Implement the getAlbumArt() method
 * [ ] Add proper error handling
 * [ ] Add HTTP client or networking library
 * [ ] Test your extension thoroughly
 * [ ] Add configuration options if needed
 * [ ] Implement optional methods if supported by your service
 * [ ] Add proper documentation
 * [ ] Create unit tests
 * [ ] Build and package your extension
 * 
 * Remember:
 * - Always handle errors gracefully
 * - Respect rate limits of the music service
 * - Follow the service's terms of use
 * - Test with various search queries
 * - Validate all input parameters
 * - Use meaningful error messages
 */ 