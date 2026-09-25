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
