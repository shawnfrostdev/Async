# Font Setup Guide - DM Mono

The Async Music Player uses **DM Mono** throughout the entire application for a consistent, modern monospace appearance.

## Current Status

Currently using `FontFamily.Monospace` (system monospace font) as a fallback. To use the actual DM Mono font, follow the steps below.

## Adding Actual DM Mono Font Files

### Step 1: Download DM Mono Fonts

1. **Visit Google Fonts**: Go to [DM Mono on Google Fonts](https://fonts.google.com/specimen/DM+Mono)
2. **Download Font Family**: Click "Download family" to get the ZIP file
3. **Extract Files**: You'll need these TTF files:
   - `DMMono-Light.ttf`
   - `DMMono-LightItalic.ttf`
   - `DMMono-Regular.ttf`
   - `DMMono-RegularItalic.ttf`
   - `DMMono-Medium.ttf`
   - `DMMono-MediumItalic.ttf`

### Step 2: Add Font Files to Project

1. **Place TTF Files**: Copy the TTF files to `app/src/main/res/font/`
2. **Rename Files** (Android requires lowercase with underscores):
   ```
   DMMono-Light.ttf → dm_mono_light.ttf
   DMMono-LightItalic.ttf → dm_mono_light_italic.ttf
   DMMono-Regular.ttf → dm_mono_regular.ttf
   DMMono-RegularItalic.ttf → dm_mono_regular_italic.ttf
   DMMono-Medium.ttf → dm_mono_medium.ttf
   DMMono-MediumItalic.ttf → dm_mono_medium_italic.ttf
   ```

### Step 3: Create Font Family XML

Create `app/src/main/res/font/dm_mono.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- DM Mono Light 300 -->
    <font
        android:fontStyle="normal"
        android:fontWeight="300"
        android:font="@font/dm_mono_light" />
    
    <!-- DM Mono Light Italic 300 -->
    <font
        android:fontStyle="italic"
        android:fontWeight="300"
        android:font="@font/dm_mono_light_italic" />
    
    <!-- DM Mono Regular 400 -->
    <font
        android:fontStyle="normal"
        android:fontWeight="400"
        android:font="@font/dm_mono_regular" />
    
    <!-- DM Mono Regular Italic 400 -->
    <font
        android:fontStyle="italic"
        android:fontWeight="400"
        android:font="@font/dm_mono_regular_italic" />
    
    <!-- DM Mono Medium 500 -->
    <font
        android:fontStyle="normal"
        android:fontWeight="500"
        android:font="@font/dm_mono_medium" />
    
    <!-- DM Mono Medium Italic 500 -->
    <font
        android:fontStyle="italic"
        android:fontWeight="500"
        android:font="@font/dm_mono_medium_italic" />
</font-family>
```

### Step 4: Update Typography

In `app/src/main/java/com/example/async/ui/theme/AsyncTypography.kt`, replace:

```kotlin
val AsyncFontFamily = FontFamily.Monospace
```

With:

```kotlin
val AsyncFontFamily = FontFamily(Font(R.font.dm_mono))
```

And add the import:

```kotlin
import androidx.compose.ui.res.fontResource
```

## Font Weights Available

DM Mono comes in 3 weights:
- **Light (300)**: Used for large displays and headers
- **Regular (400)**: Used for body text and most UI elements
- **Medium (500)**: Used for emphasis and labels

## Typography Hierarchy

The app uses DM Mono consistently across all text elements:

- **Display Large/Medium/Small**: Light weight for large headers
- **Headline Large/Medium/Small**: Regular weight for section headers
- **Title Large/Medium/Small**: Regular to Medium weight for component titles
- **Body Large/Medium/Small**: Regular weight for content text
- **Label Large/Medium/Small**: Medium weight for interactive elements

## Why DM Mono?

- **Consistent Character Width**: Makes text alignment predictable
- **Modern Appearance**: Clean, geometric design
- **Excellent Readability**: Optimized for both display and code
- **Good Unicode Support**: Covers most Latin-based languages
- **Open Source**: SIL Open Font License

## License

DM Mono is licensed under the SIL Open Font License 1.1, which allows free use in both commercial and non-commercial projects.

## Troubleshooting

### Build Errors
- Ensure TTF files are properly named (lowercase, underscores)
- Check that all files are in `app/src/main/res/font/`
- Verify XML syntax in font family file

### Font Not Appearing
- Clean and rebuild project: `./gradlew clean build`
- Check Android Studio's font preview in Resources
- Verify font family is properly referenced in Typography

### Performance Issues
- Font files are cached by Android system
- Consider using only needed weights to reduce APK size
- Monitor memory usage with large font displays 