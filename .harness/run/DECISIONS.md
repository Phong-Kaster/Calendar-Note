# DECISIONS

> Your answers go here, never in `.harness/run/ESCALATION.md`. That file is the engine's own log —
> it may still be appending to it or rewriting it long after this file first appeared, because
> queuing a decision does not stop the run (the engine keeps working on everything else it can).
> Waiting for `ESCALATION.md` to exist is not the same as the engine having actually stopped, and a
> decision written into that file while the engine is still using it can be lost or half-read.
>
> This file is yours alone. The Runtime denies the engine both Edit and Write on it, mechanically —
> not by asking it nicely — so nothing you write here can ever race an engine write.
>
> For each entry you want to answer, copy its id from `ESCALATION.md` (e.g. `D-001`) as a heading
> below, and write your decision and rationale under it. The rationale joins the audit trail.
> The engine picks up every id present here at the **start** of its next Iteration, never mid-flight
> — so answer, save, and only then re-run. Wait for `Status: ESCALATE` in the run's output as your
> signal that it is safe to answer, not for this file or `ESCALATION.md` merely existing.

---

## D-001
Approved with one change: do NOT remove `android.permission.VIBRATE`.

The PRD's reason for removing it was wrong — `AlarmNotifier.kt` calls
`.setVibrate(...)` and `channel.enableVibration(true)`, so the permission backs a
feature that actually exists. Revise DoD.md to drop criterion 9's VIBRATE clause,
drop criterion 14 (the KDoc correction is unnecessary if the permission stays),
and drop criterion 23 (there is no VIBRATE removal left to verify by hand).

Everything else is approved as written: remove ACCESS_COARSE_LOCATION and
ACCESS_FINE_LOCATION plus the code that requests them, and build the
once-a-day greeting notification as specified.
---

## D-002

Partial answer. Criteria 15, 16, 17 and 21 are signed off below on instrumented
evidence from a real device; 18, 19 and 20 are **not answered yet** and stay
queued.

How this evidence was obtained, so it can be weighed properly: it was collected
by Claude over adb against device `b56e2819` (23021RAAEG, Android 15), by reading
`dumpsys notification`, `dumpsys package` and `logcat` — not by a person looking
at the screen. It proves what the system recorded. It does **not** prove how
anything looked. Where a criterion asks about appearance, that part is explicitly
carved out below.

**15 FAIL.**

*What the human saw, 2026-09-15:* cleared the app's data, opened the app, granted
the notification permission when asked, closed the app, opened it again — and no
greeting ever appeared.

*What I had recorded before that, and why it was wrong:* I marked this pass on
evidence from a device where `POST_NOTIFICATIONS` was already granted before the
first open, because the app had been installed there previously. That path works
and the notification is correct on it — title `"Calendar Note"`, text `"Hello!
What will you write today?"`, `channel=greeting`, `importance=3`, no sound or
vibration. But it is not the path this criterion describes. Criterion 15 says
*install with app data cleared, grant the permission when asked, then open* — and
on that path the greeting never arrives at all. I tested the easy branch and
called the criterion proved.

*Cause, in the code:* `GreetingNotifier.greetOnceToday()` (lines 121-128) does

    withContext(NonCancellable) {
        postGreeting()
        settingRepository.setLastGreetedDate(date = greetingDate)
    }

`postGreeting()` catches the missing-permission case and quietly posts nothing on
Android 13+. The date is then written regardless, so `greetingDueOn` answers
`null` for the rest of the day and the user is never greeted on the day they
installed the app.

The comment above that line defends it: retrying "would only greet them
repeatedly on the day they finally grant the permission". That does not hold.
If the date were written only when the greeting was actually delivered, the next
foreground that has the permission would greet exactly once and record it —
`greetingDueOn` already guarantees the once. There is no repetition to prevent.

*Not prescribing the fix* — that is the engine's to design, and it should decide
what "actually delivered" can honestly be checked as (`NotificationManagerCompat
.areNotificationsEnabled()`, a permission check, or the `SecurityException` path)
and what should happen when the user has switched the greeting channel off on
purpose, which must not turn into a greeting on every single foreground.

**16 pass.** With the greeting already posted, the app was backgrounded via a
HOME intent and brought to the front twice more on the same day. The notification
record's `when=1789465609575` was byte-identical before and after, and the
greeting count stayed at one. No second greeting was posted.

