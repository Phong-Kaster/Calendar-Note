# LOOP ISSUES REPORT

> Regenerated every iteration and kept at `.harness/ISSUES.md` — a **sibling** of `run/`, not inside
> it, which is why the Cleanup Commit that removes `.harness/run/` leaves this file standing. This is
> what a human reads when they come back to the run.
>
> Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-08 - branch `loop/todo-calendar-screens` - iteration 4_

## Abandoned tasks

None — every task (T-001..T-007, all seven, run complete) succeeded on its first attempt.

## Unreachable tasks

| Task | Blocked by |
|---|---|

None — no abandonment has occurred.

## Decisions awaiting an answer

None. D-002 (Todo/Calendar screens' hardcoded colors vs. DoD criterion 30) was answered by the
supervising session and consumed in Iteration 4 — DoD criterion 30 was reworded to match; full
request/decision archived in `.harness/run/HISTORY.md` under "Archived Decisions".

## Review findings not fixed

None outstanding. The one prior open finding (hardcoded `Color.White` in `TodoTaskItem.kt`,
`CalendarMonthHeader.kt`, `CalendarDayCell.kt`) is resolved as intended-and-documented behavior via
D-002's amendment to DoD criterion 30, not a defect — see `.harness/run/HISTORY.md`.

## Assumptions recorded

- Task shape: `Task(id, title, isDone, createdAt)` — PRD names only a title.
- Note shape: `Note(id, epochDay, title, createdAt)` — date stored as `LocalDate.toEpochDay()`, no new
  Room `TypeConverter`.
- Blank/whitespace titles are silently rejected (no-op), not surfaced as an error, on add or edit, for
  both tasks and notes.
- Dark mode requirement is satisfied by the existing `CoreFragment` → `MyApplicationTheme(isSystemInDarkTheme())`
  mechanism; `CoreFragment.enableDarkMode` itself is dead code and is left untouched.
- Two new `BottomBarDestination` entries (Todo, Calendar) are the entry point, added to the existing
  `CoreBottomBar` (Home+Todo on the left, Calendar+Setting on the right of the existing unused center
  button).
- Room migration correctness (DoD 16, 24) is proven structurally (entity + DAO + registration + migration
  present, `assembleDebug` succeeds) rather than via an instrumented/Robolectric migration test.
- The app is effectively dark-only today: `CoreLayout`'s background is hardcoded black regardless of
  system theme, and no screen (old or new) sources text/icon colors from `MaterialTheme.colorScheme`
  where doing so would conflict with that background. Pre-existing, not introduced by this run. DoD
  criterion 30 was reworded (D-002) to describe this reality rather than an unmet ideal; full app-wide
  theme-awareness is recorded as a separate follow-up goal, not silently dropped.

See `.harness/run/STATE.md` for the full rationale behind each, and `.harness/run/HISTORY.md` /
`.harness/run/AMENDMENTS.md` for what each Iteration built, wired, and fixed.
