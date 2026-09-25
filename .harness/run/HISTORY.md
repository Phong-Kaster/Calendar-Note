# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next.

<!-- Newest first. One entry per iteration. -->

### Iteration 6 - 2026-09-25 (Verifier)
- **Phase:** none — Verifier (§11); wrote no implementation.
- Recover: no tracked changes except `skills-lock.json` (not the engine's); same untracked non-loop files. No debris.
- Consumed D-003: all 14 rows PASS → DoD #8–#16 signed off.
- Re-proved `machine` criteria: #1 `assembleDebug` OK; #2 `testDebugUnitTest --rerun` OK, 10/12/6/10/4
  tests, 0 failures; #3 `lintDebug --rerun` 0 errors, 63 warnings; #4 merged manifest complete; #5
  `PlaybackQueuePolicy` in the four seek overrides, `PlaybackStopPolicy` in `onTaskRemoved`; #6 device
  `b56e2819`: installed, launched, `MusicFragment` present, no crash entry for `com.example.myapplication`; #7 README.
- Reconciled: no gaps. Recorded sign-offs at `6b8c80e`. Attempted Cleanup Commit: `git rm -r -q .harness/run`
  → "This command requires approval" (no ledger grants `git rm`). Capability needed → queued D-004;
  knowledge update → PROJECT.md. Pushed the Loop Branch. Status `ESCALATE`.
- Drafted Cleanup Commit subject: `loop(done): local music player — every Definition of Done criterion proved`;
  body lists #1–#7 with this iteration's evidence and #8–#16 signed off by the human via D-003.

### Iteration 5 - 2026-09-25 (Verifier)
- **Phase:** none — Verifier (§11); wrote no implementation.
- Recover: tree clean apart from the same untracked non-loop files as iteration 4 and `skills-lock.json`;
  no debris. Decisions: nothing new in `DECISIONS.md` (D-001/D-002 already consumed).
- Re-proved `machine` criteria with fresh evidence: #1 `assembleDebug` BUILD SUCCESSFUL; #2
  `testDebugUnitTest --rerun` BUILD SUCCESSFUL, the five named classes present with 0 failures (10/12/6/10/4);
  #3 `lintDebug --rerun` 0 errors, 63 warnings; #4 merged manifest has all 5 permissions + the
  `mediaPlayback` `MediaSessionService`; #5 `QueuePolicyPlayer` (the session's player) calls
  `PlaybackQueuePolicy` in all four seek overrides, `onTaskRemoved` calls `PlaybackStopPolicy`; #6 device
  `b56e2819`: installed, launched, `MusicFragment` resumed (mState=7), no crash entry for the app (the
  first launch's task was removed from Recents by someone using the phone at 15:30:15; relaunched); #7 README.
- Reconciled: no gaps. 9 `human` criteria unsigned → queued D-003 (Human Verification Request, 14 rows).

### Iteration 4 - 2026-09-25
- **Phase:** 3 — T-004 (attempt 1, Capable)
- Recover: tree clean apart from untracked non-loop files (`.agents/`, `.claude/`, `.harness/loop/`, ledgers,
  `build-top.txt`) and `skills-lock.json` — none of it this run's debris; left untouched.
- Decisions: D-001, D-002 already consumed; nothing new.
- Attempted: Worker wrote `PlaybackStopPolicy`, `onTaskRemoved` → `pauseAllPlayersAndStopSelf()`, session
  activity PendingIntent → `MainActivity` (`EXTRA_OPEN_MUSIC`, `onCreate` + `onNewIntent` → `toMusic`),
  `MusicNotificationPermissionRequest` (Android 13+, once per visit, never blocks playback). Iteration wrote
  `PlaybackStopPolicyTest` (C-11) and added `NotificationPermissionDecisionTest`; README.
- Scope check: 5 written files, all inside the Declared File Scope. Build/test/lint green (47 tests, lint
  0 errors / 63 warnings). Device `b56e2819`: installed, `MusicFragment` resumed, no app crash.
- Review: 0 blocking; N-1 (`playWhenReady` alone counts an errored/ended player as playing), N-2 (guard
  clause), N-3 (param name `isPlaying` invites the wrong caller) — all fixed, re-verified green.
- Reconciled: N-1 is a trap → Constraint C-12. All tasks complete; every `machine` criterion appears met →
  DONE-candidate recorded.

### Iteration 3 - 2026-09-25
- **Phase:** 2 — T-003 (Capable Worker, attempt 1)
- Recover: tree clean apart from tooling files that are not the engine's (`.agents/`, `.claude/agents|skills`,
  `.harness/loop/`, ledgers, `DECISIONS.md`, `skills-lock.json`); no debris. No new decisions (D-001/D-002
  already consumed).
- Attempted: Worker wrote the 7 new Kotlin files + 4 screen edits; its Write was refused for
  `res/drawable/*.xml` and `app/src/test/...` (in scope) — the Iteration wrote those 5 files from its report.
  Iteration wiring: Media3 1.8.0, manifest FGS permissions + service, Koin, 4 strings EN + DE, README.
  Scope check: every changed file inside T-003's scope or the Iteration's wiring list.
- Evidence: build/test/lint BUILD SUCCESSFUL first try and after review fixes; 39 tests, 0 failures; lint 0
  errors. Device `10AECY1ZXG003MQ`: installed, MusicFragment resumed, no app crash.
- Review: 0 blocking; N-1, N-2, N-3 fixed by the Iteration; N-4 kept (convention).
- Reconciled: Worker write limit → Knowledge update, Constraint C-11. New device serial → PROJECT.md.
  `rm` refused, so a temporary `build-top.txt` (dumpsys output) is left untracked, not committed.

### Iteration 2 - 2026-09-25
- **Phase:** 1 — T-001 (dark theme), T-002 (Music list)
- Recovered: working tree held only human tooling files (`skills-lock.json`, `.agents/`, `.claude/agents|skills/`,
  runtime-provisioned ledgers + `DECISIONS.md`) — not engine debris; left untouched and uncommitted.
- Consumed D-002 (ledgers written). Verified `assembleDebug` on the unchanged tree before dispatching.
- Workers: T-001 at Fast (haiku), T-002 at Capable (opus). Scope check: file sets disjoint and inside scope.
  Iteration wired manifest, nav graph (Music start, `toMusic`, toHome/toSetting popUpTo musicFragment),
  BottomBarDestination.Music + CoreBottomBar, Koin, both strings.xml (+4 missing German keys), README.
- Attempt failures: T-001 attempt 1 failed lint (`windowLightNavigationBar` NewApi) → scope reverted,
  re-dispatched at Capable (mechanical escalation), passed. Review (Capable, fresh context): B-1 blocking
  (Fragment-level request trigger replays on back-stack return → Settings/dialog opens without a tap);
  N-1..N-4 non-blocking. T-002 attempt 2 fixed B-1, N-2, N-3; Iteration fixed N-1 (`toMusic` popUpTo
  musicFragment). N-4 recorded unfixed.
- Device: first build installed, launched, `MusicFragment` resumed, no crash. Final build install refused on
  device (MIUI prompt).
- Reconciled: knowledge update → C-10 (API-gated theme attrs) + toolchain verified + env facts in PROJECT.md.

### Archived D-002 (answered, iteration 2)
- **Q:** Write the two capability ledger files so the D-001 grants take effect.
- **A:** Option 1, done — both files written with the D-001 content. Rationale in `AMENDMENTS.md`.

### Iteration 1 - 2026-09-25
- **Phase:** none executed
- Recover: tree dirty with human/tooling changes only (DoD APPROVED tick, `skills-lock.json`, untracked
  `.agents/`, `.claude/agents|skills`, `.harness/loop/`, runtime-provisioned `DECISIONS.md`). No engine
  debris. The DoD tick is committed with this checkpoint; the tooling files are left untouched (not the engine's).
- Consumed D-001 (approved as written).
- Attempted: `./gradlew :app:assembleDebug` → "This command requires approval". Neither
  `.harness/knowledge/capabilities.json` nor `.harness/run/capabilities.json` exists; `run.ps1` compiles only
  those files' `allow` arrays, so the D-001 approval never reached the permission settings.
- Reconciled: capability needed → queued D-002 (write the two ledgers, exact content given); T-001..T-004
  deferred on it. No Workers dispatched — nothing could be verified. Knowledge update → PROJECT.md.

### Iteration 0 (bootstrap) - 2026-09-25
- **Phase:** none (bootstrap)
- Attempted: read PRD; surveyed the tree; three Capable analyst passes (conventions + DoD proposal,
  decomposition + file scopes + tiers, critique of plan / DoD classes / tiers); created Loop Branch
  `loop/music-player-v2` from `main` @ `027d3ea`; wrote DoD (16 criteria), PLAN (4 tasks, 3 phases),
  task files, `.harness/knowledge/PROJECT.md` (C-01..C-09), ISSUES, SUGGESTIONS; queued D-001.
- Learned: `./gradlew` is refused without a standing capability, so the toolchain is unverified on this
  branch; `cd … && cmd` is refused as multiple operations; `git ls-files`/`ls-tree` are outside the baseline;
  `git show <sha>:.harness/...` works where `MSYS_NO_PATHCONV=1` is refused. Sibling branch
  `loop/music-player` (same feature, same base) supplied verified knowledge.
- Reconciled: critique findings folded in — lint baseline fix and all Phase-1 wiring listed; nav
  `toSetting` popUpTo re-point; notification controls moved to T-003; `PlaybackQueuePolicy` /
  `PlaybackStopPolicy` must be the code that runs (DoD #5); `MainActivity.kt` added to T-004;
  `data/model` → `data/mediastore`; natural end-of-queue behaviour stated (repeat-all). Knowledge updates → PROJECT.md.

## Archived Decisions

### D-003 (answered, iteration 6)
- **Q:** Human Verification Request — 14-row phone checklist for DoD #8–#16.
- **A:** All 14 rows PASS. Rationale in `AMENDMENTS.md`.

### D-001 (answered, iteration 1)
- **Q:** Approve the DoD (16 criteria + Constraints) and the standing / goal-scoped toolchain capabilities.
- **A:** Option 1, approve as written — DoD, standing and goal-scoped capabilities all approved. Rationale in
  `AMENDMENTS.md`.
