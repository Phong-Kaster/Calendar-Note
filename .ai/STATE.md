# STATE

> Machine-owned execution memory. Updated every iteration; committed atomically with the code it describes.
> A fresh engine invocation must be able to resume from this file plus the repository alone.
>
> **This file is read in full at Orient, every iteration, so it must not grow with the run.** Keep
> only the last three iterations verbatim under *Recent Iterations*; older ones become one-line rows
> in *Iteration Index*, and each consumed escalation becomes one line in *Escalation Index*. Both
> rows carry the checkpoint SHA that still holds the full text: `git show <sha>:.ai/STATE.md`.
>
> **This file is around 50 KB and the discipline above has not been applied for several
> iterations.** T-009 is the cheapest place to fix it: fold everything older than the last three
> iterations into one-line index rows. A file read in full at every Orient that grows with the run
> taxes every iteration after it.

## Current

- **Phase:** executing
- **Loop Branch:** `loop/calendar-note-app`
- **Next task:** **T-009** — README, package tree, strings audit, and the human-inspection
  checklist. **It is the last task**, and both its dependencies (T-005, T-008) are complete, so
  after it this run is a DONE-candidate. **Every DoD criterion now has its implementation
  landed**; what T-009 does is (a) verify criterion 14 against the final tree, and (b) collect the
  human-inspection items every task has been accumulating into one checklist a person can work
  through.

  **The (I) items are the substance of T-009, and they are scattered across nine task files.**
  Each `.ai/TASKS/T-0*.md` ends with a "human inspection needed" list — 3, 5, 3, 5, 5, 8, 5 and 5
  items respectively. Collect them; do not re-derive them. Several are *reference images awaiting
  first approval*, which is a different ask from a device walkthrough, and the checklist should
  keep those two apart because the first is "look at this picture once" and the second is "install
  the app".

  **Then the DONE-candidate rule (ENGINE.md §2, invariant 8).** T-009 writes documentation and a
  checklist, so the *next* invocation after it can be the one that declares completion having
  written none of the implementation. Do not declare `DONE` in the same iteration that finishes
  T-009.

  **Two things T-009 should check rather than assume:**

  1. **The README package tree against the real tree.** Iteration 10 added
     `domain/model/FutureDateRefusedException.kt` and the review caught the README not listing it
     — criterion 14 is a per-file claim, so count the files.
  2. **The strings audit is a two-file job.** `res/values/strings.xml` has 61 keys and
     `res/values-de/strings.xml` has 60; the difference is `app_name`, deliberately untranslated.
     Criterion 14 also asks that every string added by this run is named for its own words — the
     one pre-existing key that is not is already filed in `knowledge/ISSUES.md`, so do not
     re-report it.
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
  satisfied.** Rule 2's *"a per-day list on the Calendar screen follows the same ordering"* clause
  landed in iteration 9 and is mutation-checked at both layers (store and screen). Neither rule is
  finished being *defended*: any new write path must reach the same `save`, any new *list* must
  carry the ordering, and any test that carries either rule should be mutation-checked rather than
  trusted — per assertion, not per test (iteration 6). **Rule 1 gained a second caller in
  iteration 10 and was re-verified from it**, including that the boundary is inclusive of today
  (six tests fail if it moves) and that the refusal now travels as a *typed* value
  (`FutureDateRefusedException` inside `Outcome.Error.throwable`) so a screen can tell "I will not
  store this" from "I could not store this" without reading message text.

### First thing the next iteration should do

**`grep -r MUTATION app/src`, before anything else, if the working tree is dirty.** Iteration 10
ran eleven mutations and the grep is what licensed that: it came back empty before the checkpoint. Iteration 9
started clean and the check cost ten seconds; iteration 7 started dirty and the check is the only
reason that iteration did not build on a deliberately-wrong `NoteViewModel.delete`. Iteration 7
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
  enum (new in iteration 7);
