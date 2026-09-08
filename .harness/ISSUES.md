# LOOP ISSUES REPORT

> Regenerated every iteration and kept at `.harness/ISSUES.md` — a **sibling** of `run/`, not inside
> it, which is why the Cleanup Commit that removes `.harness/run/` leaves this file standing. This is
> what a human reads when they come back to the run.
>
> Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-08 - branch `loop/todo-calendar-screens` - iteration 0 (Bootstrap)_

## Abandoned tasks

None yet — no task has been attempted (Bootstrap does not dispatch Workers).

## Unreachable tasks

| Task | Blocked by |
|---|---|

None — no abandonment has occurred.

## Decisions awaiting an answer

| # | Question | Blocks |
|---|---|---|
| D-001 | Approve the Definition of Done and two proposed standing capabilities (repo build/test/lint; a conditional, narrowly-scoped test-dependency addition) | T-001, T-002, T-003, T-004, T-005, T-006, T-007 (every task in this run) |

## Review findings not fixed

None yet — no Fresh-Context Review has run.

## Assumptions recorded

- Task shape: `Task(id, title, isDone, createdAt)` — PRD names only a title.
- Note shape: `Note(id, epochDay, title, createdAt)` — date stored as `LocalDate.toEpochDay()`, no new
  Room `TypeConverter`.
- Blank/whitespace titles are silently rejected (no-op), not surfaced as an error, on add or edit, for
  both tasks and notes.
- Dark mode requirement is satisfied by the existing `CoreFragment` → `MyApplicationTheme(isSystemInDarkTheme())`
  mechanism; `CoreFragment.enableDarkMode` itself is dead code and is left untouched.
- Two new `BottomBarDestination` entries (Todo, Calendar) are the entry point, added to the existing
  `CoreBottomBar`, which today hard-codes only Home/Setting around an unused center button.
- Room migration correctness (DoD 16, 24) is proven structurally (entity + DAO + registration + migration
  present, `assembleDebug` succeeds) rather than via an instrumented/Robolectric migration test.

See `.harness/run/STATE.md` for the full rationale behind each.
