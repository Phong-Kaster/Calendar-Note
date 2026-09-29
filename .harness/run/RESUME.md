# RESUME BLOCK

> Derived cache; on disagreement with task files or git, this is wrong.

- **Stage:** executing (Phase 2 / T-002 done, committed)
- **Next Phase:** Phase 3 = T-003 (scope `ui/fragment/library/**`) + T-004 (scope `ui/fragment/nowplaying/**`, see TASKS/T-004.md). Iteration must first wire: nav graph (`nowPlayingFragment` + action `toNowPlaying`), `NowPlayingViewModel` DI (`playerRepository = get()`), strings (`play`, `pause`, ...), drawables `ic_player_play/pause/...` per the task files. `MediaItemMapper.kt` already has the Player<->domain mappers. `PlaybackState.hasNext/hasPrevious` approximate under shuffle.
- **Later phases:** 4 = T-005 (Iteration removals); 5 = T-006 (Iteration sweep/README)
- **Device drive pending** for DoD 3-6,8-11,15,17,19,20,22,23 (phone install blocked by on-device confirm; use AVD `astronex_test` at Verifier).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew.bat :app:assembleDebug` | test: `./gradlew.bat :app:testDebugUnitTest` | androidTest compile: `./gradlew.bat :app:assembleDebugAndroidTest` | lint: `./gradlew.bat :app:lintDebug` (baseline fails on MissingTranslation until T-005) | device: `adb` (one phone attached, API 36); PKG `com.example.myapplication`, ACT `com.example.skeleton.MainActivity`
- **Model tiers (from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
- **Run Mode:** Autonomous. Wire shared files (manifest, nav, DI, Gradle, strings, themes.xml, README) yourself; Workers cannot delete or build.
