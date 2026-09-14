# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

### D-007 — `AlarmsPermissionNoticeCase` needs a first `@PreviewTest` reference image; grant `updateDebugScreenshotTest` for it?

- **Type:** Capability grant | **Queued:** iteration 7, 2026-09-14
- **Blocks:** A-006 (its acceptance explicitly requires screenshots green) and, transitively, A-007 (depends
  on A-005 **and** A-006 both landing). A-005 has no screenshot dependency and is complete regardless.
- **Question:** Grant a single-use, goal-scoped `updateDebugScreenshotTest` entry so
  `AlarmsPermissionNoticeCase` (`AlarmsScreenshotTest.kt`) gets its first reference image?
- **Context:** A-006 (the permissions-missing banner) landed this iteration with a Worker-authored
  `AlarmsPermissionNotice` composable and an Iteration-owned `@PreviewTest` case pinning it
  (`widthDp = 360, heightDp = 260`, sized from the Worker's own reported 222dp banner height + 24dp of
  `ScreenshotScaffold` padding, per C-05). `validateDebugScreenshotTest` fails on exactly this one case with
  `ScreenshotImageNotFoundException` — expected: it is a brand-new preview, never recorded before, and this
  is the fourth time this exact situation has occurred in this run (D-003, D-004, D-006). Everything else is
  green: `assembleDebug`, 251 unit tests (0 failures), `lintDebug` (0 errors, 72 warnings — 3 new, all
  pre-existing lint categories already tolerated elsewhere in this codebase: `ModifierParameter`,
  `InlinedApi` ×2, `UseKtx`), and the other 22 of 23 screenshot cases.
- **Options:** (1) grant it, goal-scoped to exactly `*AlarmsPermissionNoticeCase*`, with a before/after
  live-hash report for `AlarmsScreenshotTestKt` restricted to that class; (2) refuse — a human records it
  manually and the run reports `ESCALATE` until the reference appears on disk; (3) drop the `@PreviewTest`
  annotation, keep the composable as a plain `@Preview` — not recommended, listed only because it needs no
  grant, and it is the one DoD-31 criterion (*"the screen says so and offers the fix"*) that a picture is
  the only evidence for.
- **Engine recommendation:** option 1, matching D-003/D-004/D-006's precedent exactly. The case is small
  (one component, two conditions both failing, the tallest the banner ever gets), the render has no
  locale-formatted date or time so it is not host-dependent, and a human looking at one rendered banner
  before approving is the same cheap check the three prior grants already relied on.
- **Decision:** **Option 1 — granted, goal-scoped to exactly one invocation of
  `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsPermissionNoticeCase*"`.** Single-use, not a
  standing permission: run it once to record `AlarmsPermissionNoticeCase`'s reference image, report the
  before/after state of `AlarmsScreenshotTestKt`'s reference directory as specified, and then remove this
  entry from `.harness/run/capabilities.json` (or let it expire with the run) — do not reuse it for any
  other test name or any later re-record of this same case without a fresh Decision.
- **Applied (iteration 8).** Ran exactly `./gradlew :app:updateDebugScreenshotTest --tests
  "*AlarmsPermissionNoticeCase*"` once. Before: `AlarmsScreenshotTestKt`'s reference directory held two
  files (`AlarmDeleteConfirmationCase_...png`, `AlarmsEmptyStateCase_...png`), no `AlarmsPermissionNoticeCase`
  entry. After: a third file,
  `AlarmsPermissionNoticeCase_Alarms - permission notice_a2b5ee82_0.png`, appeared — no other case's file
  changed. Re-verified `assembleDebug` + `testDebugUnitTest` + `lintDebug` + `validateDebugScreenshotTest`
  together: `BUILD SUCCESSFUL`, 251 tests / 0 failures, lint 0 errors / 72 warnings, 23/23 screenshot cases
  green. A-006 marked complete; A-007 becomes selectable.

### D-008 — Human Verification Request: sixteen `human`-class DoD criteria need a person on a real phone

- **Type:** Human Verification (ENGINE.md §11) | **Queued:** iteration 9 (Verifier), 2026-09-14
- **Blocks:** the Cleanup Commit and the `DONE` report. Nothing else — there is no more code to write and no
  task left unreached.
- **Context:** This iteration is the Verifier: it wrote none of this implementation and re-proved every
  `machine` criterion fresh rather than trusting a prior iteration's numbers.
  - `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest` →
    `BUILD SUCCESSFUL`, re-run this iteration.
  - Unit tests: summed fresh from the 15 JVM test-result XML files this iteration read directly — **251
    tests, 0 failures, 0 errors.**
  - Lint: `app/build/reports/lint-results-debug.txt` tail this iteration read directly — **0 errors, 72
    warnings.**
  - Screenshots: `validateDebugScreenshotTest` succeeded against **23** reference PNGs this iteration counted
    directly under `app/src/screenshotTestDebug/reference/`, including all three `AlarmsScreenshotTestKt`
    cases.
  - Criterion 3: `AppDatabase.kt:36` reads `version = 4`; `Migration.kt:83` defines `MIGRATION_3_4`;
    `DatabaseModule.kt:29` passes it to `addMigrations(...)`.
  - Criterion 12: `navigation_graph.xml` declares `alarmsFragment` and `alarmEditorFragment`, a `toAlarmEditor`
    action between them, and `alarmEditorFragment`'s one `<argument name="alarmId">` carries
    `android:defaultValue="-1L"`.
  - Criterion 33: `grep -nE 'Color\.Black|Color\.White|Color\(0x'` over every alarm file → no matches.
  - Criterion 34: `grep -nE 'text = "|contentDescription = "'` over every alarm file → no matches; lint is 0
    errors (above).
  - Criterion 36: `README.md` lists `ui/fragment/alarms/` and `ui/fragment/alarm_editor/` in its package tree
    with purpose notes, and its feature table marks Alarms rows `✅ built`.
  - Every other `machine` criterion (5, 6, 7, 14, 15, 17, 19, 20, 21, 24, 26, 27, 29) is a JVM unit test
    inside the 251-test, 0-failure run above — not spot-checked individually, covered by the full suite
    passing.
  - **All `machine` criteria hold. Nothing is abandoned, nothing is deferred, no other decision is queued.**
    Per `STATE.md` § Human sign-offs, no person has looked at the running app in this run — all sixteen
    `human` criteria are unsigned.
