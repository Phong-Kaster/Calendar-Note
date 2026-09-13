# Definition of Done — Alarms feature

> Derived from the `PRD.md` addendum at bootstrap. Human-owned after approval: the engine may propose
> changes (Tier 3) but never apply them.
> Every criterion is verifiable by evidence and declares WHO can verify it.

## Scope

This DoD covers **only** the Alarms addendum (`PRD.md` §"Addendum — Alarms feature"). The Calendar Note
feature was built and verified by a previous run on this branch and its criteria are not re-litigated here.
Criterion 2 is the regression gate that keeps it standing.

The requirement verbatim:

> *"i need you continue on this branch with new screen named alarms where display all alarms the app have,
> i need a floating aciton button which open a new screen allow i can write my message, set time to fire
> alarm. For instance, on 12h everyday, the app will fire a notification with my message with highest
> priority, i need notification appears as popup notification to attract my focus on"*

## Status

- [ ] APPROVED — approve via the pending `.harness/run/ESCALATION.md` (D-001).
      **Edit these criteria freely before approving.** Approval covers the Verification Class of each
      criterion, not only its wording, and it covers the Assumptions section below — each assumption is the
      engine's reading of something the three-sentence addendum does not say. Changing one changes the
      criteria that depend on it, and those dependencies are named.

## Assumptions this DoD is written on

Each is a reading of an ambiguity in the addendum. They are recorded rather than queued individually because
the DoD gate already stops the run — you can overturn any of them in the same pass.

| # | Ambiguity | Assumed reading | Criteria affected | Cost to reverse later |
|---|---|---|---|---|
| AS-1 | "on 12h everyday" | An alarm is **a time of day that repeats every day**. Not one-off, not a weekday mask, not "every 12 hours". | 14, 17, 18, 20 | Low if one-off is added later; **high** if a weekday mask is wanted — that is a schema column. |
| AS-2 | Can an alarm be edited? | **Yes** — tapping a row re-opens the editor pre-filled. House precedent (a note works this way). | 22, 23 | Low. |
| AS-3 | Can an alarm be deleted? | **Yes**, behind a confirmation. A daily alarm with no way to stop it is a trap that gets worse every day. | 24, 25, 26 | Low. |
| AS-4 | Can an alarm be switched off without deleting it? | **Yes** — a per-row switch. This is **engine-added scope**: the addendum never asks for it. It costs a schema column that must ship in the single migration. | 27, 28, 29 | **High** — reversing it after release leaves a dead column, and adding it later costs a second hand-written migration, which is this plan's most dangerous operation (C-03). |
| AS-5 | Where does the screen live, and what about the existing centre "+"? | Alarms is the **fourth bottom-bar tab**; a Material FAB on that screen creates an alarm; `CoreBottomBar`'s centre "+" is **hidden on the Alarms screen only**. See D-001 option A — this is the one assumption the engine is genuinely unsure of and asks about explicitly. | 8, 12, 13 | Medium — it touches a shared component and re-records two reference images. |
| AS-6 | What does tapping the notification do? | Opens the app on the **Alarms list**. A deep link to the individual alarm is an upgrade, not a rewrite. | 21 | Low. |
| AS-7 | Message only, or title + message? | **Message only.** The notification's title is the app name; the body is the user's message. | 14, 20 | Low. |
| AS-8 | Is a blank message allowed? | **No** — save is refused and the refusal says why, tagged by exception type (C-09). A notification with an empty body is not a reminder. | 15 | Low. |
| AS-9 | Exact or inexact? | `setExactAndAllowWhileIdle`, re-armed on each firing, using the already-declared `SCHEDULE_EXACT_ALARM` and the existing request flow. **`USE_EXACT_ALARM` is not claimed** — Google Play restricts it to alarm-clock-class apps. Where the permission is not granted, fall back to an inexact window and tell the user (criterion 30). | 20, 30 | Low. |
| AS-10 | Sound, vibration, Do Not Disturb? | High-importance channel with its **default notification sound**; `CATEGORY_ALARM`; **no** full-screen intent and **no** DND override. "Popup" is read as heads-up, not as an alarm-clock takeover screen. | 19, 20 | **High on API 26+** — channel importance and sound are immutable after first creation (C-10). |
| AS-11 | List order | **Time of day ascending**, ties broken by id. | 9 | Low. |

## Acceptance Criteria

### Build, wiring and regression

**1. [machine] The whole app, including every new screen, compiles.**

**2. [machine] Nothing already verified regresses: unit tests, lint and the screenshot references all stay
green at the end of the run.** Note the sequencing hazard recorded in D-001: adding a fourth bottom-bar tab
invalidates two committed reference images the moment it lands, and only a human-granted re-record can
restore them. Until that grant exists this criterion is unsatisfiable from the first task onward.

