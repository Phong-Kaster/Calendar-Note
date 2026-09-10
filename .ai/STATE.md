# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> A fresh engine invocation must be able to resume from this file plus the repository alone.
>
> **This file is read in full at Orient, every iteration, so it must not grow with the run.** Keep
> only the last three iterations verbatim under *Recent Iterations*; older ones become one-line rows
> in *Iteration Index*, and each consumed escalation becomes one line in *Escalation Index*. Both
> rows carry the checkpoint SHA that still holds the full text: `git show <sha>:.ai/STATE.md`.

## Current

- **Phase:** escalated
- **Loop Branch:** `loop/calendar-note-app`
- **Next task:** T-001 — blocked until `.ai/DoD.md` is approved (ENGINE.md §5: the DoD gate is the
  only mandatory human gate before autonomous execution)
- **DONE-candidate:** no

## Progress

| Task | Status | Evidence |
|---|---|---|
| T-001 | pending | — |
| T-002 | pending | — |
| T-003 | pending | — |
| T-004 | pending | — |
| T-005 | pending | — |
| T-006 | pending | — |
| T-007 | pending | — |
| T-008 | pending | — |
| T-009 | pending | — |

## Assumptions

The twelve product assumptions (A1–A12) that turned `PRD.md`'s nine open questions into testable
criteria are recorded in `.ai/DoD.md` § *Assumptions baked into these criteria*, where the human can
edit them before approving. They are not duplicated here.

Execution assumptions made without asking, because they are minor and reversible:

- **The demo `Post` / network stack stays.** `PostApi`, `PostRepository`, `PostDao`, `WeatherApi` and
  `networkModule` remain wired in Koin but stop being used once Home is repointed at notes (T-002).
  The PRD asks for no network feature but also does not ask for a deletion, and this repository is
  explicitly a reusable skeleton whose network layer is part of its value. Reversible: deleting it
  later is a small, isolated change. If it is still unused at completion it becomes a
  `knowledge/ISSUES.md` entry, not a silent leftover.
- **`applicationId` and the `com.example.skeleton` package are not renamed.** Only the `app_name`
  display string changes (A12). Renaming the package touches every file and is not asked for.
- **Notes are dated by the device's local date.** `LocalDate.now()` with the system zone. Stated
  again in the KDoc of the future-date rule in T-008.
- **`date` is persisted as an epoch-day `Long`**, not through a type converter. The existing
  `DateConverter` handles `java.util.Date`, not `java.time.LocalDate`, and a `Long` column keeps the
  DAO's `ORDER BY` and `WHERE` straightforward.

## Expected untracked paths — not debris

`git status` on this branch permanently shows three untracked directories:
`.loop/`, `.claude/skills/`, `.agents/skills/`. They are human-installed tooling (the Foreman runtime
and its skills), they were already untracked on `main` before the run began, and `.loop/` is a
protected path the engine may not write to. **They are not the debris of a crashed invocation.**

At Recover (ENGINE.md §6.1), a dirty tree means a previous invocation crashed mid-flight — but only
if the dirt is *this run's*. Treat these three paths as clean; anything else untracked or modified is
genuine debris to salvage or revert. Do not `git clean` them.

## Recent Iterations

### Iteration 1 — 2026-09-10 — Bootstrap

- **Attempted:** the Bootstrap Iteration (ENGINE.md §5). No implementation, by design.
- **Did:** read `PRD.md`, `CLAUDE.md` and the five `.claude/*.md` rule files, the Gradle
  configuration, the manifest, the nav graph, the themes, and the existing `core/`, `data/`,
  `domain/`, `injection/`, `ui/` sources. Created `knowledge/PROJECT.md` and `knowledge/ISSUES.md`,
  the Loop Branch `loop/calendar-note-app`, and `.ai/` (`DoD.md`, `PLAN.md`, nine `TASKS/`,
  `STATE.md`, `AMENDMENTS.md`, `ESCALATION.md`).
- **Learned:**
  - `./gradlew --version` was **refused** by the permission layer. No toolchain command has ever run
    here, so every command in `knowledge/PROJECT.md` is recorded `Verified: no`. Proposed as standing
    capabilities in the escalation. → `knowledge/ISSUES.md`.
  - `CLAUDE.md` `@`-imports three rule files that do not exist (`view-model-layer.md`,
    `jetpack-compose-ui-layer.md`, `wiki-connection.md`); the real names are `viewmodel-layer.md` and
    `jetpack-compose-ui.md`, and there is no wiki file. The rules were read directly from the real
    files. `CLAUDE.md` is human-owned → `knowledge/ISSUES.md`, not an edit.
  - PRD open question 1 (Compose-in-Fragment vs. `RecyclerView`) is already answered by the codebase:
    `CoreFragment` exists solely to host a `ComposeView`, and `HomeFragment` already renders a
    `LazyColumn`. Recorded as A1 rather than escalated as an open choice.
  - `java.time` is safe on `minSdk 24` here because core-library desugaring is enabled — a build
    configuration fact, not a language guarantee. → `knowledge/PROJECT.md`.
  - There is no host-side test infrastructure and no CI. Anything provable only by rendering is
    currently unprovable; the DoD names that gap explicitly rather than narrowing the criteria to
    their machine-checkable half (`POLICIES.md` § Evidence Requirements).
- **Reconciled:** two entries filed to `knowledge/ISSUES.md` (unverified toolchain; broken
  `CLAUDE.md` imports); operational facts written to `knowledge/PROJECT.md`; one Escalation Request
  written covering the DoD approval, the standing toolchain capability, and the host-side UI-test
  proposal that would close part of the evidence gap. No plan amendments (the plan is new).
- **Checkpoint:** `9b25307`, plus one follow-up commit backfilling that SHA into the two
  `knowledge/ISSUES.md` entries. `POLICIES.md` requires every entry to cite the commit holding its
  full record, and a commit cannot contain its own hash — amending would only have moved the
  self-reference, so the backfill is a second commit rather than a history rewrite. Both entries now
  resolve after the Cleanup Commit removes `.ai/`.

## Iteration Index

<!-- One line per iteration older than the three above. The SHA is the checkpoint commit whose
     STATE.md still holds that iteration's full entry:  git show <sha>:.ai/STATE.md -->

| Iteration | Checkpoint | What happened |
|---|---|---|
| — | — | none yet |

## Escalation Index

<!-- One line per consumed Escalation Request, newest first. A pending escalation is not indexed —
     it is still in .ai/ESCALATION.md, unanswered. -->

| ID | Checkpoint | Question → decision |
|---|---|---|
| — | — | none consumed yet (D-001 is pending) |
