# SimpleAudioPlayer

A feature-rich Android audio player built with Kotlin, designed to provide a modern and intuitive music listening experience.

## Features

### 🎵 Core Playback
- **Audio Playback**: Support for multiple audio formats (MP3, WAV, OGG, M4A, AAC, FLAC, WMA, AIFF, OPUS)
- **Playlist Management**: Create, edit, and manage custom playlists
- **Background Playback**: Continue listening while using other apps
- **Audio Focus**: Intelligent audio focus management for seamless playback

### 🎛️ Advanced Controls
- **Volume Control**: Precise volume adjustment with percentage display
- **Balance Control**: Left/right channel balance adjustment
- **Seek Control**: Smooth track progress seeking
- **Playback Modes**: Repeat track, repeat playlist, and shuffle modes

### 🎨 Audio Effects
- **Equalizer**: 10-band equalizer with presets (Classical, Dance, Folk, Jazz, Pop, Rock, etc.)
- **Bass Boost**: Adjustable bass enhancement
- **Virtualizer**: 3D audio virtualization
- **Reverb**: Multiple reverb presets for different acoustic environments

### 📱 User Interface
- **Material Design 3**: Modern, intuitive interface following Google's design guidelines
- **Dark/Light Theme**: Automatic theme switching based on system preferences
- **Responsive Layout**: Optimized for various screen sizes and orientations
- **Custom Visualizations**: Multiple audio visualization types (bars, waveform, circle, spectrum)

### ⏰ Smart Features
- **Sleep Timer**: Configurable sleep timer with fade-out option
- **Favorites**: Mark and organize your favorite tracks
- **Recently Played**: Automatic tracking of listening history
- **Smart Playlists**: Auto-generated playlists based on various criteria

### 🔧 Settings & Customization
- **Comprehensive Settings**: Extensive customization options for all features
- **Audio Quality**: Configurable bitrate and sample rate settings
- **Performance Modes**: Optimize for battery life or audio quality
- **Import/Export**: M3U playlist support and settings backup

## Technical Architecture

### Core Components

#### AudioPlayer
- Manages MediaPlayer instances
- Handles playback state and controls
- Implements repeat and shuffle logic
- Provides progress updates and callbacks

#### AudioService
- Background service for continuous playback
- Notification controls and media session management
- Audio focus handling and wake lock management
- Foreground service for Android 8+ compatibility

#### FileManager
- Audio file scanning and discovery
- Permission management for different Android versions
- File system navigation and filtering
- Metadata extraction support

#### PlaylistManager
- Playlist creation and management
- Smart playlist generation
- Favorites and recently played tracking
- M3U import/export functionality

#### AudioEffectsManager
- Equalizer, bass boost, virtualizer, and reverb
- Preset management and customization
- Real-time audio effect processing
- Effect state persistence

#### AudioVisualizationManager
- Real-time audio visualization
- Multiple visualization types and color schemes
- Custom drawing and rendering
- Performance-optimized updates

#### SleepTimerManager
- Configurable countdown timer
- Fade-out functionality
- Timer state management
- Preset duration options

#### SettingsManager
- Comprehensive app configuration
- Settings persistence and backup
- Theme and language management
- Performance and audio quality settings

### Data Management

#### Storage
- **SharedPreferences**: App settings and user preferences
- **Gson**: JSON serialization for complex data structures
- **File System**: Direct audio file access and management

#### Caching
- **Metadata Cache**: Efficient audio metadata storage
- **Playlist Cache**: Fast playlist access and updates
- **Settings Cache**: Quick configuration retrieval

### Permissions

#### Android 13+ (API 33+)
- `READ_MEDIA_AUDIO`: Access to audio files
- `POST_NOTIFICATIONS`: Display playback notifications

#### Android 12 and below
- `READ_EXTERNAL_STORAGE`: Access to external storage
- `WRITE_EXTERNAL_STORAGE`: Playlist and settings backup

#### Background Services
- `FOREGROUND_SERVICE`: Background playback
- `WAKE_LOCK`: Prevent device sleep during playback

## Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (Android 7.0) or higher
- Kotlin 1.9.10 or later
- Gradle 8.4 or later

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/MykhailoIlyashDev/MelodyFlow.git
   cd MelodyFlow
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open the project folder
   - Wait for Gradle sync to complete

3. **Configure SDK**
   - Ensure Android SDK 24+ is installed
   - Set `ANDROID_HOME` environment variable
   - Accept SDK licenses: `sdkmanager --licenses`

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

### APK Generation

Generate a release APK:
```bash
./gradlew assembleRelease
```

The APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

## Usage

### Basic Playback

1. **Add Music Files**
   - Tap the floating action button (+)
   - Grant storage permissions when prompted
   - Select audio files or folders to scan

2. **Play Music**
   - Tap any track in the playlist to start playback
   - Use play/pause button to control playback
   - Navigate between tracks with previous/next buttons

3. **Control Playback**
   - Drag the progress slider to seek within tracks
   - Adjust volume with the volume slider
   - Set left/right balance with the balance slider

### Advanced Features

#### Equalizer
1. Tap the "Equalizer" button
2. Adjust frequency bands as desired
3. Choose from preset configurations
4. Apply settings to current playback

#### Sleep Timer
1. Tap the "Sleep Timer" button
2. Select preset duration (15, 30, 45, 60, 90, 120 minutes)
3. Or set custom duration
4. Timer will automatically stop playback

#### Playlist Management
1. Create new playlists via the "Playlists" button
2. Add tracks to playlists using the "More" button on tracks
3. Organize favorites and recently played
4. Import/export M3U playlists

