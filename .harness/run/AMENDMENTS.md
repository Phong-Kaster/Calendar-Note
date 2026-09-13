# AMENDMENTS

> Every plan mutation, logged. Tier 1 is automatic and recorded here. Tier 2 arrives here only after the
> human answered the decision that authorised it; Tier 3 never arrives here at all, because intent is not
> the engine's to amend.

<!-- Newest first. -->

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
