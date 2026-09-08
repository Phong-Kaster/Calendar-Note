# Definition of Done

> Derived from `PRD.md` at bootstrap. Human-owned after approval: the engine may propose changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence — a command that can be run, an observable behavior, a measurable property.

## Status

- [x] APPROVED — approved via D-001, 2026-09-08 19:02, as authored (no edits).

## Acceptance Criteria

**Build & test gates**

1. `gradlew.bat assembleDebug` succeeds.
2. `gradlew.bat test` succeeds.
3. New JVM unit tests cover create + read of tasks against an in-memory fake `TaskRepository`.
4. New JVM unit tests cover toggle (done/not-done) and delete of tasks against the fake `TaskRepository`.
5. New JVM unit tests cover edit-title of tasks against the fake `TaskRepository`, completing full task CRUD coverage.
6. New JVM unit tests cover create + date-scoped read of notes against an in-memory fake `NoteRepository`, proving a note added to one date is returned for that date and not for a different date.
7. New JVM unit tests cover edit and delete of notes against the fake `NoteRepository`, completing full note CRUD coverage.
8. A JVM unit test proves moving back one month from January yields December of the previous year.
9. A JVM unit test proves a leap-year February has 29 days.
10. All new tests run on the JVM via `gradlew.bat test` with no emulator or connected device; no criterion depends on an instrumented test.
11. No test dependency beyond what is explicitly approved via the Decision Queue is added to the test source set.

**To-do screen**

12. A task can be added by entering a title; it appears in the list.
13. A task can be toggled between done and not-done, reflected immediately in the list.
14. A task can be deleted and no longer appears in the list.
15. An existing task's title can be edited.
16. Tasks are persisted via Room (entity + DAO, registered in `AppDatabase`, migration present) — not in-memory only.

**Calendar screen**

17. The current month is shown when the screen is opened.
18. The user can move to the previous month and to the next month.
19. A note can be added to a specific date by entering a title.
20. A date that has notes is rendered distinguishably from a date that has none.
21. Selecting a date shows only that date's notes.
22. A note on the selected date can be edited.
23. A note on the selected date can be deleted.
24. Notes are persisted via Room (entity + DAO, registered in `AppDatabase`, migration present) — not in-memory only.
25. No recurrence field, API, or UI exists anywhere in the note model — a note belongs to exactly one date.

**Architecture, conventions, look & feel**

26. All new UI is Jetpack Compose; navigation stays Fragment-based via the existing `NavHostFragment` / `navigation_graph.xml`. No Navigation 3 or alternative navigation library is introduced.
27. Each new Fragment extends `CoreFragment` and lives under `ui/fragment/<feature>/`, matching the Home/Setting layout.
28. Repository / mapper / DI conventions in `.claude/repository-layer.md` are followed (interface in `domain/repository/`, impl in `data/repository/impl/`, DI binding).
29. New UI uses Material 3 with a blue colour scheme (`dynamicColor` forced off so the custom scheme actually takes effect on API 31+).
30. Dark mode works for the new screens (inherited via the existing `CoreFragment` → `MyApplicationTheme(isSystemInDarkTheme())` mechanism; new screens read colors from `MaterialTheme.colorScheme`, never hardcoded).
31. Home and Setting still build and remain reachable from the app (existing nav destinations/actions intact).

## Constraints

- Fragment-based navigation only, via the existing `NavHostFragment` / `navigation_graph.xml`. Navigation 3 and other navigation libraries are forbidden.
- Jetpack Compose + Material 3 for all new UI. Blue colour scheme. Dark mode must keep working through the existing `CoreFragment` mechanism.
- Persistence uses the Room database already present. No new persistence engine, no in-memory-only storage for tasks or notes.
- Follow existing repo conventions (`CoreFragment`, `ui/fragment/<feature>/`, `.claude/repository-layer.md`, `.claude/viewmodel-layer.md`). No new architectural pattern.
- Test source set stays plain JUnit 4 unless a specific addition is approved through the Decision Queue.
- No instrumented test may be load-bearing for any criterion.
- Recurring events are out of scope.
- No regression to Home or Setting (build + reachability; a global colour scheme change is expected and is not itself a regression).

## Verification Evidence Required

| Criterion | Evidence |
|---|---|
| 1 | `gradlew.bat assembleDebug` exits 0 |
| 2 | `gradlew.bat test` exits 0 |
| 3 | Task-create/read test (fake `TaskRepository`) passes, part of `gradlew.bat test` |
| 4 | Task-toggle/delete test passes |
| 5 | Task-edit-title test passes |
| 6 | Note-create/date-scoped-read test passes (asserts presence for the target date, absence for another) |
| 7 | Note-edit/delete test passes |
| 8 | Month-navigation test: January → previous → December of prior year |
| 9 | Month-navigation test: leap-year February has 29 days |
| 10 | No new files under `app/src/androidTest/`; all new tests under `app/src/test/` |
| 11 | `app/build.gradle.kts` / `gradle/libs.versions.toml` diff contains only dependency changes explicitly approved in `AMENDMENTS.md` / `HISTORY.md` |
| 12–15 | `TodoViewModel`/`TaskRepository` behavior + corresponding tests (3–5); Compose UI in `ui/fragment/todo/` |
| 16 | `TaskEntity` + `TaskDao` exist under `data/database/local/`, registered in `AppDatabase` with a version bump and a migration in `Migration.kt` |
| 17–18 | `CalendarViewModel` initializes to the current month; prev/next covered by tests 8–9 |
| 19, 22, 23 | Note CRUD in `NoteRepository`/`CalendarViewModel` + tests 6–7 |
| 20 | Calendar day cell composable branches on a `hasNotes`/`datesWithNotes` flag in `CalendarUiState` |
| 21 | Date-scoping test (6) + `CalendarUiState` field populated by a date-filtered query only |
| 24 | `NoteEntity` + `NoteDao` registered in `AppDatabase`, version bumped again, migration present |
| 25 | No recurrence field in `NoteEntity` / `Note` domain model |
| 26 | New `<fragment>` entries in `navigation_graph.xml`; no Navigation-3 dependency in `app/build.gradle.kts` |
| 27 | New fragments declare `: CoreFragment()`, live at `ui/fragment/todo/` and `ui/fragment/calendar/` |
| 28 | `domain/repository/TaskRepository.kt` / `NoteRepository.kt`, `data/repository/impl/*Impl.kt`, bindings in `RepositoryModule.kt` / `ViewModelModule.kt` |
| 29 | `ui/theme/Theme.kt` sets `dynamicColor = false`; `ui/theme/Color.kt` blue values used by both light/dark schemes |
| 30 | New fragments render through `CoreFragment`; no new theme wrapper; colors sourced from `MaterialTheme.colorScheme` |
| 31 | `homeFragment`, `settingFragment`, `settingLanguageFragment` and their actions still present in `navigation_graph.xml`; `gradlew.bat assembleDebug` passes |
