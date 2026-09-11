# AMENDMENTS

> Append-only log of plan mutations. Every Tier-1 amendment is logged here as it is applied; every
> Tier-2/3 change is logged here once the human has decided it.
>
> One entry per amendment: timestamp, tier, reason, affected tasks, decision, expected impact.

---

## A-001 — Notes gain a `title` field

- **Timestamp:** 2026-09-10 (iteration 1, bootstrap)
- **Tier:** 1 — task amendment within the approved shape. The *decision* itself was Tier 3 (it
  changed intent), but the human made it directly by editing `.ai/DoD.md` before approving; what is
  logged here is only the propagation of that decision into the task files.
- **Reason:** the human approved the DoD and overruled assumption **A5**. The engine had proposed
  body-only notes, following the inferred data model in `PRD.md` §4, which lists `content` and marks
  a title "unconfirmed". The human's rationale, recorded in `DoD.md`: a reverse-chronological list of
  every note in the app is the primary navigation surface, and it is far harder to scan without
  titles — and the engine's own point that this is expensive to retrofit is the reason to do it now.
- **Decision:** `Note` carries `title: String` alongside `content: String`. The title may be blank; a
  blank title falls back to the first line of the body for display. DoD criteria 3, 4 and 6 were
  edited by the human to match.
- **Affected tasks:** T-002 (entity, migration, domain model, mapper, Home row), T-003 (create
  screen), T-004 (edit screen). T-001 and T-005 through T-009 are unaffected.
- **Expected impact:** small and entirely front-loaded. `title` is one more `TEXT NOT NULL DEFAULT ''`
  column in the same `CREATE TABLE` that T-002 was already going to write, one more field through the
  mapper, and one more editor field on the Note screen. Because no `NoteEntity` exists yet, this
  costs nothing extra — which is precisely why the human made the call now rather than after T-002
  had shipped a table without it. No change to the task graph, the dependency order, or the evidence
  strategy.

---

## A-002 — Import the existing screenshot harness; the evidence strategy changes

- **Timestamp:** 2026-09-10 (iteration 1, bootstrap)
- **Tier:** 1 — adding a task and re-describing evidence, within a shape the human explicitly
  directed in escalation D-001. The underlying decision (Q4, Option B+) was Tier 2 and is the
  human's; this entry logs only the propagation.
- **Reason:** the engine reported that this repository "has no host-side test setup" and classified
  the perceptual clauses of DoD criteria 1, 2, 7 and 9 as *"no command can ever prove"*. **That was
  wrong, and the correction came from the human.** It is true of `main` and false of the repository:
  a prior Foreman run on the unmerged branch `loop/todo-calendar-screens` already built and verified
  a complete Compose Preview Screenshot Testing harness on this exact toolchain. The engine branched
  from `main` and never looked at sibling `loop/*` branches, so it re-derived a conclusion the
  repository's own history already refuted.
- **Decision:**
  1. **New task T-010 — import the harness**, placed between T-001 and T-002. Not re-derived:
     imported from `loop/todo-calendar-screens` (plugin, catalogue entries, `gradle.properties` flag,
     `ScreenshotScaffold.kt`). Reference PNGs are regenerated, not copied — our screens differ and
     now carry a title field.
  2. **The evidence class of criteria 1, 2, 7 and 9 changes** from "unprovable, human-inspection
     only" to **"the human approves the reference image once; the machine defends it thereafter"**.
     A committed reference image *is* a standard a command can fail against, which is exactly what
     the engine said could not exist.
  3. **Criterion 13 gains `:app:validateDebugScreenshotTest`** once the harness is in (human's
     instruction in D-001). ⚠️ `.ai/DoD.md` itself was **not** edited to say so — it is approved and
     therefore immutable to the engine (Invariant 2). The instruction is honoured from here and from
     the task files. If the human wants the DoD file to match, they paste that task name into
     criterion 13 themselves.
  4. **Every task's "needs human inspection" line is re-scoped** to "approve the reference image
     once" wherever a screenshot can pin the property.
