package com.async.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.async.core.model.SearchResult
import com.async.app.ui.vm.SearchViewModel
import com.async.app.ui.components.AppText
import com.async.app.ui.components.AppCards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onTrackClick: (SearchResult) -> Unit = {},
    onPlayTrack: (SearchResult) -> Unit = {},
    searchViewModel: SearchViewModel = viewModel()
) {
    val uiState = searchViewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        AppText.TitleLarge(
            text = "Search",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Search Input
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { searchViewModel.updateQuery(it) },
            label = { Text("Search for music...") },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.clearSearch() }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    searchViewModel.search(uiState.query)
                    keyboardController?.hide()
                }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Results
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    AppText.BodyMedium(
                        text = "Searching for music...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (uiState.error != null) {
            // Error state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    AppText.TitleMedium(
                        text = "Search Error",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    AppText.BodyMedium(
                        text = uiState.error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { searchViewModel.search(uiState.query) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Retry Search")
                    }
                }
            }
        } else if (uiState.results.isNotEmpty()) {
            // Search results
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText.TitleMedium(text = "Search Results")
                        AppText.LabelMedium(
                            text = "${uiState.results.size} tracks found",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                items(uiState.results) { track ->
                    SearchResultItem(
                        track = track,
                        onTrackClick = onTrackClick,
                        onPlayClick = onPlayTrack
                    )
                }
            }
        } else if (uiState.query.isNotEmpty()) {
            // No results found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppText.TitleMedium(
                        text = "No results found",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    AppText.BodyMedium(
                        text = "Try searching with different keywords",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // Empty search state
            EmptySearchContent(
                onSampleSearch = { query ->
                    searchViewModel.updateQuery(query)
                    searchViewModel.search(query)
                }
            )
        }
    }
}



@Composable
private fun SearchResultItem(
    track: SearchResult,
    onTrackClick: (SearchResult) -> Unit,
    onPlayClick: (SearchResult) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onTrackClick(track) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork placeholder
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppText.TitleMedium(text = track.title ?: "Unknown Title")
                AppText.BodyMedium(
                    text = track.artist ?: "Unknown Artist",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (track.duration != null) {
                    AppText.LabelMedium(
                        text = formatDuration(track.duration!!),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Actions
            Row {
                IconButton(onClick = { onPlayClick(track) }) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "Play"
                    )
                }
                IconButton(onClick = { /* TODO: Add to playlist */ }) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add to playlist"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchContent(
    onSampleSearch: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText.TitleMedium(
                text = "Search for Music",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            AppText.BodyMedium(
                text = "Find songs, artists, and albums from various sources",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sample searches
            AppText.TitleMedium(text = "Try searching for:")
            Spacer(modifier = Modifier.height(8.dp))
            
            val sampleQueries = listOf("Daft Punk", "Bohemian Rhapsody", "Chill Music", "Jazz")
            sampleQueries.forEach { query ->
                AssistChip(
                    onClick = { onSampleSearch(query) },
                    label = { Text(query) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (query != sampleQueries.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
} 
