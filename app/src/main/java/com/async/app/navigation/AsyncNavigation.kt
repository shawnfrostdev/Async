package com.async.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material3.MaterialTheme
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.async.app.ui.screens.home.HomeScreen
import com.async.app.ui.screens.search.SearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.app.ui.vm.PlayerViewModel
import com.async.app.ui.vm.LibraryViewModel
import logcat.logcat
import com.async.app.ui.screens.library.LibraryScreen
import com.async.app.ui.screens.library.PlaylistDetailScreen
import com.async.app.ui.screens.player.PlayerScreen
import com.async.app.ui.screens.settings.SettingsScreen
import com.async.app.ui.screens.settings.AboutScreen
import com.async.app.ui.screens.extensions.ExtensionManagementScreen
import com.async.domain.model.Playlist
import com.async.domain.model.Track
import com.async.core.model.SearchResult
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay

/**
 * Main navigation setup using Voyager
 */
@Composable
fun AsyncNavigation() {
    Navigator(HomeTab) { navigator ->
        SlideTransition(navigator)
    }
}

/**
 * Navigation tabs for bottom navigation
 */
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home"
        )

    @Composable
    override fun Content() {
        HomeScreen()
    }
}

object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Search"
        )

    @Composable
    override fun Content() {
        val playerViewModel: PlayerViewModel = viewModel()
        val libraryViewModel: LibraryViewModel = viewModel()
        SearchScreen(
            onPlayTrack = { track -> 
                logcat("Navigation") { "onPlayTrack called for: ${track.title}" }
                playerViewModel.playTrack(track) 
            },
            libraryViewModel = libraryViewModel
        )
    }
}

object LibraryTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Library"
        )

    @Composable
    override fun Content() {
        Navigator(LibraryMainScreen) { navigator ->
            navigator.lastItem.Content()
        }
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = "Settings"
        )

    @Composable
    override fun Content() {
        Navigator(SettingsMainScreen) { navigator ->
            navigator.lastItem.Content()
        }
    }
}

object SettingsMainScreen : Screen {
    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

/**
 * Individual screens that can be navigated to
 */
object PlayerScreenNav : Screen {
    @Composable
    override fun Content() {
        com.async.app.ui.screens.player.PlayerScreenContent()
    }
}

object ExtensionManagementScreenNav : Screen {
    @Composable
    override fun Content() {
        ExtensionManagementScreen().Content()
    }
}

object AboutScreenNav : Screen {
    @Composable
    override fun Content() {
        AboutScreen()
    }
}

// Library navigation screens
object LibraryMainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        
        LibraryScreen(
            onPlaylistClick = { playlist ->
                when (playlist.name) {
                    "Liked Songs" -> {
                        navigator?.push(PlaylistDetailLikedScreen)
                    }
                    "Downloads" -> {
                        navigator?.push(PlaylistDetailDownloadsScreen)
                    }
                    else -> {
                        // Custom playlist - navigate to custom playlist detail
                        navigator?.push(PlaylistDetailCustomScreen(playlist.id))
                    }
                }
            }
        )
    }
}

object PlaylistDetailLikedScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val libraryViewModel: LibraryViewModel = viewModel()
        val playerViewModel: PlayerViewModel = viewModel()
        
        PlaylistDetailScreen(
            playlistName = "Liked Songs",
            playlistDescription = "Your favorite tracks",
            playlistIcon = Icons.Outlined.Favorite,
            playlistIconTint = MaterialTheme.colorScheme.error,
            tracks = libraryViewModel.uiState.likedTracks,
            onNavigateBack = { navigator?.pop() },
            onTrackClick = { searchResult ->
                // Play the clicked track and set up the playlist as queue
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.likedTracks)
                val clickedIndex = allTracks.indexOfFirst { it.id == searchResult.id }
                if (clickedIndex >= 0) {
                    playerViewModel.setQueue(allTracks, clickedIndex)
                }
                logcat("PlaylistDetail") { "Playing track: ${searchResult.title} from Liked Songs" }
            },
            onTrackMenuClick = { track ->
                // Handle track menu click
                logcat("PlaylistDetail") { "Track menu clicked: ${track.title}" }
            },
            onShuffleClick = {
                // Smart shuffle: toggle shuffle mode if already playing this playlist, otherwise start shuffled
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.likedTracks)
                val currentTrack = playerViewModel.uiState.currentTrack
                val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                
                if (isPlayingFromThisPlaylist) {
                    // Just toggle shuffle mode without changing current track
                    playerViewModel.toggleShuffle()
                } else {
                    // Start playing shuffled from beginning
                    val shuffledTracks = allTracks.shuffled()
                    playerViewModel.setQueue(shuffledTracks, 0)
                }
                logcat("PlaylistDetail") { "Shuffle action for Liked Songs" }
            },
            onPlayClick = {
                // Smart play: resume if paused, or start from beginning if not playing this playlist
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.likedTracks)
                val currentTrack = playerViewModel.uiState.currentTrack
                val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                
                if (isPlayingFromThisPlaylist && !playerViewModel.uiState.isPlaying) {
                    // Resume current track
                    playerViewModel.playPause()
                } else if (!isPlayingFromThisPlaylist || currentTrack == null) {
                    // Start playing from beginning
                    playerViewModel.setQueue(allTracks, 0)
                }
                logcat("PlaylistDetail") { "Play action for Liked Songs" }
            },
            onEditPlaylist = {
                // System playlists can't be edited
                logcat("PlaylistDetail") { "Edit not available for system playlists" }
            }
        )
    }
}

object PlaylistDetailDownloadsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val libraryViewModel: LibraryViewModel = viewModel()
        val playerViewModel: PlayerViewModel = viewModel()
        
        PlaylistDetailScreen(
            playlistName = "Downloads",
            playlistDescription = "Your downloaded music",
            playlistIcon = Icons.Outlined.Download,
            playlistIconTint = MaterialTheme.colorScheme.primary,
            tracks = libraryViewModel.uiState.downloadedTracks,
            onNavigateBack = { navigator?.pop() },
            onTrackClick = { searchResult ->
                // Play the clicked track and set up the playlist as queue
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.downloadedTracks)
                val clickedIndex = allTracks.indexOfFirst { it.id == searchResult.id }
                if (clickedIndex >= 0) {
                    playerViewModel.setQueue(allTracks, clickedIndex)
                }
                logcat("PlaylistDetail") { "Playing track: ${searchResult.title} from Downloads" }
            },
            onTrackMenuClick = { track ->
                // Handle track menu click
                logcat("PlaylistDetail") { "Track menu clicked: ${track.title}" }
            },
            onShuffleClick = {
                // Smart shuffle: toggle shuffle mode if already playing this playlist, otherwise start shuffled
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.downloadedTracks)
                val currentTrack = playerViewModel.uiState.currentTrack
                val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                
                if (isPlayingFromThisPlaylist) {
                    // Just toggle shuffle mode without changing current track
                    playerViewModel.toggleShuffle()
                } else {
                    // Start playing shuffled from beginning
                    val shuffledTracks = allTracks.shuffled()
                    playerViewModel.setQueue(shuffledTracks, 0)
                }
                logcat("PlaylistDetail") { "Shuffle action for Downloads" }
            },
            onPlayClick = {
                // Smart play: resume if paused, or start from beginning if not playing this playlist
                val allTracks = libraryViewModel.tracksToSearchResults(libraryViewModel.uiState.downloadedTracks)
                val currentTrack = playerViewModel.uiState.currentTrack
                val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                
                if (isPlayingFromThisPlaylist && !playerViewModel.uiState.isPlaying) {
                    // Resume current track
                    playerViewModel.playPause()
                } else if (!isPlayingFromThisPlaylist || currentTrack == null) {
                    // Start playing from beginning
                    playerViewModel.setQueue(allTracks, 0)
                }
                logcat("PlaylistDetail") { "Play action for Downloads" }
            },
            onEditPlaylist = {
                // System playlists can't be edited
                logcat("PlaylistDetail") { "Edit not available for system playlists" }
            }
        )
    }
}

