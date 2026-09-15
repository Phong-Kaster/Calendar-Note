# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads.
> A derived cache, never a source of truth. On any disagreement with the task files or git, this file is
> the one that is wrong: correct it and trust the source.

- **Stage:** executing — Phase 2 (T-003) complete and checkpointed. `STATE.md` records
  **DONE-candidate: yes**, set fresh by iteration 4, so the next invocation is the **Verifier**
  (ENGINE.md §11) and it wrote none of this implementation.
- **Next Phase:** none. The task graph is empty: T-001, T-002, T-003 all complete, nothing abandoned,
  nothing unreachable, nothing deferred.
- **What the next invocation actually has to do:**
  1. **Re-prove all fourteen `machine` criteria yourself** (§11.1). Do **not** reuse iteration 3's table —
     the tree changed under it. `GreetingNotifier.kt`, `HomeFragment.kt`, `README.md` changed and
     `domain/greeting/GreetOnceADay.kt` + its test are new, so criteria 4, 5, 6 and 14 in particular need
     your own eyes.
  2. Then queue **one** consolidated Human Verification Request for criteria **15, 16, 17, 18, 19, 20**
     and report `ESCALATE`. **Criterion 21 is signed and stays signed** — do not re-ask it; T-003 touched
     neither the manifest nor the permission code. Re-asking a settled question is the failure mode §7
     names by example.
  3. Write items 19 and 20 to be done **by hand on the phone**. The last attempt failed to automate them:
     the device refuses injected input (`SecurityException: Injecting input events requires ...
     INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate). Item 18 needs the system date moved
     forward a day on what is the human's daily-driver phone — say so, and let them decide.
  4. Criterion 15's re-check must name its precondition explicitly: **app data cleared, permission granted
     when asked.** The last pass recorded on it came from a device where the permission was already
     granted — the other branch — and that is how the bug shipped.
- **Queued decisions:** none. D-002 was consumed this iteration (answered in part; 18/19/20 carried into
  the next request rather than left dangling). Nothing blocks any task.
- **Abandoned:** none. **Unreachable:** none.
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest`
  | lint: `./gradlew :app:lintDebug` | screenshots: `./gradlew :app:validateDebugScreenshotTest`
  (all standing-granted in `.harness/knowledge/capabilities.json`). Iteration 4's own fresh run:
  `BUILD SUCCESSFUL`, **296 tests / 0 failures across 19 classes**, lint 0 errors / 75 warnings,
  screenshots 23/23 with no orphans.
  **Trap:** a re-run on an unchanged tree returns `BUILD SUCCESSFUL` with everything `UP-TO-DATE`, and
  `--rerun` does **not** force `:app:testDebugUnitTest` — only `--rerun-tasks` does. A task line without an
  `UP-TO-DATE` marker is what says the evidence is yours. Full note in `.harness/knowledge/PROJECT.md`.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `sonnet` | capable: `opus`
