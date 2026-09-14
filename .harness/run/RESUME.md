# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing. **A-001, A-002, A-003 and A-004 are all complete.** A-004's notification/
  scheduler/receiver vertical is wired end to end: DI (`SchedulerModule.kt`), the manifest
  `<receiver>`, channel creation at `MainApplication` startup, and `MainActivity` opening the Alarms
  list on a notification tap (`R.id.toAlarms`, guarded on `savedInstanceState == null` so it does not
  replay on rotation).
- **Next Phase: A-005 and A-006**, both depending only on A-004 (complete) and nothing else.
  Declared File Scopes (read from the task files this iteration, not assumed):
  - **A-005** (`TASKS/A-005.md`) — CREATE `data/receiver/BootReceiver.kt`,
    CREATE `test/data/scheduler/RearmAllTest.kt`, MODIFY `domain/scheduler/AlarmScheduler.kt`,
    MODIFY `data/scheduler/AlarmManagerAlarmScheduler.kt`.
  - **A-006** (`TASKS/A-006.md`) — CREATE `ui/fragment/alarms/component/AlarmsPermissionNotice.kt`,
    MODIFY `ui/fragment/alarms/AlarmsFragment.kt` / `AlarmsUiState.kt` / `AlarmsViewModel.kt` /
    `AlarmsViewModelTest.kt`.
  - Disjoint — confirmed by inspection, not by memory of an earlier plan note. Dispatch as one Phase,
    two Workers.
  - **A-007** (`.harness/knowledge/PROJECT.md` only) depends on all six tasks and stays unreachable
    until both land.
- **Queued decisions:** none. `ESCALATION.md` is empty.
- **Abandoned:** none. **Unreachable:** none.
- **Verified commands** — build/test/lint/screenshots run together in one `BUILD SUCCESSFUL` this
  iteration, twice (once before the Fresh-Context Review's fixes, once after):
  build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` | screenshots `./gradlew :app:validateDebugScreenshotTest`.
  Current baseline to compare against: **239 unit tests, 0 failures; lint 0 errors, 68 warnings; 23 of
  23 screenshot cases green.** A test count that does not move after adding tests means the task did
  not run.
  - `:app:kspDebugKotlin` is **not** in the granted capability set — `assembleDebug` is how you reach
    Room's generated `AppDatabase_Impl.kt` (needed for C-03).
  - `updateDebugScreenshotTest` stays ungranted except via a named goal-scoped entry in
    `capabilities.json`. D-003, D-004 and D-006's entries are all spent.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`. Pass the tier explicitly
  on every dispatch — omitting it silently inherits the Runtime's `-Model`.
- **Closed, not outstanding — do not re-raise:** the `org.gradle.java.home` pin question (removed,
  re-verified); `fallbackToDestructiveMigration` / D-005 (call removed, Constraint C-13 records the
  resolved state); D-006 (single-use screenshot grant, consumed, spent).
- **A-004's Fresh-Context Review found and fixed two MAJOR defects** (see `AMENDMENTS.md` A-12 for
  detail): a notification-tap navigation bug in `MainActivity` that replayed on activity recreation,
  and a missing `android.permission.VIBRATE` declaration. Both fixed this checkpoint — do not
  reintroduce either while touching those files.
- **A-005 must reconcile a known race, not fix it as a bug report.** `AlarmReceiver.onReceive` rebuilds
  the fired alarm from the intent's own extras rather than re-reading the table — a deliberate,
  task-blessed design (`onReceive` has seconds to live; reading Room means `goAsync()` and real
  machinery). The consequence: a delete confirmed in the sub-second window between the notify and the
  re-arm steps can resurrect a just-deleted alarm's schedule. A-005 introduces a `rearmAll` seam that
  reads the table on boot — this is the point where that race should be looked at again, not treated
  as new.
- **Watch for commits made outside the loop.** None since iteration 4 (`ee7b5c9`). `git log` against
  the last `loop(...)` commit is cheap; do it before trusting either this file or `STATE.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `skills-lock.json`,
  `.claude/`, `.agents/skills/`, `.harness/loop/`, and `SUGGESTIONS.html`, which has been dirty since
  before this run began. A human filling in an `ESCALATION.md` `## Decision` section between iterations
  is also expected, not debris — consume it per §6.2.
