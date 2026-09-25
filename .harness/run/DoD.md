# Definition of Done

> Derived from `PRD.md` at bootstrap (run 2 on `loop/music-player-v2`, 2026-09-25). Human-owned after approval:
> the engine may propose changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence, and must declare WHO can verify it (ADR-015).

## Status

- [ ] APPROVED — approve via `D-001` in `.harness/run/ESCALATION.md` (answer in `DECISIONS.md`); edit criteria
      freely before approving. Approval covers the Verification Class of each criterion, not only its wording.

## Acceptance Criteria

1. [machine] The tree builds, tests and lints: `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest`,
   `./gradlew :app:lintDebug` each print `BUILD SUCCESSFUL`; the lint text report shows **0 errors**.
2. [machine] Unit tests prove the Now Playing time rules (plain Kotlin, `PlaybackTimeTest`): `formatPlaybackTime(0)` =
   `"0:00"`, `65_000` → `"1:05"`, `3_725_000` → `"1:02:05"`, a negative value → `"0:00"`; slider fraction → position:
   `0.5` of `200_000` → `100_000`, below `0` → `0`, above `1` → the duration, an unknown/zero duration → `0`; and the
   position → fraction direction returns `0f` for a zero duration and stays within `0f..1f`.
3. [machine] Unit tests prove the album-art address (plain Kotlin, `AlbumArtUriTest` + `SongMapperTest`): album id `42` →
   `"content://media/external/audio/albumart/42"`; a missing (`null`) or non-positive id → `null`; a MediaStore row with an
   album id maps to a `Song` whose `albumArtUri` is that address.
4. [machine] The notification is still the standard system MediaStyle player provided by Media3: `MusicPlaybackService`
   still extends `MediaSessionService`; app source (`app/src/main/java`) contains **no** `RemoteViews`,
   `setCustomContentView`, `setCustomBigContentView` or `DecoratedMediaCustomViewStyle`; the service installs its
   notification provider with `setMediaNotificationProvider(...)` using the app's own small icon
   `R.drawable.ic_notification_music`; `MediaItemMapper.kt` sets the song's artwork via `setArtworkUri`.
5. [machine] The Now Playing screen is wired: `res/navigation/navigation_graph.xml` declares `nowPlayingFragment` and an
   action `toNowPlaying` usable from `musicFragment`; `injection/ViewModelModule.kt` binds `NowPlayingViewModel`;
   `MusicPlayerRepository` exposes `seekTo(positionMs)`; every new string key exists in both `values/strings.xml` and
   `values-de/strings.xml`; `README.md`'s package tree lists `ui/fragment/nowplaying/`.
6. [machine] The app installs and starts without crashing: `./gradlew :app:installDebug`, then
   `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`; `adb shell dumpsys activity top`
   shows `MusicFragment`; `adb logcat -d -b crash` has no line naming `com.example.myapplication`. (Needs the goal
   capability proposed in D-001; if it is not granted this becomes a `human` item.)
7. [human] **Now Playing opens from the mini bar.** Open the app on the Music tab, tap any song, then tap the mini
   "now playing" bar (not its buttons). Expect a separate full screen with: a large square album picture (or, for a
   song with no picture, a clearly visible music-note placeholder on a lighter panel — not a dark shape on black), the
   song title and artist, a seek slider with elapsed time on the left and total time on the right, and large Material 3
   buttons: previous, a big filled play/pause, next. All text and icons are clearly legible on the dark background; the
   play/pause icon contrasts with its filled button; the slider thumb and track are visible.
8. [human] **Seeking and the clock.** On Now Playing, drag the slider to about the middle and let go. Expect the music
   to jump there and the elapsed time to match. While playing, the elapsed time goes up about once a second and the
   slider moves; tap pause → the time stops moving; tap play → it resumes.
