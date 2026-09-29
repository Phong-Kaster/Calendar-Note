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

## Iteration 7 - 2026-09-29 (Verifier)
- Clean rebuild: assembleDebug, testDebugUnitTest (6 classes, 45 tests, 0 fail), assembleDebugAndroidTest, lintDebug exit 0; UnusedResources 0 (`evidence/V-gradle.txt`). Criteria 1, 2, 3, 14, 16, R1-R4, R6-R8 re-proved (`evidence/V-manifest.txt`, `V-badging.txt`, `V-removals.txt`, `V-deps.txt`).
- R5 FAILS: `NavigationUtil.safePopBackstack` (both overloads) and `safeNavigate(NavDirections)` are unreferenced (`evidence/V-sweep.txt`). Filed T-007, cleared DONE-candidate.
- Device drive not done: AVD `astronex_test` booted, then was shut down externally before boot completed (emulator log: graceful-shutdown request); phone b56e2819 detached and a different phone (CPH2895, 3H164700ALT00000) appeared. Did not drive a personal device mid-swap; next Verifier drives on the AVD.
- Reconciled: R5 gap -> Tier-1 amendment (new task T-007); environment fact -> PROJECT.md.

## Iteration 8 - 2026-09-29 (Phase 6, T-007)
- Iteration-executed: removed unreferenced NavigationUtil members; gradle assemble/test/androidTest/lint exit 0; R5 sweep clean except allow-listed theme member. DONE-candidate recorded.
- Reconciled: no amendments.

## Iteration 9 - 2026-09-29 (Verifier)
- Recover: tree had `.harness/TELEMETRY.tsv` (runtime, committed) and untracked `.claude/agents/`. That is runtime-provisioned agent definitions, not debris, so it was left untouched and not committed.
- Clean rebuild + unit tests (45/45) + androidTest compile + lint (0 errors, UnusedResources 0) exit 0. The R5 sweep is clean. Criteria 1-3, 14, 16 and R1-R8 were re-proved.
- The phone CPH2895 was in use by someone else (a foreign app took the foreground mid-drive) and has only 2 songs. Stopped driving it and recorded A-006. Drove on AVD `astronex_test` with 3 generated WAVs. Criteria 4-6, 8-13, 22-24 pass; pre-checks for 7 and 15 did not fail; the notification-shade part of 19 was checked, the lock screen was not driven (no keyguard). Details: `evidence/V2-SUMMARY.md`.
- Driver fixes along the way (not app defects): MSYS path conversion broke the APK path; the second system dialog uses `permission_deny_and_dont_ask_again_button`; a stale shade dump led to a tap on the media card's pause button.
- C10 transcript: PAUSED(2) pos 13507 -> PLAYING(3) -> next: Bravo_Tone pos 0 -> previous: Alpha_Tone pos 0.
- Reconciled: no gaps, so no tasks filed. Seek landing short of the drag target -> no action (already an open review note; C13 threshold met). Criteria awaiting signatures -> §14.2 ISSUES "Awaiting a person". Result: DONE_PARTIAL, no Cleanup Commit.
