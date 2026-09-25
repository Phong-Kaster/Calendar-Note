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

- **Status:** pending
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
