# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

_Last updated: 2026-09-16 - branch `loop/calendar-note-app` - iteration 6 (Verifier, final)_

**Nothing is blocking. The run is complete.** All fourteen `machine` criteria were re-proved by iteration
6 on its own fresh Gradle run, and all seven `human` criteria are signed by a person — the last six on
2026-09-16 (`D-003`). No task was abandoned, none is unreachable, no decision is queued.

The branch is the deliverable. **Merging `loop/calendar-note-app` is the only thing left, and it is a
human's act.** What follows is the standing detail a reader coming back needs — none of it blocks the
merge, and none of it is a defect this run introduced unless it says so.

## Abandoned tasks

None. T-001, T-002 and T-003 each completed on attempt 1 of 3.

## Unreachable tasks

None.

## Decisions awaiting an answer

None. `D-001` (DoD approval, answered 2026-09-15), `D-002` (Human Verification Request, answered in part
2026-09-15) and `D-003` (the consolidated re-request for criteria 15-20, answered in full 2026-09-16) are
all consumed. The full record — questions, the human's verbatim rationale, and what each one changed — is
in `git log` and in the run's `AMENDMENTS.md` / `HISTORY.md` at commit `HEAD~1`, recoverable with:

```
MSYS_NO_PATHCONV=1 git show HEAD~1:.harness/run/HISTORY.md
MSYS_NO_PATHCONV=1 git show HEAD~1:.harness/run/AMENDMENTS.md
```

## `human` criteria still unsigned

**None — all seven are signed.**

| # | What was checked | Signed |
|---|---|---|
| 15 | Fresh install, first open of the day → greeting appears, legible, flat white icon, quiet rather than a banner | ✓ 2026-09-16 — **this is the one that failed on 2026-09-15** and was re-checked after T-003 fixed it |
| 16 | Second open the same day → no second notification | ✓ 2026-09-16 |
| 17 | Force-stop then relaunch, same day → still none | ✓ 2026-09-16 |
| 18 | Next calendar day → greeting returns unprompted | ✓ 2026-09-16 |
| 19 | Home's permission sheet: no Location row, no leftover gap, remaining rows open the right system screens | ✓ 2026-09-16 — see the clause note below |
| 20 | Alarms permission notice unchanged in both states | ✓ 2026-09-16 |
| 21 | App installs and opens after the permission removal | ✓ 2026-09-15 — one carve-out the human recorded: the notes list was not scrolled |

**Criterion 19's three clauses, because the human separated them and the distinction should not be lost.**
"Only the Notification row and, on Android 12+, the Exact alarms row — no Location row" was
machine-verified on a clean AOSP API 36 emulator, installed fresh so the sheet appeared on its own rather
than being assumed. "No leftover gap" and "toggling each row still opens the correct system dialog/screen"
were **not** — a view hierarchy lists nodes, not how they look, and the second needs a tap the attached
device refuses to inject. Those two rest on the human's eyes, which is exactly what the `human` class is
for. Nobody should read "19 pass" and assume a command proved all three.

**What would unsign any of this.** A signature covers the build it was given, not the file forever.
Changing `GreetingNotifier`, `MainActivity.onStart`, `SettingDatastore`'s greeting key, or the Home
permission sheet unsigns the criteria that look at them (ENGINE.md §11).

## Verification status

**All fourteen `machine` criteria hold**, re-proved by iteration 6 — an invocation that wrote none of this
implementation and read none of the previous tables as input. Its own run:

```
:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest --rerun-tasks
  -> BUILD SUCCESSFUL in 3m 11s, 61 actionable tasks: 61 executed (no UP-TO-DATE on any line)
unit tests   296 across 19 classes, 0 failures, 0 errors, 0 skipped (baseline was 251)
lint         0 errors, 75 warnings
screenshots  23 rendered, 0 diffs; 23 reference PNGs on disk == 23 rendered, no orphans
```

The per-criterion table is in the completion commit's message and in `STATE.md` at `HEAD~1`. Nothing on it
is a gap.

Three methodological traps, all recorded in `.harness/knowledge/PROJECT.md` so they are not rediscovered:

- Re-running the verification command on an unchanged tree returns `BUILD SUCCESSFUL` with almost
  everything `UP-TO-DATE`, and `--rerun` does **not** force `:app:testDebugUnitTest` — only
  `--rerun-tasks` does. Read the per-task `UP-TO-DATE` marker, not the `BUILD SUCCESSFUL` line.
