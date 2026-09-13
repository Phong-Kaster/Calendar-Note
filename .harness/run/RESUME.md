# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing — bootstrap complete, all three decisions consumed (iteration 2, 2026-09-13).
  `ESCALATION.md` is empty; every task file's `Blocked by decision` line is cleared and each is `pending`.
- **Next Phase:** Phase 1 — A-001
  - A-001 — scope: `ui/fragment/alarms/{AlarmsFragment,AlarmsUiState,AlarmsViewModel}.kt`,
    `ui/fragment/alarms/component/AlarmsEmptyState.kt`, `res/drawable/ic_bottom_alarm.xml`
  - **Not yet selected.** Iteration 2 stopped before dispatching any Worker because the build toolchain
    itself does not run here — see below. A fresh iteration must re-confirm `./gradlew --version` before
    treating A-001 as executable.
- **Queued decisions:** 0. Nothing blocks any task on decision grounds.
- **Abandoned:** none. **Unreachable:** none.
- **Blocking condition (not a decision, not a capability gap):** `./gradlew --version` fails in this
  environment with `JAVA_HOME is not set and no 'java' command could be found in your PATH`.
  `gradle.properties` sets no `org.gradle.java.home`. The capability grant itself is correctly installed
  (`.harness/knowledge/capabilities.json` carries the standing gradlew/lint/screenshot blocks; confirmed
  present in iteration 2) — this is the environment underneath the grant, not the permission. Iteration 1's
  `RESUME.md` pre-answered this exact scenario: if Gradle is still denied/non-functional after the ledger
  is installed, that is `FAILED` (execution broken), not a new queued decision. **A fresh iteration should
  re-run `./gradlew --version` first.** If it now succeeds (human installed a JDK / set `JAVA_HOME` /
  set `org.gradle.java.home`), proceed to select and dispatch Phase 1 (A-001) normally. If it still fails,
  report `FAILED` again without re-litigating this as a decision.
- **Verified commands (once the JDK is available):** build: `./gradlew :app:assembleDebug` | test:
  `./gradlew :app:testDebugUnitTest` | lint: `./gradlew :app:lintDebug` | screenshots:
  `./gradlew :app:validateDebugScreenshotTest` | re-record (goal-scoped, narrow, only
  `BottomBar_*`/`BottomBarSystemNight_*`): `./gradlew :app:updateDebugScreenshotTest`.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `PRD.md` (the human's
  addendum), `skills-lock.json`, `.claude/`, `.agents/skills/`, `.harness/loop/`.
