# RESUME BLOCK

> Derived cache; on disagreement with task files or git, this is wrong.

- **Stage:** executing (Phase 3 / T-003+T-004 done, committed)
- **Next Phase:** Phase 4 = T-005 (Iteration-executed removals, no Worker; see TASKS/T-005.md and PROJECT.md removal-order traps). Then Phase 5 = T-006 (sweep/README).
- **Later phases:** 5 = T-006 (Iteration sweep/README). Then DONE-candidate -> Verifier drives AVD `astronex_test`.
- **Device drive pending** for DoD 3-6,8-11,15,17,19,20,22,23 (phone install blocked by on-device confirm; use AVD `astronex_test` at Verifier).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew.bat :app:assembleDebug` | test: `./gradlew.bat :app:testDebugUnitTest` | androidTest compile: `./gradlew.bat :app:assembleDebugAndroidTest` | lint: `./gradlew.bat :app:lintDebug` (baseline fails on MissingTranslation until T-005) | device: `adb` (one phone attached, API 36); PKG `com.example.myapplication`, ACT `com.example.skeleton.MainActivity`
- **Model tiers (from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
- **Run Mode:** Autonomous. Wire shared files (manifest, nav, DI, Gradle, strings, themes.xml, README) yourself; Workers cannot delete or build.