#### Visualizations
1. Enable visualizations in settings
2. Choose visualization type (bars, waveform, circle, spectrum)
3. Select color scheme (Classic, Neon, Ocean, Fire, Forest)
4. Adjust sensitivity and smoothness

## Configuration

### Audio Settings

#### Quality
- **Sample Rate**: 44.1kHz, 48kHz, 96kHz
- **Bitrate**: 128kbps, 192kbps, 256kbps, 320kbps
- **Buffer Size**: Configurable for performance vs. latency

#### Effects
- **Equalizer**: 10-band with ±15dB range
- **Bass Boost**: 0-1000% enhancement
- **Virtualizer**: 0-100% 3D effect
- **Reverb**: 8 preset configurations

### Performance Settings

#### Modes
- **Battery Saver**: Optimized for long battery life
- **Balanced**: Good performance and battery balance
- **High Performance**: Maximum audio quality and features

#### Caching
- **Metadata Cache**: 50-500MB configurable
- **Playlist Cache**: Automatic cleanup
- **Artwork Cache**: Quality and size settings

### Theme Settings

#### Appearance
- **System Theme**: Follow device theme automatically
- **Light Theme**: Always use light appearance
- **Dark Theme**: Always use dark appearance
- **Custom Colors**: Personalized accent colors

## Troubleshooting

### Common Issues

#### Permission Denied
- Ensure storage permissions are granted
- For Android 13+, check `READ_MEDIA_AUDIO` permission
- Restart app after granting permissions

#### Audio Not Playing
- Check device volume and media volume
- Verify audio file format support
- Ensure audio focus is not held by other apps

#### Equalizer Not Working
- Verify audio effects are enabled in settings
- Check device audio effect support
- Restart playback after enabling effects

#### High Battery Usage
- Reduce visualization update frequency
- Disable unnecessary audio effects
- Use battery saver performance mode

### Performance Optimization

#### For Low-End Devices
- Disable visualizations
- Use lower audio quality settings
- Enable battery saver mode
- Reduce cache sizes

#### For High-End Devices
- Enable high-quality audio
- Use maximum visualization settings
- Enable all audio effects
- Increase cache sizes

## Development

### Project Structure

```
SimpleAudioPlayer/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/simpleaudioplayer/
│   │   │   ├── AudioPlayer.kt              # Core playback logic
│   │   │   ├── AudioService.kt             # Background service
│   │   │   ├── FileManager.kt              # File operations
│   │   │   ├── PlaylistManager.kt          # Playlist management
│   │   │   ├── AudioEffectsManager.kt      # Audio effects
│   │   │   ├── AudioVisualizationManager.kt # Visualizations
│   │   │   ├── SleepTimerManager.kt        # Sleep timer
│   │   │   ├── SettingsManager.kt          # App settings
│   │   │   ├── AudioMetadataManager.kt     # Metadata extraction
│   │   │   ├── DialogManager.kt            # UI dialogs
│   │   │   ├── PlayerUI.kt                 # UI management
│   │   │   ├── MainActivity.kt             # Main activity
│   │   │   ├── PlaylistAdapter.kt          # Playlist adapter
│   │   │   └── AudioTrack.kt               # Data model
│   │   ├── res/                            # Resources
│   │   └── AndroidManifest.xml             # App manifest
│   └── build.gradle                        # App build configuration
├── build.gradle                            # Project build configuration
├── gradle.properties                       # Gradle properties
└── README.md                               # This file
```

### Key Dependencies

#### Core Libraries
- **AndroidX**: Modern Android support libraries
- **Material Design**: UI components and theming
- **Gson**: JSON serialization and parsing

#### Audio Libraries
- **MediaPlayer**: Native Android audio playback
- **AudioFX**: Audio effects and processing
- **Visualizer**: Audio visualization support

#### UI Libraries
- **RecyclerView**: Efficient list display
- **ConstraintLayout**: Flexible UI layouts
- **Material Components**: Material Design components

### Building from Source

#### Development Setup
1. **Fork the repository**
2. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make changes and commit**
   ```bash
   git add .
   git commit -m "Add your feature description"
   ```
4. **Push and create pull request**

#### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comprehensive comments for complex logic
- Include error handling for all operations

#### Testing
- Test on multiple Android versions (7.0+)
- Verify on different screen sizes
- Test audio file format compatibility
- Validate permission handling

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Areas for Contribution
- **New Audio Formats**: Support for additional audio codecs
- **Enhanced Visualizations**: New visualization types and effects
- **Cloud Integration**: Google Drive, Dropbox, OneDrive support
- **Social Features**: Sharing playlists and recommendations
- **Accessibility**: Screen reader and accessibility improvements
- **Localization**: Additional language support

### Reporting Issues
- Use GitHub Issues for bug reports
- Include device information and Android version
- Provide steps to reproduce the issue
- Attach relevant logs and screenshots

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Android Team**: For the excellent platform and APIs
- **Material Design**: For the beautiful design system
- **Open Source Community**: For inspiration and libraries
- **Beta Testers**: For feedback and bug reports

## Support

### Getting Help
- **Documentation**: Check this README and inline code comments
- **Issues**: Search existing GitHub issues
- **Discussions**: Use GitHub Discussions for questions
- **Email**: Contact the development team

### Community
- **GitHub**: [Project Repository](https://github.com/MykhailoIlyashDev/MelodyFlow)
- **Issues**: [Bug Reports & Feature Requests](https://github.com/MykhailoIlyashDev/MelodyFlow/issues)
- **Discussions**: [Community Discussions](https://github.com/MykhailoIlyashDev/MelodyFlow/discussions)

---

**SimpleAudioPlayer** - Bringing music to life on Android devices.

*Built with ❤️ using Kotlin and Material Design*
