package com.shawnfrost.async.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Player : Screen("player")
    
    // Detail screens
    object TrackDetail : Screen("track_detail/{trackId}") {
        fun createRoute(trackId: String) = "track_detail/$trackId"
    }
    
    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
    
    object CreatePlaylist : Screen("create_playlist")
} 