# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - run 2, iteration 7 (Phase 3)_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

none

## Human criteria unsigned

- **#7 and #11 failed on the phone (D-002): no album cover.** Fix T-006 landed (iteration 7) but is not device-proved:
  the phone's two songs have no embedded cover (D-002 addendum). Will be re-asked with a song that has a cover.
- #9 passed, but T-006 changes the picture it checked → re-asked with #7.
- Signed: #8, #10, #12, #13.

## Review findings not fixed

Phase 3 (non-blocking):
- `service/AlbumArtBitmapLoader.kt`: one background thread, loads never cancelled — skipping many songs fast can delay
  the new cover in the notification by a few seconds.
- `NowPlayingArtwork.kt`: an old song's decode keeps running after a song change (blocking call, not cancellable);
  fast skipping can run several decodes at once (OOM is caught; the cover then stays a placeholder).
- `AlbumArtBitmapLoader.supportsMimeType` accepts every `image/*`, including types `BitmapFactory` cannot decode.
- `NowPlayingArtwork` calls the data-layer `loadAlbumArtBitmap` directly (as T-006 asked) instead of via a repository.

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

- Untracked captures — `build-top.txt`, `dumpsys-top.txt`, `crash-log.txt`, `review-diff.txt` at the root, and
  `.harness/verify-build.txt`, `.harness/verify-lint.txt` — are
  engine scratch files left because `rm` is not an allowed command. Safe to delete.
- Untracked tooling not created by the engine (`.agents/`, `.claude/agents/`, `.claude/skills/`, `.harness/loop/`,
  `.harness/knowledge/capabilities.json`) and the modified `skills-lock.json` are left for you to commit.