- **Affected tasks:** T-010 (new); T-001, T-002, T-004, T-005, T-006, T-007, T-008, T-009 (evidence
  sections); `.ai/PLAN.md` (strategy, task graph, risks).
- **Expected impact:** substantially more of the DoD becomes machine-defended, and the T-009 human
  checklist shrinks to first-time reference-image approval rather than a full manual walkthrough on
  every criterion. Cost: one extra task, one Gradle plugin, and a set of reference PNGs in the
  repository. The prior run's own test file names the three regressions this catches — an invisible
  has-notes dot on today's fill, a selection that never reached the grid, and an unexplained blank
  gap for an empty day — all of which passed a green unit suite.
- **Standing lesson, recorded in `knowledge/PROJECT.md`:** before deriving that this repository
  cannot do something, check the sibling `loop/*` branches. `git branch --list 'loop/*'` was
  available under baseline capabilities the whole time.

---

## A-003 — Every task that adds a user-visible string must also add its German translation

- **Timestamp:** 2026-09-10 (iteration 2)
- **Tier:** 1 — adds a prerequisite step to existing tasks. No change to the PRD, the DoD, the
  architecture, or the task graph.
- **Reason:** discovered by running lint for the first time. `./gradlew :app:lintDebug` **failed on
  the untouched baseline** with four `MissingTranslation` errors — this repository ships a
  `values-de/` locale and AGP lint treats a missing translation as an *error*, not a warning. The
  bootstrap iteration could not have known: it was never able to run a single Gradle command.
  The consequence is bigger than the four strings. **DoD criterion 13 (lint is green) and criterion
  14 (new user-visible strings live in `res/values/strings.xml`) were on a collision course:** this
  run adds roughly fifteen new strings across T-002 to T-009, and each one, added to `values/` alone,
  produces a fresh lint error and turns criterion 13 red.
- **Decision:** treat `res/values-de/strings.xml` as part of the definition of "added a string".
  Every task that appends to `values/strings.xml` appends the German equivalent in the same
  checkpoint. The four baseline gaps were filled in T-001 so the run starts from green.
  Rejected alternatives, and why:
  - *`lint { disable += "MissingTranslation" }` or `tools:ignore`* — switches off a check the
    repository deliberately has at error severity, to make a self-inflicted problem disappear. It
    would also silently green-light criterion 13 over a real defect, which is the exact failure mode
    `POLICIES.md` § Evidence Requirements forbids.
  - *Mark the new strings `translatable="false"`* — false: they are ordinary UI copy.
  - *Delete `values-de/`* — plausibly what the human wants (see below), but deleting a shipped locale
    is not the engine's call.
- **Affected tasks:** T-002 through T-009 — every task whose acceptance includes a new string.
  `.ai/PLAN.md` § Verification approach records the standing rule.
- **Expected impact:** a few minutes per task, and criterion 13 stays satisfiable for the whole run.
- **⚠️ Recorded as an assumption the human may overrule cheaply, in `STATE.md` § Assumptions.** The
  engine is now authoring German product copy, which is content the human never asked for. The
  honest alternative is "German is a stale demo locale — drop it", and that is a one-line answer
  that deletes this amendment. It was **not** escalated because it blocks nothing: the conservative
  route (keep an enabled check green) is available, reversible, and its worst failure is a clumsy
  German phrase, not a wrong app.

---

## A-004 — 2026-09-10 (iteration 3) — the screenshot layer does not evidence DoD criterion 2

- **Tier:** 1 — corrects a factual claim about what a verification layer proves. No change to the
  PRD, the DoD, the architecture, or the task graph. The approved verification shape already lists
  review and human inspection as layers; this only moves criterion 2 between existing layers.
