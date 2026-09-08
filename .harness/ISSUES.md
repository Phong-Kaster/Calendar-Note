# LOOP ISSUES REPORT

> Regenerated every iteration and kept at `.harness/ISSUES.md` — a **sibling** of `run/`, not inside
> it, which is why the Cleanup Commit that removes `.harness/run/` leaves this file standing. This is
> what a human reads when they come back to the run.
>
> Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-08 - branch `loop/todo-calendar-screens` - iteration 3_

## Abandoned tasks

None — every task (T-001..T-007, all seven, run complete) succeeded on its first attempt.

## Unreachable tasks

| Task | Blocked by |
|---|---|

None — no abandonment has occurred.

## Decisions awaiting an answer

| # | Question | Blocks |
|---|---|---|
| D-002 | Todo/Calendar screens hardcode `Color.White` for text/icons instead of sourcing from `MaterialTheme.colorScheme`, literally violating DoD criterion 30. A literal fix would require changing `CoreLayout.kt`'s hardcoded black background too, which risks making Home/Setting's own hardcoded-white text invisible in light mode unless those pre-existing files are also touched (out of every task's Declared File Scope). See full analysis and options in `.harness/run/ESCALATION.md`. | No task remains to select — all seven tasks are complete. Blocks only final DoD sign-off on criterion 30 at Verification (§ENGINE 11); the run cannot proceed past `ESCALATE` to Verification until this is answered. |

## Review findings not fixed

- (D-002, above) Hardcoded `Color.White` in `TodoTaskItem.kt` (including its new edit-pencil icon added in
  Phase 3), `CalendarMonthHeader.kt`, `CalendarDayCell.kt` — deliberately not fixed inline; see D-002. Note:
  the two new dialogs added in Phase 3 (`TodoEditTaskDialog.kt`, `CalendarEditNoteDialog.kt`) and
  `CalendarNoteList.kt`'s text/icons correctly source colors from `MaterialTheme.colorScheme` instead —
  they are not part of this finding.

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

See `.harness/run/STATE.md` for the full rationale behind each, and `.harness/run/HISTORY.md` /
`.harness/run/AMENDMENTS.md` for what Iteration 1 built, wired, and fixed.
