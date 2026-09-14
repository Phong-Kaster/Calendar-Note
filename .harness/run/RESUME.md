# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** executing → verification. **All seven tasks (A-001 through A-007) are complete.** Nothing
  abandoned, nothing deferred, no queued decision outstanding. `STATE.md` records `DONE-candidate: yes`.
- **The next invocation is the Verifier (ENGINE.md §11).** It wrote none of this implementation and must
  distrust all of it: re-run and re-read fresh evidence for every `machine` criterion rather than trusting
  the numbers below, which are this iteration's own and not yet independently re-proved.
  1. Re-verify all four commands together: `./gradlew :app:assembleDebug :app:testDebugUnitTest
     :app:lintDebug :app:validateDebugScreenshotTest`. Expected, from this iteration's own run:
     `BUILD SUCCESSFUL`, 251 tests / 0 failures, lint 0 errors / 72 warnings, 23/23 screenshot cases.
  2. Re-check DoD criteria 3, 12, 33, 34, 36 by reading the source directly (version numbers, navigation
     graph, grep for colour literals and hardcoded strings, README content) — see `DoD.md` § Verification
     Evidence Required for exactly what each asks for.
  3. If every `machine` criterion holds and nothing is abandoned or deferred: raise the **Human
     Verification Request** (§11) for all sixteen unsigned `human` criteria — do **not** report `DONE`
     yet. Report `ESCALATE`.
     - Criteria: **4** (v3→v4 install migrates without crashing), **8** (Alarms tab findable), **9** (long
       list scrolls, last row fully visible), **10** (empty state reads as empty), **11** (long message
       overflows cleanly), **13** (FAB visible, opens editor), **16** (editor usable with keyboard up),
       **18** (alarm survives force-stop), **22** (alarm actually fires as a heads-up popup, repeats daily),
       **23** (tapping the notification opens Alarms without stacking a second app instance), **25**
       (tapping a row opens it pre-filled), **28** (delete control findable, confirming button
       unmistakable), **30** (on/off switch reads correctly at a glance), **31** (permission-missing banner
       shows the right fix and clears itself), **32** (alarm survives a reboot with the app never opened),
       **35** (both new screens look like they belong to this app — black ground, product blue, no
       Material-default lilac).
     - Each item's exact steps are already written in `DoD.md` § Verification Evidence Required — copy
       them into the request rather than re-deriving "check the UI looks right" from scratch.
  4. Only once a person has signed off every item (recorded with the date in `STATE.md` § Human sign-offs)
     does a later invocation create the Cleanup Commit (remove `.harness/run/`, keep `.harness/ISSUES.md`)
     and report `DONE`.
- **Abandoned:** none. **Unreachable:** none. **Queued decisions:** none outstanding — D-001 through D-007
  are all consumed; see `AMENDMENTS.md` for the full trail.
- **Verified commands** — build/test/lint/screenshots run together this iteration, twice (once before this
  iteration's own review fix, once after): build `./gradlew :app:assembleDebug` | test
  `./gradlew :app:testDebugUnitTest` | lint `./gradlew :app:lintDebug` | screenshots
  `./gradlew :app:validateDebugScreenshotTest`. **251 unit tests, 0 failures; lint 0 errors, 72 warnings;
  23 of 23 screenshot cases green** — all 23 references are now committed (the `AlarmsPermissionNoticeCase`
  reference D-007 recorded was untracked until this checkpoint's commit).
  - `:app:kspDebugKotlin` is **not** in the granted capability set — `assembleDebug` is how you reach
    Room's generated `AppDatabase_Impl.kt`.
  - `updateDebugScreenshotTest` stays ungranted except via a named goal-scoped entry in
    `capabilities.json`. D-003, D-004, D-006 and D-007's entries are all spent. No further screenshot case
    is expected in this run — there is no more code to write.
- **Model tiers (resolved from `.harness/loop/models.json`):** fast: `haiku` | capable: `opus`
- **Every task in this run was Capable.** No Fast dispatch exists in `PLAN.md`.
- **Closed, not outstanding — do not re-raise:** the `org.gradle.java.home` pin question; D-005
  (`fallbackToDestructiveMigration` — call removed, Constraint C-13 records the resolved state); D-006 and
  D-007 (single-use screenshot grants, both consumed and spent).
- **This iteration (8) consumed D-007 and completed the run's last two tasks.** D-007's goal-scoped grant
  was applied (one invocation of `updateDebugScreenshotTest --tests "*AlarmsPermissionNoticeCase*"`,
  before/after state reported in `ESCALATION.md`), unblocking A-006's completion. A-007 then ran as a
  single-task Phase: a Worker reconciled `.harness/knowledge/PROJECT.md` with the tree (new Constraints
  C-16, C-17), and a Fresh-Context Review found one MAJOR (an overstated testability claim in the C-06
  edit, contradicting 85 existing tests and C-17's own citation) and one MINOR (a stale "four constants"
  comment) — both fixed by the Iteration. See `AMENDMENTS.md` A-15 and A-16.
- **Watch for commits made outside the loop.** None since iteration 4 (`ee7b5c9`). Cheap to check with
  `git log` against the last `loop(...)` commit before trusting either this file or `STATE.md`.
- **Expected dirty paths** (not run debris — do not salvage or revert in §6.1): `skills-lock.json`,
  `.claude/`, `.agents/skills/`, `.harness/loop/`, and `SUGGESTIONS.html`, which has been dirty since
  before this run began.