- **a stray reference image is told from a live one by the validation report, not by the
  tracked/untracked split** — one `grep` on
  `app/build/reports/screenshotTest/preview/debug/…<Class>Kt.html` lists the hashes actually used.
  Iteration 10's review reasoned from the git index instead and reported a live image as dead (new
  in iteration 10).

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
| T-007 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 64 warnings`, unchanged) + `testDebugUnitTest --rerun-tasks` (**129 tests, 0 failures**) + `validateDebugScreenshotTest` (**19 cases, 0 failures**) all green, all re-run after the review fixes and after every mutation was reverted. **Nine mutations, eight matching their prediction exactly.** M8 is the one to remember: deleting the empty-state branch — the prior run's defect 3, restored — leaves **all 129 unit tests green** and fails only two pictures, so criterion 11's empty-state clause has no other evidence. Fresh-context review: 7 findings — 0 Critical, **1 Major** (the heading named one day while the rows below were another day's, in the window between the tap and the query; fixed by making the mismatch unrepresentable), 6 Minor; **all seven fixed, none filed**. One of them was fixed *and refuted*: the review's German-truncation arithmetic was wrong, and the reference image is what says so. Full record in `.ai/TASKS/T-007.md`. **5 items await human eyes, three of them new reference images** — see that file. |
| T-008 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 64 warnings`, unchanged) + `testDebugUnitTest --rerun-tasks` (**138 tests, 0 failures**) + `validateDebugScreenshotTest` (**20 cases, 0 failures**) all green, all re-run after the review fixes and after every mutation was reverted. **Eleven mutations, ten matching their prediction exactly.** Two to remember: **M4** — untagging the refusal in the real `save` leaves **137 of 138 tests green** while every refusal in the app reverts to "please try again", because every ViewModel test builds its own tagged outcome through the fake; and **M10** — drawing the add action with no day picked leaves **all 138 unit tests green** and fails only pictures. M9 shows the future-date boundary is defended six ways, not one. Fresh-context review: 12 findings — 0 Critical, 6 Major, 6 Minor; **9 fixed, 2 accepted with reasons, 1 refuted** (a live reference image reported as a stray — see § Toolchain). The Major worth knowing: the new add action could push two blank editors onto the back stack, so all three navigation routes off this screen now go through one guarded door. **DoD criterion 11 is complete and criterion 10 is re-verified from its second caller.** Full record in `.ai/TASKS/T-008.md`. **5 items await human eyes, two of them new: the refusal message itself (no test in this project reaches `NoteFragment`) and whether the centre button should mean the picked day here (A-010).** |
| T-009 | pending — **the last task**, both dependencies now complete | — |

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

Iteration 9 added **one** new orphan, the same trap on the other axis:
`CalendarScreenshotTestKt/DayNotesGermanHeading_…_c2297230_0.png`. The German day-notes case was
first recorded at 312 × 120 to size the heading, the empty-state message underneath it was
measured to nothing and vanished, and the preview was re-recorded at 312 × 200 — which hashes to
a different filename, so `updateDebugScreenshotTest` wrote `…_fa3cecc9_0.png` and left the first
behind. Only `fa3cecc9` is committed. A human can delete `c2297230` with the other four.

Iteration 10 added **three** new orphans, and these are the first that were *tracked* before being
orphaned — so they needed `git rm --cached` as well as listing:
`DayNotesEmptyDay_…_aec4ce2e_0.png`, `DayNotesGermanHeading_…_fa3cecc9_0.png`,
`DayNotesWithNotes_…_22d24b4b_0.png`, all under `CalendarScreenshotTestKt/`. Three day-notes
previews grew to fit T-008's add-note row (180→260, 200→280, 320→400), and a reference filename
hashes the preview's parameters. The four live images are committed. **The eight strays are now
listed in one place — `knowledge/ISSUES.md` § the stray entry — rather than split between there and
here**, because a human deleting them wants one list, and because the tracked/untracked split turned
out to be a *misleading* way to tell a stray from a reference: see the refuted review finding in
`.ai/TASKS/T-008.md` and the one-command test in `knowledge/PROJECT.md`.

Iteration 6 added **no** new orphans and left nothing uncommitted. It did leave one ignored file:
the review subagent wrote **`build-t004-review.log`** at the repository root. It is git-ignored, so
it never appears in `git status` and cannot be mistaken for debris, and `rm` is denied — a human can
delete it, and nothing depends on it.

