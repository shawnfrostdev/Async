package com.example.async.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {}
) {
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header
        HeadlineLarge(
            text = "Home",
            modifier = Modifier.padding(bottom = AppSpacing.m)
        )
        
        // Extension integration placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.m)
            ) {
                TitleMedium(
                    text = "Music Discovery Coming Soon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BodyMedium(
                    text = "Extension integration will provide music recommendations, recent tracks, and popular content here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(AppSpacing.s))
                Button(
                    onClick = onNavigateToSearch,
                    contentPadding = AppSpacing.smallButtonPadding
                ) {
                    LabelMedium("Go to Search")
                }
            }
        }
    }
} 