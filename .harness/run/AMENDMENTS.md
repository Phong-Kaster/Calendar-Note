# AMENDMENTS

> Every plan mutation, logged. Tier 1 is automatic and recorded here. Tier 2 arrives here only after the
> human answered the decision that authorised it; Tier 3 never arrives here at all, because intent is not
> the engine's to amend.

<!-- Newest first. -->

### A-12 — Four Fresh-Context Review findings fixed inside already-owned files, A-004 marked complete (Tier 1, review-driven)

- **Iteration:** 6
- **Date:** 2026-09-14
- **What changed:** A-004 completed across two Worker dispatches (a prior, crashed invocation's
  dispatch for the scheduler/receiver/notifier vertical, salvaged after re-verification per §6.1; this
  iteration's own dispatch for the repository-mirroring half) plus this Iteration's DI/manifest/
  Activity/Application wiring. A clean-context Fresh-Context Review against every Constraint and DoD
  19/20/21/29/33/34/22/23 found no Constraint violation and six findings, four fixed this checkpoint:
  1. **MAJOR** — `MainActivity`'s notification-tap handling replayed on every activity recreation (a
     rotation, or the in-app language change, both of which recreate the activity without a fresh
     `Intent`) and could stack a duplicate Alarms screen over an open editor or Settings. Fixed:
     `onCreate` now guards on `savedInstanceState == null`, and navigation goes through the
     `R.id.toAlarms` tab-swap action (`popUpTo="@id/homeFragment"` + `launchSingleTop`) instead of the
     raw destination.
  2. **MAJOR** — `android.permission.VIBRATE` was never declared, so the vibration pattern
     `AlarmNotifier` builds (and `AlarmNotifierConstantsTest` asserts) could never fire — silently
     dropping half of the API-24/25 heads-up recipe (C-10) on a silenced phone. Added to the manifest.
  3. **MINOR** — `ScheduleDecision`/`scheduleDecision`/`requestCodeFor` were `public` with no caller
     outside `AlarmRepositoryImpl`'s own mirroring. Narrowed to `internal` (still visible to the test
     source set) so nothing can bypass the repository and arm or cancel an alarm directly.
  4. **MINOR** — `MainApplication` built a second, DI-invisible `AlarmNotifier` instance to create the
     notification channel at startup, separate from the one `schedulerModule` binds for `AlarmReceiver`.
     Now resolved through Koin's own `startKoin { ... }` return value instead.
  A fifth, a misleading test comment claiming the repository's own `clock` decides an alarm's fire
  instant (it does not — only `AlarmManagerAlarmScheduler`'s does), was also corrected.
- **Recorded, not acted on:** `AlarmNotifier.createChannelIfNeeded()`'s name promises a guard its body
  does not contain (harmless — the platform call itself is idempotent, per C-10 — but worth a rename);
  and a sub-second race where a delete confirmed between `AlarmReceiver.onReceive`'s notify and re-arm
  steps can resurrect a just-deleted alarm's schedule — the "rebuild from the intent, never re-read the
  table" design is this task's own deliberate choice, and reconciling it is A-005's job. Both in
  `ISSUES.md` and `TASKS/A-004.md` § Notes.
- **Why:** all four fixed findings sit inside files already in A-004's Declared File Scope or are
  Iteration-owned (`MainActivity.kt`, `MainApplication.kt`, `AndroidManifest.xml`); none changes the
  PRD, DoD, architecture, or A-004's acceptance criteria — only correctness of what already exists.
- **Tier:** 1 — review-driven correctness fixes; no PRD/DoD/architecture change.

### A-11 — D-006 consumed: goal-scoped `updateDebugScreenshotTest` grant applied, A-003 marked complete (Tier 2)

- **Iteration:** 6
- **Date:** 2026-09-14
- **What changed:** A-003's completion is no longer blocked. `.harness/run/capabilities.json` gained a
  third, single-use entry matching D-006's approved text exactly (scoped to one invocation of
  `updateDebugScreenshotTest --tests "*AlarmDeleteConfirmationCase*"`); the Iteration ran that one
  command, recorded the reference image, and re-verified `assembleDebug` + `testDebugUnitTest` +
  `lintDebug` + `validateDebugScreenshotTest` together (`BUILD SUCCESSFUL`, 23/23 screenshot cases). A-003
  marked complete; its `human` criteria (25, 28, 30) join the pool ready for the end-of-run Human
  Verification Request. A-004 becomes selectable.
- **Why:** The human approved option 1 as written: a single-use grant for exactly one new, prior-reference-
  free case, with a before/after report of the reference directory.