**3. [machine] The database upgrade path for existing installs exists and is registered.** `AppDatabase`
declares `version = 4`, `Migration.kt` defines `MIGRATION_3_4`, and `DatabaseModule` passes it to
`addMigrations`. Verified by reading the source, not by reflection: Room's `@Database` annotation is not
retained at runtime and the builder needs an Android `Context`, so neither is reachable from a JVM test.
The migration's **SQL being correct** is not machine-checkable here at all (C-03) — it is covered by
criterion 4.

**4. [human] The app installs over an existing v3 install, launches, and the Alarms screen opens without
crashing.**

### The Alarms list

**5. [machine] Every alarm the user saved comes back out of the store.** A round-trip through the repository
with a fake DAO.

**6. [machine] The repository does not throw across its boundary.** A deliberately-failing fake DAO reaches
every catch block and each returns an `Outcome.Error`, not an exception.

**7. [machine] The list is ordered by time of day ascending, ties broken by id (AS-11), and the ordering is
the repository's, not the DAO's.** The fake hands back a jumbled list.

**8. [human] The Alarms screen can be found, and it is obvious which screen you are on.**

**9. [human] A list longer than the screen scrolls, and the last alarm is fully readable — not hidden under
the floating action button and not cut off by the bottom bar.**

**10. [human] An empty Alarms screen says it is empty rather than looking broken.**

**11. [human] A row whose message is longer than the row has a defined overflow, and the alarm's time stays
readable.**

### Creating an alarm

**12. [machine] The Alarms screen and the alarm-editor screen exist as navigation destinations with an
action between them, and every `<argument>` carries a `defaultValue` (C-08).**

**13. [human] A floating action button is visible on the Alarms screen and opens the "new alarm" screen.**

**14. [machine] Saving stores an alarm carrying exactly the message typed and the time set, and it repeats
daily (AS-1).**

**15. [machine] Saving a blank message is refused, and the refusal is reported as a value tagged by
exception type — not thrown, and not distinguished by message text (AS-8, C-09).**

**16. [human] The editor is usable: the message field accepts text, the time control sets a time, and the
keyboard does not cover what you are typing.**

**17. [machine] An alarm written through the store is readable through a freshly constructed repository
instance** — the ViewModel is not the source of truth.

**18. [human] An alarm survives the app being force-stopped and reopened.**

### Scheduling and the notification

**19. [machine] "Next time this fires" is computed correctly against an injected `Clock`, including the
boundaries.** Required cases: set for later today → fires today; set for earlier today → fires tomorrow;
set for exactly now → the chosen side of the boundary, asserted explicitly (an off-by-one here either
double-fires or skips a day); 00:00 when now is 23:59 → one minute away, tomorrow's date; a rollover across
a month boundary; and the occurrence after a firing at T is exactly T + one day at the same wall-clock time.

**20. [machine] The app asks the operating system for a heads-up notification.** The channel importance
constant is `IMPORTANCE_HIGH`, the builder's priority is `PRIORITY_HIGH`, the category is `CATEGORY_ALARM`,
and a small icon that is not the launcher mipmap is set. **This criterion pins intent, not rendering** — it
proves the app asked, and proves nothing about what a phone does. Assert the app's own constants: a
`NotificationChannel` constructed on the JVM is a stub whose getters return 0 (C-06), so asserting on the
object rather than on the constants would be a tautology.

**21. [machine] Every mutation point mirrors into the scheduler.** With a fake scheduler: saving an enabled
alarm arms exactly one schedule at the expected instant; editing the time cancels the old and arms the new;
deleting cancels; disabling cancels. **A delete that removes the row but leaves the schedule pending is the
worst defect this feature can have** — it notifies for an alarm the user deleted.

**22. [human] The alarm actually fires at the time set, with the user's message, and it appears as a popup
over whatever is on screen — and it fires again the next day without being re-created.**

**23. [human] Tapping the notification opens the app on the Alarms screen and dismisses the notification
(AS-6)** — it does not launch a second copy on top of the running one.

### Editing, deleting, switching off

**24. [machine] Re-opening an existing alarm loads its stored values, and saving updates that row rather
than creating a second one.**

**25. [human] Tapping an alarm in the list opens it with its message and time already filled in.**

**26. [machine] A single tap cannot delete anything: the confirmation step is unconditional.** Asking to
delete leaves the store untouched; a delete path that skipped the confirmation does nothing.

**27. [machine] Deleting removes the record, and a delete that matched no row is an `Outcome.Error`, not a
silent success (C-09).**

**28. [human] The delete control is findable, and the confirming button is unmistakably different from the
safe one** — differing in both colour role and emphasis, not colour alone.

