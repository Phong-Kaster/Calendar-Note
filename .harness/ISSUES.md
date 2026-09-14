# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

## Abandoned tasks

None. No task has failed an attempt in this run.

## Unreachable tasks

None. A-005 and A-006 depend only on A-004, now complete, and are both selectable. A-007 depends on all
six other tasks and stays unreachable until A-005 and A-006 land.

## Queued decisions awaiting an answer

None. `ESCALATION.md` is empty — D-001 through D-006 are all answered and archived into `HISTORY.md`.

## `human` criteria still unsigned

All of them. No person has looked at the running app in this run — the engine cannot, and says so
rather than certifying what it cannot see.

Fifteen are **ready to be shown to someone now**, since A-001 through A-004 are all complete with
green machine evidence:

- **3, 4** — upgrade over a v3 install does not crash.
- **8** — the Alarms tab is findable and it is obvious which screen you are on.
- **9** — the list scrolls and the last row is not hidden under the floating button.
- **10** — the empty screen reads as empty rather than broken.
- **11** — a long alarm message has defined overflow.
- **13, 16** — the floating button is visible and opens the editor; the editor stays usable with the
  keyboard up.
- **25** — tapping an alarm in the list opens it with its message and time already filled in.
- **28** — the delete control is findable and the confirming button is unmistakably different from the
  safe one.
- **30** — the enabled switch reads correctly at a glance and its state marker contrasts with its
  background.
- **22** — the alarm actually fires at the time set, with the user's message, as a popup over whatever
  is on screen, and repeats the next day unattended.
- **23** — tapping the notification opens the app on the Alarms screen and dismisses the notification,
  without launching a second copy of the app.

None of these are being requested yet, deliberately. The Human Verification Request is raised once at
the end of the run (§11) rather than per phase: it costs one sitting instead of several, and some of
these will be re-opened anyway by A-006, which touches the same Alarms screen files.

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
a routine one. Flagged for A-005, which introduces a `rearmAll` seam that reads the table on boot — the
natural place to have every re-arm consult the table rather than only the intent's cached copy of it.

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

- **D-006** (the screenshot-recording grant for A-003's delete confirmation) is answered and applied.
  The reference image is recorded; A-003 is marked complete.
- **A-004** is complete: an alarm now actually arms, notifies and re-arms itself daily, mirrored on
  every save/delete through the `AlarmScheduler` seam.
- **Two MAJOR defects** the Fresh-Context Review found in A-004's diff are fixed: a `MainActivity`
  notification-tap handler that replayed on every activity recreation and could stack a duplicate
  Alarms screen; a missing `android.permission.VIBRATE` declaration that silently dropped the
  vibration half of the API-24/25 heads-up recipe. See `AMENDMENTS.md` A-12.
- **`README.md`**'s feature table and package tree are brought current with A-004 (the notification
  feature row, and the `notification/`, `receiver/`, `scheduler/` packages).
