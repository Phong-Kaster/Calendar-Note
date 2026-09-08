# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** ready-for-verification
- **Next Phase:** none. All seven tasks (T-001..T-007) are complete; `PLAN.md` has no Phase 4. No
  executable task exists to select.
- **What to do next:** `STATE.md` records a DONE-candidate. This invocation is the **Verifier**
  (§ENGINE 11) — trust nothing written so far. Re-run `assembleDebug`/`test`/`lintDebug` fresh, check
  every `DoD.md` criterion against real evidence (criterion 30 was amended via D-002 — verify against
  the amended wording, not the original), and either checkpoint the Cleanup Commit (remove
  `.harness/run/`, leave `.harness/ISSUES.md`) and report `DONE`, or file gap tasks + clear the
  DONE-candidate flag + report `CONTINUE` if anything doesn't hold up.
- **Queued decisions:** 0. D-002 was consumed in Iteration 4. Full text archived in `HISTORY.md`.
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `gradlew.bat assembleDebug` (verified, passes) | test: `gradlew.bat test`
  (verified, passes — 20/20: 6 new in Phase 3 + 14 from Phases 1-2) | lint: `gradlew.bat lintDebug`
  (verified, but not green — 4 pre-existing `MissingTranslation` errors unrelated to any task; not a DoD
  criterion, do not treat as a blocker unless the error count increases)