- **Conditions met:** answered decision, named task (A-003) unblocked, transitively unblocking
  A-004 → {A-005, A-006} → A-007, per ENGINE.md §6.2.

### A-10 — Three Fresh-Context Review findings fixed inside A-003's own files (Tier 1, review-driven)

- **Iteration:** 5
- **Date:** 2026-09-14
- **What changed:**
  1. `AlarmsUiState.deletedTrigger` was never consumed. Unlike the note editor (which leaves the screen
     on a delete and never needs to), the Alarms screen stays put, so a `LaunchedEffect` keyed on the
     trigger would replay "Alarm deleted" on the next unrelated recomposition (a rotation, or a return
     from the editor). Added `AlarmsViewModel.consumeDeleted()`, wired it from `AlarmsFragment`, and
     added a regression test (`the delete trigger clears once it has been shown, so a later delete is
     still announced`).
  2. The delete bin's content description reused `delete_this_alarm` — the confirmation sheet's
     *question* text ("Delete this alarm?") — so a screen reader announced a question where an action
     name belongs. Added a separate `delete_alarm` string ("Delete alarm") for the icon.
  3. `README.md`'s feature table and package tree still said three things this task's diff made false:
     switching/deleting were listed as not-yet-built, `AlarmDao` was described as having no delete, and
     the new `AlarmDeleteConfirmSheet.kt` was missing from the tree. Corrected in the same change,
     per `.claude/android-skeleton-project.md`'s own rule that README is updated in the change that lands
     each piece.
- **Why:** all three are inside files already in A-003's Declared File Scope or are Iteration-owned
  (`README.md`, `strings.xml`); none changes the PRD, DoD, architecture, or A-003's acceptance criteria —
  only correctness and truthfulness of what already exists.
- **Recorded, not acted on:** the Review also flagged `AlarmDeleteConfirmSheet.kt` as a near-duplicate of
  `NoteDeleteConfirmSheet.kt` with no shared component behind either. Not extracted this checkpoint —
  doing so would touch `NoteFragment.kt` and `NoteScreenshotTest.kt`, both outside this task and both
  already shipped and reviewed. Noted in `TASKS/A-003.md` and `ISSUES.md` instead.
- **Tier:** 1 — review-driven correctness fixes inside already-owned files; no PRD/DoD/architecture change.

### A-9 — D-005 consumed: the destructive-migration call is removed, four KDoc blocks corrected (Tier 2)

- **Iteration:** 5
- **Date:** 2026-09-14
- **What changed:** `injection/DatabaseModule.kt` no longer calls `.fallbackToDestructiveMigration(false)`
  — Room's default now applies: a missing migration path throws `IllegalStateException` at launch instead
  of silently dropping and recreating tables. The four KDoc blocks that asserted the opposite behaviour
  while the call was still in force are corrected (`DatabaseModule.kt`, `Migration.kt` ×2,
  `AlarmEntity.kt`, `Alarm.kt`). Constraint **C-13** in `.harness/knowledge/PROJECT.md` rewritten to
  record the resolved state rather than only the trap.
- **Why:** The human answered D-005 with option 1, matching the engine's own recommendation: this is a
  reusable skeleton meant to be copied into other projects, so a loud crash a developer hits immediately
  beats a silent data-loss bug a real user hits later.
