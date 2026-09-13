# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** escalated — bootstrap complete, waiting on the DoD gate
- **Next Phase:** Phase 1 — A-001, **not selectable** (blocked by D-001 and D-002)
  - A-001 — scope: `ui/fragment/alarms/{AlarmsFragment,AlarmsUiState,AlarmsViewModel}.kt`,
    `ui/fragment/alarms/component/AlarmsEmptyState.kt`, `res/drawable/ic_bottom_alarm.xml`
- **Queued decisions:** 3
  - D-001 (DoD approval + standing toolchain capabilities) → blocks **A-001 … A-007**, the whole run
  - D-002 (AS-5: fourth bottom-bar tab, and what the centre "+" means there) → blocks A-001, A-002, A-006
  - D-003 (goal-scoped `updateDebugScreenshotTest`) → blocks A-001's **completion** only
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` |
  lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  — **all four verified by a previous run but NOT currently granted.** `.harness/knowledge/capabilities.json`
  does not exist; the old ledger sits at `knowledge/capabilities.json`, which the runtime no longer reads.
  This is what D-001 re-installs. Do not attempt them before D-001 is answered.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `PRD.md` (the human's
  addendum), `skills-lock.json`, `.claude/`, `.agents/skills/`, `.harness/loop/`.
