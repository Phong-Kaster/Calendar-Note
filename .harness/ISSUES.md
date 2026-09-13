# LOOP ISSUES REPORT

> Regenerated every iteration and kept at `.harness/ISSUES.md` — a **sibling** of `run/`, not inside
> it, which is why the Cleanup Commit that removes `.harness/run/` leaves this file standing. This is
> what a human reads when they come back to the run.
>
> Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-13 — branch `loop/calendar-note-app` — iteration 1_

**Read this first: the run has not started.** The Alarms feature is fully planned — a Definition of Done,
an execution plan and seven task files — and **not one line of it has been written**, because the engine
cannot build, test or lint anything in this repository and three decisions are waiting for you. Everything
below is either a question for you or a defect inherited from before this run.

## Abandoned tasks

None. No task has been attempted.

## Unreachable tasks

None by abandonment. All seven tasks are **blocked**, which is different and reversible — see the next
section.

| Task | Blocked by |
|---|---|
| A-001 | D-001, D-002 (and D-003 for completion) |
| A-002 | D-001, D-002 |
| A-003 | D-001 |
| A-004 | D-001 |
| A-005 | D-001 |
| A-006 | D-001, D-002 |
| A-007 | D-001 |

## Decisions awaiting an answer

**None.** All three were answered and approved on 2026-09-13, shortly after this iteration's checkpoint —
D-001 as written (DoD approved, AS-4 kept, all three capability blocks granted), D-002 option 1 (fourth
bottom-bar tab, centre "+" hidden on Alarms, dedicated FAB), D-003 granted goal-scoped and narrow.
Iteration 2 consumes them at §6.2.

## Capability ledgers — installed and verified

Both were written by the human at the end of iteration 1 and checked by the engine:

- `.harness/knowledge/capabilities.json` — the three D-001 blocks at `"lifetime": "permanent"`
  (build/compile/test/lint, `validateDebugScreenshotTest`, and the `MSYS_NO_PATHCONV=1` git-show
  workaround), plus the placeholder entry that records why `updateDebugScreenshotTest` is **not** there.
- `.harness/run/capabilities.json` — the single D-003 block at `"lifetime": "goal"`, so it expires when
  `.harness/run/` is removed at completion.

The two are not swapped, which is the one thing worth stating plainly because nothing downstream checks it:
a `goal` grant written into the standing ledger would be permanent in fact while calling itself temporary.
`updateDebugScreenshotTest` was granted standing once before, on 2026-09-10, and withdrawn the next day —
that is the mistake this split exists to prevent repeating.

Iteration 1 could not use any of it: the runtime compiles permissions at invocation start and these files
landed after it. **Iteration 2 is the first that can build anything**, and therefore the first that can mark
a task complete.

## `human` criteria still unsigned

All 17. No implementation exists yet, so none of them could be looked at:

| # | What a person has to check |
|---|---|
| 4 | The app installs **over an existing v3 install** and launches without crashing |
| 8 | The Alarms screen can be found, and it is obvious which screen you are on |
| 9 | A long list scrolls and the last alarm is not hidden under the FAB or the bottom bar |
| 10 | An empty Alarms screen says it is empty rather than looking broken |
| 11 | A long message clips with an ellipsis and the alarm's time stays readable |
| 13 | The floating action button is visible and opens the "new alarm" screen |
| 16 | The editor is usable — the keyboard does not cover the line you are typing |
| 18 | An alarm survives the app being force-stopped and reopened |
| 22 | **The alarm actually fires, as a popup, with your message — and again the next day** |
| 23 | Tapping the notification opens the Alarms list without stacking a second copy of the app |
| 25 | Tapping an alarm opens it pre-filled |
| 28 | The delete control is findable and its confirming button is unmistakable |
| 30 | The enabled switch reads correctly at a glance |
| 31 | With notifications off, the screen says so and offers the fix |
| 32 | Alarms still fire after the phone is **rebooted** |
| 35 | The two new screens look like they belong to this app |

The exact instructions for each are in `.harness/run/DoD.md` § Verification Evidence Required. Criteria 22
and 32 need a real phone and real waiting; there is no shortcut and no command here that can stand in for
them.

## Review findings not fixed

None — no diff has been reviewed.

## Inherited defects (predate this run)

These were found by the previous run on this branch and are recorded, not fixed.

- **43 hardcoded colour literals survive in the tree.** `Color.White`, `Color.Black` and `Color(0x…)` in
  existing sources. Nothing mechanical catches them: AGP lint reports no colour finding at all because it
  reads `android:text` in layout XML and this app has no layout XML for its screens, and a screenshot test
  cannot distinguish a literal from a theme lookup because they render identical pixels. This is why C-01
  exists and why DoD criterion 33 is a grep. **New files must not add to the count**; the existing 43 are
  out of this run's scope.
- **Eight orphaned screenshot reference images** sit in `app/src/screenshotTestDebug/reference/`. A
  reference filename ends in a hash of the preview's *parameters*, so changing a `@Preview`'s `name`,
  `widthDp` or `heightDp` writes a new file and leaves the old one behind. Validation ignores strays and
  stays green, so nothing ever reports them.
- **German bottom-bar labels truncate.** "Einstellungen" already ellipsizes at three tabs. D-002's fourth
  tab makes every slot narrower and makes this worse. Recorded rather than fixed because it predates the
  feature — but the new tab's label should be chosen short in *both* languages, and that instruction is in
  A-001's task file.
- **`.claude/figma-design-system.md` § 4(b) contradicts C-01**, telling a reader to do the opposite about
  colour literals. It is a human-owned rule file; the engine reads it and never edits it. Overridden in
  `PROJECT.md` and filed here as a rule-file defect for you to resolve.

## Assumptions recorded

- **The bootstrap that produced `DoD.md`, `PLAN.md`, `A-001` and `A-002` was interrupted before writing any
  run scaffolding**, and iteration 1 salvaged that debris rather than reverting it. The four documents were
  internally consistent and cross-referenced the same constraint and decision ids; reverting would have
  discarded the whole analysis fan-out to re-derive the same result. If you disagree, the cheap fix is to
  delete `.harness/run/` and re-run, which re-enters Bootstrap from scratch.
- **`PRD.md` is left modified and uncommitted in the working tree.** It holds your Alarms addendum. It is
  your intent file, not the engine's to stage, so it is deliberately not in the checkpoint.
- **AS-1 … AS-11** — eleven readings of ambiguities in the three-sentence addendum — live in
  `.harness/run/DoD.md` § Assumptions, each with the criteria it affects and what it costs to reverse. They
  are not duplicated here because approving D-001 approves them, and two copies would drift.
