# Extension Developer Guide

This guide explains how to create music extensions for the Async music player.

> **⚠️ Important API Changes**: This guide reflects the latest API changes:
> - `getStreamUrl(mediaId: String)` - parameter renamed from `trackId` to `mediaId`
> - `getAlbumArt(url: String): ExtensionResult<ByteArray>` - now downloads image data, not returns URL
> - Added `ExtensionException.ExtensionNotFound` for extension not found errors

## Overview

Extensions are APK files that implement the `MusicExtension` interface to provide music streaming functionality from various sources. Extensions run in a sandboxed environment and communicate with the main app through a well-defined API.

## Extension Architecture

### Core Interface

All extensions must implement the `MusicExtension` interface:

```kotlin
interface MusicExtension {
    val id: String
    val version: Int
    val name: String
    val developer: String
    val description: String
    val baseUrl: String
    
    suspend fun initialize(): ExtensionResult<Unit>
    suspend fun search(query: String): ExtensionResult<List<SearchResult>>
    suspend fun getStreamUrl(mediaId: String): ExtensionResult<String>
    suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray>
    suspend fun getArtistInfo(artistId: String): ExtensionResult<Artist?>
    suspend fun getAlbumInfo(albumId: String): ExtensionResult<Album?>
    suspend fun configure(settings: Map<String, Any>): ExtensionResult<Unit>
    suspend fun cleanup(): ExtensionResult<Unit>
}
```

### Data Models

Extensions work with these core data models:

```kotlin
@Serializable
data class SearchResult(
    val id: String,
    val extensionId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long?, // in milliseconds
    val thumbnailUrl: String?
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val description: String? = null
)

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String?,
    val year: Int?,
    val imageUrl: String?,
    val trackCount: Int?
)
```

## Creating an Extension

### 1. Project Setup

Create a new Android library module:

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yourname.async.extension.yourextension"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    // Include Async core API
    compileOnly(project(":core"))
    
    // Networking (if needed)
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
    
    // JSON parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

### 2. Implement the Extension

```kotlin
class YourMusicExtension : MusicExtension {
    
    override val id: String = "com.yourname.yourextension"
    override val version: Int = 1
    override val name: String = "Your Music Source"
    override val developer: String = "Your Name"
    override val description: String = "Extension for Your Music Source"
    override val baseUrl: String = "https://yourmusicapi.com"
    
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
        }
    }
    
    override suspend fun initialize(): ExtensionResult<Unit> {
        return try {
            // Perform any initialization (API keys, etc.)
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Initialization failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
    
    override suspend fun search(query: String): ExtensionResult<List<SearchResult>> {
        return try {
            // Make API call to search for music
            val response = httpClient.get("$baseUrl/search") {
                parameter("q", query)
                parameter("type", "track")
            }
            
            val searchData = response.body<YourApiSearchResponse>()
            
            val results = searchData.tracks.map { track ->
                SearchResult(
                    id = track.id,
                    extensionId = id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.durationMs,
                    thumbnailUrl = track.thumbnailUrl
                )
            }
            
            ExtensionResult.Success(results)
            
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.NetworkError(
                    "Search failed: ${e.message}"
                )
            )
        }
    }
    
    override suspend fun getStreamUrl(mediaId: String): ExtensionResult<String> {
        return try {
            // Get streaming URL for the media
            val response = httpClient.get("$baseUrl/track/$mediaId/stream")
            val streamData = response.body<YourApiStreamResponse>()
            
            ExtensionResult.Success(streamData.streamUrl)
            
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.NetworkError(
                    "Failed to get stream URL: ${e.message}"
                )
            )
        }
    }
    
    override suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray> {
        return try {
            // Download album art from URL
            val response = httpClient.get(url)
            val imageData = response.body<ByteArray>()
            
            ExtensionResult.Success(imageData)
            
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.NetworkError(
                    "Failed to get album art: ${e.message}"
                )
            )
        }
    }
    
    override suspend fun getArtistInfo(artistId: String): ExtensionResult<Artist?> {
        // Optional: Implement artist info retrieval
        return ExtensionResult.Success(null)
    }
    
    override suspend fun getAlbumInfo(albumId: String): ExtensionResult<Album?> {
        // Optional: Implement album info retrieval
        return ExtensionResult.Success(null)
    }
    
    override suspend fun configure(settings: Map<String, Any>): ExtensionResult<Unit> {
        // Optional: Handle extension settings
        return ExtensionResult.Success(Unit)
    }
    
    override suspend fun cleanup(): ExtensionResult<Unit> {
        return try {
            httpClient.close()
            ExtensionResult.Success(Unit)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError(
                    "Cleanup failed: ${e.message}",
                    e.javaClass.simpleName
                )
            )
        }
    }
}

// Your API response models
@Serializable
data class YourApiSearchResponse(
    val tracks: List<YourApiTrack>
)

@Serializable
data class YourApiTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long?,
    val thumbnailUrl: String?
)

@Serializable
data class YourApiStreamResponse(
    val streamUrl: String
)

@Serializable
data class YourApiTrackResponse(
    val id: String,
    val title: String,
    val albumArtUrl: String?
)
```

### 3. Create Extension Manifest

