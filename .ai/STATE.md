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
- **Next task:** **T-007** — selecting today or a past day on the Calendar shows that day's notes,
  with an explicit empty state. Its dependency (T-006) is now complete and it is the only
  unblocked task — T-008 waits on it, T-009 on T-008.

  **Most of what T-007 needs already exists and should be read, not rebuilt.** `selectedDate` is
  in `CalendarUiState`, a day can already be picked, and the ViewModel already subscribes to
  `notesFlow` — it currently keeps only the *dates* (`datesWithNotes`) because the grid draws a
  dot, and `CalendarViewModel.collectNotes`'s KDoc says out loud that the day's actual notes are
  a T-007 concern. Ordering comes free and must not be re-derived: `knowledge/DOMAIN.md` rule 2
  says a per-day list follows `updatedAt` descending like Home's, and `notesFlow` already arrives
  in that order.

  **The third defect the prior run shipped on this screen is T-007's, and it is named in
  `.ai/TASKS/T-006.md` § Notes: a selected day with no notes rendered an unexplained blank gap.**
  `POLICIES.md` § User-Interface Defects wants an explicit empty state; record a reference image
  of it, because that is a defect only a picture catches. Note also that `selectedDate` is
  nullable and **can now become null** — `refreshToday` drops a selection a backwards clock has
  overtaken (iteration 8) — so "nothing picked" is a state the screen has to draw, not just an
  initial value it never sees.

  **Read `.ai/TASKS/T-003.md` before starting T-008**; it had work removed and its task file says
  what is left. T-008 must also resolve part 2 of `knowledge/ISSUES.md` § *Leaving the Note editor
  either loses the user's text or traps them on the screen* — a refusal becomes reachable by
  ordinary use the moment a past day can be picked.
- **DONE-candidate:** no
- **DoD:** ✅ approved 2026-09-10, A5 overruled to title + body. Immutable from here — propose
  changes (Tier 3), never apply them.
- **Escalation:** D-001 ✅ consumed. `.ai/ESCALATION.md` is marked CONSUMED and must not be
  re-consumed.
- **Capabilities:** `knowledge/capabilities.json` grants `./gradlew`
  assemble/compile/test/lint, `validate|updateDebugScreenshotTest`, and
  `MSYS_NO_PATHCONV=1 git show|ls-tree`. **All six are now verified working** — build, unit test and
  lint in iteration 2, the two screenshot tasks in iteration 3. See `knowledge/PROJECT.md`
  § Toolchain. **`rm` is denied**, so the engine cannot delete a file it creates by mistake; that is
  not a bug to route around (ENGINE.md §13) but a constraint to plan for.
- **`knowledge/DOMAIN.md` exists** (two rules: no future-dated notes; `updatedAt`-descending
  ordering). Read it at Orient every iteration. It outranks the codebase, and it is deny-listed —
  a rule you disagree with is an escalation, never an edit. **Both rules are now implemented and
  tested** — rule 2 in iteration 4, rule 1 in iteration 5 (amendment A-006, moved forward from
  T-008 because T-003 created the first write path), and rule 2's *list* half — that an edit
  actually moves the note to the top of what Home shows — in iteration 6. **DoD criterion 10 is
  satisfied.** Neither is finished being *defended*: any new write path must reach the same `save`,
  and any test that carries either rule should be mutation-checked rather than trusted — per
  assertion, not per test (iteration 6).

### First thing the next iteration should do

**`grep -r MUTATION app/src`, before anything else, if the working tree is dirty.** Iteration 7
began by finding a *previous* invocation's mutation still applied in `NoteViewModel.delete` — the
process had died between applying it and reverting it. The rest of the debris was coherent,
complete and would have built and tested green, which is exactly what makes it dangerous: mutated
code is *deliberately wrong*, and an invocation that resumes on top of it inherits a defect it did
not write and has no reason to suspect. See `.ai/PLAN.md` § Verification approach. Iteration 8 ran
the check on a dirty tree and it came back clean in ten seconds — which is the point: it is cheap
enough to be unconditional.

**Then ask what the debris cannot tell you.** Iteration 8's debris was a *complete* task — green
on all four commands — and its own `knowledge/ISSUES.md` entries cited "the fresh-context review
of T-006, finding 1 / finding 3". So a review had been run and **its output died with the
process.** Green commands are reproducible; a review is not. The re-run returned a Critical that
the first review had filed rather than fixed, so accepting the dead review's word would have
checkpointed a task whose own notes admitted a DoD criterion was false. **A review whose findings
cannot be read is not evidence. Re-run it.**

Then read `knowledge/PROJECT.md` § Toolchain before running anything. It carries thirteen traps,
every one of them earned by a real failure or a denied command:

- a **piped Gradle command reports the pipe's exit code**, so a `BUILD FAILED` looks like a pass;
- **Gradle run in the background races your own edits**;
- **lint fails on a missing German translation**, which makes adding a string a two-file operation;
- **`updateDebugScreenshotTest` orphans reference images** rather than replacing them when a
  preview's size or name changes;
- **a green `validateDebugScreenshotTest` is not evidence for DoD criterion 2** — a screenshot
  cannot tell a hardcoded literal from a theme lookup;
