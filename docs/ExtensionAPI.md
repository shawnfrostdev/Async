# Async Music Player - Extension API Documentation

This document provides comprehensive information for developers who want to create extensions for the Async Music Player.

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [API Reference](#api-reference)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Security Guidelines](#security-guidelines)
7. [Best Practices](#best-practices)
8. [Example Extension](#example-extension)
9. [Testing](#testing)
10. [Distribution](#distribution)

## Overview

Async Music Player uses a dynamic extension system that allows developers to add support for various music sources without modifying the core application. Extensions are loaded at runtime and provide music search, streaming, and metadata capabilities.

### Key Features

- **Dynamic Loading**: Extensions are loaded from APK/JAR files at runtime
- **Sandboxed Execution**: Extensions run in isolated environments
- **Async Operations**: All extension methods are suspend functions
- **Rich Error Handling**: Comprehensive error types and handling
- **Configuration Support**: Extensions can provide user-configurable settings
- **Metadata Rich**: Support for detailed track, artist, and album information

### Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Async App     │    │  Extension API  │    │  Your Extension │
│                 │◄──►│                 │◄──►│                 │
│  - UI Layer     │    │  - Interfaces   │    │  - Implementation│
│  - Player       │    │  - Data Models  │    │  - Music Source │
│  - Management   │    │  - Error Types  │    │  - Custom Logic │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Getting Started

### Prerequisites

- Android development environment (Android Studio)
- Kotlin knowledge
- Understanding of coroutines and suspend functions
- Target Android API level 24+

### Creating Your First Extension

1. **Create a new Android Library module**:
   ```kotlin
   // build.gradle.kts
   plugins {
       id("com.android.library")
       id("org.jetbrains.kotlin.android")
       id("kotlinx-serialization")
   }
   
   dependencies {
       implementation(project(":core")) // Async core module
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
       // Add your networking libraries (OkHttp, Ktor, etc.)
   }
   ```

2. **Implement the MusicExtension interface**:
   ```kotlin
   class MyMusicExtension : MusicExtension {
       override val id = "com.example.mymusicextension"
       override val version = 1
       override val name = "My Music Extension"
       override val developer = "Your Name"
       override val description = "Extension for accessing music from MyMusicService"
       
       override suspend fun search(query: String, limit: Int, offset: Int): ExtensionResult<List<SearchResult>> {
           // Implementation here
       }
       
       override suspend fun getStreamUrl(mediaId: String): ExtensionResult<String> {
           // Implementation here
       }
       
       override suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray> {
           // Implementation here
       }
   }
   ```

## API Reference

### MusicExtension Interface

The core interface that all extensions must implement:

#### Required Properties

- `id: String` - Unique extension identifier (reverse domain format)
- `version: Int` - Extension version for compatibility
- `name: String` - Human-readable extension name
- `developer: String` - Developer/organization name
- `description: String` - Extension description

#### Optional Properties

- `iconUrl: String?` - URL to extension icon
- `websiteUrl: String?` - Extension website/source code URL
- `minApiLevel: Int` - Minimum required API level (default: 1)
- `maxApiLevel: Int` - Maximum supported API level (default: Int.MAX_VALUE)
- `requiresNetwork: Boolean` - Whether extension needs network access (default: true)

#### Required Methods

##### `search(query: String, limit: Int = 50, offset: Int = 0): ExtensionResult<List<SearchResult>>`
Search for music tracks based on a query string.

**Parameters:**
- `query` - Search terms (song title, artist, album, etc.)
- `limit` - Maximum results to return
- `offset` - Pagination offset

**Returns:** List of `SearchResult` objects

##### `getStreamUrl(mediaId: String): ExtensionResult<String>`
Get the direct streaming URL for a track.

**Parameters:**
- `mediaId` - Track ID from `SearchResult.id`

**Returns:** Direct streaming URL

##### `getAlbumArt(url: String): ExtensionResult<ByteArray>`
Download album artwork or thumbnail.

**Parameters:**
- `url` - Image URL from `SearchResult.thumbnailUrl` or `Album.artworkUrl`

**Returns:** Image data as ByteArray

#### Optional Methods

Extensions can optionally implement these methods for enhanced functionality:

- `getArtist(artistId: String): ExtensionResult<Artist>`
- `getAlbum(albumId: String): ExtensionResult<Album>`
- `getAlbumTracks(albumId: String): ExtensionResult<List<SearchResult>>`
- `getArtistTracks(artistId: String, limit: Int, offset: Int): ExtensionResult<List<SearchResult>>`
- `initialize(): ExtensionResult<Unit>`
- `cleanup()`
- `getConfiguration(): Map<String, Any>`
- `updateConfiguration(config: Map<String, Any>): ExtensionResult<Unit>`

## Data Models

### SearchResult

Represents a music track found by the extension:

```kotlin
@Serializable
data class SearchResult(
    val id: String,              // Unique track ID
    val title: String,           // Track title
    val artist: String? = null,  // Artist name
    val album: String? = null,   // Album name
    val duration: Long? = null,  // Duration in milliseconds
    val thumbnailUrl: String? = null, // Thumbnail URL
    val extensionId: String = "", // Set automatically
    val metadata: Map<String, String> = emptyMap() // Additional data
)
```

### Artist

Represents a music artist:

```kotlin
@Serializable
data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val biography: String? = null,
    val extensionId: String = "",
    val metadata: Map<String, String> = emptyMap()
)
```

### Album

Represents a music album:

```kotlin
@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val artworkUrl: String? = null,
    val description: String? = null,
    val trackCount: Int? = null,
    val duration: Long? = null,
    val extensionId: String = "",
    val metadata: Map<String, String> = emptyMap()
)
```

## Error Handling

Extensions should use the `ExtensionResult<T>` wrapper for all operations:

### ExtensionResult

```kotlin
sealed class ExtensionResult<out T> {
    data class Success<T>(val data: T) : ExtensionResult<T>()
    data class Error(val exception: ExtensionException) : ExtensionResult<Nothing>()
    data object Loading : ExtensionResult<Nothing>()
}
```

### ExtensionException Types

- `NetworkError` - Connection, timeout, HTTP errors
- `ParseError` - Data parsing failures
- `AuthError` - Authentication/authorization issues
- `RateLimitError` - Rate limiting
- `NotFoundError` - Content not found
- `ConfigurationError` - Extension configuration issues
- `GenericError` - Other errors

### Example Error Handling

```kotlin
override suspend fun search(query: String, limit: Int, offset: Int): ExtensionResult<List<SearchResult>> {
    return try {
        val response = httpClient.get("https://api.example.com/search?q=$query")
        if (response.status.isSuccess()) {
            val results = parseSearchResults(response.bodyAsText())
            ExtensionResult.Success(results)
        } else {
            ExtensionResult.Error(
                ExtensionException.NetworkError(
                    "Search failed with status ${response.status}",
                    response.status.value
                )
            )
        }
    } catch (e: Exception) {
        ExtensionResult.Error(
            ExtensionException.GenericError(
                "Search failed: ${e.message}",
                e.javaClass.simpleName
            )
        )
    }
}
```

## Security Guidelines

### Sandboxing
- Extensions run in isolated ClassLoaders
- Limited access to system resources
- Network access must be declared

### Permissions
Extensions should declare required permissions:
```kotlin
override val requiresNetwork = true
// Additional permissions can be specified in metadata
```

### Data Validation
- Always validate input parameters
- Sanitize URLs and user input
- Implement proper error handling

### Rate Limiting
- Respect source website's rate limits
- Implement exponential backoff
- Cache responses when appropriate

## Best Practices

### Performance
- Use coroutines for async operations
- Implement proper cancellation
- Cache frequently accessed data
- Minimize memory usage

### User Experience
- Provide meaningful error messages
- Support pagination for large result sets
- Include rich metadata when available
- Implement configuration options

### Reliability
- Handle network failures gracefully
- Implement retry logic with backoff
- Validate all external data
- Log errors appropriately

### Code Quality
- Follow Kotlin coding conventions
- Write unit tests for core functionality
- Document public APIs
- Use meaningful variable names

## Example Extension

Here's a complete example of a simple extension:

```kotlin
class ExampleMusicExtension : MusicExtension {
    override val id = "com.example.musicextension"
    override val version = 1
    override val name = "Example Music Extension"
    override val developer = "Example Developer"
    override val description = "Example extension for demonstration"
    
    private val httpClient = HttpClient()
    
    override suspend fun search(
        query: String, 
        limit: Int, 
        offset: Int
    ): ExtensionResult<List<SearchResult>> {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val response = httpClient.get("https://api.example.com/search") {
                parameter("q", encodedQuery)
                parameter("limit", limit)
                parameter("offset", offset)
            }
            
            val results = parseSearchResponse(response.bodyAsText())
            ExtensionResult.Success(results)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError("Search failed: ${e.message}")
            )
        }
    }
    
    override suspend fun getStreamUrl(mediaId: String): ExtensionResult<String> {
        return try {
            val response = httpClient.get("https://api.example.com/track/$mediaId")
            val trackInfo = parseTrackResponse(response.bodyAsText())
            ExtensionResult.Success(trackInfo.streamUrl)
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.GenericError("Failed to get stream URL: ${e.message}")
            )
        }
    }
    
    override suspend fun getAlbumArt(url: String): ExtensionResult<ByteArray> {
        return try {
            val response = httpClient.get(url)
            ExtensionResult.Success(response.body())
        } catch (e: Exception) {
            ExtensionResult.Error(
                ExtensionException.NetworkError("Failed to download image: ${e.message}")
            )
        }
    }
    
    private fun parseSearchResponse(json: String): List<SearchResult> {
        // Parse JSON and return SearchResult objects
        // Implementation depends on the API response format
    }
    
    private fun parseTrackResponse(json: String): TrackInfo {
        // Parse JSON and return track information
    }
    
    override suspend fun cleanup() {
        httpClient.close()
    }
}
```

## Testing

### Unit Testing
Create unit tests for your extension logic:

```kotlin
class ExampleMusicExtensionTest {
    private val extension = ExampleMusicExtension()
    
    @Test
    fun `search returns results for valid query`() = runTest {
        val result = extension.search("test query")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }
    
    @Test
    fun `getStreamUrl returns valid URL`() = runTest {
        val result = extension.getStreamUrl("test-id")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.startsWith("http") == true)
    }
}
```

### Integration Testing
Test your extension with real API calls (use test endpoints when available).

## Distribution

### Building
1. Build your extension as an Android Library (AAR)
2. Ensure all dependencies are included
3. Test thoroughly on different Android versions

### Packaging
Extensions can be distributed as:
- APK files (recommended for complex extensions)
- JAR files (for simple extensions)
- ZIP archives containing multiple files

### Installation
Users can install extensions by:
1. Downloading the extension file
2. Opening it with Async Music Player
3. Confirming the installation
4. Configuring extension settings if needed

### Updates
- Increment the `version` property for updates
- Ensure backward compatibility when possible
- Provide migration logic for breaking changes

## API Versioning

Current API version: **1**

When the API changes:
- Minor changes: Same API version, backward compatible
- Major changes: New API version, may break compatibility

Extensions should specify compatible API versions:
```kotlin
override val minApiLevel = 1  // Minimum required API level
override val maxApiLevel = 2  // Maximum supported API level
```

---

## Support

For questions and support:
- Check the [GitHub repository](https://github.com/async-music-player/async)
- Join our [Discord community](https://discord.gg/async-music)
- Read the [FAQ](https://async-player.com/faq)

## Legal

- Extensions are the responsibility of their developers
- Follow all applicable laws and terms of service
- Respect copyright and licensing requirements
- Async Music Player is not responsible for extension behavior 