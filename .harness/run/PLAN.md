# PLAN — Alarms feature

> Machine-owned execution strategy. The human never reviews this file — only the Definition of Done.
> Evolves through Tier-1 amendments (logged in `AMENDMENTS.md`) and Tier-2 queued decisions.

## Strategy

**Shape of the work.** Seven tasks, executed as **navigable → persistent → editable → scheduled → durable →
honest about failure → documented**. Each arrow is something a person can be shown. The order is not a
preference: every task after the first rewrites files the previous one created, so the dependency chain is
a *file* dependency, not just a logical one.

**Integration approach.** Almost every file that would let two tasks run in parallel is a shared file —
`navigation_graph.xml`, both `strings.xml`, `AndroidManifest.xml`, the three Koin modules, `AppDatabase.kt`,
`Migration.kt`, `BottomBarDestination.kt`, `README.md`, and the screenshot test class. All of them are
**Iteration-owned**: no Worker writes them. A Worker references `R.string.xxx` and reports the key with its
English and German text; the Iteration writes both files. This is what makes the Phases safe, and it is also
why there is almost no parallelism to have — see Phase Grouping.

**Verification approach.** Machine evidence comes from `assembleDebug` + `testDebugUnitTest` + `lintDebug` +
`validateDebugScreenshotTest`, run together whenever a composable signature changes (because the screenshot
source set is the only thing that compiles `app/src/screenshotTest/`). The testable seam that makes anything
about scheduling provable at all is an **`AlarmScheduler` interface in `domain/`** with an
`AlarmManager`-backed implementation in `data/`. That seam is a requirement of this plan, not a style
preference: without it nothing about "did we schedule the right thing at the right instant" can be checked
in this repository, because `AlarmManager`, `PendingIntent` and `NotificationManager` all return silent
stubs under the JVM test toolchain (C-06). With it, only "did the OS deliver it, and did it look like a
popup" is left for a person — and that residue is named honestly in DoD criteria 22, 31 and 32.

**What the plan deliberately does not do.** No new dependency. No `USE_EXACT_ALARM`. No full-screen intent.
No second Room migration: the schema — including the `enabled` column that nothing reads until A-004 — ships
whole in A-002, because a hand-written migration is the one operation here that cannot be tested and that
fails as a launch crash (C-03), and paying that risk twice for a column is a bad trade.

## Task Graph

- A-001 — Alarms is the fourth bottom-bar tab; the screen opens and says it is empty (depends on: –) — scope: `ui/fragment/alarms/**`, `res/drawable/ic_bottom_alarm.xml` — tier: Capable
- A-002 — An alarm can be written, saved, and listed, and it survives a restart (depends on: A-001) — scope: `domain/model/Alarm.kt`, `domain/repository/AlarmRepository.kt`, `data/**` alarm files, `ui/fragment/alarm_editor/**`, `ui/fragment/alarms/component/AlarmRow.kt`, its own tests — tier: Capable
- A-003 — An alarm can be re-opened and changed, switched off, and deleted behind a confirmation (depends on: A-002) — scope: the alarm/alarms/data files A-002 created, plus the confirmation sheet and its tests — tier: Capable
- A-004 — At the set time a heads-up notification arrives with the message, and it repeats daily (depends on: A-003) — scope: `domain/scheduler/`, `data/scheduler/`, `data/receiver/AlarmReceiver.kt`, `data/notification/`, `res/drawable/ic_notification_alarm.xml`, the four mutation call sites, its own tests — tier: Capable
- A-005 — Alarms still fire after the phone is restarted (depends on: A-004) — scope: `data/receiver/BootReceiver.kt`, `rearmAll` on the scheduler seam, its own test — tier: Capable
- A-006 — When the OS will not let an alarm fire, the Alarms screen says so and offers the fix (depends on: A-004) — scope: `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` plus the alarms screen trio — tier: Capable
- A-007 — The knowledge documents describe the run that happened (depends on: A-001…A-006) — scope: `.harness/knowledge/PROJECT.md` — tier: Capable

**Every task is Capable.** Not one survives the Fast criteria: A-001, A-002, A-004, A-005 and A-006 each
touch a shared/integration file or embed an architecture decision (criterion (a) or (c)); A-003 defines a
new navigation-argument contract and a destructive-confirmation UI, which is a judgement call, not an
existing pattern to extend (criteria (b) and (c)); A-007 is pure judgement about what is wrong with the run
(criterion (b)). `README.md`'s package tree would be the only genuinely Fast unit of work here, and it is
Iteration-owned rather than a task.

## Phase Grouping

