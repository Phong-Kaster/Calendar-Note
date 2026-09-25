# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> This file is the engine's own log. **Answer in `.harness/run/DECISIONS.md`, never here** - copy an
> entry's id as a heading there and write your decision underneath it, then re-run.

---

## D-001 - Approve the Definition of Done for the Material 3 player (13 criteria) and one goal capability

- **Status:** answered (iteration 2 — option 1; archived in HISTORY.md)
- **Type:** DoD approval + Capability grant
- **Iteration:** 1 (run 2 bootstrap)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** T-001, T-002, T-003, T-004

### Question

Approve `.harness/run/DoD.md` (edit it freely first — including each criterion's `machine` / `human` class), and
optionally grant the goal-scoped capability below so DoD #6 (install + start on the phone without a crash) can be
proved by the engine instead of by you.

### Context

The new PRD asks for Material 3 on the play screen and the notification: a separate Now Playing screen (art, title,
seek slider, big M3 buttons) opened from the mini bar, and a standard MediaStyle notification with album art, the
app's own small icon, theme accent and Material Symbols icons.

Choices the engine made (listed in `DoD.md` § Constraints and `STATE.md` § Assumptions — change any in the DoD):

1. **Accent colour and icons only where Android honours them.** Android 12+ colours the media card from the album
   art, and Android 13+ draws its own control icons, whatever the app sets. DoD #11 therefore requires the art, the
   title/artist and the app's small icon everywhere, and the accent + Material Symbols icons on Android 12 and older.
2. **Tapping the notification still opens the Music tab** (not Now Playing).
3. **No new image library**: album art is read with the platform `ContentResolver` from MediaStore's album-art address.
   Songs without art show a music-note placeholder.
4. **Text keeps the house style** (`customizedTextStyle`), controls are M3 (`Slider`, `FilledIconButton`, `IconButton`,
   `Surface`); no Material `Card` (house rule).
5. The mini bar gets no thumbnail.

The standing build/test/lint rules already in `.harness/knowledge/capabilities.json` cover everything else. The final
Cleanup Commit is covered by the baseline (`git rm -r .harness/run*`).

### Options Considered

1. Approve as written + grant the capability - consequences: DoD #6 proved by the engine; 7 `human` rows to try at the end.
2. Approve as written, no capability - consequences: the engine reclassifies #6 as `human` (you install and open the app yourself).
3. Edit the DoD, then approve - consequences: the plan follows your edits.

### Engine Recommendation

Option 1.

### Proposed Capabilities (if any)

A capability is only in effect once you write it into the ledger file (the Runtime compiles only the ledgers).
Add this entry to `.harness/run/capabilities.json` → `"entries": [ ... ]`:

```json
{
  "intent": "Install the debug build on the attached phone and start it, to prove DoD #6 (starts without crashing)",
  "command": "./gradlew :app:installDebug ; adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity",
  "scope": "this repository's debug APK; the attached test device",
  "lifetime": "goal",
  "allow": [
    "Bash(./gradlew :app:installDebug*)",
    "Bash(adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity*)"
  ],
  "target_ledger": ".harness/run/capabilities.json"
}
```

### Decision

<!-- Answer in `.harness/run/DECISIONS.md` under `## D-001` - never here. -->

---

## D-002 - Human Verification Request: try 7 things on the phone (DoD #7–#13), and plug the phone back in

- **Status:** answered (consumed iteration 6: #7 and #11 fail — no album art → T-006; the rest pass; see A-004)
- **Type:** Human Verification Request
- **Iteration:** 4 (run 2, Verifier)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** none remain — this blocks **completion** (the Cleanup Commit and `DONE`)

### Question

Every automatic check passed again with fresh evidence, except DoD #6: **no phone is attached** (`adb devices` lists
nothing), so the engine could not install and start the app. Please plug the phone in (USB debugging on), then try
the 7 checks below and mark each **pass** or **fail**.

### Context

Re-proved this iteration by the Verifier (who wrote none of the code):

- #1 `assembleDebug`, `testDebugUnitTest` (re-run), `lintDebug` (`--rerun-tasks`) → `BUILD SUCCESSFUL`; lint **0 errors**.
- #2 `PlaybackTimeTest` 16/16; #3 `AlbumArtUriTest` 4/4, `SongMapperTest` 14/14 (76 tests total, 0 failures).
- #4 0 hits for `RemoteViews` / custom-view names; `MediaSessionService()`, `setMediaNotificationProvider`,
  `ic_notification_music`, `setArtworkUri` all present.
- #5 `nowPlayingFragment` + `toNowPlaying` in the nav graph, `NowPlayingViewModel` in Koin, `seekTo(positionMs)`,
  README tree lists `nowplaying/`; both-locale strings covered by lint's `MissingTranslation` (an error) = 0.
- #6 **not re-proved** — no device. The next run re-proves it once the phone is attached.
- **Update, iteration 5:** #1–#5 re-proved again, and **#6 re-proved** on the attached phone (install OK, `MusicFragment`
  on top, 0 crash lines). The app is already installed; only the 7 checks below are still needed.

Checklist (install first: `./gradlew :app:installDebug`, then open the app):

