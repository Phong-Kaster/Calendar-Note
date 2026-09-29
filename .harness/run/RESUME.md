# RESUME BLOCK

> Derived cache; on disagreement with task files or git, this is wrong.

- **Stage:** executing (Phase 4 / T-005 done, committed)
- **Next Phase:** Phase 5 = T-006 (Iteration: R5 dead-code sweep incl. lint UnusedResources, README rewrite + package tree, R6/R8 check). No Worker.
- **Later phases:** none. After T-006: DONE-candidate -> Verifier drives AVD `astronex_test`.
- **Device drive pending** for DoD 3-6,8-11,15,17,19,20,22,23 (phone install blocked by on-device confirm; use AVD `astronex_test` at Verifier).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew.bat :app:assembleDebug` | test: `./gradlew.bat :app:testDebugUnitTest` | androidTest compile: `./gradlew.bat :app:assembleDebugAndroidTest` | lint: `./gradlew.bat :app:lintDebug` | device: `adb` (one phone attached, API 36); PKG `com.example.myapplication`, ACT `com.example.skeleton.MainActivity`
- **Model tiers (from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
- **Run Mode:** Autonomous. Wire shared files (manifest, nav, DI, Gradle, strings, themes.xml, README) yourself; Workers cannot delete or build.
