# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** verified (machine) — awaiting human sign-off
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none — Phase 1 was the whole task graph; both tasks complete
- **DONE-candidate:** yes, **held**. Iteration 3 was the Verifier (ENGINE.md §11) and wrote none of the
  implementation. It re-proved all fourteen `machine` criteria against its own fresh evidence — no gaps
  found, so the flag stands. It is held, not converted to `DONE`, because the seven `human` criteria
  (15-21) are unsigned; §11.3 forbids both the Cleanup Commit and a `DONE` report until a person has
  signed them. Queued as **D-002**. The next invocation is the Verifier again: consume D-002 if answered,
  and on an all-pass answer it may create the Cleanup Commit and report `DONE`.

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | this checkpoint commit; build/test/lint/screenshot output below |
| T-002 | complete | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt` | this checkpoint commit; build/test/lint/screenshot output below |

**Evidence (iteration 2, the implementing iteration):** `:app:assembleDebug :app:testDebugUnitTest
:app:lintDebug :app:validateDebugScreenshotTest` → `BUILD SUCCESSFUL`. 288 tests across 18 classes, 0
failures (up from 272 measured at the start of this run — see `.harness/knowledge/PROJECT.md`'s corrected
baseline note). Lint: 0 errors, 75 warnings. Screenshots: 23 of 23 green, no orphans. Fresh-Context Review
(Capable-tier, clean context): **APPROVE**, three non-blocking findings (see Assumptions below and
`.harness/ISSUES.md`).

## Machine verification (iteration 3, the Verifier — independent of the above)

Re-proved by an invocation that wrote none of the implementation. Every one of the fourteen `machine`
criteria was checked explicitly; none was taken on the previous iteration's word.

| Criteria | How it was re-proved this iteration | Result |
|---|---|---|
| 1, 2 | `GreetingDecisionTest` — 10 tests, 0 failures, including the named `four minutes across midnight is two different days and greets twice` and `a nearly twenty-four hour gap inside one day is still one day` cases | pass |
| 3 | `MainActivity.kt:58` — `override fun onStart()` launches `greetingNotifier.greetIfFirstForegroundToday()` on `lifecycleScope`; no `lifecycle-process` dependency added | pass |
| 4 | `GreetingNotifier.kt:72` — `private val greetingMutex = Mutex()`; `greetIfFirstForegroundToday()` wraps the whole read-decide-post-write in `withLock` | pass |
| 5 | `SettingDatastore.kt:37,93-98` — `lastGreetedDateKey` + `lastGreetedDateFlow`/`setLastGreetedDate`; `GreetingNotifier` holds no `var` or field caching the date | pass |
| 6 | `hello_what_will_you_write_today` and `daily_greeting` present in both `values/strings.xml` and `values-de/strings.xml`; the notifier reads both via `getString`, no quoted greeting literal | pass |
| 7 | `CHANNEL_ID = "greeting"`, `CHANNEL_IMPORTANCE = IMPORTANCE_DEFAULT`; `MainApplication.onCreate:49` creates it alongside `AlarmNotifier`'s; `GreetingNotifierConstantsTest` 6/6 | pass |
| 8 | `AlarmNotifier.kt` and `AlarmNotifierConstantsTest.kt` absent from `git diff 3506b33 HEAD`; that test 6/6 green | pass |
| 9 | Source manifest: no `ACCESS_COARSE_LOCATION`/`ACCESS_FINE_LOCATION`; all six required permissions present incl. `VIBRATE` (kept per D-001) | pass |
| 10 | Merged manifest `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` — neither location permission; no dependency reintroduces one | pass |
| 11 | All nine listed location symbols grepped across `app/src/main/java` — zero hits | pass |
| 12 | `HomePermissionBottomSheet.kt` — zero case-insensitive `location` hits | pass |
| 13 | Diff names no file under `ui/fragment/alarms/`; `AlarmsPermissionNoticeCase_*.png` validates in the 23/23 run | pass |
| 14 | `BUILD SUCCESSFUL`; **288** tests / 0 failures / 0 errors across 18 classes (above the 251 floor); lint 0 errors, 75 warnings; screenshots 23 of 23, 23 reference files on disk, no orphans | pass |

**On the freshness of that test count.** The first re-run returned `BUILD SUCCESSFUL` with
`1 executed, 60 up-to-date` — Gradle's cache, not a re-execution. `--rerun` re-ran `lintDebug` and
`validateDebugScreenshotTest` but left `testDebugUnitTest` `UP-TO-DATE`. The 288/0 figure above comes from
`./gradlew :app:testDebugUnitTest --rerun-tasks` (`28 actionable tasks: 28 executed`), which genuinely
re-executed them. Recorded in `.harness/knowledge/PROJECT.md` so the next Verifier does not accept a cache
hit as proof.

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

None yet. All seven `human` criteria (15-21) are unsigned; **D-002** (iteration 3) is the Human
Verification Request carrying them as a seven-item checklist, each written to be followed without reading
any code. Answer in `.harness/run/DECISIONS.md` under `## D-002`.

| Criterion | What a person must confirm | Signed |
|---|---|---|
| 15 | Fresh install, first open of the day → greeting appears, legible, proper white icon, quiet (no banner) | ☐ |
| 16 | Second open the same day → no second notification | ☐ |
| 17 | Force-stop then relaunch the same day → still none (proves it survived process death) | ☐ |
| 18 | Next calendar day → the greeting returns unprompted | ☐ |
| 19 | Home's permission sheet has no Location row, no leftover gap; remaining rows still open the right system screens | ☐ |
| 20 | Alarms screen's permission notice unchanged in both states | ☐ |
| 21 | App installs and opens after the permission removal; notes list renders | ☐ |

A signed criterion is **unsigned again** if the implementation behind it later changes — the thing that was
looked at no longer exists (ENGINE.md §11).
