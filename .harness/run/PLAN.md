# PLAN

> Machine-owned execution strategy. The human never reviews this file - only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 queued decisions.

## Strategy

Build the player as three observable checkpoints on top of a theme fix: (1) a Music screen that lists the
device's songs, (2) tapping a song plays it through a Media3 `MediaSessionService` with an in-app
now-playing bar, (3) the notification/background behaviour finished off (session activity, swipe-away
stop). Media3's default media notification already provides play/pause/next/previous once the service
exists, so T-003 delivers most of the PRD's notification requirement and T-004 finishes its edges.

Every decision a DoD `machine` criterion depends on is pulled into plain Kotlin (query spec, row mapper,
permission-by-SDK, ViewModel over fake repository/player seams) because JVM tests run against the stub
`android.jar` (PROJECT.md C-04). Device checks are limited to install/launch/dumpsys/logcat (the phone
refuses injected input); playback and notification behaviour are `human` criteria.

Architecture (A-001): `domain/model/Song`, `domain/repository/SongRepository` (+ `data/repository/impl/SongRepositoryImpl`
over `ContentResolver`), `domain/repository/PlayerRepository` (+ impl over a Media3 `MediaController`),
`data/service/MusicPlaybackService : MediaSessionService` hosting ExoPlayer with `REPEAT_MODE_ALL` and a
`ForwardingPlayer` so "previous" always means previous song (A-004).

## Iteration-owned shared files (belong to no task)

`AndroidManifest.xml`, `res/navigation/navigation_graph.xml`, `gradle/libs.versions.toml`,
`app/build.gradle.kts`, `injection/*.kt`, `domain/enums/BottomBarDestination.kt`,
`ui/component/CoreBottomBar.kt`, `ui/util/PermissionUtil.kt`, `res/values/strings.xml`,
`res/values-de/strings.xml`, `README.md`.

## Task Graph

- T-001 - App renders a fixed dark theme that matches the black ground (depends on: -) - scope: `ui/theme/Theme.kt`, `ui/theme/Color.kt` - tier: Fast
- T-002 - Music screen lists the device's songs, with permission and empty states (depends on: -) - scope: see TASKS/T-002.md - tier: Capable
- T-003 - Tapping a song plays it in a foreground media service; now-playing bar controls it (depends on: T-002) - scope: see TASKS/T-003.md - tier: Capable
- T-004 - Notification opens the app and swipe-away stops a paused player (depends on: T-003) - scope: `data/service/MusicPlaybackService.kt` - tier: Capable

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001, T-002 | manifest (audio permissions), nav graph (`musicFragment` start + `toMusic`), `BottomBarDestination.Music`, `CoreBottomBar` left list, `RepositoryModule`, `ViewModelModule`, strings en/de (+ the 4 missing German strings, A-006), `libs.versions.toml`/`build.gradle.kts` (`kotlinx-coroutines-test`, `unitTests.isReturnDefaultValues`), `README.md` |
| 2 | T-003 | manifest (`<service>`, FGS permissions), Media3 deps (+ `kotlinx-coroutines-guava` if needed), `RepositoryModule`, `ViewModelModule`, strings, `README.md` |
| 3 | T-004 | strings, `README.md` |

## Known Risks

- Media3 dependency resolution needs network; version must support compileSdk 36 / AGP 9.
- `ExoPlayer.seekToPrevious()` restarts after ~3 s and the notification uses it — hence the `ForwardingPlayer` in T-003.
- MIUI may kill the process on swipe-from-Recents regardless of code (DoD 13).
- No device input injection: every playback criterion waits on a person.