- **Reason:** T-010 landed the screenshot harness on the strength of a claim in `PLAN.md` that it
  would cover the perceptual halves of criteria 1, 2, 7 and 9. The fresh-context review of T-010
  showed that is **false for criterion 2**, and gave the proof rather than the argument: fourteen
  files under `ui/` hold 65 hardcoded colour literals *today*, and all six screenshot cases pass
  with `diffPercent 0.0`. A screenshot cannot distinguish `Color.White` from
  `colorScheme.onBackground` — both are `#FFFFFF` pixels. Criterion 2 is about where a value came
  from, which is a property of the source, not of the rendering.
  What made this worth an amendment rather than a note: the wrong claim had been **written into the
  test file's own header comment** as settled fact. Left there, the next reader treats criterion 2
  as defended, the reviewer stops looking for it because a command appears to cover it, and the run
  reports a verified `DONE` over a requirement nothing checked. That is `POLICIES.md` § Evidence
  Requirements' third route — quietly treating the machine-checkable subset as the whole
  requirement — arriving by accident instead of by intent.
- **Decision:** criterion 2 is carried by the fresh-context review of each diff, and by nothing
  else, until a static check exists. Recorded in three places so it cannot be quietly forgotten:
  the header comment of `ThemeScreenshotTest.kt` (which now states what the images do **not**
  prove), `.ai/PLAN.md` § Verification approach, and the hardcoded-colour entry in
  `knowledge/ISSUES.md`. Criteria 1, 7 and 9 are unaffected — those *are* properties of the
  rendering, and the images do defend them.
- **Rejected:** writing a custom lint rule or a grep-based Gradle check inside T-010. It is the
  right fix and it is proposed in `knowledge/ISSUES.md`, but building a new static-analysis gate is
  not "import the harness", and slipping it in would make T-010's evidence about two different
  things at once.
- **Affected tasks:** T-002 through T-009 — each states criterion 2's evidence as review, not as a
  green screenshot run. No task is added, removed or reordered.
- **Expected impact:** none on schedule. The effect is on honesty of reporting: at Final
  Verification, criterion 2 must be reported as reviewed-by-reading, and criteria 1, 7 and 9 as
  "approved once by a human, defended since by `validateDebugScreenshotTest`".

---

## A-005 — 2026-09-10 (iteration 4) — MIGRATION_2_3 copies Room's own CREATE TABLE instead of the SQL T-002 described

- **Tier:** 1 — a correction to one task's implementation detail. No change to the PRD, the DoD, the
  architecture or the task graph; `AppDatabase` still goes to `version = 3` with a hand-written
  `MIGRATION_2_3` registered in `DatabaseModule`, exactly as approved.
- **Reason:** `.ai/TASKS/T-002.md` specified the `title` column as `TEXT NOT NULL DEFAULT ''`. Room
  generates the same table on a **fresh install** from `NoteEntity`, and that generated statement
  has no `DEFAULT` — an entity without `@ColumnInfo(defaultValue = ...)` produces a plain
  `TEXT NOT NULL`. Two tables that differ is precisely the situation Room's first-open validation
  exists to reject, and `fallbackToDestructiveMigration(false)` turns a rejection into a **launch
  crash** for every user upgrading from version 2. Nothing in this repository can catch that: there
  is no Robolectric, `MigrationTestHelper` needs an instrumented device, and no unit test executes
  SQLite. So the difference would have shipped unverified.
  (Room's `TableInfo` comparison happens to tolerate a database-side default the entity does not
  declare — the expected column carries `defaultValue = null` and the check is skipped. Tolerating
  a difference is not the same as not having one, and relying on that detail buys nothing.)
- **Decision:** the migration SQL is a **copy of the statement Room itself generates**, read from
  `app/build/generated/ksp/debug/kotlin/.../AppDatabase_Impl.kt` § `createAllTables` after the
  build. Byte-identical, backticks included. `title` therefore carries no SQL default; "a note may
  have no heading" is expressed by the Kotlin type being an empty `String`, which is where it was
  always enforced.
