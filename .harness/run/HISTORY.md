# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

### Iteration 0 (bootstrap) - 2026-09-24
- **Phase:** none (bootstrap)
- Attempted: read PRD, surveyed `main` (@ `027d3ea`, pristine skeleton), verified toolchain (`assembleDebug` + `testDebugUnitTest` green, 1 test; `lintDebug` red with 4 pre-existing `MissingTranslation` errors), `adb devices` shows one device. Created `loop/music-player`. Two Capable analysts (conflict analysis + tiers; critique of decomposition, DoD classes and tiers). Conventions survey and DoD proposal were done by the Iteration itself, cross-checked against the prior run's PROJECT.md on `loop/calendar-note-app` (re-verified only what is true of `main`).
- Learned: `main` lint is red; `CoreBottomBar.kt` hard-codes its tab lists; `PermissionUtil.kt` would be touched by two tasks → both made Iteration-owned; `seekToPrevious` restarts after ~3 s → ForwardingPlayer in T-003; media-session notifications are exempt from POST_NOTIFICATIONS on 33+.
- Reconciled: critique folded in — DoD 3/4 rewritten to target plain-Kotlin seams; DoD 9 adds exported + intent-filter; DoD 7 adds "grant loads without restart"; DoD 10 adds "previous after 10 s"; DoD 12 adds "no second copy"; DoD 13 notes MIUI. Kept T-003 whole (the a/b split would share the same screen files and run sequentially anyway). Kept T-001 separate (disjoint scope, runs in parallel with T-002). Tiers: T-001 Fast (both analysts agree), others Capable. Knowledge: PROJECT.md created with C-01…C-05. Assumptions A-001…A-006. Queued D-001.

## Archived Decisions