data class PlaylistDetailCustomScreen(val playlistId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val libraryViewModel: LibraryViewModel = viewModel()
        val playerViewModel: PlayerViewModel = viewModel()
        
        // Find the playlist by ID
        val playlist = libraryViewModel.uiState.customPlaylists.find { it.id == playlistId }
        
        // Preload tracks when this screen is opened
        LaunchedEffect(playlistId) {
            libraryViewModel.preloadPlaylistTracks(playlistId)
        }
        
        // Use cached tracks first, then fall back to flow if cache is empty
        val cachedTracks = libraryViewModel.getCachedPlaylistTracks(playlistId)
        val flowTracks by libraryViewModel.getPlaylistTracks(playlistId).collectAsState(initial = emptyList())
        
        val playlistTracks = if (cachedTracks.isNotEmpty()) cachedTracks else flowTracks
        val isLoading = playlist != null && playlistTracks.isEmpty() && cachedTracks.isEmpty()
        
        if (playlist != null) {
            PlaylistDetailScreen(
                playlistName = playlist.name,
                playlistDescription = playlist.description,
                playlistIcon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                playlistIconTint = MaterialTheme.colorScheme.primary,
                tracks = playlistTracks, // Now using actual tracks from database
                isLoading = isLoading,
                onNavigateBack = { navigator?.pop() },
                onTrackClick = { searchResult ->
                    // Play the clicked track and set up the playlist as queue
                    val allTracks = libraryViewModel.tracksToSearchResults(playlistTracks)
                    val clickedIndex = allTracks.indexOfFirst { it.id == searchResult.id }
                    if (clickedIndex >= 0) {
                        playerViewModel.setQueue(allTracks, clickedIndex)
                    }
                    logcat("PlaylistDetail") { "Playing track: ${searchResult.title} from ${playlist.name}" }
                },
                onTrackMenuClick = { track ->
                    // Handle track menu click
                    logcat("PlaylistDetail") { "Track menu clicked: ${track.title}" }
                },
                onShuffleClick = {
                    // Smart shuffle: toggle shuffle mode if already playing this playlist, otherwise start shuffled
                    val allTracks = libraryViewModel.tracksToSearchResults(playlistTracks)
                    val currentTrack = playerViewModel.uiState.currentTrack
                    val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                    
                    if (isPlayingFromThisPlaylist) {
                        // Just toggle shuffle mode without changing current track
                        playerViewModel.toggleShuffle()
                    } else {
                        // Start playing shuffled from beginning
                        val shuffledTracks = allTracks.shuffled()
                        playerViewModel.setQueue(shuffledTracks, 0)
                    }
                    logcat("PlaylistDetail") { "Shuffle action for ${playlist.name}" }
                },
                onPlayClick = {
                    // Smart play: resume if paused, or start from beginning if not playing this playlist
                    val allTracks = libraryViewModel.tracksToSearchResults(playlistTracks)
                    val currentTrack = playerViewModel.uiState.currentTrack
                    val isPlayingFromThisPlaylist = currentTrack != null && allTracks.any { it.id == currentTrack.id }
                    
                    if (isPlayingFromThisPlaylist && !playerViewModel.uiState.isPlaying) {
                        // Resume current track
                        playerViewModel.playPause()
                    } else if (!isPlayingFromThisPlaylist || currentTrack == null) {
                        // Start playing from beginning
                        playerViewModel.setQueue(allTracks, 0)
                    }
                    logcat("PlaylistDetail") { "Play action for ${playlist.name}" }
                },
                onEditPlaylist = {
                    libraryViewModel.showEditPlaylistDialog(playlist)
                }
            )
        } else {
            // Playlist not found, show error or navigate back
            LaunchedEffect(Unit) {
                logcat("PlaylistDetail") { "Playlist with ID $playlistId not found" }
                navigator?.pop()
            }
        }
    }
}

/**
 * Navigation destinations
 */
sealed class NavigationDestination(val route: String) {
    object Home : NavigationDestination("home")
    object Search : NavigationDestination("search")
    object Library : NavigationDestination("library")
    object Playlists : NavigationDestination("playlists")
    object Player : NavigationDestination("player")
    object Settings : NavigationDestination("settings")
    object Extensions : NavigationDestination("extensions")
} 
