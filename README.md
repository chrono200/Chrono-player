# CHRONO PLAY

A native Android music player by Chronodisrup. Built with Kotlin, Jetpack Compose and AndroidX Media3, with a configurable Muriel Server backend.

## Project status

**Phase 1 — foundation:** Android application structure, black/electric-orange design system, navigation shell, backend configuration, and Media3 playback service.

## Requirements

- Android Studio with Android SDK 35
- JDK 17
- Gradle 8.11.1 (wrapper will be added with the next build-tooling pass)

## Build

Open this repository in Android Studio and sync Gradle. Set the Muriel API base URL in the app's Settings screen before using online search or streaming. The backend URL must end in `/`.

```bash
./gradlew assembleDebug
```

## Backend

CHRONO PLAY talks to a self-hosted Muriel Server. Configure its base URL in Settings; do not expose an unauthenticated server publicly. The app does not bundle or host music, and users are responsible for complying with source-platform terms and applicable music rights.

## Design

- Background: `#090909`
- Surface: `#141414`
- Accent: `#FF5A36`
- Typography: white with muted grey secondary text

## Roadmap

1. Android foundation and design system
2. Home, search and discovery backed by Muriel
3. Media3 playback, media notification and lock-screen controls
4. Library, playlists, lyrics and device-local offline downloads
5. Artist/album pages, recommendations, listening stats and optimization