**29. [machine] Switching an alarm off persists the off state, and switching it on again schedules from the
*current* time, not from when the alarm was created** — an alarm toggled off in the morning and on again in
the evening must next fire tomorrow, not immediately.

**30. [human] The enabled switch reads correctly at a glance, and its state marker contrasts with whatever
it is drawn on.**

### Failure modes the user must not have to guess at

**31. [human] With notifications disabled, or exact alarms not permitted, the Alarms screen says so and
offers the fix** — rather than saving an alarm that looks armed and will never fire.

**32. [human] Alarms still fire after the phone is restarted.**

### House rules

**33. [machine] No colour in any file this feature adds is hardcoded (C-01).** A grep, because neither lint
nor the screenshot harness can see this.

**34. [machine] Every user-visible string this feature adds lives in `strings.xml` with a German
counterpart (C-02).**

**35. [human] The two new screens look like they belong to this app** — black ground, the product blue
`#35A0F5`, no Material default lilac on any control, every label legible against what is behind it.

**36. [machine] `README.md`'s package tree and feature table describe the app that now exists.**

## Constraints

- All work on the Loop Branch. No merge, no history rewrite, never the default branch.
- No new dependency. `AlarmManager` is framework and `NotificationCompat` ships in `androidx-core-ktx`;
  a task proposing to edit `gradle/libs.versions.toml` or `app/build.gradle.kts` has become an architecture
  decision and must be escalated rather than implemented.
- `minSdk 24` / `targetSdk 36` are unchanged.
- The Constraints in `.harness/knowledge/PROJECT.md` apply to every file this run writes. A violation is a
  blocking review finding, not an opinion.
- `.claude/*.md` and `CLAUDE.md` are human-owned rule files; the engine reads them and never edits them.
- `PRD.md` is immutable to the engine.

## Verification Evidence Required

| # | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1 | machine | `./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL` | n/a |
| 2 | machine | `:app:testDebugUnitTest`, `:app:lintDebug` (`0 errors`), `:app:validateDebugScreenshotTest` each → `BUILD SUCCESSFUL`, each read from a run whose own success line was seen | n/a |
| 3 | machine | `version = 4` in `AppDatabase.kt`; `MIGRATION_3_4` in `Migration.kt` and inside `addMigrations(...)` in `DatabaseModule.kt` | n/a |
| 4 | human | Install the debug build **over an existing install that already has the app's v3 database** (do not uninstall first — that is what makes this a migration test). Launch. Expect: no crash dialog. Open Home, Calendar, Settings — expect each still opens and Home still lists any notes you had. | ☐ |
| 5 | machine | JVM unit test: save two alarms through the repository over a fake DAO, read them back, assert message and time | n/a |
| 6 | machine | JVM unit test with a `BrokenAlarmDao` failing every call: every repository method returns `Outcome.Error` and none throws | n/a |
| 7 | machine | JVM unit test: fake DAO emits `[21:30, 07:00, 12:00]`; the repository's flow emits `[07:00, 12:00, 21:30]` | n/a |
| 8 | human | Open the app. Look at the bottom bar. Expect a fourth entry whose icon and label read as "alarms" without being told which one it is. Tap it. Expect a screen headed *Alarms*. Tap it again — expect nothing to stack or flicker. Then tap Home: expect to get back in one tap. | ☐ |
| 9 | human | Create twelve alarms at different times. On the Alarms screen swipe up. Expect the list to move and the twelfth row to be **completely** visible — its time and its message both, with no part under the round add button and no part cut off by the bottom bar. Swipe back to the top; expect the first row fully visible too. | ☐ |
| 10 | human | On a fresh install, or after deleting every alarm, open Alarms. Expect a sentence in the middle of the content area saying there are no alarms yet — not a blank black rectangle, which reads as a load failure. | ☐ |
| 11 | human | Create an alarm whose message is a full paragraph (≥ 300 characters). Look at its row in the list. Expect: the **time** is still readable and the row is still the same height as its neighbours; the message is clipped with an ellipsis rather than reflowing into a block that buries the time. | ☐ |
| 12 | machine | `res/navigation/navigation_graph.xml` contains a `<fragment>` per screen, an `<action>` from list to editor, and a `defaultValue` on every `<argument>` | n/a |
| 13 | human | Open Alarms. Expect a round button floating above the list, in the bottom corner, that you can see without hunting and that does not sit on top of a row's text. Tap it. Expect a new screen to slide in with somewhere to type a message and a control to set a time. | ☐ |
| 14 | machine | JVM unit test: set message `"stand up"` and time `12:00`, save, assert the stored row's message is `"stand up"`, its time is 12:00, and its repeat is daily | n/a |
| 15 | machine | JVM unit test: save with a blank message → `Outcome.Error` whose `throwable` is the alarm-specific refusal type; the store is unchanged | n/a |
| 16 | human | Tap the add button. Tap the message field. Type four lines of text. Expect **the line you are typing stays visible above the keyboard** at all times. Set the time to 12:00 and expect to see `12:00` (or `12:00 PM`) before you save. Tap save; expect to land back on the list with the new alarm in it. | ☐ |
| 17 | machine | JVM unit test: write through one repository instance, read through a freshly constructed one over the same fake DAO | n/a |
| 18 | human | Create an alarm. Force-stop the app from Android Settings → Apps. Reopen it and open Alarms. Expect the alarm still listed with the same message and time. | ☐ |
| 19 | machine | JVM unit tests over the pure next-fire function with a fixed `Clock`, one test per case listed in the criterion | n/a |
| 20 | machine | JVM unit test asserting the app's own constants, plus a grep confirming `setSmallIcon` does not reference `@mipmap/ic_launcher` | n/a |
| 21 | machine | JVM unit tests with a fake `AlarmScheduler`: save-arms-one, edit-cancels-then-arms, delete-cancels, disable-cancels | n/a |
| 22 | human | On a real phone, create an alarm with the message `stand up`, set for **three minutes from now**. Press Home so the app is in the background. Wait. Expect: at that minute a banner **slides down over the top of whatever you are looking at** — not merely a silent icon in the status bar — showing `stand up`. Pull down the shade and confirm it is listed there too. Then **leave the phone until the same minute the next day** and confirm it fires again without you re-creating it. | ☐ |
| 23 | human | With the notification showing, tap it. Expect the app to open on the Alarms list and the notification to disappear. Expect **not** a second copy of the app stacked on the running one — press Back once and expect to leave the app, not to find another Alarms screen underneath. | ☐ |
| 24 | machine | JVM unit test: open an existing alarm by id, assert the UI state carries its stored values; save; assert the row count is still 1 and the row was updated | n/a |
| 25 | human | On the Alarms list, tap an existing alarm. Expect the editor to open with that alarm's message already in the field and its time already set — not blank, and not a second new alarm. | ☐ |
| 26 | machine | JVM unit test: "ask to delete" leaves the store untouched and raises a confirming flag; the delete path invoked without confirmation does nothing | n/a |
| 27 | machine | JVM unit test: delete an existing alarm → gone; delete a non-existent id → `Outcome.Error` | n/a |
| 28 | human | On the Alarms screen, find how to delete an alarm **without being told how** — if it is a gesture with no visible affordance, that is a fail. Trigger it. Expect a confirmation step. Expect the destructive button to be **filled in the error colour with bold text** while the way out is unfilled plain text: the two must differ in both colour role and emphasis, because on a black screen two buttons differing only in hue are a coin toss. | ☐ |
| 29 | machine | JVM unit test with a fixed `Clock`: disable → cancel called, stored flag false; advance the clock past the alarm's time; enable → armed for **tomorrow**, not for a moment already past | n/a |
| 30 | human | Look at an alarm row with the switch **on**, then with it **off**, in a dim room at arm's length. Expect to be able to tell the two apart at a glance, and expect the switch's thumb/marker to be clearly distinct from the track it sits on — the failure this exists for is a blue marker drawn on a blue fill, which vanishes and reads as a rendering bug. | ☐ |
| 31 | human | In Android Settings, turn **off** notifications for the app. Open Alarms. Expect the screen to tell you alarms cannot notify you, with a control that takes you to the right system screen. Turn notifications back on, return to the app, and expect the warning to go away by itself. Repeat for exact alarms (Settings → Apps → Special app access → Alarms & reminders). | ☐ |
| 32 | human | Create an alarm for ten minutes' time. **Reboot the phone.** Do **not** open the app afterwards. Wait. Expect the notification to arrive on time. (Android drops every scheduled alarm on restart; without a boot receiver this fails silently and there is no way to notice except by trying it.) | ☐ |
| 33 | machine | `grep -nE 'Color\.Black\|Color\.White\|Color\(0x'` over every file this feature adds → no matches | n/a |
| 34 | machine | `grep -nE 'text = "\|contentDescription = "'` over every new file → no matches; and `:app:lintDebug` → `0 errors` | n/a |
| 35 | human | On a phone, open Alarms and the alarm editor. Expect a black background, white body text, and the **same blue** as the rest of the app on the add button and the save control — not a second blue, not a purple, not a Material default lilac anywhere. Expect every label legible against what is behind it. | ☐ |
| 36 | machine | `README.md` contains `ui/fragment/alarms/` and the alarm editor package in its tree, with a purpose note per folder, and the feature table lists Alarms as built | n/a |
