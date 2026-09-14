# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

## Abandoned tasks

None. No task has failed an attempt in this run.

## Unreachable tasks

None. All seven tasks (A-001 … A-007) are complete.

## Queued decisions awaiting an answer

**D-008 — Human Verification Request**, raised iteration 9 (the Verifier). Every `machine` criterion was
re-proved fresh this iteration (build/test/lint/screenshots all green, re-read from disk rather than
trusted); the request asks a person to work through the sixteen `human` checks below on the running app
and record pass/fail for each. See `ESCALATION.md` D-008 for the exact steps. D-001 through D-007 are all
consumed — see `AMENDMENTS.md` for that trail.

## `human` criteria still unsigned

**All sixteen of them.** No person has looked at the running app in this run — the engine cannot, and
says so rather than certifying what it cannot see. This is now the **only** thing standing between this
run and `DONE`: every task is complete, every `machine` criterion is green, nothing is abandoned or
deferred.

- **3, 4** — upgrade over a v3 install does not crash.
- **8** — the Alarms tab is findable and it is obvious which screen you are on.
- **9** — the list scrolls and the last row is not hidden under the floating button.
- **10** — the empty screen reads as empty rather than broken.
- **11** — a long alarm message has defined overflow.
- **13, 16** — the floating button is visible and opens the editor; the editor stays usable with the
  keyboard up.
- **18** — an alarm survives the app being force-stopped and reopened.
- **22** — the alarm actually fires at the time set, with the user's message, as a popup over whatever
  is on screen, and repeats the next day unattended.
- **23** — tapping the notification opens the app on the Alarms screen and dismisses the notification,
  without launching a second copy of the app.
- **25** — tapping an alarm in the list opens it with its message and time already filled in.
- **28** — the delete control is findable and the confirming button is unmistakably different from the
  safe one.
- **30** — the enabled switch reads correctly at a glance and its state marker contrasts with its
  background.
- **31** — with notifications off, or exact alarms not permitted, the Alarms screen says so and offers
  the fix, and the warning clears itself once the setting is corrected.
- **32** — an alarm set before a reboot still arrives after it, without the user opening the app.
- **35** — both new screens look like they belong to this app: black ground, the product blue, no
  Material-default lilac, every label legible.

Each item's exact steps (what to open, what to do, what to expect) are already written in `DoD.md` §
Verification Evidence Required and copied into `ESCALATION.md` D-008. **The Human Verification Request
has now been raised** (iteration 9, the Verifier, which re-proved every `machine` criterion fresh first).
Answer D-008 — either by filling in its `## Decision` section or by recording each item directly in
`STATE.md` § Human sign-offs, dated — and the next invocation will consume it, re-open any failed item as
a normal discovery, and create the Cleanup Commit once every item passes.

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
this app drift apart with a green build. Not extracted: doing so would touch `NoteFragment.kt` and
`NoteScreenshotTest.kt`, both outside any Alarms task and both already shipped and reviewed. Worth a small
future task if a third confirmation sheet ever appears, or if the two drift.

### `AlarmNotifier.createChannelIfNeeded()`'s name promises a guard its body does not contain

`data/notification/AlarmNotifier.kt`. The function always builds the `NotificationChannel` and always
calls `createNotificationChannel` — safe today, because the platform call itself is a documented no-op
for an id that already exists (which is the entire point of C-10's "immutable once created"), but the
name reads as if the function checks something first. The next person to add a side effect inside it
(logging, a metric) will believe it runs exactly once and be wrong. A rename
(`ensureChannelExists` or similar) or an explicit early-return guard would close the gap. Not touched
this checkpoint — a naming nit, not a behavioural defect.

### A sub-second window lets a confirmed delete be undone by the alarm that was already firing

`data/receiver/AlarmReceiver.kt`. `onReceive` notifies, then re-arms the next occurrence by rebuilding
the alarm from the intent's own extras — deliberately, per A-004's own design: `onReceive` has only
seconds to live, and reading Room from it means `goAsync()` and a coroutine, real machinery the task
chose not to add. Consequence: if a delete is confirmed in the window between the notify and the
re-arm steps, `AlarmRepositoryImpl.delete`'s `mirrorCancel` finds nothing pending (the receiver hasn't
re-armed yet) and the receiver's re-arm then puts a schedule back for a row that no longer exists in
the table — one that can never be cancelled again through the app, because there is no row left to
delete a second time. The window is sub-second (device-dependent), so this is a rare-but-real race, not
a routine one.

