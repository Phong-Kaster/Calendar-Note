# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing
  <!-- "Stage" is the run's lifecycle position. A "Phase" is a group of tasks. Do not conflate them. -->
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none executable right now. A-003 is implemented, reviewed and evidenced but **blocked
  from "complete" by D-006** (a screenshot-reference grant; see ESCALATION.md). A-004 → {A-005, A-006} →
  A-007 all depend on A-003 transitively and stay unreachable until D-006 is answered. Once it is: Phase 4
  is A-004 alone; A-005 and A-006 (both depending only on A-004) are the first genuine pairing candidate —
  check their Declared File Scopes for disjointness when that phase is selected, since both touch
  `AlarmScheduler`.
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| A-001 | **complete** (iteration 4) | `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` | See `TASKS/A-001.md` § Evidence |
| A-002 | **complete** (iteration 4) | `domain/model/Alarm*`, `domain/repository/AlarmRepository.kt`, alarm files under `data/**`, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, own tests | See `TASKS/A-002.md` § Evidence |
| A-003 | **blocked by decision D-006** (iteration 5) | the alarm/alarms/data files A-002 creates, plus `AlarmDeleteConfirmSheet.kt` and tests | See `TASKS/A-003.md` § Evidence |
| A-004 | pending | `domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`, `res/drawable/ic_notification_alarm.xml`, `AlarmRepositoryImpl.kt`, own tests | – |
| A-005 | pending | `data/receiver/BootReceiver.kt`, `AlarmScheduler.kt`, `AlarmManagerAlarmScheduler.kt`, own test | – |
| A-006 | pending | `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` plus the alarms screen trio and its test | – |
| A-007 | pending | `.harness/knowledge/PROJECT.md` | – |

## Assumptions

<!-- Minor PRD/DoD ambiguities resolved by recorded assumption (auditable, reversible). Also copied
     into ISSUES.md. Behavior-defining ambiguity queues a decision instead. -->

- **AS-1 … AS-11 live in `DoD.md`**, not here — see the note in D-001's archived exchange.
- The bootstrap-debris salvage (iteration 1) and the human-installed-tooling exclusions
  (`.harness/loop/`, `.claude/`, `.agents/skills/`, `skills-lock.json`, root `knowledge/`) still stand as
  recorded in prior iterations; see `HISTORY.md`.
- **Three commits landed on the Loop Branch outside the loop** between iterations 3 and 4 — `7d0c0da`
  (twelve of A-002's source files plus the `AlarmsEmptyStateCase` reference image), `be95b3f` (alarm
  strings + an `onTimeChange` default), `ee7b5c9` (dropped the `org.gradle.java.home` pin). All three
  reconciled into the plan as `AMENDMENTS.md` A-7; none required reverting.
- **The `org.gradle.java.home` question is closed, not outstanding.** The pin was removed by `ee7b5c9` and
  the toolchain was re-verified from scratch this iteration: all four commands reach `BUILD SUCCESSFUL`
  without it. `.harness/knowledge/PROJECT.md` § Environmental Facts updated accordingly.
- **D-005 is resolved, not outstanding.** `fallbackToDestructiveMigration(false)` was removed from
  `DatabaseModule.kt` this iteration, restoring Room's throw-on-missing-migration default. Constraint C-13
  now records the resolved state; see `AMENDMENTS.md` A-9.
- **A-001's Phase 1 gained an Iteration-owned file the original plan omitted**: `AlarmsScreenshotTest.kt`,
  required by A-001's own acceptance criteria (a `@PreviewTest` pinning the empty state). Logged as
  `AMENDMENTS.md` A-5. A second amendment, A-6, moved AS-5's hide-the-centre-button logic onto
  `BottomBarDestination.hidesCreateButton` after a Fresh-Context Review flagged the original placement as
  fragile.
- **A-003 is implemented, reviewed and fully machine-evidenced except for one thing**: three brand-new
  `@PreviewTest` screenshot cases (the delete confirmation, and `AlarmRow` in each switch state) have no
  reference image yet. Queued as **D-006**, mirroring D-004's shape exactly. Blocks A-003's completion
  only, and every downstream task transitively.

## Human sign-offs

<!-- `human` DoD criteria signed off, with the date. Re-opened if the implementation behind one changes. -->

None yet. All 17 `human` criteria are unsigned — no person has looked at the running app in this run.

A-001 and A-002 are now both complete with fully green machine evidence, so criteria **8, 10** (the tab is
findable; the empty screen reads as empty rather than broken) and **3, 4, 9, 11, 13, 16** (upgrade over a
v3 install; the list scrolls and the last row clears the FAB; long-message overflow; the FAB is visible and
opens the editor; the editor is usable with the keyboard up) are all *ready to be shown to a person*.

**A-003's `human` criteria (25, 28, 30 — the row opens pre-filled; the delete control is findable and its
confirming button unmistakable; the switch reads correctly at a glance) are code-complete and reviewed**,
but A-003 itself is not marked complete (blocked by D-006), so per §11 they are not requested yet either —
a Human Verification Request is never raised against a task the run has not finished.

They are deliberately **not** being requested yet. A Human Verification Request is raised once at the end
(§11), not per phase — batching them costs the human one sitting instead of six, and several of these
criteria will be re-opened anyway by A-003, which changes the same screens. Listed in `ISSUES.md` so the
waiting work is visible now rather than arriving as a surprise at the end.
