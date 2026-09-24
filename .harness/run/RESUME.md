# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on disagreement with task files or
> git, this file is wrong. Keep it small.

- **Stage:** verifying — all tasks T-001…T-004 complete (T-004 in iteration 3). DoD APPROVED.
- **Run Mode:** Autonomous
- **Next Phase:** none. `STATE.md` records **PARTIAL-candidate** → this invocation is the Verifier (§11 step 1, §14.4):
  re-prove `machine` criteria 1–5, 8, 9, 15 by fresh commands; write the "Awaiting a person" checklist for
  `human` criteria 6, 7, 10, 11, 12, 13, 14 into `.harness/ISSUES.md`; report `DONE_PARTIAL` (no Cleanup Commit).
- **Queued decisions:** 0
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` (34 tests) | lint: `./gradlew :app:lintDebug` (0 errors) | device: `~/AppData/Local/Android/Sdk/platform-tools/adb` (was disconnected at end of iteration 3; no `pm grant`, no input)
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
