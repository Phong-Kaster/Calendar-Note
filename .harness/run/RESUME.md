# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** executing — Phase 3 (D-002 fix). DONE-candidate cleared.
- **Next Phase:** 3 — **T-006** (Capable, attempt 1). Scope (under `app/src/main/java/com/example/skeleton/`):
  `data/albumart/AlbumArtLoader.kt` (new), `data/mapper/AlbumArtUri.kt`, `service/AlbumArtBitmapLoader.kt` (new),
  `service/MusicPlaybackService.kt`, `ui/fragment/nowplaying/component/NowPlayingArtwork.kt`,
  `ui/fragment/nowplaying/NowPlayingFragment.kt`; [I] `app/src/test/.../data/mapper/AlbumArtUriTest.kt`.
  Iteration wires: `README.md` tree (`data/albumart/`).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Human criteria:** signed #8, #10, #12, #13. Unsigned: #7, #11 (failed, T-006), #9 (re-ask; art path changes).
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest --rerun` |
  lint: `./gradlew :app:lintDebug --rerun-tasks` (plain run may be UP-TO-DATE) | device: `adb devices`,
  `./gradlew :app:installDebug`, `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
