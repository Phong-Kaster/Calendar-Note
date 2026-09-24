# LOOP ISSUES REPORT

_Last updated: 2026-09-24 - branch `loop/music-player` - iteration 4 (Verifier) - run ended `DONE_PARTIAL`_

## Abandoned tasks

None.

## Unreachable tasks

None.

## Decisions awaiting an answer

None (D-001 approved 2026-09-24).

## Review findings not fixed

| Sev | Where | Finding |
|---|---|---|
| Minor | `data/mapper/SongMapper.kt:41` | Sort `title ASC` may be case-sensitive in MediaStore's SQLite (lowercase titles after uppercase). Unverified — no music-file check possible by command. |
| Minor | `ui/fragment/music/component/SongRow.kt` | UI imports `formatDuration` from `data.mapper` (UI → data dependency). |
| Minor | `data/mapper/SongMapper.kt:54` | Data class `SongRow` shares its simple name with the `SongRow` composable. |
| Minor | `data/service/MusicPlaybackService.kt` | Exported session service (required by DoD 9) accepts any controller — any installed app can control playback. Normal for a media app; add an `onConnect` allow-list if unwanted. |
| Minor | `MainActivity.kt` (`onCreate`) | A task started by the notification replays `EXTRA_OPEN_MUSIC` when reopened from Recents on Android 12+. Harmless while Music is the start destination. |
| Info | `data/repository/impl/PlayerRepositoryImpl.kt` | Reference counting / reconnect logic has no unit test (it sits on `MediaController` + `Log`, C-04); covered only by the ViewModel-level fake and by DoD 10/11 on a device. |

## Awaiting a person

Every `machine` criterion the Verifier could run was re-proved on 2026-09-24 (iteration 4): build + 34 unit
tests (0 failures) + lint (0 errors), merged manifest (DoD 4, 9), query spec / mapper / permission / ViewModel
tests (DoD 3, 4, 5, 8), README tree against `git diff --stat main` (DoD 15). DoD 2 could not be re-run — see
limits below. These 7 criteria need a person. Install: `./gradlew :app:installDebug` on an Android phone that
has a few music files. Mark each pass / fail.

1. **DoD 6 — song list.** Open the app, grant the audio permission. Expect: the Music tab is the first screen;
   a scrollable list; each row shows title, artist and duration, all legible. A very long title stays on one line
   (it scrolls sideways) and the duration stays visible at the right.
2. **DoD 7 — denied / empty states.** Clear app data, open the app, tap *Deny*. Expect: an explanation and a
   button; the button brings back the system dialog (or opens app settings after a permanent deny). Grant →
   the list appears without restarting. On a phone with no music: a "no songs" message, not a blank screen.
3. **DoD 10 — in-app playback.** Tap a song: you hear it and a now-playing bar shows title + artist. Pause →
   silence, icon becomes *play*; play → resumes. Next → next song. After 10 s, previous → the *previous* song
   (not a restart). Next on the last song → first song; previous on the first → last song.
4. **DoD 11 — notification.** While playing, pull down the shade. Expect: a media notification with the title
   and previous / play-pause / next; each works. Press Home: music keeps playing. Pause from the notification,
   reopen the app: the bar shows paused. Android 13+: deny notification permission — the media notification still appears.
5. **DoD 12 — notification tap.** Tap the notification body. Expect: the app opens on Music; pressing Back once
   leaves the app (no second copy underneath).
6. **DoD 13 — turning off.** Pause, swipe the app away in Recents: the notification disappears within a few
   seconds. Start playing, swipe away: music and notification continue. (MIUI may kill the process — note the device.)
7. **DoD 14 — contrast.** In system light mode, then dark mode, look at Music (list, denied/empty states,
   now-playing bar), Home and Setting. Expect: every text and icon clearly legible against its background.

## Known verification limits

- DoD 2 (`machine`) was **not re-proved by the Verifier**: no device attached (`adb devices` empty) on 2026-09-24. Last evidence: iteration 2 relaunch, not-granted permission only (0 crashes, MainActivity resumed). A person can close this by running the DoD 2 commands in `DoD.md` with a device attached.

- DoD 2 (launch with permission granted *and* revoked): the device refuses `pm grant` / `pm revoke` and `install -g`, so only the not-granted launch was proved by command (MainActivity resumed, 0 crashes).
- Playback (DoD 10/11, incl. "previous goes to the previous song") is unverified by command: the device refuses injected taps.
- Notification tap and swipe-away (DoD 12/13, T-004) were not exercised on a device: the device disconnected mid-install in iteration 3.

## Assumptions recorded

See `.harness/run/ASSUMPTIONS.md` (A-001 … A-006). Tier-1 amendments in `AMENDMENTS.md` (iterations 1 and 3).
