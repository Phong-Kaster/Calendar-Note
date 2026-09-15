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

## Archived Decisions

<!-- Full request + decision + rationale of every consumed Decision Queue entry. None yet this run. -->
