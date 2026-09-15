# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none — Phase 1 was the whole task graph; all `machine` criteria appear satisfied
- **DONE-candidate:** yes (set this iteration; the next invocation is the Verifier and must re-prove every
  `machine` criterion fresh before trusting this)

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | this checkpoint commit; build/test/lint/screenshot output below |
| T-002 | complete | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt` | this checkpoint commit; build/test/lint/screenshot output below |

**Evidence (one invocation, this iteration):** `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug
:app:validateDebugScreenshotTest` → `BUILD SUCCESSFUL`. 288 tests across 18 classes, 0 failures (up from
272 measured at the start of this run — see `.harness/knowledge/PROJECT.md`'s corrected baseline note).
Lint: 0 errors, 75 warnings. Screenshots: 23 of 23 green, no orphans. Merged manifest
(`app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml`) confirmed to
carry `VIBRATE` and neither location permission. Fresh-Context Review (Capable-tier, clean context):
**APPROVE**, three non-blocking findings (see Assumptions below and `.harness/ISSUES.md`).

## Assumptions

- The greeting is written to `SettingDatastore` regardless of whether the notification permission is
  actually granted at post time — "once per calendar day" is read as a calendar fact, not as "once per
  day it successfully reached the user." Minor, reversible if the human disagrees.
- D-001 (2026-09-15) overrode the PRD's `VIBRATE` removal — the human kept the permission because
  `AlarmNotifier.kt` independently uses it. `DoD.md` was revised accordingly (criterion 9 no longer lists
  `VIBRATE`, the old KDoc-correction and VIBRATE-heads-up-check criteria are dropped, remaining criteria
  renumbered) and T-002's scope no longer touches `AlarmNotifier.kt` at all.
- Two location-only string keys (`location`, `allow_location_to_help_you`) became dead once T-002 landed.
  Decided at wiring time: removed from both `res/values/strings.xml` and `res/values-de/strings.xml` in
  the same checkpoint (kept in sync, no lint mismatch). Reversible if the human disagrees.
- Fresh-Context Review raised three non-blocking findings, none fixed this iteration (recorded in
  `.harness/ISSUES.md` for visibility, not blocking a DONE candidacy since none breaks a DoD criterion):
  `GreetingNotifier`'s default `Clock.systemDefaultZone()` binds the timezone at construction time (a
  device timezone change mid-process could misjudge "today" until the process restarts — self-healing,
  and the same pattern this codebase already uses elsewhere); the new `lastGreetedDateFlow` in
  `SettingDatastore.kt` has no `.catch { IOException -> ... }` guard, consistent with every sibling flow
  in that file (a pre-existing repo-wide gap, not a new deviation); and a pre-existing dead private
  function `shouldShowRequestPermissionRationale` in `HomeRequestPermission.kt` was confirmed (via `git
  diff`) to already be an orphan before this run's diff, not something T-002 caused.

## Human sign-offs

<!-- Populated by the Verifier once every `machine` criterion holds and a Human Verification Request has
     been answered. Format: criterion # - date - result. -->
