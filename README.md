# Calendar-Note — Music Player

A reusable Android Compose skeleton app that now ships a **local music player**: it lists the songs
already stored on the phone and (in later steps) plays them with a foreground media service.

## Features

- **Music tab (start screen)** — asks for the "Music and audio" permission (Android 13+) or "Files and
  media" (Android 12 and older), then lists every song on the phone with its title and artist.
  - Permission denied → a message and a button to grant it (opens Settings after a permanent denial).
  - No songs → a "No songs found on this phone" message.
  - Granting the permission in Settings shows the list as soon as you come back.
- **Always-dark theme** — one fixed dark colour scheme, whatever the phone's light/dark setting.
- **Home** and **Setting** tabs from the skeleton (posts feed, language picker, notification settings).
- English and German translations.

Planned next: tap-to-play with a now-playing bar (play/pause, next, previous), and media notification
controls backed by an AndroidX Media3 `MediaSessionService`.

## Tech stack

- Kotlin, Jetpack Compose + Material3 (inside Fragments), Navigation Component
- Koin (DI), Room, DataStore, Retrofit
- Accompanist Permissions
- MediaStore (on-device audio)

## Package tree

`app/src/main/java/com/example/skeleton/`

```
skeleton/
├── common/                    # shared types and constants (Outcome, Constant, Language)
├── core/                      # skeleton base classes: CoreActivity, CoreFragment, CoreLayout
│   ├── config/                # AppConfig (base URLs, timeouts)
│   └── extension/             # plain Kotlin extensions (dates, numbers, flows, validation…)
├── data/                      # everything that talks to a data source
│   ├── database/local/        # Room database, DAOs, entities, converters
│   ├── datastore/             # DataStore preference classes
│   ├── mapper/                # toDomain()/toEntity() extensions
│   │   └── SongMapper.kt      # raw MediaStore row → Song (drops broken rows, cleans title/artist)
│   ├── mediastore/            # raw MediaStore column holders (plain Kotlin, unit-testable)
│   │   └── SongRow.kt
│   ├── remote/                # Retrofit APIs, DTOs, safeApiCallFlow
│   └── repository/impl/       # repository implementations
│       └── SongRepositoryImpl.kt  # reads on-device songs from MediaStore, fail-soft
├── domain/                    # Android-free business layer
│   ├── enums/                 # BottomBarDestination (Music, Home, Setting tabs)
│   ├── model/                 # domain models
│   │   └── Song.kt            # one song on the phone
│   └── repository/            # repository interfaces
│       └── SongRepository.kt  # "give me the songs on this phone"
├── injection/                 # Koin modules (repositories, view models, network, database…)
├── ui/                        # screens, shared components, theme
│   ├── component/             # shared widgets (CoreTopBar, CoreBottomBar, rate sheet)
│   ├── fragment/              # one folder per screen
│   │   ├── home/              # Home tab
│   │   ├── music/             # Music tab: permission flow + song list
│   │   │   ├── component/     # SongItem, MusicPermissionRequest, MusicEmptyState
│   │   │   ├── model/         # AudioPermission (which permission per API level), MusicScreenContent
│   │   │   ├── MusicFragment.kt
│   │   │   ├── MusicUiState.kt
│   │   │   └── MusicViewModel.kt
│   │   └── setting/           # Setting tab and its language sub-screen
│   ├── modifier/              # custom Compose modifiers
│   ├── theme/                 # the fixed dark colour scheme, typography (customizedTextStyle)
│   └── util/                  # UI helpers (NavigationUtil, error mapping, system bars)
├── MainActivity.kt
└── MainApplication.kt
```

## Build

```
./gradlew :app:assembleDebug       # build
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew :app:lintDebug           # lint
```

@author Phong-Kaster
