# Async Music Player - Dependencies Analysis

This document lists all dependencies actually used in the Async music player app, categorized by purpose and explaining what each dependency does.

## Core Architecture Dependencies

### Multi-Module Project Dependencies
```kotlin
// Internal modules
implementation(project(":core"))                // Shared models and extension contracts
implementation(project(":domain"))              // Business logic and use cases
implementation(project(":data"))                // Data layer with Room database
implementation(project(":playback"))            // Audio playback service
implementation(project(":extensions"))          // Extension system implementation
```

## UI Framework Dependencies

### Jetpack Compose (Material 3)
```kotlin
// Compose BOM for version alignment
implementation(platform(libs.androidx.compose.bom))  // Compose Bill of Materials (2024.04.01)

// Core Compose
implementation(libs.androidx.ui)                     // Core UI components
implementation(libs.androidx.ui.graphics)            // Graphics utilities
implementation(libs.androidx.ui.tooling.preview)     // Preview annotations
implementation(libs.androidx.material3)              // Material Design 3 components
implementation(libs.androidx.material.icons.extended) // Extended Material Design icons (1.7.6)

// Debug tools
debugImplementation(libs.androidx.ui.tooling)        // Development tools (debug only)
debugImplementation(libs.androidx.ui.test.manifest)  // Test manifest
```

### AndroidX Core Libraries
```kotlin
// Core AndroidX
implementation(libs.androidx.core.ktx)               // Core KTX extensions (1.13.1)
implementation(libs.androidx.lifecycle.runtime.ktx)  // Lifecycle runtime KTX (2.9.2)
implementation(libs.androidx.activity.compose)       // Activity integration for Compose (1.10.1)

// ViewModel
implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel Compose (2.9.2)
```

## Navigation Dependencies

### Voyager Navigation (NOT Navigation Compose)
```kotlin
// Complete navigation solution - Used instead of Navigation Compose
implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")      // Core navigation
implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0")  // Tab navigation
implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")    // Screen transitions
```

## Data Processing Dependencies

### Database (Room)
```kotlin
// Room Database (2.6.1)
implementation(libs.androidx.room.runtime)          // Room runtime
implementation(libs.androidx.room.ktx)              // Room KTX extensions
kapt(libs.androidx.room.compiler)                   // Room annotation processor

// Additional data storage
implementation("androidx.preference:preference-ktx:1.2.1")        // SharedPreferences
implementation("androidx.datastore:datastore-preferences:1.0.0")  // DataStore (unused but available)
```

### Serialization
```kotlin
// Kotlinx Serialization (1.7.3)
implementation(libs.kotlinx.serialization.json)     // JSON serialization
```

## Media Playback Dependencies

### ExoPlayer (Media3)
```kotlin
// ExoPlayer and Media3 (1.5.0)
implementation(libs.androidx.media3.exoplayer)      // Core ExoPlayer
implementation(libs.androidx.media3.ui)             // ExoPlayer UI components
implementation(libs.androidx.media3.session)        // Media3 session support

// Legacy MediaSession support
implementation("androidx.media:media:1.7.0")        // MediaBrowserServiceCompat support

// Notifications
implementation("androidx.core:core:1.13.1")         // Core notification support
```

### Image Loading (Partially Used)
```kotlin
// Glide (4.16.0) - For album art loading
implementation("com.github.bumptech.glide:glide:4.16.0")         // Image loading
kapt("com.github.bumptech.glide:compiler:4.16.0")                // Glide annotation processor
// Note: Currently commented out in PlaybackNotificationManager
```

## Language and Concurrency

### Kotlin Extensions
```kotlin
// Coroutines (1.9.0)
implementation(libs.kotlinx.coroutines.core)        // Core coroutines
implementation(libs.kotlinx.coroutines.android)     // Android coroutines
```

## Dependency Injection

