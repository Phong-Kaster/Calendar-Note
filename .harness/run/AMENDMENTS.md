# AMENDMENTS

> Every plan mutation (Tier 1, logged) and every applied decision (with the human's rationale).

<!-- Newest first. -->

### Iteration 1 - applied D-001 (2026-09-25)
- **Decision:** Option 1 — approve as written. `DoD.md` approved (16 criteria, Verification Classes and
  Constraints unchanged; the human ticked the APPROVED box). Standing capabilities (gradle
  assembleDebug / testDebugUnitTest / lintDebug / processDebugMainManifest / --stop,
  `set -o pipefail; ./gradlew :app:*`, read-only adb) and goal-scoped capabilities (installDebug, `am start`,
  push `loop/music-player-v2`) approved.
- **Human rationale:** "The DoD matches the PRD (local music, play/pause, next/previous, foreground-service
  notification controls). Without build/test/lint the loop cannot verify any checkpoint; install/launch and
  pushing the loop branch are wanted for device checks and to back up the run."
- **Effect:** `DoD.md` is now immutable. T-001..T-004 released from D-001 and re-deferred on D-002: the
  approved rules were never written to the ledger files, so the Runtime did not grant them.