- **Rejected:** adding `@ColumnInfo(defaultValue = "''")` to `NoteEntity` so both paths declare the
  default. It works, and it makes the entity carry a SQL detail for the sole benefit of matching a
  migration — the tail wagging the dog, and one more thing to get right for no behaviour gained.
- **Affected tasks:** T-002 only. No task added, removed or reordered.
- **Expected impact:** none on schedule. The standing consequence is a technique worth reusing, now
  in `knowledge/PROJECT.md`: when hand-writing a Room migration here, copy Room's generated
  statement rather than composing one from the entity by eye.

---

## A-006 — 2026-09-10 (iteration 5) — the future-date rule lands in T-003, not T-008

- **Tier:** 1 — a prerequisite moved earlier inside the approved task shape. No change to the PRD,
  the DoD, the architecture or the task graph. `knowledge/DOMAIN.md` is untouched; the rule is
  implemented, not edited.
- **Reason:** `PLAN.md` put the rule in T-008 on the reasoning that the Calendar screen is where a
  future date first becomes *selectable*. That reasoning is about the UI, and the rule is not:
  `knowledge/DOMAIN.md` says the enforcement point is "in the repository layer, below the UI, so
  that no caller can bypass it", and adds that "a UI that merely hides the affordance does not
  satisfy this rule". T-003 is the task that creates the first write path. The moment
  `NoteRepository.save` exists and accepts any date it is handed, the codebase contradicts the rule
  as written — and ENGINE.md §9 classifies that as a domain defect to fix inside the current task,
  not a gap to schedule. Deferring would have meant checkpointing code that is known-wrong against
  a human-owned rule, then filing an `ISSUES.md` entry against my own fresh diff.
- **Decision:** `NoteRepositoryImpl.save` refuses any note dated after `LocalDate.now(clock)` and
  reports it as `Outcome.Error`. `isAfter`, so today is allowed — the boundary the rule spells out
  explicitly. Enforced on every write path, so editing an existing note's date into the future is
  refused identically. **DoD criterion 10 is therefore satisfied by this checkpoint**, with the
  test it names: tomorrow refused, today and yesterday accepted, and the row verified absent after
  a refusal so that a `save` which errored *and* wrote would still fail.
- **Rejected:** enforcing it in the ViewModel, or only in T-008's calendar cell. Both are the "a
  rule enforced in one caller is a rule the second caller walks past" failure `PLAN.md` itself
  warns about two paragraphs earlier.
- **Affected tasks:** T-003 gains the rule and the test. **T-008 keeps its calendar work and its
  acceptance changes from implementing the refusal to re-verifying it** — its own tests stay, as a
  second caller reaching the same guard is worth its own case. No task added, removed or reordered.
- **Expected impact:** T-008 gets smaller. The `Clock` seam this needed is also what made
  `createdAt`/`updatedAt` stamping testable, which was not the motivation but is the larger win.

---

## A-007 — 2026-09-10 (iteration 5) — T-003 loads an existing note as well as creating one

- **Tier:** 1 — one prerequisite folded from T-004 into T-003. No change to the PRD, the DoD or the
  architecture; the task graph keeps all nine tasks and their order.
- **Reason:** `.ai/TASKS/T-003.md` asked for a nav destination carrying **both** a date and a note
  id (`-1` for new), so the navigation contract would not need reshaping in T-004 or T-008. Adding
  the id argument while `NoteViewModel` ignored it would have shipped a live trap: a real id
  arriving on a screen that pre-fills nothing, whose save then upserts that row — blanking a note
  the user only meant to open. The two honest options were to leave the id argument out until T-004
  or to honour it now. Honouring it costs `NoteRepository.getNote(id)`, which gets its caller in the
  same checkpoint, so it is not a method with nothing wired to it (`POLICIES.md` § Task
  Decomposition).
  The stronger reason is the one found while writing the stamping logic: `DOMAIN.md` rule 2 defines
  *two* behaviours — creating sets both timestamps, editing moves `updatedAt` and leaves `createdAt`
  alone. Both live in the same four lines of `save`. Testing the create half now and the edit half a
  task later would leave the trap (`createdAt` clobbered on every edit) untested in the checkpoint
  that introduces it, and it is invisible on a fresh database.
