# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> This file is the engine's own log. **Answer in `.harness/run/DECISIONS.md`, never here** - copy an
> entry's id as a heading there and write your decision underneath it, then re-run. The engine is
> denied write access to `DECISIONS.md`, mechanically, so nothing it does here can ever race what you
> write there. Answer any number of entries; unanswered ones stay queued and the tasks they name stay
> unselectable.

---

## D-001 - Approve the Definition of Done for the greeting-notification / permission-cleanup run

- **Status:** answered
- **Type:** DoD approval
- **Iteration:** 1
- **Timestamp:** 2026-09-15
- **Blocks tasks:** T-001, T-002
  <!-- This is the standing gate every run stops on once, per ENGINE.md §5: no task exists yet, so
       nothing is executable until the exam it will be graded against is approved. -->

### Question

Do you approve `.harness/run/DoD.md` as written (23 acceptance criteria, 6 Constraints), including the
Verification Class (machine/human) assigned to each criterion? Edit the file freely before approving —
approval covers the wording and the class of each criterion, not just the intent to build the feature.

### Context

`PRD.md` describes two independent, pre-decided pieces of work: a once-a-day greeting notification on
first foreground open, and removing three unused Android permissions (`VIBRATE`,
`ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`) plus the code that requests the location ones. The PRD
explicitly pre-answers the two questions it expects would otherwise block the run (which permissions to
drop, and where the "last greeted" date should live) and asks that this run not stop to re-ask them.

Bootstrap fan-out (three proposal roles, then two arm's-length critique/conflict-analysis roles, all
Capable-tier `loop-analyst`) surfaced one real tension worth your attention even though it does not block
this decision: `PRD.md`'s stated reason for removing `VIBRATE` ("no `Vibrator` usage anywhere in the
app") is literally true but incomplete — `data/notification/AlarmNotifier.kt` independently calls
`.setVibrate(...)`/`channel.enableVibration(true)` for alarm heads-up notifications on API 24-25. The PRD
still asks for the removal, so the run proceeds with it; DoD criterion 23 is a human-verified check for
whether alarms still produce a heads-up (they should, since a sound is also set), and the possible loss is
just the buzz, not the notification. Nothing here needs a decision — it is a known, accepted risk, named
so it is not silently discovered later.

No new capability is needed for this run. The existing standing ledger
(`.harness/knowledge/capabilities.json`) already grants `assembleDebug`/`compileDebugKotlin`/
`testDebugUnitTest`/`lintDebug`, `validateDebugScreenshotTest`, and the `MSYS_NO_PATHCONV=1 git show`
workaround — everything both tasks and their verification need.

### Options Considered

1. Approve as written. Both tasks become selectable immediately.
2. Edit specific criteria (e.g. drop criterion 23's human check, or re-word the Verification Class of any
   criterion) before approving.
3. Reject and send back for rework, with reasons — the engine will revise `DoD.md` and re-queue.

### Engine Recommendation

Approve as written. The criteria are drawn directly from the PRD's own stated requirements (including its
explicit "must not happen" list and its 251-test/23-screenshot regression floor), classified against this
engine's Verification Class rules with two independent arm's-length passes, and no criterion asks for
anything beyond what the PRD already specified.

### Proposed Capabilities (if any)

None. No new capability is requested for this run.

### Decision

Answered 2026-09-15 in `.harness/run/DECISIONS.md` under `## D-001`. Archived in full in `HISTORY.md`
under "Archived Decisions". Summary: approved with one change — `android.permission.VIBRATE` is kept;
`DoD.md` revised (criterion 9's VIBRATE clause dropped, the KDoc-correction and VIBRATE-heads-up-check
criteria dropped and remaining criteria renumbered); T-001 and T-002 unblocked.

---

## D-002 - Human Verification Request: seven things only a person can look at

- **Status:** queued
- **Type:** Human Verification Request (ENGINE.md §11)
- **Iteration:** 3
- **Timestamp:** 2026-09-15
- **Blocks:** the `DONE` report itself. No task remains — T-001 and T-002 are complete, nothing was
  abandoned, nothing is deferred, and all fourteen `machine` criteria were re-proved fresh by this
  invocation. These seven signatures are the only thing between this run and completion.

### Question

Please work through the seven checks below on a real device or emulator and mark each **pass** or **fail**.
They are `DoD.md` criteria 15-21, the ones classed `human` at bootstrap because no command in this
repository can prove them: there is no emulator, no device and no Robolectric here, so nothing the engine
can run ever sees the app actually running. A previous run in this repository shipped a delete button that
was correctly wired and rendered invisible against its own background with every test green — that is the
class of thing this list exists to catch.

Answer in `.harness/run/DECISIONS.md` under a `## D-002` heading. A one-line-per-item list is plenty
(`15 pass`, `16 pass`, …). Any **fail** should say what you saw instead; the engine will reconcile it into
a task or an amendment rather than arguing with you.

### The APK to install

`app/build/outputs/apk/debug/app-debug.apk` — built by this invocation from the branch tip
(`loop/calendar-note-app`). Rebuild it yourself with `./gradlew :app:assembleDebug` if you would rather
not trust the one on disk.

