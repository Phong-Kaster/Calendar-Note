# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads.
> A derived cache, never a source of truth. On any disagreement with the task files or git, this file is
> the one that is wrong: correct it and trust the source.

- **Stage:** **complete.** Iteration 6 consumed `D-003` (all six items pass), re-proved all fourteen
  `machine` criteria on its own fresh Gradle run, signed criteria 15-20, created the Cleanup Commit and
  reported `DONE`. Nothing remains for another iteration.
- **Next Phase:** none. T-001, T-002, T-003 all complete; nothing abandoned, unreachable or deferred.
- **Queued decisions:** none. `D-001`, `D-002` and `D-003` are all answered and consumed.
- **Abandoned:** none. **Unreachable:** none.
- **If you are reading this, something is wrong.** The Cleanup Commit removes `.harness/run/` from the
  branch tip, so a live copy of this file at the tip means either the Cleanup Commit did not land or a new
  run was bootstrapped over the old state. Check `git log` before acting on anything here: the last
  commit of this run is the completion summary and lists every criterion with its evidence.
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  Iteration 6's own run, with `--rerun-tasks`: `BUILD SUCCESSFUL in 3m 11s`, **61 actionable tasks: 61
  executed**, **296 tests / 19 classes / 0 failures / 0 errors**, lint 0 errors / 75 warnings,
  screenshots 23 rendered / 0 diffs / 23 references on disk / no orphans.
  **Trap:** a re-run on an unchanged tree returns `BUILD SUCCESSFUL` with everything `UP-TO-DATE`, and
  `--rerun` does **not** force `:app:testDebugUnitTest` — only `--rerun-tasks` does. A task line without an
  `UP-TO-DATE` marker is what says the evidence is yours. Full note in `.harness/knowledge/PROJECT.md`.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
