package com.example.async.ui.screens.extensions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionManagementScreen(
    onNavigateBack: () -> Unit = {},
    repositories: List<String> = emptyList() // Future: will come from ViewModel
) {
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar with proper positioning
        TopAppBar(
            title = { Text("Extensions & Sources") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showAddRepositoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Repository"
                    )
                }
            }
        )
        if (repositories.isEmpty()) {
            // Show empty state for first-time users
            EmptyRepositoryState(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                onAddRepository = { showAddRepositoryDialog = true }
            )
        } else {
            // Show repository list (future implementation)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Future: Repository list items will go here
            }
        }
    }
    
    // Add Repository Dialog
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepositoryDialog = false },
            onConfirm = { url ->
                // Future: Add repository logic
                showAddRepositoryDialog = false
            }
        )
    }
}

@Composable
private fun EmptyRepositoryState(
    modifier: Modifier = Modifier,
    onAddRepository: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Extensions Yet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Add extension repositories to discover music sources and enhance your listening experience.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var isUrlValid by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repository") },
        text = {
            Column {
                Text(
                    text = "Enter the URL of the extension repository:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        isUrlValid = isValidUrl(it)
                    },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://github.com/user/repo") },
                    isError = !isUrlValid && url.isNotEmpty(),
                    supportingText = if (!isUrlValid && url.isNotEmpty()) {
                        { Text("Please enter a valid URL") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url) },
                enabled = url.isNotEmpty() && isUrlValid
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun isValidUrl(url: String): Boolean {
    if (url.isEmpty()) return true
    return try {
        val pattern = Regex("^https?://.*")
        pattern.matches(url)
    } catch (e: Exception) {
        false
    }
} 