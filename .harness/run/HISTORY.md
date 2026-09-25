# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

<!-- Newest first. One entry per iteration. -->

### Iteration 3 (run 2) - 2026-09-25
- **Phase:** 2 — T-004, T-005 (two Capable Workers in parallel)
- Recover: no engine debris beyond the untracked root captures (`*.txt`) and the human's tooling; nothing to salvage.
  `review-diff.txt` overwritten with this Phase's diff for the reviewer. No new decisions in `DECISIONS.md`.
- Scope check: files disjoint, inside scopes (T-005 + new `model/NowPlayingCloseRule.kt`, A-003), union = `git status`.
  Wired: `NowPlayingCloseRuleTest.kt` (C-11), README feature text + tree note.
- Build, 76 tests, lint (0 errors) green first try.
- Review: C-01…C-12 clean, no blocking. NIT 1 (decode target used the longer screen side) and NIT 3 (trailing lambda on
  `produceState`) fixed by the Iteration, re-built green; NIT 2 (file-level string `TAG`) recorded in ISSUES.md.
- Learned: `produceState` keeps its old value across key changes; `StateFlow` drops no-field-change events → counter.
- All tasks complete → DONE-candidate recorded; next Iteration verifies.

### Iteration 2 (run 2) - 2026-09-25
- **Phase:** 1 — T-001, T-002, T-003 (three Capable Workers in parallel)
- Recover: tree dirty only with human edits (`DECISIONS.md`, `run/capabilities.json`) and untracked tooling
  (`.agents/`, `.claude/agents|skills/`, `.harness/loop/`, `.harness/knowledge/capabilities.json`, `skills-lock.json`)
  — not engine debris; left for the human, not committed by the engine except the two `run/` files.
- Consumed D-001 (option 1, DoD approved, install/start capability granted) → A-001.
- Scope check: 3 Workers' files disjoint, each inside its scope, union = `git status`. Wired nav graph, Koin,
  both `strings.xml` (4 keys), `colors.xml`, 5 drawables, 3 test files, README.
- Build, 69 tests, lint (0 errors) green on the first try; installed + started on `3H164700ALT00000`, `MusicFragment`
  on top, no app crash line (DoD #6).
- Review: no Constraint violation. Blocking: README tree missing `nowplaying/` → fixed in this checkpoint.
  Should-fix 2–4 + NIT 5 (paused external seek not seen; empty restore shows a blank screen; 500 ms poll runs
  off-screen; unused TAG) → new task T-005 (A-002).
- Learned: Media3 1.8.0 exposes `media3_icon_{play,pause,next,previous}` (overridable); the provider-wrapper pattern
  compiles; `Cursor.getLong` turns NULL into 0; `cd … && cmd` lines are accepted again, but `;`-chained / `sed -i`
  / redirected gradle lines are refused.

### Iteration 1 (run 2 bootstrap) - 2026-09-25
- **Phase:** bootstrap (no implementation)
- Attempted: read the new `PRD.md` (Material 3 Now Playing screen + M3 MediaStyle notification); surveyed the run-1
  music code; two Capable analyst fan-outs (conflict analysis + tiers; critique of decomposition and DoD classes).
- Learned: position has no push event in Media3 (needs polling); Android 12+/13+ override notification accent and
  icons; nav graph start destination is `musicFragment` (PROJECT.md said `homeFragment` — corrected); the Cleanup
  Commit's `git rm -r .harness/run` is now in the baseline.
- Reconciled: split the album-art plumbing (T-001) from the notification look (T-002) so Phase 1 holds three disjoint
  tasks; all four tasks Capable (analysts agreed); accent/icon platform limits written into DoD #11 as the proposed
  reading; notification tap target kept (assumption, surfaced in D-001). Loop Branch reused as the PRD instructs.
  Queued D-001 (DoD approval + goal install/start capability).

## Archived Decisions

<!-- Full request + decision + rationale of every consumed Decision Queue entry. -->

### D-001 - Approve the Definition of Done for the Material 3 player (13 criteria) and one goal capability
- **Asked:** iteration 1 (run 2 bootstrap). Approve `DoD.md` (edit freely) and optionally grant a goal capability to
  install the debug build and start `MainActivity`, so DoD #6 is engine-proved. Options: 1 approve + grant; 2 approve,
  no grant (#6 → human); 3 edit then approve. Recommendation: 1. Blocked T-001…T-004.
- **Answered:** iteration 2 consumed. **Decision:** option 1 — DoD as written; capability written into
  `.harness/run/capabilities.json`. **Rationale:** "The DoD matches the PRD and the choices already fixed there (full
  Now Playing screen; standard MediaStyle notification with art, small icon, M3 accent/icons). Letting the engine
  prove #6 saves a manual step."