- **a hand-written Room migration is unverifiable here**, so copy the statement out of Room's
  generated `AppDatabase_Impl` rather than composing one;
- **`git reset`/`git restore` are denied** — the allowed way to unstage is `git rm --cached`,
  naming individual files rather than a folder;
- **`assembleDebug` + `testDebugUnitTest` green does not mean the tree compiles** — neither task
  touches `app/src/screenshotTest/`, so run `validateDebugScreenshotTest` in the same command
  whenever a composable's signature changes (new in iteration 5);
- **unit tests run against a stub `android.jar`**, and since iteration 5
  `isReturnDefaultValues = true` makes framework calls return defaults instead of throwing — which
  is what makes error paths testable, and also means a `Bundle`/`Intent`/`Uri` can never be the
  subject of a unit test here (new in iteration 5);
- **nothing in this app handles IME insets**, so any new screen with a text field needs
  `Modifier.imePadding()` before its `verticalScroll` (new in iteration 5);
- **`NavigationUtil.canNavigate()` is one 800 ms clock for the whole app**, armed even by a
  bottom-bar tap that navigates nowhere — so using it to debounce a list row leaves that row dead
  for 800 ms after any tab tap. Ask the graph where you are instead (iteration 6);
- **the engine runs as a single `claude -p` invocation**, so ending the turn ends the iteration —
  a background subagent must be waited on *inside* the turn, or launched synchronously (new in
  iteration 7, and the likeliest explanation for the two invocations that died earlier in this run);
- **one failing Gradle task aborts the others, and the stale XML left behind reads exactly like a
  pass** — a test count that did not go up after adding tests means the task did not run (new in
  iteration 7);
- **`ScreenshotScaffold` applies the theme itself**, so a `Color` passed in from a call site or a
  default argument is resolved against Material's *light* baseline — which is why its ground is an
  enum (new in iteration 7).

## Progress

