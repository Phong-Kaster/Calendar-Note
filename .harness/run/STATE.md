# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** executing
  <!-- "Stage" is the run's lifecycle position. A "Phase" is a group of tasks. Do not conflate them. -->
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** Phase 1 — A-001 is implemented and checkpointed; its completion is blocked on D-004
  (a goal-scoped screenshot-recording grant). No other task is executable — A-002 … A-007 all depend on
  A-001, directly or transitively.
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| A-001 | blocked by decision (D-004) — implementation complete | `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` | See `TASKS/A-001.md` § Evidence |
| A-002 | pending | `domain/model/Alarm*`, `domain/repository/AlarmRepository.kt`, alarm files under `data/**`, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, own tests | – |
| A-003 | pending | the alarm/alarms/data files A-002 creates, plus `AlarmDeleteConfirmSheet.kt` and tests | – |
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
- `PRD.md` is modified and left uncommitted on purpose — the human's Alarms addendum, not the engine's to
  stage.
- **`gradle.properties` now carries `org.gradle.java.home`, pinned to this machine's Android Studio JBR.**
  Added to the working tree by a human between iteration 2 and this one — not by any task. Iteration 3
  only escaped the drive-letter colon that was failing `lintDebug`'s `PropertyEscape` check. Full note in
  `.harness/knowledge/PROJECT.md` § Environmental Facts and `.harness/ISSUES.md`.
- **A-001's Phase 1 gained an Iteration-owned file the original plan omitted**: `AlarmsScreenshotTest.kt`,
  required by A-001's own acceptance criteria (a `@PreviewTest` pinning the empty state). Logged as
  `AMENDMENTS.md` A-5. A second amendment, A-6, moved AS-5's hide-the-centre-button logic onto
  `BottomBarDestination.hidesCreateButton` after a Fresh-Context Review flagged the original placement as
  fragile.

## Human sign-offs

<!-- `human` DoD criteria signed off, with the date. Re-opened if the implementation behind one changes. -->

None yet. All 17 `human` criteria are unsigned. A-001 touches criteria 8 and 10, but neither is ready to
show a person until D-004 lands and Phase 1's evidence is fully green.
