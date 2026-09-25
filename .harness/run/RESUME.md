# RESUME BLOCK

> Regenerated every iteration. A derived cache, never a source of truth: on any disagreement with the task
> files or with git, this file is the one that is wrong.

- **Stage:** escalated — DoD approved (D-001); waiting for D-002 (human writes the capability ledger files)
- **Next Phase:** Phase 1 (after D-002)
  - T-001 (Fast) - scope: `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `core/CoreLayout.kt`, `core/CoreActivity.kt`, `res/values/themes.xml`
  - T-002 (Capable) - scope: `TASKS/T-002.md` § Declared File Scope
  - Iteration wiring: manifest audio perms, nav graph (music start dest, `toMusic`, re-point `toSetting` popUpTo), `BottomBarDestination` + `CoreBottomBar`, DI, both strings.xml (incl. 4 missing German keys), README
- **Queued decisions:** 1 — D-002 blocks T-001, T-002, T-003, T-004
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** none verified on this branch (still denied in iteration 1 — ledger files missing). First act next run: `./gradlew :app:assembleDebug` on the unchanged tree to confirm the grant. Expected once granted: build `./gradlew :app:assembleDebug` | test `./gradlew :app:testDebugUnitTest` | lint `./gradlew :app:lintDebug` — run `cd` in its own Bash call first
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