| Task | Status | Evidence |
|---|---|---|
| T-001 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 55 warnings`) + `testDebugUnitTest` (6 tests, 0 failures) all green; no `lightColorScheme`/`isSystemInDarkTheme`/dynamic-colour hits in `app/src/main`; 36/36 Material roles assigned **and now guarded by `DarkColorSchemeTest`**; README tree verified against all 82 source files. Fresh-context review: 16 findings, 1 Critical + 4 Major all fixed, 3 filed to `ISSUES.md`. Full record in `.ai/TASKS/T-001.md`. **3 perceptual items await human eyes** (see that file). |
| T-010 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 59 warnings`) + `testDebugUnitTest --rerun-tasks` (6 tests, 0 failures) + `validateDebugScreenshotTest` (**6 cases, 0 failures**) all green. `ScreenshotScaffold.kt` diffed byte-identical against `loop/todo-calendar-screens`. Six reference PNGs committed. Fresh-context review: 16 findings, 0 Critical; 3 Major + 5 Minor fixed, the rest filed. Full record in `.ai/TASKS/T-010.md`. **Human approval of the six images is still open** — see that file. |
| T-002 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 59 warnings`) + `testDebugUnitTest --rerun-tasks` (**17 tests, 0 failures**) + `validateDebugScreenshotTest` (**7 cases, 0 failures**) all green, all re-run after the review fixes. `MIGRATION_2_3` is a verbatim copy of Room's generated `createAllTables` statement. Fresh-context review: 0 Critical, 2 Major (both knowledge reconciliation, both fixed), 7 Minor (6 fixed, 3 accepted with reasons). Full record in `.ai/TASKS/T-002.md`. **3 items await human eyes** — see that file. |
| T-003 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 60 warnings`) + `testDebugUnitTest --rerun-tasks` (**38 tests, 0 failures**) + `validateDebugScreenshotTest` (**7 cases, 0 failures**) all green, all re-run after the review fixes. **The new tests were mutation-checked**: three separate mutations to the implementation failed exactly the five expected tests. **DoD criterion 10 is satisfied here** (A-006). Fresh-context review: 2 Critical (both fixed — the editor was unusable with the keyboard open; a double tap on Save wrote two notes), 2 Major (both fixed), 4 Minor (2 filed, 2 accepted with reasons). Full record in `.ai/TASKS/T-003.md`. **5 items await human eyes** — see that file. |
| T-004 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 60 warnings`, unchanged) + `testDebugUnitTest --rerun-tasks` (**41 tests, 0 failures**) + `validateDebugScreenshotTest` (**7 cases, 0 failures**) all green, all re-run after the review fixes. **The new tests were mutation-checked, and the fourth mutation exists because the reviewer proved one assertion could not fail.** Fresh-context review: 0 Critical, 3 Major (1 fixed — note rows were dead for 800 ms after any bottom-bar tap; 2 filed with a schedule), 3 Minor (all fixed). **DoD criterion 6 is satisfied bar its device walkthrough, and criterion 12's "full text reachable" clause is now true.** Full record in `.ai/TASKS/T-004.md`. **4 items await human eyes** — see that file. |
| T-005 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 61 warnings`) + `testDebugUnitTest --rerun-tasks` (**63 tests, 0 failures**) + `validateDebugScreenshotTest` (**8 cases, 0 failures**) all green, all re-run after the review fixes. **Eight mutations, each failing exactly the expected test(s)** — three of them aimed at the review's own fixes. **A-009 is discharged: `getNote` returns `Outcome<Note?>` and the `ISSUES.md` entry is deleted.** Fresh-context review: 10 findings — **1 Critical** (a tap on Save during the 500 ms exit animation put the deleted note back), 3 Major, 6 Minor; 8 fixed here, 2 filed. Full record in `.ai/TASKS/T-005.md`. **5 items await human eyes, one of which is criterion 7's perceptual clause** — see that file. |
| T-006 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 64 warnings`) + `testDebugUnitTest --rerun-tasks` (**113 tests, 0 failures**) + `validateDebugScreenshotTest` (**16 cases, 0 failures**) all green, all re-run after the review fixes. **Seven mutations, each failing exactly the predicted tests — and three of them correctly failing nothing extra.** M7 proves the prior run's "selection never reached the grid" defect is now machine-defended, and that only the *grid-level* image can catch it. Fresh-context review: 16 findings — **1 Critical** (DoD criterion 9 was knowingly false across midnight, and the `ISSUES.md` entry filed for it refuted its own stated blocker), 2 Major, 13 Minor; 12 fixed here, 3 filed, 1 recorded as a product call. Full record in `.ai/TASKS/T-006.md`. **8 reference images await human eyes, one of which is criterion 9's perceptual clause** — see that file. |
| T-007 | pending | — |
| T-008 | pending — **reduced by A-006** | — |
| T-009 | pending | — |

## Assumptions

The twelve product assumptions (A1–A12) that turned `PRD.md`'s nine open questions into testable
criteria are recorded in `.ai/DoD.md` § *Assumptions baked into these criteria*. **They are no longer
assumptions — the DoD is approved, so they are intent.** A5 was overruled by the human (body-only →
title + body); the other eleven were accepted as written. They are not duplicated here.

Execution assumptions made without asking, because they are minor and reversible:

- **The demo `Post` / network stack stays — and since iteration 4 the demo `UserAction` stack does
  too.** `PostApi`, `PostRepository`, `PostDao`, `WeatherApi` and `networkModule` are wired in Koin
  and unused now that Home reads notes; `UserActionRepository` lost its only caller in the same
  change, when `HomeViewModel.createUser()` went with the posts. The PRD asks for no network
  feature but also does not ask for a deletion, and this repository is explicitly a reusable
  skeleton whose network and Room verticals are part of its value. Reversible: deleting them later
  is a small, isolated change. `injection/RepositoryModule.kt` now says out loud that both are
  deliberate rather than forgotten. If they are still unused at completion they become a
  `knowledge/ISSUES.md` entry, not a silent leftover.
- **`applicationId` and the `com.example.skeleton` package are not renamed.** Only the `app_name`
  display string changes (A12). Renaming the package touches every file and is not asked for.
- **Notes are dated by the device's local date.** `LocalDate.now()` with the system zone. Stated
  again in the KDoc of the future-date rule in T-008.
- **`date` is persisted as an epoch-day `Long`**, not through a type converter. The existing
  `DateConverter` handles `java.util.Date`, not `java.time.LocalDate`, and a `Long` column keeps the
  DAO's `ORDER BY` and `WHERE` straightforward.
- **The bottom bar's centre button creates a note from Settings too, and saving there returns to
  Settings.** Added iteration 5, and the one place T-003 does not read DoD criterion 5 literally.
  The criterion says "saving it returns to **Home**, where the new note is the first row" — true on
  the Home path, and from Settings the user lands back on Settings without seeing what they wrote.
  Three reasons it was taken this way rather than forcing every save to Home: the criterion is
  written about the Home flow (assumption A11 contemplated no other), `safeNavigateUp()` returning
  the user where they came from is the behaviour T-008 actually needs (a note added to a selected
  calendar day belongs back on the Calendar screen, per criterion 11), and the alternative —
  leaving Settings' centre button dead — is the defect T-003 existed to remove. Reversible in one
  line: `safePopBackstack(destination = R.id.homeFragment, inclusive = false, saveState = false)`
  instead of `safeNavigateUp()`. Say so and it changes.

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

Since iteration 3 there are also **three untracked orphan PNGs** under
`app/src/screenshotTestDebug/reference/com/example/skeleton/screenshot/ThemeScreenshotTestKt/`:
`PaletteAccents_…_479b9379_0.png`, `PaletteSurfaces_…_56c32381_0.png`,
`ScreenShell_…_4c69c6b2_0.png`. They are superseded renderings that `updateDebugScreenshotTest`
left behind when previews changed size; the six live references *are* committed. They are untracked
rather than deleted because `rm` is denied to the engine. Listed in `knowledge/ISSUES.md` for a
human to remove. **Do not treat them as this run's debris and do not try to route around the deny
rule to delete them.**

Iteration 4 added **no** new orphans — one new reference image, one new case, and the six inherited
PNGs verified byte-unchanged by `git status` after the `update` run. But it did find the trap next
to them: **`git add -A app/src` stages the three strays**, and `git reset` / `git restore --staged`
are both refused by the permission layer. The way back is `git rm --cached`, naming the three files
individually — never `-r` on their folder, which holds the six committed references too. Recorded
in `knowledge/PROJECT.md`.

Iteration 5 added **no** new orphans and left nothing uncommitted. It did, however, *start* at
Recover: a modified `domain/model/Note.kt` was sitting in the tree, which is exactly what this
section is for distinguishing. **The test that worked, and is worth reusing:** compare the file's
mtime against the tip commit's timestamp, and check whether `.ai/` was touched. Here the file was
eight minutes *newer* than the `loop(T-002)` checkpoint while `.ai/` was untouched — so an
invocation had begun T-003 and died before persisting anything, and the debris was a coherent
partial edit rather than a half-written mess. It was verified by building and testing before being
built on (ENGINE.md §6.1 forbids the reverse) and then salvaged.

Iteration 8 added **one** new orphan, and it is the trap working exactly as `PROJECT.md`
describes: `CalendarScreenshotTestKt/BottomBarTabGermanLabel_…_e7c93008_0.png`. The German
bottom-bar case was first recorded at `heightDp = 70` and the label did not fit, so the preview's
height was changed to 94 — and because the filename hashes the preview's *parameters*,
`updateDebugScreenshotTest` wrote a **new** file (`…_967f30ea_0.png`) and left the first sitting
there. Only `967f30ea` is committed. `e7c93008` is untracked because `rm` is denied, and it is
listed here rather than in `ISSUES.md` because it is debris of this iteration, not a defect in the
app. A human can delete it with the other three.

Iteration 6 added **no** new orphans and left nothing uncommitted. It did leave one ignored file:
the review subagent wrote **`build-t004-review.log`** at the repository root. It is git-ignored, so
it never appears in `git status` and cannot be mistaken for debris, and `rm` is denied — a human can
delete it, and nothing depends on it.

At Recover (ENGINE.md §6.1), a dirty tree means a previous invocation crashed mid-flight — but only
if the dirt is *this run's*. Treat the paths above as clean; anything else untracked or modified is
genuine debris to salvage or revert. Do not `git clean` them.

## Recent Iterations

### Iteration 8 — 2026-09-11 — T-006, the calendar, and a review that died with its process

- **Started at Recover, on the debris of *two* dead invocations.** The tree held T-006
  essentially finished — the domain grid, the screen, three test files, six reference images,
  eleven strings in two locales, the README — stamped 16:41–17:11 on 09-10 and 11:06–11:08 on
  09-11 against a 16:30 checkpoint with `.ai/` untouched. `grep -r MUTATION app/src` came back
  clean, and all four commands were green on the debris before anything was built on it.
- **Did:** verified and salvaged the debris; re-ran the fresh-context review; fixed twelve of its
  sixteen findings; added `millisUntilNextDay()` + a `LaunchedEffect` midnight timer, a selection
  clamp in `refreshToday`, two screenshot cases, and eight tests; reverted the bottom bar to 14sp;
  made `BottomBarElement` internal; ran seven mutations.
- **Learned:**
  - **A review whose findings cannot be read is not evidence, and green commands hide that.** The
    debris was complete *and* green *and* carried `ISSUES.md` entries citing "the fresh-context
    review of T-006, finding 1 / finding 3" — so a review had run and its output died with the
    process. Every other kind of evidence here is reproducible by re-running a command; a review
    is not. Re-running it returned a **Critical the first review had filed rather than fixed**.
    Accepting the dead review's word would have checkpointed a task whose own notes admitted DoD
    criterion 9 was false. → *First thing the next iteration should do*.
  - **An `ISSUES.md` entry that argues its own blocker away is a defect being laundered into a
    decision.** The midnight entry said the fix needed "new machinery… a broadcast receiver is a
    lifecycle concern this screen does not otherwise have", and four lines later gave the
    resolution as "a `LaunchedEffect` in `CalendarFragment.ComposeView()`". Both sentences were
    written by the same invocation. The filing convention is what makes deferral honest, so an
    entry whose *Why it is still open* is refuted by its own *What would resolve it* is worse
    than no entry — it converts "I did not do this" into "this could not be done". **Read the two
    sections against each other before filing.** Applies to both entries rewritten this iteration.
  - **The code assumed the clock only moves forward, and said so in a KDoc.** `refreshToday` left
    the selection alone on principle; flying west or an NTP step-back makes `today` *earlier*, and
    the picked day is then in the future — drawn with a full-strength selection ring on a square
    that is dimmed and refuses taps. `CalendarUiState`'s KDoc asserted it could not happen
    ("Never a day after `today`"), naming a guard that only ran on the tap path. **Fourth
    iteration running that the defect sat in the most confidently documented area**, and the
    sharpest version of it yet: the sentence named the enforcement and was wrong about its
    *reach*. A KDoc should say *enforced here, and here* rather than *impossible*.
  - **A component image cannot catch a wiring bug; only an image of the assembly can.** Of seven
    mutations, exactly one was caught by a picture and nothing else — dropping `selectedDate` on
    its way into the grid, the prior run's defect 2. It failed the full-grid case and **passed
    every day-cell case**, because those call the cell directly with `isSelected = true`. → `PLAN.md`.
  - **A mutation that fails nothing extra is as informative as one that fails.** Three of the
    seven left tests green that a careless reading would have expected to break, each because the
    test declines to claim what its neighbour claims. Predicting the failure list before running
    it turned the exercise from "did something go red" into a check on what each test means. → `PLAN.md`.
  - **A `@Preview` too small for its content records the content *missing*, not clipped.** The
    German bottom-bar case was recorded at the bar's real 70dp height and came back an icon with
    no label at all: `ScreenshotScaffold` insets 12dp on every side, so the tab had 46dp for a
    24dp icon plus a 14sp line. A reference that silently drops the thing it was taken for passes
    validation for ever while defending nothing. Caught by *looking at the first image*, which is
    the only way it could have been. → `knowledge/PROJECT.md`.
  - **The review verified every contrast number in the diff and they were all honest — the
    *threshold* was not.** 2.14:1, 3.95:1, 2.18:1 all recomputed correctly. But 3:1 was cited as
    "the floor for something a user is meant to read", and AA for normal text is 4.5:1. The real
    argument is that disabled controls are exempt; 60% is chosen to be legible rather than to pass
    anything. Getting the number right and the standard wrong is a way of being confidently
    unfalsifiable.
- **Reconciled:** one operational fact → `knowledge/PROJECT.md` (undersized previews record
  content as absent) plus the test count refreshed; **one `ISSUES.md` entry deleted** (the
  midnight staleness, now fixed and tested), one rewritten honestly with a reference image behind
  it (the German label), **one added** (day-cell touch targets at ~43.4dp, accepted with the
  arithmetic showing seven 48dp columns cannot fit a 360dp screen); `PLAN.md` gained the
  assembly-vs-component rule and the failed-nothing-extra rule, and closed T-006. No amendment:
  the plan's shape did not change.
- **Not done, on purpose:** no `basicMarquee` on the bottom-bar label, though the house rule makes
  it the default for single-line overflow — this bar is on every screen for the life of the app,
  so a label scrolling sideways for ever is a permanent distraction; the same deviation and the
  same reason are already written into `HomeNoteList`. The touch-target size is filed rather than
  fixed because reaching 48dp means a grid with no margins and no gutters, which is a design
  decision. Sunday-first stays as T-006 directed, and is flagged for human eyes because `values-de`
  ships and Germany starts weeks on Monday.
- **Checkpoint:** see the `loop(T-006)` commit.

### Iteration 7 — 2026-09-10 — T-005, delete lands, and the debris was hiding a live mutation

- **Started at Recover, and the debris was booby-trapped.** The tree held most of T-005 — the
  repository change, the ViewModel, the sheet, the screenshot case, both test files, strings in two
  locales — all stamped 15:06–15:20 against a 15:00 checkpoint with `.ai/` untouched, so a previous
  invocation had died mid-task. That much matched iterations 3 and 5. What was new:
  `NoteViewModel.delete` contained `delete(note = note.copy(id = 99L))` under a comment reading
  `MUTATION M7`. **The invocation had died between applying a mutation and reverting it**, and the
  surrounding work was coherent enough that building on it without reading it would have shipped a
  delete that removes the wrong row. Reverted, then re-applied deliberately to confirm the test
  catches it (it does — the invocation that applied it never got to see the result). → `PLAN.md`,
  and the first line of *First thing the next iteration should do*.
- **Did:** `getNote` → `Outcome<Note?>` (the A-009 prerequisite); `delete` on the repository, with
  `NoteDao.delete` now returning a row count; `askToDelete`/`dismissDelete`/`delete` +
  `NoteProblem` on the ViewModel; `NoteDeleteConfirmSheet`; the top-bar delete action; a
  `@PreviewTest` and its reference image; eight strings in two locales; 22 new unit tests; README.
- **Learned:**
  - **A crashed invocation can leave the tree *deliberately* wrong.** Every previous Recover
    assumed debris was merely incomplete — a half-written feature, salvageable by reading it. A
    mutation is different in kind: it is a correct-looking edit whose whole purpose is to be wrong,
    and it survives a build and most of a test suite. The cheap fix is a convention rather than a
    rule: **mark every mutation with the literal word `MUTATION` and grep before checkpointing**,
    which turns a subtle recovery hazard into a ten-second check.
  - **The review's Critical was the exact mirror of the defect T-003 was fixed for, and the
    argument against it was already written in the file.** A tap on Save during the 500 ms exit
    animation after a delete re-inserted the deleted row at the top of Home. The `saving` guard's
    KDoc, four functions higher, already explained that the composition keeps taking touches for
    the length of the exit animation — the reasoning was present, correct, and simply not applied
    to the new path. **Third iteration running that the defect sat in the most confidently
    documented area.** The refinement worth carrying: when adding a second path beside a guarded
    one, re-read the guard's *justification*, not just the guard — and treat an asymmetry between
    two sibling paths as a question, not a style choice.
  - **A comment that claims more than the code does is a defect with a delay fuse.** Two Majors
    were exactly this shape: `delete`'s KDoc argued that Room silently matching no row is the
    danger — true of *every* absent id — while the code guarded only the `UNSAVED_ID` sentinel;
    and `askToDelete` had no guard at all while the KDoc beside it described the discipline. Both
    were fixed by making the code as broad as the sentence, not by narrowing the sentence.
  - **`ScreenshotScaffold` applies the theme, so a colour cannot be handed to it.** Fixing a real
    finding — the confirmation was rendered on `background` when the sheet paints `surface` —
    nearly introduced a worse one, because `ground: Color = MaterialTheme.colorScheme.background`
    evaluates in the *caller's* composition, outside the theme, against Material's **light**
    baseline. That reads as a no-op and would have re-recorded every reference in the project on
    white. The parameter is an enum resolved inside the theme instead. → `knowledge/PROJECT.md`.
  - **The engine is a single `claude -p` invocation**, found by reading `.loop/run.ps1` while a
    background reviewer was still running and ending the turn looked like a reasonable way to wait.
    It is not: the turn ending *is* the process exiting, and the Runtime would have counted a crash
    and thrown the iteration away. It is also the likeliest explanation for the two earlier deaths
    in this run. → `knowledge/PROJECT.md`.
  - **A failing Gradle task aborts the ones after it, and the stale XML reads as a pass.** After
    the review fixes, `validateDebugScreenshotTest` failed (expected — the ground colour had
    changed on purpose) and `testDebugUnitTest` never ran, but its result files were still there
    from the previous invocation reporting `failures="0"`. The tell was that the counts had not
    gone *up* after two tests were added. → `knowledge/PROJECT.md`.
  - **The reviewer found three tests that could not fail for the reason they stated** — one
    asserting a field's declared default, one never checking the pre-state it cleared, one
    returning at a different guard than the one its comment named. Iteration 6 learned this from a
    reader too. It is now clear that **mutation testing and careful reading catch different
    species**: mutation finds assertions the code can't break, reading finds assertions that never
    described the code. Running both is not redundancy.
- **Reconciled:** five operational facts → `knowledge/PROJECT.md` (the `Outcome<T?>` three-answer
  convention and its deliberate divergence from `.claude/repository-layer.md`; the row-count rule
  for writes that may affect nothing; the `-p` invocation model; the aborted-task/stale-XML trap;
  the `ScreenshotScaffold` ground trap), plus the test count. **One `ISSUES.md` entry deleted** —
  the failed-read defect, resolved, which discharges A-009 — one narrowed (the Note *editor* is
  still unpinned; the confirmation now is not), and **two added** (a sheet dismissed by its own
  button does not animate out; the failed-open exit is a one-way door). `PLAN.md`'s mutation rule
  gained the marker convention and its task graph closed T-005. No new amendment: the plan's shape
  did not change.
- **Not done, on purpose:** `CoreBottomSheet`'s dismissal animation stays as it is — it is a
  shared primitive with two other callers and no reference image to catch a regression, so it is
  its own change, not a passenger on this one. The `Gone`/`Unreadable` one-way door stays too: the
  real fix needs the Fragment to observe the pop, which nothing in this app does yet. Both filed
  rather than deferred silently. No `@PreviewTest` for `NoteEditor` — still blocked on the same
  host-locale lock as the populated Home row.
- **Checkpoint:** see the `loop(T-005)` commit.

### Iteration 6 — 2026-09-10 — T-004, the loop closes, and a debounce that made rows dead

- **Clean start.** The working tree held only the paths listed above as expected, so Recover was a
  no-op and Select took T-004 as the only unblocked task.
- **Did:** `HomeNoteList` gained a required `onOpenNote` and its private `NoteRow` became clickable
  (clip → border → background → clickable → padding, with an `open_note` label and a bounded
  ripple); `HomeFragment` routes a row tap through the existing `toNote` action with
  `argumentsFor(date = note.date, noteId = note.id)`; one string in two locales; three new unit
  tests; the README feature table split so "open and edit" is marked built and delete is not.
  Nothing in the editor, the ViewModel or the store needed writing — A-007 had already moved it all
  into T-003, and this iteration confirmed that rather than rebuilding it.
- **Learned:**
  - **A shared navigation debounce is the wrong instrument for "this control must not navigate
    twice", and the review caught it.** `NavigationUtil.canNavigate()` looked like the obvious reuse
    — `CoreBottomBar`'s centre button uses it, the comment justifying it was already written, and it
    is *correct* for the button it was written for. But it is one process-wide `lastNavTime`, and
    `BottomBarElement` arms it before checking whether the tab tap goes anywhere, so tapping "Home"
    while already on Home would have left every note row showing a ripple and doing nothing for the
    next 800 ms. The row existed precisely so taps would stop being ignored. **Asking the graph
    where you are** (`currentDestination?.id == R.id.homeFragment`) has no dead window: `navigate`
    moves `currentDestination` synchronously, so the second tap of a double tap is refused and a
    deliberate tap a moment later is not. → `knowledge/PROJECT.md`.
  - **One mutation per test is not enough — a test can fail for one reason while another of its
    assertions is dead.** The new reordering test asserted that an edit does not re-file a note onto
    another day, against a fixture the helper had already dated to the clock's today. It could not
    fail, and it survived three implementation mutations before a *reader* found it. The fix was one
    line in the fixture; the lesson is to mutate toward each assertion a test claims to carry, and
    to treat a fixture that already equals the expected value as the smell it is. This refines
    iteration 5's rule rather than replacing it. → `PLAN.md`.
  - **Making a dead branch reachable is a change worth reviewing as one.** Nothing in
    `getNote`/`openNote` was touched this iteration, and the diff still made a real defect live:
    until Home rows became clickable, every caller left `noteId` at its default, so `getNote` had no
    in-app caller and the "note not found → start a new note" fall-through was unreachable code.
    Now a failed read opens a blank-but-correctly-dated editor whose save **inserts**, leaving two
    notes for that day. Filed and made a prerequisite of T-005 (amendment **A-009**) rather than
    fixed here: the clean fix changes `getNote`'s return type and overturns a deliberate, tested,
    documented T-003 decision, from inside a task scoped to "the route in".
  - **A promise written into a test file is a promise that comes due.** `HomeScreenshotTest.kt`
    said the row's `Untitled note` fallback "arrives with the rows, when they become clickable".
    The rows became clickable here; the fallback did not arrive, for the same locale reason as
    before — and `@Preview(locale = "en")` turns out to pin the row's *language* but not its
    *format*, because `ofLocalizedDate` follows the JDK's CLDR data and this project pins no
    toolchain. The sentence is now the honest version. Better to write "still unpinned, and here is
    what it would cost" than to name a task and let it come and go.
  - **Two of the three Majors were about what the diff did to things it did not touch** — the
    shared debounce, and the reachability of a dead branch. Both twice removed from the lines
    changed. The confident-comment pattern from iteration 5 held again in a new shape: the debounce
    comment was confident *and* copied from a place where it was true.
- **Reconciled:** one operational fact → `knowledge/PROJECT.md` (the `canNavigate()` clock, with
  why `launchSingleTop` is not the alternative here) and the test count refreshed; one new
  `knowledge/ISSUES.md` entry (the failed-read insert) and the editor-exits entry amended, because
  losing an *edit* is worse in kind than losing a new note — Home then looks exactly like a save
  that did nothing; amendment **A-009**; `PLAN.md`'s mutation rule sharpened and its task graph
  updated; T-005 given the prerequisite and a new acceptance criterion; T-004 closed with its
  evidence, its review and its four human-inspection items.
- **Not done, on purpose:** the back-affordance decision stays as it is — a discard confirmation and
  an autosave are different products with different failure modes and no criterion asks for either,
  so choosing is the human's call; the reasoning is written into `.ai/TASKS/T-004.md` as that task
  required, and the `ISSUES.md` entry now covers the edit case. No populated-row reference image
  (the CLDR lock above). `NoteDao.upsert`'s discarded return id stays discarded — this task never
  needed the ViewModel to adopt a note it just created.
- **Checkpoint:** see the `loop(T-004)` commit.

## Iteration Index

<!-- One line per iteration older than the three above. The SHA is the checkpoint commit whose
     STATE.md still holds that iteration's full entry:  git show <sha>:.ai/STATE.md -->

| Iteration | Checkpoint | What happened |
|---|---|---|
| 5 | `e50bcb1` | T-003 — the Note editor, `save`/`getNote` with an injected `Clock`, the bottom bar's centre button wired on every screen. **Started at Recover** on a coherent partial edit to `Note.kt`, verified before being built on. Five lessons still live: **a domain rule belongs to the task that creates its first write path, not the task that creates its first tempting caller** — the future-date rule moved from T-008 to here the moment `save` existed and accepted any date it was handed (amendment **A-006**), which is why **DoD criterion 10 was satisfied four tasks early**; the same reasoning shrank T-004 (**A-007**); **a test is not evidence until it has been made to fail**, first proved here with three mutations failing exactly 3, 1 and 1 tests; **`android.util.Log` on an error path made that path untestable** against the stub `android.jar`, fixed by `isReturnDefaultValues = true` (**A-008**) at the cost that a `Bundle`/`Intent`/`Uri` can never be unit-tested here; and **a ViewModel *is* testable on this toolchain** via `Dispatchers.setMain` + an unconfined dispatcher, which nothing had tried. Both review Criticals sat in the most confidently documented code: the editor was unusable under the keyboard (nothing in `app/src/main` handled IME insets) while `NoteEditor`'s KDoc claimed to prevent exactly that, and a double tap on Save wrote the note twice. Also: **a README can invent a domain rule**, and doing so routes around `DOMAIN.md` being human-owned. |
| 4 | `627d25a` | T-002 — the `notes` table, the store above it, and Home repointed off the demo post list. Clean start. Four lessons, all still live: **a Room migration cannot be tested here but can be made right by construction** — copy Room's own `CREATE TABLE` out of the generated `AppDatabase_Impl` rather than composing one by eye, which had already produced a `DEFAULT ''` the entity does not declare (amendment A-005); **a fake DAO only proves something if the fake refuses to help** — `FakeNoteDao` returns rows in deliberately jumbled construction order, which is what moves the "newest first" promise off a SQL string and onto the repository; **`git reset`/`git restore` are denied and `git rm --cached` is the unstage**, found by `git add -A app/src` sweeping up the three orphan PNGs; and **a locale-correct date is not `Locale.getDefault()`** but `LocalConfiguration.current.locales[0]`, because this app has its own language picker. The review's two Majors were **both in `knowledge/`, not in the code** — a stale `AppDatabase version = 2` line that would have made the next task bump to 3 twice, and an `ISSUES.md` entry asking for two string keys this task had just deleted: the reconciliation step is part of the work, and a stale line in a file read every iteration is a defect with a delay fuse. |
| 3 | `2114e47` | T-010 — the host-side screenshot harness imported from `loop/todo-calendar-screens`, six references recorded. **Started at Recover:** modified build files plus untracked `app/src/screenshotTest*/` were sitting in the tree, stamped after the `loop(T-001)` tip with `.ai/` untouched, so an invocation had done most of T-010 and died before checkpointing. Verified before building on it (ENGINE.md §6.1) — `ScreenshotScaffold.kt` diffed byte-identical against the source branch, all four Gradle tasks green, the screenshot XML reporting 4 real cases rather than a vacuous zero — then salvaged. Four traps earned, all now in `knowledge/PROJECT.md`: the harness runs host-side in ~15s with **no emulator**; **`updateDebugScreenshotTest` orphans references rather than replacing them** (the filename hashes the preview's *parameters*, and validation ignores strays and stays green — two update runs left three orphans); **`rm` is denied**, found while trying to clean them, and not routed around; and **a swatch can be wrong in a way only rendering shows** (`inversePrimary` paired a fill with an on-colour Material never puts together, 3.94:1, under AA). Fresh-context review: 16 findings, 0 Critical; **the real defect was a sentence** — the test file's header claimed these images make DoD criteria 1 *and 2* machine-defendable, and criterion 2 cannot be, because `Color.White` and `colorScheme.onBackground` are the same pixels (proved, not argued: 65 hardcoded literals in the tree and every case passing at `diffPercent 0.0`). Left standing it would have retired criterion 2 from being checked at all → amendment **A-004**. Also learned: **writing an image down as "approved" is not approving it** — the engine looked at all six and they are correct, which is not the human approval the criterion asks for. |
| 2 | `fe607a9` | T-001 — the fixed dark theme, the blue primary, the app rename, the README. **The first build this repository ever ran**, and it was run *before* touching a file, which is the only reason the next fact is legible: `lintDebug` was **already failing on the pristine tree** with four `MissingTranslation` errors, so a pre-existing red would otherwise have read as this run's regression (→ amendment **A-003**: adding a string here is a two-file operation). Four more traps earned, all now in `knowledge/PROJECT.md`: a **piped Gradle command returns the pipe's exit code**; **never run Gradle in the background while editing**; `.claude/figma-design-system.md` **permits hardcoded colour where DoD criterion 2 forbids it** (resolved by source-of-truth order, ENGINE.md §3); and **compute a dark theme's contrast ratios rather than eyeballing swatches** — which caught `outline` at 2.19:1, under the 3:1 floor, and would have given T-006's calendar grid invisible borders. `DarkColorSchemeTest` **failed on its first run**, which is why criterion 1 is a test rather than a KDoc claim. Fresh-context review: 16 findings, 1 Critical (the new README named **Retrofit**; this app uses **Ktor** — criterion 14 made false by the very file added to satisfy it) + 4 Major, all fixed; 3 filed to `ISSUES.md`. |
| 1 | `9b25307` (+ `6cfdd4e`) | Bootstrap. Read the PRD, the rule files and the whole codebase; created `knowledge/`, the Loop Branch and `.ai/` with nine tasks. No implementation, by design. `./gradlew --version` was refused, so every toolchain command was recorded unverified and proposed as a capability. Escalation D-001 was raised **and answered mid-iteration**: the DoD was approved with A5 overruled to title+body, the toolchain was granted, and Q4 refuted my conclusion that this repository had no host-side test setup — a working screenshot harness was sitting on the sibling branch `loop/todo-calendar-screens` the whole time, reachable under baseline capabilities. That produced T-010 and amendments A-001 and A-002. Reported `CONTINUE`. |

## Escalation Index

<!-- One line per consumed Escalation Request, newest first. A pending escalation is not indexed —
     it is still in .ai/ESCALATION.md, unanswered. -->

| ID | Checkpoint | Question → decision |
|---|---|---|
| D-001 | `9b25307` | Approve DoD + grant toolchain + choose UI-evidence strategy → DoD approved with A5 overruled to title+body; C1 toolchain granted as proposed (narrow rules) plus screenshot and `MSYS_NO_PATHCONV` entries; test deps granted; Q4 Option B **plus** import the existing harness from `loop/todo-calendar-screens`; `DOMAIN.md` created with rules 1+2. |

`9b25307` holds the request **as issued**. The decision text is in `.ai/ESCALATION.md` at iteration
1's second checkpoint (marked CONSUMED) and reproduced in that commit's message — read it back with
`MSYS_NO_PATHCONV=1 git show <sha>:.ai/ESCALATION.md`.
