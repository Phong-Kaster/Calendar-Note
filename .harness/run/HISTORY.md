# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

### Iteration 4 - 2026-09-08

- **Phase:** none — all seven tasks were already complete; this Iteration consumed a queued decision,
  it did not select or dispatch a Phase.
- Consumed D-002 (answered by the supervising session): amended `DoD.md` criterion 30's wording to
  match the human's decided text, added a Status-section note and updated the Verification Evidence
  Required row 30 to name which files use `MaterialTheme.colorScheme` and which follow the pre-existing
  hardcoded pattern. No implementation code touched. Full request + decision archived below under
  "Archived Decisions".
- D-002 named no blocked tasks (none remained to select) — nothing to unblock beyond final DoD sign-off.
- Since no queued decisions remain, no task is abandoned or deferred, and all seven tasks report
  complete, `STATE.md` now records a DONE-candidate. This Iteration wrote no implementation, but per
  `ENGINE.md` §6.11 an Iteration that has just resolved the blocking question on its own DoD criterion
  still reports `CONTINUE`, not `DONE` — the next, fresh invocation runs Verification (§ENGINE 11)
  against the amended criterion with no bias from having just decided it.

### Iteration 3 - 2026-09-08

- **Phase:** 3 (T-004, T-007) — the last Phase in `PLAN.md`.
- Dispatched T-004 (To-do edit-title) and T-007 (Calendar note edit/delete), both Capable tier, in
  parallel. Verified pairwise-disjoint Declared File Scopes against `git status` — no violations; both
  succeeded on attempt 1.
