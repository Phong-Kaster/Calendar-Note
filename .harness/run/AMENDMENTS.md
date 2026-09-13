# AMENDMENTS

> Every plan mutation, logged. Tier 1 is automatic and recorded here. Tier 2 arrives here only after the
> human answered the decision that authorised it; Tier 3 never arrives here at all, because intent is not
> the engine's to amend.

<!-- Newest first. -->

### A-4 — D-003 consumed: goal-scoped `updateDebugScreenshotTest` grant applied (Tier 2)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** A-001's completion is no longer blocked. `.harness/run/capabilities.json` already
  carried the granted entry (written by the human at the end of iteration 1); this iteration consumes
  the decision itself — clears A-001's `Blocked by decision` line and archives the full exchange into
  `HISTORY.md`.
- **Why:** The human approved option 1 as written: re-record only `BottomBar_*` and
  `BottomBarSystemNight_*`, reporting the live-hash list before and after. That report is owed the first
  time A-001's Phase actually triggers a re-record — not at consumption time, since no re-record has run
  yet.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2.

### A-3 — D-002 consumed: Alarms is the fourth tab; centre "+" hidden there (Tier 2)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** A-001, A-002, A-006 unblocked. AS-5 stands as written — no change to `PLAN.md` or any
  task file's Declared File Scope or Description, because both already assumed this outcome (the fourth
  tab, the hidden centre "+", the dedicated FAB) when they were authored in iteration 1. Consumption here
  is bookkeeping: the decision that made that assumption load-bearing is now on record as answered.
- **Why:** The human chose option 1 of D-002 — the only reading that honors the addendum's explicit ask
  for a floating action button without putting two "create" controls on one screen.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2.

### A-2 — D-001 consumed: DoD approved, standing capabilities installed (Tier 2 / Tier 3 mixed)

- **Iteration:** 2
- **Date:** 2026-09-13
- **What changed:** All seven tasks (A-001 … A-007) unblocked. `DoD.md` is now immutable to the engine
  per ENGINE.md §5. `.harness/knowledge/capabilities.json` already carried the three approved standing
  blocks (gradlew assemble/compile/test/lint, `validateDebugScreenshotTest`, and the
  `MSYS_NO_PATHCONV` git-show/ls-tree workaround), written by the human at the end of iteration 1.
  AS-4 (the per-alarm on/off switch) is kept, as the human's decision directed.
- **Why:** The human approved the DoD, its Verification Class split, and the Assumptions table as
  written — option 1 of D-001 — and approved the capability proposal as written.
- **Conditions met:** answered decision, named tasks unblocked per ENGINE.md §6.2. The DoD-approval half
  of this decision touches what a human may change forever (Tier 3, never the engine's to make) but is
  logged here because §6.2 requires every consumed decision logged, regardless of the tier of the
  underlying question.

### A-1 — A-007's Declared File Scope loses `.harness/ISSUES.md` (Tier 1)

- **Iteration:** 1
- **Date:** 2026-09-13
- **What changed:** `PLAN.md`'s Task Graph gave A-007 the scope
  `.harness/knowledge/PROJECT.md`, `.harness/ISSUES.md`. The second path is removed; A-007 now writes
  `PROJECT.md` only, and `.harness/ISSUES.md` stays Iteration-owned. `PLAN.md`'s Phase Grouping row for
  Phase 6 gains `.harness/ISSUES.md` and `README.md` as the Iteration's shared files.
- **Why:** ENGINE.md §6.10 and §10 require the Iteration to regenerate `.harness/ISSUES.md` in **every**
  checkpoint. A Worker holding it in scope would write the same file the Iteration writes in the same
  commit — precisely the collision the §6.6 scope check exists to catch, and it would have fired on the
  last Phase of the run after six clean ones.
- **Conditions met:** PRD, DoD and architecture unchanged. No task's behaviour changes; only who holds
  the pen on one file.
