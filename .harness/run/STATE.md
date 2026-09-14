# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing
  <!-- "Stage" is the run's lifecycle position. A "Phase" is a group of tasks. Do not conflate them. -->
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none executable. A-005 is complete. A-006 is code-complete and blocked only on D-007
  (a screenshot reference image, queued this iteration). A-007 depends on both A-005 and A-006 and stays
  unreachable until A-006's completion is unblocked. No other task exists in this run.
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| A-001 | **complete** (iteration 4) | `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` | See `TASKS/A-001.md` § Evidence |
| A-002 | **complete** (iteration 4) | `domain/model/Alarm*`, `domain/repository/AlarmRepository.kt`, alarm files under `data/**`, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, own tests | See `TASKS/A-002.md` § Evidence |
| A-003 | **complete** (iteration 6) | the alarm/alarms/data files A-002 creates, plus `AlarmDeleteConfirmSheet.kt` and tests | See `TASKS/A-003.md` § Evidence |
| A-004 | **complete** (iteration 6) | `domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`, `res/drawable/ic_notification_alarm.xml`, `AlarmRepositoryImpl.kt`, own tests | See `TASKS/A-004.md` § Evidence |
| A-005 | **complete** (iteration 7) | `data/receiver/BootReceiver.kt`, `AlarmScheduler.kt`, `AlarmManagerAlarmScheduler.kt`, own test | See `TASKS/A-005.md` § Evidence |
| A-006 | pending — code complete, blocked on D-007 | `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` plus the alarms screen trio and its test | See `TASKS/A-006.md` § Evidence |
| A-007 | pending — unreachable | `.harness/knowledge/PROJECT.md` | – |

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
- **Phase 5 (A-005, A-006) landed via two parallel Workers with disjoint scope, verified against
  `git status` before either was trusted.** A Fresh-Context Review found two MAJOR and four MINOR
  defects across both — none inside either Worker's own correctness for the files it alone touched,
  all at the integration seam (the exact-alarm grant not re-arming; a test exercising a code path
  production didn't run; two settings deep-links landing one screen short; two silent failures; one
  unbounded `goAsync()`). All six fixed this checkpoint by the Iteration; see `AMENDMENTS.md` A-14 and
  Constraints C-14/C-15 in `.harness/knowledge/PROJECT.md`. A-005 is complete; A-006 is code-complete
  and blocked only on D-007, a screenshot capability grant for its one brand-new preview case — the
  same recurring situation as D-003/D-004/D-006.
- **A-001's Phase 1 gained an Iteration-owned file the original plan omitted**: `AlarmsScreenshotTest.kt`,
  required by A-001's own acceptance criteria (a `@PreviewTest` pinning the empty state). Logged as
  `AMENDMENTS.md` A-5. A second amendment, A-6, moved AS-5's hide-the-centre-button logic onto
  `BottomBarDestination.hidesCreateButton` after a Fresh-Context Review flagged the original placement as
  fragile.
- **D-006 is resolved, not outstanding.** Answered option 1: a single-use goal-scoped
  `updateDebugScreenshotTest` grant for exactly the new `AlarmDeleteConfirmationCase`. Applied, the
  reference recorded, A-003 marked complete. See `AMENDMENTS.md` A-11.
- **This iteration recovered from a crashed prior invocation (§6.1).** The working tree on entry
  already carried D-006's consumption (`AMENDMENTS.md`, `ESCALATION.md`, `TASKS/A-003.md` — all
  uncommitted) **and** a partial Worker dispatch for A-004 (`AlarmScheduler.kt`, `NextFireTime.kt`,
  `AlarmManagerAlarmScheduler.kt`, `AlarmReceiver.kt`, `AlarmNotifier.kt` and their tests, all
  untracked) — the prior invocation died after that dispatch but before the repository-wiring half of
  A-004, before Iteration-owned wiring, and before ever checkpointing. Both halves were re-verified
  (`assembleDebug`/`testDebugUnitTest`/`lintDebug`/`validateDebugScreenshotTest` all green,
  224 tests before this iteration's own dispatch) rather than trusted, then salvaged forward: a second
  Worker completed A-004's remaining scope (`AlarmRepositoryImpl.kt` mirroring + its tests,
  `AlarmNotifierConstantsTest.kt`), and this Iteration did the DI/manifest/Activity/Application wiring
  and the Fresh-Context Review's fixes. One stray scratch file from an earlier iteration's review pass
  (`a003_review.diff`, untracked, unrelated to any task) was found alongside the debris and removed.

## Human sign-offs

<!-- `human` DoD criteria signed off, with the date. Re-opened if the implementation behind one changes. -->

None yet. All `human` criteria are unsigned — no person has looked at the running app in this run.

A-001 and A-002 are now both complete with fully green machine evidence, so criteria **8, 10** (the tab is
findable; the empty screen reads as empty rather than broken) and **3, 4, 9, 11, 13, 16** (upgrade over a
v3 install; the list scrolls and the last row clears the FAB; long-message overflow; the FAB is visible and
opens the editor; the editor is usable with the keyboard up) are all *ready to be shown to a person*.

**A-003 and A-004 are now both complete.** A-003's `human` criteria (25, 28, 30 — the row opens
pre-filled; the delete control is findable and its confirming button unmistakable; the switch reads
correctly at a glance) and A-004's (22, 23 — the alarm actually fires as a popup with the user's
message and repeats the next day; tapping it opens Alarms without stacking a second copy) join the
same ready pool.

**A-005 is now complete.** Its `human` criterion (32 — an alarm set before a reboot still arrives
after it) joins the pool too. A-006 is code-complete but not yet marked done — its own `human`
criterion (31) will join once D-007 unblocks its completion.

All of these are deliberately **not** being requested yet. A Human Verification Request is raised once
at the end (§11), not per phase — batching them costs the human one sitting instead of several, and
some of these criteria will be re-opened anyway by later tasks touching the same screens (A-006 adds
to `AlarmsFragment`/`AlarmsUiState`/`AlarmsViewModel`). Listed in `ISSUES.md` so the waiting work is
visible now rather than arriving as a surprise at the end.
