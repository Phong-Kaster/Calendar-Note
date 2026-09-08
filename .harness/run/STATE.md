# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** escalated
- **Loop Branch:** loop/todo-calendar-screens
- **Next Phase:** none selectable — all tasks blocked by D-001
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | blocked | `ui/theme/Color.kt`, `ui/theme/Theme.kt` | - |
| T-002 | blocked | `domain/model/Task.kt` + Task CRUD data/ui files (see PLAN.md) | - |
| T-003 | blocked | Task toggle/delete extension (see PLAN.md) | - |
| T-004 | blocked | Task edit-title extension (see PLAN.md) | - |
| T-005 | blocked | `domain/model/CalendarMonth.kt` + Calendar shell (see PLAN.md) | - |
| T-006 | blocked | Note CRUD (create/read/mark) + calendar wiring (see PLAN.md) | - |
| T-007 | blocked | Note edit/delete (see PLAN.md) | - |

All seven tasks are blocked by D-001 (DoD approval + standing capability grant) — this is the bootstrap's
single mandatory gate (ENGINE.md §5). No task can be selected into a Phase until it is answered, since none
can be verified via §ENGINE 6.7 without the build/test capability it grants.

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