### Manual Dependency Injection (NOT Hilt)
```kotlin
// Custom AppModule object - Manual DI implementation
object AppModule {
    private lateinit var application: Application
    
    // Lazy initialized dependencies
    private val _database: AsyncDatabase by lazy { /* Room database */ }
    private val _extensionManager: ExtensionManager by lazy { /* Extension system */ }
    private val _playbackManager: PlaybackManager by lazy { /* Playback service */ }
    // ... other dependencies
    
    fun initialize(app: Application) {
        application = app
    }
    
    // Public accessors for dependencies
    fun getExtensionManager(): ExtensionManager = _extensionManager
    fun getPlaybackManager(): PlaybackManager = _playbackManager
    // ... other getters
}
```

**Note**: The project was originally planned to use Hilt, but due to compilation issues on Windows with long package paths, it was replaced with a simpler manual dependency injection system using an object-based service locator pattern.

## Development and Debugging

### Logging
```kotlin
// Logcat (0.1) - Used instead of Timber
implementation("com.squareup.logcat:logcat:0.1")    // Enhanced logging for debug
```

## Testing Dependencies

### Unit Testing
```kotlin
testImplementation(libs.junit)                      // JUnit (4.13.2)
testImplementation("androidx.room:room-testing:2.6.1")           // Room testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Coroutines testing

// UI Testing
androidTestImplementation(libs.androidx.junit)      // AndroidX JUnit (1.3.0)
androidTestImplementation(libs.androidx.espresso.core) // Espresso (3.7.0)
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.ui.test.junit4)
```

## Build System Dependencies

### Gradle Plugins
```kotlin
// Main plugins used
alias(libs.plugins.android.application)             // Android Application Plugin
alias(libs.plugins.android.library)                 // Android Library Plugin  
alias(libs.plugins.kotlin.android)                  // Kotlin Android Plugin
alias(libs.plugins.kotlin.compose)                  // Kotlin Compose Plugin
alias(libs.plugins.kotlin.serialization)            // Kotlin Serialization Plugin
alias(libs.plugins.kotlin.kapt)                     // Kotlin KAPT Plugin
alias(libs.plugins.hilt.android)                    // Hilt Android Plugin
```

## Version Information

### Key Version Numbers
- **Android Gradle Plugin**: 8.8.0
- **Kotlin**: 2.0.0
- **Compose BOM**: 2024.04.01
- **Room**: 2.6.1
- **Hilt**: 2.53.1
- **ExoPlayer (Media3)**: 1.5.0
- **Coroutines**: 1.9.0
- **Voyager**: 1.0.0

## Dependencies NOT Currently Used

### Listed in docs but not implemented:
- **Hilt** - Replaced with manual dependency injection due to Windows compilation issues
- **Timber** - Replaced with Logcat
- **Ktor Client** - No networking implementation yet
- **Navigation Compose** - Using Voyager instead
- **DataStore** - Available but using SharedPreferences
- **Glide** - Imported but commented out in code

### Missing from current implementation:
- **Analytics** - No Firebase or crash reporting
- **Testing** - Minimal test coverage
- **Networking** - No HTTP client implemented
- **Image Loading** - Glide available but not actively used

## Architecture Highlights

### Clean Architecture Layers
1. **Presentation**: Compose UI + Voyager navigation
2. **Domain**: Business logic with repository interfaces  
3. **Data**: Room database + Repository implementations
4. **Extensions**: Dynamic extension loading system
5. **Playback**: ExoPlayer service with MediaSession

### Key Design Patterns
- **MVVM**: ViewModels with Compose state management
- **Repository Pattern**: Data abstraction layer
- **Extension System**: Dynamic code loading with security
- **Clean Architecture**: Clear separation of concerns across modules

### Current Status
- **✅ Working**: Core dependencies, UI system, database, playback engine, manual DI
- **⚠️ Partial**: Image loading (Glide commented out)
- **❌ Missing**: Networking, analytics, comprehensive testing, Hilt (replaced with manual DI)

This dependency analysis reflects the actual current state of the Async music player codebase as of the latest commit. 