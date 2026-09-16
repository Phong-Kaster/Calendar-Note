# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-15 - branch `loop/calendar-note-app` - iteration 5 (Verifier)_

**Where the run stands.** Everything a command can prove is proved. Iteration 5 wrote no code: it
re-measured all fourteen `machine` criteria from scratch — `--rerun-tasks`, 61 of 61 Gradle tasks actually
executed, nothing served from cache — and all fourteen hold. **The only thing between this run and `DONE`
is six checks that need a person and a phone**, raised as `D-003`. One of them, criterion 15, is the check
that failed last time; its cause is fixed and written down as Constraint C-18.

## Abandoned tasks

None. T-001, T-002 and T-003 each completed on attempt 1 of 3.

## Unreachable tasks

None.

## Decisions awaiting an answer

**One: `D-003`** — the consolidated Human Verification Request for DoD criteria 15-20, queued by iteration
5. Answer it in `.harness/run/DECISIONS.md` under a `## D-003` heading, one line per item; a partial answer
is fine and the engine re-raises only what is still unsigned. It blocks the `DONE` report and no task,
because no task remains.

Two of its items cannot be automated on the device that exists here (19 and 20 need real taps — MIUI's
"USB debugging (Security settings)" gate refuses injected input), and one (18) asks for the system date to
be moved forward on a daily-driver phone, which is stated as the human's call with "open it tomorrow"
offered instead.

## `human` criteria still unsigned

Six of seven — the six in `D-003`. Criterion **21 is signed and stays signed**, and is deliberately **not**
in that request: it is about the removed location permissions, and nothing since has touched the manifest
or the permission code. (Its one carve-out: the notes list was not scrolled when it was checked.)

| # | What to check | Standing |
|---|---|---|
| 15 | **Fresh install, first open of the day → greeting appears**, legible, flat white icon, quiet rather than a banner | **Failed 2026-09-15; cause fixed by T-003, needs re-checking.** The precondition is the whole point: app data cleared, permission granted *when asked*. A device that already had the permission tests a different path, and that is how this shipped |
| 16 | Second open the same day → no second notification | **Unsigned again.** It passed on 2026-09-15, but T-003 changed `GreetingNotifier` — ENGINE.md §11 unsigns any item whose implementation changed, because the thing that was looked at no longer exists |
| 17 | Force-stop then relaunch, same day → still none | **Unsigned again**, same reason. Worth not skipping: this is the one check that separates "remembered on disk" from "remembered in memory" |
| 18 | Next calendar day → greeting returns unprompted | **Not answered.** Needs the device's system date moved forward a day, which affects every app on what is the human's daily-driver phone. Their call, and nobody should make it for them |
| 19 | Home's permission sheet: no Location row, no leftover gap, remaining rows open the right system screens | **Not answered, and not automatable on this device.** Injected input is refused — `SecurityException: Injecting input events requires ... INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate. It needs a finger |
| 20 | Alarms permission notice unchanged in both states | **Not answered**, same gate as 19. Belt-and-braces: no file under `ui/fragment/alarms/` has been touched by this run and that screen's pinned reference still validates |

## Verification status

**All fourteen `machine` criteria hold**, re-proved by iteration 5 — an invocation that wrote none of this
implementation and read none of the previous tables as input. Its own run:

```
:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest --rerun-tasks
  -> BUILD SUCCESSFUL in 1m 8s, 61 actionable tasks: 61 executed (no UP-TO-DATE on any line)
unit tests   296 across 19 classes, 0 failures, 0 errors
lint         0 errors, 75 warnings
screenshots  23 rendered, 0 diffs; 23 live hashes == 23 files on disk, no orphans
```

The per-criterion table is in `.harness/run/STATE.md` § Machine verification. Nothing on it is a gap.

Two methodological traps, both recorded in `.harness/knowledge/PROJECT.md` so they are not rediscovered:

