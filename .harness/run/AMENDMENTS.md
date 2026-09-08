# AMENDMENTS

> Tier-1 plan mutations (split/merge/reorder/re-group/add-prerequisite/remove-obsolete), logged as they
> happen. PRD/DoD/architecture are unchanged by definition of Tier 1 — anything bigger is a queued
> decision in `ESCALATION.md`, not an entry here.

### Bootstrap - 2026-09-08

- Split the analysis-proposed "T-001" (To-do add/list bundled with the blue-theme/dynamic-color change)
  into two tasks: `T-001` (theme only) and `T-002` (To-do add/list only). Reason: the theme change was the
  only thing forcing the Calendar vertical to depend on the To-do vertical; splitting it lets three tasks
  (`T-001`, `T-002`, `T-005`) share Phase 1 with genuinely disjoint file scopes instead of two Phases with
  a false dependency.
- Folded the analysis-proposed "dates-with-notes marker" task into the note-create/date-scoped-read task
  (now `T-006`). Reason: it touched the same five files as note-create with no independently observable
  behavior of its own (a query + a UI flag is not a user-visible checkpoint on its own) — the critique
  role flagged this as a thin layer-slice in disguise.
- Renumbered the resulting seven tasks sequentially (`T-001`..`T-007`) for the final `PLAN.md`/`TASKS/`.
