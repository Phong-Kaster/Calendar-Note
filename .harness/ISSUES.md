# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - run 2, iteration 3 (Phase 2)_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

none

## Human criteria unsigned

DoD #7–#13 (Now Playing screen, seeking, controls/back, long names, notification shade, lock screen / Quick Settings,
opening from the notification) — asked at the end of the run.

## Review findings not fixed

Phase 2 (NIT, non-blocking):
- `NowPlayingArtwork.kt` keeps a file-level `private const val TAG` string (jetpack-compose-ui.md allows only numbers there).
- A failed controller connect while the Music tab still holds the connection also closes Now Playing (no retry).

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
- Now Playing uses its own top bar (not `CoreTopBar`, which hard-codes colours).

## Environment

- Untracked captures at the repo root — `build-top.txt`, `dumpsys-top.txt`, `crash-log.txt`, `review-diff.txt` — are
  engine scratch files left because `rm` is not an allowed command. Safe to delete.
- Untracked tooling not created by the engine (`.agents/`, `.claude/agents/`, `.claude/skills/`, `.harness/loop/`,
  `.harness/knowledge/capabilities.json`) and the modified `skills-lock.json` are left for you to commit.
