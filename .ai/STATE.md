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
- **Next task:** **T-010** — import the host-side screenshot harness from
  `loop/todo-calendar-screens`. Its only dependency (T-001) is complete. Nothing is pending.
- **DONE-candidate:** no
- **DoD:** ✅ approved 2026-09-10, A5 overruled to title + body. Immutable from here — propose
  changes (Tier 3), never apply them.
- **Escalation:** D-001 ✅ consumed. `.ai/ESCALATION.md` is marked CONSUMED and must not be
  re-consumed.
- **Capabilities:** `knowledge/capabilities.json` grants `./gradlew`
  assemble/compile/test/lint, `validate|updateDebugScreenshotTest`, and
  `MSYS_NO_PATHCONV=1 git show|ls-tree`. **Build, unit test and lint are now all verified working**
  (iteration 2) — see `knowledge/PROJECT.md` § Toolchain. The two screenshot tasks remain unrun
  because the harness they drive is not imported yet; that is T-010.
- **`knowledge/DOMAIN.md` exists** (two rules: no future-dated notes; `updatedAt`-descending
  ordering). Read it at Orient every iteration. It outranks the codebase, and it is deny-listed —
  a rule you disagree with is an escalation, never an edit. Neither rule is implemented yet; both
  land in T-002/T-008.

### First thing the next iteration should do

Read `knowledge/PROJECT.md` § Toolchain before running anything — it now carries three traps that
cost this iteration real time: a **piped Gradle command reports the pipe's exit code**, so a
`BUILD FAILED` looks like a pass; **Gradle run in the background races your own edits**; and **lint
fails on a missing German translation**, which makes adding a string a two-file operation.

## Progress

