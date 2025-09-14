package app.async.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import app.async.app.ui.theme.iconSizing
import app.async.app.ui.theme.IconSizing

/**
 * Mihon-style App Bar as per UI guide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    subtitle: String? = null,
    navigateUp: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    actionModeCounter: Int = 0,
    onCancelActionMode: () -> Unit = {},
    actionModeActions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val isActionMode by remember(actionModeCounter) {
        derivedStateOf { actionModeCounter > 0 }
    }

    AppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        titleContent = {
            AppBarTitle(
                title = title,
                subtitle = subtitle,
            )
        },
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        actions = actions,
        isActionMode = isActionMode,
        actionModeCounter = actionModeCounter,
        onCancelActionMode = onCancelActionMode,
        actionModeActions = actionModeActions,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    titleContent: @Composable () -> Unit,
    navigateUp: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    isActionMode: Boolean = false,
    actionModeCounter: Int = 0,
    onCancelActionMode: () -> Unit = {},
    actionModeActions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val containerColor = backgroundColor ?: TopAppBarDefaults.topAppBarColors().containerColor

    TopAppBar(
        title = {
            if (isActionMode) {
                AppBarTitle(
                    title = if (actionModeCounter == 1) {
                        "1 selected" // TODO: Use string resource
                    } else {
                        "$actionModeCounter selected" // TODO: Use string resource
                    }
                )
            } else {
                titleContent()
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (isActionMode) {
                IconButton(onClick = onCancelActionMode) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel selection", // TODO: Use string resource
                        modifier = Modifier.size(IconSizing.default) // 24dp as per Mihon guide
                    )
                }
            } else if (navigateUp != null) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = navigationIcon ?: Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate up", // TODO: Use string resource
                        modifier = Modifier.size(IconSizing.default) // 24dp as per Mihon guide
                    )
                }
            }
        },
        actions = {
            if (isActionMode) {
                actionModeActions()
            } else {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun AppBarTitle(
    title: String?,
    subtitle: String? = null,
) {
    Column {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge, // 22sp as per Mihon guide
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium, // 14sp as per Mihon guide
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Mihon-style Search Toolbar as per UI guide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchToolbar(
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit = {},
    navigateUp: (() -> Unit)? = null,
    searchEnabled: Boolean = true,
    placeholderText: String? = null,
    onSearch: (String) -> Unit = {},
    onClickCloseSearch: () -> Unit = { onChangeSearchQuery(null) },
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val focusRequester = remember { FocusRequester() }

    AppBar(
        modifier = modifier,
        titleContent = {
            if (searchQuery == null) return@AppBar titleContent()

            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            BasicTextField(
                value = searchQuery,
                onValueChange = onChangeSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.titleMedium.copy( // 16sp Medium as per Mihon guide
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true,
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty() && placeholderText != null) {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.titleMedium.copy( // 16sp Medium as per Mihon guide
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                    innerTextField()
                }
            )
        },
        navigateUp = navigateUp,
        actions = {
            if (searchQuery != null) {
                IconButton(onClick = onClickCloseSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search", // TODO: Use string resource
                        modifier = Modifier.size(IconSizing.default) // 24dp as per Mihon guide
                    )
                }
            }
            actions()
        },
        scrollBehavior = scrollBehavior,
    )
} 