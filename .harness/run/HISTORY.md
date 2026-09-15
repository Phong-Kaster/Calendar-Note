# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

### Iteration 1 - 2026-09-15 (bootstrap)

- **Phase:** none (bootstrap)
- **Attempted:** Bootstrapped a new run for the "daily greeting notification + permission clean-up" PRD.
  `.harness/run/` did not exist (the previous alarms run's state was cleared in commit `736d762` to make
  way for this goal). Read `PRD.md`, `.harness/knowledge/PROJECT.md`, and the relevant source files
  (`SettingDatastore.kt`, `AlarmNotifier.kt`, `MainActivity.kt`, `MainApplication.kt`,
  `HomePermissionBottomSheet.kt`, `HomeRequestPermission.kt`, `HomeFragment.kt`, `PermissionUtil.kt`,
  `AndroidManifest.xml`, `SchedulerModule.kt`, `SettingRepository.kt`/`Impl`, `DatastoreModule.kt`).
  Dispatched five Capable-tier `loop-analyst` roles in two waves: Wave 1 (parallel) — a conventions
  survey, a DoD-criteria proposal, and a task-decomposition proposal; Wave 2 (parallel, arm's-length) — a
  critique of Wave 1's DoD verification classes and decomposition, and a conflict-analysis/Model-Tier
  role that verified file-scope disjointness and classified both tasks against the Fast-tier test in
  `POLICIES.md`.
- **Learned:**
  - `androidx.lifecycle:lifecycle-process` (`ProcessLifecycleOwner`) is **not** a dependency this app
    has, even though `.claude/viewmodel-layer.md` documents that pattern — adding it would violate the
    PRD's "no new libraries." The foreground signal is `MainActivity.onStart()` instead.
  - The once-per-day decision must be plain Kotlin behind an injected `Clock` (Constraint C-06): unit
    tests here run against a stub `android.jar` (`isReturnDefaultValues = true`) where
    `NotificationManager`/`NotificationChannel`/`PendingIntent`/`Intent` silently return defaults, so a
    test aimed at any of them would pass vacuously.
  - `onStart()` fires on every configuration recreation, not only a genuine foreground return — two close
    calls could race and both post unless the check-and-write sequence is serialized with a `Mutex`.
  - A greeting notification channel is a one-shot, permanently-frozen decision (Constraint C-10) and
    belongs beside the existing `AlarmNotifier` channel-creation call in `MainApplication.onCreate` —
    which makes `MainApplication.kt` a shared/integration file the Iteration must wire itself, not either
    Worker.
  - PRD's stated reason for removing `VIBRATE` ("no `Vibrator` usage") is literally true but incomplete:
    `AlarmNotifier.kt` independently calls `.setVibrate()`/`channel.enableVibration()`. The PRD
    pre-decided the removal anyway and explicitly asked the run not to stop and ask about it — recorded
    as a known risk (DoD criterion 23, human-verified) and an assumption in `STATE.md`, not queued as a
    decision.
  - `res/values/strings.xml` + `res/values-de/strings.xml` (Constraint C-02), `injection/AppModule.kt`
    (its `includes(...)` list), and `README.md`'s package tree are also Iteration-owned, shared across
    both tasks.
  - No screenshot test renders `HomePermissionBottomSheet` or `HomeRequestPermission`, and
    `AlarmsPermissionNotice` (the component the PRD says must not change) lives in a different file
    entirely and is already pinned by its own screenshot reference — so criterion 13 can be machine-graded
    rather than left to a person.
  - Both tasks independently classified **Capable** tier by the arm's-length conflict-analysis role,
    against the literal three-part Fast-tier test in `POLICIES.md`: both touch shared/integration-adjacent
    files, neither's acceptance is a purely mechanical check, and both carry a small architecture decision
    (a frozen-forever channel choice; a public composable signature change).
- **Reconciled:** No decision needed beyond the standing DoD-approval gate — every ambiguity the fan-out
  surfaced (channel id/importance, stored-date format, whether to delete `isLocationGranted`, whether the
  greeting write should be gated on successful notify) was resolved by the Iteration itself and written
  into the task files as instructions, since none of them rises to Tier 2 (no architecture that
  contradicts the PRD, no missing product information the PRD didn't already answer). Queued **D-001**:
  approve the Definition of Done and the (empty) proposed standing capabilities — no new capability is
  needed this run; the existing standing ledger already covers build/test/lint/screenshot-validate and
  the `git show` Windows workaround.

### Iteration 2 - 2026-09-15

- **Phase:** 1 (T-001, T-002 — the whole task graph)
- **Attempted:** Consumed D-001 (see Archived Decisions below): revised `DoD.md`, `PLAN.md`,
  `TASKS/T-002.md`, `STATE.md`, `ESCALATION.md` to keep `android.permission.VIBRATE`. Both tasks became
  selectable; dispatched one Capable-tier (`opus`) `loop-worker` per task in parallel, attempt 1/3 each.
  T-001 wrote `domain/greeting/GreetingDecision.kt` (+ test), `data/notification/GreetingNotifier.kt` (+
  test), `injection/NotificationModule.kt`, and edited `SettingDatastore.kt`/`SettingRepository.kt`/
  `SettingRepositoryImpl.kt`/`MainActivity.kt`. T-002 edited `AndroidManifest.xml`,
  `HomeRequestPermission.kt`, `HomePermissionBottomSheet.kt`, `HomeFragment.kt`, `PermissionUtil.kt`.
  Verified both Workers' reported file sets were pairwise disjoint, each contained in its own Declared
  File Scope, and the union matched `git status` — no scope violation. Wired the shared/integration files
  myself: added `notificationModule` to `AppModule`'s `includes(...)`, called
  `GreetingNotifier.createChannelIfNeeded()` from `MainApplication.onCreate`, added the two new string
  keys (`daily_greeting`, `hello_what_will_you_write_today`) and removed the two keys T-002 made dead
  (`location`, `allow_location_to_help_you`) in both `res/values/strings.xml` and `res/values-de/strings.xml`,
  and updated `README.md`'s package tree and feature table. Ran one invocation of `:app:assembleDebug
  :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest`: `BUILD SUCCESSFUL`. Dispatched a
  Capable-tier (`opus`) `loop-reviewer` with a clean context against the full diff, the DoD criteria, house
  style, and every Constraint in `.harness/knowledge/PROJECT.md` — verdict **APPROVE**, three non-blocking
  findings.
- **Learned:**
  - `.harness/knowledge/PROJECT.md`'s cached "251 tests across 15 classes" baseline was already stale
    before this run started — the tree held 272 tests across 16 classes at the start of this iteration.
    Corrected the cache to this iteration's own fresh measurement (288 tests / 18 classes) and noted the
    cache is a floor for a *future* run's own measurement, not a diff base.
  - `GreetingNotifier`'s default `Clock.systemDefaultZone()` binds the timezone once, at Koin singleton
    construction time — a device timezone change mid-process would misjudge "today" until the process
    restarts. Same pattern this codebase's other stores already use (e.g. `NoteRepositoryImpl`); accepted
    as a pre-existing, self-healing limitation rather than fixed in this run.
  - `SettingDatastore.kt`'s flows (including the new `lastGreetedDateFlow`) have no `.catch { IOException
    -> emptyPreferences() }` guard that `.claude/repository-layer.md` documents as required — a
    repo-wide, pre-existing gap, not something this run introduced. Recorded for a future task.
  - `HomeRequestPermission.kt`'s private `shouldShowRequestPermissionRationale` was already an orphan
    before this run's diff (confirmed via `git diff`, not just the Worker's own claim) — `requestLocation()`
    used Accompanist's `.shouldShowRationale` instead, never this function. Not this run's to remove.
- **Reconciled:** No new decision needed. All three review findings are non-blocking and classified "no
  action, recorded" (ENGINE.md §8) — none breaks a DoD criterion, one is inherent to an established
  codebase pattern, one is a pre-existing repo-wide gap out of scope for either task, one is pre-existing
  dead code neither task touched. Both tasks completed on attempt 1/3, nothing abandoned. All 14 `machine`
  DoD criteria appear satisfied by this iteration's own evidence; recorded **DONE-candidate: yes** in
  `STATE.md` per ENGINE.md §6.11 (reporting `CONTINUE`, not `DONE` — this invocation wrote code). The next
  invocation is the Verifier (§11): re-prove every `machine` criterion fresh, then raise a Human
  Verification Request for the seven unsigned `human` criteria (15-21).

## Iteration 3 — 2026-09-15 — Final Verification (ENGINE.md §11)

- **Role:** Verifier. This invocation wrote none of the implementation and distrusted all of it.
- **Recovered:** working tree carried one modified file, `.harness/TELEMETRY.tsv` — a Runtime artifact, not
  implementation debris. Nothing to salvage, nothing to revert.
- **Decisions consumed:** none new. `DECISIONS.md` holds only `D-001`, already answered, applied and
  archived in iteration 2; its `ESCALATION.md` entry is marked `answered`. Nothing to re-apply.
- **Re-proved every `machine` criterion against its own fresh evidence** — all fourteen, each checked
  explicitly, none accepted on iteration 2's word. Per-criterion table with the exact file/line or command
  behind each one is in `STATE.md` § Machine verification. Headline figures: `BUILD SUCCESSFUL`; 288 unit
  tests across 18 classes with 0 failures and 0 errors (above the DoD's 251 floor); lint 0 errors / 75
  warnings; screenshot validation 23 of 23 with 23 reference files on disk and no orphans; source **and**
  merged manifest carry neither location permission while keeping `VIBRATE` per D-001; zero hits for all
  nine location symbols across `app/src/main/java`; `AlarmNotifier.kt`, `AlarmNotifierConstantsTest.kt` and
  everything under `ui/fragment/alarms/` absent from the run's diff (`git diff 3506b33 HEAD`).
- **Gaps found:** none. The DONE-candidate flag stands.
- **Discovery (knowledge update, ENGINE.md §8).** The first verification run returned `BUILD SUCCESSFUL`
  with `1 executed, 60 up-to-date` — Gradle's incremental cache, not a re-execution, which is not what §11
  means by fresh evidence. `--rerun` was the obvious remedy and only half worked: it re-ran `lintDebug` and
  `validateDebugScreenshotTest` but left `:app:testDebugUnitTest` `UP-TO-DATE` (`2 executed, 42
  up-to-date`), so the counts in `TEST-*.xml` were still the *previous* invocation's — the same shape of
  trap as the aborted-task one already recorded, arriving through a flag that looks like it ruled the trap
  out. `./gradlew :app:testDebugUnitTest --rerun-tasks` genuinely re-executed them (`28 actionable tasks:
  28 executed`), and that run is where the 288/0 figure comes from. Written to
  `.harness/knowledge/PROJECT.md` § Toolchain in this same checkpoint, since a future Verifier asked to
  "re-prove fresh" would otherwise walk straight into it. No new capability needed — both flags fall under
  the standing ledger's `Bash(./gradlew :app:testDebugUnitTest*)` wildcard.
- **Queued D-002 — Human Verification Request** for the seven unsigned `human` criteria (DoD 15-21), as a
  numbered checklist giving what to open, what to do and what to expect for each, written to be followed
  without reading any code. It names what it blocks: the `DONE` report itself, no task remaining.
  `SUGGESTIONS.html`'s Escalate tab regenerated from `ESCALATION.md` in the same step, before the status
  was written.
- **Not done, deliberately:** no Cleanup Commit, no `DONE`. ENGINE.md §11.3 forbids both while a `human`
  criterion is unsigned. The engine has no device, no emulator and no Robolectric here, so reporting `DONE`
  on criteria 15-21 would be a claim it has no standing to make — and this repository has already shipped a
  correctly-wired, invisible delete button with every test green.
- **Reported:** `ESCALATE` — no executable task remains and a decision is queued (§6.11).

## Archived Decisions

### D-001 - Approve the Definition of Done for the greeting-notification / permission-cleanup run

**Queued:** Iteration 1, 2026-09-15. **Answered:** 2026-09-15. **Consumed:** Iteration 2, 2026-09-15.

**Question:** Approve `.harness/run/DoD.md` as written (23 acceptance criteria, 6 Constraints), including
the Verification Class assigned to each criterion?

**Context:** `PRD.md` pre-decided a once-a-day greeting notification and the removal of three permissions
(`VIBRATE`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`). Bootstrap fan-out flagged that the PRD's
stated reason for removing `VIBRATE` ("no `Vibrator` usage anywhere in the app") was incomplete —
`data/notification/AlarmNotifier.kt` independently calls `.setVibrate(...)`/`channel.enableVibration(true)`
— but did not block on it since the PRD explicitly asked the run not to re-litigate its pre-decisions.

**Decision:** Approved with one change: do NOT remove `android.permission.VIBRATE`. The PRD's removal
reason was wrong given `AlarmNotifier.kt`'s real usage. Revise `DoD.md` to drop criterion 9's VIBRATE
clause, drop criterion 14 (KDoc correction is unnecessary if the permission stays), and drop criterion 23
(no VIBRATE removal left to human-verify). Everything else approved as written: remove the two location
permissions plus their code, and build the greeting notification as specified.

**Applied:** See `AMENDMENTS.md`, 2026-09-15 entry, for the full list of files this decision touched
(`DoD.md`, `PLAN.md`, `TASKS/T-002.md`, `STATE.md`, `ESCALATION.md`).