| Task | Status | Evidence |
|---|---|---|
| T-001 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 55 warnings`) + `testDebugUnitTest` (6 tests, 0 failures) all green; no `lightColorScheme`/`isSystemInDarkTheme`/dynamic-colour hits in `app/src/main`; 36/36 Material roles assigned **and now guarded by `DarkColorSchemeTest`**; README tree verified against all 82 source files. Fresh-context review: 16 findings, 1 Critical + 4 Major all fixed, 3 filed to `ISSUES.md`. Full record in `.ai/TASKS/T-001.md`. **3 perceptual items await human eyes** (see that file). |
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

### ⚠️ The engine is now writing German product copy — overrule this cheaply if it is wrong

Added iteration 2, and the one assumption here a human might actually want to reverse.

Lint in this repository treats a missing German translation as a build **error**, and the tree was
already failing that way on four strings before this run began. Since criterion 13 wants lint green
and criterion 14 wants new strings in `res/values/strings.xml`, something had to give. **The
assumption taken: `res/values-de/strings.xml` is kept in sync, so the engine authors a German
translation for every user-visible string this run adds** (~15 across T-002 to T-009). Four were
written in T-001 to clear the baseline failure. Logged in full as amendment **A-003**.

Why this was recorded rather than escalated: it blocks nothing, it is reversible, and its worst
outcome is clumsy German, not a wrong app. But it *is* content nobody asked for. The alternative is
a one-line answer — **"German is a stale demo locale, drop `values-de/`"** — which would delete
A-003 and every German string with it. If that is the intent, say so and it costs one task.
Suppressing the `MissingTranslation` check was considered and rejected: it would green-light
criterion 13 over a real defect.

## Expected untracked paths — not debris

`git status` on this branch permanently shows three untracked directories:
`.loop/`, `.claude/skills/`, `.agents/skills/`. They are human-installed tooling (the Foreman runtime
and its skills), they were already untracked on `main` before the run began, and `.loop/` is a
protected path the engine may not write to. **They are not the debris of a crashed invocation.**

At Recover (ENGINE.md §6.1), a dirty tree means a previous invocation crashed mid-flight — but only
if the dirt is *this run's*. Treat these three paths as clean; anything else untracked or modified is
genuine debris to salvage or revert. Do not `git clean` them.

## Recent Iterations

### Iteration 2 — 2026-09-10 — T-001, and the first build this repository has ever run

- **Attempted:** T-001 — the fixed dark theme, the blue primary, the app rename, the README.
- **Did it in this order, deliberately:** ran `./gradlew :app:assembleDebug` **before touching a
  single file**, because iteration 1 left instructions to. Then the theme, then `Color.kt`, then
  `CoreLayout`/`CoreBottomBar`, then `themes.xml`, strings and README, then build + lint + test
  again, then a fresh-context review.
- **Learned — and this is the finding of the iteration:**
  - **The skeleton builds.** `BUILD SUCCESSFUL in 58s`, cold, unmodified. So do
    `testDebugUnitTest` and `compileDebugKotlin`. `knowledge/PROJECT.md` § Toolchain went from
    six `Verified: no` rows to four `yes`, and the `ISSUES.md` entry tracking that gap is deleted.
  - **But `./gradlew :app:lintDebug` FAILED on the pristine tree** — `Lint found 4 errors`, all
    `MissingTranslation` for German. **Measuring before touching is the only reason this is
    legible.** Had T-001's diff gone in first, a pre-existing red would have read as this run's
    regression, and the obvious "fix" would have been to unpick correct theme work. The four
    strings were translated; lint now reports `0 errors, 56 warnings`. → amendment **A-003**, and
    the standing consequence is in `PLAN.md`: adding a string here is a two-file operation.
  - **A piped Gradle command returns the pipe's exit code.** `… | tail -20` exited **0** on that
    `BUILD FAILED`. Caught only by reading the `BUILD` line. → `knowledge/PROJECT.md`.
  - **Never run Gradle in the background while editing.** A backgrounded baseline
    `testDebugUnitTest` reached the tree mid-edit, compiled half-written files and reported a
    failure that described nothing real. → `knowledge/PROJECT.md`.
  - **`CLAUDE.md`'s rule files and the DoD disagree about hardcoded colour.**
    `.claude/figma-design-system.md` § "Colors: two systems" explicitly permits inline
    `Color(0xFF…)` for one-off values; DoD criterion 2 and `POLICIES.md` § User-Interface Defects
    forbid it. Resolved by source-of-truth order (ENGINE.md §3): DoD and POLICIES win for code this
    run writes. Recorded in `PROJECT.md` § Architecture Conventions so it is not re-litigated, and
    flagged in the summary as the rule files themselves instruct.
  - **`values-v30/themes.xml` needed no edit** — it inherits `Core.Theme.JetpackCompose`, where
    `windowLightStatusBar` lives, so the fix propagates to API 30+ on its own.
  - **`app_name` had a stale German copy** despite being `translatable="false"`. `translatable` is
    a lint hint, not a runtime one, so German users would have kept the old app name. Deleted.
  - **Writing the contrast ratios down caught a defect that reading the colours did not.**
    `outline` started as `#3B4652` — a perfectly ordinary-looking grey that is **2.19:1** on black,
    under the 3:1 floor for a visible boundary. T-006 draws calendar day-cell borders from that
    role, so the grid would have had invisible edges and criterion 9 would have been judged against
    it. Raised to `#55616E` (3.35:1). **Lesson worth carrying: for a dark theme, compute the ratio
    rather than eyeballing the swatch** — every one of these greys looks fine in isolation.
  - **The fresh-context review earned its keep — 16 findings, 1 Critical.** The README's tech-stack
    table said **Retrofit**; this app uses **Ktor**. That is criterion 14's own "documentation
    matches the code" made false by the very file added to satisfy it. Fixed, along with 4 Major:
    invisible dividers (`outlineVariant` at **1.26:1** — the role `HorizontalDivider` uses by
    default), `customizedTextStyle`'s default still being a `Color.White` literal, and
    `CoreBottomSheet.containerColor` defaulting to **white in a dark-only app**. Full table in
    `.ai/TASKS/T-001.md`.
  - **A test that fails is worth more than a KDoc that asserts.** Criterion 1 says no role is left
    at a Material default; that was a comment until `DarkColorSchemeTest` was written, and the test
    **failed on its first run**. The flagged role (`scrim`) turned out to be the test's blind spot
    rather than the theme's bug — a role deliberately set to Material's own value is
    indistinguishable from one forgotten — so it now has an explicit assertion, plus a reflection
    check that counts `ColorScheme`'s roles (36) so a Compose upgrade adding roles fails loudly.
- **Reconciled:** toolchain facts + three earned traps → `knowledge/PROJECT.md`; the resolved
  toolchain entry deleted from `knowledge/ISSUES.md` and replaced with **three** entries — the
  hardcoded colour literals still in 8 pre-existing files, the `.claude/figma-design-system.md`
  rule file that both contradicts criterion 2 *and* names four tokens that do not exist in
  `Color.kt`, and three misnamed pre-existing string keys (one of which names a prayer-time
  feature this app has never had). Amendment **A-003** for the translation rule; `PLAN.md` risk 1
  closed with what it actually caught; review finding #16 parked in T-003's notes.
- **Not done, on purpose:** the six `Purple*`/`Pink*` template tokens were deleted only after
  confirming `Theme.kt` was their sole referent. The demo `Post`/network stack is untouched, per
  the standing assumption above.
- **Checkpoint:** see the `loop(T-001)` commit.

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
