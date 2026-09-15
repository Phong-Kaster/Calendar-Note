# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** verified — awaiting human sign-off. Every `machine` criterion is proved; the run is blocked on
  six pairs of eyes and nothing else.
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none — the task graph is empty. T-001, T-002 and T-003 are all complete.
- **DONE-candidate:** yes, and **now certified for its `machine` half** by iteration 5, which wrote none of
  this implementation and re-measured all fourteen criteria from scratch rather than reading iteration 4's
  table. The flag stands; nothing was found wrong, so there was nothing to clear.
- **What the next invocation does:** if `DECISIONS.md` carries a `## D-003` heading, consume it (§6.2). Any
  item answered **pass** is signed below; any **fail** is an ordinary discovery — file a task, clear the
  DONE-candidate, keep going. If every one of 15-20 comes back pass, that invocation creates the Cleanup
  Commit (remove `.harness/run/`, keep `.harness/ISSUES.md`) and reports `DONE`. If some are still
  unanswered, re-raise **only those**, never the ones already signed.

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | commit `e7798dd`; iteration 3 re-proved it |
| T-002 | complete | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt` | commit `e7798dd`; iteration 3 re-proved it |
| T-003 | complete | `domain/greeting/GreetOnceADay.kt` (new), `test/.../GreetOnceADayTest.kt` (new), `data/notification/GreetingNotifier.kt`, `ui/fragment/home/HomeFragment.kt` | this checkpoint; full evidence in `TASKS/T-003.md` |

## Machine verification (iteration 5, the Verifier — measured by this invocation, not copied)

One invocation of
`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest --rerun-tasks`
→ `BUILD SUCCESSFUL in 1m 8s`, **`61 actionable tasks: 61 executed`**. Not one `UP-TO-DATE` marker: that,
and not the `BUILD SUCCESSFUL` line, is what makes this evidence this invocation's own.

| # | Criterion, in short | How it was proved here | Holds |
|---|---|---|---|
| 1 | plain-Kotlin decision behind an injected `Clock` | `greetingDueOn(lastGreetedDate, clock)` in `domain/greeting/GreetingDecision.kt`; `GreetingDecisionTest` — never-greeted greets, twice on one day greets once, a new day greets again, a stored day reads back | ✓ |
| 2 | calendar day, not a rolling 24 hours | `GreetingDecisionTest`: *"four minutes across midnight is two different days and greets twice"* **and** *"a nearly twenty-four hour gap inside one day is still one day"* — both directions, which is what makes it a calendar comparison | ✓ |
| 3 | triggered from `MainActivity.onStart()` | `MainActivity.kt:58-64` — `override fun onStart()` → `greetingNotifier.greetIfFirstForegroundToday()` | ✓ |
| 4 | the sequence is serialized | `GreetingNotifier.kt:80,96` — `greetingMutex.withLock { greetOnceToday() }`; the lock wraps read→decide→post→write entire, not just the write | ✓ |
| 5 | persisted, never only in memory | `SettingDatastore.kt:37` `stringPreferencesKey("lastGreetedDateKey")`, read back through `lastGreetedDateFlow.first()` on every call; no field caches it | ✓ |
| 6 | greeting text from `strings.xml`, both locales | `R.string.hello_what_will_you_write_today` in `values/` **and** `values-de/`; no quoted greeting literal in the notifier | ✓ |
| 7 | own channel, id `greeting`, `IMPORTANCE_DEFAULT`, created at launch | `GreetingNotifier` constants + `GreetingNotifierConstantsTest` (6 green, incl. *"the greeting has a channel of its own and not the alarms one"*); `MainApplication.onCreate` creates it through Koin's instance beside the alarms one | ✓ |
| 8 | `AlarmNotifier` wholly unchanged | absent from the run's diff (`3506b33..HEAD`); `AlarmNotifierConstantsTest.kt` absent too, 6 tests green | ✓ |
| 9 | source manifest | no `ACCESS_COARSE_LOCATION`/`ACCESS_FINE_LOCATION`; all six required present, `VIBRATE` included per D-001 | ✓ |
| 10 | **merged** manifest | `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` — same six, neither location permission. (AGP injects its own `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; not a source declaration) | ✓ |
| 11 | no location symbols survive | **0 hits** for all nine names across `app/src/main/java` | ✓ |
| 12 | no Location row in the sheet | **0** case-insensitive hits for "location" in `HomePermissionBottomSheet.kt` | ✓ |
| 13 | alarms notice untouched and still pinned | no file under `ui/fragment/alarms/` in the run's diff; `AlarmsPermissionNoticeCase`'s `a2b5ee82_0.png` is a live hash in the report and validated | ✓ |
| 14 | one green invocation, tests up, lint clean, 23/23 | 296 tests / 19 classes / 0 failures / 0 errors (> the 251 baseline); lint 0 errors, 75 warnings; 23 rendered, **0 diffs**; 23 live hashes == 23 files on disk, compared name by name — no orphan, no new reference | ✓ |

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

D-002 was answered on 2026-09-15 (see `AMENDMENTS.md`). Six of the seven `human` criteria are unsigned
today, and **iteration 5 queued `D-003` covering exactly those six** — one request, not six.

| Criterion | Signed | Standing |
|---|---|---|
| 15 | ✗ **FAIL**, 2026-09-15 | Cause fixed by T-003. Re-check on the branch the criterion actually names: **app data cleared, permission granted when asked**. The recorded pass that preceded the failure came from a device where the permission was already granted — the other branch, and how the bug shipped. |
| 16 | ☐ unsigned | Passed 2026-09-15, then **unsigned again**: T-003 changed `GreetingNotifier`, and §11 unsigns any item whose implementation changed — the thing that was looked at no longer exists. |
| 17 | ☐ unsigned | Same. Worth not skipping: the only check that separates "remembered on disk" from "remembered in memory". |
| 18 | ☐ not answered | Needs the device's system date moved forward a day on a daily-driver phone. D-003 states that plainly and offers "just open it tomorrow" instead; it is the human's call either way. |
| 19 | ☐ not answered | Needs taps the device refuses to inject (`SecurityException: … INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate). Written up in D-003 as a by-hand item. |
| 20 | ☐ not answered | Same gate as 19. |
| 21 | ✓ 2026-09-15 | **Stands, and is deliberately not in D-003.** It is about the removed location permissions; nothing since has touched the manifest or the permission code. One carve-out the human recorded: the notes list was not scrolled. |