**17 pass.** `am force-stop com.example.myapplication` at 16:49:20 (logcat
confirms `onNotificationRemoved ... 1000000`, so the shade was emptied), then the
app was relaunched. Logcat confirms it genuinely reached the foreground —
`topActivity=ComponentInfo{com.example.myapplication/...MainActivity}`, task
id=169, at 16:50:59. A fresh `dumpsys notification` then showed **zero** greeting
notifications. Process death on the same day does not re-arm the greeting, which
is exactly what this criterion exists to catch.

**21 pass (with one carve-out).** `adb install -r` reported `Success`; the app
opened to Home and rendered an existing note ("11 Sept 2026 / Jfkfkfgkyxdy");
`logcat -b crash` contains no record for the package. The installed package's
requested permissions are now:

    POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, INTERNET,
    ACCESS_NETWORK_STATE, SCHEDULE_EXACT_ALARM, VIBRATE

Neither `ACCESS_COARSE_LOCATION` nor `ACCESS_FINE_LOCATION` is present, and
`VIBRATE` is, which is the D-001 decision reflected at package level on a real
device.
*Carve-out:* the notes list was not scrolled.

**18 not answered.** Requires moving the device's system date forward a day. This
is the human's daily-driver phone and that change affects every app on it, so it
was not made without being asked.

**19 not answered.** Requires revoking permissions and then tapping each row of
the Home permission sheet. The device refuses injected input —
`SecurityException: Injecting input events requires ... INJECT_EVENTS`, MIUI's
"USB debugging (Security settings)" gate — so no tap could be performed.

**20 not answered.** Same reason: it requires opening the Alarms tab and tapping
its fix buttons.

Do not treat this entry as complete. Three criteria remain unsigned, and the run
cannot honestly report DONE until a person has answered them.

---

## D-003

- [x] 15 — fresh install, first open of a calendar day → the greeting appears
- [x] 16 — second open the same day → no second greeting
- [x] 17 — force-stop and relaunch the same day → still no greeting
- [x] 18 — next calendar day → the greeting comes back on its own
- [x] 19 — Home's permission sheet no longer mentions location
- [x] 20 — the Alarms screen's own permission notice is unchanged

**All six pass.** Signed by the human on 2026-09-16: *"tớ đồng ý với các tiêu chí bên trên,
tớ đã check rồi"* — I agree with the criteria above, I have checked them.

Recorded verbatim rather than paraphrased, and with the same note this file carries
for `D-008`: this is a person stating that the software behaves as the criteria
describe, on their own device. It is what the `human` class asks for and the only
thing that can close these six. Where machine evidence exists it is written below;
where it does not, the signature stands alone, which is the normal and intended
case for this class.

What backs which half, so a later reader does not have to guess. The criterion
has three clauses, and machine evidence reaches only the first of them:

- *"the sheet shows only the Notification row and, on Android 12+, the Exact
  alarms row — no Location row"* — **machine-verified.** On a clean emulator
  (`astronex_test`, AOSP, API 36) the app was installed fresh, so no permission
  had been granted and the sheet appeared on its own — the starting state this
  criterion names, reached rather than assumed. The dumped view hierarchy carried
  exactly two rows and nothing else:

      Notification         — Allow notification to send you important updates and alerts
      Alarms & reminders   — Allow exact alarms so notifications can trigger on time

  A search of the hierarchy for "location" returned nothing.

- *"no leftover gap"* — **not machine-verified.** A view hierarchy lists nodes; it
  does not say how the layout looks. This rests on the human's eyes.

- *"toggling each row still opens the correct system dialog/screen"* — **not
  machine-verified.** It needs a tap. The emulator that could have supplied one
  has been shut down, and the attached physical device refuses injected input
  (`SecurityException: INJECT_EVENTS`, a MIUI restriction that also defeats
  instrumentation — tested, not assumed).

The signature covers the whole criterion. It is the human's to give, and the
`human` class exists precisely because the two clauses above cannot be reached
any other way. This note exists so that nobody later reads "19 pass" and assumes
a command proved all three.

No criterion remains unsigned. Every `machine` criterion was re-proved by the
Verifier at `aaa810a`; these six were the only thing between the run and `DONE`.
