# Definition of Done

> Derived from `PRD.md` at bootstrap (2026-09-15). Human-owned after approval: the engine may propose
> changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence, and must declare WHO can verify it (ADR-015).

## Status

- [x] APPROVED — D-001, 2026-09-15, with one change: `android.permission.VIBRATE` is kept (the human's
      reason: `AlarmNotifier.kt` calls `.setVibrate(...)`/`channel.enableVibration(true)`, so the
      permission backs a feature that actually exists — the PRD's removal rationale was incomplete).
      Criterion 9 no longer lists VIBRATE; the old KDoc-correction and VIBRATE-heads-up-check criteria are
      dropped entirely (nothing left to verify once the permission stays). Criteria renumbered below to
      close the gaps. Everything else approved as written.

## Acceptance Criteria

<!-- [machine] a command's output or a named file proves it; the Verifier proves it itself.
     [human]   a person must look at the running software; blocks DONE until signed off. -->

### Greeting notification

1. [machine] A plain-Kotlin decision function (`domain/greeting/GreetingDecision.kt`) decides whether to
   greet, given a stored last-greeted local date and today's local date from an injected `java.time.Clock`.
   Never-greeted → greet. The same calendar day asked twice through a fake store → greet only the first
   time. A new calendar day → greet again.
2. [machine] The comparison is by **calendar day**, not a rolling 24-hour timer: a fixed clock at 23:58 and
   again at 00:02 the next day is two different days and both greet.
3. [machine] `MainActivity.kt` triggers the greeting check from an overridden `onStart()` — chosen over
   `ProcessLifecycleOwner` because `androidx.lifecycle:lifecycle-process` is not a dependency this app has,
   and adding it would violate the PRD's "no new libraries."
4. [machine] The check-and-write sequence is serialized (a `Mutex`) so two rapid calls into it (e.g. a
   configuration recreation re-firing `onStart`) cannot both observe "not yet greeted" and both post.
5. [machine] The "already greeted today" fact is persisted through `SettingDatastore`, never held only in
   a `var`/singleton field.
6. [machine] The greeting's text is a `strings.xml` key present in both `res/values/strings.xml` and
   `res/values-de/strings.xml`, never a hardcoded literal.
7. [machine] The greeting posts on its own notification channel (id `"greeting"`, `IMPORTANCE_DEFAULT`),
   distinct from the alarms channel, created at app launch alongside the existing `AlarmNotifier` channel
   creation in `MainApplication.onCreate` — mirroring the precedent Constraint C-10 protects (a channel's
   importance is frozen the first time it is created).
8. [machine] `AlarmNotifier`'s existing constants, behavior, and KDoc are wholly unchanged by this run;
   `AlarmNotifierConstantsTest` still passes unmodified.

### Permission removal

9. [machine] `app/src/main/AndroidManifest.xml` no longer declares `ACCESS_COARSE_LOCATION` or
   `ACCESS_FINE_LOCATION`, and still declares `android.permission.VIBRATE`, `INTERNET`,
   `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
10. [machine] The **merged** manifest AGP writes under `app/build/intermediates/merged_manifests/debug/`
    (after `:app:assembleDebug`) does not carry the two removed permissions either — a source-file grep
    alone does not prove a manifest-injecting dependency isn't reintroducing one.
11. [machine] No source file under `app/src/main/java` references location-permission checking or
    requesting (`ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `isLocationGranted`, `isLocationEnable`,
    `onGrantLocation`, `onLocationGranted`, `requestLocation`, `hasRequestedLocation`,
    `locationPermissions`) — the dead code is removed, not merely orphaned.
12. [machine] `HomePermissionBottomSheet.kt` no longer has a Location switch/row or an
    `isLocationEnable`/`onGrantLocation` parameter.
13. [machine] `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` is untouched by this run's diff, and
    its pinned screenshot case (`AlarmsPermissionNoticeCase`, reference
    `AlarmsScreenshotTestKt/AlarmsPermissionNoticeCase_*.png`) still validates.

### Regression guard

14. [machine] One invocation of
    `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest` ends in
    `BUILD SUCCESSFUL`; `testDebugUnitTest` reports 0 failures with a test count that has visibly
    increased above the 251 baseline; `lintDebug` reports 0 errors; `validateDebugScreenshotTest` is 23 of
    23 green with no new or orphaned reference files.

### Human verification

16. [human] Fresh install, first open of a calendar day — install the debug APK with app data cleared and
    launch it. Expect a notification with a friendly greeting, legible, with a proper icon (not the
    launcher mipmap).
17. [human] Second open, same day — close the app to the background and bring it back to the foreground
    (or relaunch it) without crossing local midnight. Expect no second notification.
18. [human] Force-stop and relaunch, same day — Settings → App info → Force stop, then relaunch from the
    launcher on the same day. Expect still no notification, proving the fact survived process death, not
    just backgrounding.
