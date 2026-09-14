# ISSUES

> Problems only. Regenerated every iteration. What succeeded is in `git log`, not here.
>
> This file sits beside `run/` rather than inside it, so it survives the Cleanup Commit that removes
> `.harness/run/` when the run completes.

## Abandoned tasks

None. No task has failed an attempt in this run.

## Unreachable tasks

None.

## Queued decisions awaiting an answer

### D-005 — `fallbackToDestructiveMigration(false)` silently wipes the database on a missing migration

**Blocks nothing.** Every remaining task runs while this is unanswered; it does not stop the loop.

`injection/DatabaseModule.kt:29` calls `.fallbackToDestructiveMigration(false)`. On Room 2.7 (this project
is on `2.7.2`) **calling that method enables destructive migration** — the boolean only selects whether all
tables are dropped or just Room-owned ones. The behaviour four KDoc blocks in this repository claim is in
force, "it throws instead of wiping", is what you get by *not calling it at all*.

What it allows: a future task bumps `AppDatabase.version` to 5 and forgets to register the migration. Room
drops and recreates `alarms`, `notes`, `posts` and `user_actions`. The app opens looking healthy and every
alarm and note the user owned is gone — no crash, no log line. No build, test, lint or fresh-install QA
pass can catch it, because a fresh install has no data to lose.

Recorded as Constraint **C-13** in `.harness/knowledge/PROJECT.md` so the trap survives whatever is
decided. The engine recommends removing the call. It did not do so unilaterally because that changes
upgrade behaviour for every installed copy of the app. Full options in `.harness/run/ESCALATION.md`.

## `human` criteria still unsigned

**All 17.** No person has looked at the running app in this run — the engine cannot, and says so rather
than certifying what it cannot see.

Nine are **ready to be shown to someone now**, since A-001 and A-002 are complete with green machine
evidence: criteria **3, 4** (upgrade over a v3 install does not crash), **8** (the Alarms tab is findable
and it is obvious which screen you are on), **9** (the list scrolls and the last row is not hidden under
the floating button), **10** (the empty screen reads as empty rather than broken), **11** (a long alarm
message has defined overflow), **13, 16** (the floating button is visible and opens the editor; the editor
stays usable with the keyboard up).

They are **not** being requested yet, deliberately. The Human Verification Request is raised once at the
end of the run (§11) rather than per phase: it costs one sitting instead of six, and several of these
criteria will be re-opened by A-003 anyway, which changes the same two screens. Listed here so the work
waiting for a human is visible now instead of arriving as a surprise at the end.

## Review findings recorded but not fixed

### The bottom bar's "+" flashes into the Alarms tab during the push to the editor

`ui/fragment/alarms/AlarmsFragment.kt` with `ui/component/CoreBottomBar.kt:94-97`. `hideCreateButton` is
derived from the live back-stack entry, so the instant `navigate(R.id.toAlarmEditor)` runs,
`currentDestination` becomes `alarmEditorFragment`, matches no `BottomBarDestination`, and the flag flips
to false — while the Alarms view is still composed and animating out. A "+" button materialises in the bar
for the length of the exit animation, wired to a deliberate no-op.

Cosmetic, and visible only during a transition. Not fixed here because the clean fix changes
`CoreBottomBar`'s signature to take the decision rather than re-derive it, and that file is shared by four
screens — a change worth making deliberately in a task that owns it, not as a drive-by in A-002's
checkpoint.

## Recorded assumptions

- **AS-1 … AS-11 live in `.harness/run/DoD.md`**, not here — see D-001's archived exchange.
- `AlarmRow` now formats its time to the device's 12/24-hour setting and the app's locale, which makes its
  rendered text **host-dependent**. Any future screenshot case photographing `AlarmRow` is therefore
  host-locked and must be handed a pre-formatted `String` — the same trap `PROJECT.md` already records for
  the Note editor. No reference photographs `AlarmRow` today.
- `AlarmEntity.enabled` ships in the table while nothing reads it until A-004. Deliberate: adding a column
  after the table has shipped costs a second migration, and C-13 makes a missing migration path a silent
  data wipe rather than a crash.
- `AlarmEditorUiState.openFailed` merges "the alarm is gone" with "the store would not answer". Tapping a
  row now reaches the editor with a real id, so the distinction is reachable — but nothing deletes an
  alarm yet, so the first case cannot actually occur. Revisit in A-003, which adds deletion.

## Closed since the last report

- **The `org.gradle.java.home` pin** is gone (removed outside the loop in `ee7b5c9`) and the toolchain was
  re-verified from scratch this iteration: all four commands reach `BUILD SUCCESSFUL` without it. This was
  previously filed here as a personal absolute path committed to a shared file. No longer an issue.
- **D-004** (the screenshot-recording grant) is answered and closed. The reference image arrived via
  commit `7d0c0da` and validates, so the granted command was never run.
