# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing — the toolchain works in this environment (confirmed iteration 3:
  `./gradlew --version` succeeds). A-001 is implemented, wired, and checkpointed; its completion is
  blocked on **D-004** only (a goal-scoped grant to record one new screenshot reference).
- **Next Phase:** none executable. Phase 1 (A-001) is done except for D-004's evidence; Phase 2 (A-002)
  and everything after depends on A-001 being complete, so nothing is selectable until D-004 is answered.
  Once it is: consume it per §6.2 (run
  `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsEmptyStateCase*"`, confirm via
  `git status` that exactly one new reference file appears under
  `app/src/screenshotTestDebug/reference/com/example/skeleton/screenshot/AlarmsScreenshotTestKt/` and
  nothing else changes, re-run the full four-command verification, mark A-001 complete, then select
  Phase 2 (A-002) normally.
- **Queued decisions:** 1 — **D-004**, blocking A-001's completion (and therefore every later task
  transitively). Full text in `ESCALATION.md`.
- **Abandoned:** none. **Unreachable:** none (A-002 … A-007 are merely not yet selectable, not
  unreachable — they depend on a task that is implemented, not on one that failed).
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest` |
  re-record (goal-scoped only, never speculative): `./gradlew :app:updateDebugScreenshotTest --tests
  "<name>"` — the `--tests` filter works on this task and is how both D-003 and the pending D-004 stay
  narrow to the named case(s) rather than re-recording the whole class.
  All four verification commands were run together in one `BUILD SUCCESSFUL` this iteration: 138 unit
  tests (0 failures), lint 0 errors / 64 warnings, 21 of 21 *existing* screenshot cases green. The one
  failing case (`AlarmsEmptyStateCase`, no reference yet) is exactly what D-004 is for.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `PRD.md` (the human's
  addendum), `skills-lock.json`, `.claude/`, `.agents/skills/`, `.harness/loop/`. `SUGGESTIONS.html` has
  also been dirty since before this run began and is untouched by it.
- **`gradle.properties` now carries a machine-specific `org.gradle.java.home`** (added outside the loop,
  between iterations 2 and 3). This iteration only fixed its escaping for `lintDebug`. See
  `.harness/knowledge/PROJECT.md` § Environmental Facts and `.harness/ISSUES.md` for the full note — it is
  flagged for the human to relocate, not blocking anything.
