# Music Player (Android Compose skeleton)

A simple music player built on a reusable Android skeleton. It plays the songs that are **already on the
phone** — no internet, no streaming.

## Features

- **Music library** — the first screen lists every song on the device (title, artist, duration), sorted by title.
- **Audio permission** — asks for `READ_MEDIA_AUDIO` (Android 13+) or `READ_EXTERNAL_STORAGE` (Android 12 and
  lower). If the permission is blocked, the button opens the app's settings page instead. Coming back from
  settings loads the list without a restart.
- **Clear states** — loading, "permission needed", "no songs found" and the song list.
- **Fixed dark theme** — one explicit dark colour scheme, so text is always light on the black background.
- *Planned:* tap-to-play, play/pause, next, previous, and a foreground media service with notification controls.

## Tech stack

- Kotlin, Jetpack Compose + Material 3 (screens hosted in Fragments via `CoreFragment`)
- Navigation Component (single `MainActivity`, `navigation_graph.xml`)
- Koin for dependency injection
- `MediaStore` / `ContentResolver` for the local music library
- Room, DataStore, Ktor (skeleton infrastructure)
- JUnit 4 + `kotlinx-coroutines-test` for JVM unit tests

## Package tree

`app/src/main/java/com/example/skeleton/`

```
skeleton/
├── common/                       // shared types used everywhere: Outcome<T>, Constant, Language
├── core/                         // skeleton base classes: CoreActivity, CoreFragment, CoreLayout, AppConfig
│   ├── config/                   // AppConfig (base URLs, timeouts)
│   └── extension/                // plain Kotlin extensions (collections, dates, numbers, strings, flows)
├── data/                         // where data really comes from (database, datastore, network, device)
│   ├── database/local/           // Room database, DAOs, entities, converters
│   ├── datastore/                // Preferences DataStore wrappers
│   ├── mapper/                   // turn raw rows / DTOs / entities into domain models
│   │   └── SongMapper.kt         // MediaStore query spec + song row → Song (plain Kotlin, unit-tested)
│   ├── remote/                   // Ktor APIs, DTOs, safeApiCallFlow
│   └── repository/impl/          // repository implementations
│       └── SongRepositoryImpl.kt // reads the device's music through ContentResolver
├── domain/                       // Android-free business types and contracts
│   ├── enums/                    // BottomBarDestination (Music, Home, Setting tabs)
│   ├── model/                    // domain models
│   │   └── Song.kt               // one song on the device
│   └── repository/               // repository interfaces
│       └── SongRepository.kt     // "give me the songs on this phone"
├── injection/                    // Koin modules (repositories, view models, database, network, ...)
├── ui/                           // everything the user sees
│   ├── component/                // shared widgets: CoreTopBar, CoreBottomBar, bottom sheets
│   ├── fragment/                 // one folder per screen: Fragment + ViewModel + UiState
│   │   ├── home/                 // skeleton demo screen (posts)
│   │   ├── music/                // Music tab: permission, song list
│   │   │   ├── component/        // SongRow, MusicPermissionNotice
│   │   │   ├── AudioPermission.kt
│   │   │   ├── MusicFragment.kt
│   │   │   ├── MusicUiState.kt
│   │   │   └── MusicViewModel.kt
│   │   └── setting/              // settings + language picker
│   ├── modifier/                 // custom Compose modifiers
│   ├── theme/                    // fixed dark colour scheme (Color.kt, Theme.kt), customizedTextStyle (Type.kt)
│   └── util/                     // UI helpers: navigation, permissions, locale, system bars, error mapping
├── MainActivity.kt               // single activity hosting the navigation graph
└── MainApplication.kt            // starts Koin
```

## Build

```
./gradlew :app:assembleDebug        # build the debug APK
./gradlew :app:testDebugUnitTest    # JVM unit tests
./gradlew :app:lintDebug            # Android lint
```
