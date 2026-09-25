# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on any disagreement with the task
> files or with git, this file is the one that is wrong.

- **Stage:** executing — Phase 2 (T-003) complete in iteration 3
- **Next Phase:** Phase 3
  - T-004 (Capable) - scope: `TASKS/T-004.md` § Declared File Scope (`service/MusicPlaybackService.kt`,
    new `service/PlaybackStopPolicy.kt`, `MainActivity.kt`, new
    `ui/fragment/music/component/MusicNotificationPermissionRequest.kt`, `MusicFragment.kt`, new
    `app/src/test/.../service/PlaybackStopPolicyTest.kt`)
  - Worker Write is refused outside `app/src/main/java/` (C-11): the Worker reports the test file's content,
    the Iteration writes it.
  - Iteration wiring: strings (EN + DE); `MainActivity` `launchMode` in the manifest if needed; README
    (`PlaybackStopPolicy`, notification permission, tap-to-open).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` (verified iteration 3; run unpiped, `cd` in its own Bash call; with
  `--quiet` a clean exit prints nothing). Install `./gradlew :app:installDebug` (device `10AECY1ZXG003MQ`).
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
