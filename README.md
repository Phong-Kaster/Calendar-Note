# Music Player

An Android app that plays the songs already stored on your phone. Open it, allow access to your music, tap a song, and it plays in the background with a media notification.

## Features

- **Library** – lists on-device songs (title, artist, duration) behind a runtime-permission flow with an "Open settings" fallback.
- **Background playback** – a Media3 `MediaSessionService` keeps music playing with the screen off and shows a media notification with play/pause/next/previous.
- **Mini player** – a bar at the bottom of the library follows the current song and opens Now Playing.
- **Now Playing** – artwork, title, artist, seek bar, play/pause, next/previous, shuffle and repeat (off / all / one).
- Offline only: no network, no accounts, no ads.

## Tech stack

Kotlin, Jetpack Compose + Material3 (inside Fragments), Navigation Component, Koin, AndroidX Media3 (ExoPlayer + MediaSession), MediaStore, accompanist-permissions.

## Package tree

`app/src/main/java/com/example/skeleton/`

```
skeleton/
├── common/                      Shared types used by every layer
│   └── Outcome.kt               Loading / Success / Error result wrapper
├── core/                        Skeleton base classes, reusable across projects
│   ├── extension/               Small helper functions
│   │   └── date_and_time/
│   │       └── DurationExtension.kt   Milliseconds to "m:ss" text
│   ├── CoreActivity.kt          Base activity (edge-to-edge, system bars)
│   ├── CoreFragment.kt          Base fragment that hosts a Compose screen
│   └── CoreLayout.kt            Scaffold wrapper with inset handling
├── data/                        Talks to the outside world (MediaStore, player)
│   ├── mapper/                  Converts data types into domain models
│   │   ├── MediaItemMapper.kt
│   │   └── SongMapper.kt
│   └── repository/
│       └── impl/                Repository implementations
│           ├── MusicRepositoryImpl.kt    Reads songs from MediaStore
│           └── PlayerRepositoryImpl.kt   Controls playback via MediaController
├── domain/                      Pure Kotlin business types, no Android
│   ├── model/                   Plain data classes and enums
│   │   ├── PlaybackState.kt
│   │   ├── RepeatMode.kt
│   │   └── Song.kt
│   └── repository/              Repository interfaces
│       ├── MusicRepository.kt
│       └── PlayerRepository.kt
├── injection/                   Koin dependency-injection modules
│   ├── AppModule.kt
│   ├── RepositoryModule.kt
│   └── ViewModelModule.kt
├── service/                     Android services
│   └── PlaybackService.kt       Media3 session service that owns the player
├── ui/                          Everything the user sees
│   ├── component/               Reusable composables
│   │   ├── CoreTopBar.kt
│   │   └── SongArtwork.kt
│   ├── fragment/                One folder per screen
│   │   ├── library/             Song list screen
│   │   │   ├── component/
│   │   │   │   ├── LibraryEmptyState.kt
│   │   │   │   ├── LibraryPermissionRequired.kt
│   │   │   │   ├── LibraryTopBar.kt
│   │   │   │   ├── MiniPlayerBar.kt
│   │   │   │   └── SongItem.kt
│   │   │   ├── LibraryFragment.kt
│   │   │   ├── LibraryUiState.kt
│   │   │   └── LibraryViewModel.kt
│   │   └── nowplaying/          Full-screen player
│   │       ├── component/
│   │       │   ├── NowPlayingControls.kt
│   │       │   ├── NowPlayingEmptyState.kt
│   │       │   ├── NowPlayingSeekBar.kt
│   │       │   └── NowPlayingSongInfo.kt
│   │       ├── NowPlayingFragment.kt
│   │       ├── NowPlayingUiState.kt
│   │       └── NowPlayingViewModel.kt
│   ├── theme/                   Colours, typography, Material theme (forced dark)
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── util/                    UI-only helpers
│       ├── NavigationUtil.kt
│       ├── PermissionUtil.kt
│       └── SystemBarUtil.kt
├── MainActivity.kt              Single activity hosting the navigation graph
└── MainApplication.kt           Starts Koin
```

## Build

```
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:lintDebug
```

Package id: `com.example.myapplication`.