| Phase | Tasks | Shared files the Iteration wires itself |
|---|---|---|
| 1 | A-001 | `navigation_graph.xml` (`alarmsFragment` + `toAlarms`), `BottomBarDestination.kt`, `ViewModelModule.kt`, both `strings.xml`, `CoreBottomBar.kt` (hide the centre "+" on Alarms, per AS-5), `README.md`, `AlarmsScreenshotTest.kt` (new — the `@PreviewTest` case A-001's acceptance requires, omitted from this row by the original plan; see AMENDMENTS.md A-5), and the **re-record of the two `ThemeScreenshotTestKt/BottomBar*` references** |
| 2 | A-002 | `navigation_graph.xml` (editor destination + arguments), `AppDatabase.kt`, `Migration.kt`, `DatabaseModule.kt`, `RepositoryModule.kt`, `ViewModelModule.kt`, both `strings.xml`, `AlarmsScreenshotTest.kt`, `README.md` |
| 3 | A-003 | both `strings.xml`, `AlarmsScreenshotTest.kt` |
| 4 | A-004 | `AndroidManifest.xml` (`<receiver>`, `MainActivity` launchMode), `RepositoryModule.kt` or a new `SchedulerModule.kt` + `AppModule.kt`, `MainApplication.kt` (channel creation), both `strings.xml`, `README.md` |
| 5 | A-005, A-006 | `AndroidManifest.xml` (`RECEIVE_BOOT_COMPLETED` + boot `<receiver>`), both `strings.xml`, `AlarmsScreenshotTest.kt` |
| 6 | A-007 | `.harness/ISSUES.md`, `README.md` |

**Phase 5 is the only Phase with two tasks, and the only parallelism available in this plan.** A-005 lives
in `data/receiver/` and `data/scheduler/`; A-006 lives in `ui/fragment/alarms/`. Neither reads the other's
files, and their only overlaps — the manifest, the strings files, the screenshot class — are Iteration-owned.
The disjointness holds **only if** A-006's permission check reads `PermissionUtil` / the existing top-level
helpers rather than querying scheduler state; that is written into A-006's brief.

Phases 1–4 are single-task by necessity, not by caution: A-002 rewrites all three files A-001 creates,
A-003 rewrites A-002's, and A-004 reaches back into the repository and the editor ViewModel to call
`schedule()`. Four of the seven tasks touch `AlarmsViewModel.kt`. Merging any of them would put two Workers
in one file and leave the Iteration reconstructing the diff by hand — worse than the orientation cost it
would save.

## Known Risks

1. **The screenshot references break on Phase 1 and only a human can fix them.** `CoreBottomBar` renders
   `BottomBarDestination.entries`, and `ThemeScreenshotTest` photographs the whole bar. A fourth entry moves
   every tab slot, so `BottomBar_Bottom bar_7f47a575_0.png` and `BottomBarSystemNight_…_d7a4f6ad_0.png`
   mismatch from the first commit — in a file no task opens. `updateDebugScreenshotTest` is deliberately
   withheld from the engine. **This blocks DoD criterion 2 for the whole run** and is the second half of
   D-001. It is also why A-001 is scheduled first: finding this out on day one is cheaper than finding it
   out behind four tasks of work.

2. **`MIGRATION_3_4` cannot be written in one pass and cannot be tested.** The SQL must be copied from
   Room's generated `AppDatabase_Impl.kt`, which only exists after a build that already contains
   `AlarmEntity` — so A-002's integration step is *write the entity → build → copy the statement → write the
   migration → build again*. `fallbackToDestructiveMigration(false)` turns a wrong one into a launch crash
   for every existing install, and every command in the toolchain goes green on it. This is the silent risk
   of the plan; risk 1 is the loud one.

3. **A-004's central promise cannot be verified by any command in this repository.** "A popup notification
   appears and grabs my attention" needs a phone. The `AlarmScheduler` seam converts *most* of it — the
   arithmetic, the arming, the cancelling — into JVM unit tests, and A-004's completion report must name
   what is left over rather than let a green suite imply it.

4. **The schema is declared final before the scheduler is designed.** If A-004 turns out to need a stable
   `Int` PendingIntent request code column, or a `lastFiredAt`, A-002's promise breaks and a second
   migration happens anyway. Mitigation: A-002's brief carries the scheduler's data requirements as a
   design sketch, decided at planning time rather than discovered in Phase 4. The sketch is in A-002's task
   file.

5. **`minSdk = 24` means two notification paths, and only one of them is the obvious one.** Channels do not
   exist below API 26; a heads-up there needs `PRIORITY_HIGH` plus a sound or vibration. Setting only the
   channel leaves the PRD's central ask unmet on two API levels with a fully green build (C-10).

6. **A fourth tab makes the known German truncation defect worse.** "Einstellungen" already ellipsizes at
   three tabs; a fourth shrinks the slots further. Recorded in `.harness/ISSUES.md` rather than fixed, since
   it predates this feature — but the new tab's label should be chosen short in both languages.

7. **The engine currently has no build capability at all.** The previous run's standing ledger sits at
   `knowledge/capabilities.json`; the runtime now reads `.harness/knowledge/capabilities.json`. Until D-001
   is answered, no task can be evidenced and therefore no task can be marked complete. This is the first
   half of D-001 and it blocks every task in the run.
