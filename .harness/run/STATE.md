# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** bootstrap
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** -  <!-- nothing is executable until the DoD is approved -->
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | pending | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | - |
| T-002 | pending | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt`, `data/notification/AlarmNotifier.kt` (KDoc only) | - |

## Assumptions

- The greeting is written to `SettingDatastore` regardless of whether the notification permission is
  actually granted at post time — "once per calendar day" is read as a calendar fact, not as "once per
  day it successfully reached the user." Minor, reversible if the human disagrees.
- `VIBRATE` removal is implemented exactly as the PRD instructs even though `AlarmNotifier.kt`
  independently sets vibration on the alarms channel; this is a known, PRD-accepted risk (DoD criterion
  23 is the human check for it), not a contradiction to resolve — the PRD explicitly pre-decided this
  removal and asked the run not to stop and ask about it.
- Two location-only string keys (`location`, `allow_location_to_help_you`) become dead once T-002 lands.
  The Iteration will decide at wiring time whether to remove them from both locale files (avoiding a
  lint `MissingTranslation`/`ExtraTranslation` mismatch either way) or leave them; either choice is
  reversible and recorded in `AMENDMENTS.md` when made.

## Human sign-offs

<!-- Populated by the Verifier once every `machine` criterion holds and a Human Verification Request has
     been answered. Format: criterion # - date - result. -->
