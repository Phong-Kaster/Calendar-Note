# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads.
> A derived cache, never a source of truth. On any disagreement with the task files or git, this file is
> the one that is wrong: correct it and trust the source.

- **Stage:** bootstrap (blocked on DoD approval)
- **Next Phase:** none selectable yet — both tasks exist but nothing is executable until D-001 is answered
  - T-001 - scope: `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt`
  - T-002 - scope: `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt`, `data/notification/AlarmNotifier.kt` (KDoc only)
- **Queued decisions:** 1 — D-001 (DoD approval), blocks T-001 and T-002 (blocks everything, by design:
  no task exists until the exam it will be graded against is approved)
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  (all standing-granted in `.harness/knowledge/capabilities.json`)
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
