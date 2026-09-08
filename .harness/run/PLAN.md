# PLAN

> Machine-owned execution strategy. The human never reviews this file - only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 queued decisions.

## Strategy

Two independent verticals — To-do (task CRUD) and Calendar (month navigation + per-date notes) — share only
a handful of integration files (Room database registration/migrations, DI modules, the nav graph, the
bottom bar, `strings.xml`). Those shared files belong to no task; the Iteration edits them itself
immediately after each Phase's Workers land, following the exact patterns already used for `Post`/`Setting`.

A standalone theme task (T-001) forces the PRD's blue colour scheme and disables Compose's dynamic-color
override so the scheme actually takes effect; splitting it out (rather than folding it into the first
To-do task, as originally proposed) removes a false dependency and lets the To-do and Calendar verticals
start in the same Phase.

Each task ships its own JVM unit tests in the same checkpoint as the behavior they prove — no separate
"add tests" task. Tests exercise in-memory fakes of the repository interfaces directly (`runBlocking` +
`Flow.first()`/`MutableStateFlow`), never a real Room DB and never a ViewModel instance, so no
`Dispatchers.Main` stubbing is needed. Whether `kotlinx-coroutines-core`'s `runBlocking` resolves on the
JVM test classpath without an explicit `testImplementation` entry (it should, transitively via
`room-ktx`/`lifecycle-runtime-ktx`) is verified empirically the first time Phase 1 runs `gradlew.bat test`
— not assumed. If it does not resolve, that is a capability request, per the pending decision.

Room schema: `AppDatabase` is at `version = 2` today. Task persistence (Phase 1) bumps it to 3 with a new
`MIGRATION_2_3`; note persistence (Phase 2) bumps it to 4 with `MIGRATION_3_4`. Both follow the existing
raw-`CREATE TABLE` pattern in `Migration.kt`. Migration correctness is verified structurally (entity + DAO
+ registration + migration all present, KSP/Room annotation processing succeeds at `assembleDebug` time)
per the DoD's own evidence scoping — no instrumented/Robolectric migration test is added without a
separate approval.

Verification per Phase: build, test, lint on the combined tree (§ENGINE 6.7), then a Fresh-Context Review
(§ENGINE 6.8) before checkpointing.

## Task Graph

