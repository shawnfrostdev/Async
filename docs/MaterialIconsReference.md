# Material Icons Reference

## Overview
This document provides guidance on using [Google Material Icons](https://fonts.google.com/icons) in our Async music player app.

## Current Status
The app currently uses basic Material Icons that are guaranteed to be available in the `androidx.compose.material.icons.filled` package:

### Currently Used Icons:
- **Home**: `Icons.Default.Home` ✅
- **Search**: `Icons.Default.Search` ✅  
- **Library**: `Icons.Default.Star` ⚠️ (placeholder)
- **Settings**: `Icons.Default.Settings` ✅
- **Favorite**: `Icons.Default.Favorite` ✅
- **Play**: `Icons.Default.PlayArrow` ✅
- **Info**: `Icons.Default.Info` ✅
- **Add**: `Icons.Default.Add` ⚠️ (placeholder)

## Desired Music-Specific Icons
Based on [Google Material Icons](https://fonts.google.com/icons), we would prefer:

### Navigation Icons:
- **Library**: `LibraryMusic` (instead of Star)
- **Audio Quality**: `GraphicEq` (instead of Star) 
- **Theme**: `Palette` (instead of Add)

### Library Screen Icons:
- **Playlists**: `PlaylistPlay` (instead of PlayArrow)
- **Recently Played**: `History` (instead of Star)

## Implementation Options

### Option 1: Material Icons Extended
Add the extended Material Icons library to get more icons:
```kotlin
implementation "androidx.compose.material:material-icons-extended:$compose_version"
```

### Option 2: Custom SVG Icons
Create custom SVG icons and convert them to vector drawables:
```kotlin
// Example usage
Icon(
    painter = painterResource(id = R.drawable.ic_library_music),
    contentDescription = "Library"
)
```

### Option 3: Material Symbols (Newer)
Use the newer Material Symbols library:
```kotlin
// Requires additional setup
implementation "androidx.compose.material3:material3:$material3_version"
```

## Future Task
- [ ] Implement proper music-specific icons
- [ ] Test extended Material Icons library
- [ ] Create consistent icon theming
- [ ] Add icon size and color customization

## References
- [Google Material Icons](https://fonts.google.com/icons)
- [Material Design Icons Guidelines](https://material.io/design/iconography/system-icons.html)
- [Compose Material Icons Documentation](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary) 