9. [human] **Controls and back.** On Now Playing tap next, previous and play/pause. Expect the song to change / pause /
   resume, the play/pause icon to swap, and title, artist, picture, total time and the slider (back to 0:00) to update to
   the new song — also when a song ends and the next starts on its own. Then press the system back button (and, if
   shown, the back arrow at top-left). Expect the song list again with the mini bar still showing the current song and
   still reacting to play/pause.
10. [human] **Long names.** Play a song with a very long title or artist and open Now Playing. Expect the long text to
    scroll sideways (marquee) on one line; nothing is cut off mid-word without scrolling, and the buttons are not pushed
    off the screen.
11. [human] **Notification in the shade.** Play a song that has album art in your music app, then pull down the
    notification shade. Expect the system's standard media player card showing that album art, the title and artist,
    and working previous / play-pause / next buttons. The status bar shows the app's own music-note icon (not a generic
    square/bell). On Android 12 and older, the action icons are the app's Material Symbols icons and the accent colour
    matches the app's theme; on Android 13+ the system draws its own controls and colours from the album art — that is
    accepted, as long as the art, title, artist and the app's small icon appear.
12. [human] **Lock screen and Quick Settings still work.** While a song plays, lock the phone: the lock-screen media
    player shows the song and its buttons work. Unlock, open Quick Settings: the media player is there and its buttons
    work.
13. [human] **Opening the app from the notification.** With Now Playing open, go Home, then tap the notification.
    Expect the app to open on the Music tab (the list with the mini bar) without crashing; tapping the mini bar opens
    Now Playing again.

## Constraints

- Keep the song list and the mini now-playing bar (PRD). Now Playing is a separate screen (a new Fragment), reached
  only by tapping the mini bar; it uses Material 3 components (`Slider`, `FilledIconButton` / `IconButton`, `Surface`),
  with text still drawn through the house `customizedTextStyle(...)` and every colour from `MaterialTheme.colorScheme`.
- Keep the system's standard MediaStyle notification through Media3's `MediaSessionService` — no custom notification
  layout. Customisation is limited to what Media3's notification provider supports: small icon, accent colour, action
  icons, album art (via `MediaMetadata` artwork).
- No new third-party dependency (no Coil/Glide): album art is loaded with the platform `ContentResolver`.
- The app stays dark-only (existing theme). The notification accent is a colour resource equal to the theme's primary.
- Tapping the notification keeps its current target (the Music tab), unchanged by this run.
- Existing behaviour of run 1 (queue order, wrap-around skip, swipe-away rule, permission flows) must not regress.

## Verification Evidence Required

| Criterion | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1 | machine | the three commands' `BUILD SUCCESSFUL` lines; `lint-results-debug.txt` error count | n/a |
| 2 | machine | `TEST-*PlaybackTimeTest.xml` in `app/build/test-results/testDebugUnitTest/`, failures="0" | n/a |
| 3 | machine | `TEST-*AlbumArtUriTest.xml`, `TEST-*SongMapperTest.xml`, failures="0" | n/a |
| 4 | machine | Grep of `app/src/main/java` for the four forbidden names (0 hits) and for `setMediaNotificationProvider`, `ic_notification_music`, `setArtworkUri` | n/a |
| 5 | machine | Grep of the named files | n/a |
| 6 | machine | install/start output, `dumpsys activity top` line, crash-buffer grep | n/a |
| 7 | human | Music → tap song → tap mini bar → full Now Playing screen, all parts legible | ☐ |
| 8 | human | drag slider → jump; time ticks while playing, stops on pause | ☐ |
| 9 | human | next / previous / play-pause update the whole screen; back returns to list with live mini bar | ☐ |
| 10 | human | long title / artist marquee, buttons stay on screen | ☐ |
| 11 | human | shade: album art, title, artist, working buttons, app's own small icon | ☐ |
| 12 | human | lock screen + Quick Settings player present and working | ☐ |
| 13 | human | notification tap with Now Playing open → Music tab, no crash | ☐ |
