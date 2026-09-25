# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - run 2, iteration 8 (Verifier)_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

- **D-003** — Human Verification Request: re-check #7, #9, #11 on the phone with a song that has an embedded cover
  (or accept the placeholder). Blocks completion only. Machine #1–#6 all re-proved this iteration.

## Human criteria unsigned

- **#7, #11** — failed in D-002 (no album cover); fix T-006 landed iteration 7, asked again in D-003.
- **#9** — passed in D-002, but T-006 changed the picture it checked → asked again in D-003.
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
