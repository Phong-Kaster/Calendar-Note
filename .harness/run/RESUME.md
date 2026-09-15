# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads.
> A derived cache, never a source of truth. On any disagreement with the task files or git, this file is
> the one that is wrong: correct it and trust the source.

- **Stage:** verified — awaiting human sign-off. Iteration 5 ran as the Verifier, re-proved all fourteen
  `machine` criteria against its own fresh evidence, queued `D-003`, and reported `ESCALATE`. It wrote no
  source file, so the DONE-candidate in `STATE.md` **stands and stays fresh**.
- **Next Phase:** none. The task graph is empty: T-001, T-002, T-003 all complete, nothing abandoned,
  nothing unreachable, nothing deferred.
- **What the next invocation actually has to do:**
  1. **Consume `D-003` from `DECISIONS.md` (§6.2).** It is the only queued entry.
  2. Every one of criteria **15-20** answered **pass** → sign them in `STATE.md` with the date, create the
     **Cleanup Commit** (remove `.harness/run/` from the branch tip; `.harness/ISSUES.md` stays), and
     report `DONE`. Criterion 21 is already signed and needs nothing.
  3. Any **fail** → an ordinary discovery (§8): file a task, **clear the DONE-candidate**, checkpoint,
     report `CONTINUE`. That is what happened to D-002's criterion 15 and it worked correctly.
  4. Partially answered → re-raise **only the still-unanswered items** as `D-004`, and **never re-ask
     anything already signed**. §7's worked example of the failure mode is this very run asking for the
     same screenshot grant four times.
  5. **Do not re-run the machine verification unless the tree changed.** If `git log` shows no commit
     touching `app/` since `8b67f40`, iteration 5's table in `STATE.md` is still about this tree. If
     anything under `app/` did change, it is stale and you re-prove all fourteen yourself.
- **Queued decisions:** **one — `D-003`**, the consolidated Human Verification Request for criteria 15-20.
  It blocks the `DONE` report and no task (there are none left to block).
- **Abandoned:** none. **Unreachable:** none.
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  (all standing-granted in `.harness/knowledge/capabilities.json`). Iteration 5's own fresh run, with
  `--rerun-tasks`: `BUILD SUCCESSFUL in 1m 8s`, **61 actionable tasks: 61 executed**, **296 tests / 19
  classes / 0 failures / 0 errors**, lint 0 errors / 75 warnings, screenshots 23 rendered / 0 diffs /
  no orphans.
  **Trap:** a re-run on an unchanged tree returns `BUILD SUCCESSFUL` with everything `UP-TO-DATE`, and
  `--rerun` does **not** force `:app:testDebugUnitTest` — only `--rerun-tasks` does. A task line without an
  `UP-TO-DATE` marker is what says the evidence is yours. Full note in `.harness/knowledge/PROJECT.md`.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
