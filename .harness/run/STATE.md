# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** awaiting human verification (iteration 5 Verifier re-proved every machine criterion #1–#6, #6 on the phone)
- **Loop Branch:** loop/music-player-v2 (run 2; PRD says continue on this branch, no new branch)
- **Next Phase:** none — next Iteration is the Verifier again (§11): consume D-002
- **DONE-candidate:** yes (recorded iteration 3; kept — no code gap found by the iteration-4 and iteration-5 Verifiers)
- **Queued:** D-002 (Human Verification Request for DoD #7–#13)

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `data/mediastore/SongRow.kt`, `data/repository/impl/SongRepositoryImpl.kt`, `domain/model/Song.kt`, `data/mapper/{SongMapper,MediaItemMapper,AlbumArtUri}.kt` | Phase 1 commit; AlbumArtUriTest 4, SongMapperTest 14 |
| T-002 | complete | `service/MusicPlaybackService.kt`, `service/MusicNotificationProvider.kt` | Phase 1 commit; grep DoD #4 |
| T-003 | complete | `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`, `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/music/{MusicFragment,component/NowPlayingBar}.kt`, `ui/fragment/nowplaying/**` | Phase 1 commit; PlaybackTimeTest 16 |
| T-004 | complete | `ui/fragment/nowplaying/component/NowPlayingArtwork.kt` | Phase 2 commit; build/lint green |
| T-005 | complete | `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`, `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/{NowPlayingViewModel,NowPlayingFragment,NowPlayingUiState}.kt`, `ui/fragment/nowplaying/model/NowPlayingCloseRule.kt` | Phase 2 commit; NowPlayingCloseRuleTest 7, PlaybackTimeTest 16 |

## Human sign-offs

none yet (DoD #7–#13 unsigned; asked in D-002, iteration 4)

## Assumptions

- "Material 3 components" for the screen means M3 controls (`Slider`, `FilledIconButton`, `IconButton`, `Surface`);
  text keeps the house `customizedTextStyle(...)` (CLAUDE.md forbids `MaterialTheme.typography`), and the house rule
  "no Material `Card`" is kept by using `Surface`.
- Notification accent colour and Material Symbols action icons are required only where Android honours them
  (≤ API 32 / Media3-drawn controls); on API 33+ the system's own controls are accepted (DoD #11).
- Tapping the notification keeps opening the Music tab (unchanged); it does not open Now Playing.
- Album art comes from MediaStore's album-art endpoint by `ALBUM_ID`; no new image-loading dependency.
- The mini bar gets no thumbnail (PRD: keep list + mini bar as they are).
