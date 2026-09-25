# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** verifying — **DONE-candidate recorded** (iteration 7); machine #1–#6 re-proved iteration 8. Next
  invocation is the Verifier (§11): consume D-003, then Cleanup Commit if all signed.
- **Next Phase:** none — T-001…T-006 complete.
- **Queued decisions:** 1 — D-003 (Human Verification Request #7, #9, #11); blocks completion only, no tasks
- **Abandoned:** none
- **Unreachable:** none
- **Human criteria:** signed #8, #10, #12, #13. Unsigned: #7, #9, #11 — asked in D-003 (with a song that has embedded
  art, or acceptance of the placeholder).
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest --rerun` |
  lint: `./gradlew :app:lintDebug --rerun-tasks` (plain run may be UP-TO-DATE) | device: `adb devices`,
  `./gradlew :app:installDebug`, `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
