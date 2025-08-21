package com.async.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a search result returned by music extensions.
 * This is the primary data model for music tracks discovered through extensions.
 */
@Serializable
data class SearchResult(
    /**
     * Unique identifier for this track within the extension's context.
     * This ID will be used to retrieve stream URLs and other track details.
     */
    val id: String,
    
    /**
     * Display title of the track
     */
    val title: String,
    
    /**
     * Artist name (optional)
     */
    val artist: String? = null,
    
    /**
     * Album name (optional)
     */
    val album: String? = null,
    
    /**
     * Track duration in milliseconds (optional)
     */
    val duration: Long? = null,
    
    /**
     * URL to track thumbnail/artwork (optional)
     */
    val thumbnailUrl: String? = null,
    
    /**
     * Extension ID that provided this result.
     * This will be automatically set by the extension manager.
     */
    val extensionId: String = "",
    
    /**
     * Additional metadata that extensions can provide
     */
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Returns a formatted display string for the track
     */
    fun getDisplayString(): String {
        return when {
            artist != null && album != null -> "$title - $artist ($album)"
            artist != null -> "$title - $artist"
            else -> title
        }
    }
    
    /**
     * Returns formatted duration string (e.g., "3:45")
     */
    fun getFormattedDuration(): String? {
        return duration?.let { durationMs ->
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = (durationMs / (1000 * 60 * 60))
            
            when {
                hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
                else -> "%d:%02d".format(minutes, seconds)
            }
        }
    }
} 