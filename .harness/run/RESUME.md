# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing — bootstrap complete, **all three decisions answered and approved** (2026-09-13,
  after iteration 1's checkpoint). Iteration 2 must consume them at §6.2: apply, log to `AMENDMENTS.md`,
  archive into `HISTORY.md`, and clear the `Blocked by decision` line on every task file.
- **Next Phase:** Phase 1 — A-001
  - A-001 — scope: `ui/fragment/alarms/{AlarmsFragment,AlarmsUiState,AlarmsViewModel}.kt`,
    `ui/fragment/alarms/component/AlarmsEmptyState.kt`, `res/drawable/ic_bottom_alarm.xml`
- **Queued decisions:** 0 unanswered. All three decided:
  - D-001 → **approved as written.** DoD approved, `machine`/`human` split approved, AS-1…AS-11 approved,
    **AS-4 kept**. All three capability blocks granted. `DoD.md` is now immutable to the engine.
  - D-002 → **option 1.** Alarms is the fourth bottom-bar tab; `CoreBottomBar`'s centre "+" is hidden on
    the Alarms screen only; a dedicated FAB creates the alarm. AS-5 stands as written.
  - D-003 → **granted, goal-scoped.** Re-record **only** `BottomBar_*` and `BottomBarSystemNight_*`, and
    report the live-hash list **before and after** so an unexpected third file is visible in the checkpoint.
    Must not be copied into the standing ledger.
- **⚠ The grants are approved but NOT INSTALLED.** Neither `.harness/knowledge/capabilities.json` nor
  `.harness/run/capabilities.json` exists on disk. The runtime compiles permissions from those files, and
  the engine may never write a ledger (§12). **Until a human creates them, `./gradlew` stays denied and no
  task can be evidenced**, regardless of the approval text above. Iteration 2 must test this before
  selecting work rather than assuming the approval took effect — and if still denied, that is a `FAILED`
  (execution broken), not a queued decision: the question has been answered.
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` |
  lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  | re-record (goal-scoped, D-003, narrow): `./gradlew :app:updateDebugScreenshotTest`
  — verified by a previous run; granted in writing, pending ledger installation per the warning above.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `PRD.md` (the human's
  addendum), `skills-lock.json`, `.claude/`, `.agents/skills/`, `.harness/loop/`.