At Recover (ENGINE.md §6.1), a dirty tree means a previous invocation crashed mid-flight — but only
if the dirt is *this run's*. Treat the paths above as clean; anything else untracked or modified is
genuine debris to salvage or revert. Do not `git clean` them.

## Recent Iterations

### Iteration 10 — 2026-09-11 — T-008, a note on the picked day, and a refusal that says why

**Did:** T-008, the last feature task. The Calendar's day section gained an add-note action dated
to the **selected** day (DoD criterion 11), and a refused save stopped looking like a failed one.
Eleven mutations, a fresh-context review, and all four commands green: 138 unit tests, 20
screenshot cases, `0 errors, 64 warnings`, `assembleDebug` successful.

**The three decisions, in full in `.ai/TASKS/T-008.md` § Decisions:**

1. **The centre bottom-bar button now means the picked day on this screen** and today everywhere
   else (amendment **A-010**). Two add affordances on one screen filing notes on two different days
   is a failure nobody can see at the moment it happens. **A11 was read before implementing and it
   is Home-scoped** — had it not been, this would have been a Tier-3 proposal, not an amendment.
2. **A picked day the clock has overtaken is handed to the store as it is**, not swapped for
   today. Swapping files a note on a day nobody chose; re-checking the rule in the UI puts
   `DOMAIN.md` rule 1 in two places. So the store refuses it and the editor names the day.
3. **The refusal is tagged by type, not by message** — a never-thrown `FutureDateRefusedException`
   inside `Outcome.Error.throwable`. `Outcome.Success` carrying a refusal would have read better
   against `getNote`'s existing three-answer shape, but the DoD's evidence row for criterion 10
   says "returns an error outcome", and the DoD is immutable.

**Learned, and all three are in `.ai/PLAN.md` or `knowledge/PROJECT.md` now:**

- **A fake can express a contract without verifying it.** Mutation M4 removed the tag from the real
  `NoteRepositoryImpl.save`: **137 of 138 tests stayed green** while every refusal in the running
  app reverted to "something went wrong, please try again", because every `NoteViewModel` refusal
  test builds its own tagged outcome through the fake. One repository test was the whole join. Where
  a fake hands back what the implementation is supposed to construct, test that it constructs it.
- **`Fragment` and `@Composable` are both "no test can see this".** Criterion 11's decision was
  heading for a navigation lambda; moved to `CalendarViewModel.dateForNewNote()` it is four tests.
  Ask which class a decision is written in *before* writing it, not after.
