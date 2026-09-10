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
- **Next task:** **T-004** — tapping a row on Home opens that note for editing and the list
  re-sorts. Its dependency (T-003) is complete. Nothing is pending. **It is much smaller than its
  original description**: amendment A-007 moved the load-and-re-save machinery into T-003, where it
  is implemented and tested, so what remains is the *route in* (the row click, down through
  `HomeNoteList`'s private `NoteRow`), the re-sort evidence on the list, and the one genuinely
  uncovered case — a title edited to blank. **Read `.ai/TASKS/T-003.md` before starting T-004 or
  T-008**; both had work removed and both task files say what is left.
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
  T-008 because T-003 created the first write path). **DoD criterion 10 is satisfied.** Neither is
  finished being *defended*: any new write path must reach the same `save`, and any test that
  carries either rule should be mutation-checked rather than trusted.

### First thing the next iteration should do

Read `knowledge/PROJECT.md` § Toolchain before running anything. It carries ten traps, every one of
them earned by a real failure or a denied command:

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
  `Modifier.imePadding()` before its `verticalScroll` (new in iteration 5).

## Progress

| Task | Status | Evidence |
|---|---|---|
| T-001 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 55 warnings`) + `testDebugUnitTest` (6 tests, 0 failures) all green; no `lightColorScheme`/`isSystemInDarkTheme`/dynamic-colour hits in `app/src/main`; 36/36 Material roles assigned **and now guarded by `DarkColorSchemeTest`**; README tree verified against all 82 source files. Fresh-context review: 16 findings, 1 Critical + 4 Major all fixed, 3 filed to `ISSUES.md`. Full record in `.ai/TASKS/T-001.md`. **3 perceptual items await human eyes** (see that file). |
| T-010 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 59 warnings`) + `testDebugUnitTest --rerun-tasks` (6 tests, 0 failures) + `validateDebugScreenshotTest` (**6 cases, 0 failures**) all green. `ScreenshotScaffold.kt` diffed byte-identical against `loop/todo-calendar-screens`. Six reference PNGs committed. Fresh-context review: 16 findings, 0 Critical; 3 Major + 5 Minor fixed, the rest filed. Full record in `.ai/TASKS/T-010.md`. **Human approval of the six images is still open** — see that file. |
| T-002 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 59 warnings`) + `testDebugUnitTest --rerun-tasks` (**17 tests, 0 failures**) + `validateDebugScreenshotTest` (**7 cases, 0 failures**) all green, all re-run after the review fixes. `MIGRATION_2_3` is a verbatim copy of Room's generated `createAllTables` statement. Fresh-context review: 0 Critical, 2 Major (both knowledge reconciliation, both fixed), 7 Minor (6 fixed, 3 accepted with reasons). Full record in `.ai/TASKS/T-002.md`. **3 items await human eyes** — see that file. |
| T-003 | ✅ **complete** | `assembleDebug` + `lintDebug` (`0 errors, 60 warnings`) + `testDebugUnitTest --rerun-tasks` (**38 tests, 0 failures**) + `validateDebugScreenshotTest` (**7 cases, 0 failures**) all green, all re-run after the review fixes. **The new tests were mutation-checked**: three separate mutations to the implementation failed exactly the five expected tests. **DoD criterion 10 is satisfied here** (A-006). Fresh-context review: 2 Critical (both fixed — the editor was unusable with the keyboard open; a double tap on Save wrote two notes), 2 Major (both fixed), 4 Minor (2 filed, 2 accepted with reasons). Full record in `.ai/TASKS/T-003.md`. **5 items await human eyes** — see that file. |
| T-004 | pending — **reduced by A-007** | — |
| T-005 | pending | — |
| T-006 | pending | — |
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

At Recover (ENGINE.md §6.1), a dirty tree means a previous invocation crashed mid-flight — but only
if the dirt is *this run's*. Treat the paths above as clean; anything else untracked or modified is
genuine debris to salvage or revert. Do not `git clean` them.

## Recent Iterations

### Iteration 5 — 2026-09-10 — T-003, notes become writable, and two Criticals in the confident parts

- **Started at Recover, not at Select.** `app/src/main/.../domain/model/Note.kt` was modified and
  uncommitted — not on the expected-untracked list, so genuine debris. Its mtime (12:08) postdated
  the T-002 checkpoint (12:00) with `.ai/` untouched, so a previous invocation had begun T-003 and
  died before checkpointing. The debris was one coherent addition (`Note.UNSAVED_ID`,
  `Note.UNSAVED_AT`, `Note.draft(...)`) and nothing else had been created. **Verified before
  building on it** (ENGINE.md §6.1): `assembleDebug` + `testDebugUnitTest` green, 17 tests, 0
  failures. Salvaged rather than reverted — it was exactly what T-003 needed and it turned out to
  have anticipated the `-1` (navigation) vs `0` (Room) id translation correctly.
- **Did:** `NoteRepository.save` + `getNote` with an injected `Clock`; the whole `ui/fragment/note/`
  screen (Fragment, UiState, ViewModel, `component/NoteEditor.kt`); the `noteFragment` destination
  and `toNote` action; `CoreBottomBar`'s centre button repointed with a **required** callback, a
  `contentDescription` and the tab-tint fix; both host screens wired; five strings in two locales;
  22 new unit tests; `testOptions { isReturnDefaultValues = true }`;
  `windowSoftInputMode="adjustResize"`; README.
- **Learned:**
  - **A domain rule should be scheduled to the task that creates its first write path, not the task
    that creates its first tempting caller.** `PLAN.md` had the future-date rule in T-008 because
    that is where a future date first becomes *selectable* — a fact about the UI. `DOMAIN.md` puts
    the enforcement point in the repository "so that no caller can bypass it", so the instant
    `save` existed and accepted any date it was handed, the code contradicted a human-owned rule.
    Deferring would have meant checkpointing known-wrong code and then filing an `ISSUES.md` entry
    against my own fresh diff. Moved into T-003 → amendment **A-006**; **DoD criterion 10 is now
    satisfied**, four tasks earlier than planned.
  - **The same reasoning shrank T-004.** Honouring the `noteId` nav argument (rather than shipping
    one the screen half-ignored, which would have blanked a real note) cost one repository method
    that got its caller in the same checkpoint — and it meant *both* halves of `DOMAIN.md` rule 2
    got tested where the stamping logic was written. The `createdAt`-clobbering half is invisible on
    a fresh database, so testing it a task later would have left the trap uncovered in the very
    checkpoint that introduced it. → amendment **A-007**.
  - **A test is not evidence until it has been made to fail.** Three mutations were applied and
    reverted — guard disabled, `createdAt = now` unconditional, `saving = false` moved above the
    success branch — and they failed exactly 3, 1 and 1 tests. Cheap (one edit, ~20s each) and the
    only thing separating "green against correct code" from "green against a tautology". Iteration 4
    reasoned its way to a non-tautological fake DAO; this iteration *proved* it. → `PLAN.md`.
  - **`android.util.Log` on an error path made that path untestable, and it had been silently true
    since T-002.** A JVM unit test compiles against a stub `android.jar` where every framework
    method *throws*, so the criterion-10 refusal test crashed on its own log line rather than
    failing on behaviour. `notesFlow`'s `.catch` has been in the same position all along. Fixed with
    `testOptions { unitTests { isReturnDefaultValues = true } }` → amendment **A-008**, with the
    cost recorded: `Bundle.putLong` is now a silent no-op, so `NoteFragment.argumentsFor()` can
    never be unit-tested and any assertion against it would pass vacuously.
  - **A ViewModel is testable on this toolchain**, which nothing here had tried.
    `Dispatchers.setMain(UnconfinedTestDispatcher())` and `viewModelScope` works; 12 of the 22 new
    tests are ViewModel tests. Worth knowing before declaring screen logic unprovable — the DoD's
    (H) evidence class is unavailable, but that never meant *screen logic* was out of reach, only
    rendering. → `knowledge/PROJECT.md`.
  - **`assembleDebug` and `testDebugUnitTest` both report green on a tree that does not compile.**
    Neither touches `app/src/screenshotTest/`. Giving `CoreBottomBar` a required parameter broke two
    screenshot cases and only `validateDebugScreenshotTest` said so. Run it in the same command
    whenever a composable's signature changes. → `knowledge/PROJECT.md`.
  - **The review's two Criticals were both in the parts the code documented most confidently, and
    that is now twice in a row.** (1) The editor was unusable with the keyboard open: the app is
    edge-to-edge and `CoreLayout` zeroes its `contentWindowInsets`, so the window is never resized
    for the IME and *nothing in `app/src/main` handled IME insets* — no `imePadding` anywhere. The
    viewport stayed full-screen height under a keyboard covering 40% of it, and the scroll range
    could never reach the covered strip. `NoteEditor`'s own KDoc claimed to prevent exactly that
    failure. (2) A double tap on Save wrote the note twice: the re-entry guard cleared when the
    database returned (~5ms) while the exit animation keeps the composition touchable for a few
    hundred more, and a note just created still carried `UNSAVED_ID`, so Room's `autoGenerate`
    handed out a second row. **The pattern: the confident comment is where to look.** A sentence
    asserting a defect is impossible is a sentence nobody re-checks — and in both cases the code was
    written *from* that belief.
  - **A README can invent a domain rule, and that is worse than a wrong comment.** I wrote "**The
    store owns the clock.** No screen reads the time" as a bullet beside the two real business
    rules — false (five `LocalDate.now()` call sites decide a note's *date*; the store owns only the
    *timestamps*) and, formatted that way, indistinguishable from `DOMAIN.md` content. `DOMAIN.md`
    is human-owned precisely so the engine cannot add rules to it; adding one to the README routes
    around that. Reworded, and the README now says out loud which two rules are the rulebook and
    that the third point is a convention serving them.
- **Reconciled:** four operational facts → `knowledge/PROJECT.md` (the screenshot source set,
  stubbed framework calls, IME insets, and the hand-rolled navigation-argument convention with its
  sentinel rule); three new `knowledge/ISSUES.md` entries (IME below API 30 unverified; no reference
  image for the Note screen; the editor's two exits — silent discard, and a refusal with no way
  forward, which **T-008 must resolve**); amendments **A-006**, **A-007**, **A-008**; `PLAN.md`'s
  ordering rationale corrected and its verification section widened; T-004 and T-008 rewritten to
  say what was removed and what is left; the Settings-create behaviour recorded as an assumption
  above.
- **Not done, on purpose:** no reference image for the Note editor (its date line is host-locale
  dependent — the same trap that kept populated note rows unpinned in T-002; filed with a concrete
  cheaper fix). No autosave or discard confirmation — a product decision, filed. `save` still
  discards the row id `NoteDao.upsert` returns; changing the return type belongs to the task that
  needs it. `NoteEditor` kept `LocalConfiguration` over the project's `LocalLocale` for consistency
  with `HomeNoteList`, to be unified in T-009 rather than split across two tasks.
- **Checkpoint:** see the `loop(T-003)` commit.

### Iteration 4 — 2026-09-10 — T-002, the app stops being a skeleton

- **Attempted:** T-002 — the notes table, the store above it, and Home repointed off the demo post
  list. Clean start: the working tree held only the paths listed above as expected, so Recover was
  a no-op and Select took T-002 as the only unblocked task.
- **Did:** `Note` + `NoteEntity` + `NoteDao` + `MIGRATION_2_3` + `NoteMapper` + `NoteRepository`
  (interface and impl) + Koin wiring in three modules, then `HomeUiState`/`HomeViewModel`/
  `HomeLayout` rewritten around notes, a new `HomeNoteList` component, two new unit-test classes,
  one new screenshot case, and the README's feature table and package tree.
- **Learned:**
  - **A Room migration cannot be tested here, but it can be made right by construction.** No
    Robolectric, no device, no unit test that executes SQLite — and
    `fallbackToDestructiveMigration(false)` turns a wrong migration into a *launch crash*, not a
    degraded read. The fix that worked: after any build, Room's own `CREATE TABLE` sits in
    `app/build/generated/ksp/debug/kotlin/.../AppDatabase_Impl.kt` § `createAllTables`, with the
    `TableInfo` it validates against directly below. Copy it verbatim. Writing it by eye first
    produced a `DEFAULT ''` the entity does not declare — harmless as it happens, and a difference
    nothing here could have caught. → `knowledge/PROJECT.md`, amendment **A-005**.
  - **A fake DAO only proves something if the fake refuses to help.** The DAO orders in SQL, which
    no available command can execute, so a fake that returned rows sorted would have made the
    ordering test a tautology — the classic test-agrees-with-the-code failure. `FakeNoteDao`
    returns its rows in construction order, deliberately jumbled, and the repository sorts them
    itself. The redundancy with the SQL `ORDER BY` is the point: **the "newest first" promise now
    belongs to the repository rather than to a query string a future edit can quietly change**, and
    the suite fails against three separate breakages including `createdAt` ordering, which is the
    exact trap `knowledge/DOMAIN.md` names.
  - **`git reset` and `git restore` are denied; `git rm --cached` is the unstage.** Found by
    `git add -A app/src` sweeping up the three orphan PNGs that must stay untracked. Not routed
    around. → `knowledge/PROJECT.md`, with the warning not to use `-r` on the folder, which would
    untrack the six committed references beside them.
  - **The review's two Majors were both in `knowledge/`, not in the code.** `PROJECT.md` still said
    `AppDatabase` is at `version = 2`; that file is read as *convention* at Orient, so the next task
    would have read it as current and bumped to 3 a second time — a version/migration pair Room
    rejects at launch. And `ISSUES.md` still asked for two string keys this task had just deleted.
    Worth naming as a pattern: **the reconciliation step is not paperwork after the work, it is
    part of the work**, and a stale line in a file read every iteration is a defect with a delay
    fuse. Both fixed in this checkpoint, as `POLICIES.md` § Reconciliation Rules requires.
  - **A locale-correct date is not `Locale.getDefault()`.** The row formats its date with
    `LocalConfiguration.current.locales[0]`, because this app has its own language picker in
    Settings — the device default answers a different question, and a user who switched the app to
    German could have read an English date under German copy.
  - **What a screenshot case is worth depends on what it renders.** The new empty-state reference
    defends `HomeNoteList`, not `HomeLayout` — the layout is `private`, as the house rules require,
    so the screenshot source set cannot reach it. Populated rows are pinned by nothing at all,
    because a row prints a locale-dependent date and a reference image of that fails on a German
    machine for reasons unrelated to the app. Both gaps are written into `HomeScreenshotTest.kt`
    itself rather than left for someone to discover, and the untested "Untitled note" fallback is
    carried into T-004.
- **Reconciled:** `knowledge/PROJECT.md` — Room now at version 3 with the epoch-day note, the test
  surface, the migration technique, and the `git rm --cached` rule; `knowledge/ISSUES.md` — the
  string-key entry reduced to the one surviving prayer-time key (two resolved themselves here);
  amendment **A-005**; `PLAN.md`'s Room-migration risk closed with what it actually cost; three
  human-inspection items and the uncovered row fallback recorded in `.ai/TASKS/T-002.md`.
- **Not done, on purpose:** rows are not clickable (T-004 opens a note), so **DoD criterion 12 is
  half-satisfied** — bounded overflow exists, the route to the full text does not. No populated-row
  screenshot, for the locale reason above. `saveNote` was not added to `NoteRepository` "while I was
  there": a repository method with no caller is not a checkpoint of working behaviour
  (`POLICIES.md` § Task Decomposition), and T-003 is where the write path becomes observable.
- **Checkpoint:** see the `loop(T-002)` commit.

### Iteration 3 — 2026-09-10 — T-010, salvaged from a crashed invocation

- **Started at Recover, not at Select.** The working tree was dirty with debris that was *not* on
  the expected-untracked list: modified `app/build.gradle.kts`, `gradle.properties`,
  `gradle/libs.versions.toml`, plus untracked `app/src/screenshotTest/` and
  `app/src/screenshotTestDebug/`. Build outputs were stamped 11:00 against a 10:35 `STATE.md` and a
  `loop(T-001)` tip, so an invocation had done most of T-010 and died before checkpointing. `.ai/`
  was untouched, so the debris was self-consistent rather than half-written.
- **Verified before building on it** (ENGINE.md §6.1: never build on unverified debris). The import
  was faithful — `ScreenshotScaffold.kt` diffed byte-identical against
  `loop/todo-calendar-screens` — and all four Gradle tasks passed, with the screenshot XML reporting
  4 real cases rather than a vacuous zero. Salvaged rather than reverted.
- **Learned:**
  - **The screenshot harness works on this toolchain, unchanged.** `validateDebugScreenshotTest`
    and `updateDebugScreenshotTest` both run host-side, ~15s, no emulator. Two more `PROJECT.md`
    toolchain rows went from `no` to `yes`; every granted capability is now exercised.
  - **`updateDebugScreenshotTest` orphans references instead of replacing them.** The filename
    carries a hash of the preview's *parameters*, so changing `widthDp` writes a new file and leaves
    the old one — and validation ignores strays and stays green, so nothing tells you. Two update
    runs produced three orphans. → `knowledge/PROJECT.md`.
  - **`rm` is denied.** Discovered trying to clean those orphans. Not routed around (ENGINE.md §13):
    only the six live PNGs were staged, the strays are left untracked, and both the orphan list and
    the missing capability are in `knowledge/ISSUES.md` for a human. Worth knowing before creating
    a file you will want to remove.
  - **The review found the iteration's real defect, and it was a sentence.** The test file's header
    asserted that these images make DoD criteria 1 **and 2** machine-defendable. Criterion 2 is not
    covered and cannot be: `Color.White` and `colorScheme.onBackground` are the same pixels. The
    reviewer proved it rather than argued it — 65 hardcoded literals are in the tree right now and
    every case passes at `diffPercent 0.0`. Left standing, that comment would have retired criterion
    2 from being checked at all, which is exactly `POLICIES.md` § Evidence Requirements' forbidden
    third route arriving by accident. → amendment **A-004**; the file now says what it does *not*
    prove, and `ISSUES.md` carries the residue.
  - **Writing the images down as "approved" is not the same as approving them.** The engine can
    look at a PNG and did — all six are correct. That is not the human approval the acceptance
    criterion asks for, and the difference is the whole point of the image existing. Recorded as
    still open rather than quietly closed.
  - **A swatch can be wrong in a way only rendering shows.** The `inversePrimary` row paired a fill
    with an on-colour Material never puts together — 3.94:1, under AA — asking a human to approve a
    combination the app will never paint. And `surfaceDim` renders as a seamless black rectangle,
    which is *correct* (`Theme.kt` assigns it the ground on purpose) but reads as a missing row; it
    is now labelled so nobody "fixes" the theme over it.
  - **README was the third Major.** `CLAUDE.md` requires README sync in the same change as a
    structural addition, and criterion 13 names `validateDebugScreenshotTest` — yet neither the
    command, the reference location, nor the never-re-record-to-go-green rule was written anywhere a
    human would look. Added, with a test-source-set table.
- **Reconciled:** two toolchain rows verified + four operational facts → `knowledge/PROJECT.md`
  (orphaned references, spaces in reference filenames, `rm` denied, what a screenshot cannot prove);
  one new `ISSUES.md` entry (host-locked exact-pixel references, no threshold or toolchain pin, plus
  the three orphans) and the hardcoded-colour entry extended with criterion 2's missing gate;
  amendment **A-004**; `PLAN.md`'s verification section corrected; `CoreBottomBar`'s uncovered active
  state carried forward to T-006 in `.ai/TASKS/T-010.md`.
- **Not done, on purpose:** no `imageDifferenceThreshold` and no `jvmToolchain` were added. Both are
  the right fix for the host-lock risk and both are build-configuration changes with their own blast
  radius; guessing at a threshold number with no observed drift to calibrate against would be
  inventing evidence. Filed instead.
- **Checkpoint:** see the `loop(T-010)` commit.

## Iteration Index

<!-- One line per iteration older than the three above. The SHA is the checkpoint commit whose
     STATE.md still holds that iteration's full entry:  git show <sha>:.ai/STATE.md -->

| Iteration | Checkpoint | What happened |
|---|---|---|
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
