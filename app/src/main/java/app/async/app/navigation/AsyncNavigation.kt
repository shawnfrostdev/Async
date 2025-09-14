package app.async.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import soup.compose.material.motion.MaterialFadeThrough
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import app.async.app.ui.screens.home.HomeScreen as HomeScreenContent
import app.async.app.ui.screens.search.SearchScreen
import app.async.app.ui.screens.library.LibraryScreen
import app.async.app.ui.screens.settings.MoreScreen
import app.async.app.ui.screens.settings.AboutScreen
import app.async.app.ui.screens.extensions.ExtensionManagementScreen
import app.async.app.ui.vm.PlayerViewModel
import app.async.app.ui.vm.LibraryViewModel
import app.async.app.ui.utils.isTabletUi
import app.async.app.ui.components.PermissionManager
import app.async.app.ui.components.MiniPlayer
import app.async.app.ui.theme.AsyncColors
import app.async.app.di.AppModule
import logcat.logcat

/**
 * Tab fade animation duration as per Mihon guide
 */
private const val TabFadeDuration = 200

/**
 * Custom Tab interface that extends Voyager's tab system with reselection behavior
 */
interface AsyncTab : Tab {
    suspend fun onReselect(navigator: Navigator) {}
}

/**
 * HomeScreen - Main container that manages the bottom navigation system
 * Following Mihon navigation pattern exactly
 */
object HomeScreen : Screen {
    
    // Event channels for inter-tab communication as per Mihon guide
    private val openTabEvent = Channel<AsyncTab>()
    private val showBottomNavEvent = Channel<Boolean>()
    
    // Tab configuration - following Mihon pattern
    private val TABS = listOf(
        HomeTab,        // Index 0
        SearchTab,      // Index 1  
        LibraryTab,     // Index 2
        SettingsTab,    // Index 3
    )
    
    suspend fun openTab(tab: AsyncTab) {
        openTabEvent.send(tab)
    }
    
    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }
    
    @UnstableApi
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var permissionsGranted by remember { mutableStateOf(false) }
        
        // Note: Extension sync moved to be lazy to prevent startup crashes

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AsyncColors.Background
        ) {
            // Temporarily skip permissions for debugging
            LaunchedEffect(Unit) {
                permissionsGranted = true
            }
            
            if (permissionsGranted) {
                // Main tab navigation following Mihon pattern exactly
                TabNavigator(
                    tab = HomeTab,  // Default tab as per Mihon
                    key = "HomeTabs",
                ) { tabNavigator ->
                    CompositionLocalProvider(LocalNavigator provides navigator) {
                        val playerViewModel: PlayerViewModel = viewModel()
                        val playerUiState = playerViewModel.uiState
                        var isPlayerExpanded by remember { mutableStateOf(false) }
                        
                        // Tab-level back navigation - back goes to Home tab first, then pops screen
                        BackHandler(
                            enabled = true,
                            onBack = {
                                when {
                                    // If not on Home tab, go to Home tab first
                                    tabNavigator.current != HomeTab -> {
                                        tabNavigator.current = HomeTab
                                    }
                                    // If on Home tab and can pop screen, pop the screen
                                    navigator.canPop -> {
                                        navigator.pop()
                                    }
                                    // Otherwise, let the system handle it (exit app)
                                    else -> {
                                        // Do nothing - let system handle app exit
                                    }
                                }
                            }
                        )
                        
                        AsyncScaffold(
                            startBar = {
                                // Navigation Rail for tablets as per Mihon guide
                                if (isTabletUi()) {
                                    NavigationRail {
                                        TABS.forEach { tab ->
                                            NavigationRailItem(tab)
                                        }
                                    }
                                }
                            },
                            bottomBar = {
                                // Bottom Navigation for phones as per Mihon guide
                                if (!isTabletUi()) {
                                    val bottomNavVisible by produceState(initialValue = true) {
                                        showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                                    }
                                    AnimatedVisibility(
                                        visible = bottomNavVisible,
                                        enter = expandVertically(),
                                        exit = shrinkVertically(),
                                    ) {
                                        Column {
                                            // MiniPlayer above navigation
                                            if (playerUiState.currentTrack != null) {
                                                MiniPlayer(
                                                    currentTrack = playerUiState.currentTrack,
                                                    isPlaying = playerUiState.isPlaying,
                                                    onPlayPause = { playerViewModel.playPause() },
                                                    onExpandPlayer = { isPlayerExpanded = true }
                                                )
                                            }
                                            NavigationBar {
                                                TABS.forEach { tab ->
                                                    AsyncNavigationBarItem(tab)
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            contentWindowInsets = WindowInsets(0),
                        ) { contentPadding ->
                            // Tab content with Material Fade Through as per animation.md guide
                            MaterialFadeThrough(
                                targetState = tabNavigator.current
                            ) { tab ->
                                tab.Content()
                            }
                        }
                        
                        // Player Screen overlay
                        if (isPlayerExpanded) {
                            // Player screen implementation would go here
                        }
                    }
                }
            }
        }
    }
}

// Tab implementations following Mihon pattern
object HomeTab : AsyncTab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home"
        )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        
        HomeScreenContent()
    }
}

object SearchTab : AsyncTab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Search"
        )

    @UnstableApi
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        
        // Following Mihon pattern for screen models
        val playerViewModel: PlayerViewModel = viewModel()
        val libraryViewModel: LibraryViewModel = viewModel()
        
        SearchScreen(
            onPlayTrack = { track -> 
                logcat("Navigation") { "onPlayTrack called for: ${track.title}" }
                playerViewModel.playTrack(track) 
            },
            libraryViewModel = libraryViewModel
        )
    }
}