19. [human] Next calendar day — advance the device's date by one day (or wait past local midnight) and
    open the app. Expect the greeting appears again with no action from the user.
20. [human] Home's permission sheet no longer mentions location — with notifications off (and, on Android
    12+, exact alarms not permitted), open Home. Expect the sheet shows only the Notification row and, on
    Android 12+, the Exact alarms row — no Location row, no leftover gap — and toggling each row still
    opens the correct system dialog/screen.
21. [human] Alarms screen's own permission notice is unchanged — open the Alarms tab with a permission
    missing, then with it granted. Expect the same banner text, the same fix buttons landing on the same
    system screens, and the banner drawing nothing once both permissions are in order.
22. [human] The app still installs and opens after the permission removal — install the debug APK and tap
    the launcher icon. Expect Home appears with no crash and the notes list renders. (There is no
    emulator/device/Robolectric in this repository, so this cannot be proven by command.)
23. [human] An alarm still produces a heads-up notification with sound after `VIBRATE` is removed — set an
    alarm a minute out on a real device. Expect the heads-up banner and sound still occur; note whether
    the buzz is still felt. This is a known, PRD-accepted risk: `AlarmNotifier.kt` sets vibration
    independently, and losing the permission may silently drop only the buzz, not the heads-up, because a
    sound is also set.

## Constraints

- **K-1** — `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`,
  `RECEIVE_BOOT_COMPLETED` are not this run's to touch, even if one looks dead on inspection — say so and
  leave it (PRD §2).
- **K-2** — No new dependency, no new Gradle module declaration, no edit to `gradle/libs.versions.toml` or
  `app/build.gradle.kts`. No `ProcessLifecycleOwner` / `androidx.lifecycle:lifecycle-process`.
- **K-3** — The once-per-day decision is plain Kotlin behind an injected `Clock` (Constraint C-06 —
  `NotificationManager`/`NotificationChannel`/`PendingIntent`/`Intent` are silent stubs under this
  toolchain). A test whose subject is one of those framework types is not evidence.
- **K-4** — `res/values/strings.xml` and `res/values-de/strings.xml` are Iteration-owned (Constraint
  C-02); no Worker edits either file.
- **K-5** — `AlarmNotifier.CHANNEL_ID` (`"alarms"`) is never reused for the greeting and never renamed
  (Constraint C-10).
- **K-6** — Alarm scheduling, alarm notification behavior, and `AlarmsPermissionNotice` are out of scope:
  neither task's diff may touch `domain/scheduler/`, `data/scheduler/`, `data/receiver/`, or
  `ui/fragment/alarms/`.

## Verification Evidence Required

| Criterion | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1, 2 | machine | `GreetingDecisionTest` in `app/build/test-results/testDebugUnitTest/TEST-*.xml` | n/a |
| 3 | machine | grep `MainActivity.kt` for `override fun onStart` calling the greeting check | n/a |
| 4 | machine | `GreetingNotifier` source shows a `Mutex` guarding read-decide-write | n/a |
| 5 | machine | grep `SettingDatastore.kt` for the new key; grep `app/src/main` for no in-memory holder | n/a |
| 6 | machine | grep the notifier for no quoted greeting literal; grep both `strings.xml` for the new key | n/a |
| 7 | machine | grep `MainApplication.kt`; `GreetingNotifierConstantsTest` (channel id, importance) | n/a |
| 8 | machine | `AlarmNotifierConstantsTest` passes unmodified | n/a |
| 9 | machine | grep source `AndroidManifest.xml` | n/a |
| 10 | machine | grep the merged manifest under `app/build/intermediates/merged_manifests/debug/` | n/a |
| 11 | machine | grep `app/src/main/java` for the listed location symbols — zero hits | n/a |
| 12 | machine | grep `HomePermissionBottomSheet.kt` | n/a |
| 13 | machine | diff names no file under `ui/fragment/alarms/`; `validateDebugScreenshotTest` 23/23 | n/a |
| 14 | machine | `AlarmNotifier.kt`'s `VIBRATION_PATTERN` KDoc text | n/a |
| 15 | machine | one Gradle invocation's console output + `TEST-*.xml` + lint report + screenshot report | n/a |
| 16 | human | fresh install, first open of the day → greeting notification appears | ☐ |
| 17 | human | second open same day → no notification | ☐ |
| 18 | human | force-stop + relaunch same day → no notification | ☐ |
| 19 | human | next calendar day → greeting appears again | ☐ |
| 20 | human | Home permission sheet has no Location row; other rows still work | ☐ |
| 21 | human | Alarms permission notice unchanged | ☐ |
| 22 | human | app installs and opens after permission removal | ☐ |
| 23 | human | an alarm still heads-up with sound after VIBRATE removal | ☐ |
