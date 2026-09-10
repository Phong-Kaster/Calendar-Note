# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> A fresh engine invocation must be able to resume from this file plus the repository alone.
>
> **This file is read in full at Orient, every iteration, so it must not grow with the run.** Keep
> only the last three iterations verbatim under *Recent Iterations*; older ones become one-line rows
> in *Iteration Index*, and each consumed escalation becomes one line in *Escalation Index*. Both
> rows carry the checkpoint SHA that still holds the full text: `git show <sha>:.ai/STATE.md`.

## Current

- **Phase:** executing
- **Loop Branch:** `loop/calendar-note-app`
- **Next task:** **T-001** — dark-only theme with blue primary. Unblocked; nothing is pending.
- **DONE-candidate:** no
- **DoD:** ✅ approved 2026-09-10, A5 overruled to title + body. Immutable from here — propose
  changes (Tier 3), never apply them.
- **Escalation:** D-001 ✅ consumed. `.ai/ESCALATION.md` is marked CONSUMED and must not be
  re-consumed.
- **Capabilities:** `knowledge/capabilities.json` now grants `./gradlew`
  assemble/compile/test/lint, `validate|updateDebugScreenshotTest`, and
  `MSYS_NO_PATHCONV=1 git show|ls-tree`. **None has been executed yet** — iteration 1's permissions
  were compiled before the ledger existed, which is why this iteration still ended without a build.
- **`knowledge/DOMAIN.md` now exists** (two rules: no future-dated notes; `updatedAt`-descending
  ordering). Read it at Orient every iteration. It outranks the codebase, and it is deny-listed —
  a rule you disagree with is an escalation, never an edit.

### First thing the next iteration should do

Run `./gradlew :app:assembleDebug` **before** writing any code. Nothing in this repository has been
compiled by this run, so "the skeleton builds" is an assumption. Finding out after T-001's changes
are in the tree makes a pre-existing failure look like one this run caused.

## Progress

| Task | Status | Evidence |
|---|---|---|
| T-001 | pending | — |
| T-010 | pending | — (new — amendment A-002; runs between T-001 and T-002) |
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
criteria are recorded in `.ai/DoD.md` § *Assumptions baked into these criteria*. **They are no longer
assumptions — the DoD is approved, so they are intent.** A5 was overruled by the human (body-only →
title + body); the other eleven were accepted as written. They are not duplicated here.

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
- **Then, mid-iteration, the human answered D-001 in full** — first approving `.ai/DoD.md` (A5
  overruled), then writing the complete decision, `knowledge/capabilities.json` and
  `knowledge/DOMAIN.md`. Consumed and reconciled inside this same iteration:
  - **A5 → title + body** propagated to T-002/T-003/T-004; amendment **A-001**.
  - **Q4 corrected a wrong conclusion of mine.** I reported that this repository "has no host-side
    test setup" and classified the perceptual clauses of criteria 1, 2, 7 and 9 as provable by no
    command. A prior run on the unmerged branch `loop/todo-calendar-screens` had already built a
    working screenshot harness on this exact toolchain. I branched from `main` and never checked
    sibling `loop/*` branches — the refutation was reachable under baseline capabilities the whole
    time. New task **T-010** imports it; amendment **A-002**; the standing lesson is in
    `knowledge/PROJECT.md`.
  - **Evidence classes re-scoped:** criteria 1, 2, 7, 9 are no longer "unprovable" but "human
    approves the reference image once, machine defends it thereafter".
  - **Criterion 13 gains `:app:validateDebugScreenshotTest`** per the decision. `.ai/DoD.md` was
    **not** edited to say so — it is approved and immutable to the engine. Honoured from the task
    files; the human can paste it into the DoD if they want the file to match.
  - **`knowledge/DOMAIN.md` rule 1** applies to *every* write path, not just create — T-004 and
    T-008 updated accordingly.
  - The prior run's three recorded regressions (invisible has-notes dot on today's fill, selection
    never reaching the grid, blank gap for an empty day — all green under its unit suite) written
    into T-006 and T-007 as traps to avoid rather than rediscover.
- **Reporting `CONTINUE`, not `ESCALATE`:** nothing is pending. Reporting `DONE` would be absurd and
  `ESCALATE` would be false. No code was written because this was the Bootstrap Iteration and
  ENGINE.md §5 forbids implementing in it — and because the granted `./gradlew` capability is not in
  this process's compiled permissions, so T-001 could have been written but not evidenced.

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
| D-001 | `9b25307` | Approve DoD + grant toolchain + choose UI-evidence strategy → DoD approved with A5 overruled to title+body; C1 toolchain granted as proposed (narrow rules) plus screenshot and `MSYS_NO_PATHCONV` entries; test deps granted; Q4 Option B **plus** import the existing harness from `loop/todo-calendar-screens`; `DOMAIN.md` created with rules 1+2. |

`9b25307` holds the request **as issued**. The decision text is in `.ai/ESCALATION.md` at iteration
1's second checkpoint (marked CONSUMED) and reproduced in that commit's message — read it back with
`MSYS_NO_PATHCONV=1 git show <sha>:.ai/ESCALATION.md`.
