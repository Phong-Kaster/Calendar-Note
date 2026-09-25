# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** awaiting human verification — DONE-candidate kept; T-001…T-005 complete. Next Iteration is the Verifier (§11).
- **Next Phase:** none
- **Queued decisions:** 1 — D-002 (Human Verification Request, DoD #7–#13); blocks completion only (no tasks remain)
- **Abandoned:** none
- **Unreachable:** none
- **Machine re-proved iteration 4:** #1–#5. **#6 pending:** `adb devices` was empty — re-prove on the phone before `DONE`.
- **Human criteria unsigned:** DoD #7–#13 (D-002).
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest --rerun` |
  lint: `./gradlew :app:lintDebug --rerun-tasks` (plain run may be UP-TO-DATE) | device: `adb devices`,
  `./gradlew :app:installDebug`, `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
