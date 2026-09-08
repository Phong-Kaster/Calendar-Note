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

### Iteration 2 - 2026-09-08

- Dispatched Phase 2 (T-003 at Fast/haiku tier, T-006 at Capable tier) in parallel. Verified pairwise-
  disjoint Declared File Scopes against `git status` — no violations.
- Added `ui/fragment/todo/TodoFragment.kt` and `ui/fragment/calendar/component/CalendarMonthGrid.kt` to
  Phase 2's shared-files list in `PLAN.md` (not anticipated at planning time): T-003's Worker correctly
  left `TodoTaskItem`'s new `onToggle`/`onDelete` callbacks unwired at their `TodoFragment.kt` call site
  (out of its Declared File Scope), and T-006's Worker correctly reported that `CalendarMonthGrid.kt`
  hardcoded `hasNotes = false` with no parameter to carry `datesWithNotes` through (also out of its scope).
  Both are plain integration wiring with no design judgment involved — done by the Iteration itself, same
  as the standing DI/Room wiring.
- Wired shared files per the (now-amended) Phase 2 row: `AppDatabase.kt`/`Migration.kt` (`NoteEntity`/
  `NoteDao` registered, version → 4, `MIGRATION_3_4`), all three Koin modules, `TodoFragment.kt`
  (toggle/delete callback wiring), `CalendarMonthGrid.kt` + `CalendarFragment.kt` (`datesWithNotes`
  threading), `strings.xml`/`values-de/strings.xml` (`note_title`, `delete_task` — replacing
  `CalendarAddNoteRow`'s hardcoded-English placeholders the Worker flagged as out of its scope).
- Two build/test failures were fixed directly by the Iteration rather than re-dispatching a Worker, since
  both were mechanical, single-line, non-design defects rather than evidence of task misclassification or
  a misunderstood requirement — re-dispatching would have discarded substantial correct work to fix a
  keystroke:
  1. `compileDebugKotlin` failed: `onToggle: (Long, Boolean) -> Unit = {}` does not compile in Kotlin (an
     empty-bodied lambda default infers as a zero-arg function type, not the declared two-arg one) — a
     language-level footgun, not a logic error. It appeared identically in `TodoTaskItem.kt` (T-003's own
     file) and in `TodoFragment.kt` (the Iteration's own wiring file) for the same mechanical reason.
     Fixed both to `= { _, _ -> }`.
  2. `testDebugUnitTest` failed: `TaskToggleAndDeleteTest`'s "deleting a task removes it from the list"
     test asserted the wrong remaining task's title (`"Task 1"` instead of `"Task 2"`), given
     `FakeTaskRepository`'s insert-at-front order — a test-assertion typo, not an implementation defect
     (the other two tests in the same file, exercising the same `deleteTask` code path, passed both before
     and after the fix). Corrected the assertion.
  3. `CalendarFragment.kt`'s class-level and `CalendarLayout`-level KDoc (present since T-005/Phase 1) were
     found dropped by T-006's Worker edit, with no replacement. Restored and updated both to describe the
     new note-related behavior, per `android-skeleton-project.md`'s documentation requirement.
- Re-ran `gradlew.bat assembleDebug`/`test`/`lintDebug` after all fixes: build passes, all 14 tests pass
  (10 new this Phase + 4 from Phase 1), lint shows exactly the same 4 pre-existing errors (the 2 new
  string keys this Phase both have German translations and add no new errors).
- Fresh-Context Review (clean context, Capable tier) found no critical or blocking issues. One "major"
  finding (no note edit/delete exists yet) is exactly T-007's scope, correctly deferred to Phase 3, not a
  defect in this Phase. Two minor findings, neither actioned: `CalendarViewModel`'s unused `TAG` field
  matches the codebase's standing (also-unused) `TAG`-per-ViewModel convention, not a regression; and
  `CalendarNoteList` uses a plain `Column`/`forEach` rather than `LazyColumn`, acceptable given a single
  day's note count is expected small (avoiding premature optimization per `CLAUDE.md`).

### Iteration 3 - 2026-09-08

- Dispatched Phase 3 (T-004, T-007), both at Capable tier per their planned Model Tier, in parallel — the
  last Phase in `PLAN.md`. Verified pairwise-disjoint Declared File Scopes against `git status` — no
  violations; both succeeded on attempt 1.
- Added five files to Phase 3's shared-files list in `PLAN.md` (not anticipated at planning time), all
  discovered before dispatch by reading the existing code the new interface methods would touch: adding
  `TaskRepository.updateTitle`/`NoteRepository.update`+`delete` to the domain interfaces would break
  `FakeTaskRepository.kt`/`FakeNoteRepository.kt` (interface implementers used only by tests, outside
  either task's Declared File Scope) unless those fakes gained matching overrides; and the new edit
  entry points needed wiring into `TodoTaskItem.kt`/`TodoFragment.kt` and `CalendarFragment.kt`
  respectively, none of which were in-scope for a Worker to touch. Same category of gap as Iteration 2's
  `TodoFragment.kt`/`CalendarMonthGrid.kt` addition — plain integration wiring, no design judgment, done by
  the Iteration itself.
- Wired shared files per the (now-amended) Phase 3 row: `FakeTaskRepository.kt` (`updateTitle`,
  trim-and-blank-guard mirroring `TaskRepositoryImpl`), `FakeNoteRepository.kt` (`update`/`delete`,
  mirroring `NoteRepositoryImpl`), `TodoTaskItem.kt` (added an `onEdit` pencil `IconButton` beside the
  existing delete button), `TodoFragment.kt` (wired `onEditTask` + rendered `TodoEditTaskDialog` as a
  `ComposeView()` sibling overlay, per `figma-design-system.md` §12), `CalendarFragment.kt` (wired
  `onEditNote`/`onDeleteNote` to `CalendarNoteList` + rendered `CalendarEditNoteDialog` the same way),
  `strings.xml`/`values-de/strings.xml` (`edit_task`, `save`, `cancel`, `edit_note`, `delete_note` — the
  last four replacing `CalendarEditNoteDialog.kt`'s/`CalendarNoteList.kt`'s hardcoded-English placeholders
  the T-007 Worker flagged as out of its scope).
- No Room migration needed this Phase — both tasks only add `UPDATE`/`DELETE` queries against existing
  columns/tables, no schema change, `AppDatabase` stays at version 4.
- Ran `gradlew.bat assembleDebug`/`test`/`lintDebug`: build passes; all 20 tests pass (6 new this Phase: 3
  in `TaskEditTitleTest`, 3 in `NoteEditAndDeleteTest`); lint shows exactly the same 4 pre-existing errors
  (this Phase's 5 new string keys all have German translations, zero new errors).
- Fresh-Context Review (clean context, Capable tier) found two major and two minor issues, all fixed
  before checkpoint: (1) `TodoEditTaskDialog.kt`'s `OutlinedTextField` set `imeAction = Done` but never
  wired `KeyboardActions(onDone = ...)`, unlike every other single-line title field in this run including
  the sibling `CalendarEditNoteDialog.kt` added in this same Phase — added the missing `KeyboardActions`;
  (2) `TodoViewModel.updateTaskTitle` cleared `editingTask` synchronously outside the write coroutine while
  `CalendarViewModel.updateNoteTitle` cleared `editingNote` only after the write completed inside its
  coroutine — same interaction, two different behaviors from two independent workers; changed
  `updateTaskTitle` to match `updateNoteTitle`'s after-write pattern; (3) `NoteDao.kt` was missing the
  class-level KDoc every sibling DAO (`TaskDao.kt`) has — added; (4) a cosmetic import-ordering slip in
  `TodoEditTaskDialog.kt` — fixed alongside the `KeyboardActions` import. Both new dialogs were confirmed
  to correctly use `MaterialTheme.colorScheme` defaults rather than copying the pre-existing hardcoded-white
  pattern (the dialogs' own `Surface` isn't affected by `CoreLayout`'s black background, so no D-002-style
  tension exists for them).
- Re-ran `gradlew.bat assembleDebug`/`test` after the review fixes: both still pass, 20/20 tests.
- All seven tasks (`T-001`..`T-007`) are now complete. No Phase 4 exists in `PLAN.md`. D-002 remains
  queued and unanswered — it blocks no task (none exist to select), but blocks final DoD sign-off on
  criterion 30 at Verification (§ENGINE 11). Since no executable task remains and a decision is queued,
  this Iteration reports `ESCALATE` per §ENGINE 6.11, not a DONE-candidate.

### Iteration 4 - 2026-09-08

- Consumed D-002 (Tier 2/3, answered by the supervising session — Option 1 + Option 3, full text
  archived in `HISTORY.md`). Applied the human-authored amended wording for DoD criterion 30 to
  `DoD.md` verbatim: Status-section note, the criterion text itself, and the Verification Evidence
  Required row 30 (now naming which files source `MaterialTheme.colorScheme` and which follow the
  pre-existing hardcoded-white-on-black pattern). No implementation file touched.
  `.harness/run/ESCALATION.md`'s D-002 entry replaced with a "consumed" pointer, matching the D-001
  pattern.
- D-002 named no blocked tasks, so nothing to unblock beyond final DoD sign-off.
- No Phase selected this Iteration (all seven tasks already complete, no Phase 4 in `PLAN.md`). With
  the only queued decision now consumed and nothing abandoned or deferred, `STATE.md` now records a
  DONE-candidate for the next, fresh invocation to verify per §ENGINE 11.
