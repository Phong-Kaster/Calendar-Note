# AMENDMENTS

> Every plan mutation (Tier 1 logged, Tier 2/3 decisions applied), newest first.

### Iteration 1 — 2026-09-24
- **D-001 applied (DoD approval).** Human: "Approved as proposed. All 15 criteria and their Verification Classes stand unchanged … Assumptions A-001 … A-006 are not overturned." Rationale: "the human reviewed the DoD and explicitly agreed to every `human` criterion." DoD marked APPROVED; T-001…T-004 unblocked.
- **Tier 1 — Phase 1 wiring deviation:** `unitTests.isReturnDefaultValues = true` (listed in PLAN Phase 1) was **not** added — no test needs it, and leaving it off keeps C-04's stub-jar guard honest.
- **Tier 1 — Iteration-owned files widened for a review fix:** `core/CoreActivity.kt` and `res/values/themes.xml` edited by the Iteration (system bars always light-on-dark) so T-001's fixed dark theme holds for the status bar too (review finding, DoD 14). Both added to the Iteration-owned shared-file list.
- **Tier 1 — Nav:** `toHome` / `toSetting` now `popUpTo musicFragment` (Music is the new root), so Back from Home/Setting returns to Music and Back from Music leaves the app.
