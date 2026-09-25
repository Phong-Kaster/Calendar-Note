# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next.

<!-- Newest first. One entry per iteration. -->

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

### D-001 (answered, iteration 1)
- **Q:** Approve the DoD (16 criteria + Constraints) and the standing / goal-scoped toolchain capabilities.
- **A:** Option 1, approve as written — DoD, standing and goal-scoped capabilities all approved. Rationale in
  `AMENDMENTS.md`.
