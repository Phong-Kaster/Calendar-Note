# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

### Iteration 0 (bootstrap) - 2026-09-24
- **Phase:** none (bootstrap)
- Attempted: read PRD, surveyed `main` (@ `027d3ea`, pristine skeleton), verified toolchain (`assembleDebug` + `testDebugUnitTest` green, 1 test; `lintDebug` red with 4 pre-existing `MissingTranslation` errors), `adb devices` shows one device. Created `loop/music-player`. Two Capable analysts (conflict analysis + tiers; critique of decomposition, DoD classes and tiers). Conventions survey and DoD proposal were done by the Iteration itself, cross-checked against the prior run's PROJECT.md on `loop/calendar-note-app` (re-verified only what is true of `main`).
- Learned: `main` lint is red; `CoreBottomBar.kt` hard-codes its tab lists; `PermissionUtil.kt` would be touched by two tasks → both made Iteration-owned; `seekToPrevious` restarts after ~3 s → ForwardingPlayer in T-003; media-session notifications are exempt from POST_NOTIFICATIONS on 33+.
- Reconciled: critique folded in — DoD 3/4 rewritten to target plain-Kotlin seams; DoD 9 adds exported + intent-filter; DoD 7 adds "grant loads without restart"; DoD 10 adds "previous after 10 s"; DoD 12 adds "no second copy"; DoD 13 notes MIUI. Kept T-003 whole (the a/b split would share the same screen files and run sequentially anyway). Kept T-001 separate (disjoint scope, runs in parallel with T-002). Tiers: T-001 Fast (both analysts agree), others Capable. Knowledge: PROJECT.md created with C-01…C-05. Assumptions A-001…A-006. Queued D-001.

### Iteration 1 - 2026-09-24
- **Phase:** 1 — T-001 (Fast, `haiku`), T-002 (Capable, `opus`), dispatched in parallel.
- Recovery: tree clean apart from runtime-provisioned `DECISIONS.md` (committed), `.harness/loop/`, `.claude/agents/` (runtime files, left untracked).
- Consumed D-001 (approved as proposed). Scope check: both manifests disjoint and inside scope; union matched `git status`.
- Wired: coroutines-test dep, audio permissions, nav (`musicFragment` start, `toMusic`, pops to Music), `BottomBarDestination.Music` + `CoreBottomBar`, Koin, 7 new strings en+de + 4 missing German strings (A-006), README created (none existed).
- Build/test/lint green first time: 25 tests, lint 0 errors. Device: launch OK, no crash; `pm grant` / `install -g` refused (MIUI).
- Review (Capable, fresh): 0 Critical, 3 Major, 5 Minor. Fixed: permission dead-tap/dismiss (M1), system bars dark in light mode (M2), marquee instead of ellipsis (M3), surface ramp (m4), unused import (m8). Recorded: `title ASC` case-sensitivity (m5), `formatDuration` layer (m6), `SongRow` name clash (m7). Re-verified green.
- Knowledge: C-06 (single-line text = marquee), C-07 (always-dark system bars), device facts (no `pm grant`). Suggestion S-01 added.

## Archived Decisions

- **D-001** (DoD approval, bootstrap) — answered 2026-09-24: "Approved as proposed … all 7 `human` criteria (6, 7, 10, 11, 12, 13, 14) accepted in full. Assumptions A-001 … A-006 are not overturned." Applied iteration 1.

### Iteration 2 - 2026-09-24
- **Phase:** 2 — T-003 (Capable, `opus`), one Worker.
- Recovery: tree clean apart from runtime files (`.harness/loop/`, `.claude/agents/`, left untracked). No new decisions in `DECISIONS.md`.
- Scope check: Worker files inside Declared File Scope; union matched `git status`.
- Wired: Media3 1.8.0 (`exoplayer`, `session`, `common`) + `material-icons-extended` (BOM), manifest FGS permissions + `MusicPlaybackService`, Koin `PlayerRepository` + `MusicViewModel(playerRepository=)`, strings `play`/`pause`/`next_song`/`previous_song` en+de, README features/stack/tree.
- First build hit a Windows file lock on `classes.jar` (environment); `gradlew --stop` + rerun → green. 32 tests.
- Review (Capable, fresh): 1 blocking (B-1 shared connection released by old ViewModel), 3 major (M-1 dead controller, M-2 stale state, M-3 evidence gap only — DoD 10/11 are human), 3 minor (exported service accepts any controller — accepted; trailing content lambdas — fixed; README — fixed by Iteration). Re-dispatched Worker (attempt 2) with B-1/M-1/M-2/minor → green, 34 tests, lint 0 errors; device relaunch 0 crashes.
- Knowledge: C-08 (shared singleton connections are ref-counted, never torn down by one screen). Assumptions: A-002/5/6 SHA `ccf22b3` filled; A-001/3/4 first built on here.

### Iteration 3 - 2026-09-24
- **Phase:** 3 — T-004 (Capable, `opus`), one Worker.
- Recovery: tree clean apart from runtime files (`.harness/loop/`, `.claude/agents/`, left untracked). No new decisions.
- Attempt 1: green build, rejected by fresh review (B-1 notification did not land on Music; N-1 `stopSelf` blocked by bound controller; N-2 failed player counted as playing). Tier-1: scope + `MainActivity.kt`. Attempt 2: extra + `CLEAR_TOP|SINGLE_TOP`, `onNewIntent` → `toMusic`, `pauseAllPlayersAndStopSelf()`, IDLE excluded → green.
- Re-review: 0 blocking; fixed by Iteration: skip redundant `toMusic` when Music is current (cold start), two missing `@author` tags. Recorded: notification extra replays on reopen from Recents (harmless while Music is the start destination).
- Build/test/lint green: 34 tests, lint 0 errors. Device disconnected mid-install (serial now `10AECY1ZXG003MQ`, then gone) — no device check.
- Assumptions A-001/3/4: SHA `e3d77de` + revert filled. All tasks complete → PARTIAL-candidate recorded.