- **Decision:** T-003 adds `NoteRepository.getNote(id): Note?`, `NoteViewModel.openNote` loads a
  stored note when given a real id, and `save` preserves that note's `id` and `createdAt`. Both
  halves of rule 2 are tested here.
- **Rejected:** leaving the `noteId` argument out of the nav graph until T-004. Cheaper by two lines
  of XML, but it makes the create path the only one the screen supports while the screen is already
  general enough to do both.
- **Affected tasks:** T-003 gains `getNote` and the load path. **T-004 is reduced to what makes
  editing observable** — Home rows become clickable, they navigate through the route that now
  exists, and the list re-sorts. Its acceptance keeps the re-sort evidence. No task added, removed
  or reordered.
- **Expected impact:** T-004 gets smaller. The residual cost is honest and recorded: `getNote` and
  the `noteId` argument have no *in-app* caller until T-004, so today they are exercised only by
  tests.

---

## A-008 — 2026-09-10 (iteration 5) — unit tests may run against stubbed Android framework calls

- **Tier:** 1 — a test-runtime setting in `app/build.gradle.kts`. No production code path changes,
  no dependency added, no architecture or contract affected.
- **Reason:** a JVM unit test here runs against a stub `android.jar` in which every framework method
  **throws** `"not mocked"`. `NoteRepositoryImpl.save` logs one line when it refuses a future-dated
  note, and DoD criterion 10 requires a unit test proving exactly that refusal — so the test did not
  fail on behaviour, it crashed on `Log.w`. The same wall stands in front of every error path in
  every repository in this project: `notesFlow`'s `.catch` has been untestable for the same reason
  since T-002. The alternative was to strip logging from the paths tests reach, which trades an
  observable production behaviour for a testable one — the wrong direction.
- **Decision:** `testOptions { unitTests { isReturnDefaultValues = true } }`. Framework stubs return
  `0` / `false` / `null` instead of throwing, and the code under test runs.
- **Rejected:** adding Robolectric. It is the real answer for a test that needs framework
  *behaviour* rather than framework *silence*, and it is a new dependency with its own runtime and
  configuration — far past the scope of "create a note from Home". Also rejected: removing the log
  lines.
- **Affected tasks:** none. No task added, removed or reordered.
- **Expected impact:** error paths in repositories and ViewModels become testable, which is a gain
  this run will keep using. The cost is recorded in `knowledge/PROJECT.md` and is not zero: an
  unmocked framework call in a unit test now returns a default **quietly**. Concretely,
  `android.os.Bundle.putLong` is a no-op under this flag, so `NoteFragment.argumentsFor()` cannot be
  unit-tested — it would silently return an empty `Bundle` and any assertion would be meaningless.
  A test that needs a framework return value needs Robolectric, not this flag.

---

## A-009 — 2026-09-10 (iteration 6) — T-005 must resolve the failed-read defect before adding delete

- **Tier:** 1 — a prerequisite added to an existing task inside the approved shape. No task added,
  removed or reordered; PRD, DoD and architecture unchanged.
- **Reason:** the fresh-context review of T-004 (finding 2) found that `NoteRepositoryImpl.getNote`
  answers `null` both for "no such row" and for "the read threw", and that
  `NoteViewModel.openNote` treats `null` as "start a new note". The editor therefore opens **blank
  but correctly dated**, and its save **inserts** — the draft still carries `Note.UNSAVED_ID`, so
  Room's `autoGenerate` hands out a fresh row. Home then shows two notes for that day: the original
  with its old text and old list position, plus the retyped copy, with nothing telling the user
  either happened.

  T-004 is what made that branch reachable at all: until Home rows became clickable, every caller
  of `NoteFragment.argumentsFor` left `noteId` at its default, so `getNote` had no in-app caller and
  the fall-through was dead code. Today reaching it still needs a database failure. **T-005 is where
  it stops being rare** — delete turns "the note you opened is not there" from a disk fault into
  ordinary use, and a note deleted on one surface then opened from a stale Home row would silently
  resurrect itself as a duplicate.
