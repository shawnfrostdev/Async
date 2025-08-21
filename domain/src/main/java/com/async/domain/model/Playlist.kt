package com.async.domain.model

/**
 * Domain model representing a playlist
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverArtUrl: String? = null,
    val coverArtPath: String? = null,
    val trackCount: Int = 0,
    val totalDuration: Long = 0, // in milliseconds
    val dateCreated: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isSystemPlaylist: Boolean = false,
    val sortOrder: Int = 0
) {
    
    /**
     * Get display name for UI
     */
    fun getDisplayName(): String = name.takeIf { it.isNotBlank() } ?: "Untitled Playlist"
    
    /**
     * Get display description for UI
     */
    fun getDisplayDescription(): String = description?.takeIf { it.isNotBlank() } ?: "No description"
    
    /**
     * Get formatted total duration
     */
    fun getFormattedDuration(): String {
        return if (totalDuration > 0) {
            Track.formatDuration(totalDuration)
        } else {
            "--:--"
        }
    }
    
    /**
     * Get track count display text
     */
    fun getTrackCountText(): String {
        return when (trackCount) {
            0 -> "No tracks"
            1 -> "1 track"
            else -> "$trackCount tracks"
        }
    }
    
    /**
     * Check if playlist is empty
     */
    fun isEmpty(): Boolean = trackCount == 0
    
    /**
     * Check if playlist can be modified
     */
    fun isModifiable(): Boolean = !isSystemPlaylist
    
    /**
     * Get cover art URL (local or remote)
     */
    fun getCoverArt(): String? {
        return coverArtPath ?: coverArtUrl
    }
    
    /**
     * Check if playlist has custom cover art
     */
    fun hasCustomCoverArt(): Boolean {
        return !coverArtPath.isNullOrBlank() || !coverArtUrl.isNullOrBlank()
    }
    
    companion object {
        
        // System playlist names
        const val RECENTLY_PLAYED = "Recently Played"
        const val FAVORITES = "Favorites"
        const val MOST_PLAYED = "Most Played"
        
        /**
         * Create a system playlist
         */
        fun createSystemPlaylist(
            name: String,
            description: String,
            sortOrder: Int = 0
        ) = Playlist(
            name = name,
            description = description,
            isSystemPlaylist = true,
            sortOrder = sortOrder
        )
        
        /**
         * Create "Recently Played" system playlist
         */
        fun createRecentlyPlayedPlaylist() = createSystemPlaylist(
            name = RECENTLY_PLAYED,
            description = "Your recently played tracks",
            sortOrder = 0
        )
        
        /**
         * Create "Favorites" system playlist
         */
        fun createFavoritesPlaylist() = createSystemPlaylist(
            name = FAVORITES,
            description = "Your favorite tracks",
            sortOrder = 1
        )
        
        /**
         * Create "Most Played" system playlist
         */
        fun createMostPlayedPlaylist() = createSystemPlaylist(
            name = MOST_PLAYED,
            description = "Your most played tracks",
            sortOrder = 2
        )
        
        /**
         * Create empty user playlist
         */
        fun createUserPlaylist(name: String, description: String? = null) = Playlist(
            name = name,
            description = description,
            isSystemPlaylist = false
        )
        
        /**
         * Validate playlist name
         */
        fun isValidName(name: String): Boolean {
            return name.isNotBlank() && name.length <= 50 && !name.contains("/")
        }
        
        /**
         * Get validation error for name
         */
        fun getNameValidationError(name: String): String? {
            return when {
                name.isBlank() -> "Name cannot be empty"
                name.length > 50 -> "Name too long (max 50 characters)"
                name.contains("/") -> "Name cannot contain '/' character"
                else -> null
            }
        }
    }
} 