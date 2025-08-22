# Android UI Design System Implementation Prompt

Create a comprehensive, production-ready UI design system for my Android app based on the Mihon manga reader architecture. Implement a complete Material Design 3 compliant design system with all components, typography, colors, and layouts.

## 1. Core Design System Architecture

**Create these files:**

### DesignTokens.kt
```kotlin
// Central design tokens: colors, typography, spacing, elevation, shapes
// Material Design 3 compliant color schemes (light/dark)
// Dynamic color support with fallbacks
// Semantic color naming (primary, secondary, surface, error, etc.)
```

### AppTypography.kt
```kotlin
// Complete typography scale with all Material 3 text styles
// Custom font family support with fallbacks
// Text size variants: display, headline, title, body, label
// Line height, letter spacing, font weights
// Responsive typography for different screen sizes
```

### AppTheme.kt
```kotlin
// Main theme composable with Material 3 theming
// Light/dark theme switching
// Dynamic color integration
// Custom theme variants (AMOLED, high contrast)
// Theme state management and persistence
```

### Spacing.kt
```kotlin
// Consistent spacing scale (4dp, 8dp, 16dp, 24dp, 32dp, etc.)
// Semantic spacing names (xs, s, m, l, xl, xxl)
// Component-specific spacing (list item padding, card margins)
// Responsive spacing for tablets
```

## 2. Typography System

### Text Components

**AppText.kt** - Semantic text components:
```kotlin
@Composable
fun DisplayLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE
)

@Composable
fun DisplayMedium(...)

@Composable
fun DisplaySmall(...)

@Composable
fun HeadlineLarge(...)

@Composable
fun HeadlineMedium(...)

@Composable
fun HeadlineSmall(...)

@Composable
fun TitleLarge(...)

@Composable
fun TitleMedium(...)

@Composable
fun TitleSmall(...)

@Composable
fun BodyLarge(...)

@Composable
fun BodyMedium(...)

@Composable
fun BodySmall(...)

@Composable
fun LabelLarge(...)

@Composable
fun LabelMedium(...)

@Composable
fun LabelSmall(...)
```

**Usage Context Documentation:**
```kotlin
// Display Large: Hero text, main titles (57sp)
// Display Medium: Section headers (45sp)  
// Display Small: Subsection headers (36sp)
// Headline Large: Card titles, dialog titles (32sp)
// Headline Medium: List headers (28sp)
// Headline Small: Component titles (24sp)
// Title Large: Screen titles, app bar titles (22sp)
// Title Medium: Card subtitles (16sp)
// Title Small: Dense lists, tabs (14sp)
// Body Large: Main content, article text (16sp)
// Body Medium: Default body text (14sp)
// Body Small: Captions, metadata (12sp)
// Label Large: Buttons, prominent labels (14sp)
// Label Medium: Form labels, chips (12sp)
// Label Small: Dense UI, timestamps (11sp)
```

### Text Utilities

**TextExtensions.kt** - Text styling utilities:
```kotlin
// Modifier extensions for common text styling
fun Modifier.textEmphasis(emphasis: TextEmphasis): Modifier
fun Modifier.textColor(color: Color): Modifier
fun Modifier.textBackground(color: Color): Modifier

enum class TextEmphasis {
    HIGH,      // 87% opacity
    MEDIUM,    // 60% opacity  
    DISABLED   // 38% opacity
}

// Text measurement utilities
@Composable
fun rememberTextMeasurer(): TextMeasurer

// Custom text selection
@Composable
fun SelectableText(...)
```

## 3. Button System

### Primary Buttons

**AppButtons.kt** - Complete button system:
```kotlin
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun SecondaryButton(...)

@Composable
fun TertiaryButton(...)

@Composable
fun OutlinedButton(...)

@Composable
fun TextButton(...)

@Composable
fun IconButton(...)

@Composable
fun FloatingActionButton(...)

@Composable
fun ExtendedFloatingActionButton(...)
```

### Button Variants

**ButtonVariants.kt** - Specialized button types:
```kotlin
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
    loadingText: String? = null,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun SplitButton(
    onMainClick: () -> Unit,
    onDropdownClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    mainContent: @Composable () -> Unit,
    dropdownContent: @Composable () -> Unit
)

@Composable
fun ChipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
)
```

