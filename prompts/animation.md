# Android Animation System Implementation Prompt

Create a comprehensive, production-ready animation system for my Android app based on the Mihon manga reader architecture. Implement the following components:

## 1. Core Animation Architecture

**Create these files:**

### AnimationConfig.kt
```kotlin
// System-wide animation configuration that respects user preferences and system settings
// Include: duration multipliers, transition toggles, performance modes, accessibility support
// Must read from Android's ANIMATOR_DURATION_SCALE setting
```

### AppAnimationSpecs.kt
```kotlin
// Centralized animation specifications with Material Design 3 compliant timing and easing
// Include: micro (100ms), short (200ms), medium (300ms), long (500ms) durations
// Include: emphasized, standard, decelerate, accelerate easing curves
// Make all specs configurable through AnimationConfig
```

### NavigationTransitions.kt
```kotlin
// Flexible navigation transition system using Voyager + Compose
// Support: SharedAxisX, SharedAxisY, FadeThrough, ContainerTransform, Slide
// Direction-aware animations (forward/backward)
// Configurable transition types per screen
```

## 2. UI Component Animations

### AnimatedFAB.kt
```kotlin
// Extended FAB with smooth expand/collapse animations
// Features: width animation, text fade in/out, staggered timing
// Custom easing curves for Material 3 feel
```

### AnimatedProgressIndicator.kt
```kotlin
// Combined determinate/indeterminate progress indicator
// Smooth progress animations, infinite rotation for indeterminate
// Respect system animation scale
```

### AnimatedSheet.kt
```kotlin
// Bottom sheet with smooth slide animations
// Drag gesture integration, settle animations
// Support for different sheet sizes and behaviors
```

### AnimatedVisibilityComponents.kt
```kotlin
// Reusable components for enter/exit animations
// Slide, fade, scale, expand/shrink variants
// Composable modifiers for easy application
```

## 3. Advanced Animation Features

### ChapterTransitionAnimation.kt
```kotlin
// Page-to-page transition animations for reader-like apps
// Smooth page turning effects, chapter boundary indicators
// User-configurable transition styles
```

### ListItemAnimations.kt
```kotlin
// Smooth list animations: add, remove, reorder
// Shared element transitions between list and detail views
// Staggered animations for list updates
```

### LoadingAnimations.kt
```kotlin
// Various loading states with animations
// Skeleton loading, shimmer effects, progressive loading
// Smooth state transitions between loading/content/error
```

## 4. Animation Utilities

### AnimationExtensions.kt
```kotlin
// Extension functions for common animation patterns
// System animation scale integration
// Conditional animations based on user preferences
```

### AnimationPreviewHelpers.kt
```kotlin
// Preview composables for testing animations
// Animation state management helpers
// Debug tools for animation timing
```

## 5. Requirements & Constraints

**Performance:**
- Target 60fps on mid-range devices
- Respect system animation preferences
- Graceful degradation for low-end devices
- Efficient memory usage during animations

**Accessibility:**
- Support reduced motion preferences
- High contrast mode compatibility
- Screen reader friendly animations
- Configurable animation speeds

**Customization:**
- User settings for animation preferences
- Per-screen transition overrides
- Performance mode selection
- Easy theme integration

**Technical:**
- Use Jetpack Compose Animation APIs
- Support for legacy View animations where needed
- Proper lifecycle management
- State restoration after configuration changes

## 6. Integration Points

Create the animation system to integrate with:
- Navigation component/Voyager
- Material 3 theming
- Preference management system  
- Performance monitoring
- Accessibility services

## 7. Implementation Guidelines

- Follow Material Design 3 motion principles
- Use composition over inheritance
- Make animations interruptible and reversible  
- Provide sensible defaults with easy customization
- Include comprehensive documentation and examples
- Add unit tests for animation logic
- Implement proper error handling

## 8. Detailed Implementation Requirements

### Core Animation System

**AnimationConfig.kt** - Create a comprehensive animation configuration system:
```kotlin
@Stable
data class AnimationConfig(
    val durationMultiplier: Float = 1f,
    val enableTransitions: Boolean = true,
    val enableMicroInteractions: Boolean = true,
    val enableLargeMotions: Boolean = true,
    val reducedMotion: Boolean = false,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED
) {
    companion object {
        fun fromSystemSettings(context: Context): AnimationConfig
        fun disabled(): AnimationConfig
        fun highPerformance(): AnimationConfig
    }
}

enum class PerformanceMode {
    HIGH_PERFORMANCE,  // 60fps+, full animations
    BALANCED,          // Standard animations
    BATTERY_SAVER     // Reduced animations
}

class AnimationConfigProvider {
    val config: StateFlow<AnimationConfig>
    fun updateConfig(newConfig: AnimationConfig)
    fun observeSystemSettings(context: Context)
}
```

**AppAnimationSpecs.kt** - Material Design 3 compliant specifications:
```kotlin
object AppAnimationSpecs {
    object Duration {
        const val MICRO = 100      // Button press, ripple
        const val SHORT = 200      // Small state changes
        const val MEDIUM = 300     // Screen transitions
        const val LONG = 500       // Complex animations
        const val EXTRA_LONG = 1000 // Dramatic effects
    }
    
    object Easing {
        val Linear: Easing
        val FastOutSlowIn: Easing
        val EmphasizedDecelerate: Easing
        val EmphasizedAccelerate: Easing
        val Emphasized: Easing
    }
    
    fun microInteraction(config: AnimationConfig): AnimationSpec<Float>
    fun contentTransition(config: AnimationConfig): AnimationSpec<Float>
    fun largeMotion(config: AnimationConfig): AnimationSpec<Float>
    fun enter(config: AnimationConfig): EnterTransition
    fun exit(config: AnimationConfig): ExitTransition
}
```