- **Conditions met:** answered decision, applied directly by the Iteration (not a Worker, since the
  touched files sit outside A-003's Declared File Scope) per ENGINE.md §6.2. Blocked nothing, so no task
  needed unblocking — logged regardless, per §6.2's rule that every consumed decision is logged.

### A-8 — A-002's Worker scope shrank to what was actually missing (Tier 1)

- **Iteration:** 4
- **Date:** 2026-09-14
- **What changed:** A-002's Declared File Scope lists 14 CREATE targets and 3 MODIFY targets. Twelve of the
  CREATE targets already existed on disk, committed between iterations by `7d0c0da` (see A-7). Rather than
  re-dispatch a Worker to write files that were already written — which would have overwritten reviewed,
  working code with a second independent guess at the same spec — the Worker Brief named those twelve as
  read-only context and scoped the Worker to what was genuinely absent: the three `alarms/` MODIFY targets
  (still in their A-001 "nothing is stored yet" state) and the two test files (which did not exist).
- **Why:** the task's acceptance criteria are unchanged and every file in the original scope is still
  covered — the difference is only *who wrote it and when*. Re-writing them would have been volume for its
  own sake, and would have thrown away the one thing the existing files have that a fresh attempt would
  not: they already compile and validate.
- **Guard against trusting them blindly:** the twelve pre-existing files had never been reviewed by
  anything, so the Fresh-Context Review this iteration was explicitly pointed at them as well as at the
  new work. It found no Constraint violation in them and four non-blocking defects across the vertical,
  all fixed this iteration (see `HISTORY.md`).
- **Tier:** 1 — the PRD, the DoD, the architecture and the task's acceptance criteria are all unchanged.

### A-7 — The plan is reconciled with three commits made outside the loop (Tier 1)

- **Iteration:** 4
- **Date:** 2026-09-14
- **What changed:** three commits landed on the Loop Branch between iteration 3 and iteration 4 that no
  task and no Worker produced. `STATE.md` and `RESUME.md` described a repository that no longer existed,
  and §6.3's rule applied: the derived cache was wrong and git was right. Reconciled as follows.
  - **`7d0c0da`** ("uncompleted work because run out of quota") — landed twelve of A-002's source files
    (the whole domain/data vertical plus the `alarm_editor/` screen and `AlarmRow.kt`) **and** the
    `AlarmsEmptyStateCase` screenshot reference that D-004 was queued to obtain. It did **not** land
    A-002's two test files, nor any of the Iteration-owned wiring the vertical needs to function
    (`AppDatabase` was still `version = 3` with no `AlarmEntity`, no `MIGRATION_3_4`, no DI bindings, no
    `alarmEditorFragment` in the navigation graph). The app therefore compiled while the entire feature
    was unreachable and unpersisted — which is why "it builds" was not treated as "it works".
  - **`be95b3f`** — added the alarm strings to both `values/` and `values-de/` and fixed an
    `onTimeChange` default. Absorbed as-is; it is consistent with C-02 and needed no correction.
  - **`ee7b5c9`** — removed the machine-specific `org.gradle.java.home` pin from `gradle.properties`.
    Re-verified this iteration: all four commands still reach `BUILD SUCCESSFUL` without it. The
    `.harness/knowledge/PROJECT.md` entry that flagged the pin for relocation is now closed rather than
    outstanding.
- **Tier:** 1 — no PRD, DoD or architecture change. The task list, the dependency graph and every
  acceptance criterion are exactly as approved; only the record of which work was already done moved.

### A-6 — `AlarmRow` formats its time to the device's clock rather than hardcoding 24-hour (Tier 1, review-driven)

- **Iteration:** 4
- **Date:** 2026-09-14
- **What changed:** `AlarmRow` rendered `21:30` unconditionally, while the editor's
  `rememberTimePickerState` leaves `is24Hour` at its default — the device setting. On a 12-hour device a
  user set an alarm reading `9:30 PM`, saved, and the row that appeared read `21:30`: the same alarm in
  two notations one screen apart, which reads as the app having changed what they typed. Now formatted
  through `DateTimeFormatter` against `LocalConfiguration.current.locales[0]` and
  `DateFormat.is24HourFormat(...)`, per C-11.
- **Consequence to carry forward:** the row's text is now host-dependent, so **any future screenshot case
  photographing `AlarmRow` is host-locked** and must take a pre-formatted `String` instead — the trap
  PROJECT.md already records for the Note editor. No reference photographs `AlarmRow` today.
- **Tier:** 1 — a defect fix inside a file already in A-002's Declared File Scope.

### A-5 — Phase 1 gains `AlarmsScreenshotTest.kt` as an Iteration-owned file (Tier 1)

- **Iteration:** 3
- **Date:** 2026-09-13
- **What changed:** `PLAN.md`'s Phase 1 row omitted `AlarmsScreenshotTest.kt`, even though A-001's own
  Acceptance section requires *"a new `@PreviewTest` case pinning the empty state, sized per C-05"* and
  every later phase's row already lists that file as Iteration-owned. A Fresh-Context Review (§6.8) caught
  the gap: no such test existed, the screenshot case count was unchanged from the previous run, and nothing
  pins `AlarmsEmptyState`'s rendering against a later regression. Added `AlarmsScreenshotTest.kt` to Phase
  1's row and wrote the file this iteration, rendering `AlarmsEmptyState` directly (the same pattern
  `HomeScreenshotTest.kt` uses for `HomeNoteList`) rather than the whole screen, so the case needs no
  `NavController` and does not accidentally photograph `CoreBottomBar`'s AS-5 special case as a side effect.
- **Why:** ENGINE.md §6.8 requires the Fresh-Context Review to check the diff against the task's own
  acceptance criteria; the reviewer found this omission before it left the run silently uncovered. A-001
  cannot honestly be evidenced as done without it.
