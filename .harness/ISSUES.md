# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - run 2, iteration 1 (bootstrap)_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

| # | Question | Blocks |
|---|---|---|
| D-001 | Approve the DoD for the Material 3 player (13 criteria) + optional goal install/start capability | T-001, T-002, T-003, T-004 |

## Human criteria unsigned

DoD #7–#13 (Now Playing screen, seeking, controls/back, long names, notification shade, lock screen / Quick Settings,
opening from the notification) — asked at the end of the run.

## Review findings not fixed

Carried from run 1 (non-blocking):
- `res/values/themes.xml` window background is `@android:color/black` while the theme background is `#0B0D10`.
- `MusicViewModel` declares an unused `TAG` (viewmodel-layer convention).
- On Android 13+ the notification may only appear at the next player event after granting the permission.

## Assumptions recorded

- M3 controls + house `customizedTextStyle` text; `Surface` instead of Material `Card`.
- Notification accent/icons required only where Android honours them (≤ Android 12); Android 13+ system controls accepted.
- Tapping the notification keeps opening the Music tab.
- Album art from MediaStore's album-art address; no new image library; placeholder when missing.
- The mini bar gets no thumbnail.

## Environment

- `build-top.txt` (a `dumpsys activity top` capture) is left untracked at the repo root: `rm` is not an allowed
  command. Safe to delete.