### Button Usage Guide
```kotlin
// Primary Button: Main actions (Save, Submit, Continue)
// Secondary Button: Secondary actions (Cancel, Back)
// Tertiary Button: Low priority actions
// Outlined Button: Alternative actions
// Text Button: Minimal actions (Skip, Learn More)
// Icon Button: Single action icons
// FAB: Primary floating action
// Extended FAB: FAB with text label
```

## 4. Card System

### Base Cards

**AppCards.kt** - Comprehensive card components:
```kotlin
@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    elevation: CardElevation = CardDefaults.cardElevation(),
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
)

@Composable
fun ContentCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
)

@Composable
fun ImageCard(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    aspectRatio: Float = 3f / 4f,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
)
```

### Specialized Cards

**SpecializedCards.kt** - Domain-specific cards:
```kotlin
@Composable
fun MangaCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    onClick: (Manga) -> Unit,
    onLongClick: ((Manga) -> Unit)? = null,
    showBadges: Boolean = true,
    showProgress: Boolean = true
)

@Composable
fun ChapterCard(
    chapter: Chapter,
    modifier: Modifier = Modifier,
    onClick: (Chapter) -> Unit,
    onDownload: ((Chapter) -> Unit)? = null,
    showProgress: Boolean = true
)

@Composable
fun SourceCard(
    source: Source,
    modifier: Modifier = Modifier,
    onClick: (Source) -> Unit,
    showStatus: Boolean = true
)

@Composable
fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
)
```

## 5. Input Components

### Text Fields

**AppTextFields.kt** - Text input components:
```kotlin
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1
)

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = {},
    active: Boolean = false,
    enabled: Boolean = true
)

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    isError: Boolean = false,
    supportingText: String? = null
)
```

### Selection Components

**SelectionComponents.kt** - Checkboxes, radio buttons, switches:
```kotlin
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors()
)

@Composable
fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null
)

@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors()
)

@Composable
fun RadioButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
)

@Composable
fun LabeledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null
)
```

## 6. List Components

### Basic Lists

**AppLists.kt** - List components:
```kotlin
@Composable
fun AppLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
)

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    onClick: (() -> Unit)? = null
)

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
)
```

### Specialized Lists

**SpecializedLists.kt** - Domain-specific list items:
```kotlin
@Composable
fun MangaListItem(
    manga: Manga,
    modifier: Modifier = Modifier,
    onClick: (Manga) -> Unit,
    onLongClick: ((Manga) -> Unit)? = null,
    showCover: Boolean = true,
    showBadges: Boolean = true,
    showProgress: Boolean = true
)

@Composable
fun ChapterListItem(
    chapter: Chapter,
    modifier: Modifier = Modifier,
    onClick: (Chapter) -> Unit,
    onDownload: ((Chapter) -> Unit)? = null,
    onBookmark: ((Chapter) -> Unit)? = null,
    showProgress: Boolean = true
)

@Composable
fun SourceListItem(
    source: Source,
    modifier: Modifier = Modifier,
    onClick: (Source) -> Unit,
    showLanguage: Boolean = true,
    showStatus: Boolean = true
)

@Composable
fun SettingListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
)
```

## 7. Navigation Components

### App Bars

**AppBars.kt** - Top and bottom app bars:
```kotlin
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
)

@Composable
fun SearchAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onBack: () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null
)

@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 3.dp,
    content: @Composable RowScope.() -> Unit
)
```

### Navigation Components

**NavigationComponents.kt** - Navigation drawers, rails, bars:
```kotlin
@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 3.dp,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun AppNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: (@Composable () -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors()
)

@Composable
fun AppNavigationRail(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)

@Composable
fun AppNavigationDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
)
```

### Tab Components

**TabComponents.kt** - Tab layouts:
```kotlin
@Composable
fun AppTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable () -> Unit = @Composable {
        HorizontalDivider()
    },
    tabs: @Composable () -> Unit
)

@Composable
fun AppTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface
)

@Composable
fun ScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    edgePadding: Dp = 52.dp,
    tabs: @Composable () -> Unit
)
```

## 8. Dialog and Sheet Components

### Dialogs

**AppDialogs.kt** - Dialog components:
```kotlin
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
)

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: (@Composable () -> Unit)? = null
)

@Composable
fun InputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    confirmText: String = "OK",
    dismissText: String = "Cancel"
)
```

### Bottom Sheets

**AppSheets.kt** - Bottom sheet components:
```kotlin
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
)

@Composable
fun MenuBottomSheet(
    items: List<BottomSheetItem>,
    onItemClick: (BottomSheetItem) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
)

data class BottomSheetItem(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true,
    val destructive: Boolean = false
)
```

