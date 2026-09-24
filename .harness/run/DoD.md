# Definition of Done

> Derived from `PRD.md` at bootstrap. Human-owned after approval: the engine may propose changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence, and must declare WHO can verify it (ADR-015).
>
> PRD (verbatim): "xây dựng một ứng dụng nghe nhạc đơn giản, có play/pause , next , previous song và có
> foreground server để bật tắt music player từ notification. Nguồn nhạc: nhạc có sẵn trên máy."
> Readings of the gaps in that text are recorded as A-001 … A-006 in `ASSUMPTIONS.md`.

## Status

- [x] APPROVED — D-001, answered in `.harness/run/DECISIONS.md` on 2026-09-24 ("Approved as proposed"), all 15 criteria and classes unchanged.
      Approval covers the Verification Class of each criterion, not only its wording.

## Acceptance Criteria

1. [machine] The whole tree builds, its unit tests pass and lint is clean: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` prints `BUILD SUCCESSFUL`, lint reports `0 errors`, and the test results show more than the skeleton's single test with 0 failures.
2. [machine] The app installs and starts without crashing on the attached device, both with the audio permission granted and revoked (`pm grant` / `pm revoke` before `am start`): no entry for `com.example.myapplication` in `adb logcat -b crash`, and `dumpsys activity activities` shows `MainActivity` resumed.
3. [machine] The song library reads only music already on the device: the MediaStore query built by the app selects `IS_MUSIC != 0` and sorts by title, and a device row maps to a song with title, artist (with a fallback when unknown), duration and a playable content URI — proved by JVM unit tests on the plain-Kotlin query spec and row mapper.
4. [machine] The app asks for the right permission per Android version: a unit test proves `READ_MEDIA_AUDIO` for API ≥ 33 and `READ_EXTERNAL_STORAGE` for API ≤ 32, and the merged debug manifest declares `READ_MEDIA_AUDIO` and `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"`.
5. [machine] The Music screen's state logic is correct: permission denied → "permission needed" state; granted with no songs → empty state; granted with songs → the list in repository order — proved by ViewModel unit tests over a fake repository.
6. [human] The Music screen is the first screen and shows the device's songs — open the app on a phone that has music files, grant the permission; expect a scrollable list, each row with title, artist and duration, all clearly legible; a very long title stays on one line and does not push the duration off the row.
7. [human] Permission-denied and empty states are understandable and recoverable — deny the permission on first launch; expect an explanation and a button that brings the permission back (system dialog, or app settings if denied permanently); after granting, the list appears without restarting the app. On a phone with no music, expect a "no songs" message rather than a blank screen.
8. [machine] The playback controls are wired: tapping song *i* asks the player to play the whole list starting at *i*; the play/pause, next and previous buttons each reach the player; the now-playing state shown by the screen follows the player's reported state — proved by ViewModel unit tests over a fake player.
9. [machine] Playback runs in a foreground media service: the merged debug manifest declares a service with `android:foregroundServiceType="mediaPlayback"`, `exported="true"` with the `androidx.media3.session.MediaSessionService` intent-filter, and the permissions `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
10. [human] In-app playback works — tap a song; expect to hear it and see a now-playing bar with its title and artist. Tap pause → sound stops and the icon becomes "play"; tap play → it resumes. Tap next → the next song plays. After 10 s of a song, tap previous → the *previous* song plays (not a restart). Next on the last song → first song; previous on the first song → last song.
11. [human] The notification controls the player — while a song plays, pull down the notification shade; expect a media notification with the song title and previous / play-pause / next buttons. Each button works. Press the phone's Home button: music keeps playing. Pause from the notification, reopen the app: the now-playing bar shows paused. On Android 13+ the notification also appears if the notification permission was denied.
12. [human] Tapping the notification's body opens the app on the Music screen, and pressing Back once from there leaves the app (no second copy of the app underneath).
13. [human] Turning the player off — pause, then swipe the app away from Recents: the notification disappears within a few seconds. While playing, swipe the app away: music and notification keep going. (Some MIUI builds kill the process on swipe regardless; note the device if so.)
14. [human] Theme contrast — on the Music screen (list, states, now-playing bar) and on the existing Home and Setting screens, in both system light and dark mode, every text and icon is clearly legible against its background.
15. [machine] `README.md` exists at the repository root and describes the app's purpose, features and tech stack, with a package tree that lists every new package added by this run (checked by reading the file against `git diff --stat main`).

## Constraints

- Follow `CLAUDE.md` and `.claude/*.md`: clean architecture layers, Koin DI, Fragment + Compose screens via `CoreFragment`, `_uiState.value = _uiState.value.copy(...)`, KDoc with `@author Phong-Kaster`.
- Music source is local only (MediaStore); no network streaming.
- `minSdk 24`, `targetSdk 36` unchanged; Android 14+ foreground-service-type rules apply.
- Never touch `main`; all work on `loop/music-player`.

## Verification Evidence Required

| Criterion | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1 | machine | `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` → `BUILD SUCCESSFUL`; lint text report `0 errors`; `TEST-*.xml` counts | n/a |
| 2 | machine | `adb install -r app-debug.apk`; `pm grant`/`pm revoke`; `am start -n com.example.myapplication/com.example.skeleton.MainActivity`; `logcat -b crash -d`; `dumpsys activity activities` | n/a |
| 3 | machine | `SongMapperTest` (or equivalent) in `testDebugUnitTest` | n/a |
| 4 | machine | permission unit test + merged manifest under `app/build/intermediates/merged_manifest*/debug/` | n/a |
| 5 | machine | `MusicViewModelTest` | n/a |
| 6 | human | open app with music on phone, grant permission → legible, scrollable list with title/artist/duration | ☐ |
| 7 | human | deny permission → explanation + fix button → grant → list without restart; no-music phone → "no songs" message | ☐ |
| 8 | machine | `MusicViewModelTest` playback cases | n/a |
| 9 | machine | merged manifest `<service>` + permissions | n/a |
| 10 | human | tap song, play/pause, next, previous after 10 s, wrap at both ends | ☐ |
| 11 | human | notification shade: title + 3 buttons working; background playback; state synced back | ☐ |
| 12 | human | tap notification → Music screen; Back once leaves the app | ☐ |
| 13 | human | paused + swipe from Recents → notification gone; playing + swipe → keeps playing | ☐ |
| 14 | human | Music, Home, Setting screens legible in system light and dark mode | ☐ |
| 15 | machine | `README.md` read against `git diff --stat main` | n/a |
