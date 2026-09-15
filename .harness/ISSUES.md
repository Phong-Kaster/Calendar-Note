# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-15 - branch `loop/calendar-note-app` - iteration 3 (Verifier)_

**The one thing waiting on a person:** seven manual checks, `D-002`. Everything a command can prove has
been proved twice, by two different invocations. Details under "`human` criteria still unsigned" below.

## Abandoned tasks

None. Both T-001 and T-002 completed on attempt 1/3.

## Unreachable tasks

None.

## Decisions awaiting an answer

**D-002 — Human Verification Request.** The only open item in the run, and the only thing between it and
completion. It blocks no task (none remains); it blocks the `DONE` report itself. The full seven-item
checklist is in `.harness/run/ESCALATION.md` under `## D-002`, mirrored in `SUGGESTIONS.html`'s Escalate
tab. Answer in `.harness/run/DECISIONS.md` under a `## D-002` heading — one line per item is plenty.

D-001 (DoD approval) was answered and consumed in iteration 2.

## `human` criteria still unsigned

All seven (`DoD.md` 15-21), carried by D-002. They stay unsigned because nothing in this repository can
prove them: no emulator, no device, no Robolectric, so no command the engine can run ever sees the app
running. Roughly ten minutes on a real device, one of which is a date change.

| # | What to check | Why it is on this list and not provable by command |
|---|---|---|
| 15 | Fresh install, first open of the day → greeting appears, legible, flat white icon, quiet rather than a banner | Whether a notification is *readable* and *correctly iconed* is appearance; no reference render exists for a system-drawn notification |
| 16 | Second open the same day → no second notification | The decision is unit-tested; that it is actually reached on a real foreground is not |
| 17 | Force-stop then relaunch, same day → still none | The one check distinguishing "remembered on disk" from "remembered in memory". Worth not skipping — its failure mode is a greeting on every single launch |
| 18 | Next calendar day → greeting returns unprompted | Needs a real clock crossing a real midnight |
| 19 | Home's permission sheet: no Location row, no leftover gap, remaining rows open the right system screens | Whether a removed row left a visible hole is appearance; the sheet has no pinned reference image |
| 20 | Alarms permission notice unchanged in both states | Belt-and-braces: the diff touches no file under `ui/fragment/alarms/` and that screen's pinned reference still validates |
| 21 | App installs and opens; notes list renders | Two permissions were deleted from the manifest; nothing host-side can launch the APK |

A **fail** on any item is an ordinary discovery, not a disaster: the engine files a task, clears the
DONE-candidate flag and keeps going.

## Verification status

Iteration 3 ran as the Verifier (ENGINE.md §11) — it wrote none of the implementation — and re-proved all
fourteen `machine` criteria against its own fresh evidence. **No gaps found.** Per-criterion table in
`.harness/run/STATE.md` § Machine verification. Summary: `BUILD SUCCESSFUL`, 288 tests / 0 failures across
18 classes, lint 0 errors / 75 warnings, screenshots 23 of 23 with no orphans, source and merged manifest
both clean of the two location permissions.

One methodological catch worth knowing about, now recorded in `.harness/knowledge/PROJECT.md`: re-running
the verification command on an unchanged tree returns `BUILD SUCCESSFUL` with almost everything
`UP-TO-DATE`, and `--rerun` does **not** force `:app:testDebugUnitTest` to re-execute — only
`--rerun-tasks` does. The 288/0 figure above is from a forced re-execution, not a cache hit.

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