- T-001 - App-wide blue Material3 theme, dynamic color forced off, dark mode confirmed (depends on: -) - scope: `ui/theme/Color.kt`, `ui/theme/Theme.kt` - tier: Capable
- T-002 - To-do: add a task, see it in a persisted list (depends on: -) - scope: `domain/model/Task.kt`, `domain/repository/TaskRepository.kt`, `data/database/local/entity/TaskEntity.kt`, `data/database/local/dao/TaskDao.kt`, `data/mapper/TaskMapper.kt`, `data/repository/impl/TaskRepositoryImpl.kt`, `ui/fragment/todo/TodoFragment.kt`, `ui/fragment/todo/TodoUiState.kt`, `ui/fragment/todo/TodoViewModel.kt`, `ui/fragment/todo/component/TodoAddTaskRow.kt`, `ui/fragment/todo/component/TodoTaskItem.kt`, `app/src/test/java/com/example/skeleton/todo/FakeTaskRepository.kt`, `app/src/test/java/com/example/skeleton/todo/TaskRepositoryCreateReadTest.kt` - tier: Capable
- T-003 - To-do: toggle done/not-done and delete a task (depends on: T-002) - scope: `domain/repository/TaskRepository.kt`, `data/database/local/dao/TaskDao.kt`, `data/repository/impl/TaskRepositoryImpl.kt`, `ui/fragment/todo/TodoViewModel.kt`, `ui/fragment/todo/component/TodoTaskItem.kt`, `app/src/test/java/com/example/skeleton/todo/FakeTaskRepository.kt`, `app/src/test/java/com/example/skeleton/todo/TaskToggleAndDeleteTest.kt` - tier: Fast
- T-004 - To-do: edit an existing task's title (depends on: T-003) - scope: `domain/repository/TaskRepository.kt`, `data/database/local/dao/TaskDao.kt`, `data/repository/impl/TaskRepositoryImpl.kt`, `ui/fragment/todo/TodoUiState.kt`, `ui/fragment/todo/TodoViewModel.kt`, `ui/fragment/todo/component/TodoEditTaskDialog.kt`, `app/src/test/java/com/example/skeleton/todo/TaskEditTitleTest.kt` - tier: Capable
- T-005 - Calendar: current month shown, navigate previous/next, arithmetic correct (depends on: -) - scope: `domain/model/CalendarMonth.kt`, `ui/fragment/calendar/CalendarFragment.kt`, `ui/fragment/calendar/CalendarUiState.kt`, `ui/fragment/calendar/CalendarViewModel.kt`, `ui/fragment/calendar/component/CalendarMonthHeader.kt`, `ui/fragment/calendar/component/CalendarMonthGrid.kt`, `ui/fragment/calendar/component/CalendarDayCell.kt`, `app/src/test/java/com/example/skeleton/calendar/CalendarMonthTest.kt` - tier: Capable
- T-006 - Calendar: add a note to a date, select a date to see only its notes, dates with notes marked (depends on: T-005) - scope: `domain/model/Note.kt`, `domain/repository/NoteRepository.kt`, `data/database/local/entity/NoteEntity.kt`, `data/database/local/dao/NoteDao.kt`, `data/mapper/NoteMapper.kt`, `data/repository/impl/NoteRepositoryImpl.kt`, `ui/fragment/calendar/CalendarUiState.kt`, `ui/fragment/calendar/CalendarViewModel.kt`, `ui/fragment/calendar/CalendarFragment.kt`, `ui/fragment/calendar/component/CalendarDayCell.kt`, `ui/fragment/calendar/component/CalendarNoteList.kt`, `ui/fragment/calendar/component/CalendarAddNoteRow.kt`, `app/src/test/java/com/example/skeleton/calendar/FakeNoteRepository.kt`, `app/src/test/java/com/example/skeleton/calendar/NoteDateScopingTest.kt` - tier: Capable
- T-007 - Calendar: edit and delete a note on the selected date (depends on: T-006) - scope: `domain/repository/NoteRepository.kt`, `data/database/local/dao/NoteDao.kt`, `data/repository/impl/NoteRepositoryImpl.kt`, `ui/fragment/calendar/CalendarUiState.kt`, `ui/fragment/calendar/CalendarViewModel.kt`, `ui/fragment/calendar/component/CalendarNoteList.kt`, `ui/fragment/calendar/component/CalendarEditNoteDialog.kt`, `app/src/test/java/com/example/skeleton/calendar/NoteEditAndDeleteTest.kt` - tier: Capable

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001, T-002, T-005 | `AppDatabase.kt` (register `TaskEntity`/`TaskDao`, version → 3), `Migration.kt` (`MIGRATION_2_3`), `injection/DatabaseModule.kt`, `injection/RepositoryModule.kt`, `injection/ViewModelModule.kt`, `app/src/main/res/navigation/navigation_graph.xml` (todo + calendar destinations), `ui/component/CoreBottomBar.kt` + `domain/enums/BottomBarDestination.kt` (entry points), `res/values/strings.xml` |
| 2 | T-003, T-006 | `AppDatabase.kt` (register `NoteEntity`/`NoteDao`, version → 4), `Migration.kt` (`MIGRATION_3_4`), `injection/DatabaseModule.kt`, `injection/RepositoryModule.kt`, `injection/ViewModelModule.kt` (CalendarViewModel gains `noteRepository`), `res/values/strings.xml`, `ui/fragment/todo/TodoFragment.kt` (wire `TodoTaskItem`'s new `onToggle`/`onDelete` callbacks — discovered necessary during execution, see `AMENDMENTS.md` Iteration 2), `ui/fragment/calendar/component/CalendarMonthGrid.kt` (thread `datesWithNotes` through to `CalendarDayCell`'s `hasNotes` param — same reason) |
| 3 | T-004, T-007 | `res/values/strings.xml` (edit-dialog strings) |

Every task in a Phase has a Declared File Scope pairwise-disjoint from its Phase-mates (verified again in
§ENGINE 6.6 before trusting Worker output, not just at planning time).

## Known Risks

- `CoreBottomBar.kt` currently hard-codes two destinations around an unused center "+" button. Extending
  it to reach Todo/Calendar is a small layout change, not pure config — the Iteration (always Capable tier)
  makes this edit itself rather than delegating it, and Fresh-Context Review should check it doesn't
  regress Home/Setting's bottom bar.
- `MyApplicationTheme` defaults `dynamicColor = true`, which silently overrides any custom light/dark
  scheme on API 31+ devices. T-001 must explicitly force it off or the blue scheme requirement (DoD #29)
  is unmet despite `Color.kt`/`Theme.kt` looking correct.
- Room migrations are verified structurally, not by an instrumented/Robolectric migration test (no such
  dependency is approved). A hand-written `CREATE TABLE` that drifts from the entity's generated schema
  would only surface as a runtime crash on a real device — Fresh-Context Review should double-check new
  migration SQL against the entity's columns/types by hand each time one is added.
- `CLAUDE.md`'s `@`-imports reference `.claude/view-model-layer.md`, `.claude/jetpack-compose-ui-layer.md`,
  `.claude/wiki-connection.md`, none of which exist on disk (real files: `viewmodel-layer.md`,
  `jetpack-compose-ui.md`; no wiki file at all). Pre-existing, out of this run's scope — noted, not fixed.
