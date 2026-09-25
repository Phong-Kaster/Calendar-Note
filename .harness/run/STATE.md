# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> Execution history lives in HISTORY.md, not here — this file must not grow with the run.

## Current

- **Stage:** escalated (bootstrap done; waiting for D-001 — DoD approval)
- **Loop Branch:** loop/music-player-v2 (run 2; PRD says continue on this branch, no new branch)
- **Next Phase:** Phase 1 — T-001, T-002, T-003 (after D-001)
- **DONE-candidate:** no

## Progress

| Task | Status | Declared File Scope | Evidence |
|---|---|---|---|
| T-001 | blocked (D-001) | `data/mediastore/SongRow.kt`, `data/repository/impl/SongRepositoryImpl.kt`, `domain/model/Song.kt`, `data/mapper/{SongMapper,MediaItemMapper,AlbumArtUri}.kt` | - |
| T-002 | blocked (D-001) | `service/MusicPlaybackService.kt`, `service/MusicNotificationProvider.kt` | - |
| T-003 | blocked (D-001) | `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`, `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/music/{MusicFragment,component/NowPlayingBar}.kt`, `ui/fragment/nowplaying/**` | - |
| T-004 | blocked (D-001; depends T-001, T-003) | `ui/fragment/nowplaying/component/NowPlayingArtwork.kt` | - |

## Human sign-offs

none yet (DoD #7–#13 unsigned)

## Assumptions

- "Material 3 components" for the screen means M3 controls (`Slider`, `FilledIconButton`, `IconButton`, `Surface`);
  text keeps the house `customizedTextStyle(...)` (CLAUDE.md forbids `MaterialTheme.typography`), and the house rule
  "no Material `Card`" is kept by using `Surface`.
- Notification accent colour and Material Symbols action icons are required only where Android honours them
  (≤ API 32 / Media3-drawn controls); on API 33+ the system's own controls are accepted (DoD #11).
- Tapping the notification keeps opening the Music tab (unchanged); it does not open Now Playing.
- Album art comes from MediaStore's album-art endpoint by `ALBUM_ID`; no new image-loading dependency.
- The mini bar gets no thumbnail (PRD: keep list + mini bar as they are).
