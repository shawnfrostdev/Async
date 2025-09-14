package app.async.app.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import app.async.app.ui.theme.padding

/**
 * Mihon-style custom Scaffold as per UI guide
 * Handles WindowInsets, StartBar (Navigation rail for tablets), Animated FAB, Dynamic Heights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    startBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .then(modifier),
        color = containerColor,
        contentColor = contentColor,
    ) {
        ScaffoldLayout(
            fabPosition = floatingActionButtonPosition,
            topBar = { topBar(topBarScrollBehavior) },
            startBar = startBar,
            bottomBar = bottomBar,
            content = content,
            snackbar = snackbarHost,
            contentWindowInsets = contentWindowInsets,
            fab = floatingActionButton,
        )
    }
}

/**
 * Custom Scaffold Layout with startBar support for Navigation Rail
 */
@Composable
private fun ScaffoldLayout(
    fabPosition: FabPosition,
    topBar: @Composable () -> Unit,
    startBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    snackbar: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    fab: @Composable () -> Unit,
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                topBar()
                
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    startBar()
                    
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        content(
                            PaddingValues(
                                bottom = if (fabPosition == FabPosition.End) 0.dp else 56.dp
                            )
                        )
                    }
                }
                
                bottomBar()
            }
            
            // FAB positioning
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = when (fabPosition) {
                    FabPosition.Start -> Alignment.BottomStart
                    FabPosition.Center -> Alignment.BottomCenter
                    FabPosition.End -> Alignment.BottomEnd
                    else -> Alignment.BottomEnd
                }
            ) {
                fab()
            }
            
            // Snackbar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                snackbar()
            }
        }
    }
}

/**
 * Standard screen container with consistent padding and responsive behavior.
 * Based on Mihon screen-level padding patterns
 */
@Composable
fun StandardScreenLayout(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true,
    hasBottomPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val horizontalPadding = MaterialTheme.padding.large // 24.dp as per Mihon guide
    val verticalPadding = if (hasTopBar) 0.dp else MaterialTheme.padding.large
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding,
                bottom = if (hasBottomPadding) MaterialTheme.padding.large else 0.dp
            ),
        content = content
    )
}

/**
 * Standard lazy column layout with consistent spacing based on Mihon guide
 */
@Composable
fun StandardLazyColumn(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(MaterialTheme.padding.large),
    content: LazyListScope.() -> Unit
) {
    val horizontalPadding = MaterialTheme.padding.large // 24.dp as per Mihon guide
    val topPadding = if (hasTopBar) MaterialTheme.padding.medium else MaterialTheme.padding.large
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = topPadding,
            bottom = MaterialTheme.padding.large
        ),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * Mihon-style Adaptive Grid System
 */
object CommonItemDefaults {
    val GridHorizontalSpacer = 4.dp      // Space between grid columns as per Mihon guide
    val GridVerticalSpacer = 4.dp        // Space between grid rows as per Mihon guide
}

@Composable
fun LazyLibraryGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    contentPadding: PaddingValues,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = if (columns == 0) GridCells.Adaptive(128.dp) else GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(LayoutDirection.Ltr) + 8.dp,
            end = contentPadding.calculateEndPadding(LayoutDirection.Ltr) + 8.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ), // 8.dp around entire grid as per Mihon guide
        verticalArrangement = Arrangement.spacedBy(CommonItemDefaults.GridVerticalSpacer),
        horizontalArrangement = Arrangement.spacedBy(CommonItemDefaults.GridHorizontalSpacer),
        content = content,
    )
}

/**
 * Standard section header for grouping content within screens based on Mihon guide
 */
@Composable
fun StandardSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.padding.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
        
        actionText?.let { text ->
            androidx.compose.material3.TextButton(
                onClick = onActionClick ?: {},
                enabled = onActionClick != null
            ) {
                Text(text = text)
            }
        }
    }
}

/**
 * Standard screen header with consistent styling and responsive text sizing based on Mihon guide
 */
@Composable
fun StandardScreenHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (subtitle != null) {
        // Header with subtitle
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.large)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(MaterialTheme.padding.extraSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Actions row if provided
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                content = actions
            )
        }
    } else {
        // Simple header with optional actions
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(content = actions)
        }
    }
}

/**
 * Standard loading state layout based on Mihon guide
 */
@Composable
fun StandardLoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium)
        ) {
            CircularProgressIndicator()
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Standard error state layout with retry functionality based on Mihon guide
 */
@Composable
fun StandardErrorState(
    title: String = "Error",
    message: String,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.padding.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.padding.medium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(MaterialTheme.padding.small))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            onRetryClick?.let { retry ->
                Spacer(modifier = Modifier.height(MaterialTheme.padding.medium))
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Standard empty state layout based on Mihon guide
 */
@Composable
fun StandardEmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium)
        ) {
            icon()
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            actionText?.let { text ->
                Spacer(modifier = Modifier.height(MaterialTheme.padding.small))
                Button(
                    onClick = onActionClick ?: {},
                    enabled = onActionClick != null
                ) {
                    Text(text)
                }
            }
        }
    }
} 
