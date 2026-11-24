# CHRONO PLAY

A native Android music player by Chronodisrup. Built with Kotlin, Jetpack Compose and AndroidX Media3, powered by a self-hosted Muriel Server.

## Current implementation

- Native dark interface with electric-orange branding.
- Search through Muriel's YouTube, YouTube Music and SoundCloud providers.
- Trending feed from Muriel.
- Muriel-backed stream resolution and Android background playback service.
- Media notification / lock-screen session foundation through Media3.
- Server-cache library view.
- Track download requests routed through Muriel, then downloaded to the phone's Music/CHRONO PLAY folder using Android DownloadManager.
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

## Connect Muriel

1. Deploy or run [Muriel Server](https://github.com/zhou9899/muriel-server).
2. Open CHRONO PLAY → Settings.
3. Enter the server base URL, including the port if self-hosted, e.g. `http://192.168.1.10:38472/` or your HTTPS deployment URL.
4. Enter the Muriel token. The server defaults to `hellozhou`; if you configured `MURIEL_TOKEN`, enter that value instead.
5. Save, then search or play.

The app sends the token in the `X-Muriel-Token` request header. Muriel also adds the token to media URLs because Android's audio player and DownloadManager cannot attach the custom header to every media request.

### Important network notes

- For a phone to reach a server on your LAN, Muriel must listen on a network-accessible interface (for example `HOST=0.0.0.0`) and the phone and server must be on the same network.
- Use HTTPS for a server exposed to the internet. The app permits HTTP to support private LAN deployments; do not send the token over an untrusted network.
- Do not expose an unauthenticated or default-token Muriel instance publicly. Set a long random `MURIEL_TOKEN` for any remotely reachable deployment.
- Muriel's `/api/offline-library` is the server's cache, not the phone's offline library. CHRONO PLAY sends completed downloads to the device's Music folder separately.

## Backend integration

The Android client maps the core Muriel API surfaces:

| Feature | Muriel routes |
| --- | --- |
| Search and browse | `/api/search`, `/api/trending`, `/api/hot` |
| Artist and albums | `/api/artist`, `/api/artist-matches`, `/api/artist-albums`, `/api/albums/search`, `/api/albums/tracks` |
| Streaming and cache | `/api/stream-info`, `/api/prefetch`, `/api/check-cache`, `/api/local-track` |
| Downloads | `/api/download`, `/api/downloaded-track` |
| Lyrics | `/api/lyrics`, `/api/lyrics-words`, `/api/save-custom-lyrics` |
| Recommendations | `/api/vibe`, `/api/discover` |
| Local/server library | `/api/library`, `/api/library-add`, `/api/library-update`, `/api/library-remove`, `/api/library-file` |
| Cached music management | `/api/offline-library`, `/api/offline-delete`, `/api/clear-offline-library` |
| Extras | `/api/track-analysis`, `/api/translate`, `/api/tools-status` |

The app does not bundle or host music. Availability, permitted playback, downloads and redistribution remain subject to the source platform's terms and applicable music rights.

## Design

- Background: `#090909`
- Surface: `#141414`
- Accent: `#FF5A36`
- Typography: white with muted grey secondary text

## Build verification

A successful Android build has not yet been run in this environment. The next check is Gradle sync/build in Android Studio or the repository's Android CI workflow.
