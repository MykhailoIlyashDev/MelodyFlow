# 🎵 MelodyFlow - Advanced Audio Player

A modern, feature-rich Android audio player built with Kotlin, featuring advanced audio playback capabilities, beautiful Material Design 3 UI, and comprehensive privacy protection.

## ✨ Features

### 🎧 Audio Playback
- **High-quality audio playback** with MediaPlayer
- **Multiple audio formats support** (MP3, WAV, M4A, AAC, OGG, FLAC)
- **Gapless playback** for seamless listening experience
- **Crossfade support** between tracks
- **Audio effects** (Equalizer, Bass Boost, Virtualizer)

### 📱 User Interface
- **Material Design 3** with dynamic color support
- **Dark/Light theme** with automatic switching
- **Customizable themes** and color schemes
- **Smooth animations** and transitions
- **Responsive design** for all screen sizes

### 🎵 Playlist Management
- **Smart playlists** with auto-sorting
- **Queue management** with drag & drop
- **Playlist import/export** (M3U, PLS)
- **Favorites system** with heart ratings
- **Recently played** tracking

### 🔒 Privacy & Security
- **Modern permission system** (Android 13+ compatible)
- **Granular audio access** control
- **Data extraction rules** for privacy protection
- **Secure backup rules** implementation
- **No unnecessary permissions** requested

### ⚙️ Advanced Features
- **Sleep timer** with custom duration
- **Audio visualization** with real-time waveforms
- **Lyrics display** (LRC file support)
- **Album art fetching** from online sources
- **Audio fingerprinting** for track identification

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 34 (Android 14)
- Kotlin 1.9.10+
- Java 17+

### Installation
1. Clone the repository:
```bash
git clone git@github.com:MykhailoIlyashDev/MelodyFlow.git
cd MelodyFlow
```

2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

## 🏗️ Architecture

### Core Components
- **MainActivity**: Main UI and user interactions
- **AudioService**: Background audio playback service
- **AudioManager**: Audio session and focus management
- **PlaylistManager**: Playlist and queue management
- **AudioPlayer**: Core playback engine
- **EqualizerManager**: Audio effects and equalizer

### Design Patterns
- **MVVM Architecture** with ViewModel
- **Repository Pattern** for data management
- **Observer Pattern** for UI updates
- **Service Pattern** for background operations
- **Factory Pattern** for audio format handling

## 📱 Screenshots

*Screenshots will be added here*

## 🔧 Configuration

### Build Variants
- **Debug**: Development build with logging
- **Release**: Production build with optimizations
- **Staging**: Testing build for QA

### ProGuard Rules
- Optimized for release builds
- Preserves essential audio functionality
- Reduces APK size significantly

## 📊 Performance

- **Memory usage**: Optimized for low-end devices
- **Battery efficiency**: Smart background processing
- **Startup time**: Fast app launch (< 2 seconds)
- **APK size**: Optimized with R8 and ProGuard

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **ExoPlayer** team for audio playback inspiration
- **Material Design** team for UI components
- **AndroidX** team for modern Android libraries

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/MykhailoIlyashDev/MelodyFlow/issues)
- **Discussions**: [GitHub Discussions](https://github.com/MykhailoIlyashDev/MelodyFlow/discussions)
- **Email**: support@melodyflow.app

---

Made with ❤️ by [Mykhailo Ilyash](https://github.com/MykhailoIlyashDev)
