# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient**.

### Iteration 1 - 2026-09-15 (bootstrap)

- **Phase:** none (bootstrap)
- **Attempted:** Bootstrapped a new run for the "daily greeting notification + permission clean-up" PRD.
  `.harness/run/` did not exist (the previous alarms run's state was cleared in commit `736d762` to make
  way for this goal). Read `PRD.md`, `.harness/knowledge/PROJECT.md`, and the relevant source files
  (`SettingDatastore.kt`, `AlarmNotifier.kt`, `MainActivity.kt`, `MainApplication.kt`,
  `HomePermissionBottomSheet.kt`, `HomeRequestPermission.kt`, `HomeFragment.kt`, `PermissionUtil.kt`,
  `AndroidManifest.xml`, `SchedulerModule.kt`, `SettingRepository.kt`/`Impl`, `DatastoreModule.kt`).
  Dispatched five Capable-tier `loop-analyst` roles in two waves: Wave 1 (parallel) — a conventions
  survey, a DoD-criteria proposal, and a task-decomposition proposal; Wave 2 (parallel, arm's-length) — a
  critique of Wave 1's DoD verification classes and decomposition, and a conflict-analysis/Model-Tier
  role that verified file-scope disjointness and classified both tasks against the Fast-tier test in
  `POLICIES.md`.
- **Learned:**
  - `androidx.lifecycle:lifecycle-process` (`ProcessLifecycleOwner`) is **not** a dependency this app
    has, even though `.claude/viewmodel-layer.md` documents that pattern — adding it would violate the
    PRD's "no new libraries." The foreground signal is `MainActivity.onStart()` instead.
  - The once-per-day decision must be plain Kotlin behind an injected `Clock` (Constraint C-06): unit
    tests here run against a stub `android.jar` (`isReturnDefaultValues = true`) where
    `NotificationManager`/`NotificationChannel`/`PendingIntent`/`Intent` silently return defaults, so a
    test aimed at any of them would pass vacuously.
  - `onStart()` fires on every configuration recreation, not only a genuine foreground return — two close
    calls could race and both post unless the check-and-write sequence is serialized with a `Mutex`.
  - A greeting notification channel is a one-shot, permanently-frozen decision (Constraint C-10) and
    belongs beside the existing `AlarmNotifier` channel-creation call in `MainApplication.onCreate` —
    which makes `MainApplication.kt` a shared/integration file the Iteration must wire itself, not either
    Worker.
  - PRD's stated reason for removing `VIBRATE` ("no `Vibrator` usage") is literally true but incomplete:
    `AlarmNotifier.kt` independently calls `.setVibrate()`/`channel.enableVibration()`. The PRD
    pre-decided the removal anyway and explicitly asked the run not to stop and ask about it — recorded
    as a known risk (DoD criterion 23, human-verified) and an assumption in `STATE.md`, not queued as a
    decision.
  - `res/values/strings.xml` + `res/values-de/strings.xml` (Constraint C-02), `injection/AppModule.kt`
    (its `includes(...)` list), and `README.md`'s package tree are also Iteration-owned, shared across
    both tasks.
  - No screenshot test renders `HomePermissionBottomSheet` or `HomeRequestPermission`, and
    `AlarmsPermissionNotice` (the component the PRD says must not change) lives in a different file
    entirely and is already pinned by its own screenshot reference — so criterion 13 can be machine-graded
    rather than left to a person.
  - Both tasks independently classified **Capable** tier by the arm's-length conflict-analysis role,
    against the literal three-part Fast-tier test in `POLICIES.md`: both touch shared/integration-adjacent
    files, neither's acceptance is a purely mechanical check, and both carry a small architecture decision
    (a frozen-forever channel choice; a public composable signature change).
- **Reconciled:** No decision needed beyond the standing DoD-approval gate — every ambiguity the fan-out
  surfaced (channel id/importance, stored-date format, whether to delete `isLocationGranted`, whether the
  greeting write should be gated on successful notify) was resolved by the Iteration itself and written
  into the task files as instructions, since none of them rises to Tier 2 (no architecture that
  contradicts the PRD, no missing product information the PRD didn't already answer). Queued **D-001**:
  approve the Definition of Done and the (empty) proposed standing capabilities — no new capability is
  needed this run; the existing standing ledger already covers build/test/lint/screenshot-validate and
  the `git show` Windows workaround.

### Iteration 2 - 2026-09-15

- **Phase:** 1 (T-001, T-002 — the whole task graph)
- **Attempted:** Consumed D-001 (see Archived Decisions below): revised `DoD.md`, `PLAN.md`,
  `TASKS/T-002.md`, `STATE.md`, `ESCALATION.md` to keep `android.permission.VIBRATE`. Both tasks became
  selectable; dispatched one Capable-tier (`opus`) `loop-worker` per task in parallel, attempt 1/3 each.
  T-001 wrote `domain/greeting/GreetingDecision.kt` (+ test), `data/notification/GreetingNotifier.kt` (+
  test), `injection/NotificationModule.kt`, and edited `SettingDatastore.kt`/`SettingRepository.kt`/
  `SettingRepositoryImpl.kt`/`MainActivity.kt`. T-002 edited `AndroidManifest.xml`,
  `HomeRequestPermission.kt`, `HomePermissionBottomSheet.kt`, `HomeFragment.kt`, `PermissionUtil.kt`.
  Verified both Workers' reported file sets were pairwise disjoint, each contained in its own Declared
  File Scope, and the union matched `git status` — no scope violation. Wired the shared/integration files
  myself: added `notificationModule` to `AppModule`'s `includes(...)`, called
  `GreetingNotifier.createChannelIfNeeded()` from `MainApplication.onCreate`, added the two new string
  keys (`daily_greeting`, `hello_what_will_you_write_today`) and removed the two keys T-002 made dead
  (`location`, `allow_location_to_help_you`) in both `res/values/strings.xml` and `res/values-de/strings.xml`,
  and updated `README.md`'s package tree and feature table. Ran one invocation of `:app:assembleDebug
  :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest`: `BUILD SUCCESSFUL`. Dispatched a
  Capable-tier (`opus`) `loop-reviewer` with a clean context against the full diff, the DoD criteria, house
  style, and every Constraint in `.harness/knowledge/PROJECT.md` — verdict **APPROVE**, three non-blocking
  findings.
- **Learned:**
  - `.harness/knowledge/PROJECT.md`'s cached "251 tests across 15 classes" baseline was already stale
    before this run started — the tree held 272 tests across 16 classes at the start of this iteration.
    Corrected the cache to this iteration's own fresh measurement (288 tests / 18 classes) and noted the
    cache is a floor for a *future* run's own measurement, not a diff base.
  - `GreetingNotifier`'s default `Clock.systemDefaultZone()` binds the timezone once, at Koin singleton
    construction time — a device timezone change mid-process would misjudge "today" until the process
    restarts. Same pattern this codebase's other stores already use (e.g. `NoteRepositoryImpl`); accepted
    as a pre-existing, self-healing limitation rather than fixed in this run.
  - `SettingDatastore.kt`'s flows (including the new `lastGreetedDateFlow`) have no `.catch { IOException
    -> emptyPreferences() }` guard that `.claude/repository-layer.md` documents as required — a
    repo-wide, pre-existing gap, not something this run introduced. Recorded for a future task.
  - `HomeRequestPermission.kt`'s private `shouldShowRequestPermissionRationale` was already an orphan
    before this run's diff (confirmed via `git diff`, not just the Worker's own claim) — `requestLocation()`
    used Accompanist's `.shouldShowRationale` instead, never this function. Not this run's to remove.
- **Reconciled:** No new decision needed. All three review findings are non-blocking and classified "no
  action, recorded" (ENGINE.md §8) — none breaks a DoD criterion, one is inherent to an established
  codebase pattern, one is a pre-existing repo-wide gap out of scope for either task, one is pre-existing
  dead code neither task touched. Both tasks completed on attempt 1/3, nothing abandoned. All 14 `machine`
  DoD criteria appear satisfied by this iteration's own evidence; recorded **DONE-candidate: yes** in
  `STATE.md` per ENGINE.md §6.11 (reporting `CONTINUE`, not `DONE` — this invocation wrote code). The next
  invocation is the Verifier (§11): re-prove every `machine` criterion fresh, then raise a Human
  Verification Request for the seven unsigned `human` criteria (15-21).

## Iteration 3 — 2026-09-15 — Final Verification (ENGINE.md §11)

- **Role:** Verifier. This invocation wrote none of the implementation and distrusted all of it.
- **Recovered:** working tree carried one modified file, `.harness/TELEMETRY.tsv` — a Runtime artifact, not
  implementation debris. Nothing to salvage, nothing to revert.
- **Decisions consumed:** none new. `DECISIONS.md` holds only `D-001`, already answered, applied and
  archived in iteration 2; its `ESCALATION.md` entry is marked `answered`. Nothing to re-apply.
- **Re-proved every `machine` criterion against its own fresh evidence** — all fourteen, each checked
  explicitly, none accepted on iteration 2's word. Per-criterion table with the exact file/line or command
  behind each one is in `STATE.md` § Machine verification. Headline figures: `BUILD SUCCESSFUL`; 288 unit
  tests across 18 classes with 0 failures and 0 errors (above the DoD's 251 floor); lint 0 errors / 75
  warnings; screenshot validation 23 of 23 with 23 reference files on disk and no orphans; source **and**
  merged manifest carry neither location permission while keeping `VIBRATE` per D-001; zero hits for all
  nine location symbols across `app/src/main/java`; `AlarmNotifier.kt`, `AlarmNotifierConstantsTest.kt` and
  everything under `ui/fragment/alarms/` absent from the run's diff (`git diff 3506b33 HEAD`).
- **Gaps found:** none. The DONE-candidate flag stands.
- **Discovery (knowledge update, ENGINE.md §8).** The first verification run returned `BUILD SUCCESSFUL`
  with `1 executed, 60 up-to-date` — Gradle's incremental cache, not a re-execution, which is not what §11
  means by fresh evidence. `--rerun` was the obvious remedy and only half worked: it re-ran `lintDebug` and
  `validateDebugScreenshotTest` but left `:app:testDebugUnitTest` `UP-TO-DATE` (`2 executed, 42
  up-to-date`), so the counts in `TEST-*.xml` were still the *previous* invocation's — the same shape of
  trap as the aborted-task one already recorded, arriving through a flag that looks like it ruled the trap
  out. `./gradlew :app:testDebugUnitTest --rerun-tasks` genuinely re-executed them (`28 actionable tasks:
  28 executed`), and that run is where the 288/0 figure comes from. Written to
  `.harness/knowledge/PROJECT.md` § Toolchain in this same checkpoint, since a future Verifier asked to
  "re-prove fresh" would otherwise walk straight into it. No new capability needed — both flags fall under
  the standing ledger's `Bash(./gradlew :app:testDebugUnitTest*)` wildcard.
- **Queued D-002 — Human Verification Request** for the seven unsigned `human` criteria (DoD 15-21), as a
  numbered checklist giving what to open, what to do and what to expect for each, written to be followed
  without reading any code. It names what it blocks: the `DONE` report itself, no task remaining.
  `SUGGESTIONS.html`'s Escalate tab regenerated from `ESCALATION.md` in the same step, before the status
  was written.
- **Not done, deliberately:** no Cleanup Commit, no `DONE`. ENGINE.md §11.3 forbids both while a `human`
  criterion is unsigned. The engine has no device, no emulator and no Robolectric here, so reporting `DONE`
  on criteria 15-21 would be a claim it has no standing to make — and this repository has already shipped a
  correctly-wired, invisible delete button with every test green.
- **Reported:** `ESCALATE` — no executable task remains and a decision is queued (§6.11).

## Iteration 4 — 2026-09-15 — a human found the bug every green test had missed

- **Recovered:** nothing to recover. The working tree held only `.harness/TELEMETRY.tsv` (the Runtime's)
  and `.harness/run/DECISIONS.md` (the human's answer). No crash debris, no source change since the
  `loop(verify)` commit — confirmed with `git diff --name-only 88f2151 -- app/`, which was empty.
- **Consumed D-002.** Criteria 16, 17, 21 signed; **15 FAIL**; 18, 19, 20 not answered and carried
  forward. The human also corrected their own earlier evidence, which is the part worth remembering: the
  pass previously recorded on criterion 15 came from a device where `POST_NOTIFICATIONS` was already
  granted before the first open. That is the easy branch. Criterion 15 describes the other one.
- **Cleared the DONE-candidate** iteration 3 had set. A failed acceptance criterion is not something
  candidacy survives, and clearing it is what let this invocation continue past §6.3 into an ordinary
  Iteration instead of verifying a run that was no longer complete.
- **Reconciled (§8):** the failure is a Tier-1 discovery — cause known, inside the existing design, no
  human input needed. Filed as **T-003** and Phase 2 (`AMENDMENTS.md` A-15). No `DoD.md` change: the
  criterion was right and the code was wrong.
- **Phase 2, one Worker, Capable tier.** Recorded the greeting day only on delivery; pulled the sequence
  into `domain/greeting/GreetOnceADay.kt` as plain Kotlin so it could be tested at all; wired
  `HomeFragment`'s empty `onNotificationGranted` hook so the greeting arrives in the same session as the
  grant instead of on the next app open. Scope check passed: the reported file set, the Declared File
  Scope and `git status` all named the same four files.
- **Evidence:** `BUILD SUCCESSFUL`, no `UP-TO-DATE` marker on any of the four tasks. 296 tests across 19
  classes, 0 failures (was 288/18). Lint 0 errors, 75 warnings. Screenshots 23/23, no orphans.
- **Fresh-Context Review:** APPROVE, zero blocking findings. Two of its three non-blocking findings were
  wired by the Iteration in this same checkpoint (A-16): a KDoc in `GreetingDecisionTest.kt` that still
  claimed to mirror the shipped sequence — the drift hazard was the *claim*, and left standing it would
  have invited a future reader to "restore consistency" by deleting the very guard T-003 added — and
  `README.md`'s package tree, which had not learned about the new file.
- **Learned, and written down as Constraint C-18:** `NotificationManagerCompat.notify()` does not report
  the failure that matters. Without `POST_NOTIFICATIONS` on Android 13+ it accepts the notification, shows
  nothing, and returns normally — so the `catch (SecurityException)` that looks like careful error
  handling catches essentially nothing, and writing "done" immediately after posting records something
  that did not happen. Recorded in the same checkpoint as the fix, not the next one.
- **Reported:** `CONTINUE`. A DONE-candidate is recorded, but this invocation wrote the implementation and
  may not certify it (invariant 8). The next one is the Verifier.

## Iteration 5 — 2026-09-15 — Final Verification (ENGINE.md §11), second pass

- **Role:** Verifier. This invocation wrote none of the implementation — no Worker was dispatched, no
  source file was touched, and the only files it changed are `.harness/` bookkeeping and
  `SUGGESTIONS.html`. It distrusted iteration 4's evidence table on principle and re-measured everything.
- **Recovered:** working tree clean apart from `.harness/TELEMETRY.tsv`, which the Runtime appends to on
  every invocation. Not debris (already noted in `PROJECT.md`); left alone.
- **Decisions consumed:** none. `DECISIONS.md` holds D-001 and D-002, both already answered and archived.
  No new id appeared, so nothing was unblocked and nothing was waiting.
- **All fourteen `machine` criteria re-proved from scratch**, with `--rerun-tasks` rather than `--rerun`,
  because the trap `PROJECT.md` records is precisely that `--rerun` leaves `:app:testDebugUnitTest`
  `UP-TO-DATE` while looking like it forced a re-execution. Result: `BUILD SUCCESSFUL in 1m 8s`,
  **61 actionable tasks: 61 executed** — not one `UP-TO-DATE` marker on any line, which is what makes the
  numbers below this invocation's own rather than a cache's.
  - **296 tests across 19 classes, 0 failures, 0 errors** (criteria 1, 2, 7, 8, 14). The named cases exist
    and are green: `GreetingDecisionTest`'s *"four minutes across midnight is two different days and greets
    twice"* and *"a nearly twenty-four hour gap inside one day is still one day"* are criterion 2 written
    as a test; `GreetOnceADayTest`'s *"failure then success on the same day greets exactly once and records
    only the success"* is the criterion-15 fix; `AlarmNotifierConstantsTest` still passes unmodified.
  - **Lint 0 errors, 75 warnings** (criterion 14).
  - **Screenshots: 23 rendered, 0 diffs.** The 23 live hashes in the validation report and the 23 `.png`
    files on disk are the *same 23*, compared name by name — so no orphan and no new reference (13, 14).
    `AlarmsPermissionNoticeCase`'s `a2b5ee82_0.png` is among them.
  - **Manifest, source and merged** (9, 10): neither location permission in either; all six required
    permissions present, `VIBRATE` included per D-001. The merged manifest additionally carries AGP's own
    injected `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, which is not a source declaration.
  - **Location symbols: 0 hits** across `app/src/main/java` for all nine names, and 0 in
    `HomePermissionBottomSheet.kt` (11, 12).
  - **Greeting mechanics read in the source, not taken on trust** (3-7): `MainActivity.onStart` →
    `greetIfFirstForegroundToday`; `greetingMutex.withLock` wraps the whole read-decide-post-write
    sequence, not just the write; the fact is persisted through `SettingDatastore.lastGreetedDateKey` and
    re-read from the flow on every call, with no in-memory holder; the text comes from
    `R.string.hello_what_will_you_write_today`, present in both `values/` and `values-de/`; channel id
    `"greeting"` at `IMPORTANCE_DEFAULT`, created in `MainApplication.onCreate` through Koin's own
    instance, beside the alarms channel.
  - **Alarms untouched** (8, 13): `AlarmNotifier.kt`, `AlarmNotifierConstantsTest.kt` and every file under
    `ui/fragment/alarms/` appear nowhere in this run's diff (`3506b33..HEAD`, the last pre-run commit).
- **Queued D-003** — one consolidated Human Verification Request for the six unsigned `human` criteria
  (15-20), and **not** for 21, which the human signed on 2026-09-15 and which nothing since has touched.
  Re-asking it is the failure mode §7 names by example. Items 19 and 20 are written to be done by hand,
  because the device refuses injected input; item 18 says plainly that it needs the system date moved on a
  daily-driver phone and that the decision is the human's, with "just open it tomorrow" offered as the
  alternative. Item 15 states its precondition — data cleared, permission granted *when asked* — as the
  check itself, since testing the other branch is how the bug shipped.
- **`SUGGESTIONS.html`'s Escalate tab regenerated in the same step**, before the status was written: D-003
  added as the pending card, D-002 marked closed with a pointer to where the answer now goes, the
  standfirst corrected from seven items to six.
- **No amendment.** The plan did not change: no task was split, merged, reordered or added, and the
  DONE-candidate flag stands rather than being cleared, because nothing was found wrong.
- **Reported:** `ESCALATE`. Every `machine` criterion holds; six `human` criteria are unsigned; no
  executable task remains. ENGINE.md §11.3 — do not create the Cleanup Commit, do not report `DONE`.

## Archived Decisions

### D-002 - Human Verification Request: seven things only a person can look at

**Queued:** Iteration 3, 2026-09-15. **Answered:** 2026-09-15. **Consumed:** Iteration 4, 2026-09-15.

**Question:** Work through DoD criteria 15-21 on a real device and mark each pass or fail — the seven
classed `human` at bootstrap, because this repository has no emulator, no device and no Robolectric, so
nothing the engine can run ever sees the app actually running.

**Decision — partial, and explicitly labelled so ("Do not treat this entry as complete").** Four items
answered, three not.

- **15 FAIL.** App data cleared, app opened, notification permission granted when asked, app closed and
  reopened — no greeting, ever. The human read the cause out of the code themselves:
  `greetOnceToday()` wrote the greeted date inside `withContext(NonCancellable)` regardless of whether
  `postGreeting()` had reached anybody, and `postGreeting()` quietly posts nothing on Android 13+ without
  the permission. They also rejected the comment that defended it — "retrying would only greet them
  repeatedly on the day they finally grant the permission" does not hold, because `greetingDueOn` already
  guarantees the once. **They deliberately did not prescribe the fix**, naming instead the two questions
  it had to answer: what "actually delivered" can honestly be checked as, and what should happen for a
  user who has switched the greeting channel off on purpose.
- **16, 17 pass**, on instrumented evidence: the notification record's `when` was byte-identical across
  two further foregrounds and the count stayed at one; and after `am force-stop` plus a confirmed
  relaunch to the foreground, `dumpsys notification` showed zero greetings.
- **21 pass**, with one carve-out (the notes list was not scrolled). The installed package requests
  `POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, INTERNET, ACCESS_NETWORK_STATE, SCHEDULE_EXACT_ALARM,
  VIBRATE` — neither location permission present, `VIBRATE` present, which is D-001 visible at package
  level on a real device.
- **18, 19, 20 not answered.** 18 needs the device's system date moved forward a day on a daily-driver
  phone. 19 and 20 need taps the device refuses to inject (`SecurityException: Injecting input events
  requires ... INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate).

**How the evidence was qualified, and why that mattered.** The human stated plainly that 16, 17 and 21
were read out of `dumpsys notification`, `dumpsys package` and `logcat` rather than seen on a screen —
"It proves what the system recorded. It does **not** prove how anything looked" — and carved out the
appearance half of every item accordingly. That is what made the 15 FAIL legible as a behaviour failure
rather than a rendering one, and it is why the fix could be designed from it directly.

**Applied:** `ESCALATION.md` D-002 marked answered-in-part; `STATE.md` sign-off table rewritten (16 and
17 are **unsigned again**, because T-003 changed the code they were looking at — §11; 21 stands);
DONE-candidate cleared, then re-set after T-003; T-003 filed and shipped. See `AMENDMENTS.md` A-15/A-16.

### D-001 - Approve the Definition of Done for the greeting-notification / permission-cleanup run

**Queued:** Iteration 1, 2026-09-15. **Answered:** 2026-09-15. **Consumed:** Iteration 2, 2026-09-15.

**Question:** Approve `.harness/run/DoD.md` as written (23 acceptance criteria, 6 Constraints), including
the Verification Class assigned to each criterion?

**Context:** `PRD.md` pre-decided a once-a-day greeting notification and the removal of three permissions
(`VIBRATE`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`). Bootstrap fan-out flagged that the PRD's
stated reason for removing `VIBRATE` ("no `Vibrator` usage anywhere in the app") was incomplete —
`data/notification/AlarmNotifier.kt` independently calls `.setVibrate(...)`/`channel.enableVibration(true)`
— but did not block on it since the PRD explicitly asked the run not to re-litigate its pre-decisions.

**Decision:** Approved with one change: do NOT remove `android.permission.VIBRATE`. The PRD's removal
reason was wrong given `AlarmNotifier.kt`'s real usage. Revise `DoD.md` to drop criterion 9's VIBRATE
clause, drop criterion 14 (KDoc correction is unnecessary if the permission stays), and drop criterion 23
(no VIBRATE removal left to human-verify). Everything else approved as written: remove the two location
permissions plus their code, and build the greeting notification as specified.

**Applied:** See `AMENDMENTS.md`, 2026-09-15 entry, for the full list of files this decision touched
(`DoD.md`, `PLAN.md`, `TASKS/T-002.md`, `STATE.md`, `ESCALATION.md`).

### Iteration 6 - 2026-09-16 (Verifier — final)

- **Phase:** none. The task graph was already empty: T-001, T-002, T-003 all complete, nothing abandoned,
  nothing unreachable, nothing deferred. This invocation wrote no source file, which is what gives it
  standing to declare completion (ENGINE.md invariant 8).
- **Recovered:** working tree carried one modification, `.harness/run/DECISIONS.md` — the human's answer
  to `D-003`. Not crash debris: that file is theirs alone and the Runtime denies the engine both Edit and
  Write on it. Nothing to salvage, nothing to revert.
- **Consumed `D-003` (§6.2).** All six items pass. See `AMENDMENTS.md` A-17 for the decision, the human's
  verbatim rationale, and their own clause-by-clause note on criterion 19.
- **Re-verified all fourteen `machine` criteria on its own fresh evidence (§11.1).** It did not read
  iteration 5's table as an input. `RESUME.md` advised skipping the re-run because no commit had touched
  `app/` since `8b67f40`; that advice was not taken. §11 says the Verifier runs the build itself, the
  spec outranks a derived cache, and this is the invocation that removes `.harness/run/` and reports
  `DONE` — it should stand on evidence it produced.

  ```
  :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest --rerun-tasks
    -> BUILD SUCCESSFUL in 3m 11s, 61 actionable tasks: 61 executed
  unit tests   296 across 19 classes, 0 failures, 0 errors, 0 skipped (baseline was 251)
  lint         0 errors, 75 warnings
  screenshots  23 rendered, 0 diffs; 23 reference PNGs on disk == 23 rendered, no orphan, no new file
  ```

  Not one `UP-TO-DATE` marker on any task line. That, and not the `BUILD SUCCESSFUL` line, is what makes
  the evidence this invocation's own — the trap recorded in `PROJECT.md` and re-read before trusting it.
  The per-criterion table is in `STATE.md`. All fourteen hold; no gap was found, so no task was filed and
  the DONE-candidate flag had nothing to clear.
- **Learned:** the app's `namespace` is `com.example.skeleton` but its `applicationId` is
  `com.example.myapplication`. D-002's adb evidence names the latter and the source tree the former, and
  the two look like a contradiction until you read `app/build.gradle.kts:11,21`. They agree. Recorded in
  `PROJECT.md` so a later reader does not spend the same minutes on it.
- **Completed.** Every `machine` criterion re-proved by an invocation that wrote none of the
  implementation; every `human` criterion signed by a person. Cleanup Commit created (`.harness/run/`
  removed from the branch tip; `.harness/ISSUES.md` kept). Reported `DONE`. Merging is the human's act.

---

## Archived decision exchanges

### D-003 - Human Verification Request: six checks, and the one that failed last time

**Queued:** Iteration 5, 2026-09-15. **Answered:** 2026-09-16. **Consumed:** Iteration 6, 2026-09-16.

**Question:** Work through DoD criteria **15-20** on a real device and mark each pass or fail. Criterion
21 was deliberately excluded — it was signed on 2026-09-15 and nothing since had touched the manifest or
the permission code, and re-asking a settled question is noise, not diligence.

**Context:** criterion 15 came back **FAIL** from D-002 — a fresh install never greeted, because the app
wrote down "greeted today" whether or not the system had actually shown anything, burning the day's one
greeting on the very first foreground, which on a fresh install happens *before* the user has been asked
for the notification permission. T-003 (`8b67f40`) fixed it: `areNotificationsEnabled()` is asked first
and the day is recorded only on an accepted post. That fix changed `GreetingNotifier`, which unsigned 16
and 17 under §11 even though they had passed — the code that was looked at no longer existed.

**Decision:** **all six pass**, signed 2026-09-16: *"tớ đồng ý với các tiêu chí bên trên, tớ đã check
rồi"* — I agree with the criteria above, I have checked them.

The human added a note of their own on criterion 19, separating what a command proved from what their
eyes did: the two-rows-and-no-Location-row clause was machine-verified on a clean AOSP API 36 emulator
where the sheet appeared on its own; the "no leftover gap" clause and the "each row opens the correct
system screen" clause were not, and rest on the signature. Their words for why that is fine: the `human`
class "exists precisely because the two clauses above cannot be reached any other way." Kept verbatim in
`AMENDMENTS.md` A-17 so that nobody later reads "19 pass" and assumes a command proved all three.

**Applied:** `ESCALATION.md` D-003 marked answered-in-full; `STATE.md` sign-off table completed (15-20
signed 2026-09-16, 21 standing from 2026-09-15); `ISSUES.md` regenerated; `SUGGESTIONS.html`'s Escalate
tab emptied. No task filed — nothing failed.
