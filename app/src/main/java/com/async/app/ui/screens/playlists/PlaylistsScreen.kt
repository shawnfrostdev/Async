package com.async.app.ui.screens.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.app.ui.components.AppText
import com.async.app.ui.vm.LibraryViewModel

/**
 * Playlists screen displaying user playlists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = {
                AppText.TitleLarge(text = "Playlists")
            },
            actions = {
                IconButton(
                    onClick = { 
                        viewModel.createPlaylist("New Playlist", "")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist"
                    )
                }
            }
        )

        // Content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.playlists.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText.TitleMedium(
                        text = "No Playlists Yet",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppText.BodyMedium(
                        text = "Create your first playlist to get started"
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.playlists) { playlist ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.onPlaylistClick(playlist) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                AppText.TitleMedium(text = playlist.name)
                                AppText.BodySmall(
                                    text = "${playlist.trackCount} tracks"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 