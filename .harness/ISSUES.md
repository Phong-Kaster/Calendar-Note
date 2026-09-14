# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

## Abandoned tasks

None. No task has failed an attempt in this run.

## Unreachable tasks

None yet. A-004 … A-007 are blocked behind A-003's dependency, not abandoned — they become reachable the
moment D-006 is answered and A-003 is marked complete.

## Queued decisions awaiting an answer

### D-006 — A goal-scoped grant to record one brand-new screenshot reference: the delete confirmation

**Blocks A-003's completion, and A-004 … A-007 transitively** (every task from here depends on A-003).

`AlarmsScreenshotTestKt.AlarmDeleteConfirmationCase` — a picture of `AlarmDeleteConfirmContent()`, the
confirmation sheet's two controls — has no reference image yet. Without the grant,
`validateDebugScreenshotTest` fails on this one case with `ScreenshotImageNotFoundException`; every other
DoD 24, 26, 27, 29, 33, 34 evidence for A-003 is green (assemble, 206 unit tests, lint 0 errors). Full
options in `.harness/run/ESCALATION.md`.

Note what this grant does **not** cover: `AlarmRow` in both switch states, which A-003's own Acceptance
line also asked to be pinned. That half was deliberately not attempted — `AlarmRow`'s displayed time is
host-dependent (device 12/24-hour setting + the app's own locale), so a pinned reference would only be
valid on the host it was recorded on. See D-006's own text and `AMENDMENTS.md` A-6 for the trap this
avoids. Plain (non-pinned) `@Preview` cases for both switch states exist in `AlarmRow.kt`.

## `human` criteria still unsigned

**All 17.** No person has looked at the running app in this run — the engine cannot, and says so rather
than certifying what it cannot see.

Nine are **ready to be shown to someone now**, since A-001 and A-002 are complete with green machine
evidence: criteria **3, 4** (upgrade over a v3 install does not crash), **8** (the Alarms tab is findable
and it is obvious which screen you are on), **9** (the list scrolls and the last row is not hidden under
the floating button), **10** (the empty screen reads as empty rather than broken), **11** (a long alarm
message has defined overflow), **13, 16** (the floating button is visible and opens the editor; the editor
stays usable with the keyboard up).

**A-003's `human` criteria (25, 28, 30) are code-complete and reviewed** but not yet requestable — A-003
itself is not marked complete (blocked by D-006), and a Human Verification Request is never raised against
a task the run has not finished.

None of these are being requested yet, deliberately. The Human Verification Request is raised once at the
end of the run (§11) rather than per phase: it costs one sitting instead of several.

## Review findings recorded but not fixed

### The bottom bar's "+" flashes into the Alarms tab during the push to the editor

`ui/fragment/alarms/AlarmsFragment.kt` with `ui/component/CoreBottomBar.kt:94-97`. `hideCreateButton` is
derived from the live back-stack entry, so the instant `navigate(R.id.toAlarmEditor)` runs,
`currentDestination` becomes `alarmEditorFragment`, matches no `BottomBarDestination`, and the flag flips
to false — while the Alarms view is still composed and animating out. A "+" button materialises in the bar
for the length of the exit animation, wired to a deliberate no-op.

Cosmetic, and visible only during a transition. Not fixed here because the clean fix changes
`CoreBottomBar`'s signature to take the decision rather than re-derive it, and that file is shared by four
screens — a change worth making deliberately in a task that owns it, not as a drive-by in a checkpoint.

### `AlarmDeleteConfirmSheet.kt` duplicates `NoteDeleteConfirmSheet.kt`, with no shared component behind either

Same structure, same padding, same body-copy pattern, same accessibility handling — a widget used by two
unrelated screens with no `ui/component/` extraction, which `.claude/figma-design-system.md` §15 calls for.
A future change to the shared recipe (the 14dp padding that buys Cancel's 48dp touch target, the
`error`/`onError` pairing) applied to one copy and not the other would let the two confirmation sheets in
this app drift apart with a green build. Not extracted this checkpoint: doing so would touch
`NoteFragment.kt` and `NoteScreenshotTest.kt`, both outside A-003's scope and both already shipped and
reviewed. Worth a small future task if a third confirmation sheet ever appears, or if the two drift.

## Recorded assumptions

- **AS-1 … AS-11 live in `.harness/run/DoD.md`**, not here — see D-001's archived exchange.
- `AlarmRow` formats its time to the device's 12/24-hour setting and the app's locale, which makes its
  rendered text **host-dependent**. Confirmed again this iteration: it is why `AlarmRow` carries no pinned
  `@PreviewTest` case even though A-003's Acceptance line asked for one — see D-006 and `AMENDMENTS.md`
  A-6. Any future screenshot case photographing `AlarmRow` must hand it a pre-formatted `String` instead,
  the same trap `PROJECT.md` already records for the Note editor.
- `AlarmEntity.enabled` now drives the per-row switch (A-003), but nothing schedules an alarm or wakes the
  device for it yet — that is A-004.

## Closed since the last report

- **D-004** (the screenshot-recording grant for A-001's empty state) is answered and closed. The reference
  image arrived via commit `7d0c0da` and validates, so the granted command was never run.
- **D-005** (`fallbackToDestructiveMigration(false)` enabling destructive migration) is answered and
  applied this iteration. The call is removed from `DatabaseModule.kt`; Constraint C-13 in
  `.harness/knowledge/PROJECT.md` now records the resolved state rather than only the trap.
- **The `deletedTrigger` re-announcement bug** the Fresh-Context Review found this iteration is fixed —
  `AlarmsViewModel.consumeDeleted()` added and wired, with a regression test.
- **`README.md`**'s feature table and package tree are brought current with A-003 (switching, deleting,
  and `AlarmDeleteConfirmSheet.kt` all now listed as built).
