# PLAN

> Machine-owned execution strategy. The human never reviews this file - only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 queued decisions.

## Strategy

Two independent, disjoint tasks per PRD.md's own instruction ("two tasks... if you find yourself
proposing more than a handful, the decomposition is wrong"). Both run in a single Phase, concurrently.
Bootstrap fan-out (three parallel proposal roles, then two arm's-length critique/conflict-analysis roles,
all Capable-tier `loop-analyst`) converged on:

- The app has no `ProcessLifecycleOwner`/`lifecycle-process` dependency, so "brought to the foreground"
  is observed through `MainActivity.onStart()` rather than adding one (PRD forbids new libraries).
- The once-per-day decision must live behind an injected `Clock` as plain Kotlin (Constraint C-06 — this
  toolchain's unit tests run against a stub `android.jar` where framework calls silently no-op).
- A handful of files are genuinely shared between "what T-001 needs" and "conventions this codebase
  already protects" and are wired by the Iteration itself, never a Worker: `MainApplication.kt` (channel
  creation at launch, mirroring the existing `AlarmNotifier` precedent under Constraint C-10),
  `injection/AppModule.kt` (the `includes(...)` list), `res/values/strings.xml` + `res/values-de/strings.xml`
  (Constraint C-02), and `README.md`'s package tree.
- Both tasks are Capable tier — confirmed by two independent arm's-length roles applying the Fast-tier
  three-part test in `POLICIES.md`: both touch shared/integration-adjacent territory, neither's acceptance
  is a literal checkable output with no judgment call, and both carry a small architecture decision (a
  frozen-forever notification channel choice for T-001; a public composable signature change for T-002).

## Task Graph

- T-001 - the app greets the user once per calendar day, on first foreground open (depends on: -) -
  scope: `domain/greeting/`, `data/notification/GreetingNotifier.kt`, `data/datastore/SettingDatastore.kt`,
  `domain/repository/SettingRepository.kt`, `data/repository/impl/SettingRepositoryImpl.kt`,
  `injection/NotificationModule.kt`, `MainActivity.kt` - tier: Capable
- T-002 - the app no longer requests or declares the two unused location permissions (depends on: -) -
  scope: `AndroidManifest.xml`, `ui/fragment/home/component/HomeRequestPermission.kt`,
  `ui/fragment/home/component/HomePermissionBottomSheet.kt`, `ui/fragment/home/HomeFragment.kt`,
  `ui/util/PermissionUtil.kt` - tier: Capable

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | T-001, T-002 | `res/values/strings.xml`, `res/values-de/strings.xml`, `MainApplication.kt`, `injection/AppModule.kt`, `README.md` |

## Known Risks

- D-001 (2026-09-15) overrode the PRD's `VIBRATE` removal: `AlarmNotifier.kt` independently calls
  `.setVibrate(...)`/`channel.enableVibration(true)`, so the permission backs a real feature. `VIBRATE`
  stays declared; only the two location permissions are removed. This risk is closed, not open.
- `onStart()` fires on every configuration recreation (rotation, the in-app language picker), not only on
  a genuine foreground return — handled by making the check-and-write sequence idempotent-safe under
  concurrent calls (DoD criterion 4), not by trying to distinguish "real" opens from recreations.
- No device/emulator/Robolectric exists in this repository, so criteria 16-23 can only be verified by a
  person; the Verifier queues a Human Verification Request rather than claiming `DONE` on machine
  evidence alone.
