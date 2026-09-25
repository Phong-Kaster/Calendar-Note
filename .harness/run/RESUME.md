# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on any disagreement with the task
> files or with git, this file is the one that is wrong.

- **Stage:** awaiting human verification — DONE-candidate (iteration 4); Verifier (iteration 5) re-proved
  DoD #1–#7 (`machine`) with fresh evidence.
- **Next Phase:** none. Next invocation: consume D-003 (§6.2). All 14 rows PASS → record sign-offs in
  `STATE.md`, act as Verifier again (§11 step 4: Cleanup Commit, `DONE`). Any FAIL → reconcile into tasks.
- **Queued decisions:** 1 — D-003 (Human Verification Request, DoD #8–#16); blocks `DONE` only, no task
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` (re-verified iteration 5 with `--rerun`; run unpiped, `cd` in its own Bash call; with
  `--quiet` a clean exit prints nothing). Install `./gradlew :app:installDebug` (device `b56e2819`, iteration 4).
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
