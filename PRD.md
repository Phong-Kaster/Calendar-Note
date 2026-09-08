# Requirement: To-do and Calendar screens

Add two features to this existing Android app: a **To-do** screen and a **Calendar** screen with
per-day notes.

## To-do screen

- The user can add a task by entering a title.
- Added tasks appear in a list.
- The user can toggle a task between done and not-done, and the change is visible in the list.
- The user can delete a task, and it disappears from the list.
- The user can edit an existing task's title.
- Tasks survive an app restart (persisted locally, not in memory).

## Calendar screen

- Shows the current month on entry.
- The user can move to the previous and next month.
- The user can add a note/event to a specific date by entering a title.
- A date that has notes is visually distinguishable from a date that has none.
- Selecting a date shows the notes belonging to that date, and only that date.
- The user can edit and delete a note on the selected date.
- Notes survive an app restart (persisted locally).
- **Out of scope:** recurring events. A note belongs to exactly one date.

## Technology and conventions

- Android, Kotlin, Jetpack Compose for all new UI.
- **Navigation stays Fragment-based** using the existing `NavHostFragment` setup. Do not introduce
  Navigation 3 or any other navigation library.
- Follow this repository's existing conventions rather than inventing new ones. In particular:
  the `CoreFragment` pattern for a Fragment that hosts Compose content, the
  `ui/fragment/<feature>/` package layout used by the existing Home and Setting screens, and the
  layer conventions documented in `.claude/*.md` (repository layer, use-case layer, viewmodel layer).
- Persistence uses the Room database already present in this project.
- Each feature lives in its own package. `app/src/main/res/navigation/navigation_graph.xml` is the
  one file both features need, plus whatever single entry point reaches the new screens.

## Look and feel

- Material 3.
- A blue colour scheme.
- Dark mode must work. `CoreFragment` already carries a dark-mode flag; use what exists.

## How completion will be judged

Every criterion below must be provable by a command whose output can be read, or by a named file
existing with named content. Nothing here should require a human to look at a screen.

- `gradlew.bat assembleDebug` succeeds.
- `gradlew.bat test` succeeds and includes new JVM unit tests covering, at minimum:
  - the full create / read / update / delete behaviour of tasks, against an in-memory fake of the
    task repository;
  - the full create / read / update / delete behaviour of notes, against an in-memory fake of the
    note repository;
  - that a note added to one date is returned for that date and not for a different date;
  - month navigation arithmetic (moving from January back one month gives the previous December,
    and a leap-year February has 29 days).
- The unit tests must run on the JVM via `gradlew.bat test` without an emulator or a connected
  device. Instrumented tests are not required, and no criterion may depend on one.
- The existing test source set uses plain JUnit 4 only. If a test dependency is genuinely needed
  (for example to test a Flow or a Room DAO), propose it as a capability/architecture decision
  rather than assuming it is available.
- No existing screen regresses: Home and Setting still build and remain reachable.
