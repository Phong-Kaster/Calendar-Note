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

### Iteration 1 - 2026-09-08

- Consumed D-001 (approved). Applied the human's placement correction: the repo build/test/lint grant
  moved to the standing ledger `.harness/knowledge/capabilities.json` with `lifetime: "permanent"`
  (proposal had said `"goal"` while targeting the standing ledger — the human corrected the lifetime, not
  the scope); the conditional coroutines-test-dependency grant moved to the goal-scoped ledger
  `.harness/run/capabilities.json` with `lifetime: "goal"`, unchanged in scope. Both ledger files already
  existed on disk (written by the human's supervising session alongside the decision) and were verified
  to match the decision text verbatim — no further edit needed to them.
- Cleared "Blocked by decision: D-001" on all seven task files (`T-001`..`T-007`); T-003, T-004, T-006,
  T-007 remain pending on their task-graph dependencies, which is a different, ordinary kind of block.
- Dispatched Phase 1 (T-001, T-002, T-005) to three Workers at Capable tier. Verified pairwise-disjoint
  Declared File Scopes against `git status` — no violations. Wired shared files myself per PLAN.md's
  Phase 1 row: `AppDatabase.kt` (registered `TaskEntity`/`TaskDao`, version → 3), `Migration.kt`
  (`MIGRATION_2_3`), `DatabaseModule.kt`, `RepositoryModule.kt`, `ViewModelModule.kt`,
  `navigation_graph.xml` (`todoFragment`/`calendarFragment` + `toTodo`/`toCalendar` actions),
  `BottomBarDestination.kt` + `CoreBottomBar.kt` (added `Todo`/`Calendar` entries either side of the
  existing center button), two new drawables (`ic_bottom_todo.xml`, `ic_bottom_calendar.xml` — no
  existing icon fit, added matching the existing stroke-icon style), `strings.xml`/`values-de/strings.xml`
  (`todo`, `calendar` keys). Also touched `TodoFragment.kt`/`CalendarFragment.kt` (both Worker-owned
  files) to replace the Workers' hardcoded title-string workaround with `stringResource(...)` now that
  the string keys exist — a direct, anticipated follow-up of the strings.xml wiring, not new task work.
- Fresh-Context Review (clean-context, Capable tier, diff + task descriptions + DoD + standards only)
  found 6 issues. Fixed 5 before checkpoint, all mechanical and confined to already-Worker-touched files:
  (1) `TodoAddTaskRow.kt` was clearing the input field even when a blank title was silently rejected —
  now only clears on a successful add; (2) the same file's KDoc claimed an IME "Done" action submits,
  which wasn't wired — added `KeyboardOptions(imeAction = Done)` + `KeyboardActions(onDone = ...)`; (3)
  the Add button's label was a hardcoded `"Add"` string — moved to `stringResource(R.string.add)`, added
  to both string files; (4) `CalendarMonthHeader.kt`'s prev/next `IconButton`s had no accessibility label
  — added `Modifier.semantics { contentDescription = ... }` backed by two new string resources
  (`previous_month`, `next_month`); (5) `CalendarFragment.kt` was missing the class-level KDoc/`@author`
  tag every other fragment in this run has — added. The 6th finding (hardcoded `Color.White` on the new
  screens vs. DoD criterion 30) was **not** fixed inline — see D-002 in `ESCALATION.md` for why a
  literal-compliance fix would trade a checklist violation for a real, worse, invisible-text bug, and why
  a correct fix is out of every task's Declared File Scope with app-wide blast radius (Tier 2, queued
  rather than guessed).
- Re-ran `gradlew.bat assembleDebug`, `test`, and `lint` after the review fixes: build and all 7 tests
  (6 new + 1 pre-existing) still pass; lint still shows exactly the same 4 pre-existing errors, confirming
  the fixes introduced no regression.
