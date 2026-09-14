# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

### D-006 — A goal-scoped grant to record one brand-new screenshot reference: the delete confirmation

- **Type:** Capability grant | **Queued:** iteration 5, 2026-09-14
- **Blocks:** A-003's completion only. A-004 … A-007 are unreachable regardless, since each depends on
  A-003 directly or transitively and none is executable until it is marked complete.
- **Question:** Grant `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmDeleteConfirmationCase*"`,
  goal-scoped, to record the reference image for the **new**
  `AlarmsScreenshotTestKt.AlarmDeleteConfirmationCase` case, added this iteration —
  `AlarmDeleteConfirmContent()` rendered on `ScreenshotGround.Surface`, 360×280.
- **Context:** Exactly the D-004 situation one Phase earlier: a case with **no reference at all yet**,
  which `.harness/knowledge/PROJECT.md`'s own `updateDebugScreenshotTest` note calls out as needing an
  Escalation Request exactly like an intentional change does. A-003's own Acceptance section
  (`TASKS/A-003.md`) requires *"a `@PreviewTest` case for the confirmation sheet ... sized per C-05"* —
  this case is that requirement.
  Without this grant, `validateDebugScreenshotTest` fails on this one case with
  `ScreenshotImageNotFoundException` and DoD criterion 28 stays without pinned evidence for a reason
  unrelated to whether the confirmation sheet is actually correct — everything else about this Phase is
  green: `assembleDebug`, 206 unit tests (0 failures), `lintDebug` (0 errors, 68 warnings), and the other
  22 screenshot cases.
  **The task's Acceptance line also asked for `AlarmRow` pinned in both switch states, and that half is
  deliberately not attempted.** `AlarmRow` formats its displayed time via
  `DateFormat.is24HourFormat(LocalContext.current)` and `LocalConfiguration.current.locales[0]` — both
  host-dependent, exactly the trap `AMENDMENTS.md` A-6 flagged when this formatting was added ("any future
  screenshot case photographing `AlarmRow` is host-locked and must take a pre-formatted `String`
  instead") and the same reason `NoteScreenshotTest.kt` carries no reference for the Note editor. Pinning
  `AlarmRow` today would record a reference valid on this one machine's clock format and locale only, and
  `validateDebugScreenshotTest` run on a host with the other 12/24-hour setting would then fail for a
  reason that has nothing to do with correctness. Fixing it properly means `AlarmRow` taking a
  pre-formatted time `String` rather than formatting it internally — a real change to the component's
  public signature and every existing caller, outside this task's scope. `AlarmRow.kt` still carries
  plain (non-pinned) `@Preview` cases for both switch states, so a person can still look at them in
  Android Studio.
- **Options:** (1) grant it, goal-scoped to exactly this one test name, with a before/after live-hash
  report for `AlarmsScreenshotTestKt` (two files today — `AlarmsEmptyStateCase`'s — plus this one after,
  so an unexpected extra file is visible in the checkpoint); (2) refuse — a human records it manually from
  Android Studio and the run reports `ESCALATE` until the reference appears on disk; (3) drop the
  `@PreviewTest` annotation and keep the composable as a plain `@Preview` — satisfies nothing and reopens
  the gap the task's own Acceptance line names; not recommended, listed only because it needs no grant.
- **Engine recommendation:** option 1. The case is small and deterministic (no locale-formatted date or
  time — the confirmation sheet's two strings are static), and a human looking at one rendered
  confirmation sheet before approving is a cheap check against the same class of risk
  `updateDebugScreenshotTest` always carries.
- **Decision:**

