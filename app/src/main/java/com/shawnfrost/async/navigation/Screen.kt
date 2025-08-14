package com.shawnfrost.async.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Player : Screen("player")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object ArtistDetail : Screen("artist/{artistId}") {
        fun createRoute(artistId: String) = "artist/$artistId"
    }
    object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: String) = "album/$albumId"
    }

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                "home" -> Home
                "search" -> Search
                "library" -> Library
                "settings" -> Settings
                "player" -> Player
                "playlist" -> PlaylistDetail
                "artist" -> ArtistDetail
                "album" -> AlbumDetail
                null -> Home
                else -> throw IllegalArgumentException("Route $route is not recognized")
            }
        }
    }
} 