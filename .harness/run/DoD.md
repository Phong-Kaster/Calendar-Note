# Definition of Done

> Derived from `PRD.md` at bootstrap. Human-owned after approval: the engine may propose changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence, and must declare WHO can verify it (ADR-015).

## Status

- [x] APPROVED — approve via the pending `.harness/run/ESCALATION.md` entry **D-001** (answer in
      `.harness/run/DECISIONS.md`); edit criteria freely before approving.
      Approval covers the Verification Class of each criterion, not only its wording.

## Acceptance Criteria

1. [machine] The app builds: `./gradlew :app:assembleDebug` prints `BUILD SUCCESSFUL`.
2. [machine] Unit tests pass: `./gradlew :app:testDebugUnitTest` prints `BUILD SUCCESSFUL`, and the result
   files under `app/build/test-results/testDebugUnitTest/` contain these test classes, each with ≥1 test and
   0 failures: `AudioPermissionTest` (API ≥33 → `READ_MEDIA_AUDIO`, below → `READ_EXTERNAL_STORAGE`),
   `SongMapperTest` (row with duration ≤ 0 dropped; blank title → file name; `<unknown>`/blank artist → none),
   `MusicUiStateTest` (denied / loading / empty / songs resolution), `PlaybackQueuePolicyTest` (next and
   previous at first, middle and last index — wrapping at both ends — and for queue sizes 0 and 1),
   `PlaybackStopPolicyTest` (stop-on-swipe-away truth table).
3. [machine] Lint is clean: `./gradlew :app:lintDebug` prints `BUILD SUCCESSFUL` with 0 errors (this includes
   fixing the 4 `MissingTranslation` errors already present on `main`).
