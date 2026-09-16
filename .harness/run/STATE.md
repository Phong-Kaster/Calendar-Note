# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** **complete.** Every `machine` criterion re-proved by iteration 6 — an invocation that wrote
  none of this implementation — and every `human` criterion signed by a person. Nothing abandoned, nothing
  unreachable, nothing deferred, no decision queued.
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none. The task graph is empty and stays empty.
- **DONE-candidate:** **discharged.** It stood through iterations 5 and 6 and was never cleared, because
  nothing was ever found wrong with it. Iteration 6 consumed `D-003`, re-measured all fourteen `machine`
  criteria from its own fresh Gradle run rather than reading iteration 5's table, signed criteria 15-20,
  created the Cleanup Commit and reported `DONE`.
- **What remains:** merging `loop/calendar-note-app`. That is the human's act and never the engine's.

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`, `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`, `injection/NotificationModule.kt`, `MainActivity.kt` | commit `e7798dd`; re-proved by iterations 3, 5 and 6 |
| T-002 | complete | `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`, `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`, `ui/util/PermissionUtil.kt` | commit `e7798dd`; re-proved by iterations 3, 5 and 6 |
| T-003 | complete | `domain/greeting/GreetOnceADay.kt` (new), `test/.../GreetOnceADayTest.kt` (new), `data/notification/GreetingNotifier.kt`, `ui/fragment/home/HomeFragment.kt` | commit `8b67f40`; full evidence in `TASKS/T-003.md`; re-proved by iterations 5 and 6 |

## Machine verification (iteration 6, the Verifier — measured by this invocation, not copied)

One invocation of
`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest --rerun-tasks`
→ `BUILD SUCCESSFUL in 3m 11s`, **`61 actionable tasks: 61 executed`**. Not one `UP-TO-DATE` marker: that,
and not the `BUILD SUCCESSFUL` line, is what makes this evidence this invocation's own.

`RESUME.md` advised skipping this re-run, on the ground that no commit had touched `app/` since `8b67f40`.
The advice was not taken. §11 says the Verifier runs the build itself; the spec outranks a derived cache;
and this is the invocation that removes `.harness/run/` and reports `DONE`, so it should stand on evidence
it produced rather than evidence it inherited.

| # | Criterion, in short | How it was proved here | Holds |
|---|---|---|---|
| 1 | plain-Kotlin decision behind an injected `Clock` | `domain/greeting/GreetingDecision.kt`; `GreetingDecisionTest` **10/10 green** — *"somebody who has never been greeted is greeted"*, *"asking twice on the same day greets only the first time"*, *"a new calendar day greets again"*, *"a stored day reads back as the same day"* | ✓ |
| 2 | calendar day, not a rolling 24 hours | `GreetingDecisionTest`: *"four minutes across midnight is two different days and greets twice"* **and** *"a nearly twenty-four hour gap inside one day is still one day"* — both directions, which is what makes it a calendar comparison rather than a duration | ✓ |
| 3 | triggered from `MainActivity.onStart()` | `MainActivity.kt:58-62` — `override fun onStart()` → `greetingNotifier.greetIfFirstForegroundToday()` | ✓ |
| 4 | the sequence is serialized | `GreetingNotifier.kt:80` `private val greetingMutex = Mutex()`; `:96-97` `greetingMutex.withLock { greetOnceToday() }` — the lock wraps read→decide→post→write entire, and `greetOnceToday` is private and documented as only ever called from inside it | ✓ |
| 5 | persisted, never only in memory | `SettingDatastore.kt:37` `stringPreferencesKey("lastGreetedDateKey")`, read back through `lastGreetedDateFlow` (`:93-94`) on every call; no field caches it | ✓ |
| 6 | greeting text from `strings.xml`, both locales | `hello_what_will_you_write_today` at `values/strings.xml:113` and `values-de/strings.xml:100`; **0 hits** for a quoted greeting literal in the notifier | ✓ |
| 7 | own channel, id `greeting`, `IMPORTANCE_DEFAULT`, created at launch | `GreetingNotifier.kt:279` `CHANNEL_ID = "greeting"`, `:294` `CHANNEL_IMPORTANCE = IMPORTANCE_DEFAULT`; `MainApplication.kt:49` creates it through Koin's own instance beside the alarms one at `:44`; `GreetingNotifierConstantsTest` **6/6 green** | ✓ |
| 8 | `AlarmNotifier` wholly unchanged | **no file matching `alarm`** in the run's diff (`3506b33..HEAD -- app/**`), so neither `AlarmNotifier.kt` nor `AlarmNotifierConstantsTest.kt` was touched; that test is **6/6 green** unmodified | ✓ |
| 9 | source manifest | `app/src/main/AndroidManifest.xml` declares exactly `ACCESS_NETWORK_STATE`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED` — all six required, `VIBRATE` kept per D-001, neither location permission | ✓ |
| 10 | **merged** manifest | `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` — the same six and no location permission. (AGP injects its own `com.example.myapplication.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; that is not a source declaration) | ✓ |
| 11 | no location symbols survive | **0 hits** across `app/src/main/java` for all nine names (`ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `isLocationGranted`, `isLocationEnable`, `onGrantLocation`, `onLocationGranted`, `requestLocation`, `hasRequestedLocation`, `locationPermissions`) | ✓ |
| 12 | no Location row in the sheet | **0** case-insensitive hits for "location" in `HomePermissionBottomSheet.kt` | ✓ |
| 13 | alarms notice untouched and still pinned | no file under `ui/fragment/alarms/` in the run's diff; `AlarmsPermissionNoticeCase_Alarms - permission notice_a2b5ee82_0.png` present on disk and live in this run's own screenshot report, validated with no diff | ✓ |
| 14 | one green invocation, tests up, lint clean, 23/23 | **296 tests / 19 classes / 0 failures / 0 errors / 0 skipped** (above the 251 baseline); lint **0 errors**, 75 warnings; **23 rendered, 0 diffs**, and 23 reference PNGs on disk against 23 rendered — no orphan and no new reference file, confirmed by a clean `git status` | ✓ |

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

**All seven signed. None outstanding.** D-002 (2026-09-15) signed 21 and failed 15; D-003 (2026-09-16)
signed 15-20 in one answer: *"tớ đồng ý với các tiêu chí bên trên, tớ đã check rồi"* — I agree with the
criteria above, I have checked them.

| Criterion | Signed | Standing |
|---|---|---|
| 15 | ✓ 2026-09-16 (D-003) | The one that **failed** on 2026-09-15. Cause fixed by T-003 (`8b67f40`) and re-checked on the branch the criterion actually names — app data cleared, permission granted *when asked*. The earlier recorded pass had come from a device where the permission was already granted; that is the other branch through the code, and testing it is how the bug shipped |
| 16 | ✓ 2026-09-16 (D-003) | Passed 2026-09-15, unsigned again when T-003 changed `GreetingNotifier`, re-signed here against the code that now exists |
| 17 | ✓ 2026-09-16 (D-003) | Same history. This is the one check that separates "remembered on disk" from "remembered in memory" |
| 18 | ✓ 2026-09-16 (D-003) | Unanswered in D-002 — it needs the device's system date moved forward on a daily-driver phone, and nobody made that change for the human |
| 19 | ✓ 2026-09-16 (D-003) | Unanswered in D-002 (the device refuses injected input — `SecurityException: … INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate). **Three clauses, one signature:** the no-Location-row clause was machine-verified on a clean AOSP API 36 emulator; "no leftover gap" and "each row opens the correct system screen" were not, and rest on the human's eyes. Their note, kept so nobody reads "19 pass" as three proved clauses |
| 20 | ✓ 2026-09-16 (D-003) | Same gate as 19. Belt-and-braces rather than an expected problem: no file under `ui/fragment/alarms/` is in this run's diff and that screen's pinned reference validated green |
| 21 | ✓ 2026-09-15 (D-002) | **Stands, and was deliberately kept out of D-003** — it is about the removed location permissions and nothing since touched the manifest or the permission code. One carve-out the human recorded: the notes list was not scrolled |

**What would unsign any of these.** A signature covers the build it was given, not the file forever. Change
`GreetingNotifier`, `MainActivity.onStart`, `SettingDatastore`'s greeting key or the Home permission sheet,
and the criteria that look at them are unsigned again (§11).
