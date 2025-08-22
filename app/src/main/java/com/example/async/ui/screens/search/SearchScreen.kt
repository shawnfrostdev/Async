package com.example.async.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.async.ui.components.text.HeadlineLarge
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.LabelMedium
import com.example.async.ui.components.text.TitleMedium
import com.example.async.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.m)
    ) {
        // Header
        HeadlineLarge(
            text = "Search",
            modifier = Modifier.padding(bottom = AppSpacing.m)
        )
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { BodyMedium("Search for music...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // TODO: Implement search with extensions
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.m))
        
        // Extension status placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.m)
            ) {
                BodyMedium(
                    text = "Search Integration Coming Soon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabelMedium(
                    text = "Extension search functionality will be connected here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        
        // Search placeholder
        if (searchQuery.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                ) {
                    TitleMedium(
                        text = "Search: '$searchQuery'",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BodyMedium(
                        text = "Extension integration in progress",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                ) {
                    TitleMedium(
                        text = "Search for Music",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BodyMedium(
                        text = "Enter a search query to find music",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
} 