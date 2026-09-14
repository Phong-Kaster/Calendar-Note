# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

### Iteration 8 — 2026-09-14 — D-007 consumed; A-006 and A-007 completed; run's last task closed, DONE-candidate

- **Recovery:** working tree was clean on entry apart from the human's filled-in `## Decision` on D-007 in
  `ESCALATION.md` — expected, not debris (§6.1 treats a human answer between iterations as normal input).
- **Decisions consumed:** D-007 — option 1, granted, goal-scoped to one invocation of
  `updateDebugScreenshotTest --tests "*AlarmsPermissionNoticeCase*"`. Ran it once; before/after state of
  `AlarmsScreenshotTestKt`'s reference directory reported in `ESCALATION.md` (two files → three, only the
  new case's file appeared). Re-verified `assembleDebug` + `testDebugUnitTest` + `lintDebug` +
  `validateDebugScreenshotTest` together: `BUILD SUCCESSFUL`, 251 tests / 0 failures, lint 0 errors / 72
  warnings, 23/23 screenshot cases. A-006 marked complete (`AMENDMENTS.md` A-15).
- **Phase executed:** Phase 6 (A-007, single task) — the only task left in the run. Dispatched one Worker,
  scope `.harness/knowledge/PROJECT.md` only; verified against `git status` (no scope violation). The
  Worker reconciled every stale claim in the file against the tree (notification/alarm infrastructure,
  the four-tab bottom bar, the toolchain numbers, the capability ledger note) and added two new
  Constraints — **C-16** (an Activity's launch `Intent` replays on every recreation) and **C-17**
  (revoking the exact-alarm permission cancels pending alarms; granting it back arms nothing without an
  explicit re-arm).
- **Fresh-Context Review:** one MAJOR (C-06's new sentence claimed the `AlarmScheduler` seam was "the only
  part of the alarms feature any test can see", contradicting the 85 tests that exist over
  `AlarmRepositoryImpl`/`AlarmsViewModel`/`AlarmEditorViewModel` and contradicting C-17's own citation of
  `AlarmsViewModelTest` nine lines later) and one MINOR (`AlarmNotifier` exposes five constants, not four
  — a stale KDoc comment in the source file itself said "four"). Both fixed by the Iteration this
  checkpoint (`AMENDMENTS.md` A-16). A-007 marked complete.
- **Also fixed:** the `AlarmsPermissionNoticeCase` reference PNG D-007 recorded was untracked in git —
  staged in this checkpoint's commit so a fresh clone gets all 23 references.
- **Result:** all seven tasks (A-001 … A-007) are complete. Nothing abandoned, nothing deferred, no queued
  decision outstanding. `STATE.md` set `DONE-candidate: yes`. All sixteen `human` DoD criteria remain
  unsigned — a Human Verification Request is owed before this run can report `DONE`; that is the next
  (Verifier) invocation's job per §11, not this one's, since this iteration wrote implementation
  (dispatched a Worker and made review-driven fixes) and the DONE-candidate rule requires a clean
  invocation to certify.
- **Report:** `CONTINUE`.

### Iteration 7 — 2026-09-14 — Phase 5 (A-005, A-006) dispatched and reviewed; A-005 completed; A-006 code-complete, blocked on D-007

- **Phase:** 5 — A-005 and A-006, dispatched as two parallel Workers with disjoint Declared File Scopes
  (verified against `git status` before either was trusted — the union matched exactly, no overlap).
- **Recover (§6.1):** working tree was clean on entry; no debris to salvage.
- **A-005 Worker** (boot re-arm): added `AlarmScheduler.rearmAll` with a **default body** so
  out-of-scope fakes (`FakeAlarmScheduler`, `BrokenAlarmScheduler`) kept compiling untouched; a new
  `BootReceiver` (`goAsync()` + coroutine reading `alarmRepository.alarmsFlow.first()`, then
  `rearmAll`); a new `RearmAllTest` (5 tests) against the real default. Initially **overrode**
  `rearmAll` in `AlarmManagerAlarmScheduler` for a minor efficiency gain — removed by the
  Fresh-Context Review (see below).
- **A-006 Worker** (permission notice): a new `AlarmsPermissionNotice` banner; `AlarmsUiState` /
  `AlarmsViewModel` gained the permission state and `setPermissionState`; `AlarmsFragment` re-reads on
  every resume and wires two fix buttons; 4 new `AlarmsViewModelTest` cases. Correctly avoided the
  `requestExactAlarm()` trap (a local function in `HomeRequestPermission.kt`, not importable) by
  writing its own small version — independently confirmed by both this Worker and A-005's, worth
  recording as Constraint C-15.
- **Iteration wiring (§6.6):** `AndroidManifest.xml` (`RECEIVE_BOOT_COMPLETED` + exported
  `BootReceiver` receiver), both `strings.xml` (5 keys), `AlarmsScreenshotTest.kt`
  (`AlarmsPermissionNoticeCase`, `heightDp = 260`), `README.md` (feature table + package tree).
- **Build/test/lint (§6.7):** `assembleDebug` + `testDebugUnitTest` + `lintDebug` → `BUILD SUCCESSFUL`,
  251 tests (0 failures, up from 239), lint 0 errors / 72 warnings (3 new, all pre-existing tolerated
  categories). `validateDebugScreenshotTest`: 22/23 green, 1 expected failure (no reference image yet
  for the brand-new `AlarmsPermissionNoticeCase` — queued as D-007, not treated as a defect).
- **Fresh-Context Review (§6.8):** no Constraint violation. Six findings, all fixed this checkpoint —
  two MAJOR (granting exact alarms back re-armed nothing; `RearmAllTest` exercised a code path
  production had overridden away) and four MINOR (two settings deep-links one screen short; two
  silent failures; one unbounded `goAsync()`). Full detail in `AMENDMENTS.md` A-14. One of my own
  fixes (the re-arm call) initially used `Dispatchers.IO` explicitly and broke a test by escaping the
  test's unconfined dispatcher — caught immediately by re-running the test suite, fixed by matching
  this ViewModel's own established convention (no explicit dispatcher on `viewModelScope.launch` for
  a write that delegates to an already-dispatcher-safe seam).
