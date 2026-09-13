# LOOP ISSUES REPORT

> Regenerated every iteration and kept at `.harness/ISSUES.md` — a **sibling** of `run/`, not inside
> it, which is why the Cleanup Commit that removes `.harness/run/` leaves this file standing. This is
> what a human reads when they come back to the run.
>
> Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-13 — branch `loop/calendar-note-app` — iteration 3_

**Read this first: the environment now builds. A-001 is implemented and almost entirely evidenced —
one new screenshot reference needs your approval (D-004) before it can be marked complete.** Everything
else in Phase 1 is green: `assembleDebug`, all 138 unit tests, `lintDebug` (0 errors), and 21 of 21
*existing* screenshot cases. No task is abandoned. No task failed an attempt.

## Abandoned tasks

None.

## Unreachable tasks

None. A-002 … A-007 are simply not yet selectable: each depends on A-001 (directly or transitively),
which cannot be marked complete until D-004 is answered.

## Decisions awaiting an answer

**D-004 — a goal-scoped grant to record one brand-new screenshot reference.** Full text in
`.harness/run/ESCALATION.md`. Short version: `AlarmsScreenshotTest.kt` is a new file this iteration added
(a `@PreviewTest` case A-001's own acceptance criteria required but the plan's Phase 1 row omitted — see
`AMENDMENTS.md` A-5) and it has no reference image yet. Recording one needs
`./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsEmptyStateCase*"`, which is not covered by the
existing D-003 grant (that one is scoped by name to the two `BottomBar*` cases only). Blocks A-001's
completion only.

D-001, D-002, D-003 were answered 2026-09-13 and consumed in iteration 2 — archived in
`.harness/run/HISTORY.md` § Archived Decisions.

## Capability ledgers — installed and verified

- `.harness/knowledge/capabilities.json` — the three D-001 standing blocks, unchanged since iteration 2.
- `.harness/run/capabilities.json` — the D-003 goal-scoped block, already consumed against the two
  `BottomBar*` references this iteration. D-004, once answered, adds a second goal-scoped entry here.

## Review findings — fixed this iteration

The Fresh-Context Review (§6.8) on A-001's diff found two issues, both corrected before this checkpoint:

- **Missing acceptance evidence:** A-001 required a new `@PreviewTest` pinning the empty state; none
  existed. Fixed — `AlarmsScreenshotTest.kt` added (AMENDMENTS.md A-5); its reference image is what D-004
  is waiting on.
- **AS-5's hide-the-"+"-button check lived in the wrong file:** a hardcoded destination-id comparison
  inside `CoreBottomBar` rather than a property on `BottomBarDestination` itself, which would silently stop
  working the day Alarms is reached through a different back-stack hierarchy (e.g. the notification-tap
  route in criterion 23). Fixed — `BottomBarDestination.hidesCreateButton` (AMENDMENTS.md A-6).

No Constraint (C-01 … C-12) violation was found. Both fixes are already in this iteration's checkpoint.

## Flagged, not fixed — a personal path is now committed in `gradle.properties`

Between iteration 2 (no JDK reachable at all) and iteration 3 (this iteration), `gradle.properties` picked
up `org.gradle.java.home=<this machine's Android Studio JBR path>` in the tracked working tree — not
written by any task. Iteration 3 only escaped its drive-letter colon, which `lintDebug` was rating a
`PropertyEscape` **error** unrelated to anything in this feature. The engine cannot relocate this to a
user-level, untracked `~/.gradle/gradle.properties` (its working directory is sandboxed to the
repository), so a personal absolute path stays committed for now. Low stakes today — this is a
single-developer skeleton with no CI — but worth moving out of the tracked file at your convenience. Full
note in `.harness/knowledge/PROJECT.md` § Environmental Facts.

## `human` criteria still unsigned

All 17 — unchanged from iteration 2; A-001 adds no `human`-signable surface of its own beyond criteria 8
and 10, and neither is ready to show a person until the D-004 grant lands and the full Phase is complete.

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

## Inherited defects (predate this run)

These were found by the previous run on this branch and are recorded, not fixed.

- **43 hardcoded colour literals survive in the tree**, none added by this run. See C-01.
- **Eight orphaned screenshot reference images** in `app/src/screenshotTestDebug/reference/`, none added
  by this run.
- **German bottom-bar labels truncate** ("Einstellungen" already ellipsizes at three tabs; the new fourth
  tab makes every slot narrower). A-001 chose short labels in both languages ("Alarms"/"Wecker") so it does
  not make this worse, but the underlying truncation defect predates this feature and is not fixed by it.
- **`.claude/figma-design-system.md` § 4(b) contradicts C-01** — a human-owned rule file the engine reads
  but never edits. Overridden in `PROJECT.md`; still worth your correcting at the source.

## Assumptions recorded

- **The bootstrap that produced `DoD.md`, `PLAN.md`, `A-001` and `A-002` was interrupted before writing any
  run scaffolding**, and iteration 1 salvaged that debris rather than reverting it — see `STATE.md`.
- **`PRD.md` is left modified and uncommitted in the working tree.** It holds your Alarms addendum. It is
  your intent file, not the engine's to stage.
- **AS-1 … AS-11** live in `.harness/run/DoD.md` § Assumptions, not duplicated here.
