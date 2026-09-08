# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

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
