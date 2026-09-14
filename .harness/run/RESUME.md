# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing. **A-001 and A-002 are complete**; the alarm vertical works end to end — FAB →
  editor → save → the row appears in the list, and it survives a restart. Nothing is blocked.
- **Next Phase:** **Phase 3 — A-003 alone** ("An alarm can be re-opened and changed, switched off, and
  deleted behind a confirmation"). Its Declared File Scope is the alarm/alarms/data files A-002 created,
  plus `AlarmDeleteConfirmSheet.kt` and its tests — read `TASKS/A-003.md` for the exact list. One task, so
  no disjointness check is needed. The chain is strictly linear from here: A-003 → A-004 → {A-005, A-006}
  → A-007. **A-005 and A-006 are the first real pairing candidate** (both depend only on A-004); check
  their scopes for disjointness then, since both name `AlarmScheduler.kt`.
- **Queued decisions:** 1 — **D-005** (`fallbackToDestructiveMigration(false)` enables destructive
  migration instead of disabling it). It **blocks nothing**: every remaining task is executable while it
  sits unanswered. Do not wait on it. Full text in `ESCALATION.md`, and the trap itself is written down as
  Constraint **C-13** so it survives whatever the answer is.
- **Abandoned:** none. **Unreachable:** none.
- **Verified commands** — all four run together in one `BUILD SUCCESSFUL` this iteration:
  build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` | screenshots `./gradlew :app:validateDebugScreenshotTest`.
  Current baseline to compare against: **177 unit tests, 0 failures; lint 0 errors, 66 warnings; all
  screenshot cases green.** A test count that does not move after adding tests means the task did not run.
  - `:app:kspDebugKotlin` is **not** in the granted capability set — `assembleDebug` is how you reach
    Room's generated `AppDatabase_Impl.kt` (needed for C-03).
  - Re-recording screenshots (`updateDebugScreenshotTest`) stays ungranted except via a named goal-scoped
    entry in `capabilities.json`. Both existing entries (D-003, D-004) are spent or unused; a new baseline
    needs a new Escalation Request.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`. Pass the tier explicitly on
  every dispatch — omitting it silently inherits the Runtime's `-Model`.
- **The toolchain needs no `org.gradle.java.home` pin.** It was removed outside the loop (`ee7b5c9`) and
  re-verified working this iteration. Ignore older notes saying it is load-bearing or awaiting relocation.
- **Watch for commits made outside the loop.** Three landed between iterations 3 and 4 and left `STATE.md`
  and this file describing a repository that no longer existed. `git log` against the last `loop(...)`
  commit is cheap; do it before trusting either file.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `skills-lock.json`,
  `.claude/`, `.agents/skills/`, `.harness/loop/`, and `SUGGESTIONS.html`, which has been dirty since
  before this run began.
