# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

### D-007 — `AlarmsPermissionNoticeCase` needs a first `@PreviewTest` reference image; grant `updateDebugScreenshotTest` for it?

- **Type:** Capability grant | **Queued:** iteration 7, 2026-09-14
- **Blocks:** A-006 (its acceptance explicitly requires screenshots green) and, transitively, A-007 (depends
  on A-005 **and** A-006 both landing). A-005 has no screenshot dependency and is complete regardless.
- **Question:** Grant a single-use, goal-scoped `updateDebugScreenshotTest` entry so
  `AlarmsPermissionNoticeCase` (`AlarmsScreenshotTest.kt`) gets its first reference image?
- **Context:** A-006 (the permissions-missing banner) landed this iteration with a Worker-authored
  `AlarmsPermissionNotice` composable and an Iteration-owned `@PreviewTest` case pinning it
  (`widthDp = 360, heightDp = 260`, sized from the Worker's own reported 222dp banner height + 24dp of
  `ScreenshotScaffold` padding, per C-05). `validateDebugScreenshotTest` fails on exactly this one case with
  `ScreenshotImageNotFoundException` — expected: it is a brand-new preview, never recorded before, and this
  is the fourth time this exact situation has occurred in this run (D-003, D-004, D-006). Everything else is
  green: `assembleDebug`, 251 unit tests (0 failures), `lintDebug` (0 errors, 72 warnings — 3 new, all
  pre-existing lint categories already tolerated elsewhere in this codebase: `ModifierParameter`,
  `InlinedApi` ×2, `UseKtx`), and the other 22 of 23 screenshot cases.
- **Options:** (1) grant it, goal-scoped to exactly `*AlarmsPermissionNoticeCase*`, with a before/after
  live-hash report for `AlarmsScreenshotTestKt` restricted to that class; (2) refuse — a human records it
  manually and the run reports `ESCALATE` until the reference appears on disk; (3) drop the `@PreviewTest`
  annotation, keep the composable as a plain `@Preview` — not recommended, listed only because it needs no
  grant, and it is the one DoD-31 criterion (*"the screen says so and offers the fix"*) that a picture is
  the only evidence for.
- **Engine recommendation:** option 1, matching D-003/D-004/D-006's precedent exactly. The case is small
  (one component, two conditions both failing, the tallest the banner ever gets), the render has no
  locale-formatted date or time so it is not host-dependent, and a human looking at one rendered banner
  before approving is the same cheap check the three prior grants already relied on.
- **Decision:** **Option 1 — granted, goal-scoped to exactly one invocation of
  `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsPermissionNoticeCase*"`.** Single-use, not a
  standing permission: run it once to record `AlarmsPermissionNoticeCase`'s reference image, report the
  before/after state of `AlarmsScreenshotTestKt`'s reference directory as specified, and then remove this
  entry from `.harness/run/capabilities.json` (or let it expire with the run) — do not reuse it for any
  other test name or any later re-record of this same case without a fresh Decision.
- **Applied (iteration 8).** Ran exactly `./gradlew :app:updateDebugScreenshotTest --tests
  "*AlarmsPermissionNoticeCase*"` once. Before: `AlarmsScreenshotTestKt`'s reference directory held two
  files (`AlarmDeleteConfirmationCase_...png`, `AlarmsEmptyStateCase_...png`), no `AlarmsPermissionNoticeCase`
  entry. After: a third file,
  `AlarmsPermissionNoticeCase_Alarms - permission notice_a2b5ee82_0.png`, appeared — no other case's file
  changed. Re-verified `assembleDebug` + `testDebugUnitTest` + `lintDebug` + `validateDebugScreenshotTest`
  together: `BUILD SUCCESSFUL`, 251 tests / 0 failures, lint 0 errors / 72 warnings, 23/23 screenshot cases
  green. A-006 marked complete; A-007 becomes selectable.



