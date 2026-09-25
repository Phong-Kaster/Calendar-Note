# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** escalated (DoD approved; awaiting capability ledgers, D-002)
- **Loop Branch:** loop/music-player-v2 (from `main` @ `027d3ea`)
- **Next Phase:** Phase 1 — T-001, T-002 (after D-002)
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | deferred (D-002) | `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `core/CoreLayout.kt`, `core/CoreActivity.kt`, `res/values/themes.xml` | - |
| T-002 | deferred (D-002) | new `domain/`, `data/`, `ui/fragment/music/` files + 3 tests (see task file) | - |
| T-003 | deferred (D-002) | playback files + music screen edits (see task file) | - |
| T-004 | deferred (D-002) | `service/*`, `MainActivity.kt`, music screen edits (see task file) | - |

## Assumptions

All product choices the PRD left open are written into `DoD.md` § Constraints, so the human approves them
with the DoD (D-001) rather than inheriting them silently. Engine-level assumptions:

- A-1: The PRD's branch name `loop/ music-player-v2` means `loop/music-player-v2` (stray space dropped).
- A-2: `MusicPlaybackService` goes in a new top-level `service/` package (no rule covers Android services).
- A-3: The raw MediaStore row holder goes in a new `data/mediastore/` package.

## Human sign-offs

- none yet