**Looked at again by A-005, per its own task instruction, and left open.** A-005's boot-time `rearmAll`
reads `AlarmRepository.alarmsFlow.first()` fresh, so it does not have this race — a delete confirmed
before a reboot is simply absent from the list `rearmAll` re-arms. But that is a different code path
from the one this race lives in: `AlarmReceiver.onReceive`'s ordinary daily re-arm, which still rebuilds
from the firing intent's own extras by design (A-004's deliberate choice — `onReceive` has seconds to
live) and was outside A-005's Declared File Scope. The race is unchanged and still open. A real fix
would give `AlarmReceiver` the same "read the table, not the intent" treatment `rearmAll` has, which is
a receiver-level change no task in this run's plan owns.

### The German bottom-bar label truncates

Pre-existing, not introduced by this run. Filed here per `PLAN.md` risk 6 and `TASKS/A-007.md`'s own
note — this is a standing defect, not a Constraint, because it predates the Alarms feature entirely.

### Pre-existing hardcoded colour literals outside `Color.kt`/`Type.kt`

C-01 in `.harness/knowledge/PROJECT.md` cites "43 such literals" carried forward from a previous run's
count. A fresh grep this run (`grep -rnE 'Color\(0x|Color\.White|Color\.Black' app/src/main/java`,
excluding `Color.kt` and `Type.kt` where such literals are the deliberate token definitions) finds **45**
today, across `CoreLayout.kt`, `CoreBottomSheet.kt`, `CoreTopBar.kt`, `CoreTopBar4.kt`,
`RateBottomSheet.kt`, `HomePermissionBottomSheet.kt`, `SettingItem.kt`, `LanguageItem.kt`,
`SettingLanguageFragment.kt`, `SettingFragment.kt` — none of them files this run touched. The two-count
discrepancy (43 vs 45) is unreconciled; worth a future task re-verifying the exact count and either
correcting C-01's number or explaining the drift, rather than a run that touches none of these files
guessing at it.

## Recorded assumptions

- **AS-1 … AS-11 live in `.harness/run/DoD.md`**, not here — see D-001's archived exchange.
- `AlarmRow` formats its time to the device's 12/24-hour setting and the app's locale, which makes its
  rendered text **host-dependent**. It carries no pinned `@PreviewTest` case for this reason — see
  `AMENDMENTS.md` A-6. Any future screenshot case photographing `AlarmRow` must hand it a
  pre-formatted `String` instead, the same trap `PROJECT.md` already records for the Note editor.
- **A-004's DoD 29 evidence is split across two tests rather than proven end to end in one.**
  `switching an alarm back on arms it again, for the time it is actually set for` proves the stored
  alarm reaches the scheduler on re-enable; the instant computed being genuinely in the future (not the
  alarm's original creation time) is proven separately by `AlarmSchedulingTest`/`NextFireTimeTest`. No
  single test performs disable → advance the clock past the alarm's time → enable → assert tomorrow.
  The substance is covered; the shape the DoD evidence line describes is not literally what exists.

## Closed since the last report

- **A-006** is complete: D-007's goal-scoped screenshot grant was applied, the reference image recorded,
  all four verification commands green.
- **A-007** is complete: `.harness/knowledge/PROJECT.md` reconciled with the tree, two new Constraints
  (C-16, C-17) added. A Fresh-Context Review found and the Iteration fixed one MAJOR (an overstated
  testability claim) and one MINOR (a stale constant count) — see `AMENDMENTS.md` A-16.
- **All seven tasks in this run are now complete.** Nothing abandoned, nothing deferred.
  `STATE.md` records `DONE-candidate: yes`.
- **Iteration 9 (the Verifier) re-proved every `machine` criterion from fresh evidence** — it wrote none
  of the implementation, trusted none of iteration 8's numbers, and re-ran build/test/lint/screenshots
  and re-read criteria 3, 12, 33, 34, 36 directly from source. All hold. It then raised the Human
  Verification Request as `ESCALATION.md` D-008, the only thing standing between this run and `DONE`.
