# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** escalated
  <!-- "Stage" is the run's lifecycle position. A "Phase" is a group of tasks. Do not conflate them. -->
- **Loop Branch:** loop/calendar-note-app
- **Next Phase:** Phase 1 — A-001 (unselectable: blocked by D-001, D-002)
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| A-001 | blocked (D-001, D-002; D-003 for completion) | `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` | – |
| A-002 | blocked (D-001, D-002) | `domain/model/Alarm*`, `domain/repository/AlarmRepository.kt`, alarm files under `data/**`, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, own tests | – |
| A-003 | blocked (D-001) | the alarm/alarms/data files A-002 creates, plus `AlarmDeleteConfirmSheet.kt` and tests | – |
| A-004 | blocked (D-001) | `domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`, `res/drawable/ic_notification_alarm.xml`, `AlarmRepositoryImpl.kt`, own tests | – |
| A-005 | blocked (D-001) | `data/receiver/BootReceiver.kt`, `AlarmScheduler.kt`, `AlarmManagerAlarmScheduler.kt`, own test | – |
| A-006 | blocked (D-001, D-002) | `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` plus the alarms screen trio and its test | – |
| A-007 | blocked (D-001) | `.harness/knowledge/PROJECT.md` | – |

## Assumptions

<!-- Minor PRD/DoD ambiguities resolved by recorded assumption (auditable, reversible). Also copied
     into ISSUES.md. Behavior-defining ambiguity queues a decision instead. -->

- **AS-1 … AS-11 live in `DoD.md`**, not here — they are part of what the human approves in D-001, and
  duplicating them would let the two copies drift.
- The bootstrap that produced `DoD.md`, `PLAN.md`, `A-001` and `A-002` was **interrupted before it wrote
  any run scaffolding**. Iteration 1 salvaged that debris rather than reverting it: the four documents are
  internally consistent, cross-reference the same constraint ids, and reverting would have discarded the
  whole analysis fan-out to re-derive the same thing. Recorded because it is an assumption about work this
  process did not do and cannot see.
- `.harness/loop/`, `.claude/`, `.agents/skills/`, `skills-lock.json` and the root `knowledge/` directory
  are **human-installed tooling**, not run debris, and are deliberately left untracked and uncommitted.
- `PRD.md` is modified in the working tree — the human's Alarms addendum. It is **left uncommitted on
  purpose**: it is the human's intent file and not the engine's to stage. A future iteration seeing it dirty
  in §6.1 should not read it as a crashed run.

## Human sign-offs

<!-- `human` DoD criteria signed off, with the date. Re-opened if the implementation behind one changes. -->

None yet. All 17 `human` criteria are unsigned; the run has written no implementation.
