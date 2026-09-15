# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-15 - branch `loop/calendar-note-app` - iteration 2_

## Abandoned tasks

None. Both T-001 and T-002 completed on attempt 1/3.

## Unreachable tasks

None.

## Decisions awaiting an answer

None. D-001 was answered and consumed this iteration.

## `human` criteria still unsigned

All seven `human` criteria in `DoD.md` (15-21) are unsigned. Both tasks are complete and every `machine`
criterion appears satisfied (DONE-candidate set in `STATE.md`), so the next invocation (the Verifier) is
expected to re-prove the `machine` criteria and then raise a Human Verification Request for these seven:
fresh install/first-open greeting (15), no second greeting same day (16), survives force-stop (17), greets
again next calendar day (18), Home permission sheet has no Location row (19), Alarms permission notice
unchanged (20), app still installs and opens (21).

## Review findings not fixed

Three non-blocking findings from this iteration's Fresh-Context Review (verdict: APPROVE). None breaks a
DoD criterion; none fixed, all reasoned through and accepted:

1. **`GreetingNotifier.kt`'s default `Clock.systemDefaultZone()` binds the timezone at construction time**
   (Koin singleton, built once at app start). A device timezone change while the process stays alive keeps
   "today" computed against the old zone until the next process restart — one missed or one double
   greeting in that window. Self-healing; matches this codebase's existing "a store owns its clock"
   pattern elsewhere (e.g. `NoteRepositoryImpl`). Not fixed.
2. **`SettingDatastore.kt`'s flows, including the new `lastGreetedDateFlow`, have no `.catch { IOException
   -> emptyPreferences() }` guard**, which `.claude/repository-layer.md` documents as required on every
   DataStore-backed flow. A pre-existing, repo-wide gap — every sibling flow in that file already lacks
   it — not a deviation introduced by this run. Not fixed.
3. **`HomeRequestPermission.kt`'s private `shouldShowRequestPermissionRationale` is dead code**, confirmed
   via `git diff` to have already been an orphan before this run's diff (its would-be caller,
   `requestLocation()`, used a different Accompanist symbol). Neither task's business to remove. Not fixed.

## Assumptions recorded

See `.harness/run/STATE.md` § Assumptions: the greeting is written regardless of notification-permission
grant state at post time; D-001 overrode the PRD's `VIBRATE` removal (kept, `DoD.md` revised and
renumbered accordingly); the two now-dead location string keys (`location`,
`allow_location_to_help_you`) were removed from both `res/values/strings.xml` and `res/values-de/strings.xml`
in this iteration's checkpoint.

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
Queue — this note exists only so they are not silently forgotten. Answering them is independent of this
run and can happen at any time.
