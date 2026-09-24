# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on disagreement with task files or
> git, this file is wrong. Keep it small.

- **Stage:** escalated — waiting on D-001 (DoD approval). Once `DECISIONS.md` approves it: mark DoD APPROVED, unblock all tasks, run Phase 1.
- **Run Mode:** Autonomous
- **Next Phase:** Phase 1
  - T-001 (Fast) - scope: `ui/theme/Theme.kt`, `ui/theme/Color.kt`
  - T-002 (Capable) - scope: `domain/model/Song.kt`, `domain/repository/SongRepository.kt`, `data/repository/impl/SongRepositoryImpl.kt`, `data/mapper/SongMapper.kt`, `ui/fragment/music/{AudioPermission,MusicFragment,MusicUiState,MusicViewModel}.kt`, `ui/fragment/music/component/{SongRow,MusicPermissionNotice}.kt`, `res/drawable/ic_bottom_music.xml`, tests `data/mapper/SongMapperTest.kt`, `ui/fragment/music/{AudioPermissionTest,MusicViewModelTest}.kt`
  - Iteration wiring before/after Workers: add `kotlinx-coroutines-test` + `unitTests.isReturnDefaultValues = true`; 4 missing German strings (A-006); manifest audio permissions; nav `musicFragment` start + `toMusic`; `BottomBarDestination.Music` + `CoreBottomBar`; Koin; strings en/de; README.
- **Queued decisions:** 1 — D-001 (DoD approval) blocks T-001, T-002, T-003, T-004
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` | lint: `./gradlew :app:lintDebug` (red on main: 4 MissingTranslation) | device: `~/AppData/Local/Android/Sdk/platform-tools/adb`
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
