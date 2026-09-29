# RESUME BLOCK

> Derived cache; on disagreement with task files or git, this is wrong.

- **Stage:** DONE-candidate (all tasks complete, Phase 5 committed)
- **Next:** this is the Verifier invocation: re-prove every machine criterion, drive machine-then-human on AVD `astronex_test`, then Cleanup Commit / DONE or DONE_PARTIAL.
- **Device drive pending** for DoD 3-6,8-11,15,17,19,20,22,23 (phone install blocked by on-device confirm; use AVD `astronex_test` at Verifier).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew.bat :app:assembleDebug` | test: `./gradlew.bat :app:testDebugUnitTest` | androidTest compile: `./gradlew.bat :app:assembleDebugAndroidTest` | lint: `./gradlew.bat :app:lintDebug` | device: `adb` (one phone attached, API 36); PKG `com.example.myapplication`, ACT `com.example.skeleton.MainActivity`
- **Model tiers (from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
- **Run Mode:** Autonomous. Wire shared files (manifest, nav, DI, Gradle, strings, themes.xml, README) yourself; Workers cannot delete or build.
