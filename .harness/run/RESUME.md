# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** verifying — DONE-candidate recorded (iteration 3); T-001…T-005 complete. Next Iteration is the Verifier (§11).
- **Next Phase:** none
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Human criteria unsigned:** DoD #7–#13 → Verifier queues a Human Verification Request once machine #1–#6 re-prove.
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` |
  lint: `./gradlew :app:lintDebug` | device: `./gradlew :app:installDebug`, `adb shell am start -n
  com.example.myapplication/com.example.skeleton.MainActivity` (install verified iteration 2; re-run `adb devices`)
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
