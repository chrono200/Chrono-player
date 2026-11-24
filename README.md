# CHRONO PLAY

A native Android music player by Chronodisrup. Built with Kotlin, Jetpack Compose, AndroidX Media3 and an embedded Android yt-dlp engine. No server URL, token, account or manual backend setup is required.

## Current implementation

- Native dark interface with electric-orange branding.
- Search through Muriel's YouTube, YouTube Music and SoundCloud providers.
- Trending feed from Muriel.
- Muriel-backed stream resolution and Android background playback service.
- Media notification / lock-screen session foundation through Media3.
- Server-cache library view.
- Direct on-device audio extraction and MP3 downloads to the public Music/CHRONO PLAY folder using Android MediaStore.
- Lyrics lookup through Muriel.
- Configurable server URL and access token saved with Android DataStore.
- Handles Muriel's loopback media URLs by routing them back through the configured server host.

## Requirements

- Android Studio with Android SDK 35
- JDK 17
- Gradle 8.11.1

Open the repository in Android Studio and sync Gradle. The repository currently does not include the Gradle wrapper, so build with Android Studio or an installed Gradle 8.11.1:

```bash
gradle assembleDebug
```

## Standalone operation\n\nCHRONO PLAY runs its extractor locally on the Android device. The downloader runtime is bundled in the APK and initializes automatically when the user first searches, plays or downloads. Downloads are written to the shared Music/CHRONO PLAY folder and indexed through Android MediaStore. The app does not need a running Muriel server.\n\nThe Muriel Server repository remains the reference backend for future optional server-powered features (artist metadata, richer recommendations, synchronized cache and lyrics), but none of those features are required for basic search, playback or downloading.\n\n## Design

- Background: `#090909`
- Surface: `#141414`
- Accent: `#FF5A36`
- Typography: white with muted grey secondary text

## Build verification

The Android CI workflow builds the debug APK and publishes it as a downloadable artifact. The embedded downloader increases APK size and currently targets ARMv7 and ARM64 Android devices.