Create `src/main/assets/extension_manifest.json`:

```json
{
    "id": "com.yourname.yourextension",
    "version": 1,
    "name": "Your Music Source",
    "developer": "Your Name",
    "description": "Extension for Your Music Source",
    "baseUrl": "https://yourmusicapi.com",
    "permissions": [
        "INTERNET",
        "ACCESS_NETWORK_STATE"
    ],
    "minAppVersion": 1,
    "settings": [
        {
            "key": "api_key",
            "type": "text",
            "title": "API Key",
            "description": "Your API key for the service",
            "required": false
        },
        {
            "key": "quality",
            "type": "dropdown",
            "title": "Audio Quality",
            "description": "Preferred audio quality",
            "options": [
                {"value": "low", "label": "Low (128kbps)"},
                {"value": "medium", "label": "Medium (256kbps)"},
                {"value": "high", "label": "High (320kbps)"}
            ],
            "default": "medium"
        }
    ]
}
```

## Repository Structure

To distribute extensions, create a repository with this structure:

```
your-extension-repo/
├── manifest.json           # Repository manifest
├── extensions/            # Extension APK files
│   ├── yourextension-v1.apk
│   └── anotherextension-v2.apk
├── icons/                # Extension icons
│   ├── yourextension.png
│   └── anotherextension.png
└── README.md             # Repository documentation
```

### Repository Manifest

Create `manifest.json`:

```json
{
    "name": "Your Extension Repository",
    "description": "Collection of music extensions",
    "version": 1,
    "extensions": [
        {
            "id": "com.yourname.yourextension",
            "name": "Your Music Source",
            "version": 1,
            "developer": "Your Name",
            "description": "Extension for Your Music Source",
            "downloadPath": "extensions/yourextension-v1.apk",
            "iconUrl": "icons/yourextension.png",
            "sourceUrl": "https://github.com/yourname/yourextension",
            "permissions": ["INTERNET", "ACCESS_NETWORK_STATE"],
            "minAppVersion": 1
        }
    ]
}
```

## Exception Types

The extension system provides several exception types for different error scenarios:

- **`ExtensionException.NetworkError`** - Network-related errors (connection, timeout)
- **`ExtensionException.ParseError`** - JSON/data parsing errors  
- **`ExtensionException.AuthError`** - Authentication/authorization errors
- **`ExtensionException.RateLimitError`** - Rate limiting errors
- **`ExtensionException.NotFoundError`** - Content not found errors
- **`ExtensionException.ConfigurationError`** - Extension configuration errors
- **`ExtensionException.ExtensionNotFound`** - Extension not found errors
- **`ExtensionException.GenericError`** - Generic extension errors

## Best Practices

### Security
- Never hardcode API keys or sensitive data
- Validate all input parameters
- Handle network errors gracefully
- Use HTTPS for all network requests
- Implement proper timeout handling

### Performance
- Cache API responses when appropriate
- Use efficient JSON parsing
- Implement proper error handling
- Avoid blocking the main thread
- Use coroutines for async operations

### User Experience
- Provide meaningful error messages
- Implement proper loading states
- Support search suggestions
- Handle edge cases (no results, network errors)
- Optimize for different screen sizes

### Code Quality
- Follow Kotlin coding conventions
- Add proper documentation
- Write unit tests for your extension
- Use proper logging for debugging
- Handle all possible error scenarios

## Testing Your Extension

### Local Testing

1. Build your extension APK
2. Install it manually through the extension manager
3. Test all functionality (search, stream, metadata)
4. Check error handling and edge cases

### Repository Testing

1. Host your repository manifest
2. Add the repository URL to the app
3. Test installation from repository
4. Verify updates work correctly

## Distribution

### GitHub Repository

1. Create a GitHub repository for your extensions
2. Use GitHub Pages to host the manifest
3. Set up GitHub Actions for automated builds
4. Tag releases for version management

### Example Repository URL
```
https://yourname.github.io/async-extensions/manifest.json
```

## Troubleshooting

### Common Issues

1. **Extension not loading**: Check manifest format and required fields
2. **Network errors**: Verify API endpoints and authentication  
3. **Search returning no results**: Debug API response parsing
4. **Stream URLs not working**: Check URL format and expiration
5. **Installation failing**: Verify APK signing and permissions
6. **Method signature mismatch**: Ensure `getStreamUrl(mediaId: String)` and `getAlbumArt(url: String): ByteArray`
7. **Wrong return types**: `getAlbumArt` must return `ByteArray`, not `String?`

### Debugging

- Use `logcat` for debugging during development
- Check extension logs in the app's extension manager
- Test with different network conditions
- Verify API responses with tools like Postman

## Example Extensions

See the `examples/` directory for complete extension implementations:

- `SpotifyExtension` - Spotify Web API integration
- `YouTubeExtension` - YouTube Music integration
- `SoundCloudExtension` - SoundCloud API integration

## API Reference

For complete API documentation, see [ExtensionAPI.md](ExtensionAPI.md)

## Support

- GitHub Issues: [Report bugs and feature requests](https://github.com/async/extensions/issues)
- Discussions: [Community support](https://github.com/async/extensions/discussions)
- Documentation: [Extension API docs](https://async.dev/extensions) 