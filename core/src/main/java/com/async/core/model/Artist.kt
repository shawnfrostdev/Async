package com.async.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a music artist returned by extensions.
 */
@Serializable
data class Artist(
    /**
     * Unique identifier for this artist within the extension's context
     */
    val id: String,
    
    /**
     * Artist's display name
     */
    val name: String,
    
    /**
     * URL to artist's profile image or avatar (optional)
     */
    val imageUrl: String? = null,
    
    /**
     * Artist biography or description (optional)
     */
    val biography: String? = null,
    
    /**
     * Extension ID that provided this artist information
     */
    val extensionId: String = "",
    
    /**
     * Additional metadata about the artist
     */
    val metadata: Map<String, String> = emptyMap()
) 