## 9. Feedback Components

### Snackbars

**FeedbackComponents.kt** - User feedback components:
```kotlin
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { AppSnackbar(it) }
)

@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor
)

// Helper functions for common snackbar types
suspend fun SnackbarHostState.showSuccess(message: String, actionLabel: String? = null)
suspend fun SnackbarHostState.showError(message: String, actionLabel: String? = null)
suspend fun SnackbarHostState.showWarning(message: String, actionLabel: String? = null)
suspend fun SnackbarHostState.showInfo(message: String, actionLabel: String? = null)
```

### Progress Indicators

**ProgressComponents.kt** - Progress and loading indicators:
```kotlin
@Composable
fun AppCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
)

@Composable
fun AppLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor
)

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
    content: @Composable () -> Unit
)
```

## 10. Layout Components

### Containers

**LayoutComponents.kt** - Layout containers:
```kotlin
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit
)

@Composable
fun ContentContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
)

@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
)

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
)
```

### Dividers and Spacers

**DividersAndSpacers.kt** - Layout dividers and spacing:
```kotlin
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline
)

@Composable
fun VerticalSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HorizontalSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

// Semantic spacing
@Composable
fun XSmallSpacer() = VerticalSpacer(4.dp)
@Composable
fun SmallSpacer() = VerticalSpacer(8.dp)
@Composable
fun MediumSpacer() = VerticalSpacer(16.dp)
@Composable
fun LargeSpacer() = VerticalSpacer(24.dp)
@Composable
fun XLargeSpacer() = VerticalSpacer(32.dp)
```

## 11. Utility Components

### Image Components

**ImageComponents.kt** - Image handling components:
```kotlin
@Composable
fun AppAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
)

@Composable
fun CoverImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 3f / 4f,
    contentScale: ContentScale = ContentScale.Crop
)

@Composable
fun AvatarImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    fallback: (@Composable () -> Unit)? = null
)
```

### Badge Components

**BadgeComponents.kt** - Badge and chip components:
```kotlin
@Composable
fun AppBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    content: (@Composable () -> Unit)? = null
)

@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99
)

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)

@Composable
fun AppChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = ChipDefaults.shape,
    colors: ChipColors = ChipDefaults.chipColors(),
    elevation: ChipElevation? = ChipDefaults.chipElevation(),
    border: ChipBorder? = null
)
```

## 12. Theme Implementation

### Color System

**AppColors.kt** - Complete color system:
```kotlin
// Light theme colors
private val LightPrimary = Color(0xFF6750A4)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFEADDFF)
private val LightOnPrimaryContainer = Color(0xFF21005D)

private val LightSecondary = Color(0xFF625B71)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFE8DEF8)
private val LightOnSecondaryContainer = Color(0xFF1D192B)

// Dark theme colors
private val DarkPrimary = Color(0xFFD0BCFF)
private val DarkOnPrimary = Color(0xFF381E72)
private val DarkPrimaryContainer = Color(0xFF4F378B)
private val DarkOnPrimaryContainer = Color(0xFFEADDFF)

private val DarkSecondary = Color(0xFFCCC2DC)
private val DarkOnSecondary = Color(0xFF332D41)
private val DarkSecondaryContainer = Color(0xFF4A4458)
private val DarkOnSecondaryContainer = Color(0xFFE8DEF8)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    // ... all other colors
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    // ... all other colors
)

// Custom color extensions
val ColorScheme.success: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4CAF50) else Color(0xFF2E7D32)

val ColorScheme.warning: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF9800) else Color(0xFFED6C02)
```

### Typography Implementation

**AppTypographyImpl.kt** - Complete typography implementation:
```kotlin
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Spacing System

**AppSpacing.kt** - Consistent spacing system:
```kotlin
object AppSpacing {
    val xs = 4.dp      // Extra small spacing
    val s = 8.dp       // Small spacing
    val m = 16.dp      // Medium spacing (default)
    val l = 24.dp      // Large spacing
    val xl = 32.dp     // Extra large spacing
    val xxl = 48.dp    // Extra extra large spacing
    