- **Question:** Will a person work through the sixteen checks below on the running app (a real phone, per
  criteria 22 and 32) and record each as pass or fail?
- **The checklist** (steps copied verbatim from `DoD.md` § Verification Evidence Required, not re-derived):

  1. **[DoD 4]** Install the debug build **over an existing install that already has the app's v3
     database** (do not uninstall first). Launch. Expect: no crash dialog. Open Home, Calendar, Settings —
     expect each still opens and Home still lists any notes you had.
  2. **[DoD 8]** Open the app. Look at the bottom bar. Expect a fourth entry whose icon and label read as
     "alarms" without being told which one it is. Tap it. Expect a screen headed *Alarms*. Tap it again —
     expect nothing to stack or flicker. Then tap Home: expect to get back in one tap.
  3. **[DoD 9]** Create twelve alarms at different times. On the Alarms screen swipe up. Expect the list to
     move and the twelfth row to be **completely** visible — its time and its message both, with no part
     under the round add button and no part cut off by the bottom bar. Swipe back to the top; expect the
     first row fully visible too.
  4. **[DoD 10]** On a fresh install, or after deleting every alarm, open Alarms. Expect a sentence in the
     middle of the content area saying there are no alarms yet — not a blank black rectangle, which reads as
     a load failure.
  5. **[DoD 11]** Create an alarm whose message is a full paragraph (≥ 300 characters). Look at its row in
     the list. Expect: the **time** is still readable and the row is still the same height as its
     neighbours; the message is clipped with an ellipsis rather than reflowing into a block that buries the
     time.
  6. **[DoD 13]** Open Alarms. Expect a round button floating above the list, in the bottom corner, that you
     can see without hunting and that does not sit on top of a row's text. Tap it. Expect a new screen to
     slide in with somewhere to type a message and a control to set a time.
  7. **[DoD 16]** Tap the add button. Tap the message field. Type four lines of text. Expect **the line you
     are typing stays visible above the keyboard** at all times. Set the time to 12:00 and expect to see
     `12:00` (or `12:00 PM`) before you save. Tap save; expect to land back on the list with the new alarm in
     it.
  8. **[DoD 18]** Create an alarm. Force-stop the app from Android Settings → Apps. Reopen it and open
     Alarms. Expect the alarm still listed with the same message and time.
  9. **[DoD 22]** On a real phone, create an alarm with the message `stand up`, set for **three minutes from
     now**. Press Home so the app is in the background. Wait. Expect: at that minute a banner **slides down
     over the top of whatever you are looking at** — not merely a silent icon in the status bar — showing
     `stand up`. Pull down the shade and confirm it is listed there too. Then **leave the phone until the
     same minute the next day** and confirm it fires again without you re-creating it.
  10. **[DoD 23]** With the notification showing, tap it. Expect the app to open on the Alarms list and the
      notification to disappear. Expect **not** a second copy of the app stacked on the running one — press
      Back once and expect to leave the app, not to find another Alarms screen underneath.
  11. **[DoD 25]** On the Alarms list, tap an existing alarm. Expect the editor to open with that alarm's
      message already in the field and its time already set — not blank, and not a second new alarm.
  12. **[DoD 28]** On the Alarms screen, find how to delete an alarm **without being told how** — if it is a
      gesture with no visible affordance, that is a fail. Trigger it. Expect a confirmation step. Expect the
      destructive button to be **filled in the error colour with bold text** while the way out is unfilled
      plain text: the two must differ in both colour role and emphasis.
  13. **[DoD 30]** Look at an alarm row with the switch **on**, then with it **off**, in a dim room at arm's
      length. Expect to be able to tell the two apart at a glance, and expect the switch's thumb/marker to be
      clearly distinct from the track it sits on.
  14. **[DoD 31]** In Android Settings, turn **off** notifications for the app. Open Alarms. Expect the
      screen to tell you alarms cannot notify you, with a control that takes you to the right system screen.
      Turn notifications back on, return to the app, and expect the warning to go away by itself. Repeat for
      exact alarms (Settings → Apps → Special app access → Alarms & reminders).
  15. **[DoD 32]** Create an alarm for ten minutes' time. **Reboot the phone.** Do **not** open the app
      afterwards. Wait. Expect the notification to arrive on time.
  16. **[DoD 35]** On a phone, open Alarms and the alarm editor. Expect a black background, white body text,
      and the **same blue** as the rest of the app on the add button and the save control — not a second
      blue, not a purple, not a Material default lilac anywhere. Expect every label legible against what is
      behind it.

- **Engine recommendation:** none — this is not a decision with options, it is a request for observation. Mark
  each item pass or fail below (or in `STATE.md` § Human sign-offs directly, dated). A failed item is a
  discovery like any other and will be reconciled (§8) into a task, an amendment, or a queued decision on the
  next invocation — it does not require re-opening this entry.
- **Decision:**



