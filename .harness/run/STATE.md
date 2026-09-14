# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing
  <!-- "Stage" is the run's lifecycle position. A "Phase" is a group of tasks. Do not conflate them. -->
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** none. **All seven tasks (A-001 … A-007) are complete.** Nothing abandoned, nothing
  deferred, no queued decision outstanding. Every `machine` DoD criterion appears satisfied by this
  iteration's own evidence; every `human` criterion is still unsigned (no person has looked at the running
  app in this run) — a Human Verification Request is owed before this run can report `DONE`.
- **DONE-candidate:** yes (set this iteration). The next invocation is the Verifier (§11): it must
  re-prove every `machine` criterion fresh (do not trust this iteration's numbers), then raise the Human
  Verification Request for all sixteen unsigned `human` criteria (4, 8, 9, 10, 11, 13, 16, 18, 22, 23, 25,
  28, 30, 31, 32, 35) and report `ESCALATE` — not `DONE` — until a person signs each one off.

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| A-001 | **complete** (iteration 4) | `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` | See `TASKS/A-001.md` § Evidence |
| A-002 | **complete** (iteration 4) | `domain/model/Alarm*`, `domain/repository/AlarmRepository.kt`, alarm files under `data/**`, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, own tests | See `TASKS/A-002.md` § Evidence |
| A-003 | **complete** (iteration 6) | the alarm/alarms/data files A-002 creates, plus `AlarmDeleteConfirmSheet.kt` and tests | See `TASKS/A-003.md` § Evidence |
| A-004 | **complete** (iteration 6) | `domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`, `res/drawable/ic_notification_alarm.xml`, `AlarmRepositoryImpl.kt`, own tests | See `TASKS/A-004.md` § Evidence |
| A-005 | **complete** (iteration 7) | `data/receiver/BootReceiver.kt`, `AlarmScheduler.kt`, `AlarmManagerAlarmScheduler.kt`, own test | See `TASKS/A-005.md` § Evidence |
| A-006 | **complete** (iteration 8) | `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` plus the alarms screen trio and its test | See `TASKS/A-006.md` § Evidence |
| A-007 | **complete** (iteration 8) | `.harness/knowledge/PROJECT.md` | See `TASKS/A-007.md` § Evidence |

## Assumptions

<!-- Minor PRD/DoD ambiguities resolved by recorded assumption (auditable, reversible). Also copied
     into ISSUES.md. Behavior-defining ambiguity queues a decision instead. -->

- **AS-1 … AS-11 live in `DoD.md`**, not here — see the note in D-001's archived exchange.
- The bootstrap-debris salvage (iteration 1) and the human-installed-tooling exclusions
  (`.harness/loop/`, `.claude/`, `.agents/skills/`, `skills-lock.json`, root `knowledge/`) still stand as
  recorded in prior iterations; see `HISTORY.md`.
- **A-007 completed this iteration.** `.harness/knowledge/PROJECT.md` reconciled with the tree; two new
  Constraints (C-16, C-17) added, each with evidence. A Fresh-Context Review found and the Iteration fixed
  one MAJOR (an overstated testability claim in C-06 contradicting 85 existing tests and C-17's own
  citation) and one MINOR (a stale "four constants" comment, `AlarmNotifier.kt` corrected to "five"). See
  `AMENDMENTS.md` A-16.
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

**A-006 is now complete too — its `human` criterion (31) joins the pool.** All seven tasks are complete;
no later task will touch any of these screens again, so the pool is now final: all sixteen `human`
criteria (4, 8, 9, 10, 11, 13, 16, 18, 22, 23, 25, 28, 30, 31, 32, 35) are ready, and none will be
re-opened by further code changes in this run, because there are none left. A-007 (this iteration) adds
no `human` criterion of its own — DoD 36 is `machine`.

A Human Verification Request has still **not** been raised yet — that is the Verifier's job (§11), on the
invocation that finds `DONE-candidate: yes` and writes none of the implementation. Listed in `ISSUES.md`
so the waiting work is visible now rather than arriving as a surprise at the end.
