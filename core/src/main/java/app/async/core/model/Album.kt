package app.async.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a music album returned by extensions.
 */
@Serializable
data class Album(
    /**
     * Unique identifier for this album within the extension's context
     */
    val id: String,
    
    /**
     * Album title
     */
    val title: String,
    
    /**
     * Primary artist name
     */
    val artist: String,
    
    /**
     * Release year (optional)
     */
    val year: Int? = null,
    
    /**
     * URL to album artwork (optional)
     */
    val artworkUrl: String? = null,
    
    /**
     * Album description or notes (optional)
     */
    val description: String? = null,
    
    /**
     * Total number of tracks in the album (optional)
     */
    val trackCount: Int? = null,
    
    /**
     * Total album duration in milliseconds (optional)
     */
    val duration: Long? = null,
    
    /**
     * Extension ID that provided this album information
     */
    val extensionId: String = "",
    
    /**
     * Additional metadata about the album
     */
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Returns a formatted display string for the album
     */
    fun getDisplayString(): String {
        return when (year) {
            null -> "$title - $artist"
            else -> "$title - $artist ($year)"
        }
    }
} 
