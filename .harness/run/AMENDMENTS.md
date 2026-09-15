# AMENDMENTS

> Tier-1 plan mutations, logged as they happen: split/merge/reorder tasks, re-group Phases, add
> prerequisites, remove obsolete tasks. PRD, DoD, and architecture unchanged.

### 2026-09-15 - D-001 consumed: VIBRATE stays declared

Following the human's D-001 answer (`.harness/run/DECISIONS.md`), applied the DoD change and its
downstream task edits — a decision consumption (ENGINE.md §6.2), not a self-initiated Tier-1/2 mutation:

- `DoD.md`: criterion 9 no longer lists `android.permission.VIBRATE` among the removed permissions (it is
  now in the "still declares" list); old criterion 14 (`AlarmNotifier.kt` KDoc correction) and old
  criterion 23 (human check for alarm heads-up after VIBRATE removal) are dropped entirely; criterion 8
  simplified to say `AlarmNotifier` is wholly unchanged (constants, behavior, and KDoc); all criteria after
  the drop points renumbered to close the gaps (old 15-22 → new 14-21); Status marked APPROVED.
- `PLAN.md`: T-002's task-graph line and title changed from "three unused permissions" to "two unused
  location permissions"; dropped `data/notification/AlarmNotifier.kt` from its scope; the VIBRATE risk in
  Known Risks marked closed.
- `TASKS/T-002.md`: title, Declared File Scope (drops `AlarmNotifier.kt` entirely, now genuinely out of
  scope), Description, step 1 (manifest edit is now two lines not three), removed step 6
  (`AlarmNotifier.kt` KDoc edit), Acceptance (drops criterion 14 from its "Maps to" list, six kept
  permissions not five).
- `STATE.md`: Progress table's T-002 scope column, and the stale VIBRATE-removal assumption replaced with
  a note pointing at D-001; Stage moved from `bootstrap` to `executing`, Next Phase set to Phase 1.
- `ESCALATION.md`: D-001 marked answered.

T-001 is unaffected — none of its scope or acceptance criteria (1-8) reference VIBRATE. Both tasks
unblocked for Phase 1.
