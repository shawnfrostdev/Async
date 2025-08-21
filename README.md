# 🎵 Async Music Player

**A modern, extensible Android music player built with Clean Architecture and Jetpack Compose**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/kotlin-100%25-blue.svg)](https://kotlinlang.org/)

## ✨ Features

- 🏗️ **Multi-module Clean Architecture** - Separation of concerns with App, Core, Domain, Data, Extensions, and Playback modules
- 🎨 **Modern UI** - Built with Jetpack Compose and Material3 design system
- 🔧 **Dynamic Extension System** - Load music sources from external repositories
- 🗄️ **Robust Data Layer** - Room database with comprehensive caching and sync
- 🎵 **Advanced Playback** - ExoPlayer integration with MediaSession support
- ⚡ **Instant Navigation** - Zero-animation transitions for snappy performance
- 📱 **Mini Player** - Persistent playback controls above bottom navigation
- ⚙️ **Extension Management** - Easy UI for adding and managing music sources

## 🛠️ Tech Stack

### **Frontend**
- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material3** - Latest Material Design with dynamic colors
- **Navigation Compose** - Type-safe navigation

### **Architecture**
- **Clean Architecture** - Multi-module separation of concerns
- **MVVM Pattern** - ViewModel with Compose integration
- **Dependency Injection** - Hilt for DI
- **Reactive Programming** - Coroutines & Flow

### **Data & Storage**
- **Room Database** - Local data persistence
- **DataStore** - Preferences management
- **Kotlin Serialization** - JSON serialization

### **Media & Networking**
- **ExoPlayer (Media3)** - Advanced media playback
- **MediaSession** - System media controls integration
- **Ktor Client** - HTTP networking

### **Extension System**
- **DexClassLoader** - Dynamic code loading
- **Custom Security** - Extension validation and sandboxing

## 📁 Project Structure

```
Async/
├── app/                    # Main Android application module
│   ├── ui/                 # Compose UI components and screens
│   ├── navigation/         # Navigation graph and destinations
│   └── theme/              # Material3 theme and typography
├── core/                   # Shared models and extension API
│   ├── model/              # Data models (Track, Artist, Album)
│   ├── extension/          # Extension interface and metadata
│   └── result/             # Type-safe result handling
├── domain/                 # Business logic and repository contracts
│   ├── model/              # Domain-specific models
│   └── repository/         # Repository interfaces
├── data/                   # Data layer implementation
│   ├── database/           # Room database, entities, DAOs
│   ├── repository/         # Repository implementations
│   ├── mapper/             # Entity-Domain model mapping
│   └── sync/               # Data synchronization and caching
├── extensions/             # Extension system implementation
│   ├── loader/             # Dynamic extension loading
│   ├── manager/            # Extension lifecycle management
│   ├── security/           # Security validation
│   └── storage/            # Extension metadata storage
├── playback/               # Audio playback service
│   └── service/            # MediaBrowserService and notification
└── docs/                   # Documentation and API reference
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** or higher
- **Android SDK** with API level 24+ (Android 7.0)
- **Kotlin 1.9.20** or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/shawnfrostdev/Async.git
   cd Async
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Choose "Open an existing Android Studio project"
   - Navigate to the cloned directory and open it

3. **Sync Project**
   - Let Android Studio sync the project and download dependencies
   - This may take a few minutes on first setup

4. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## 📱 Usage

### Adding Extension Sources

1. Open the app and navigate to **Settings**
2. Tap **"Extensions & Sources"**
3. On first launch, you'll see an empty state
4. Tap the **"+"** button or **"Add Your First Repository"**
5. Enter a valid repository URL (e.g., `https://github.com/user/extensions`)
6. Tap **"Done"** to add the source

### Extension Repository Format

Extensions should be hosted in Git repositories with the following structure:
```
extension-repo/
├── manifest.json           # Extension metadata
├── extensions/             # Extension APK/JAR files
└── README.md              # Documentation
```

See [Extension API Documentation](docs/ExtensionAPI.md) for detailed development guidelines.

## 🏗️ Architecture Details

### Clean Architecture Layers

1. **Presentation Layer** (`app/`)
   - Jetpack Compose UI
   - ViewModels and state management
   - Navigation and theme

2. **Domain Layer** (`domain/`)
   - Business logic and use cases
   - Repository interfaces
   - Domain models

3. **Data Layer** (`data/`)
   - Repository implementations
   - Room database
   - Data synchronization

4. **Extension Layer** (`extensions/`)
   - Dynamic extension loading
   - Security validation
   - Extension lifecycle management

### Key Components

- **Extension System**: Dynamically loads music sources from external repositories
- **MediaService**: Background playback with notification controls
- **Database**: Comprehensive data storage with Room
- **UI**: Modern Material3 interface with instant navigation

## 🔧 Development

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run linting
./gradlew lint
```

### Code Style

This project follows [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and uses:
- **ktlint** for code formatting
- **detekt** for static code analysis
- **Android lint** for Android-specific checks

## 📖 Documentation

- [Extension API Guide](docs/ExtensionAPI.md) - How to create music source extensions
- [Extension Template](docs/ExtensionTemplate.kt) - Starter template for extensions
- [Development Progress](docs/PROGRESS.md) - Current development status
- [Task List](docs/taskList.md) - Development roadmap

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Material Design** - For the beautiful design system
- **JetBrains** - For the amazing Kotlin language
- **Google** - For Android development tools and libraries
- **ExoPlayer Team** - For the robust media playback solution

## 📞 Support

If you encounter any issues or have questions:
- Open an [issue](https://github.com/shawnfrostdev/Async/issues)
- Check existing [discussions](https://github.com/shawnfrostdev/Async/discussions)

---

**Built with ❤️ using Kotlin and Jetpack Compose** 