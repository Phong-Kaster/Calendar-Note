# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - iteration 3_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

none

## Human criteria unsigned

DoD #8–#16 (all 9 `human` criteria). #8–#12 and #14 are buildable now; #13, #15, #16 need Phase 3 (T-004).
They will be asked for together once every `machine` criterion holds.

## Review findings not fixed

- N-4 (non-blocking, T-001): `res/values/themes.xml` window background is `@android:color/black` while the
  theme background is `#0B0D10` — a slight shade change may show on cold start / fade transitions.
- N-4 (non-blocking, T-003): `MusicViewModel` declares an unused `TAG` (kept: viewmodel-layer convention).

## Assumptions recorded

- Branch name `loop/ music-player-v2` read as `loop/music-player-v2`.
- Playback service in a new `service/` package; raw MediaStore row holder in a new `data/mediastore/` package.
- Music tab sits with Home in the bottom bar's left group.
- "Grant permission" opens Settings only after two not-granted answers with no rationale.

## Environment

- `build-top.txt` (a `dumpsys activity top` capture) is left untracked at the repo root: `rm` is not an
  allowed command. Safe to delete.

- The final Phase-1 build could not be installed on device `b56e2819`: `INSTALL_FAILED_ABORTED: User rejected
  permissions` (MIUI asks for on-device confirmation of each install). The earlier Phase-1 build installed,
  launched on `MusicFragment`, and did not crash.