- **Decision:** T-005 resolves it as a prerequisite, not a follow-up, and gains an acceptance
  criterion: a unit test proving that opening a note the store cannot produce does not end in a
  second row being written. The task file names the trap that comes with it — the existing
  `NoteViewModelTest` case *a note that has since been deleted opens as a fresh one for the day
  asked for* pins the current behaviour deliberately, so resolving this means changing that test on
  purpose rather than working around it.
- **Rejected:** fixing it inside T-004. The clean fix distinguishes refusal from failure in
  `getNote`'s return type — a change to a public repository contract — and overturns a deliberate,
  tested, documented T-003 decision, from inside a task whose scope is "the route in". It is a
  Major, and `POLICIES.md` § Review Standards files a Major rather than expanding the current task
  around it. Also rejected: keeping the requested id on the draft so the save updates in place —
  that silently re-creates a note the user deleted, a different wrong answer rather than a smaller
  one.
- **Affected tasks:** T-005 gains one description bullet and one acceptance criterion.
- **Expected impact:** T-005 grows slightly. The exposure window is one iteration, and it is
  recorded in `knowledge/ISSUES.md` so it survives even if the plan changes shape.

---

## A-010 — On the Calendar screen the centre bottom-bar button means the picked day, not today

- **Timestamp:** 2026-09-11 (iteration 10)
- **Tier:** 1 — a screen-level behaviour choice inside T-008's approved scope. It is logged because
  it makes one shared component mean two different things depending on the screen hosting it, and
  because a reader could mistake it for a contradiction of DoD assumption A11.
- **Reason:** T-008 adds an add-note action beside the selected day's notes, dated to that day
  (DoD criterion 11). `STATE.md` left open whether the bottom bar's centre button should change
  meaning on the same screen and asked for the decision to be made deliberately and written down.
  Leaving it as "today" would put two add affordances a few centimetres apart on one screen, filing
  notes on two different days, with nothing on either one saying so — and a user who picked the 3rd,
  tapped the larger and more obvious of the two, and got a note dated today would not discover it
  until they went looking. That failure is invisible at the moment it happens, which is what makes
  it worse than the alternative.
- **Decision:** on `CalendarFragment` both routes call `CalendarViewModel.dateForNewNote()`, so the
  two controls agree by construction. Home and Settings are unchanged: today. `CoreBottomBar`'s
  KDoc now states that the meaning is each screen's own and points at the reason.
- **Why this is not a DoD violation, checked before implementing:** A11's row is titled "Create-note
  entry point **from Home** (PRD §3.1, sub-question)" — it resolves whether Home should have one at
  all. Criterion 5 is Home-scoped in its own words ("The centre action button … opens the Note
  screen for a new note dated today. Saving it returns to Home, where the new note is the first
  row") and remains true. Had either been unscoped, this would have been a Tier-3 proposal rather
  than an amendment: the engine does not get to reinterpret approved intent because it prefers a
  different screen.
- **Rejected:** (a) leaving the centre button as today — the two-meanings-one-screen trap above;
  (b) hiding or disabling the centre button on this screen — `CoreBottomBar` has no such state, and
  removing the app's primary action from one screen is a larger change than making it agree with
  its neighbour; (c) filing under today when nothing is picked *and* nothing is drawn — rejected
  because the centre button must always do something, so "nothing picked" falls back to today while
  the day's own action is simply not drawn.
- **Affected tasks:** T-008 only. T-009's human-inspection checklist gains one item — whether this
  reading of the button is the product's.
- **Expected impact:** one extra `@param` note on `CalendarLayout`, a rewritten paragraph in
  `CoreBottomBar`'s KDoc, and a human-inspection row. No change to the task graph or the evidence
  strategy.
