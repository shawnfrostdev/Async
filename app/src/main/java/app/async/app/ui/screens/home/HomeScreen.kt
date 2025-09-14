package app.async.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import app.async.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import app.async.app.ui.components.AppCards
import app.async.app.ui.components.AppText
import app.async.app.ui.components.layout.*
import app.async.app.ui.vm.HomeViewModel
import app.async.core.model.SearchResult

/**
 * Home screen displaying trending tracks, recently played, and recommendations
 * Following Material Design 3 guidelines with consistent layout and accessibility
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
        // Top app bar with consistent styling
        TopAppBar(
            title = {
                AppText.TitleLarge(stringResource(R.string.title_home))
            },
            actions = {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.size(dimensionResource(R.dimen.min_touch_target))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                        )
                    }
                }
            }
        )

        // Error state using standardized component
        uiState.error?.let { error ->
            AppCards.ErrorCard(
                title = "Error",
                message = error,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(16.dp)
            )
        }

        // Content using Material 3 LazyColumn
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Trending tracks section
            if (uiState.trendingTracks.isNotEmpty()) {
                item {
                    HomeSection(
                        title = stringResource(R.string.home_trending_title),
                        subtitle = stringResource(
                            R.string.home_trending_subtitle,
                            uiState.trendingTracks.size
                        ),
                        onSeeAllClick = onNavigateToSearch
                    ) {
                        HorizontalTrackList(
                            tracks = uiState.trendingTracks.take(10),
                            onTrackClick = onTrackClick,
                            onPlayTrack = onPlayTrack
                        )
                    }
                }
            }
            
            // Recently played section
            if (uiState.recentlyPlayed.isNotEmpty()) {
                item {
                    HomeSection(
                        title = stringResource(R.string.home_recently_played_title),
                        subtitle = stringResource(R.string.home_recently_played_subtitle),
                        onSeeAllClick = onNavigateToLibrary
                    ) {
                        HorizontalTrackList(
                            tracks = uiState.recentlyPlayed.take(10),
                            onTrackClick = onTrackClick,
                            onPlayTrack = onPlayTrack
                        )
                    }
                }
            }
            
            // Recommendations section
            if (uiState.recommendations.isNotEmpty()) {
                item {
                    HomeSection(
                        title = stringResource(R.string.home_recommended_title),
                        subtitle = stringResource(R.string.home_recommended_subtitle),
                        onSeeAllClick = onNavigateToSearch
                    ) {
                        HorizontalTrackList(
                            tracks = uiState.recommendations.take(10),
                            onTrackClick = onTrackClick,
                            onPlayTrack = onPlayTrack
                        )
                    }
                }
            }
            
            // Empty state
            if (uiState.trendingTracks.isEmpty() && uiState.recentlyPlayed.isEmpty() && 
                uiState.recommendations.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(R.string.home_welcome_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                text = stringResource(R.string.home_welcome_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text(stringResource(R.string.home_get_started))
                            }
                        }
                    }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            androidx.compose.material3.TextButton(onClick = onSeeAllClick) {
                Text(stringResource(R.string.home_see_all))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        content()
    }
}

@Composable
private fun HorizontalTrackList(
    tracks: List<SearchResult>,
    onTrackClick: (SearchResult) -> Unit,
    onPlayTrack: (SearchResult) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_normal)),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.spacing_tiny)
        )
    ) {
        items(tracks) { track ->
            TrackCard(
                track = track,
                onClick = { onTrackClick(track) },
                onPlayClick = { onPlayTrack(track) }
            )
        }
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
