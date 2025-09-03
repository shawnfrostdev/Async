package app.async.app.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import app.async.R
import app.async.app.ui.components.AppText
import app.async.app.ui.theme.AsyncColors

/**
 * Standardized layout components following Material Design 3 and Android development best practices.
 * These components ensure consistent spacing, responsive behavior, and accessibility across all screens.
 */

/**
 * Standard screen container with consistent padding and responsive behavior.
 * Automatically adjusts margins based on screen size.
 */
@Composable
fun StandardScreenLayout(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true,
    hasBottomPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val horizontalPadding = dimensionResource(R.dimen.screen_margin_horizontal)
    val verticalPadding = if (hasTopBar) 0.dp else dimensionResource(R.dimen.screen_margin_vertical)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding,
                bottom = if (hasBottomPadding) dimensionResource(R.dimen.screen_margin_vertical) else 0.dp
            ),
        content = content
    )
}

/**
 * Scrollable screen layout for content that might overflow.
 */
@Composable
fun ScrollableScreenLayout(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val horizontalPadding = dimensionResource(R.dimen.screen_margin_horizontal)
    val verticalPadding = if (hasTopBar) 0.dp else dimensionResource(R.dimen.screen_margin_vertical)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding,
                bottom = dimensionResource(R.dimen.screen_margin_vertical)
            ),
        content = content
    )
}

/**
 * Standard lazy column layout with consistent spacing and responsive behavior.
 */
@Composable
fun StandardLazyColumn(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
    content: LazyListScope.() -> Unit
) {
    val horizontalPadding = dimensionResource(R.dimen.screen_margin_horizontal)
    val topPadding = if (hasTopBar) dimensionResource(R.dimen.spacing_normal) else dimensionResource(R.dimen.screen_margin_vertical)
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = topPadding,
            bottom = dimensionResource(R.dimen.screen_margin_vertical)
        ),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * Standard screen header with consistent styling and responsive text sizing.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                .padding(vertical = dimensionResource(R.dimen.spacing_xl))
        ) {
            AppText.TitleLarge(
                text = title,
                fontWeight = FontWeight.Bold,
                color = AsyncColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
            AppText.BodyMedium(
                text = subtitle,
                color = AsyncColors.TextSecondary
            )
            
            // Actions row if provided
            actions.let { actionsComposable ->
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    content = actionsComposable
                )
            }
        }
    } else {
        // Simple header with optional actions
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.spacing_xl)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText.TitleLarge(
                text = title,
                fontWeight = FontWeight.Bold,
                color = AsyncColors.TextPrimary
            )
            
            Row(content = actions)
        }
    }
}

/**
 * Standard section header for grouping content within screens.
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
            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AppText.TitleMedium(
                text = title,
                fontWeight = FontWeight.Bold,
                color = AsyncColors.TextPrimary
            )
            subtitle?.let {
                AppText.BodySmall(
                    text = it,
                    color = AsyncColors.TextSecondary
                )
            }
        }
        
        actionText?.let { text ->
            TextButton(
                onClick = onActionClick ?: {},
                enabled = onActionClick != null
            ) {
                Text(
                    text = text,
                    color = AsyncColors.Primary
                )
            }
        }
    }
}

/**
 * Standard list item layout with consistent sizing and accessibility.
 */
@Composable
fun StandardListItem(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val itemHeight = dimensionResource(R.dimen.list_item_height)
    val itemPadding = dimensionResource(R.dimen.list_item_padding)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = itemHeight),
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = AsyncColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(itemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon or content
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            } ?: leadingContent?.let {
                it()
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            }
            
            // Main content
            Column(
                modifier = Modifier.weight(1f),
                content = content
            )
            
            // Trailing content or icon
            trailingContent?.let {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                it()
            } ?: trailingIcon?.let {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                it()
            }
        }
    }
}

/**
 * Standard empty state layout with consistent styling.
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
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_normal))
        ) {
            icon()
            
            AppText.TitleMedium(
                text = title,
                color = AsyncColors.TextPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            
            AppText.BodyMedium(
                text = subtitle,
                color = AsyncColors.TextSecondary
            )
            
            actionText?.let { text ->
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
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

/**
 * Standard error state layout with retry functionality.
 */
@Composable
fun StandardErrorState(
    title: String = stringResource(R.string.error_title),
    message: String,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_normal)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            AppText.TitleMedium(
                text = title,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            AppText.BodyMedium(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            onRetryClick?.let { retry ->
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

/**
 * Standard loading state layout.
 */
@Composable
fun StandardLoadingState(
    message: String = stringResource(R.string.loading_general),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_normal))
        ) {
            CircularProgressIndicator(
                color = AsyncColors.Primary,
                strokeWidth = 2.dp
            )
            
            AppText.BodyMedium(
                text = message,
                color = AsyncColors.TextSecondary
            )
        }
    }
}

/**
 * Responsive grid layout that adapts to screen size.
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    minItemWidth: Int = 160, // dp
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val itemSpacing = dimensionResource(R.dimen.spacing_normal).value.toInt()
    val horizontalPadding = dimensionResource(R.dimen.screen_margin_horizontal).value.toInt() * 2
    
    val availableWidth = screenWidth - horizontalPadding
    val columns = (availableWidth / (minItemWidth + itemSpacing)).coerceAtLeast(1)
    
    // For now, use a simple column since we don't have a grid layout
    // In a real implementation, you would use LazyVerticalGrid
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_normal))
    ) {
        content()
    }
} 
