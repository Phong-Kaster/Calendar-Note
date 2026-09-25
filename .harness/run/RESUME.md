# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on any disagreement with the task
> files or with git, this file is the one that is wrong.

- **Stage:** awaiting Cleanup Commit — iteration 6 Verifier re-proved DoD #1–#7; #8–#16 signed off via D-003.
- **Next Phase:** none. Next invocation: consume D-004. If granted → Verifier: re-run build/test/lint (no code
  changed since `6b8c80e`), `git rm -r -q .harness/run`, Cleanup Commit (message drafted in HISTORY iteration 6),
  `DONE`. If the human removed `.harness/run/` by hand → verify the tip, `DONE`.
- **Queued decisions:** 1 — D-004 (`git rm` capability); blocks the Cleanup Commit / `DONE` only, no task
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` |
  lint `./gradlew :app:lintDebug` | install `./gradlew :app:installDebug` (device `b56e2819`). Run `cd` in
  its own Bash call.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
