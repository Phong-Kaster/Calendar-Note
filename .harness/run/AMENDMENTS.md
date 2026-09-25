# AMENDMENTS

> Every plan mutation (Tier 1, logged) and every applied decision, with the human's rationale.

<!-- Newest first. -->

### A-002 (Tier 1) - iteration 2 - add T-005 from the Phase 1 review
- **Change:** new task T-005 "Now Playing stays in step while paused, off-screen, or restored empty" (review findings
  2, 3, 4, 5 on T-003's code). Scope `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
  `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/NowPlayingViewModel.kt`,
  `ui/fragment/nowplaying/NowPlayingFragment.kt`, `ui/fragment/nowplaying/NowPlayingUiState.kt`. Depends on T-003.
  Phase 2 = T-004 + T-005 (disjoint: T-004 writes only `component/NowPlayingArtwork.kt`).
- **Why:** PRD, DoD and architecture unchanged; the findings are correctness gaps against DoD #8 (time stays right)
  and #9 (back / screen behaviour), cheaper as a focused task than a re-dispatch of the whole T-003 scope.

### A-001 (decision) - iteration 2 - D-001 applied
- **Decision:** option 1 — DoD approved as written (13 criteria, Verification Classes and Constraints unchanged);
  goal capability for `./gradlew :app:installDebug*` + `adb shell am start -n …MainActivity*` written by the human
  into `.harness/run/capabilities.json`.
- **Human rationale:** "The DoD matches the PRD and the choices already fixed there (full Now Playing screen;
  standard MediaStyle notification with art, small icon, M3 accent/icons). Letting the engine prove #6 saves a
  manual step."
- **Effect:** `DoD.md` immutable from now on; T-001, T-002, T-003 unblocked; DoD #6 stays `machine`.