- Re-running the verification command on an unchanged tree returns `BUILD SUCCESSFUL` with almost
  everything `UP-TO-DATE`, and `--rerun` does **not** force `:app:testDebugUnitTest` — only
  `--rerun-tasks` does. Read the per-task `UP-TO-DATE` marker, not the `BUILD SUCCESSFUL` line.
- **C-18**: `NotificationManagerCompat.notify()` does not report the failure that matters. Without
  `POST_NOTIFICATIONS` on Android 13+ it accepts the notification, shows nothing and returns normally, so
  the `catch (SecurityException)` that looks like careful error handling catches essentially nothing.
  Anything recording "this happened" must ask first rather than assume.

## Review findings not fixed

From iteration 4's Fresh-Context Review (verdict **APPROVE**, zero blocking findings). Two of its three
findings were fixed in the same checkpoint (`AMENDMENTS.md` A-16); this one was not:

1. **`ui/` now has two direct edges to `data/notification/`.** `GreetingNotifier` is injected straight
   into `HomeFragment` (and into `MainActivity`), bypassing `HomeViewModel`. T-003 directed it and
   `MainActivity` set the precedent, so it is not a defect — but it is now a pattern two call sites deep
   that the next screen will copy. Worth a deliberate decision if a third appears.

Still open from iteration 2's review, all reasoned through and accepted, none breaking a DoD criterion:

2. **`GreetingNotifier`'s default `Clock.systemDefaultZone()` binds the timezone at construction time**
   (Koin singleton, built once at app start). A device timezone change mid-process keeps "today" computed
   against the old zone until the next process restart — one missed or one double greeting in that window.
   Self-healing, and it matches this codebase's existing "a store owns its clock" pattern.
3. **`SettingDatastore.kt`'s flows have no `.catch { IOException -> emptyPreferences() }` guard**, which
   `.claude/repository-layer.md` documents as required. A pre-existing, repo-wide gap — every sibling flow
   in that file already lacks it — not something this run introduced.
4. **`HomeRequestPermission.kt`'s private `shouldShowRequestPermissionRationale` is dead code**, confirmed
   via `git diff` to have been an orphan before this run's diff. No task's business to remove.

Two facts about that same file, found by this iteration's Worker and worth knowing before wiring anything
else into it (now in `PROJECT.md`): `onNotificationGranted` fires from **two** call sites, so any callback
wired there must be idempotent; and `onExactAlarmGranted` fires on every `RESUMED` transition rather than
on the false→true one, so re-arming alarms through this screen would re-arm the whole list on every
resume — exactly what Constraint C-17 warns against.

## Assumptions recorded

See `.harness/run/STATE.md` § Assumptions. The one worth reading is the **delivery rule**, because the
human deliberately left its design open: "delivered" means `areNotificationsEnabled()` is true and the post
did not throw, and a user who muted only the *greeting channel* is counted as delivered so the app stops
trying. The superseded assumption from iterations 2-3 — "once per calendar day is a calendar fact, not
once per day it actually reached the user" — is **withdrawn**; it is precisely what criterion 15 failed on.

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

### SIGNED OFF by the human, 2026-09-16

All sixteen are **confirmed passing**. Recorded verbatim, because that is what was said and nothing
more should be read into it: *"tớ đã kiểm tra rồi, tất cả ok"* — I have checked them, everything is fine.

What this signature is, exactly: a person who has the app on a real device states that the sixteen
`human` criteria of the alarms run hold. It is **not** backed by machine evidence in this repository,
and none was produced for it — the alarms run's `machine` criteria were separately re-proved by its
own Verifier at the time. Anyone re-opening this should know which half rests on which.

The alarms feature therefore has no outstanding verification debt. It remains unmerged on
`loop/calendar-note-app`; merging is a separate act.

**What would reopen this:** a change to the alarm scheduling, notification or boot-receiver code. A
signature covers the build it was given, not the file forever — these sixteen were signed against the
tree at `3506b33`, and the greeting work since then touched `AndroidManifest.xml` and the notification
package, though not the alarm path itself.
