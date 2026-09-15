# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads.
> A derived cache, never a source of truth. On any disagreement with the task files or git, this file is
> the one that is wrong: correct it and trust the source.

- **Stage:** verified (machine) — awaiting human sign-off. `STATE.md` still records **DONE-candidate: yes**,
  so the next invocation is the **Verifier** again (ENGINE.md §11).
- **Next Phase:** none. Phase 1 (T-001, T-002) was the whole task graph and both tasks are complete.
  Iteration 3 re-proved all fourteen `machine` criteria fresh and found no gaps — the per-criterion table
  is in `STATE.md`. **Do not re-run the whole machine sweep from scratch unless the tree has changed since
  commit `loop(verify)`:** what blocks `DONE` now is human sign-off, not machine evidence. The next
  invocation's job: consume **D-002** if it has been answered, and on an all-pass answer create the Cleanup
  Commit (remove `.harness/run/`, keep `.harness/ISSUES.md`) and report `DONE`. Any **fail** is an ordinary
  discovery — reconcile it into a task, clear the DONE-candidate flag, report `CONTINUE`.
- **Queued decisions:** 1 — **D-002**, a Human Verification Request covering the seven unsigned `human`
  criteria (DoD 15-21). It blocks no task (none remains); it blocks the `DONE` report itself.
- **Abandoned:** none. **Unreachable:** none.
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  (all standing-granted in `.harness/knowledge/capabilities.json`). Iteration 3's own fresh run:
  `BUILD SUCCESSFUL`, 288 tests / 0 failures across 18 classes, lint 0 errors / 75 warnings,
  screenshots 23/23.
  **Trap:** a re-run on an unchanged tree returns `BUILD SUCCESSFUL` with everything `UP-TO-DATE`, and
  `--rerun` does **not** force `:app:testDebugUnitTest` — only `--rerun-tasks` does. A task line without an
  `UP-TO-DATE` marker is what says the evidence is yours. Full note in `.harness/knowledge/PROJECT.md`.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