- Discovered before dispatch (from reading the existing interface implementers) that five files needed
  Iteration-level wiring beyond the planned `strings.xml`: `FakeTaskRepository.kt`, `FakeNoteRepository.kt`
  (would otherwise fail to compile once the domain interfaces gained new methods), `TodoTaskItem.kt`,
  `TodoFragment.kt`, `CalendarFragment.kt` (edit entry points needed wiring outside either task's scope).
  Amended Phase 3's shared-file list in `PLAN.md`; full detail in `AMENDMENTS.md`.
- Wired all shared files myself; no Room migration needed (no schema change this Phase).
- Ran `gradlew.bat assembleDebug` (pass), `gradlew.bat test` (pass, 20/20: 6 new this Phase + 14 prior),
  `gradlew.bat lintDebug` (same 4 pre-existing errors, no new ones).
- Fresh-Context Review found two major, two minor issues; all four fixed before checkpoint (missing
  `KeyboardActions` in `TodoEditTaskDialog.kt`, an inconsistent state-clear timing between
  `TodoViewModel`/`CalendarViewModel`, a missing DAO KDoc, a cosmetic import-order slip). Re-ran build+test
  after fixes: still pass, 20/20. Full detail in `AMENDMENTS.md`.
- All seven tasks now complete; no Phase 4 exists. D-002 remains queued (blocks no task, blocks only final
  DoD sign-off on criterion 30). Since no executable task remains and a decision is queued, this Iteration
  reports `ESCALATE`, not a DONE-candidate — per `ENGINE.md` §6.11 a queued decision takes precedence over
  declaring the run ready for Verification even when every task is complete.

### Iteration 2 - 2026-09-08

- **Phase:** 2 (T-003, T-006).
- Dispatched T-003 (To-do toggle/delete) at Fast tier and T-006 (Calendar note CRUD + dates-with-notes
  marker) at Capable tier, in parallel. Verified pairwise-disjoint Declared File Scopes against
  `git status` — no violations; both succeeded on attempt 1.
- Wired shared files myself, amending Phase 2's shared-file list in `PLAN.md` to add two files neither
  Worker was allowed to touch: `TodoFragment.kt` (wiring `TodoTaskItem`'s new toggle/delete callbacks) and
  `CalendarMonthGrid.kt` (threading `datesWithNotes` through to `CalendarDayCell`'s marker) — plus the
  originally-planned `AppDatabase.kt`/`Migration.kt` (version → 4, `MIGRATION_3_4`), all three Koin
  modules, and `strings.xml`/`values-de/strings.xml`. Full detail in `AMENDMENTS.md`.
- Fixed two build/test failures directly (not counted against either task's attempts): a Kotlin
  empty-lambda-default compile error appearing identically in a Worker file and my own wiring file, and a
  wrong assertion in `TaskToggleAndDeleteTest` (test-only typo, not an implementation defect). Also
  restored KDoc on `CalendarFragment.kt` that the Worker's edit had dropped. Rationale for fixing directly
  rather than re-dispatching in `AMENDMENTS.md`.
- Ran `gradlew.bat assembleDebug` (pass), `gradlew.bat test` (pass, 14/14: 10 new this Phase + 4 from
  Phase 1), `gradlew.bat lintDebug` (4 pre-existing `MissingTranslation` errors, confirmed unrelated; the
  2 new string keys this Phase add no new errors).
- Fresh-Context Review (clean context, Capable tier) found no critical/blocking issues. Full findings and
  disposition in `AMENDMENTS.md`.
- D-002 remains queued (unanswered, blocks no task — only final DoD sign-off on criterion 30).

### Iteration 1 - 2026-09-08

- **Phase:** 1 (T-001, T-002, T-005).
- Consumed D-001 (approved) — see `AMENDMENTS.md` and Archived Decisions below.
- Dispatched three Workers at Capable tier for T-001 (blue theme, `dynamicColor = false`), T-002 (To-do
  add/list vertical slice: `Task`/`TaskEntity`/`TaskDao`/`TaskMapper`/`TaskRepository`/
  `TaskRepositoryImpl`/`TodoFragment`+`TodoViewModel`+`TodoUiState`+components + tests), and T-005
  (Calendar month shell: `CalendarMonth` domain model + `CalendarFragment`+`CalendarViewModel`+
  `CalendarUiState` + components + test). Verified Declared File Scopes were pairwise-disjoint and matched
  `git status` exactly before trusting any Worker output.
- Wired shared files myself: `AppDatabase.kt`/`Migration.kt` (version → 3, `MIGRATION_2_3`), all three
  Koin modules, `navigation_graph.xml`, `BottomBarDestination.kt`+`CoreBottomBar.kt`, two new drawables,
  `strings.xml`/`values-de/strings.xml`. Details in `AMENDMENTS.md`.
- Ran `gradlew.bat assembleDebug` (pass), `gradlew.bat test` (pass, 6/6 new tests + 1 pre-existing),
  `gradlew.bat lint` (fails — 4 pre-existing `MissingTranslation` errors, confirmed unrelated to this
  Phase by exact line number; not a DoD criterion).
- Fresh-Context Review (clean context, Capable tier) found 6 issues; fixed 5 before checkpoint (see
  `AMENDMENTS.md`); queued the 6th as D-002 (Tier 2 — hardcoded colors on new screens vs. DoD 30, traced
  to `CoreLayout`'s pre-existing hardcoded background, real contrast-regression risk if "fixed" literally
  without also touching Home/Setting, which are out of this run's scope).
- Learned (recorded in `PROJECT.md`): `runBlocking`/`Flow.first()` resolve on the JVM test classpath with
  zero new test dependencies — the conditional coroutines capability in `.harness/run/capabilities.json`
  was not needed. `gradlew.bat lint` is not currently a green gate on this repo baseline (4 pre-existing
  errors, unrelated to any task). `CoreLayout.kt`'s background is hardcoded black regardless of
  `darkTheme`, and no screen (old or new) sources its own text/icon colors from `MaterialTheme.colorScheme`
  — a pre-existing, app-wide pattern, not a regression introduced by this run.

### Iteration 0 (Bootstrap) - 2026-09-08

- **Phase:** none - bootstrap does not select or dispatch Workers.
- Attempted: read `PRD.md`; inspected the existing Android skeleton (layering, `Post`/`Setting`
  conventions, Room schema, DI modules, nav graph, bottom bar, theme, test setup). Fanned out five
  Capable-tier analysis roles: repo-convention survey, DoD proposal, task decomposition, an independent
  critique of that decomposition, and a conflict-analysis/model-tier role. Converged their output into a
  single `DoD.md`, `PLAN.md`, and seven task files (`T-001`..`T-007`) across three Phases.
- Learned: `CLAUDE.md`'s `@`-imports reference three files that do not exist on disk (pre-existing,
  out of scope). `MyApplicationTheme` defaults `dynamicColor = true`, which would silently defeat the
  PRD's blue-scheme requirement on API 31+ if not addressed. `CoreFragment.enableDarkMode` is dead code;
  dark mode is actually driven by `isSystemInDarkTheme()`. The test source set has no coroutines
  dependency declared explicitly, though `runBlocking` should resolve transitively. `CoreBottomBar.kt`
  hard-codes exactly two destinations around an unused center button.
- Reconciled: the originally-proposed decomposition's T-001 (todo-CRUD + theme bundled together) was split
  so the blue-theme work is its own task (T-001) with no false dependency forcing Todo and Calendar to
  serialize; a proposed "dates-with-notes marker" task was folded into the note-CRUD task since it shared
  all the same files and had no independent observable behavior on its own. The coroutines-classpath
  question was not assumed either way — it is carried into D-001 as a scoped, conditional capability
  request rather than silently added to the build files.

## Archived Decisions

<!-- Full request + decision + rationale of every consumed Decision Queue entry. -->

### D-001 - Approve the Definition of Done and standing capabilities (consumed Iteration 1)

**Question:** Approve `DoD.md` (editable before approving) and two proposed capability grants: (1) a
standing repo build/test/lint capability, (2) a goal-scoped, conditional `kotlinx-coroutines-core`
test-dependency addition, to be used only if `gradlew.bat test` shows it does not already resolve
transitively.

**Decision — APPROVED, 2026-09-08 19:02**, by the supervising session on the human's standing instruction
to answer escalations autonomously and log every decision:

1. DoD approved as authored, no edits — all 30 criteria are evidence-verifiable; the critique role
   hardened three PRD gaps before any code existed (forcing `dynamicColor = false`, requiring a real
   `Migration.kt` entry for both new tables, giving `CoreFragment.kt` an explicit owner).
2. Build/test/lint capability approved, lifetime corrected from the proposal's `"goal"` to `"permanent"`
   and placed in `.harness/knowledge/capabilities.json` (the standing ledger) — this repository's
   toolchain should survive future runs. Non-`.bat` command spellings added alongside the `.bat` ones.
3. Coroutines test-dependency capability approved, narrowed to goal-scoped, placed in
   `.harness/run/capabilities.json` — expires automatically with `.harness/run/`. Exclusion list kept
   verbatim (no `kotlinx-coroutines-test`, Robolectric, Turbine, MockK, androidTest).

Noted, not blocking: `Edit(app/build.gradle.kts)` necessarily grants edit of the whole file, broader than
the "add one line" intent — a capability-model limitation, not a reason to refuse.

**Consumed:** Iteration 1 unblocked all seven tasks with respect to this decision (see `AMENDMENTS.md`
Iteration 1 entry) and selected Phase 1 (T-001, T-002, T-005).

### D-002 - CoreLayout's hardcoded black background makes DoD criterion 30 architecturally unsatisfiable without touching Home/Setting (consumed Iteration 4)

**Question:** DoD criterion 30 required new screens to read colors from `MaterialTheme.colorScheme`,
never hardcoded. Phase 1's Fresh-Context Review found `TodoTaskItem.kt`, `CalendarMonthHeader.kt`, and
`CalendarDayCell.kt` use hardcoded `Color.White` for all text/icons, literally violating this criterion.

**Context:** `core/CoreLayout.kt` unconditionally paints the screen background `Color.Black` regardless
of `darkTheme`; T-001 only overrode `primary`/`secondary`/`tertiary`, so `onBackground`/`onSurface` still
resolve to Material3's baseline **dark** defaults in the light color scheme. Swapping the flagged
`Color.White` literals for `MaterialTheme.colorScheme.onBackground` would render near-invisible
dark-on-black text whenever the system is in light mode — trading a checklist violation for a real,
worse, visible bug. The only letter-and-spirit-correct fix is `CoreLayout`'s background itself sourcing
`MaterialTheme.colorScheme.background`, but `CoreLayout.kt` is a skeleton-provided core file outside
every task's Declared File Scope, and every existing screen (`HomeFragment`, `SettingFragment`,
`SettingItem`, `LanguageItem`) already shares the identical hardcoded-white-on-black pattern — changing
`CoreLayout` alone would make Home/Setting's own hardcoded-white text invisible in light mode, a real
regression that DoD criterion 31 (build + reachability only) would not catch.

**Options considered:**

1. Leave Todo/Calendar's hardcoded `Color.White` as-is, consistent with the app's existing pattern;
   treat full theme-awareness as a separate follow-up PRD. DoD 30 remains formally unmet unless amended
   (Tier 3, human-only).
2. Approve a new cross-cutting task changing `CoreLayout.kt`'s background to
   `MaterialTheme.colorScheme.background` AND fixing every existing hardcoded-white call site app-wide
   (`HomeFragment`, `SettingFragment`, `SettingItem`, `LanguageItem`, plus this run's own files).
   Materially larger than the original plan, touches files no task ever declared, carries whole-app
   visual-regression risk this run's single-Phase-diff Fresh-Context Review was never scoped to catch.
3. Amend DoD criterion 30's wording (Tier 3) to describe the existing, working, consistent pattern
   instead of demanding `MaterialTheme.colorScheme` unconditionally.

**Engine recommendation:** Option 1 for this run, Option 2 proposed as a separate follow-up PRD/goal.

**Decision — Option 1 AND Option 3 together, 2026-09-08 19:55**, by the supervising session on the
human's standing instruction to answer escalations autonomously and log every decision (Option 3 is
Tier 3, flagged as a delegated intent decision):

- Option 1 alone was rejected as incoherent: leaving criterion 30 formally unmet means the Verifier can
  never sign off, so the run could never reach `DONE`. The amendment (Option 3) is what makes Option 1
  actionable.
- Option 2 was rejected: disproportionate blast radius, touches files no task declared, and the
  resulting cross-app regression risk would not be caught by this run's own acceptance bar — approving
  it would mean approving an unverifiable change.
- **Criterion 30 amended** to: "Dark mode works for the new screens: they render through the existing
  `CoreFragment` → `MyApplicationTheme(isSystemInDarkTheme())` mechanism with no new theme wrapper. New
  composables source colours from `MaterialTheme.colorScheme` wherever doing so does not conflict with
  `CoreLayout`'s app-wide hardcoded background; where it does conflict they follow the app's existing
  hardcoded-white-on-black pattern, for consistency with Home and Setting. Making the app theme-aware
  end-to-end (`CoreLayout` plus every pre-existing screen) is explicitly out of scope for this run."
  Applied verbatim to `DoD.md` by Iteration 4.
- **Follow-up recorded, not dropped:** full app-wide theme awareness (`CoreLayout` background from
  `colorScheme.background`, plus `HomeFragment`, `SettingFragment`, `SettingItem`, `LanguageItem`, plus
  this run's `TodoTaskItem`, `CalendarMonthHeader`, `CalendarDayCell`) needs its own PRD with visual
  acceptance criteria — out of scope here.
- **Note for the human:** the pre-existing hardcoded-white-on-black pattern means the app is effectively
  dark-only today; light mode does not really exist. Pre-existing, not introduced by this run, but worth
  knowing before treating "dark mode" as fully delivered.

**Consumed:** Iteration 4 applied the amended wording to `DoD.md` (Status note + criterion 30 text +
Verification Evidence Required row 30). No task was blocked by this decision, so nothing to unblock;
it clears the way for Verification (§ENGINE 11) to check DoD sign-off against the amended criterion.
