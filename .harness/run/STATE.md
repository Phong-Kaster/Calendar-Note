# STATE

## Current

- **Stage:** Verified - DONE_PARTIAL (Iteration 9 Verifier: every machine criterion re-proved, every machine-then-human pre-check driven; criteria 7,15,17-21 await a person)
- **Loop Branch:** loop/music-player-v3
- **Next Phase:** none
- **DONE-candidate:** cleared (Iteration 9: nothing left to build, but signatures are outstanding, so the run cannot be DONE. See §14.4)

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | library/song/theme (see TASKS/T-001.md) | build+unit tests pass; driven on AVD (Iteration 9) |
| T-002 | complete | service/player (see TASKS/T-002.md) | build+unit tests pass; driven on AVD (Iteration 9) |
| T-003 | complete | ui/fragment/library/** | build+unit tests pass; driven on AVD (Iteration 9) |
| T-004 | complete | ui/fragment/nowplaying/** | build+unit tests pass; driven on AVD (Iteration 9) |
| T-005 | complete | Iteration-executed removals | build+unit+androidTest compile green; R1,R3 rg empty; manifest verified |
| T-006 | complete | Iteration-executed sweep/README | build+unit+androidTest compile+lint green, UnusedResources 0 |
| T-007 | complete | `ui/util/NavigationUtil.kt` | all 4 gradle tasks exit 0; sweep lists only allow-listed InterFontFamily |

## Assumptions

See `.harness/run/ASSUMPTIONS.md` (A-001 … A-006). None are Tier 3.

## Signed-off human criteria

None yet.
