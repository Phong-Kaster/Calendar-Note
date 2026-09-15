# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing — Phase 2 complete, awaiting a fresh Verifier
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none — the task graph is empty again. T-001, T-002 and T-003 are all complete.
- **DONE-candidate:** yes, **fresh**, set by this iteration. Iteration 3's candidacy was **cleared** first:
  the human's answer to D-002 failed DoD criterion 15, which is not something a DONE-candidate survives.
  T-003 closed the cause and this iteration re-ran everything green, so the flag is set again — but by an
  invocation that **wrote this implementation**, so it cannot certify it (ENGINE.md §6.11, invariant 8).
  The next invocation is the Verifier: re-prove all fourteen `machine` criteria against its own fresh
  evidence (§11.1), then queue **one** consolidated Human Verification Request for the six `human`
  criteria that are now unsigned, and report `ESCALATE`.

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | commit `e7798dd`; iteration 3 re-proved it |
| T-002 | complete | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt` | commit `e7798dd`; iteration 3 re-proved it |
| T-003 | complete | `domain/greeting/GreetOnceADay.kt` (new), `test/.../GreetOnceADayTest.kt` (new), `data/notification/GreetingNotifier.kt`, `ui/fragment/home/HomeFragment.kt` | this checkpoint; full evidence in `TASKS/T-003.md` |

**Evidence (iteration 4, this checkpoint).** One invocation of
`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest` →
`BUILD SUCCESSFUL in 1m 7s`, `61 actionable tasks: 23 executed, 38 up-to-date`, with **no** `UP-TO-DATE`
marker on any of the four. Tests **296 across 19 classes, 0 failures, 0 errors** (up from 288/18; the 8
new ones are `GreetOnceADayTest`). Lint **0 errors, 75 warnings**. Screenshots **23 of 23**, 23 live
hashes and 23 files on disk, no orphans. Fresh-Context Review (Capable, clean context): **APPROVE**, zero
blocking findings, three non-blocking — two fixed here (`AMENDMENTS.md` A-16), one in `.harness/ISSUES.md`.

## Machine verification (iteration 3 — now partly stale, and deliberately not re-copied)

Iteration 3 re-proved all fourteen `machine` criteria fresh and its per-criterion table was correct for
the tree it saw. **That tree is no longer the tree.** T-003 changed `GreetingNotifier.kt`,
`HomeFragment.kt` and `README.md` and added two files, so criteria 4, 5, 6 and 14 in particular were
re-measured this iteration (all still hold — the mutex still guards the whole sequence, nothing caches
the date on the notifier, the greeting still comes from `R.string`, and the test count went up not down).
The next Verifier re-proves all fourteen itself rather than reading either table. That is the point of
being a Verifier, and the table here is a summary for a human, never an input.

## Assumptions

- **The delivery rule, in full, because it is a judgement the human declined to prescribe.** "Delivered"
  is `NotificationManagerCompat.areNotificationsEnabled()` being true and the post not throwing. A user
  who muted only the *greeting channel* is counted as delivered and the day is recorded, so the app stops
  trying; a user who has not yet been asked for the permission is not, so the next foreground retries.
  Reversible if the human disagrees — it is one `if` in `GreetingNotifier.postGreeting`.
- The superseded assumption from iterations 2-3 — "once per calendar day is a calendar fact, not once per
  day it successfully reached the user" — is **withdrawn.** It is exactly what criterion 15 failed on.
- D-001 (2026-09-15) overrode the PRD's `VIBRATE` removal; `DoD.md` was revised accordingly and T-002's
  scope never touched `AlarmNotifier.kt`. Unchanged.
- Two location-only string keys (`location`, `allow_location_to_help_you`) were removed from both
  `values/strings.xml` and `values-de/strings.xml` when T-002 landed. Unchanged, reversible.

## Human sign-offs

D-002 was answered on 2026-09-15 (see `AMENDMENTS.md`). Four items came back signed; **three of those
four did not survive the iteration**, because T-003 changed the code they were looking at, and ENGINE.md
§11 unsigns any item whose implementation later changed — the thing that was looked at no longer exists.

| Criterion | Signed | Standing after T-003 |
|---|---|---|
| 15 | ✗ **FAIL**, 2026-09-15 | Cause fixed by T-003. Must be re-checked, on the branch the criterion actually names: app data cleared, permission granted when asked. |
| 16 | ✓ 2026-09-15 | **Unsigned again** — `GreetingNotifier` changed. Re-check. |
| 17 | ✓ 2026-09-15 | **Unsigned again** — `GreetingNotifier` changed. Re-check. |
| 18 | ☐ not answered | Needs the device's system date moved forward a day; not done on a daily-driver phone without being asked. |
| 19 | ☐ not answered | Needs taps MIUI's "USB debugging (Security settings)" gate refuses to inject. Needs a person's finger. |
| 20 | ☐ not answered | Same reason as 19. |
| 21 | ✓ 2026-09-15 | **Stands.** It is about the removed location permissions, and nothing in T-003 touched the manifest or the permission code. One carve-out the human recorded: the notes list was not scrolled. |

So six of seven `human` criteria are unsigned. One consolidated Human Verification Request covering
15-20 is the next Verifier's job to queue — not a second one per criterion, and not a re-ask of 21.
