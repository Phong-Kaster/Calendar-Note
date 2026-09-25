# RESUME BLOCK

> Derived cache, never a source of truth. On disagreement with task files or git, this file is wrong.

- **Stage:** escalated — bootstrap done, D-001 (DoD approval + capabilities) pending
- **Next Phase:** Phase 1 (after D-001)
  - T-001 - scope: `data/mediastore/SongRow.kt`, `data/repository/impl/SongRepositoryImpl.kt`, `domain/model/Song.kt`,
    `data/mapper/SongMapper.kt`, `data/mapper/MediaItemMapper.kt`, `data/mapper/AlbumArtUri.kt` (+[I] tests)
  - T-002 - scope: `service/MusicPlaybackService.kt`, `service/MusicNotificationProvider.kt` (+[I] drawables, colors.xml)
  - T-003 - scope: `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
    `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/music/MusicFragment.kt`,
    `ui/fragment/music/component/NowPlayingBar.kt`, `ui/fragment/nowplaying/**` (+[I] PlaybackTimeTest)
  - Iteration wires: `navigation_graph.xml`, `injection/ViewModelModule.kt`, both `strings.xml`, `README.md`, all [I] files.
    Before dispatching T-002: inspect the Media3 1.8.0 session aar for `DefaultMediaNotificationProvider` API and
    `media3_notification_*` resource names.
- **Queued decisions:** 1 — D-001 blocks T-001, T-002, T-003, T-004
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `./gradlew :app:assembleDebug` | test: `./gradlew :app:testDebugUnitTest` |
  lint: `./gradlew :app:lintDebug` (verified run 1, iteration 4; re-verify in Phase 1)
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
