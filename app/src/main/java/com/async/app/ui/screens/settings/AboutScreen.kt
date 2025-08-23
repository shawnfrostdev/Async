package com.async.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.async.app.ui.components.AppText

/**
 * About screen showing app information
 */
class AboutScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top app bar
            TopAppBar(
                title = {
                    AppText.TitleLarge(text = "About")
                }
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // App Icon and Name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppText.TitleLarge(
                            text = "Async Music Player",
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        AppText.TitleMedium(
                            text = "Version 1.0.0-alpha",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppText.BodyMedium(
                            text = "A modern music streaming app with extension support",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item {
                    // Features Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AppText.TitleMedium(
                                text = "Features",
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val features = listOf(
                                "🎵 Stream music from multiple sources",
                                "🔌 Extension system for adding new sources", 
                                "📚 Smart music library management",
                                "🎨 Modern Material Design 3 UI",
                                "🌙 Dark and light theme support",
                                "📱 Built with Jetpack Compose"
                            )
                            
                            features.forEach { feature ->
                                AppText.BodyMedium(
                                    text = feature,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                item {
                    // Technology Stack Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AppText.TitleMedium(
                                text = "Technology Stack",
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val technologies = listOf(
                                "Kotlin" to "Programming language",
                                "Jetpack Compose" to "Modern Android UI toolkit",
                                "Material Design 3" to "UI design system",
                                "Voyager" to "Type-safe navigation",
                                "Room Database" to "Local data storage",
                                "ExoPlayer (Media3)" to "Advanced media playback",
                                "Coroutines & Flow" to "Asynchronous programming",
                                "Manual DI" to "Dependency injection"
                            )
                            
                            technologies.forEach { (tech, description) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AppText.BodyMedium(
                                        text = tech,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AppText.BodySmall(
                                        text = description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Developer Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AppText.TitleMedium(
                                text = "Developer",
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            AppText.BodyMedium(
                                text = "Developed with ❤️ for music lovers"
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            AppText.BodyMedium(
                                text = "Open source project built for educational purposes"
                            )
                        }
                    }
                }
                
                item {
                    // Footer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 32.dp)
                    ) {
                        AppText.BodySmall(
                            text = "© 2024 Async Music Player",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        AppText.BodySmall(
                            text = "Made with Jetpack Compose",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
} 