- **A live reference image is identified by the validation report, not by the git index.** The
  review reported a live tracked PNG as a stray — which would have meant criterion 13 passing here
  and failing on a clean clone — and one `grep` of
  `app/build/reports/screenshotTest/preview/debug/…<Class>Kt.html` refuted it in ten seconds.
  **That is the second consecutive iteration where a confident review finding about images was
  wrong and the tooling settled it** (iteration 9's was German truncation arithmetic).

**Also:** the review's sharpest finding was a *dead assertion* in the engine's own new tests — both
refusal tests opened the editor on the same day the refusal named, so the ViewModel's documented
choice between the two sources had no evidence. Fixed, then verified by M11, which fails now and
would have passed before. Eleven mutations were not enough to find it; a reader was.

**Reconciled:** one operational fact → `knowledge/PROJECT.md` (the validation report names the live
reference image, so a stray is identified in one `grep`), plus the test count. **One `ISSUES.md`
entry halved** — the editor-exits entry lost part 2, which this task resolved, and kept part 1
(text lost on the way out), which no criterion asks for. **One widened**: the stray-image entry now
lists eight files rather than three, including the first three that were *tracked* before being
orphaned. `PLAN.md` gained two rules — a fake can express a contract without verifying it, and
`Fragment`/`@Composable` are both "no test can see this" — and its task graph closed T-008.
Amendment **A-010** logged for the bottom-bar decision. This iteration also folded iteration 7 out
of *Recent Iterations* into the index, which is what this file's own header asks for.

**Not done, on purpose:** the add affordance is not re-checked against the real clock inside the
component — that would put `DOMAIN.md` rule 1 above the repository, and the refusal chain exists
precisely so it does not have to be. No `@PreviewTest` of the *whole* Calendar screen, so the
"add action reaches the screen" wiring is still defended only at component level; iteration 8's
lesson says an assembly image is what catches a wiring bug, and that is a T-009-sized job rather
than a passenger here. `STATE.md` is still oversized — one iteration was folded, not the four that
should be.

**Checkpoint:** see the `loop(T-008)` commit.

### Iteration 9 — 2026-09-11 — T-007, the day's notes, and a picture that overruled the review

- **Clean start, and the first one in four iterations.** The working tree held only the paths
  listed above as expected, `grep -r MUTATION app/src` came back empty, and Select took T-007 as
  the only unblocked task. Recover was a no-op.
- **Did:** `NoteRepository.notesForDateFlow(date)` over the `observeByDate` query T-002 left
  waiting; `CalendarUiState.loadedDay` / `loadedDayNotes` with a derived `notesForSelectedDay`;
  `collectNotesForSelectedDay()` (`flatMapLatest` over the distinct selection);
  `CalendarDayNotes`; `NoteSummaryRow` extracted out of `HomeNoteList` and shared; the row-tap
  route with Home's destination guard; two strings in two locales; 16 unit tests; three reference
  images; README. Then the review's seven findings, then nine mutations.
- **Learned:**
  - **Record the picture before accepting a review's arithmetic about what fits.** The review
    argued the day heading would truncate in German, with the character count and the font size
    to back it, and recommended wrapping. Rendering it at 288dp — a 320dp screen, the narrowest
    this app supports — showed the German date fitting on one line with room to spare. The wrap
    stayed as insurance and the *stated reason* was struck from the KDoc. This is iteration 8's
    lesson inverted: there every contrast number was right and the threshold cited was wrong;
    here the arithmetic itself was wrong. **"Does it fit", "does it contrast", "is it legible"
    are questions this repository answers in fifteen seconds, and a confident paragraph is not
    an answer.** → `PLAN.md`.
  - **Two state fields written by two different events are one field, or they are a bug.** The
    review's only Major: a tap sets `selectedDate` at once while the store's answer arrives a
    query later, so the screen had a window where the heading named one day and the rows below
    were another day's. Every test passed, because a synchronous fake on an unconfined dispatcher
    closes the window — the race is unreachable in the suite and perfectly reachable on a device.
    The fix that generalises is not "gate the write" but **store the answer with the question it
    answers** and derive what the screen reads, which makes the mismatch unrepresentable rather
    than unlikely. → `PLAN.md`.
  - **The empty state is defended by a picture and by nothing else, and a mutation proved it.**
    Deleting the empty-state branch — restoring the prior run's third defect exactly — left all
    **129 unit tests green** and failed only two reference images. Criterion 11's empty-state
    clause has no other evidence, which means re-baselining those two images with
    `updateDebugScreenshotTest` would silently retire it. Written into `.ai/TASKS/T-007.md` where
    the next reader of that criterion will find it.
  - **A fake that filters the way the code filters proves nothing about *which query ran*.** The
    review showed that `notesForDateFlow` could have sifted `observeAll()` in Kotlin and passed
    every new test, because both routes return the same list — the acceptance criterion names the
    per-day query and nothing checked that it was used. Fixed by making the fake *record* what it
    was asked for. M3 confirms one test now catches it and no other does. The iteration-4 rule
    was "a fake only proves something if it refuses to help"; the refinement is that **a fake can
    also be asked to testify** about what it was called with.
  - **The undersized-preview trap fired again, on the axis nobody was watching.** The German case
    was recorded at 120dp to size a *heading*, and the heading came out perfect — while the
    empty-state message below it was measured to nothing and vanished. Everything the author was
    looking at was right; the casualty was the part they were not thinking about. Add up the
    whole component, not the part the case is for. → `knowledge/PROJECT.md`, as a second entry
    under the trap iteration 8 earned.
- **Reconciled:** one operational fact → `knowledge/PROJECT.md` (the undersized-preview trap
  recurring on the other axis) plus the test count refreshed; **one new `knowledge/ISSUES.md`
  entry** (an untitled note prints its first line twice — pre-existing since T-002, now visible in
  a committed reference image, and a design call rather than a bug to fix inside this task); two
  rules added to `PLAN.md` and T-007 closed in its task graph. No amendment: the plan's shape did
  not change, and T-008 is still the task the plan said it was.
- **Not done, on purpose:** no `SELECT DISTINCT date` query for the has-notes markers, though the
  review is right that `notesFlow` reads every note to build a set of days — the KDoc that
  overclaimed was corrected instead, and the query is named as a thing to do when a table large
  enough to notice exists. The untitled-note duplication is filed rather than fixed: suppressing
  the body would also hide lines 2–3 of a *long* untitled note, and which reads better is not the
  engine's call. The day section is a plain `Column` rather than a `LazyColumn`, because the task
  asks for the whole page to scroll as one and a lazy list inside a `verticalScroll` throws at
  measure time; the trade is written into the component's KDoc.
- **Checkpoint:** see the `loop(T-007)` commit.

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

### Iteration 7 — folded into the Iteration Index below

Its full entry is in the `loop(T-005)` checkpoint: `git show 124ce87:.ai/STATE.md`.

<!-- Folded in iteration 10 to honour this file's own three-iterations-verbatim rule. The lessons
     that are still live were already copied into knowledge/PROJECT.md and .ai/PLAN.md when they
     were learned; the index row below carries the summary. -->

## Iteration Index

<!-- One line per iteration older than the three above. The SHA is the checkpoint commit whose
     STATE.md still holds that iteration's full entry:  git show <sha>:.ai/STATE.md -->

| Iteration | Checkpoint | What happened |
|---|---|---|
| 7 | `124ce87` | T-005 — delete behind a confirmation, and `getNote` → `Outcome<Note?>` (discharging A-009). **Started at Recover on booby-trapped debris:** `NoteViewModel.delete` held `delete(note = note.copy(id = 99L))` under a comment reading `MUTATION M7` — a previous invocation had died between applying a mutation and reverting it, and the surrounding work was coherent enough that building on it would have shipped a delete that removes the wrong row. That is the origin of the `MUTATION`-marker convention and of the grep at the top of this file. Six lessons still live, all copied where they are used: **a crashed invocation can leave the tree *deliberately* wrong**; **the review's Critical was the mirror of the defect T-003 was fixed for and the argument against it was already in the file four functions higher** (a tap on Save during the 500 ms exit animation put the deleted note back) — when adding a second path beside a guarded one, re-read the guard's *justification*, not just the guard; **a comment that claims more than the code does is a defect with a delay fuse** (two Majors of exactly that shape); **`ScreenshotScaffold` applies the theme, so a `Color` parameter resolves against Material's light baseline**; **the engine is a single `claude -p` invocation**, so a background subagent must be waited on inside the turn; **a failing Gradle task aborts the ones after it and the stale XML reads as a pass** — the tell is a test count that did not go up. Also: mutation testing and careful reading catch different species of dead assertion, and running both is not redundancy. |
| 6 | `d68d2ac` | T-004 — Home rows became clickable and the open-edit-resort loop closed. Clean start. Four lessons still live: **a shared navigation debounce is the wrong instrument for "this control must not navigate twice"** — `NavigationUtil.canNavigate()` is one process-wide 800 ms clock and `BottomBarElement` arms it even for a tab tap that goes nowhere, so using it on a list row leaves every row dead for 800 ms after any tab tap; **ask the graph where you are** (`currentDestination?.id == R.id.homeFragment`) instead, which has no dead window because `navigate` moves `currentDestination` synchronously. **One mutation per test is not enough** — a new test asserted that an edit does not re-file a note onto another day, against a fixture already dated to the clock's today, so the assertion could not fail and survived three mutations before a *reader* found it; mutate toward each assertion a test claims to carry. **Making a dead branch reachable is a change worth reviewing as one** — nothing in `getNote` was touched, and clickable rows still made a real defect live (a failed read opened a blank editor whose save *inserted* a duplicate), filed as amendment **A-009** and made a prerequisite of T-005. And **a promise written into a test file comes due**: `@Preview(locale = "en")` pins a row's *language* but not its *format*, because `ofLocalizedDate` follows the JDK's CLDR data and this project pins no toolchain. Two of the three Majors were about what the diff did to things it did not touch. |
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
