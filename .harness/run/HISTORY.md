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

### Iteration 4 - 2026-09-29
- **Phase:** 3 (T-003 mini player, T-004 Now Playing)
- Wired icons `ic_player_*`, 10 strings, nav destination + `toNowPlaying`, Koin `NowPlayingViewModel`. Two Opus Workers in parallel (disjoint scopes); combined build + 10+ new unit tests green; lint only baseline errors. Reviewer: no Constraint violations; fixed play/pause icon vs toggle mismatch (mapper `isPlaying` now playWhenReady-based).
- Learned: `safeNavigate` is a `NavigationUtil` extension (import needed); with a `bottomBar` in CoreLayout, do not add nav-bar inset to the list too.
- Reconciled: unfixed reviewer notes in ISSUES.md; no amendments.

## Archived Decisions

## Iteration: Phase 4 (T-005 removals)
- Iteration-executed (no Worker): deleted demo screens, rate sheet, bottom bar, Top-bar-4, Room/DataStore/Ktor/locale stacks, AppConfig/Constant/Language, network/location/alarm permissions, Lottie/Play/constraintlayout/navigation-compose deps, raw/values-de/demo drawables+font, Theme.Lockscreen; rebuilt Koin modules; MainApplication Koin-only; activity_main FrameLayout; app_name Music Player.
- Learned: Media3 re-merges ACCESS_NETWORK_STATE into the manifest; `tools:node="remove"` fixes it.
- Reconciled: no amendments; R5 sweep deferred to T-006 as planned.

## Iteration: Phase 5 (T-006 sweep/README)
- Iteration-executed: R5 sweep removed unreferenced helpers/resources, dropped desugaring, wrote README with package tree. All builds + lint green; UnusedResources 0.
- Reconciled: DONE-candidate recorded; no amendments.
