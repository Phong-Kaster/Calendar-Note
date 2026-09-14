# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

### D-005 — `fallbackToDestructiveMigration(false)` silently wipes the database on a missing migration path. Change it?

- **Type:** Architecture / behaviour change | **Queued:** iteration 4, 2026-09-14
- **Blocks:** nothing. Every remaining task (A-003 … A-007) is executable while this sits unanswered, and
  A-002 shipped correctly regardless — `MIGRATION_3_4` is written and registered, so the 3→4 upgrade path
  is real and does not take the destructive branch. This is about the **next** version bump, not this one.
- **Question:** `injection/DatabaseModule.kt:29` calls
  `.fallbackToDestructiveMigration(false)`. Should that call be **removed**?
- **Context:** Found by the Fresh-Context Review this iteration and confirmed against the Room version in
  `gradle/libs.versions.toml` (`2.7.2`). In Room 2.7 the no-arg overload was deprecated and replaced by
  `fallbackToDestructiveMigration(dropAllTables: Boolean)`. **Calling the method enables destructive
  migration**; the boolean only selects whether all tables are dropped or only Room-owned ones. The
  behaviour everyone here believed was in force — throw when no migration path exists — is what you get by
  **not calling it at all**.
  Four KDoc blocks in this repository assert the opposite (`DatabaseModule.kt:14-17`, `Migration.kt` twice,
  `AlarmEntity.kt`, `Alarm.kt`), and they are what a future agent will read when deciding whether a version
  bump is risky. Recorded as Constraint **C-13** in `.harness/knowledge/PROJECT.md` this checkpoint, so the
  trap is written down whichever way this is answered.
  Concrete failure it allows: A-004 or any later task bumps `version` to 5 and forgets
  `addMigrations(MIGRATION_4_5)`. Room drops and recreates `alarms`, `notes`, `posts` and `user_actions`.
  The app opens clean, no crash, no log line — and every alarm and note the user owned is gone. No build,
  test, lint or fresh-install QA pass can detect this, because a fresh install has nothing to lose.
- **Options:** (1) **remove the call**, restoring Room's default — a missing migration throws
  `IllegalStateException` at launch instead of wiping. Loud, and caught the first time any developer opens
  the app after a bad bump. Costs: an incomplete migration becomes a hard crash rather than a silent reset,
  which during development is an interruption rather than a convenience; (2) **keep it and fix only the
  four comments**, accepting silent data loss as the deliberate policy of a single-developer skeleton app
  with no shipped users yet; (3) keep it for debug builds and remove it for release, which is the shape
  most production apps use — more code, and this repo has no build-type split in `DatabaseModule` today.
- **Engine recommendation:** option 1. This app's whole point is to be copied into other projects, and the
  comments claiming safety travel with the copy. An app that silently empties a user's alarms on an upgrade
  is the worst of the three outcomes, and the crash option 1 produces is exactly the signal that prevents
  it. The interruption cost lands on a developer who already made a mistake; the data loss lands on a user
  who did not.
- **Why not just do it:** it changes upgrade behaviour for every installed copy of the app, in the layer
  below every feature. That is a Tier 2 architecture decision, not a Tier 1 amendment, so it is proposed
  rather than applied.
- **Decision:**

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
- **Applied (iteration 4) — and the grant was never exercised.** Between iterations, commit `7d0c0da`
  landed the reference image itself:
  `app/src/screenshotTestDebug/reference/.../AlarmsScreenshotTestKt/AlarmsEmptyStateCase_Alarms - empty state_d073b064_0.png`.
  So the file the grant existed to produce was already on disk and already committed. This iteration ran
  `validateDebugScreenshotTest` and the case **passes**, which is the evidence the grant was meant to
  unlock — so `updateDebugScreenshotTest` was not run at all. Exactly one file exists in that reference
  directory, as the decision required. The grant stays in `capabilities.json` unused and expires with the
  run; re-recording a reference that already validates would have been a pointless write against an
  approved baseline.
