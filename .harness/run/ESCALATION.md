# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> This file is the engine's own log. **Answer in `.harness/run/DECISIONS.md`, never here** - copy an
> entry's id as a heading there and write your decision underneath it, then re-run. The engine is
> denied write access to `DECISIONS.md`, mechanically, so nothing it does here can ever race what you
> write there. Answer any number of entries; unanswered ones stay queued and the tasks they name stay
> unselectable.

---

## D-001 - Approve the Definition of Done and the standing toolchain capabilities

- **Status:** answered (iteration 1 — approved as written; see `AMENDMENTS.md`)
- **Type:** DoD approval + Capability grant
- **Iteration:** 0 (bootstrap)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** T-001, T-002, T-003, T-004

### Question

1. Approve `.harness/run/DoD.md` (16 criteria: 7 `machine`, 9 `human`) — edit it freely first. Its
   **Constraints** section records the product choices the PRD left open; change any you disagree with:
   - Media3 1.8.0 `MediaSessionService` as the foreground service.
   - Music is a new tab left of Home and becomes the start screen; Home and Setting unchanged.
   - Queue = all device songs in list order from the tapped one; Next/Previous wrap at both ends; Previous
     always goes to the previous song; repeat-all when the last song ends.
   - Swipe away from Recents: keeps playing if playing, stops (notification gone) if paused.
   - Notification permission asked on the first song tap (Android 13+); refusing still plays.
   - One fixed dark theme for the whole app.
2. Approve the standing capabilities below (permanent, for this repository's toolchain).
3. Optionally approve pushing the Loop Branch (goal-scoped, second block below).

### Context

Bootstrap could not verify the toolchain: `./gradlew` is not in the baseline ledger and was refused. The
previous run of the same feature (`loop/music-player`, same base commit) verified these commands, and found
`lintDebug` already failing on `main` with 4 `MissingTranslation` errors — DoD #3 requires fixing them (the
plan does it in Phase 1). No device check has been run on this branch.

### Options Considered

1. Approve as written - consequences: Phase 1 (dark theme + Music list) starts next run.
2. Edit the DoD / constraints, then approve - consequences: the plan is re-derived from your edits.
3. Approve the DoD but grant fewer capabilities - consequences: without build/test/lint the loop cannot
   verify anything and will stop again with a capability request.

### Engine Recommendation

Option 1. The device-affecting commands (install, launch) are proposed goal-scoped rather than standing, and
`updateDebugScreenshotTest` is deliberately absent (no screenshot baseline exists, and re-recording one must
stay a human decision).

### Proposed Capabilities (if any)

```json
{
  "intent": "Build, unit-test and lint the app to verify every checkpoint; read device state without changing it",
  "command": "./gradlew :app:assembleDebug | :app:testDebugUnitTest | :app:lintDebug | :app:processDebugMainManifest | --stop; adb devices / logcat -d / dumpsys",
  "scope": "this repository's build outputs (app/build); read-only queries of an attached device",
  "lifetime": "permanent",
  "allow": [
    "Bash(./gradlew :app:assembleDebug*)",
    "Bash(./gradlew :app:testDebugUnitTest*)",
    "Bash(./gradlew :app:lintDebug*)",
    "Bash(./gradlew :app:processDebugMainManifest*)",
    "Bash(./gradlew --stop)",
    "Bash(set -o pipefail; ./gradlew :app:*)",
    "Bash(adb devices*)",
    "Bash(adb logcat -d*)",
    "Bash(adb shell dumpsys*)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

```json
{
  "intent": "Install the debug build on the attached device, launch it, and push the Loop Branch so the run survives a machine failure and can be reviewed elsewhere",
  "command": "./gradlew :app:installDebug; adb shell am start -n com.example.myapplication/...; git push origin loop/music-player-v2",
  "scope": "the attached test device (app com.example.myapplication only); the loop/music-player-v2 branch on origin, never main",
  "lifetime": "goal",
  "allow": [
    "Bash(./gradlew :app:installDebug*)",
    "Bash(adb shell am start -n com.example.myapplication/*)",
    "Bash(git push origin loop/music-player-v2*)"
  ],
  "target_ledger": ".harness/run/capabilities.json"
}
```

### Decision

<!-- Answered in `.harness/run/DECISIONS.md`, under a heading `## D-001` - never here. -->

---

## D-002 - The capabilities approved in D-001 are not in effect: write the two ledger files

- **Status:** answered (iteration 2 — both ledgers written; `./gradlew :app:assembleDebug` verified; see `AMENDMENTS.md`)
- **Type:** Capability grant (materialise an approval already given)
- **Iteration:** 1
- **Timestamp:** 2026-09-25
- **Blocks tasks:** T-001, T-002, T-003, T-004

### Question

