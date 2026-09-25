# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** complete — iteration 9 Verifier: all criteria hold; Cleanup Commit removes `.harness/run/`.
- **Next Phase:** none — T-001…T-006 complete.
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Human criteria:** all signed (#7–#13).
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest --rerun` |
  lint: `./gradlew :app:lintDebug --rerun-tasks` (plain run may be UP-TO-DATE) | device: `adb devices`,
  `./gradlew :app:installDebug`, `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