- **Reconcile (§6.9):** two Tier-1 amendments logged (A-13 wiring, A-14 review fixes). One Tier-2
  decision queued (D-007, capability grant) — blocks A-006's completion and, transitively, A-007's
  selectability; does not block anything else, and nothing else was executable this Phase regardless.
  Two Constraints added to `.harness/knowledge/PROJECT.md` (C-14 settings deep-link specificity, C-15
  the `requestExactAlarm` trap).
- **Persist:** `STATE.md`, `RESUME.md`, `TASKS/A-005.md`, `TASKS/A-006.md`, `AMENDMENTS.md`,
  `ESCALATION.md`, `.harness/knowledge/PROJECT.md`, `.harness/ISSUES.md`, `README.md` all updated in
  this checkpoint's commit alongside the code.
- **Report:** `ESCALATE` — A-006's completion and A-007's selectability both wait on D-007, and no
  other executable task exists in this run.

### Iteration 6 — 2026-09-14 — Recovered a crashed invocation; D-006 applied and A-003 completed; Phase 4 (A-004) completed and reviewed

- **Phase:** 4 — A-004 alone (the only executable task at Orient time, once D-006's consumption was
  recognised as already-done debris).
- **Recover (§6.1):** the working tree on entry was not clean. Uncommitted: `AMENDMENTS.md`,
  `ESCALATION.md`, `TASKS/A-003.md` (all describing D-006 answered, its single-use screenshot grant
  consumed, and A-003 marked complete) plus a new reference PNG for `AlarmDeleteConfirmationCase`.
  Untracked: a first Worker's complete output for most of A-004's scope (`domain/scheduler/`,
  `data/scheduler/AlarmManagerAlarmScheduler.kt`, `data/receiver/AlarmReceiver.kt`,
  `data/notification/AlarmNotifier.kt`, `res/drawable/ic_notification_alarm.xml`, and three test
  files) — `AlarmRepositoryImpl.kt` itself untouched, no Iteration-owned wiring done, `STATE.md`/
  `RESUME.md` still describing the pre-D-006 world. Read as a prior invocation that completed D-006's
  consumption, dispatched a Worker for A-004, and died before the repository-mirroring half, the
  wiring, or any checkpoint. Assessed rather than reverted: `assembleDebug` + `testDebugUnitTest` +
  `lintDebug` + `validateDebugScreenshotTest` run together on the tree as found —
  `BUILD SUCCESSFUL`, 224 tests (0 failures, up from 206), lint 0 errors / 68 warnings, screenshots
  23/23 — proving both halves of the debris coherent and safe to build on. One unrelated stray file
  from an earlier iteration's review pass (`a003_review.diff`, untracked) was found alongside it and
  deleted.
- **Consume decisions (§6.2):** none newly answered this iteration — D-006's consumption was already
  in the debris; `ESCALATION.md` confirmed empty.