object LibraryTab : AsyncTab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Library"
        )

    override suspend fun onReselect(navigator: Navigator) {
        // Reselect behavior: could open library settings or scroll to top
        // For now, no specific action
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val libraryViewModel: LibraryViewModel = viewModel()
        
        // Call LibraryScreen directly as per Mihon pattern
        LibraryScreen(
            libraryViewModel = libraryViewModel
        )
    }
}

object SettingsTab : AsyncTab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = "More"
        )

    override suspend fun onReselect(navigator: Navigator) {
        // Reselect behavior: could open main settings
        // For now, no specific action
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        
        // Call MoreScreen directly as per Mihon pattern
        MoreScreen()
    }
}

// Custom NavigationBar and items following Mihon guide exactly
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: androidx.compose.ui.unit.Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    androidx.compose.material3.Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(80.dp)  // Fixed height as per Mihon
                .selectableGroup(),
            content = content,
        )
    }
}

@Composable
private fun RowScope.AsyncNavigationBarItem(tab: AsyncTab) {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val selected = tabNavigator.current::class == tab::class
    
    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                tabNavigator.current = tab
            } else {
                scope.launch { tab.onReselect(navigator) }
            }
        },
        icon = { 
            Icon(
                imageVector = when (tab) {
                    is HomeTab -> Icons.Outlined.Home
                    is SearchTab -> Icons.Outlined.Search
                    is LibraryTab -> Icons.Outlined.LibraryMusic
                    is SettingsTab -> Icons.Outlined.MoreHoriz
                    else -> Icons.Outlined.Home
                },
                contentDescription = tab.options.title
            )
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        alwaysShowLabel = true,
    )
}

@Composable
fun NavigationRailItem(tab: AsyncTab) {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val selected = tabNavigator.current::class == tab::class
    
    NavigationRailItem(
        selected = selected,
        onClick = {
            if (!selected) {
                tabNavigator.current = tab
            } else {
                scope.launch { tab.onReselect(navigator) }
            }
        },
        icon = { 
            Icon(
                imageVector = when (tab) {
                    is HomeTab -> Icons.Outlined.Home
                    is SearchTab -> Icons.Outlined.Search
                    is LibraryTab -> Icons.Outlined.LibraryMusic
                    is SettingsTab -> Icons.Outlined.MoreHoriz
                    else -> Icons.Outlined.Home
                },
                contentDescription = tab.options.title
            )
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        alwaysShowLabel = true,
    )
}

// Custom Scaffold with startBar as per Mihon guide
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsyncScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    startBar: @Composable () -> Unit = {},  // Custom addition as per Mihon
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            startBar()
            content(PaddingValues())
        }
    }
}

// Additional screens needed by Settings
object ExtensionManagementScreenNav : Screen {
    @Composable
    override fun Content() {
        ExtensionManagementScreen.Content()
    }
}

object AboutScreenNav : Screen {
    @Composable
    override fun Content() {
        AboutScreen()
    }
}
