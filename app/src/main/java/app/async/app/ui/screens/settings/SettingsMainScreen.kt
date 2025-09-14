package app.async.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import app.async.app.navigation.ExtensionManagementScreenNav
import app.async.app.ui.theme.padding
import app.async.app.ui.theme.iconSizing
import app.async.app.ui.theme.IconSizing
import app.async.app.navigation.AsyncScaffold
import androidx.compose.ui.text.style.TextOverflow
import app.async.app.ui.components.AppBar

/**
 * Settings Main Screen - Mihon Implementation as per UI guide
 */
object SettingsMainScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        AsyncScaffold(
            topBar = {
                AppBar(
                    title = "Settings", // Using proper string - should be string resource
                    navigateUp = { navigator.pop() }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.padding.medium, // 16dp as per Mihon guide
                    vertical = MaterialTheme.padding.medium // 16dp as per Mihon guide
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.large) // 24dp between sections
            ) {
                // Extensions Section - Only setting as requested
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        SettingsItem(
                            title = "Extensions",
                            description = "Manage installed extensions and discover new sources",
                            icon = Icons.Outlined.Extension,
                            onClick = { 
                                navigator.push(app.async.app.ui.screens.extensions.ExtensionManagementScreen)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Settings Item Component following Mihon design patterns
@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(MaterialTheme.padding.medium), // 16dp internal item padding like More screen
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(IconSizing.default), // 24dp as per Mihon guide
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(MaterialTheme.padding.medium)) // 16dp spacing
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge, // 16sp like More screen items
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium, // 14sp as per Mihon guide
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Icon(
            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(IconSizing.default), // 24dp as per Mihon guide
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 