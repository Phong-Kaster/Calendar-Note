# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

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
