# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** complete — iteration 9 Verifier re-proved #1–#5 (#6 from iteration 8, source unchanged); D-003 signed #7, #9, #11
- **Loop Branch:** loop/music-player-v2 (run 2; PRD says continue on this branch, no new branch)
- **Next Phase:** none — all tasks complete
- **DONE-candidate:** yes → verified iteration 9; Cleanup Commit
- **Queued:** none

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | complete | `data/mediastore/SongRow.kt`, `data/repository/impl/SongRepositoryImpl.kt`, `domain/model/Song.kt`, `data/mapper/{SongMapper,MediaItemMapper,AlbumArtUri}.kt` | Phase 1 commit; AlbumArtUriTest 4, SongMapperTest 14 |
| T-002 | complete | `service/MusicPlaybackService.kt`, `service/MusicNotificationProvider.kt` | Phase 1 commit; grep DoD #4 |
| T-003 | complete | `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`, `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/music/{MusicFragment,component/NowPlayingBar}.kt`, `ui/fragment/nowplaying/**` | Phase 1 commit; PlaybackTimeTest 16 |
| T-004 | complete | `ui/fragment/nowplaying/component/NowPlayingArtwork.kt` | Phase 2 commit; build/lint green |
| T-005 | complete | `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`, `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/{NowPlayingViewModel,NowPlayingFragment,NowPlayingUiState}.kt`, `ui/fragment/nowplaying/model/NowPlayingCloseRule.kt` | Phase 2 commit; NowPlayingCloseRuleTest 7, PlaybackTimeTest 16 |
| T-006 | complete | `data/albumart/AlbumArtLoader.kt`, `data/mapper/AlbumArtUri.kt`, `service/{AlbumArtBitmapLoader,MusicPlaybackService}.kt`, `ui/fragment/nowplaying/component/NowPlayingArtwork.kt`, `ui/fragment/nowplaying/NowPlayingFragment.kt`, [I] `test/data/mapper/AlbumArtUriTest.kt` | Phase 3 commit; AlbumArtUriTest 10, 82 tests, lint 0 errors |

## Human sign-offs

- 2026-09-25 (D-003): **#7, #11 signed — music-note placeholder accepted** (no song on the phone has an embedded cover);
  **#9 signed** (D-002 PASS + placeholder acceptance). All 7 human criteria signed.
- 2026-09-25 (D-002, phone CPH2895 Android 16): **#8, #10, #12, #13 pass — signed.**
- #9 passed, but T-006 changes the art it checked → unsigned again, re-ask with #7.
- #7 FAIL, #11 FAIL — no album art → T-006 (landed iteration 7); re-ask. Per the D-002 addendum the phone's two songs
  have no embedded cover, so ask with a song that has one, or for acceptance of the placeholder.

## Assumptions

- "Material 3 components" for the screen means M3 controls (`Slider`, `FilledIconButton`, `IconButton`, `Surface`);
  text keeps the house `customizedTextStyle(...)` (CLAUDE.md forbids `MaterialTheme.typography`), and the house rule
  "no Material `Card`" is kept by using `Surface`.
- Notification accent colour and Material Symbols action icons are required only where Android honours them
  (≤ API 32 / Media3-drawn controls); on API 33+ the system's own controls are accepted (DoD #11).
- Tapping the notification keeps opening the Music tab (unchanged); it does not open Now Playing.
- Album art address stays MediaStore's album-art endpoint by `ALBUM_ID` (DoD #3); since D-002 the picture itself is
  read via `loadThumbnail` (API 29+) / `MediaMetadataRetriever` / legacy stream (T-006); no new image-loading dependency.
- The mini bar gets no thumbnail (PRD: keep list + mini bar as they are).
