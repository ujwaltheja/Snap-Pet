# Snap Pet 🐾

A modular, offline-first Android virtual pet app with no ads, no monetization, and no external telemetry.

[![Android CI](https://github.com/ujwaltheja/Snap-Pet/actions/workflows/android-ci.yml/badge.svg)](https://github.com/ujwaltheja/Snap-Pet/actions)

## Features

- 🐱 **3 Unique Pets**: Cat, Dog, and Bunny with distinct animations and voice effects
- 🎤 **Voice Talk-Back**: Record your voice and hear it played back with pet-specific pitch/tempo effects
- 🎮 **Interactive Gameplay**: Tap, pet, poke, feed, and play with your virtual pets
- 📊 **Needs System**: Monitor and maintain hunger, happiness, and energy levels
- 🛍️ **Local Shop**: Buy cosmetic items (hats, skins) with soft currency earned through interactions
- 💾 **Offline-First**: All data stored locally using Room database
- 🔒 **Privacy-Focused**: No ads, no subscriptions, no external analytics
- ♿ **Accessible**: Basic accessibility labels for screen readers

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: Multi-module MVVM
- **Database**: Room (with encrypted SharedPreferences fallback)
- **Audio**: Android AudioRecord/AudioTrack + TarsosDSP for pitch effects
- **Animations**: Lottie (with emoji placeholders for MVP)
- **Testing**: JUnit, Mockito, Compose UI Test
- **CI**: GitHub Actions

## Project Structure

```
Snap-Pet/
├── app-ui/                    # Main application module (UI, navigation, screens)
├── core-game/                 # Core game logic (Pet, PetState, PetController)
├── module-pets/               # Pet definitions and assets
├── module-media/              # Audio recording and playback (MediaController)
├── module-store-local/        # Shop and inventory (StoreRepository)
├── module-persistence/        # Room database (entities, DAOs, repository)
└── module-utils/              # Utilities (Logger, TimeUtils)
```

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Minimum SDK 24 (Android 7.0)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ujwaltheja/Snap-Pet.git
   cd Snap-Pet
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**:
   - Android Studio should automatically sync Gradle
   - If not, click "File > Sync Project with Gradle Files"

4. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run tests**:
   ```bash
   ./gradlew test
   ```

6. **Install on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

### Build Outputs

Debug APK will be located at:
```
app-ui/build/outputs/apk/debug/app-ui-debug.apk
```

## Running the App

### On Emulator

1. Create an Android Virtual Device (AVD) in Android Studio
2. Start the emulator
3. Run the app from Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### On Physical Device

1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Run:
   ```bash
   adb devices  # Verify device is connected
   ./gradlew installDebug
   ```

## Testing

### Unit Tests

Run all unit tests:
```bash
./gradlew test
```

Run specific module tests:
```bash
./gradlew :core-game:test
./gradlew :module-store-local:test
```

### Instrumentation Tests

Run Compose UI tests (requires emulator/device):
```bash
./gradlew connectedAndroidTest
```

## Permissions

The app requests the following permissions:

- **RECORD_AUDIO**: For voice recording feature
- **VIBRATE**: For haptic feedback during interactions
- **WRITE_EXTERNAL_STORAGE** (API ≤28): For screenshot export (not required on Android 10+)

All permissions are optional - the app will continue to function without them.

## Architecture

### Module Dependencies

```
app-ui
  ├── core-game
  │   ├── module-persistence
  │   │   └── module-utils
  │   └── module-utils
  ├── module-pets
  │   ├── core-game
  │   └── module-utils
  ├── module-media
  │   └── module-utils
  ├── module-store-local
  │   ├── module-persistence
  │   ├── core-game
  │   └── module-utils
  └── module-persistence
      └── module-utils
```

### Core Components

- **PetController**: Manages pet state, game loop, and interactions
- **MediaController**: Handles audio recording/playback with pitch effects
- **StoreRepository**: Manages shop items and soft currency
- **PersistenceRepository**: Abstracts Room database access

## Game Mechanics

### Needs System

- **Hunger**: Decreases at 0.01/second (~100 seconds to deplete)
- **Happiness**: Decreases at 0.008/second
- **Energy**: Decreases at 0.005/second

Needs are recalculated on app open based on elapsed time since last update.

### Interactions

| Interaction | Effect |
|------------|--------|
| **Feed** | +30 Hunger, +5 Happiness, +5 Coins |
| **Play** | +25 Happiness, -15 Energy, +5 Coins |
| **Pet** | +15 Happiness, +5 Coins |
| **Poke** | -5 Happiness, +5 Coins |
| **Tap** | +15 Happiness, +5 Coins |

### Currency

- Earn 5 coins per interaction
- Starting balance: 100 coins
- Use coins to unlock pets and buy cosmetics

## Third-Party Libraries

| Library | License | Purpose |
|---------|---------|---------|
| [AndroidX Libraries](https://developer.android.com/jetpack) | Apache 2.0 | Core Android components |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | Apache 2.0 | UI framework |
| [Room](https://developer.android.com/training/data-storage/room) | Apache 2.0 | Local database |
| [Lottie](https://github.com/airbnb/lottie-android) | Apache 2.0 | Animations |
| [TarsosDSP](https://github.com/JorenSix/TarsosDSP) | GPL 3.0 | Audio pitch shifting |
| [Coil](https://github.com/coil-kt/coil) | Apache 2.0 | Image loading |
| [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Apache 2.0 | Async operations |

## Known Limitations (MVP)

- **Assets**: Currently using emoji placeholders instead of Lottie animations
- **Audio Quality**: Basic pitch shifting; more sophisticated DSP can be added
- **Migrations**: Database uses `fallbackToDestructiveMigration()` for simplicity
- **Screenshot Export**: Not yet implemented (planned for post-MVP)
- **Pet Unlock**: UI present but unlock logic not fully integrated
- **Animations**: Static emojis instead of animated sprites/Lottie

## Roadmap

### Post-MVP Features

- [ ] Replace emoji placeholders with actual Lottie animations
- [ ] Add screenshot export with share functionality
- [ ] Implement pet unlock purchase in selector screen
- [ ] Add more cosmetic items (accessories, backgrounds)
- [ ] Implement daily rewards and achievements
- [ ] Add pet animations for different states (eating, playing, sleeping)
- [ ] Improve audio effects with better DSP algorithms
- [ ] Add mini-games for earning coins
- [ ] Implement pet evolution system
- [ ] Add more pet species

## Contributing

This is an MVP project built for demonstration. Contributions are welcome!

## License

This project is open-source and available under the MIT License.

## Troubleshooting

### Build Issues

**Gradle sync fails**:
- Ensure JDK 17 is installed and configured
- Clear Gradle cache: `./gradlew clean`
- Invalidate caches: Android Studio > File > Invalidate Caches / Restart

**TarsosDSP dependency issues**:
- The library uses GPL 3.0 license
- For commercial use, consider alternative audio libraries

**Room compilation errors**:
- Ensure KSP plugin is applied
- Clean and rebuild: `./gradlew clean build`

### Runtime Issues

**Audio recording not working**:
- Check if RECORD_AUDIO permission is granted
- Test on physical device (emulator audio can be unreliable)

**Database errors**:
- Clear app data: Settings > Apps > Snap Pet > Storage > Clear Data
- Database will reinitialize with defaults

**Performance issues**:
- Game loop runs at 1Hz (1 update/second) for battery efficiency
- If needed, adjust `UPDATE_INTERVAL_MS` in PetController

## Contact

For issues, questions, or suggestions, please open an issue on GitHub.

---

Built with ❤️ using Jetpack Compose
