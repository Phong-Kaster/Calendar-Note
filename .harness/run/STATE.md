# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** complete — iteration 7 Verifier re-proved #1–#7, D-004 consumed, Cleanup Commit made
- **Loop Branch:** loop/music-player-v2 (from `main` @ `027d3ea`)
- **Next Phase:** none — waiting for D-004 (`git rm` capability for the Cleanup Commit)
- **DONE-candidate:** yes (recorded iteration 4; iteration 5 Verifier re-proved every `machine` criterion
  #1–#7 with fresh evidence; all 9 `human` criteria unsigned → D-003 queued)

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete (2/3 attempts) | `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `core/CoreLayout.kt`, `core/CoreActivity.kt`, `res/values/themes.xml` | iteration 2 build/test/lint green |
| T-002 | complete (2/3 attempts) | new `domain/`, `data/`, `ui/fragment/music/` files + 3 tests (see task file) | iteration 2 build/test/lint green; 28 new tests; device: MusicFragment resumed, no crash |
| T-003 | complete (1/3 attempts) | playback files + music screen edits (see task file) | iteration 3 build/test/lint green; PlaybackQueuePolicyTest 10; device: MusicFragment resumed, no crash |
| T-004 | complete (1/3 attempts) | `service/*`, `MainActivity.kt`, music screen edits (see task file) | iteration 4 build/test/lint green; PlaybackStopPolicyTest 4, NotificationPermissionDecisionTest 4; device: MusicFragment resumed, no crash |

## Assumptions

All product choices the PRD left open are written into `DoD.md` § Constraints. Engine-level assumptions:

- A-1: The PRD's branch name `loop/ music-player-v2` means `loop/music-player-v2` (stray space dropped).
- A-2: `MusicPlaybackService` goes in a new top-level `service/` package (no rule covers Android services).
- A-3: The raw MediaStore row holder goes in a new `data/mediastore/` package.
- A-4: Music sits in the bottom bar's left group together with Home (Music, Home | + | Setting).
- A-5: "Grant permission" opens Settings only after ≥2 not-granted answers with no rationale (a single
  dismissed dialog is not a permanent refusal).

## Human sign-offs

- DoD #8–#16 — PASS, signed off by the human via D-003 (all 14 checklist rows), 2026-09-25.