- **C-18**: `NotificationManagerCompat.notify()` does not report the failure that matters. Without
  `POST_NOTIFICATIONS` on Android 13+ it accepts the notification, shows nothing and returns normally, so
  the `catch (SecurityException)` that looks like careful error handling catches essentially nothing.
  Anything recording "this happened" must ask first rather than assume. This is the rule criterion 15's
  failure was made of.
- The app's `namespace` (`com.example.skeleton`, the source tree) and its `applicationId`
  (`com.example.myapplication`, what the device calls it) are different strings and both are correct. A
  grep written from an `adb` transcript finds no files; one written from the source tree matches no device
  output. Neither means anything is broken.

## Review findings recorded but not fixed

None of these breaks a DoD criterion. They are recorded so the next person to touch these files does not
rediscover them, and the first is the only one worth a decision.

1. **`ui/` now has two direct edges to `data/notification/`.** `GreetingNotifier` is injected straight
   into `HomeFragment` and into `MainActivity`, bypassing `HomeViewModel`. T-003 directed it and
   `MainActivity` set the precedent, so it is not a defect — but it is now a pattern two call sites deep
   that the next screen will copy. **Worth a deliberate decision if a third appears.**
2. **`GreetingNotifier`'s default `Clock.systemDefaultZone()` binds the timezone at construction time**
   (Koin singleton, built once at app start). A device timezone change mid-process keeps "today" computed
   against the old zone until the next process restart — one missed or one double greeting in that window.
   Self-healing, and it matches this codebase's existing "a store owns its clock" pattern.
3. **`SettingDatastore.kt`'s flows have no `.catch { IOException -> emptyPreferences() }` guard**, which
   `.claude/repository-layer.md` documents as required. A pre-existing, repo-wide gap — every sibling flow
   in that file already lacks it — **not something this run introduced.**
4. **`HomeRequestPermission.kt`'s private `shouldShowRequestPermissionRationale` is dead code**, confirmed
   via `git diff` to have been an orphan before this run's diff. No task's business to remove.

Two facts about that same file, worth knowing before wiring anything else into it (both now in
`PROJECT.md` as C-15 and C-17): `onNotificationGranted` fires from **two** call sites, so any callback
wired there must be idempotent; and `onExactAlarmGranted` fires on every `RESUMED` transition rather than
on the false→true one, so re-arming alarms through this screen would re-arm the whole list on every
resume.

## Assumptions recorded

The one worth reading is the **delivery rule**, because the human deliberately left its design open:
"delivered" means `areNotificationsEnabled()` is true and the post did not throw, and a user who muted
only the *greeting channel* is counted as delivered so the app stops trying. A user who has not yet been
asked for the permission is not, so the next foreground retries. That distinction is the fix for criterion
15, and it is one `if` in `GreetingNotifier.postGreeting` if the human wants it drawn elsewhere.

The superseded assumption from iterations 2-3 — "once per calendar day is a calendar fact, not once per
day it actually reached the user" — is **withdrawn**; it is precisely what criterion 15 failed on.

Also standing, both reversible: D-001 kept `android.permission.VIBRATE` against the PRD's instruction to
remove it (the PRD's reason was wrong — `AlarmNotifier` really does vibrate), and two location-only string
keys (`location`, `allow_location_to_help_you`) were removed from both `values/strings.xml` and
`values-de/strings.xml`.

## Carried over from the previous goal (alarms) — not blocking this run

The alarms feature (7 tasks, `A-001` … `A-007`) completed all `machine` criteria and reached a
**Human Verification Request** with sixteen `human` criteria, raised as `D-008` in the alarms run's
`ESCALATION.md`. That run's `.harness/run/` was deliberately cleared in commit `736d762` to bootstrap this
new, unrelated goal — nothing was lost; the full record (DoD, the nine-iteration `HISTORY.md`, and D-008's
exact sixteen checklist items) is recoverable from git history:

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
`loop/calendar-note-app`; merging it and the greeting work is a single act now, and still the human's.

**What would reopen this:** a change to the alarm scheduling, notification or boot-receiver code. A
signature covers the build it was given, not the file forever — these sixteen were signed against the
tree at `3506b33`, and the greeting work since then touched `AndroidManifest.xml` and the notification
package, though not the alarm path itself. This run's diff contains no file under `ui/fragment/alarms/`,
`domain/scheduler/`, `data/scheduler/` or `data/receiver/`, and `AlarmNotifier.kt` is untouched.