- **Conditions met:** PRD, DoD and architecture unchanged; the task's own written acceptance criteria did
  not change, only the Iteration's bookkeeping of which file satisfies them.

### A-6 — `hidesCreateButton` moved onto `BottomBarDestination` (Tier 1)

- **Iteration:** 3
- **Date:** 2026-09-13
- **What changed:** The Fresh-Context Review flagged that hiding the centre "+" via a hardcoded
  `it.id == BottomBarDestination.Alarms.destinationId` check inside `CoreBottomBar` put AS-5's decision in
  the wrong file: a screen reachable through a different hierarchy (a future deep link, for instance) would
  silently fail to hide the button, with no compiler error and no test. Replaced with a
  `hidesCreateButton: Boolean = false` field on `BottomBarDestination` itself, resolved once against
  whichever entry matches `currentDestination.hierarchy`, so the decision travels with the destination that
  owns it rather than living as a special case one file away.
- **Why:** Same review pass as A-5; a correctness improvement inside files already Iteration-owned for
  this Phase, not a change to any task's scope, dependency, or the DoD.
- **Conditions met:** PRD, DoD and architecture unchanged; no task's Declared File Scope changed.

### A-4 — D-003 consumed: goal-scoped `updateDebugScreenshotTest` grant applied (Tier 2)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** A-001's completion is no longer blocked. `.harness/run/capabilities.json` already
  carried the granted entry (written by the human at the end of iteration 1); this iteration consumes
  the decision itself — clears A-001's `Blocked by decision` line and archives the full exchange into
  `HISTORY.md`.
- **Why:** The human approved option 1 as written: re-record only `BottomBar_*` and
  `BottomBarSystemNight_*`, reporting the live-hash list before and after. That report is owed the first
  time A-001's Phase actually triggers a re-record — not at consumption time, since no re-record has run
  yet.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2.

### A-3 — D-002 consumed: Alarms is the fourth tab; centre "+" hidden there (Tier 2)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** A-001, A-002, A-006 unblocked. AS-5 stands as written — no change to `PLAN.md` or any
  task file's Declared File Scope or Description, because both already assumed this outcome (the fourth
  tab, the hidden centre "+", the dedicated FAB) when they were authored in iteration 1. Consumption here
  is bookkeeping: the decision that made that assumption load-bearing is now on record as answered.
- **Why:** The human chose option 1 of D-002 — the only reading that honors the addendum's explicit ask
  for a floating action button without putting two "create" controls on one screen.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2.

### A-2 — D-001 consumed: DoD approved, standing capabilities installed (Tier 2 / Tier 3 mixed)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** All seven tasks (A-001 … A-007) unblocked. `DoD.md` is now immutable to the engine
  per ENGINE.md §5. `.harness/knowledge/capabilities.json` already carried the three approved standing
  blocks (gradlew assemble/compile/test/lint, `validateDebugScreenshotTest`, and the
  `MSYS_NO_PATHCONV` git-show/ls-tree workaround), written by the human at the end of iteration 1.
  AS-4 (the per-alarm on/off switch) is kept, as the human's decision directed.
- **Why:** The human approved the DoD, its Verification Class split, and the Assumptions table as
  written — option 1 of D-001 — and approved the capability proposal as written.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2. The DoD-approval half
  of this decision touches what a human may change forever (Tier 3, never the engine's to make) but is
  logged here because §6.2 requires every consumed decision logged, regardless of the tier of the
  underlying question.

### A-1 — A-007's Declared File Scope loses `.harness/ISSUES.md` (Tier 1)

- **Iteration:** 1
- **Date:** 2026-09-13
- **What changed:** `PLAN.md`'s Task Graph gave A-007 the scope
  `.harness/knowledge/PROJECT.md`, `.harness/ISSUES.md`. The second path is removed; A-007 now writes
  `PROJECT.md` only, and `.harness/ISSUES.md` stays Iteration-owned. `PLAN.md`'s Phase Grouping row for
  Phase 6 gains `.harness/ISSUES.md` and `README.md` as the Iteration's shared files.
- **Why:** ENGINE.md §6.10 and §10 require the Iteration to regenerate `.harness/ISSUES.md` in **every**
  checkpoint. A Worker holding it in scope would write the same file the Iteration writes in the same
  commit — precisely the collision the §6.6 scope check exists to catch, and it would have fired on the
  last Phase of the run after six clean ones.
- **Conditions met:** PRD, DoD and architecture unchanged. No task's behaviour changes; only who holds
  the pen on one file.
