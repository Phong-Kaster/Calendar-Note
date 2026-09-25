# PLAN

> Machine-owned execution strategy. The human never reviews this file - only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 queued decisions.

## Strategy

Run 2 on `loop/music-player-v2` (PRD: "continue on this branch, no new branch"). Builds on run 1's music player
(commit `584b542`): Media3 1.8.0 `MusicPlaybackService`, `MusicPlayerRepository` (ref-counted `MediaController`),
Music tab with list + `NowPlayingBar`.

Order of attack: Phase 1 runs three independent tasks with disjoint scopes — album-art plumbing (T-001), notification
look (T-002), Now Playing screen with a placeholder art slot (T-003). Phase 2 puts the real album art on Now Playing
(T-004), which needs both T-001's `Song.albumArtUri` and T-003's screen.

Design choices fixed at planning time (so no Worker makes them):

- **Album art address:** `SongRow.albumId: Long? = null` (MediaStore `ALBUM_ID`), plain-Kotlin
  `albumArtUriFor(albumId: Long?): String?` → `content://media/external/audio/albumart/<id>` (C-04: a `String`, never
  `Uri`), `Song.albumArtUri: String? = null` (default keeps every existing `Song(...)` call compiling),
  `MediaItemMapper` sets `MediaMetadata.artworkUri` and round-trips it in `toSongOrNull`. Media3's default bitmap
  loader draws it in the notification.
- **Notification look:** a `DefaultMediaNotificationProvider` built in `MusicPlaybackService` and installed with
  `setMediaNotificationProvider(...)`, small icon `R.drawable.ic_notification_music` (Material Symbols music note).
  Accent colour and Material Symbols action icons use whatever Media3 1.8.0 supports (provider API or overriding its
  `media3_notification_*` drawable/colour resources); the Iteration inspects the Media3 aar to confirm names before
  dispatch. On Android 13+ the system draws its own controls — accepted by DoD #11.
- **Position and seek:** `NowPlaying` gains `durationMs: Long = 0L`; `MusicPlayerRepository` gains
  `fun currentPositionMs(): Long` and `fun seekTo(positionMs: Long)` (main thread, C-07). `NowPlayingViewModel`
  polls `currentPositionMs()` about every 500 ms only while `nowPlaying.isPlaying` (a pure `shouldTickPosition` rule),
  so no ticker runs in the repository and `MusicFragment` does not recompose on every tick. `NowPlayingViewModel`
  holds its own `connect()`/`release()` share (C-08).
- **Art on screen:** a screen-local composable loads the art with `ContentResolver.openInputStream` +
  `BitmapFactory` on `Dispatchers.IO` via `produceState`, falling back to a placeholder. No new dependency.
- **Navigation:** a new `nowPlayingFragment` destination; `MusicFragment` calls `safeNavigate(R.id.toNowPlaying)`
  when the mini bar is tapped. Back pops to the list. If `currentSong` becomes null while on Now Playing, the screen
  navigates back.

Verification approach: pure-Kotlin rules unit-tested (DoD #2, #3); static checks (#4, #5); install-and-start on the
attached device if granted (#6); everything visual is `human` (#7–#13).

## Task Graph

- T-001 - Songs carry album art; the notification shows it (depends on: -) - scope: `data/mediastore/SongRow.kt`,
  `data/repository/impl/SongRepositoryImpl.kt`, `domain/model/Song.kt`, `data/mapper/SongMapper.kt`,
  `data/mapper/MediaItemMapper.kt`, new `data/mapper/AlbumArtUri.kt`; [I] `test/.../data/mapper/SongMapperTest.kt`,
  `test/.../data/mapper/AlbumArtUriTest.kt` - tier: Capable
- T-002 - Notification wears the app's M3 look (depends on: -) - scope: `service/MusicPlaybackService.kt`, new
  `service/MusicNotificationProvider.kt`; [I] `res/drawable/ic_notification_music.xml`, Media3 icon overrides
  `res/drawable/media3_*.xml`, `res/values/colors.xml` - tier: Capable
- T-003 - Tapping the mini bar opens a Now Playing screen (depends on: -) - scope:
  `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
  `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/music/MusicFragment.kt`,
  `ui/fragment/music/component/NowPlayingBar.kt`, new `ui/fragment/nowplaying/**` (including `component/NowPlayingArtwork.kt`
  with a placeholder body, which T-004 later fills); [I] `test/.../ui/fragment/nowplaying/model/PlaybackTimeTest.kt` - tier: Capable
- T-004 - Now Playing shows the real album art (depends on: T-001, T-003) - scope:
  `ui/fragment/nowplaying/component/NowPlayingArtwork.kt` - tier: Capable
- T-005 - Now Playing stays in step while paused, off-screen, or restored empty (depends on: T-003; A-002) - scope:
  `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
  `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/{NowPlayingViewModel,NowPlayingFragment,
  NowPlayingUiState}.kt`, `ui/fragment/nowplaying/model/NowPlayingCloseRule.kt` (A-003), [I] its test - tier: Capable

`[I]` = written by the Iteration from the Worker's report (C-11). Paths are under `app/src/main/java/com/example/skeleton/`
unless they start with `test/` (`app/src/test/java/com/example/skeleton/`) or `res/` (`app/src/main/res/`).

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001, T-002, T-003 | `res/navigation/navigation_graph.xml` (`nowPlayingFragment`, `toNowPlaying`), `injection/ViewModelModule.kt`, both `strings.xml`, `README.md`, every `[I]` res/test file |
| 2 | T-004, T-005 | both `strings.xml` (if new keys), `README.md` if the tree changed |
| 3 | T-006 (A-004, from D-002) | `test/data/mapper/AlbumArtUriTest.kt` `[I]`, `README.md` (new `data/albumart/` package) |

## Known Risks

- Media3 1.8.0's notification-provider API cannot be read by a Worker (zipped aar); the Iteration must confirm resource
  names / builder methods before dispatching T-002, and the build is the judge.
- `content://media/external/audio/albumart/<id>` is a legacy endpoint; on some devices it may return nothing. Fallback is
  the placeholder (DoD #7) — never a crash. ExoPlayer's embedded-artwork metadata may also supply art to the notification.
- On API 31+ the system colours MediaStyle from the artwork, and on 33+ it draws its own controls: accent/icons are only
  visible on older Androids (DoD #11 accepts this).
- Now Playing is a second holder of the shared controller (C-08); back-navigation must not drop the mini bar's updates.