### Navigation System

**NavigationTransitions.kt** - Flexible navigation animations:
```kotlin
@Composable
fun AppNavigationTransitions(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    transitionType: TransitionType = TransitionType.SHARED_AXIS_X
)

enum class TransitionType {
    SHARED_AXIS_X, SHARED_AXIS_Y, FADE_THROUGH, 
    CONTAINER_TRANSFORM, SLIDE, NONE
}

@Composable
fun ScreenTransition(
    navigator: Navigator,
    transition: AnimatedContentTransitionScope<Screen>.() -> ContentTransform,
    modifier: Modifier = Modifier,
    content: ScreenTransitionContent = { it.Content() }
)
```

### UI Components

**AnimatedFAB.kt** - Extended floating action button:
```kotlin
@Composable
fun AnimatedExtendedFAB(
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)

@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    icon: @Composable () -> Unit,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)
```

**AnimatedProgressIndicator.kt** - Smart progress indicators:
```kotlin
@Composable
fun AnimatedCircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)

@Composable
fun AnimatedLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)
```

**AnimatedSheet.kt** - Bottom sheet animations:
```kotlin
@Composable
fun AnimatedBottomSheet(
    state: SheetState,
    modifier: Modifier = Modifier,
    animationConfig: AnimationConfig = LocalAnimationConfig.current,
    content: @Composable ColumnScope.() -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAnimatedSheetState(
    skipPartiallyExpanded: Boolean = false,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
): SheetState
```

### Advanced Features

**LoadingAnimations.kt** - Loading state animations:
```kotlin
@Composable
fun ShimmerLoading(
    modifier: Modifier = Modifier,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)

@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
)
```

**ListItemAnimations.kt** - List animation utilities:
```kotlin
@Composable
fun AnimatedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    animationConfig: AnimationConfig = LocalAnimationConfig.current,
    content: LazyListScope.() -> Unit
)

fun LazyListScope.animatedItem(
    key: Any? = null,
    contentType: Any? = null,
    animationConfig: AnimationConfig = LocalAnimationConfig.current,
    content: @Composable LazyItemScope.() -> Unit
)

@Composable
fun SharedElementTransition(
    key: Any,
    modifier: Modifier = Modifier,
    animationConfig: AnimationConfig = LocalAnimationConfig.current,
    content: @Composable () -> Unit
)
```

### Utilities

**AnimationExtensions.kt** - Helper extensions:
```kotlin
// Extension for conditional animations
fun Modifier.animateIf(
    condition: Boolean,
    animationSpec: AnimationSpec<Float>,
    targetValue: Float
): Modifier

// System animation scale integration
val Context.animatorDurationScale: Float

// Animation state helpers
@Composable
fun rememberAnimationState(): AnimationState

// Gesture animation integration
fun Modifier.animateOnGesture(
    onGesture: (Offset) -> Unit,
    animationConfig: AnimationConfig = LocalAnimationConfig.current
): Modifier
```

**AnimationPreviewHelpers.kt** - Development tools:
```kotlin
@Preview
@Composable
fun AnimationPreview(
    content: @Composable () -> Unit
)

@Composable
fun AnimationDebugOverlay(
    showTimings: Boolean = false,
    showBounds: Boolean = false,
    content: @Composable () -> Unit
)

// Animation testing utilities
class AnimationTestRule : TestRule
```

## 9. Integration Setup

### CompositionLocal Setup
```kotlin
val LocalAnimationConfig = compositionLocalOf { AnimationConfig() }

@Composable
fun ProvideAnimationConfig(
    config: AnimationConfig,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAnimationConfig provides config,
        content = content
    )
}
```

### Theme Integration
```kotlin
// Add to your app theme
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    animationConfig: AnimationConfig = AnimationConfig.fromSystemSettings(LocalContext.current),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        // ... your theme setup
    ) {
        ProvideAnimationConfig(animationConfig) {
            content()
        }
    }
}
```

### Usage Examples

Include practical examples for:
1. Screen transitions setup
2. List item animations
3. Loading state management
4. User preference integration
5. Performance optimization
6. Accessibility compliance

**Deliverable:** Complete, copy-paste ready Kotlin files that I can directly add to my Android project with minimal configuration required. Include usage examples, integration instructions, and comprehensive documentation.

## 10. File Structure

Organize the animation system in this structure:
```
app/src/main/java/
├── ui/
│   ├── animation/
│   │   ├── config/
│   │   │   ├── AnimationConfig.kt
│   │   │   ├── AppAnimationSpecs.kt
│   │   │   └── AnimationConfigProvider.kt
│   │   ├── navigation/
│   │   │   ├── NavigationTransitions.kt
│   │   │   └── TransitionTypes.kt
│   │   ├── components/
│   │   │   ├── AnimatedFAB.kt
│   │   │   ├── AnimatedProgressIndicator.kt
│   │   │   ├── AnimatedSheet.kt
│   │   │   └── AnimatedVisibilityComponents.kt
│   │   ├── advanced/
│   │   │   ├── ChapterTransitionAnimation.kt
│   │   │   ├── ListItemAnimations.kt
│   │   │   └── LoadingAnimations.kt
│   │   ├── utils/
│   │   │   ├── AnimationExtensions.kt
│   │   │   └── AnimationPreviewHelpers.kt
│   │   └── AnimationModule.kt (DI setup)
```

Create all files with proper package declarations, imports, and documentation. Make the system modular so components can be used independently or together. 