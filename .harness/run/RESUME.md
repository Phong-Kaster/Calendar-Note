# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing. **A-001 and A-002 are complete.** **A-003 is implemented, reviewed and fully
  machine-evidenced, but blocked from "complete" by D-006** (a screenshot-reference grant for one
  brand-new `@PreviewTest` case, the delete confirmation — no prior reference). `AlarmRow`'s two switch
  states were deliberately **not** pinned: its time display is host-dependent, so a pinned reference
  would only be valid on the host it was recorded on (see D-006's own text). D-005 was consumed this
  iteration (the destructive-migration call is removed; Constraint C-13 now records the resolved state).
- **Next Phase:** **none executable.** A-004 → {A-005, A-006} → A-007 all depend on A-003 transitively
  and stay unreachable until D-006 is answered. Once D-006 is answered and the three references exist and
  validate, mark A-003 complete and select Phase 4 — **A-004 alone**
  (`domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`,
  `res/drawable/ic_notification_alarm.xml`, `AlarmRepositoryImpl.kt`, own tests — see `TASKS/A-004.md`).
  A-005 and A-006 (both depending only on A-004) are the first genuine pairing candidate — check their
  Declared File Scopes for disjointness then, since both touch `AlarmScheduler.kt`.
- **Queued decisions:** 1 — **D-006** (goal-scoped `updateDebugScreenshotTest` grant, restricted to
  `*AlarmDeleteConfirmationCase*`). It **blocks A-003's completion, and every task after it
  transitively** — this is why nothing is executable right now. Full text in `ESCALATION.md`.
- **Abandoned:** none. **Unreachable:** none yet (A-004 … A-007 are blocked-by-decision-dependency, not
  abandoned — they become reachable the moment D-006 is answered and A-003 completes).
- **Verified commands** — build/test/lint run together in one `BUILD SUCCESSFUL` this iteration; screenshot
  validation run separately once it started failing (PROJECT.md's "one failing task aborts the others"):
  build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` | screenshots `./gradlew :app:validateDebugScreenshotTest`.
  Current baseline to compare against: **206 unit tests, 0 failures; lint 0 errors, 68 warnings; 22 of 23
  screenshot cases green** (the 1 red one is exactly D-006's case, and is expected to stay red until
  D-006 is answered and the reference is recorded). A test count that does not move after adding tests
  means the task did not run.
  - `:app:kspDebugKotlin` is **not** in the granted capability set — `assembleDebug` is how you reach
    Room's generated `AppDatabase_Impl.kt` (needed for C-03).
  - Re-recording screenshots (`updateDebugScreenshotTest`) stays ungranted except via a named goal-scoped
    entry in `capabilities.json`. D-003 and D-004's entries are spent or unused; D-006 needs a fresh one.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run is Capable.** No Fast dispatch exists in `PLAN.md`. Pass the tier explicitly on
  every dispatch — omitting it silently inherits the Runtime's `-Model`.
- **The toolchain needs no `org.gradle.java.home` pin.** Closed, not outstanding — see `HISTORY.md`
  iteration 4 and `PROJECT.md` § Environmental Facts.
- **`fallbackToDestructiveMigration` is resolved, not outstanding.** D-005 was answered and applied this
  iteration; Constraint C-13 records the resolved state. Do not re-raise it.
- **Watch for commits made outside the loop.** Three landed between iterations 3 and 4; none since. `git
  log` against the last `loop(...)` commit is cheap; do it before trusting either this file or `STATE.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `skills-lock.json`,
  `.claude/`, `.agents/skills/`, `.harness/loop/`, and `SUGGESTIONS.html`, which has been dirty since
  before this run began. A human filling in an `ESCALATION.md` `## Decision` section between iterations is
  also expected, not debris — consume it per §6.2.
