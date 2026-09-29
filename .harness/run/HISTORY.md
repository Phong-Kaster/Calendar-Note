# HISTORY

> Append-only audit log, never read during Orient. Newest first.

### Iteration 3 - 2026-09-29
- **Phase:** 2 (T-002)
- Wired Media3 1.11.1 deps, manifest service/perms/singleTop/WAKE_LOCK, Koin. Capable Worker built PlaybackService, PlayerRepository(+Impl), mappers, PlaybackState/RepeatMode, Library tap-to-play; build + unit tests green. Reviewer: 3 fixes applied (private mappers, onTaskRemoved ended/idle, reset state on disconnect).
- Learned: phone install blocks on vivo installer confirmation; device drive still impossible. Files are CRLF: python str.replace with "
" silently misses multi-line matches.
- Reconciled: hasNext/hasPrevious shuffle approximation recorded as note (no amendment).

### Iteration 2 - 2026-09-29
- **Phase:** 1 (T-001)
- Consumed D-001 (approved as written). Wired CoreActivity dark edge-to-edge, manifest audio perms, nav start = libraryFragment, Koin, strings, themes. Worker built library/theme/tests; build + unit tests green. Reviewer: spinner race + dead code fixed.
- Learned: phone was locked and refuses input injection; on-device drive deferred. Gradle daemon can be killed externally: rerun.
- Reconciled: no amendments.

### Iteration 1 - 2026-09-29 (Bootstrap)
- **Phase:** none (bootstrap)
- Attempted: surveyed repo (template), fanned out 2 analysts (survey+DoD+removals; decomposition+conflict), authored DoD/PLAN/TASKS/knowledge, queued D-001. Baseline build/test/androidTest-compile pass; baseline lint fails on pre-existing MissingTranslation.
- Learned: attached phone has real songs (API 36); serial changes between sessions; Workers cannot delete files, so removals are Iteration-executed.
- Reconciled: assumptions A-001…A-005 recorded.

## Archived Decisions
