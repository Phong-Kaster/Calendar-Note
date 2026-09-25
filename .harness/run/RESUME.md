# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on any disagreement with the task
> files or with git, this file is the one that is wrong.

- **Stage:** executing — Phase 1 (T-001, T-002) complete in iteration 2
- **Next Phase:** Phase 2
  - T-003 (Capable) - scope: `TASKS/T-003.md` § Declared File Scope (NowPlaying, PlaybackQueuePolicy,
    MusicPlayerRepository(+Impl), MediaItemMapper, `service/MusicPlaybackService.kt`, NowPlayingBar, 4 icons,
    music screen edits, PlaybackQueuePolicyTest)
  - Iteration wiring: Media3 1.8.0 in `libs.versions.toml` + `app/build.gradle.kts`; manifest
    FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, `<service>` (mediaPlayback, MediaSessionService
    action); `RepositoryModule` single MusicPlayerRepository; `ViewModelModule` MusicViewModel gains
    `musicPlayerRepository` (same checkpoint); both strings.xml; README
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` (all verified iteration 2; run `cd` in its own Bash call first; read the
  `BUILD SUCCESSFUL` line). Install `./gradlew :app:installDebug` may be refused by the MIUI on-device prompt.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
