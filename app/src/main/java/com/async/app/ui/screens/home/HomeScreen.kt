package com.async.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.app.ui.components.AppCards
import com.async.app.ui.components.AppText
import androidx.compose.ui.unit.dp
import com.async.app.ui.vm.HomeViewModel
import com.async.core.model.SearchResult

/**
 * Home screen displaying trending tracks, recently played, and recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onPlayTrack: (SearchResult) -> Unit = {}
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }

    LaunchedEffect(Unit) {
        // Trigger initial load if needed
        if (uiState.trendingTracks.isEmpty() && !uiState.isLoading) {
            viewModel.refresh()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = {
                AppText.TitleLarge("Home")
            },
            actions = {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
        )

        // Error state
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    AppText.TitleMedium(
                        text = "Error",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    AppText.BodyMedium(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.refresh() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        // Content
        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Trending tracks section
            if (uiState.trendingTracks.isNotEmpty()) {
                item {
                    HomeSection(
                        title = "Trending Now",
                        subtitle = "${uiState.trendingTracks.size} hot tracks",
                        onSeeAllClick = onNavigateToSearch
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(uiState.trendingTracks.take(10)) { track ->
                                TrackCard(
                                    track = track,
                                    onClick = { onTrackClick(track) },
                                    onPlayClick = { onPlayTrack(track) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Recently played section
            if (uiState.recentlyPlayed.isNotEmpty()) {
                item {
                    HomeSection(
                        title = "Recently Played",
                        subtitle = "Pick up where you left off",
                        onSeeAllClick = onNavigateToLibrary
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(uiState.recentlyPlayed.take(10)) { track ->
                                TrackCard(
                                    track = track,
                                    onClick = { onTrackClick(track) },
                                    onPlayClick = { onPlayTrack(track) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Recommendations section
            if (uiState.recommendations.isNotEmpty()) {
                item {
                    HomeSection(
                        title = "Recommended",
                        subtitle = "Discover new music",
                        onSeeAllClick = onNavigateToSearch
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(uiState.recommendations.take(10)) { track ->
                                TrackCard(
                                    track = track,
                                    onClick = { onTrackClick(track) },
                                    onPlayClick = { onPlayTrack(track) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Empty state
            if (uiState.trendingTracks.isEmpty() && uiState.recentlyPlayed.isEmpty() && 
                uiState.recommendations.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyState(
                        onRefresh = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AppText.TitleLarge(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                AppText.BodyMedium(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
            }
        }
        
                    Spacer(modifier = Modifier.height(16.dp))
        
        content()
    }
}

@Composable
private fun TrackCard(
    track: SearchResult,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    AppCards.TrackCard(
        track = track,
        onClick = onClick,
        onPlayClick = onPlayClick
    )
}

@Composable
private fun EmptyState(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
                            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppText.TitleLarge(
            text = "Welcome to Async",
            fontWeight = FontWeight.Bold
        )
                        Spacer(modifier = Modifier.height(8.dp))
        AppText.BodyMedium(
            text = "Discover and play music from various sources",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
                    Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRefresh) {
            Text("Get Started")
        }
    }
} 
