# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-15 - branch `loop/calendar-note-app` - iteration 1 (bootstrap, new goal)_

## Abandoned tasks

None. This run has just been bootstrapped; no task has been attempted.

## Unreachable tasks

None.

## Decisions awaiting an answer

| # | Question | Blocks |
|---|---|---|
| D-001 | Approve the Definition of Done for the greeting-notification / permission-cleanup run | T-001, T-002 |

## `human` criteria still unsigned

All eight `human` criteria in the new `DoD.md` (16-23) are unsigned — no task has been attempted yet, so
there is nothing to verify. They will be reachable once D-001 is approved and both tasks complete.

## Review findings not fixed

None yet this run.

## Assumptions recorded

See `.harness/run/STATE.md` § Assumptions: the greeting is written regardless of notification-permission
grant state at post time; `VIBRATE` removal proceeds as the PRD instructs despite `AlarmNotifier.kt`'s
independent vibration calls (a known, PRD-accepted risk, human-verified by DoD criterion 23); whether the
two now-dead location string keys are removed from both locale files is still to be decided at Phase-1
wiring time.

## Carried over from the previous goal (alarms) — not blocking this run

The alarms feature (7 tasks, `A-001` … `A-007`) completed all `machine` criteria and reached a
**Human Verification Request** with **sixteen unsigned `human` criteria**, raised as `D-008` in the
alarms run's `ESCALATION.md`. That run's `.harness/run/` was deliberately cleared in commit `736d762` to
bootstrap this new, unrelated goal — nothing was lost; the full record (DoD, the nine-iteration
`HISTORY.md`, and D-008's exact sixteen checklist items) is recoverable from git history:

```
MSYS_NO_PATHCONV=1 git show fb4bf24:.harness/run/ESCALATION.md
MSYS_NO_PATHCONV=1 git show fb4bf24:.harness/run/HISTORY.md
```

Those sixteen items are still unverified by a person and are **not** part of this run's DoD or Decision
Queue — this note exists only so they are not silently forgotten. Answering them is independent of D-001
and can happen at any time.