- **Dispatch (§6.5):** one Worker at **Capable**, scoped to A-004's three remaining declared files
  (`AlarmRepositoryImpl.kt` MODIFY, `AlarmRepositoryImplTest.kt` MODIFY, `AlarmNotifierConstantsTest.kt`
  CREATE), briefed with the first Worker's output as read-only context (the `FakeAlarmScheduler` /
  `BrokenAlarmScheduler` test doubles it had already written) and every Constraint verbatim. Reported
  back the cancel-then-arm mirroring, nine new repository tests, and one necessary change to the
  private `FakeAlarmDao` (now emulates Room's `autoGenerate`, without which the "post-insert id, not
  zero" assertion is unreachable) — all inside its own declared scope.
- **Scope check (§6.6):** `git status` showed exactly the two MODIFY targets plus the one CREATE.
  Clean.
- **Wire (§6.6):** `injection/SchedulerModule.kt` (new — binds `AlarmScheduler` and `AlarmNotifier`),
  `AppModule.kt` (`includes`), `RepositoryModule.kt` (`alarmScheduler = get()`), `AndroidManifest.xml`
  (the `<receiver>`), `MainApplication.kt` (channel creation at startup), `MainActivity.kt` (opens
  Alarms on a notification tap, AS-6), `README.md` (feature table + package tree).
- **Build/test (§6.7):** combined run — `BUILD SUCCESSFUL`, **239 unit tests** (0 failures, up from
  224), lint **0 errors / 68 warnings** (unchanged), screenshots unaffected (no Compose UI in this
  task).
- **Fresh-Context Review (§6.8):** clean context, Capable, given the combined diff, `DoD.md` criteria
  19/20/21/29/33/34/22/23, and every Constraint verbatim. No Constraint violation. Two MAJOR findings
  (a notification-tap navigation bug that replayed on activity recreation and could double-stack the
  Alarms screen; a missing `VIBRATE` permission that silently dropped half the API-24/25 heads-up
  recipe) and four MINOR findings (public scheduling internals with no external caller; a second,
  DI-invisible `AlarmNotifier` instance; a misleading test comment; a non-guarding method name) — all
  but the last fixed this checkpoint; see `AMENDMENTS.md` A-12. Two more recorded rather than acted on
  (a naming nit; a sub-second re-arm race that is A-005's to reconcile) — see `ISSUES.md`.
  Re-verified after the fixes: `BUILD SUCCESSFUL`, still 239/0, lint unchanged.
- **Reconcile (§6.9):** all six findings classified — four Tier-1 fixes (`AMENDMENTS.md` A-12), two
  recorded-not-acted (`ISSUES.md`). A-004 marked complete; its `human` criteria (22, 23) join the pool
  for the end-of-run Human Verification Request.
- **Outcome:** real progress checkpointed — A-003 completed (carrying forward the salvaged debris),
  A-004 completed and reviewed, four defects fixed. A-005 and A-006 (both depending only on A-004,
  disjoint Declared File Scopes) are the next Phase → `CONTINUE`.

### Iteration 5 — 2026-09-14 — Phase 3 (A-003) implemented and reviewed; D-005 applied; one screenshot grant queued (D-006)

- **Phase:** 3 — A-003 alone, the only executable task (strictly linear chain).
- **Recover (§6.1):** working tree carried one uncommitted change on entry — a human had filled in
  D-005's `## Decision` section in `ESCALATION.md` directly (not run debris; a normal between-iteration
  edit, exactly what §6.2 exists to consume).
- **Consume decisions (§6.2):** **D-005 answered — option 1.** Applied directly (not through a Worker,
  since it touches files outside A-003's Declared File Scope): removed
  `.fallbackToDestructiveMigration(false)` from `DatabaseModule.kt`; corrected the four KDoc blocks that
  asserted the inverted behaviour (`DatabaseModule.kt`, `Migration.kt` ×2, `AlarmEntity.kt`, `Alarm.kt`);
  rewrote Constraint C-13 in `.harness/knowledge/PROJECT.md` to record the resolved state. Blocked
  nothing, so no task was unblocked by it — logged and archived regardless, per §6.2's "every consumed
  decision is logged" rule.
- **Dispatch (§6.5):** one Worker at **Capable**, scoped to A-003's fourteen declared files. All thirteen
  Constraints passed verbatim per ADR-016. The Worker also verified (rather than re-implemented) that
  re-opening and re-saving an alarm already worked correctly from A-002's over-delivery — confirmed by
  reading the actual chain, not assumed.
- **Scope check (§6.6):** `git status` showed exactly the declared CREATE/MODIFY set plus this
  iteration's own D-005 edits. Clean.
- **Wire (§6.6):** the six string keys the Worker reported (`delete_this_alarm`,
  `the_alarm_will_be_removed_permanently`, `alarm_deleted`, `the_alarm_could_not_be_deleted`,
  `the_alarm_could_not_be_changed`, `turn_this_alarm_on_or_off`) added to both `strings.xml` files.
- **Build/test (§6.7):** first combined run — `assembleDebug` / `testDebugUnitTest` / `lintDebug` /
  `validateDebugScreenshotTest` — was `BUILD SUCCESSFUL` with **206 unit tests** (up from 177), lint **0
  errors / 68 warnings** (+2, both pre-existing style warnings on the sibling Note pattern), screenshots
  green.
- **Fresh-Context Review (§6.8):** clean context, Capable, checked the combined diff against every
  Constraint and against `.claude/figma-design-system.md`. No Constraint violation; contrast numbers for
  the new switch verified by computation rather than trusted. Three findings acted on this checkpoint —
  see `AMENDMENTS.md` A-9. Two more recorded rather than acted on: `AlarmDeleteConfirmSheet.kt` duplicates
  `NoteDeleteConfirmSheet.kt` with no shared component (a real concern, but extracting one now would touch
  already-shipped, already-reviewed Note files outside this task's scope — noted in `ISSUES.md` instead of
  acted on); a KDoc line claiming a screenshot case that did not exist yet at review time (resolved by
  adding the case itself, see below).
- **Screenshot pin added this checkpoint:** `AlarmDeleteConfirmationCase` in `AlarmsScreenshotTestKt`,
  matching the `DeleteConfirmation` precedent in `NoteScreenshotTest.kt`. Has **no reference image yet** →
  `ScreenshotImageNotFoundException`, exactly the D-004 situation one Phase earlier. **`AlarmRow` in both
  switch states was deliberately not pinned**: its displayed time is host-dependent
  (`DateFormat.is24HourFormat` + the app's locale), the exact trap `AMENDMENTS.md` A-6 flagged when that
  formatting was added, and pinning it would record a reference valid on one machine's clock format only.
  `assembleDebug` / `testDebugUnitTest` / `lintDebug` were re-verified in a separate invocation once the
  screenshot task started failing (PROJECT.md's "one failing task aborts the others" note) — still green,
  206/0/0 and 0 errors / 68 warnings; 22 of 23 screenshot cases green.
- **Reconcile (§6.9):** the missing reference → **queued decision D-006** (goal-scoped grant, mirroring
  D-004's shape exactly, restricted to this one new test name). A-003 marked **blocked by decision**
  rather than complete — every other Acceptance line is met and machine-evidenced, but this task's own
  Acceptance explicitly required the screenshot pin, so the same standard D-004 set for A-001 applies here.
  No attempt charged; the Worker succeeded on its first try.
- **Outcome:** real progress checkpointed (D-005 applied, A-003 fully implemented and reviewed except for
  the screenshot pin), but A-003 cannot be marked complete and A-004 … A-007 are unreachable behind it
  until D-006 is answered → `ESCALATE`.

### Iteration 4 — 2026-09-14 — Phase 2 (A-002): the alarm vertical works end to end; A-001 closed

- **Phase:** 2 — A-002. A-001 also closed this iteration when D-004 was consumed.
- **Recover (§6.1):** working tree clean — nothing to salvage or revert. But `git log` showed **three
  commits on the Loop Branch that no task produced**, landed between iterations: `7d0c0da` ("uncompleted
  work because run out of quota"), `be95b3f` (alarm strings + an `onTimeChange` default) and `ee7b5c9`
  (dropped the `org.gradle.java.home` pin). `STATE.md` and `RESUME.md` described a repository that no
  longer existed. §6.3's rule decided it: the derived cache is the thing that is wrong. Reconciled as
  `AMENDMENTS.md` A-7 rather than reverted — all three were coherent and none conflicted with the plan.
- **Consume decisions (§6.2):** **D-004 answered — option 1, granted.** Applied, and found already
  satisfied: `7d0c0da` had landed the `AlarmsEmptyStateCase` reference image itself, and
  `validateDebugScreenshotTest` confirmed the case passes. **The grant was therefore never exercised** —
  re-recording a reference that already validates would be a pointless write against an approved
  baseline. Exactly one file sits in that reference directory, as the decision required. A-001 → complete.
- **Orient (§6.3):** `RESUME.md` claimed no task was executable and A-002 was untouched. Git disagreed on
  both counts: twelve of A-002's fourteen CREATE targets already existed. Verified what was genuinely
  missing — the two test files, and **every** piece of Iteration-owned wiring (`AppDatabase` still
  `version = 3` with no `AlarmEntity`, no `MIGRATION_3_4`, no DI bindings, no `alarmEditorFragment` in the
  navigation graph). The feature compiled while being completely unreachable and unpersisted, which is
  why a green `assembleDebug` was not read as "it works".
- **Toolchain re-verified first.** Because `ee7b5c9` removed the JDK pin and iteration 2 had once found no
  JDK at all, the build was re-proved from scratch before anything else: `BUILD SUCCESSFUL`. The pin is
  not load-bearing; that note in `PROJECT.md` is now closed rather than outstanding.
- **Select (§6.4):** one task — A-002. The dependency chain is strictly linear, so no larger phase exists.
- **Dispatch (§6.5):** one Worker at **Capable**, scoped to the three `alarms/` MODIFY targets and the two
  test files, with the twelve pre-existing files named as read-only context rather than as work to redo
  (`AMENDMENTS.md` A-8). All thirteen Constraints passed verbatim per ADR-016.
- **Scope check (§6.6):** `git status` showed exactly the five declared files and nothing else. Clean.
- **Wire (§6.6):** `AppDatabase` → `version = 4` + `AlarmEntity` + `alarmDao()`; `MIGRATION_3_4` with the
  `CREATE TABLE` **copied verbatim** from the generated `AppDatabase_Impl.kt:61` (C-03) — note
  `:app:kspDebugKotlin` is not a granted command, so `assembleDebug` was the route to the generated file;
  DI bindings in all three modules, including `AlarmEditorViewModel`, which had never been registered at
  all and would have crashed the editor on first open; `alarmEditorFragment` + `toAlarmEditor` with a
  `defaultValue` on the argument (C-08); `add_alarm` in both `strings.xml` files (C-02); README.
- **Build/test (§6.7):** all four commands in one invocation → `BUILD SUCCESSFUL`. **177 unit tests, 0
  failures** (up from 138), lint **0 errors** / 66 warnings, screenshots green. No re-dispatch needed.
- **Review (§6.8):** fresh context at Capable, pointed at the twelve unreviewed pre-existing files as well
  as the new work. **No Constraint violation, no blocking finding.** It checked `MIGRATION_3_4` character
  by character against the generated SQL, confirmed no vacuously-passing assertions (C-06) and that
  `FakeAlarmDao` really returns a jumbled list, and traced the round trip through the DI and nav graphs.
  Four non-blocking defects, **all fixed and re-verified**: the row/editor 12-vs-24-hour mismatch (A-6),
  two wrong README tree notes, three stale KDoc claims, and the Save control's unlabelled `clickable`.
- **Reconcile (§6.9):** the review's most serious finding was one this task did not cause —
  `fallbackToDestructiveMigration(false)` **enables** destructive migration; the boolean only chooses
  which tables get dropped. Four KDoc blocks assert the opposite. Cross-checked against Room `2.7.2` in
  `libs.versions.toml` and confirmed. Classified as a **trap**, so written down as Constraint **C-13** in
  this same checkpoint, and the fix queued as **D-005** — it changes upgrade behaviour for every installed
  copy, which is Tier 2, not the engine's to apply. It blocks nothing. C-03's stated premise corrected in
  the same edit, since it had repeated the same inverted claim.
- **Outcome:** `CONTINUE`. A-003 is executable and unblocked.

### Iteration 3 — 2026-09-13 — Phase 1 (A-001) implemented; one screenshot grant queued (D-004)

- **Phase:** 1 — A-001.
- **Recover (§6.1):** working tree was clean except for the pre-existing, expected-dirty files
  (`PRD.md`, `skills-lock.json`, `.claude/`, `.agents/skills/`, `.harness/loop/`, `SUGGESTIONS.html`) —
  none of it run debris. New finding: `gradle.properties` was also already dirty, carrying
  `org.gradle.java.home` pinned to this machine's JDK — not present in iteration 2's checkpoint, not
  written by any task, evidently added by a human between iterations. `./gradlew --version` succeeded on
  first try this iteration, resolving the environment block iteration 2 reported as `FAILED`.
- **Orient (§6.3):** read `RESUME.md`; re-confirmed the toolchain per its instruction; selected Phase 1
  (A-001), the only executable task.
- **Dispatched:** one Worker (Capable tier) for A-001, scoped to its five Declared File Scope files. Wrote
  `AlarmsFragment.kt`, `AlarmsUiState.kt`, `AlarmsViewModel.kt`, `component/AlarmsEmptyState.kt`,
  `ic_bottom_alarm.xml` — all within scope, confirmed via `git status`. Reported two new string keys
  (`alarms`, `no_alarms_yet`) for the Iteration to add.
- **Iteration wiring (§6.6):** `BottomBarDestination.kt` (new `Alarms` entry), `navigation_graph.xml`
  (`alarmsFragment` + `toAlarms`), `ViewModelModule.kt` (`AlarmsViewModel` bound), both `strings.xml`
  (the two reported keys, English + German), `CoreBottomBar.kt` (hide the centre "+" on Alarms, per
  AS-5), `README.md` (package tree + feature table).
- **Build/Test (§6.7):** first combined run surfaced the predicted risk (`PLAN.md` risk 1): the fourth
  tab broke `ThemeScreenshotTestKt/BottomBar*` and `BottomBarSystemNight*`. Re-recorded both under the
  already-consumed D-003 grant via `--tests "*BottomBar*"`; confirmed via `git status` that only those
  two reference PNGs changed, no orphans. Second finding: `lintDebug` failed on an unrelated
  `PropertyEscape` error in `gradle.properties` (the human-added JDK path, unescaped) — fixed directly per
  §6.7 (a failure naming a file in nobody's Declared File Scope is the Iteration's to fix; charged against
  no task's attempts). Third combined run: `BUILD SUCCESSFUL` — assemble, 138 unit tests (0 failures),
  lint (0 errors, 64 warnings), and 20/20 existing screenshot cases including the two re-records.
- **Fresh-Context Review (§6.8):** two findings, both fixed this iteration (see `AMENDMENTS.md` A-5, A-6):
  the acceptance-required `@PreviewTest` case was missing entirely (added `AlarmsScreenshotTest.kt`,
  rendering `AlarmsEmptyState` directly — same pattern as `HomeScreenshotTest`/`HomeNoteList`, so it needs
  no `NavController` and cannot misreport AS-5's hidden-button state as a side effect); and AS-5's
  hide-the-"+"-button check was a hardcoded destination-id comparison inside `CoreBottomBar` rather than a
  property on the destination itself (moved to `BottomBarDestination.hidesCreateButton`). No Constraint
  (C-01 … C-12) violation found. Two minor/non-blocking notes not acted on: `AlarmsUiState.isEmpty` has no
  `else` branch yet (by design — the class's own KDoc names this as what A-002 replaces), and previews of
  the whole screen cannot show the true hidden-button state without a `NavController` (a pre-existing,
  systemic preview limitation, not new to this task).
- **Fourth combined run:** the new `AlarmsEmptyStateCase` screenshot case has no reference image yet →
  `ScreenshotImageNotFoundException`. Everything else stayed green (assemble, 138 tests, lint, and the
  other 20 pre-existing screenshot cases).
- **Reconciled:** the missing `@PreviewTest` case → Tier-1 amendment, fixed directly (A-5), plus a
  `PLAN.md` correction (Phase 1's row now lists `AlarmsScreenshotTest.kt` as Iteration-owned, matching
  every later phase's row). The fragile hide-button check → Tier-1 amendment, fixed directly (A-6). The
  `gradle.properties` `PropertyEscape` failure → fixed directly per §6.7, no attempt charged, flagged (not
  escalated) as a portability concern in `knowledge/PROJECT.md` and `ISSUES.md` since the engine cannot
  write outside its repository working directory to relocate it. The new screenshot case's missing
  reference → **queued decision D-004** (goal-scoped grant, narrower than and separate from D-003, since
  a brand-new case is explicitly the other trigger `PROJECT.md`'s own `updateDebugScreenshotTest` note
  names for an Escalation Request). A-001 marked blocked by decision rather than complete; no attempt
  charged against it — the Worker succeeded on its first try.
- **Outcome:** real progress checkpointed (A-001 fully implemented, reviewed, and evidenced except for
  one screenshot reference), but A-001 cannot be marked complete and nothing downstream is selectable
  until D-004 is answered → `ESCALATE`.

### Iteration 2 — 2026-09-13 — decisions consumed; execution found broken (no JDK)

- **Phase:** none — §6.2 consumed all three queued decisions before any Phase could be selected; §6.3
  Orient then found execution itself broken.
- **Consumed (§6.2):** D-001, D-002, D-003, all answered 2026-09-13 per iteration 1's `RESUME.md`. Applied:
  DoD approved and now immutable; AS-4 kept; the fourth-tab/hidden-centre-"+" reading of AS-5 confirmed;
  the goal-scoped `updateDebugScreenshotTest` grant confirmed. Logged as amendments A-2, A-3, A-4 in
  `AMENDMENTS.md`. Full exchanges archived below. Cleared `Blocked by decision` on all seven task files
  (A-001 … A-007) and set each to `pending`. `ESCALATION.md` reset to its empty template — nothing is
  queued.
- **Orient (§6.3):** per iteration 1's `RESUME.md` instruction, ran `./gradlew --version` before selecting
  a Phase. It failed: `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.`
  `gradle.properties` sets no `org.gradle.java.home` either, so nothing in the repository can locate a
  JDK. This is not the capability gate from D-001 — that grant is installed and verified present in
  `.harness/knowledge/capabilities.json` — it is the environment underneath the grant. Confirming this
  further (locating a system JDK, checking `PATH`) requires commands (`where java`, reading `JAVA_HOME`)
  outside every granted capability, so the engine stopped rather than working around its own scope.
- **Reconciled:** three queued decisions → consumed and applied (above). The broken build tool → **not**
  a new queued decision. Iteration 1's `RESUME.md` already predicted this exact outcome and pre-answered
  it: *"if Gradle is still denied then, that is `FAILED` (execution broken), not a queued decision — the
  question has been answered and the ledger is on disk."* No task's attempts are charged — no Worker was
  dispatched, because no Phase was ever selected.
- **Outcome:** the decisions are consumed and the checkpoint is real progress, but no task could be
  attempted because the toolchain itself does not run in this environment → `FAILED`. Human repair
  needed: install a JDK and either put it on `PATH`/set `JAVA_HOME`, or set `org.gradle.java.home` in
  `gradle.properties`.

### Iteration 1 — 2026-09-13 — bootstrap recovery and completion

- **Phase:** none — no implementation task was executable, and none was attempted.
- **Attempted:** finishing an interrupted Bootstrap. `.harness/run/` existed on entry holding exactly four
  files — `DoD.md`, `PLAN.md`, `TASKS/A-001.md`, `TASKS/A-002.md` — all untracked, with no `STATE.md`. No
  `STATE.md` means no Iteration had ever run, so this was crashed bootstrap debris rather than a crashed
  iteration.
- **Recovery decision (§6.1):** salvaged, not reverted. The four documents are internally consistent, refer
  to the same constraint ids (C-01…C-12) and the same decision id (D-001), and both task files declare
  themselves blocked by a D-001 that had never been written. Reverting would have thrown away the whole
  bootstrap analysis fan-out to re-derive the same conclusions. Written up as an assumption in `STATE.md`,
  because it is a judgement about work this process cannot see.
- **Authored this iteration:** `TASKS/A-003.md` … `TASKS/A-007.md`, `ESCALATION.md` (D-001, D-002, D-003),
  `STATE.md`, `RESUME.md`, `AMENDMENTS.md`, this file, and `.harness/ISSUES.md`.
- **Learned:**
  - The engine holds **no build, test or lint capability in this repository at all**. The previous run's
    standing ledger is at `knowledge/capabilities.json`; the runtime reads `.harness/knowledge/capabilities.json`,
    which does not exist. Confirmed by inspection, not inferred: `.harness/knowledge/` contains only
    `PROJECT.md`. This was already written down as `PLAN.md` risk 7 and is now half of D-001.
  - `PROJECT.md` claims `.harness/ISSUES.md` exists and cites it as evidence for C-01's "43 surviving colour
    literals". It did **not** exist. The file is created this iteration and carries that entry forward from
    the previous run's `knowledge/ISSUES.md` rather than dropping it.
  - `PLAN.md` put `.harness/ISSUES.md` inside A-007's Declared File Scope, which collides with the
    Iteration's own §6.10 obligation to regenerate that file in every checkpoint. Amended (A-1).
- **Reconciled:**
  - capability gap → queued decision (D-001), blocking all seven tasks
  - AS-5, the one assumption the bootstrap flagged as genuinely uncertain → queued decision (D-002)
  - the predicted reference-image invalidation → queued decision (D-003), blocking A-001's completion only
  - A-007 scope collision → Tier-1 amendment (A-1), `PLAN.md` corrected
  - missing `ISSUES.md` → created, not escalated
- **Outcome:** no executable task remains and three decisions are queued → `ESCALATE`. This is the DoD
  gate firing naturally, which is what §5 predicts for the end of a Bootstrap.

## Archived Decisions

<!-- Full request + decision + rationale of every consumed Decision Queue entry. -->

### D-001 — Approve the Definition of Done, and re-install the toolchain capabilities

- **Type:** DoD approval + Capability grant | **Queued:** iteration 1, 2026-09-13 | **Consumed:** iteration 2
- **Blocked:** A-001 … A-007 (every task in the run)
- **Question:** Approve `.harness/run/DoD.md` (wording, the `machine`/`human` split, and the Assumptions
  table AS-1…AS-11 — AS-4 and AS-10 flagged as expensive to reverse), and grant the standing toolchain
  capabilities (gradlew assemble/compile/test/lint, `validateDebugScreenshotTest`, and the
  `MSYS_NO_PATHCONV` git-show/ls-tree workaround) so the engine can build, test and lint at all.
- **Context:** The addendum is three sentences; the DoD turns it into 36 criteria (19 `machine`, 17
  `human`) because this repository has no emulator, device, `adb`, or Robolectric. The engine held zero
  build capability — the previous run's ledger lived at `knowledge/capabilities.json`, the runtime now
  reads `.harness/knowledge/capabilities.json`, which did not exist.
- **Options:** (1) approve as written; (2) approve but reclassify some `human` criteria as `machine` —
  risks a false `DONE`; (3) cut AS-4 now, since adding it later costs a second hand-written migration
  (C-03); (4) grant a narrower capability set — weakens every task's completion gate.
- **Engine recommendation:** option 1, with the capability proposal approved as written; keep AS-4.
- **Decision:** Approved as written — option 1. Keep AS-4. Grant all three proposed capability blocks to
  `.harness/knowledge/capabilities.json` as specified. `updateDebugScreenshotTest` stays withheld from the
  standing ledger per the 2026-09-11 withdrawal — granted separately and narrowly in D-003 only.

### D-002 — Where does the Alarms screen live, and what does the bottom bar's centre "+" do there?

- **Type:** Tier 2 (plan/architecture) | **Queued:** iteration 1, 2026-09-13 | **Consumed:** iteration 2
- **Blocked:** A-001, A-002, A-006
- **Question:** AS-5 assumes Alarms is the fourth bottom-bar tab with the centre "+" hidden there and a
  dedicated FAB creating the alarm. Confirm or redirect.
- **Context:** `CoreBottomBar(onCreateNote: () -> Unit)` has no default deliberately — a new top-level
  screen must state what the centre button means there. A fourth tab worsens the existing German
  truncation and invalidates two committed reference images (see D-003).
- **Options:** (1) fourth tab, centre "+" hidden on Alarms, dedicated FAB (AS-5 as written); (2) fourth
  tab, centre "+" becomes "new alarm" there, no FAB — contradicts the addendum's explicit FAB ask; (3)
  fourth tab, centre "+" keeps meaning "new note" everywhere, FAB sits beside it — two create controls
  inches apart; (4) not a tab at all, reached from Settings/overflow — cheapest, but buries a
  daily-use feature.
- **Engine recommendation:** option 1 — the only reading honoring the addendum's explicit FAB ask without
  two create controls on one screen.
- **Decision:** Option 1 — fourth tab; centre "+" hidden on Alarms; a dedicated FAB creates the alarm.
  Accept the consequences: `CoreBottomBar` needs the small change and the two bottom-bar reference images
  need re-recording (D-003).

### D-003 — A goal-scoped grant to re-record the two bottom-bar reference images

- **Type:** Capability grant | **Queued:** iteration 1, 2026-09-13 | **Consumed:** iteration 2
- **Blocked:** A-001's completion only (its implementation could proceed once D-001/D-002 were answered)
- **Question:** Grant `./gradlew :app:updateDebugScreenshotTest`, goal-scoped, to re-record
  `ThemeScreenshotTestKt/BottomBar_*` and `BottomBarSystemNight_*` after the fourth tab changes the bar's
  layout.
- **Context:** A fourth `BottomBarDestination` entry moves every tab slot, so both references mismatch in
  a file no task opens. `updateDebugScreenshotTest` is deliberately withheld from the standing ledger
  (granted 2026-09-10, withdrawn 2026-09-11) because it is the one command that can make a red screenshot
  test disappear by re-recording wrong output as correct.
- **Options:** (1) grant it, goal-scoped, with a required before/after live-hash report restricted to the
  two named files; (2) refuse — human re-records manually, run reports `ESCALATE` at the end of Phase 1;
  (3) refuse outright and accept criterion 2 as permanently failing — would need D-001 reopened, a Tier-3
  change; (4) grant it standing — re-requests the withdrawn 2026-09-10 grant.
- **Engine recommendation:** option 1 if the run should proceed unattended past Phase 1; option 2 if the
  human would rather sit at that gate. The engine flagged this as the shape a wrongly-motivated request
  would take, distinguished only by the mismatch being predicted before any code was written and the two
  filenames being named in advance.
- **Decision:** Grant it, goal-scoped — option 1. Re-record only `BottomBar_*` and `BottomBarSystemNight_*`;
  report the live-hash list before and after. Lives only in `.harness/run/capabilities.json`, expires with
  the run, never copied into the standing ledger.

### D-004 — A goal-scoped grant to record one brand-new screenshot reference: the Alarms empty state

- **Type:** Capability grant | **Queued:** iteration 3, 2026-09-13 | **Consumed:** iteration 4
- **Blocked:** A-001's completion only. Every other task (A-002 … A-007) was unreachable regardless, since
  each depends on A-001 directly or transitively and none was executable until it was marked complete.
- **Question:** Grant `./gradlew :app:updateDebugScreenshotTest --tests "*AlarmsEmptyStateCase*"`,
  goal-scoped, to record the reference image for the **new** `AlarmsScreenshotTestKt.AlarmsEmptyStateCase`
  case, added iteration 3 — see `AMENDMENTS.md` A-5.
- **Context:** Not the D-003 situation (an existing, previously-approved reference invalidated by a layout
  change) — a case with **no reference at all yet**, which `.harness/knowledge/PROJECT.md`'s own
  `updateDebugScreenshotTest` note calls out as needing an Escalation Request exactly like an intentional
  change does: *"a new state with no reference yet."* Running the existing D-003 grant against this case
  would have exceeded its named scope (`BottomBar_*`/`BottomBarSystemNight_*` only).
  Without this grant, `validateDebugScreenshotTest` fails with `ScreenshotImageNotFoundException` on this
  one case and DoD criterion 2 stays red for a reason unrelated to whether the empty state is correct —
  everything else about Phase 1 was green: `assembleDebug`, all 138 unit tests, `lintDebug` (0 errors), and
  the other 20 screenshot cases including the two `BottomBar*` re-records from D-003.
- **Options:** (1) grant it, goal-scoped to exactly this one test name, with a before/after live-hash
  report for `AlarmsScreenshotTestKt` restricted to that file's directory; (2) refuse — a human records it
  manually and the run reports `ESCALATE` until that reference appears on disk; (3) drop the
  `@PreviewTest` annotation and keep the composable as a plain `@Preview` — not recommended, listed only
  because it needs no grant.
- **Engine recommendation:** option 1. The case is small (one component, one state, `heightDp = 320` per
  C-05), the render is deterministic (no locale-formatted date or time), and a human looking at one
  rendered "No alarms yet" screen before approving is a cheap check against the same class of risk
  `updateDebugScreenshotTest` always carries.
- **Decision:** Option 1 — granted, goal-scoped to exactly `*AlarmsEmptyStateCase*`. Added as a separate
  entry in `.harness/run/capabilities.json` distinct from the D-003 entry, since this is a first recording
  rather than a re-record of an already-approved baseline. Report the before/after state of
  `AlarmsScreenshotTestKt`'s reference directory (empty before, one file after).
- **Applied (iteration 4) — and the grant was never exercised.** Between iterations, commit `7d0c0da`
  landed the reference image itself. This iteration ran `validateDebugScreenshotTest` and the case
  **passed**, which is the evidence the grant was meant to unlock — so `updateDebugScreenshotTest` was
  never run. Exactly one file existed in that reference directory, as the decision required. The grant
  stayed in `capabilities.json` unused and expires with the run.

### D-005 — `fallbackToDestructiveMigration(false)` silently wipes the database on a missing migration path. Change it?

- **Type:** Architecture / behaviour change | **Queued:** iteration 4, 2026-09-14 | **Consumed:** iteration 5
- **Blocks:** nothing. Every remaining task (A-003 … A-007) was executable while this sat unanswered, and
  A-002 shipped correctly regardless — `MIGRATION_3_4` is written and registered, so the 3→4 upgrade path
  is real and does not take the destructive branch. This was about the **next** version bump, not that one.
- **Question:** `injection/DatabaseModule.kt:29` called `.fallbackToDestructiveMigration(false)`. Should
  that call be **removed**?
- **Context:** Found by the Fresh-Context Review in iteration 4 and confirmed against the Room version in
  `gradle/libs.versions.toml` (`2.7.2`). In Room 2.7 the no-arg overload was deprecated and replaced by
  `fallbackToDestructiveMigration(dropAllTables: Boolean)`. **Calling the method enables destructive
  migration**; the boolean only selects whether all tables are dropped or only Room-owned ones. The
  behaviour everyone believed was in force — throw when no migration path exists — is what you get by
  **not calling it at all**. Four KDoc blocks in the repository asserted the opposite
  (`DatabaseModule.kt:14-17`, `Migration.kt` twice, `AlarmEntity.kt`, `Alarm.kt`). Recorded as Constraint
  **C-13** in iteration 4's checkpoint so the trap was written down whichever way this was answered.
  Concrete failure it allowed: a later task bumps `version` to 5 and forgets `addMigrations(MIGRATION_4_5)`.
  Room drops and recreates `alarms`, `notes`, `posts` and `user_actions`. The app opens clean, no crash, no
  log line — and every alarm and note the user owned is gone. No build, test, lint or fresh-install QA pass
  can detect this, because a fresh install has nothing to lose.
- **Options:** (1) **remove the call**, restoring Room's default — a missing migration throws
  `IllegalStateException` at launch instead of wiping. Costs: an incomplete migration becomes a hard crash
  rather than a silent reset, an interruption rather than a convenience during development; (2) **keep it
  and fix only the four comments**, accepting silent data loss as deliberate policy for a single-developer
  skeleton app with no shipped users yet; (3) keep it for debug builds and remove it for release — more
  code, and this repo has no build-type split in `DatabaseModule` today.
- **Engine recommendation:** option 1. This app's whole point is to be copied into other projects, and the
  comments claiming safety travel with the copy. An app that silently empties a user's alarms on an upgrade
  is the worst of the three outcomes, and the crash option 1 produces is exactly the signal that prevents
  it. The interruption cost lands on a developer who already made a mistake; the data loss lands on a user
  who did not.
- **Decision:** Option 1 — removed the `.fallbackToDestructiveMigration(false)` call from
  `DatabaseModule.kt`, restoring Room's default. Accepted per the engine's own recommendation. The four
  KDoc blocks were corrected in the same checkpoint, and `AMENDMENTS.md` / `.harness/knowledge/PROJECT.md`
  Constraint C-13 were updated to reflect the resolved state rather than just the trap.
- **Applied (iteration 5).** `injection/DatabaseModule.kt`'s `.fallbackToDestructiveMigration(false)` call
  removed; the four KDoc blocks corrected to state Room's restored default; Constraint C-13 in
  `.harness/knowledge/PROJECT.md` rewritten to record the resolved state. Verified: `assembleDebug` still
  reaches `BUILD SUCCESSFUL` with the call gone.
