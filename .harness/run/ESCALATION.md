# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

### D-004 — A goal-scoped grant to record one brand-new screenshot reference: the Alarms empty state

- **Type:** Capability grant | **Queued:** iteration 3, 2026-09-13
- **Blocked:** A-001's completion only. Every other task (A-002 … A-007) is unreachable regardless, since
  each depends on A-001 directly or transitively and none is executable until it is marked complete.
- **Question:** Grant `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsEmptyStateCase*"`,
  goal-scoped, to record the reference image for the **new** `AlarmsScreenshotTestKt.AlarmsEmptyStateCase`
  case (`app/src/screenshotTest/kotlin/com/example/skeleton/screenshot/AlarmsScreenshotTest.kt`, added this
  iteration — see `AMENDMENTS.md` A-5).
- **Context:** This is not the D-003 situation (an existing, previously-approved reference invalidated by a
  layout change) — it is a case with **no reference at all yet**, which `.harness/knowledge/PROJECT.md`'s
  own `updateDebugScreenshotTest` note calls out as needing an Escalation Request exactly like an
  intentional change does: *"a new state with no reference yet."* Running the existing D-003 grant against
  this case would exceed its named scope (`BottomBar_*`/`BottomBarSystemNight_*` only), so the engine did
  not use it here even though the command text is identical.
  Without this grant, `validateDebugScreenshotTest` fails with `ScreenshotImageNotFoundException` on this
  one case and DoD criterion 2 stays red for a reason that has nothing to do with whether the empty state
  is correct — everything else about this Phase is green: `assembleDebug`, all 138 unit tests, `lintDebug`
  (0 errors), and the other 20 screenshot cases including the two `BottomBar*` re-records from D-003.
- **Options:** (1) grant it, goal-scoped to exactly this one test name, with a before/after live-hash report
  for `AlarmsScreenshotTestKt` restricted to that file's directory (currently empty — no prior references
  exist for this class); (2) refuse — a human records it manually from Android Studio and the run reports
  `ESCALATE` until that reference appears on disk; (3) drop the `@PreviewTest` annotation and keep the
  composable as a plain `@Preview`, satisfying nothing and reopening the exact gap the Fresh-Context Review
  found — not recommended, listed only because it is the alternative that needs no grant.
- **Engine recommendation:** option 1. The case is small (one component, one state, `heightDp = 320` per
  C-05), the render is deterministic (no locale-formatted date or time in `AlarmsEmptyState`, unlike the
  Note editor's known gap), and a human looking at one rendered "No alarms yet" screen before approving is a
  cheap check against the same class of risk `updateDebugScreenshotTest` always carries.
- **Decision:** **Option 1 — granted, goal-scoped to exactly `*AlarmsEmptyStateCase*`.** Added as a
  separate entry in `.harness/run/capabilities.json` distinct from the D-003 entry, since this is a
  first recording (no prior reference) rather than a re-record of an already-approved baseline. Report
  the before/after state of `AlarmsScreenshotTestKt`'s reference directory as specified (empty before,
  one file after) so an unexpected second file would be visible in the checkpoint.
