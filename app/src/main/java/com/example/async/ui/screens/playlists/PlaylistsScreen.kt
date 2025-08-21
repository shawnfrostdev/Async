package com.example.async.ui.screens.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.async.R

@Composable
fun PlaylistsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.title_playlists),
            style = MaterialTheme.typography.headlineMedium
        )
    }
} 