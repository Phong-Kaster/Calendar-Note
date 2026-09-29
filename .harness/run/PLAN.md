# PLAN

## Strategy

Behaviour-first checkpoints: (1) app opens to a real library of device songs, (2) tapping plays with a Media3 foreground service + notification, (3) mini player + Now Playing, (4) delete everything the player does not use, (5) README/sweep. The demo code is left in place until T-005 so each earlier checkpoint builds; new code must not depend on it (Constraint). The Iteration owns every shared file (manifest, nav graph, DI, Gradle, strings, themes.xml, README) and all deletions; Workers only write new/edited Kotlin + new drawables inside their scope and report needed strings as `name = "value"`. Verification per checkpoint: `assembleDebug` + `testDebugUnitTest` (+ device drive for T-001/T-002/T-003/T-004 acceptance). Final Verifier drives DoD 4–15 on the attached phone from a fresh install.

## Task Graph

- T-001 - Library screen lists device songs behind the audio-permission flow; dark full-role theme (depends on: -) - scope: `domain/model/Song.kt`, `domain/repository/MusicRepository.kt`, `data/repository/impl/MusicRepositoryImpl.kt`, `data/mapper/SongMapper.kt`, `core/extension/date_and_time/DurationExtension.kt`, `ui/fragment/library/**`, `ui/util/PermissionUtil.kt`, `ui/component/SongArtwork.kt`, `ui/theme/{Theme,Color}.kt`, `core/CoreLayout.kt`, `res/drawable/ic_music_note.xml`, `app/src/test/.../{SongMapperTest,DurationExtensionTest,LibraryUiStateTest}.kt` - tier: Capable
- T-002 - Tap a song → plays; Media3 foreground service, notification, lock-screen/BT controls (depends on: T-001) - scope: `service/PlaybackService.kt`, `domain/model/{PlaybackState,RepeatMode}.kt`, `domain/repository/PlayerRepository.kt`, `data/repository/impl/PlayerRepositoryImpl.kt`, `data/mapper/MediaItemMapper.kt`, `ui/fragment/library/**` (edit), `app/src/test/.../{PlaybackStateTest,RepeatModeTest}.kt` - tier: Capable
- T-003 - Mini player in the library follows the session and opens Now Playing (depends on: T-002) - scope: `ui/fragment/library/**` - tier: Capable
- T-004 - Now Playing screen: artwork placeholder, seek bar, prev/play/next, shuffle, repeat (depends on: T-002; verified together with T-003) - scope: `ui/fragment/nowplaying/**`, `ui/component/CoreTopBar.kt`, `app/src/test/.../NowPlayingUiStateTest.kt` - tier: Capable
- T-005 - Delete every demo feature, network/DB/DataStore/Lottie/Play/TLS stack, permissions, resources, deps (Iteration-executed; deletions need no Worker) (depends on: T-001..T-004) - scope: DoD R1–R4, R7 - tier: n/a (Iteration)
- T-006 - Dead-code sweep, lint clean, app label, real tests kept, README + package tree (Iteration-executed) (depends on: T-005) - scope: `README.md`, `res/values/strings.xml`, R5/R6/R8 - tier: n/a (Iteration)

- T-007 - Remove unreferenced NavigationUtil members (R5 gap found by Verifier, Iteration 7) (depends on: T-006) - scope: `ui/util/NavigationUtil.kt` - tier: Fast

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001 | `CoreActivity.enableEdgeToEdge(SystemBarStyle.dark)`, manifest (`READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE maxSdk 32`), nav graph (`libraryFragment` start dest), `RepositoryModule`, `ViewModelModule`, `strings.xml`, `themes.xml` (`windowLightStatusBar=false`) |
| 2 | T-002 | `libs.versions.toml` + `build.gradle.kts` (media3-exoplayer, media3-session; BEFORE dispatch), manifest (service, FOREGROUND_SERVICE*, `MainActivity launchMode=singleTop`, `tools:node=remove` for merged ACCESS_NETWORK_STATE), `RepositoryModule`, `ViewModelModule`, `strings.xml` |
| 3 | T-003, T-004 | nav graph (`nowPlayingFragment`, action `toNowPlaying`), `ViewModelModule`, `strings.xml`, shared player icons `res/drawable/ic_player_*.xml` (Iteration authors before dispatch) |
| 4 | T-005 | everything (manifest, Gradle, DI, MainApplication, nav, resources) |
| 5 | T-006 | README, strings, lint |
| 6 | T-007 | none (single file) |

## Known Risks

- Media3 version/API drift (AGP 9, Kotlin 2.2): look up current docs via ctx7 on first resolution failure (POLICIES retry rule).
- POST_NOTIFICATIONS: request it (A-001) so the media notification is visible on API 33+.
- T-005 order traps listed in `PROJECT.md` (CoreTopBar4 modifier, MainApplication imports, AppConfig/BuildConfig, Room zero-entity).
- `lintDebug` baseline fails on pre-existing `MissingTranslation`; must be green by T-006.
- Device serial may change; use the single attached device.
