# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** in-progress
- **Loop Branch:** loop/todo-calendar-screens
- **Next Phase:** Phase 3 (T-004, T-007)
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `ui/theme/Color.kt`, `ui/theme/Theme.kt` | build+test pass; see T-001.md Evidence |
| T-002 | complete | `domain/model/Task.kt` + Task CRUD data/ui files (see PLAN.md) | build+test pass; see T-002.md Evidence |
| T-003 | complete | Task toggle/delete extension (see PLAN.md) | build+test pass; see T-003.md Evidence |
| T-004 | pending (depends on T-003, now met) | Task edit-title extension (see PLAN.md) | - |
| T-005 | complete | `domain/model/CalendarMonth.kt` + Calendar shell (see PLAN.md) | build+test pass; see T-005.md Evidence |
| T-006 | complete | Note CRUD (create/read/mark) + calendar wiring (see PLAN.md) | build+test pass; see T-006.md Evidence |
| T-007 | pending (depends on T-006, now met) | Note edit/delete (see PLAN.md) | - |

Phase 1 (T-001, T-002, T-005) completed Iteration 1: three Workers dispatched at Capable tier,
pairwise-disjoint Declared File Scopes verified against `git status` before trusting output, shared files
(`AppDatabase.kt`, `Migration.kt`, `injection/*`, `navigation_graph.xml`, `CoreBottomBar.kt` +
`BottomBarDestination.kt`, `strings.xml`/`values-de/strings.xml`, two new bottom-bar drawables) wired by
the Iteration itself. Fresh-Context Review found 6 issues (1 critical, 3 major, 2 minor); 5 were fixed
before checkpoint. The 1 critical finding (hardcoded colors on new screens vs. DoD 30, traced to
`CoreLayout`'s pre-existing hardcoded black background) is queued as D-002 in `ESCALATION.md` — a Tier-2,
architecture-touching question that blocks no task but must be resolved before final DONE verification.

Phase 2 (T-003, T-006) completed Iteration 2: two Workers dispatched in parallel (T-003 at Fast tier,
T-006 at Capable tier), both succeeded on attempt 1. Shared files wired by the Iteration: `AppDatabase.kt`/
`Migration.kt` (`NoteEntity`/`NoteDao`, version → 4, `MIGRATION_3_4`), all three Koin modules,
`strings.xml`/`values-de/strings.xml` (`note_title`, `delete_task`), plus two files added to Phase 2's
shared-file list mid-iteration (`AMENDMENTS.md`): `TodoFragment.kt` (toggle/delete callback wiring) and
`CalendarMonthGrid.kt` (`datesWithNotes` threading to the day-cell marker). Two build/test failures fixed
directly rather than re-dispatched (a Kotlin empty-lambda-default compile error, a wrong test assertion) —
see `AMENDMENTS.md` Iteration 2 for why neither counted against either task's attempts. `gradlew.bat
assembleDebug` and `gradlew.bat test` both pass (14/14). `gradlew.bat lint` still shows only the same 4
pre-existing `MissingTranslation` errors; this Phase's 2 new string keys have German translations and add
no new errors. Fresh-Context Review found no critical/blocking issues.

## Assumptions

- **Task shape:** `Task(id: Long, title: String, isDone: Boolean, createdAt: Long)`. PRD names only a
  title; no due date/description/priority was requested. Minor, reversible.
- **Note shape:** `Note(id: Long, epochDay: Long, title: String, createdAt: Long)`. Date stored as
  `LocalDate.toEpochDay()` (plain `Long`) — no existing Room `TypeConverter` for `LocalDate`, and this
  avoids adding one. Minor, reversible.
- **Blank title handling:** add/edit operations reject blank or whitespace-only titles as a silent no-op
  (button/repository declines the write); no error dialog, since the PRD does not ask for one. Minor,
  reversible.
- **Dark mode:** the PRD says "`CoreFragment` already carries a dark-mode flag; use what exists" — in
  fact `CoreFragment.enableDarkMode` is dead code (`setupDarkMode()` is an empty stub); dark mode is
  actually driven today by `MyApplicationTheme(darkTheme = isSystemInDarkTheme())` in `ui/theme/Theme.kt`,
  which every `CoreFragment` subclass already inherits. Assumption: this existing mechanism satisfies
  "dark mode must work" — no new dark-mode-specific code is required for the new screens beyond sourcing
  colors from `MaterialTheme.colorScheme`. Minor, reversible if the human wants the flag itself wired up
  instead.
- **Entry point:** two new `BottomBarDestination` entries (Todo, Calendar) are added to `CoreBottomBar`,
  which today hard-codes only Home/Setting around an unused center button. Exact layout is decided by the
  Iteration when it wires Phase 1 (Capable tier throughout, so no separate task is spun up for this).
  Minor, reversible.
- **Migration evidence:** Room migration correctness (DoD criteria 16, 24) is proven structurally — entity
  + DAO + `AppDatabase` registration + a hand-written `Migration` all present, and `assembleDebug`
  succeeding (which runs Room's KSP schema validation) — not by an instrumented/Robolectric migration
  test, per the PRD's own evidence scoping to build/test commands and named files. Minor, reversible.