D-001 approved the standing and goal-scoped capabilities, but neither ledger file exists, so the Runtime
compiled only the baseline and `./gradlew :app:assembleDebug` is still refused ("requires approval"). The
engine is denied Write on both ledgers by design, so only you can create them. Create the two files below
(content is exactly what D-001 approved), then re-run.

### Context

`run.ps1` `Compile-PermissionSettings` reads only `.harness/loop/capabilities/baseline.json`,
`.harness/knowledge/capabilities.json` and `.harness/run/capabilities.json`, and only their `allow` arrays.
An answer in `DECISIONS.md` is not read by the compiler. Without build/test/lint no checkpoint can be
verified, so Phase 1 was not started (no code written in iteration 1).

### Options Considered

1. Create both ledger files as below - consequences: Phase 1 (dark theme + Music list) starts next run.
2. Create only the standing ledger - consequences: Phase 1 can run; device install/launch and push stay
   unavailable, so device checks for the `human` criteria fall entirely to you.

### Engine Recommendation

Option 1.

### Proposed Capabilities (if any)

`.harness/knowledge/capabilities.json`:

```json
{
  "entries": [
    {
      "intent": "Build, unit-test and lint the app to verify every checkpoint; read device state without changing it",
      "command": "./gradlew :app:assembleDebug | :app:testDebugUnitTest | :app:lintDebug | :app:processDebugMainManifest | --stop; adb devices / logcat -d / dumpsys",
      "scope": "this repository's build outputs (app/build); read-only queries of an attached device",
      "lifetime": "permanent",
      "allow": [
        "Bash(./gradlew :app:assembleDebug*)",
        "Bash(./gradlew :app:testDebugUnitTest*)",
        "Bash(./gradlew :app:lintDebug*)",
        "Bash(./gradlew :app:processDebugMainManifest*)",
        "Bash(./gradlew --stop)",
        "Bash(set -o pipefail; ./gradlew :app:*)",
        "Bash(adb devices*)",
        "Bash(adb logcat -d*)",
        "Bash(adb shell dumpsys*)"
      ]
    }
  ]
}
```

`.harness/run/capabilities.json`:

```json
{
  "entries": [
    {
      "intent": "Install the debug build on the attached device, launch it, and push the Loop Branch",
      "command": "./gradlew :app:installDebug; adb shell am start -n com.example.myapplication/...; git push origin loop/music-player-v2",
      "scope": "the attached test device (app com.example.myapplication only); the loop/music-player-v2 branch on origin, never main",
      "lifetime": "goal",
      "allow": [
        "Bash(./gradlew :app:installDebug*)",
        "Bash(adb shell am start -n com.example.myapplication/*)",
        "Bash(git push origin loop/music-player-v2*)"
      ]
    }
  ]
}
```

After creating the files, add `## D-002` with "done" (or the option chosen) to `DECISIONS.md`.

### Decision

<!-- Answered in `.harness/run/DECISIONS.md`, under a heading `## D-002` - never here. -->

---

## D-003 - Human Verification Request: try the music player on a phone (DoD #8–#16)

- **Status:** answered (iteration 6 — all 14 rows PASS)
- **Type:** Human verification (ADR-015)
- **Iteration:** 5 (Verifier)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** none — every task (T-001..T-004) is complete. This entry blocks **`DONE`** (and the
  Cleanup Commit) only.

### Question