4. [machine] The merged manifest `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml`
   declares `READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"`, `POST_NOTIFICATIONS`,
   `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, and a `<service>` with
   `foregroundServiceType="mediaPlayback"` exporting the `androidx.media3.session.MediaSessionService` action.
5. [machine] The next / previous / play-pause logic that the unit tests cover is the logic that runs: the
   playback service routes the player's next/previous commands (used by both the in-app bar and the
   notification) through `PlaybackQueuePolicy`, and its swipe-away handling through `PlaybackStopPolicy` —
   proved by reading `MusicPlaybackService.kt` for those call sites.
6. [machine, device] If `adb devices` lists a device: after `./gradlew :app:installDebug` and
   `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`, `adb logcat -d -b crash`
   holds no entry for `com.example.myapplication`, and `adb shell dumpsys activity top` shows `MusicFragment`.
   With no device attached this criterion is carried by #8–#16 instead.
7. [machine] `README.md` describes the music player and its features, and its package tree lists every new
   package (music screen, song and player repositories, playback service).
8. [human] **Asking for the music.** Fresh install (or Settings → Apps → the app → Storage → Clear data).
   Open the app → it opens on the **Music** tab and the system asks for "Music and audio" (Android 13+) or
   "Files and media" (Android 12 and older). Tap **Don't allow** → the screen shows a readable message saying
   the permission is needed and a button to grant it (or to open Settings after a second refusal); no blank
   screen, no crash. Tap the button and allow → the song list appears without restarting the app.
9. [human] **The song list.** With at least 15 songs on the phone: every row shows the song title and artist
   in text that is easy to read against the background, in both the phone's light and dark mode; the list
   scrolls smoothly to the last song. With no songs on the phone: a readable "no songs" message shows instead
   of an empty area. Tapping the Home and Setting tabs and back to Music works; the Music tab looks selected
   while on it.
10. [human] **Tap to play.** Tap any song → you hear it within about a second; a now-playing bar appears with
    that song's title and artist and a **Pause** button; the tapped row is marked as the one playing.
11. [human] **Play / pause in the app.** While a song plays, tap **Pause** → the sound stops and the button
    turns into **Play**. Tap **Play** → the sound continues from the same spot, not from the start.
12. [human] **Next / previous in the app.** Tap **Next** → the song below starts and the bar shows its title.
    Tap **Previous** → the song above starts (always the previous song, even mid-song). On the last song, Next
    goes to the first song; on the first song, Previous goes to the last. The three buttons are clearly
    visible and easy to hit with a thumb.
13. [human] **Notification permission.** On Android 13+ with a fresh install, the first time you tap a song the
    system asks to allow notifications. Tap **Don't allow** → the song still plays in the app, no crash.
14. [human] **Controls in the notification.** Allow notifications, play a song, press Home, pull down the
    notification shade → a music notification shows the song title and artist with Previous, Play/Pause and
    Next. Tap each: Pause stops the sound and the icon flips, Play resumes, Next/Previous change the song and
    the notification title updates. Reopen the app → its bar shows the same song and state. The same controls
    also work from the lock screen.
15. [human] **Tapping the notification.** From another app (or the Setting tab), tap the music notification →
    the app opens on the Music tab showing the playing song.
16. [human] **Background playback.** While playing, swipe the app away from Recents → the music keeps playing
    and the notification stays. Pause from the notification, then swipe the app away again (or clear the
    notification) → the notification disappears and nothing keeps running.

## Constraints

- Music source: songs already on the device (MediaStore audio marked as music). No network, no streaming.
- Playback uses **AndroidX Media3 1.8.0** (`media3-exoplayer`, `media3-session`, `media3-common`) with a
  `MediaSessionService` as the foreground service; its media notification is the one Media3 provides.
- The **Music** screen is a new bottom-bar tab placed left of Home, and it becomes the app's start
  destination. Home and Setting stay as they are (Home keeps its own permission sheet).
- The queue is every song on the device in list order, starting at the tapped song. Next on the last song
  wraps to the first; Previous always goes to the previous song and on the first song wraps to the last; when
  the last song finishes by itself, playback continues with the first (repeat-all). No shuffle, no seek bar,
  no playlists.
- Swiping the app away while **playing** keeps playing; while **paused** (or with nothing queued) it stops the
  service and removes the notification.
- The app uses one fixed dark colour scheme regardless of the phone's light/dark setting (the existing
  `CoreLayout` paints every screen black, so a light scheme would render dark text on black).
- Playback icons are vector drawables; no new icon library.
- Project rules in `CLAUDE.md` and `.claude/*.md` apply (clean architecture layers, Koin, KDoc with
  `@author Phong-Kaster`, README package tree).

## Verification Evidence Required

| Criterion | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1 | machine | `./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL` | n/a |
| 2 | machine | `./gradlew :app:testDebugUnitTest` → `BUILD SUCCESSFUL`; the five named `TEST-*.xml` files, `failures="0"` | n/a |
| 3 | machine | `./gradlew :app:lintDebug` → `BUILD SUCCESSFUL`; lint text report `0 errors` | n/a |
| 4 | machine | merged manifest file contents | n/a |
| 5 | machine | `MusicPlaybackService.kt` call sites of `PlaybackQueuePolicy` / `PlaybackStopPolicy` | n/a |
| 6 | machine | `adb devices`, `adb logcat -d -b crash`, `adb shell dumpsys activity top` output | n/a |
| 7 | machine | `README.md` contents | n/a |
| 8 | human | fresh install → permission dialog → deny → message + button → allow → list | ☐ |
| 9 | human | 15+ songs: readable rows in light and dark mode, scroll to end; 0 songs: message; tab switching | ☐ |
| 10 | human | tap a song → sound, now-playing bar with title/artist/Pause, row marked | ☐ |
| 11 | human | Pause → silence + Play icon; Play → resumes same spot | ☐ |
| 12 | human | Next/Previous incl. wrap at both ends; buttons visible and easy to hit | ☐ |
| 13 | human | Android 13+: notification prompt on first tap; deny → still plays | ☐ |
| 14 | human | shade + lock screen controls work and match the app | ☐ |
| 15 | human | tap notification → app opens on Music | ☐ |
| 16 | human | swipe away while playing → keeps playing; paused + swipe → notification gone | ☐ |
