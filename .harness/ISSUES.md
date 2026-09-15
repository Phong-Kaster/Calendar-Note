# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-15 - branch `loop/calendar-note-app` - iteration 4_

**What happened this iteration.** A person tried DoD criterion 15 on a real phone and it **failed**: on a
fresh install, the greeting never arrived — not that day, not later. Every unit test had been green, lint
clean, and an earlier device check had passed, because that check was run on a device where the
notification permission was already granted. That is the other branch. The cause is fixed (T-003) and
written down as Constraint C-18, and six of the seven `human` criteria now need a person again.

## Abandoned tasks

None. T-001, T-002 and T-003 each completed on attempt 1 of 3.

## Unreachable tasks

None.

## Decisions awaiting an answer

None right now. **D-002 was answered and consumed this iteration** — in part: four items answered, three
not. The next invocation (the Verifier) will queue **one** consolidated Human Verification Request
covering the six criteria below and stop there. Nothing is blocked in the meantime because no task remains.

## `human` criteria still unsigned

Six of seven. Criterion **21 is signed and stays signed** — it is about the removed location permissions,
and nothing since touched the manifest or the permission code. (Its one carve-out: the notes list was not
scrolled when it was checked.)

| # | What to check | Standing |
|---|---|---|
| 15 | **Fresh install, first open of the day → greeting appears**, legible, flat white icon, quiet rather than a banner | **Failed 2026-09-15; cause fixed by T-003, needs re-checking.** The precondition is the whole point: app data cleared, permission granted *when asked*. A device that already had the permission tests a different path, and that is how this shipped |
| 16 | Second open the same day → no second notification | **Unsigned again.** It passed on 2026-09-15, but T-003 changed `GreetingNotifier` — ENGINE.md §11 unsigns any item whose implementation changed, because the thing that was looked at no longer exists |
| 17 | Force-stop then relaunch, same day → still none | **Unsigned again**, same reason. Worth not skipping: this is the one check that separates "remembered on disk" from "remembered in memory" |
| 18 | Next calendar day → greeting returns unprompted | **Not answered.** Needs the device's system date moved forward a day, which affects every app on what is the human's daily-driver phone. Their call, and nobody should make it for them |
| 19 | Home's permission sheet: no Location row, no leftover gap, remaining rows open the right system screens | **Not answered, and not automatable on this device.** Injected input is refused — `SecurityException: Injecting input events requires ... INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate. It needs a finger |
| 20 | Alarms permission notice unchanged in both states | **Not answered**, same gate as 19. Belt-and-braces: no file under `ui/fragment/alarms/` has been touched by this run and that screen's pinned reference still validates |

## Verification status

Iteration 3 ran as the Verifier and re-proved all fourteen `machine` criteria — correctly, for the tree it
saw. **That tree has since changed** (T-003 added two files and modified two), so iteration 4 re-measured
what its own change touched and the next invocation re-proves all fourteen from scratch. Iteration 4's own
run: `BUILD SUCCESSFUL`, **296 tests / 0 failures across 19 classes** (up from 288/18), lint 0 errors /
75 warnings, screenshots 23 of 23 with no orphans.

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

Those sixteen items are still unverified by a person and are **not** part of this run's DoD or Decision
Queue — this note exists only so they are not silently forgotten. Answering them is independent of this
run and can happen at any time.
