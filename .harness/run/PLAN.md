# PLAN

> Machine-owned execution strategy. The human never reviews this file - only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 queued decisions.

## Strategy

Build the player outward from what the user sees, one observable checkpoint at a time:

1. A fixed dark theme so every later colour choice is judged on the ground it actually sits on.
2. A Music tab that lists on-device songs behind the audio permission (denied / empty / list states).
3. Tap-to-play with a now-playing bar, backed by a Media3 `MediaSessionService` (foreground, media
   notification with prev / play-pause / next come with it). Next/previous go through a pure-Kotlin
   `PlaybackQueuePolicy` that a `ForwardingPlayer` in the service delegates to, so the in-app bar and the
   notification run the same tested logic.
4. The notification edges: the notification permission prompt, tapping the notification opens Music, and
   swipe-away stops a paused player (pure `PlaybackStopPolicy`, called from `onTaskRemoved`).

Architecture: `SongRepository` (MediaStore, Recipe F) and `MusicPlayerRepository` (Koin `single` owning a
reference-counted `MediaController`, main thread only — Constraints C-07/C-08). `MusicPlaybackService` lives in
a new top-level `service/` package. Testable decisions are plain Kotlin taking their inputs as parameters (C-04).

Verification: build + unit tests + lint once per Iteration on the combined tree; device checks only by
`dumpsys`/`logcat` (the device refuses injected input), everything tactile is a `human` DoD criterion.

## Task Graph

- T-001 - The app is always dark (depends on: -) - scope: `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `core/CoreLayout.kt`, `core/CoreActivity.kt`, `res/values/themes.xml` - tier: Fast
- T-002 - Music tab lists the songs on the phone (depends on: -) - scope: see `TASKS/T-002.md` (new files under `domain/`, `data/`, `ui/fragment/music/`, tests) - tier: Capable
- T-003 - Tap a song to play it; now-playing bar with play/pause/next/previous; foreground media service (depends on: T-002) - scope: see `TASKS/T-003.md` - tier: Capable
- T-004 - Notification permission, tap notification opens Music, swipe-away stops a paused player (depends on: T-003) - scope: see `TASKS/T-004.md` - tier: Capable

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001, T-002 | `AndroidManifest.xml` (READ_MEDIA_AUDIO, READ_EXTERNAL_STORAGE max 32); `navigation_graph.xml` (`musicFragment` as start destination, `toMusic` action; re-point `toSetting`'s `popUpTo` to the new root); `BottomBarDestination.kt` + `CoreBottomBar.kt` (Music left of Home); `RepositoryModule.kt`, `ViewModelModule.kt`; both `strings.xml` (new keys + the 4 missing German strings that fail lint on main); `README.md` |
| 2 | T-003 | `libs.versions.toml` + `app/build.gradle.kts` (Media3 1.8.0); `AndroidManifest.xml` (FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, `<service>`); `RepositoryModule.kt`, `ViewModelModule.kt` (MusicViewModel gains `musicPlayerRepository` — same checkpoint); both `strings.xml`; `README.md` |
| 3 | T-004 | both `strings.xml`; `AndroidManifest.xml` (`MainActivity` `launchMode="singleTop"` if the Worker's approach needs it); `README.md` |

Phases 2 and 3 hold one task each: T-003 edits T-002's screen files, T-004 edits T-003's service.

## Known Risks

- Media3 API cannot be inspected by a Worker (zipped `.aar`); only the build finds signature mistakes.
- `MediaController` is asynchronous and main-thread only; commands before connection must be dropped
  fail-soft or queued (C-07, C-08).
- A process-lifetime controller keeps the service bound; the swipe-away stop must use
  `pauseAllPlayersAndStopSelf()` (C-09) and the repository must release on last ViewModel clear.
- Media3's default `seekToPrevious()` restarts a song after ~3 s; the `ForwardingPlayer` must override it or
  DoD #12/#14 fail while tests pass.
- Build/test/lint are not yet granted; the DoD gate proposes them.
- The notification may not show on the device without runtime `POST_NOTIFICATIONS` (seen on `loop/music-player`).
