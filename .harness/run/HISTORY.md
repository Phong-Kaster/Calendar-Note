# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

### Iteration 1 — 2026-09-13 — bootstrap recovery and completion

- **Phase:** none — no implementation task was executable, and none was attempted.
- **Attempted:** finishing an interrupted Bootstrap. `.harness/run/` existed on entry holding exactly four
  files — `DoD.md`, `PLAN.md`, `TASKS/A-001.md`, `TASKS/A-002.md` — all untracked, with no `STATE.md`. No
  `STATE.md` means no Iteration had ever run, so this was crashed bootstrap debris rather than a crashed
  iteration.
- **Recovery decision (§6.1):** salvaged, not reverted. The four documents are internally consistent, refer
  to the same constraint ids (C-01…C-12) and the same decision id (D-001), and both task files declare
  themselves blocked by a D-001 that had never been written. Reverting would have thrown away the whole
  bootstrap analysis fan-out to re-derive the same conclusions. Written up as an assumption in `STATE.md`,
  because it is a judgement about work this process cannot see.
- **Authored this iteration:** `TASKS/A-003.md` … `TASKS/A-007.md`, `ESCALATION.md` (D-001, D-002, D-003),
  `STATE.md`, `RESUME.md`, `AMENDMENTS.md`, this file, and `.harness/ISSUES.md`.
- **Learned:**
  - The engine holds **no build, test or lint capability in this repository at all**. The previous run's
    standing ledger is at `knowledge/capabilities.json`; the runtime reads `.harness/knowledge/capabilities.json`,
    which does not exist. Confirmed by inspection, not inferred: `.harness/knowledge/` contains only
    `PROJECT.md`. This was already written down as `PLAN.md` risk 7 and is now half of D-001.
  - `PROJECT.md` claims `.harness/ISSUES.md` exists and cites it as evidence for C-01's "43 surviving colour
    literals". It did **not** exist. The file is created this iteration and carries that entry forward from
    the previous run's `knowledge/ISSUES.md` rather than dropping it.
  - `PLAN.md` put `.harness/ISSUES.md` inside A-007's Declared File Scope, which collides with the
    Iteration's own §6.10 obligation to regenerate that file in every checkpoint. Amended (A-1).
- **Reconciled:**
  - capability gap → queued decision (D-001), blocking all seven tasks
  - AS-5, the one assumption the bootstrap flagged as genuinely uncertain → queued decision (D-002)
  - the predicted reference-image invalidation → queued decision (D-003), blocking A-001's completion only
  - A-007 scope collision → Tier-1 amendment (A-1), `PLAN.md` corrected
  - missing `ISSUES.md` → created, not escalated
- **Outcome:** no executable task remains and three decisions are queued → `ESCALATE`. This is the DoD
  gate firing naturally, which is what §5 predicts for the end of a Bootstrap.

## Archived Decisions

<!-- Full request + decision + rationale of every consumed Decision Queue entry. -->

None consumed yet.
