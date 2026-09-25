# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next.

<!-- Newest first. One entry per iteration. -->

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
