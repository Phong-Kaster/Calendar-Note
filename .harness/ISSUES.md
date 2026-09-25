# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - iteration 7 (Verifier, run complete)_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

none

## Human criteria unsigned

none — DoD #8–#16 signed off via D-003 (14/14 PASS).

## Review findings not fixed

- N-4 (non-blocking, T-001): `res/values/themes.xml` window background is `@android:color/black` while the
  theme background is `#0B0D10` — a slight shade change may show on cold start / fade transitions.
- N-4 (non-blocking, T-003): `MusicViewModel` declares an unused `TAG` (kept: viewmodel-layer convention).
- Note (T-004): playback starts before the notification dialog is answered; on Android 13+ the notification
  may only appear at the next player event (e.g. first Pause) after granting. Not flagged in D-003.

## Assumptions recorded

- Branch name `loop/ music-player-v2` read as `loop/music-player-v2`.
- Playback service in a new `service/` package; raw MediaStore row holder in a new `data/mediastore/` package.
- Music tab sits with Home in the bottom bar's left group.
- "Grant permission" opens Settings only after two not-granted answers with no rationale.
- "Asked once per screen visit" = the notification dialog resets each time the Music view is recreated.

## Environment

- `build-top.txt` (a `dumpsys activity top` capture) is left untracked at the repo root: `rm` is not an
  allowed command. Safe to delete.