1. **Now Playing opens (#7).** Music tab → tap any song → tap the mini "now playing" bar (not its buttons). Expect a
   separate full screen: a big square album picture (a song without one shows a clearly visible music note on a
   lighter panel), title and artist, a slider with elapsed time left and total time right, and big buttons previous /
   filled play-pause / next. Everything legible on the dark background; the play/pause icon stands out from its button.
2. **Seeking and the clock (#8).** Drag the slider to the middle and let go → the music jumps there and the time
   matches. While playing the time goes up once a second; pause → it stops; play → it continues.
3. **Controls and back (#9).** Tap next, previous, play/pause → the song changes / pauses / resumes; title, artist,
   picture, total time and slider (back to 0:00) update — also when a song ends by itself. Press system back (and the
   top-left arrow) → the song list, with the mini bar still showing the song and reacting to play/pause.
4. **Long names (#10).** Play a song with a very long title or artist, open Now Playing → the text scrolls sideways on
   one line; the buttons stay on screen.
5. **Notification shade (#11).** Play a song that has album art, pull down the shade → the standard media card with
   the art, title, artist and working previous / play-pause / next; the status bar shows the app's own music-note
   icon. (Android 13+ draws its own buttons and colours from the art — accepted.)
6. **Lock screen and Quick Settings (#12).** Lock the phone while playing → the lock-screen player shows the song and
   its buttons work. Unlock, open Quick Settings → the media player is there and works.
7. **Open from the notification (#13).** With Now Playing open, press Home, tap the notification → the app opens on
   the Music tab (list + mini bar), no crash; tapping the mini bar opens Now Playing again.

### Options Considered

1. All 7 pass, phone attached - consequences: the next run re-proves #6 on the phone, makes the Cleanup Commit, reports `DONE`.
2. Some fail - consequences: each failed item (with what you saw) becomes a fix task; the loop keeps working.
3. You cannot attach a phone - consequences: say so and confirm yourself that the app installs and opens without a
   crash; the engine cannot change #6's class on its own (the DoD is yours).

### Engine Recommendation

Attach the phone, run the checklist, and answer under `## D-002` in `DECISIONS.md` like: `1 pass, 2 pass, 3 fail —
<what you saw>, …`.

### Proposed Capabilities (if any)

none (the D-001 install/start capability is still granted)

### Decision

<!-- Answer in `.harness/run/DECISIONS.md` under `## D-002` - never here. -->

---

## D-003 - Human Verification Request: re-check 3 things on the phone after the album-art fix (DoD #7, #9, #11)

- **Status:** queued
- **Type:** Human Verification Request
- **Iteration:** 8 (run 2, Verifier)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** none remain — this blocks **completion** (the Cleanup Commit and `DONE`)

### Question

T-006 changed how album covers are read (Android 10+ now reads the song's embedded cover). That touches the three
checks below, so they must be looked at again. Please try them and mark each **pass** or **fail**. #8, #10, #12 and #13
stay signed from D-002.

### Context

Re-proved this iteration by the Verifier (who wrote none of the code), with fresh runs:

- #1 `assembleDebug`, `testDebugUnitTest --rerun`, `lintDebug --rerun-tasks` → `BUILD SUCCESSFUL`; lint **0 errors**
  (69 warnings).
- #2 `PlaybackTimeTest` 16/16; #3 `AlbumArtUriTest` 10/10, `SongMapperTest` 14/14 (82 tests, 0 failures).
- #4 0 hits for `RemoteViews` / custom-view names; `MediaSessionService()`, `setMediaNotificationProvider`,
  `ic_notification_music`, `setArtworkUri` present.
- #5 nav graph, Koin binding, `seekTo(positionMs)`, README `nowplaying/`; 45 string keys in each locale, lint
  `MissingTranslation` = 0.
- #6 `installDebug` → "Installed on 1 device" (`3H164700ALT00000`); `am start` OK; `dumpsys activity top` shows
  `MusicFragment`; 0 crash-buffer lines naming the app. **The new build is already installed.**

**You need a song that has a cover picture inside the file.** Per the D-002 addendum, the two songs checked before have
none, so they will (correctly) show the music-note placeholder. Copy one MP3 with an embedded cover to the phone (one
that shows a cover in another music app or on a computer), then open the app and let it rescan (reopen the Music tab).

Checklist:

1. **Now Playing shows the cover (#7).** Music tab → tap the song that has a cover → tap the mini "now playing" bar (not
   its buttons). Expect the full screen with that song's **cover picture** in the big square (a song with no cover shows
   a clearly visible music note on a lighter panel), title and artist, the slider with times, and big previous /
   filled play-pause / next buttons, all legible.
2. **Controls update the picture (#9).** On Now Playing tap next, previous and play/pause. Expect the song to change /
   pause / resume, and title, artist, **picture** (cover ↔ placeholder as the song changes), total time and slider (back
   to 0:00) to update — also when a song ends by itself. Press system back (and the top-left arrow) → the list, with
   the mini bar still showing the song and reacting to play/pause.
3. **Notification shows the cover (#11).** Play the song with a cover, pull down the shade → the standard media card
   with **that cover**, title, artist and working previous / play-pause / next; the status bar shows the app's own
   music-note icon. (Android 13+ draws its own buttons and colours from the art — accepted.)

### Options Considered

1. All 3 pass with a song that has a cover - consequences: the next run makes the Cleanup Commit and reports `DONE`.
2. Some fail - consequences: each failed item (with what you saw) becomes a fix task; the loop keeps working.
3. No song with a cover available, placeholder accepted - consequences: say so; the engine cannot sign #7/#11 on the
   cover part itself. Writing "accept placeholder for 1 and 3" counts as your sign-off.

### Engine Recommendation

Option 1: copy one MP3 with an embedded cover, run the 3 checks, and answer under `## D-003` in `DECISIONS.md` like:
`1 pass, 2 pass, 3 fail — <what you saw>`.

### Proposed Capabilities (if any)

none (the D-001 install/start capability is still granted)

### Decision

<!-- Answer in `.harness/run/DECISIONS.md` under `## D-003` - never here. -->
