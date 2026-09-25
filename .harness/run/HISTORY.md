# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

<!-- Newest first. One entry per iteration. -->

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
