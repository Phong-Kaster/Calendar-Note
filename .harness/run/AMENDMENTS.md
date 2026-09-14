# AMENDMENTS

> Every plan mutation, logged. Tier 1 is automatic and recorded here. Tier 2 arrives here only after the
> human answered the decision that authorised it; Tier 3 never arrives here at all, because intent is not
> the engine's to amend.

<!-- Newest first. -->

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