### The checklist

**1. (criterion 15) Fresh install, first open of a calendar day → the greeting appears**

- *Open:* install the debug APK on a device with this app's data cleared (a fresh install, or Settings →
  Apps → Calendar-Note → Storage → Clear storage first). Grant the notification permission when asked.
  Then tap the launcher icon.
- *Do:* nothing else — just let the app come to the front.
- *Expect:* a notification in the shade within a second or two. Its title is the app name, its text reads
  *"Hello! What will you write today?"* (or the German *"Hallo! Was schreibst du heute?"* on a German
  device). It arrives as a quiet line in the shade, **not** as a banner sliding over the screen — that is
  deliberate. Its small icon is a flat white alarm glyph, **not** a shrunken colour launcher icon and not a
  grey or white featureless square. Pull the notification open: the full greeting is readable, not clipped.

**2. (criterion 16) Second open the same day → no second greeting**

- *Open:* with the app already installed and greeted once today, press Home to send it to the background.
- *Do:* bring it back to the front (from Recents, or by tapping the launcher icon again). Do this two or
  three times. Do not cross local midnight.
- *Expect:* no new notification on any of those returns. If you dismissed the first greeting, it does not
  come back.

**3. (criterion 17) Force-stop and relaunch the same day → still no greeting**

- *Open:* Settings → Apps → Calendar-Note → **Force stop**. (This kills the process; the point is to prove
  the app remembered on disk rather than in memory.)
- *Do:* relaunch from the launcher, same calendar day.
- *Expect:* still no notification. This is the one that fails if the "already greeted" fact were being held
  in a field instead of written down.

**4. (criterion 18) Next calendar day → the greeting comes back on its own**

- *Open:* Settings → System → Date & time, turn **off** automatic date, and move the date forward by one
  day. (Or simply come back to the device after local midnight.)
- *Do:* open the app.
- *Expect:* the greeting appears again, with no action from you. Afterwards, please turn automatic date
  back on.

**5. (criterion 19) Home's permission sheet no longer mentions location**

- *Open:* turn the app's notifications **off** in system settings, and on Android 12+ also revoke *Alarms &
  reminders*. Open the app and go to the **Home** tab.
- *Do:* look at the permission sheet that appears, then tap each row it shows.
- *Expect:* exactly two rows on Android 12+ — **Notification** and **Exact alarms** — and only
  **Notification** on older versions. **No Location row**, and no blank gap or stray divider where one used
  to be. Tapping the Notification row opens the system notification dialog or settings screen for this app;
  tapping the Exact alarms row lands directly on the *Alarms & reminders* switch, not one screen short of
  it.

**6. (criterion 20) The Alarms screen's own permission notice is unchanged**

- *Open:* with a permission still missing, open the **Alarms** tab. Then grant both permissions and open it
  again.
- *Do:* read the banner in each state and tap its fix buttons.
- *Expect:* the same banner wording and the same buttons as before this run — nothing here was supposed to
  change — each landing on the correct system screen; and with both permissions in order, no banner drawn
  at all. (This run's diff touches no file under `ui/fragment/alarms/`, and that screen's pinned reference
  image still validates, so this check is a belt-and-braces look rather than an expected problem.)

**7. (criterion 21) The app still installs and opens after the permission removal**

- *Open:* install the debug APK and tap the launcher icon.
- *Do:* let Home load; scroll the notes list if you have notes.
- *Expect:* Home appears, no crash, no "app keeps stopping" dialog, and the notes list renders. Two
  permissions were deleted from the manifest this run; this is the check that nothing was quietly depending
  on them.

### Context

Every `machine` criterion was re-proved by this invocation, which wrote none of the implementation:

```
:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest
  -> BUILD SUCCESSFUL
unit tests   288 across 18 classes, 0 failures, 0 errors   (forced re-execution, --rerun-tasks)
lint         0 errors, 75 warnings
screenshots  23 of 23 green, 0 failures, 23 reference files, no orphans
manifest     source AND merged manifest carry neither location permission; VIBRATE kept per D-001
location     0 hits for all nine location symbols across app/src/main/java
alarms       AlarmNotifier.kt, ui/fragment/alarms/ and AlarmNotifierConstantsTest.kt untouched by the diff
```

### Options Considered

1. Work through the seven checks and record the results. The run then completes on the next invocation.
2. Sign off a subset now and leave the rest — the engine will re-raise only the unsigned ones.
3. Report a failure on any item. It becomes an ordinary discovery: the engine files a task, clears the
   DONE-candidate, and keeps going.

### Engine Recommendation

Option 1. Items 1-4 are the greeting's whole promise and take about five minutes plus one date change;
items 5-7 are quick looks. Item 3 (force-stop) is the one worth not skipping — it is the only check that
distinguishes "remembered on disk" from "remembered in memory", and it is the failure a user would meet as
a greeting every single time they opened the app.

### Proposed Capabilities (if any)

None. Nothing here needs a new grant — the engine cannot run the app under any capability this repository
could give it.

### Decision

_Awaiting an answer in `.harness/run/DECISIONS.md` under `## D-002`._
