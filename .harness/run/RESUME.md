# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** executing — Phase 1 (T-001, T-002, T-003) complete; DoD approved (D-001)
- **Next Phase:** Phase 2
  - T-004 - scope: `ui/fragment/nowplaying/component/NowPlayingArtwork.kt`
  - T-005 - scope: `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
    `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/{NowPlayingViewModel,
    NowPlayingFragment,NowPlayingUiState}.kt` (+[I] any new test)
  - Iteration wires: both `strings.xml` (if new keys), `README.md` if the tree changes, all [I] files.
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` |
  lint: `./gradlew :app:lintDebug` | device: `./gradlew :app:installDebug`, `adb shell am start -n
  com.example.myapplication/com.example.skeleton.MainActivity` (all verified iteration 2; device `3H164700ALT00000`)
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