Every `machine` criterion (#1–#7) was re-proved from scratch this iteration. The 9 `human` criteria need a
person looking at the running app. Please work through the checklist below and mark each row
`PASS` or `FAIL: <what you saw>`.

### Context

Fresh evidence (iteration 5, Verifier): `./gradlew :app:assembleDebug` BUILD SUCCESSFUL;
`./gradlew :app:testDebugUnitTest --rerun` BUILD SUCCESSFUL — AudioPermissionTest 10, SongMapperTest 12,
MusicUiStateTest 6, PlaybackQueuePolicyTest 10, PlaybackStopPolicyTest 4 (plus
NotificationPermissionDecisionTest 4), 0 failures; `./gradlew :app:lintDebug --rerun` 0 errors / 63 warnings;
merged manifest holds all 5 permissions and the `mediaPlayback` `MediaSessionService`; `MusicPlaybackService`
routes next/previous through `PlaybackQueuePolicy` (`QueuePolicyPlayer`, the player handed to the
`MediaSession`) and `onTaskRemoved` through `PlaybackStopPolicy`; on device `b56e2819` the app installed,
`MusicFragment` resumed, no crash entry; README covers the player and every new package.

Known quirk (review note from T-004): on Android 13+, if you allow notifications *after* the song already
started, the notification may only appear at the next player event (e.g. your first Pause). Note it in row 14
if it bothers you.

### How to run this check

Install: `./gradlew :app:installDebug` (or it is already installed on `b56e2819`). Rows 8 and 13 need a
fresh start: Settings → Apps → *Android Compose Skeleton* → Storage → Clear data (and on Android 13+
also reset its notification permission). Put at least 15 songs on the phone for rows 9–12.

| # | DoD | Open | Do | Expect | Result |
|---|---|---|---|---|---|
| 1 | 8 | the app, after Clear data | open it; when asked for "Music and audio" (13+) / "Files and media" (≤12) tap **Don't allow** | app is on the **Music** tab; a readable message says the permission is needed, with a button; no blank screen, no crash | |
| 2 | 8 | same screen | tap the button (tap again after a second refusal → it opens Settings); allow | the song list appears without restarting the app | |
| 3 | 9 | Music tab, ≥15 songs | read the rows; scroll to the last song; repeat with the phone in light mode and in dark mode | every row's title and artist is easy to read against the background in both modes; scrolling is smooth to the end | |
| 4 | 9 | Music tab, 0 songs on the phone (optional if hard to set up) | open the tab | a readable "no songs" message, not an empty area | |
| 5 | 9 | bottom bar | tap Home, then Setting, then Music | each tab opens; Music looks selected while on it | |
| 6 | 10 | Music tab | tap any song | sound within ~1 s; a now-playing bar shows that song's title, artist and a **Pause** button; the tapped row is marked as playing | |
| 7 | 11 | now-playing bar | tap **Pause**, wait, tap **Play** | silence and the button becomes **Play**; Play resumes from the same spot, not the start | |
| 8 | 12 | now-playing bar | tap **Next**; tap **Previous** (also mid-song); play the last song and tap Next; play the first and tap Previous | Next → song below; Previous → song above (always); last → first; first → last; bar title follows; the 3 buttons are clearly visible and easy to hit with a thumb | |
| 9 | 13 | Android 13+, after Clear data | tap a song for the first time; tap **Don't allow** on the notification prompt | the prompt appears; after refusing, the song still plays, no crash | |
| 10 | 14 | allow notifications, play a song, press Home | pull down the shade; tap Pause, Play, Next, Previous | the notification shows title, artist, Previous / Play-Pause / Next; each works, icon flips, title updates; reopen the app → bar shows the same song and state | |
| 11 | 14 | lock screen while playing | use the same controls | they work the same | |
| 12 | 15 | another app (or Setting tab) while playing | tap the music notification | the app opens on the **Music** tab showing the playing song | |
| 13 | 16 | while playing | swipe the app away from Recents | music keeps playing; the notification stays | |
| 14 | 16 | notification | tap Pause, then swipe the app away (or clear the notification) | the notification disappears and nothing keeps running | |

### Decision

<!-- Answer in `.harness/run/DECISIONS.md` under `## D-003`: PASS / FAIL per row, plus anything noticed
     that no row asked about. -->

---

## D-004 - Allow `git rm` so the run can make its Cleanup Commit and finish

- **Status:** pending
- **Type:** Capability grant (Tier 2)
- **Iteration:** 6 (Verifier)
- **Timestamp:** 2026-09-25
- **Blocks tasks:** none — every task is complete and every DoD criterion is proved / signed off
  (#1–#7 re-proved this iteration, #8–#16 via D-003). This entry blocks the **Cleanup Commit** and
  **`DONE`** only.

### Question

ENGINE §11 step 4 finishes the run by removing `.harness/run/` from the branch tip. `git rm -r -q .harness/run`
was refused ("This command requires approval"); no ledger grants `git rm`. May the engine run it?

### Context

The whole record (D-003 sign-off, fresh evidence) is already committed at `6b8c80e`, so removing
`.harness/run/` loses nothing from history. `.harness/ISSUES.md` stays. Every future run ends the same way,
so this is needed once per run.

### Options considered

1. **Permanent grant (recommended)** — target `.harness/knowledge/capabilities.json` (standing ledger).
   Justification: every run's completion needs exactly this command, and the rule is limited to the
   `.harness/run` path.
2. **Goal grant** — target `.harness/run/capabilities.json`; expires with this run (the next run asks again).
3. **Do it yourself** — run `git rm -r -q .harness/run` and commit on `loop/music-player-v2`, then add
   `## D-004` with "done by hand". The next invocation will verify the tip and report `DONE`.

### Capability proposal (option 1)

```json
{
  "intent": "Make the ENGINE §11 Cleanup Commit: remove .harness/run/ from the Loop Branch tip once every DoD criterion is proved",
  "command": "git rm -r -q .harness/run",
  "scope": "the .harness/run directory on the current loop/* branch only",
  "lifetime": "permanent",
  "allow": [
    "Bash(git rm -r -q .harness/run)"
  ]
}
```

For option 2 use the same entry with `"lifetime": "goal"` in `.harness/run/capabilities.json`.

### Recommendation

Option 1.

### Decision

<!-- Answer in `.harness/run/DECISIONS.md` under `## D-004`. -->
