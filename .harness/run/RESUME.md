# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing. **A-001 through A-005 are complete.** A-006 is code-complete (banner, ViewModel
  wiring, re-arm-on-grant, tests all written and green) but not marked complete — it is blocked purely
  on D-007, a screenshot capability grant for one brand-new preview case.
- **No executable task exists this run.** A-006 cannot proceed further without a human answer to D-007.
  A-007 depends on A-005 **and** A-006 and stays unreachable until A-006 completes. There is nothing to
  select and nothing to dispatch until D-007 is answered.
- **Queued decisions: D-007** (in `ESCALATION.md`), blocking A-006's completion (and transitively A-007).
  Not blocking A-006's *selectability* — there is no more code to write for it — only its "screenshots
  green" acceptance criterion.
- **If D-007 is answered "grant it" (option 1):** run exactly
  `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsPermissionNoticeCase*"` once, using the
  goal-scoped entry the human adds to `.harness/run/capabilities.json`. Report the before/after state of
  `app/src/screenshotTestDebug/reference/` for `AlarmsScreenshotTestKt` (empty for this case before, one
  file after — check with the live-hash grep in `.harness/knowledge/PROJECT.md` § `updateDebugScreenshotTest`).
  Then re-verify all four commands together, mark A-006 complete, and A-007 becomes selectable.
- **If D-007 is answered "refuse" (option 2):** A-006 stays permanently blocked on that one criterion;
  escalate again once no other option exists, or ask the human to record the reference manually.
- **Abandoned:** none. **Unreachable:** A-007 (depends on A-006).
- **Verified commands** — build/test/lint run together this iteration, twice (once before the
  Fresh-Context Review's fixes, once after):
  build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` | screenshots `./gradlew :app:validateDebugScreenshotTest`.
  Current baseline to compare against: **251 unit tests, 0 failures; lint 0 errors, 72 warnings; 22 of
  23 screenshot cases green** (the one exception is `AlarmsPermissionNoticeCase`, expected — see D-007).
  A test count that does not move after adding tests means the task did not run.
  - `:app:kspDebugKotlin` is **not** in the granted capability set — `assembleDebug` is how you reach
    Room's generated `AppDatabase_Impl.kt` (needed for C-03).
  - `updateDebugScreenshotTest` stays ungranted except via a named goal-scoped entry in
    `capabilities.json`. D-003, D-004 and D-006's entries are all spent. D-007 is the fourth such
    request in this run — same shape each time.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`. Pass the tier explicitly
  on every dispatch — omitting it silently inherits the Runtime's `-Model`.
- **Closed, not outstanding — do not re-raise:** the `org.gradle.java.home` pin question (removed,
  re-verified); `fallbackToDestructiveMigration` / D-005 (call removed, Constraint C-13 records the
  resolved state); D-006 (single-use screenshot grant, consumed, spent).
- **Phase 5's Fresh-Context Review found and fixed six defects** (see `AMENDMENTS.md` A-14 for detail):
  two MAJOR — granting the exact-alarm permission back was not re-arming anything (`AlarmsViewModel`
  now takes `AlarmScheduler` and re-arms on the false→true transition of `exactAlarmGranted` only), and
  `RearmAllTest` was exercising a default-interface body that `AlarmManagerAlarmScheduler` no longer
  overrides (the override was removed; there is exactly one `rearmAll` implementation now, the tested
  one) — and four MINOR (settings deep-links landing one screen short of the named switch, now
  `ACTION_APP_NOTIFICATION_SETTINGS`/`EXTRA_APP_PACKAGE` and a `package:` data URI respectively;
  swallowed failures now logged; `BootReceiver`'s `goAsync()` now bounded by
  `withTimeoutOrNull(8_000L)`). Constraints **C-14** (settings deep-link specificity) and **C-15**
  (`HomeRequestPermission.kt`'s `requestExactAlarm` is a local function, not importable) added to
  `.harness/knowledge/PROJECT.md`. Do not reintroduce any of the six while touching those files.
- **Watch for commits made outside the loop.** None since iteration 4 (`ee7b5c9`). `git log` against
  the last `loop(...)` commit is cheap; do it before trusting either this file or `STATE.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `skills-lock.json`,
  `.claude/`, `.agents/skills/`, `.harness/loop/`, and `SUGGESTIONS.html`, which has been dirty since
  before this run began. A human filling in an `ESCALATION.md` `## Decision` section between iterations
  is also expected, not debris — consume it per §6.2.