    // Component specific spacing
    val cardPadding = m
    val listItemPadding = m
    val screenPadding = m
    val buttonPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    val chipPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    
    // Layout spacing
    val sectionSpacing = l
    val componentSpacing = s
    val contentSpacing = m
}
```

## 13. Usage Examples and Integration

### Basic Usage Examples

**UsageExamples.kt** - Practical implementation examples:
```kotlin
// Example screen implementation
@Composable
fun ExampleScreen() {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Example Screen",
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* action */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        AppLazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(AppSpacing.m)
        ) {
            item {
                ContentCard(
                    title = "Card Title",
                    subtitle = "Card subtitle"
                ) {
                    BodyMedium("Card content goes here")
                }
            }
            
            item {
                MediumSpacer()
            }
            
            items(10) { index ->
                ListItem(
                    headlineContent = { TitleMedium("Item $index") },
                    supportingContent = { BodySmall("Supporting text") },
                    leadingContent = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    },
                    onClick = { /* item click */ }
                )
            }
        }
    }
}

// Example form implementation
@Composable
fun ExampleForm() {
    Column(
        modifier = Modifier.padding(AppSpacing.m),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
    ) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var agreeToTerms by remember { mutableStateOf(false) }
        
        TitleLarge("Sign Up")
        
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = { LabelMedium("Full Name") },
            placeholder = { BodyMedium("Enter your name") }
        )
        
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = { LabelMedium("Email") },
            placeholder = { BodyMedium("Enter your email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        LabeledCheckbox(
            checked = agreeToTerms,
            onCheckedChange = { agreeToTerms = it },
            label = "I agree to the terms and conditions"
        )
        
        PrimaryButton(
            onClick = { /* submit */ },
            enabled = name.isNotBlank() && email.isNotBlank() && agreeToTerms,
            modifier = Modifier.fillMaxWidth()
        ) {
            LabelLarge("Sign Up")
        }
    }
}
```

### Integration Setup

**ThemeSetup.kt** - Complete theme integration:
```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
```

## 14. File Structure

Organize the UI system in this structure:
```
app/src/main/java/
├── ui/
│   ├── theme/
│   │   ├── AppColors.kt
│   │   ├── AppTypography.kt
│   │   ├── AppTheme.kt
│   │   └── AppSpacing.kt
│   ├── components/
│   │   ├── text/
│   │   │   ├── AppText.kt
│   │   │   └── TextExtensions.kt
│   │   ├── buttons/
│   │   │   ├── AppButtons.kt
│   │   │   └── ButtonVariants.kt
│   │   ├── cards/
│   │   │   ├── AppCards.kt
│   │   │   └── SpecializedCards.kt
│   │   ├── inputs/
│   │   │   ├── AppTextFields.kt
│   │   │   └── SelectionComponents.kt
│   │   ├── lists/
│   │   │   ├── AppLists.kt
│   │   │   └── SpecializedLists.kt
│   │   ├── navigation/
│   │   │   ├── AppBars.kt
│   │   │   ├── NavigationComponents.kt
│   │   │   └── TabComponents.kt
│   │   ├── dialogs/
│   │   │   ├── AppDialogs.kt
│   │   │   └── AppSheets.kt
│   │   ├── feedback/
│   │   │   ├── FeedbackComponents.kt
│   │   │   └── ProgressComponents.kt
│   │   ├── layout/
│   │   │   ├── LayoutComponents.kt
│   │   │   └── DividersAndSpacers.kt
│   │   ├── utils/
│   │   │   ├── ImageComponents.kt
│   │   │   └── BadgeComponents.kt
│   │   └── ComponentModule.kt (DI setup)
│   ├── screens/
│   │   └── examples/
│   │       ├── ExampleScreen.kt
│   │       └── UsageExamples.kt
```

## 15. Requirements & Constraints

**Performance:**
- Efficient recomposition with stable parameters
- Lazy loading for large lists
- Image caching and optimization
- Memory-efficient component state

**Accessibility:**
- Screen reader support for all components
- Proper content descriptions
- Touch target sizes (48dp minimum)
- Color contrast compliance (WCAG AA)
- Focus management and navigation

**Customization:**
- Easy theming with Material 3
- Component variants and configurations
- Responsive design for tablets
- RTL language support

**Technical:**
- Type-safe component APIs
- Consistent naming conventions
- Comprehensive documentation
- Unit tests for component logic
- Preview functions for all components

**Deliverable:** Complete, copy-paste ready Kotlin files that I can directly add to my Android project with minimal configuration required. Include comprehensive documentation, usage examples, accessibility guidelines, and integration instructions for a production-ready UI design system. 