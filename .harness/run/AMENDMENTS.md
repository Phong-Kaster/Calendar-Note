# AMENDMENTS

> Tier-1 plan mutations, logged as they happen: split/merge/reorder tasks, re-group Phases, add
> prerequisites, remove obsolete tasks. PRD, DoD, and architecture unchanged.

### 2026-09-15 - D-001 consumed: VIBRATE stays declared

Following the human's D-001 answer (`.harness/run/DECISIONS.md`), applied the DoD change and its
downstream task edits — a decision consumption (ENGINE.md §6.2), not a self-initiated Tier-1/2 mutation:

- `DoD.md`: criterion 9 no longer lists `android.permission.VIBRATE` among the removed permissions (it is
  now in the "still declares" list); old criterion 14 (`AlarmNotifier.kt` KDoc correction) and old
  criterion 23 (human check for alarm heads-up after VIBRATE removal) are dropped entirely; criterion 8
  simplified to say `AlarmNotifier` is wholly unchanged (constants, behavior, and KDoc); all criteria after
  the drop points renumbered to close the gaps (old 15-22 → new 14-21); Status marked APPROVED.
- `PLAN.md`: T-002's task-graph line and title changed from "three unused permissions" to "two unused
  location permissions"; dropped `data/notification/AlarmNotifier.kt` from its scope; the VIBRATE risk in
  Known Risks marked closed.
- `TASKS/T-002.md`: title, Declared File Scope (drops `AlarmNotifier.kt` entirely, now genuinely out of
  scope), Description, step 1 (manifest edit is now two lines not three), removed step 6
  (`AlarmNotifier.kt` KDoc edit), Acceptance (drops criterion 14 from its "Maps to" list, six kept
  permissions not five).
- `STATE.md`: Progress table's T-002 scope column, and the stale VIBRATE-removal assumption replaced with
  a note pointing at D-001; Stage moved from `bootstrap` to `executing`, Next Phase set to Phase 1.
- `ESCALATION.md`: D-001 marked answered.

T-001 is unaffected — none of its scope or acceptance criteria (1-8) reference VIBRATE. Both tasks
unblocked for Phase 1.

---

## 2026-09-15 — Iteration 4 — D-002 consumed, and Phase 2 added (T-003)

### Decision consumption (ENGINE.md §6.2) — not a self-initiated mutation

**D-002** (the Human Verification Request for DoD criteria 15-21) was answered in `DECISIONS.md`.
The answer is deliberately partial and says so: "Do not treat this entry as complete."

- **Criterion 15 — FAIL.** With app data cleared, the human opened the app, granted the notification
  permission when asked, closed it and reopened it; no greeting ever appeared. The human also corrected
  their own earlier evidence: the pass previously recorded came from a device where `POST_NOTIFICATIONS`
  was already granted before the first open — the easy branch, not the branch criterion 15 describes.
- **Criteria 16, 17, 21 — pass**, on instrumented evidence read out of `dumpsys notification`,
  `dumpsys package` and `logcat` against device `b56e2819` (23021RAAEG, Android 15). 21 carries one
  carve-out: the notes list was not scrolled.
- **Criteria 18, 19, 20 — not answered.** 18 needs the device's system date moved forward a day on a
  daily-driver phone; 19 and 20 need taps the device refuses to inject (`SecurityException: Injecting
  input events requires ... INJECT_EVENTS`, MIUI's "USB debugging (Security settings)" gate).

**Applied:** `ESCALATION.md` D-002 marked answered-in-part with the carry-forward recorded;
`STATE.md`'s sign-off table updated; the DONE-candidate flag cleared, because criterion 15 failing
means the run is not a candidate for completion.

### A-15 (Tier 1) — Phase 2 and task T-003

Criterion 15's failure is a discovery, reconciled (§8) into one new task rather than an amendment or a
decision: the cause is known, sits inside the existing design, and needs no human input.

- **`TASKS/T-003.md` added** — "The greeting day is recorded only when the greeting was actually
  delivered". Depends on T-001. Capable tier. Scope: `domain/greeting/GreetOnceADay.kt` (new),
  `test/.../GreetOnceADayTest.kt` (new), `data/notification/GreetingNotifier.kt`,
  `ui/fragment/home/HomeFragment.kt`.
- **`PLAN.md`** — T-003 added to the Task Graph, Phase 2 added to the Phase Grouping.
- No PRD change, no DoD change, no architecture change. `DoD.md` is untouched: criterion 15 was always
  the right criterion and the code was wrong, which is exactly the case Tier 1 covers.

**The design decision inside T-003, recorded because it is a judgement a reader will question.** The
human explicitly declined to prescribe the fix and named the trade-off: what may honestly be called
"delivered", and what should happen for a user who turned the greeting channel off on purpose. The
answer shipped is `NotificationManagerCompat.areNotificationsEnabled()` as the single gate — it covers
both `POST_NOTIFICATIONS` ungranted on API 33+ and app-level notifications switched off — plus the
`SecurityException` path for the revoked-mid-call race. **A user who muted only the greeting channel is
deliberately counted as delivered**: `areNotificationsEnabled()` stays `true` for a single muted channel,
so the day is written and the app stops trying. That user made a choice about this notification; the user
who has not yet been asked for the permission has not, and that is the whole distinction.

### A-16 (Tier 1) — two Fresh-Context Review findings wired by the Iteration

Both sat outside T-003's Declared File Scope, so the Iteration made them itself (§6.6):

1. `GreetingDecisionTest.kt`'s `askForAGreeting` helper carried a KDoc claiming it was "the **same four
   steps, in the same order**" as the shipped sequence. After T-003 that is false — production records
   only on delivery. The claim, not the helper, was the hazard: a future reader reconciling the two
   copies could have "restored consistency" by deleting the delivery guard and reintroducing the bug.
   KDoc rewritten to say what the helper actually models and to point at `GreetOnceADayTest`.
2. `README.md`'s package tree gained `domain/greeting/GreetOnceADay.kt`, and two stale one-line notes
   were corrected (`GreetingNotifier` no longer "owns the read-decide-post-write sequence"; and
   `GreetingDecision.kt` is no longer "the only tested part of the greeting").

### A-17 (Tier 1) — D-003 consumed: the last six `human` criteria signed

**Decision:** `D-003`, answered 2026-09-16. All six items (DoD criteria 15-20) returned **pass**.

**The human's rationale, recorded verbatim rather than paraphrased** — it is the whole of what closes a
`human` criterion: *"tớ đồng ý với các tiêu chí bên trên, tớ đã check rồi"* ("I agree with the criteria
above, I have checked them"). A person with the app on their own device states that the software behaves
as the six criteria describe. That is exactly what the `human` Verification Class asks for, and the only
thing that can close these items.

**The human also drew the line for us on criterion 19, and it is worth keeping.** They separated the
three clauses of that criterion by what backs each:

- *"only the Notification row and, on Android 12+, the Exact alarms row — no Location row"* —
  **machine-verified** on a clean emulator (`astronex_test`, AOSP, API 36), installed fresh so the sheet
  appeared on its own rather than being assumed into existence. The dumped hierarchy carried exactly two
  rows; a search for "location" returned nothing.
- *"no leftover gap"* — **not machine-verified.** A view hierarchy lists nodes, not how they look.
- *"toggling each row still opens the correct system dialog/screen"* — **not machine-verified.** It needs
  a tap; the emulator is gone and the physical device refuses injected input (`SecurityException:
  INJECT_EVENTS`, a MIUI restriction that also defeats instrumentation — tested, not assumed).

The signature covers the whole criterion. The note exists so that nobody later reads "19 pass" and
assumes a command proved all three clauses.

**Applied:** `ESCALATION.md` D-003 marked answered-in-full; `STATE.md`'s sign-off table shows all seven
`human` criteria signed and carries this iteration's own re-measurement of all fourteen `machine`
criteria; `ISSUES.md` regenerated with no outstanding problem; `SUGGESTIONS.html`'s Escalate tab shows an
empty queue. No task was filed: nothing failed. No PRD change, no DoD change, no architecture change.

**Not an amendment to anything executable** — the task graph was already empty. Logged here because §6.2
requires every consumed decision to be logged with the human's rationale, and because this is the entry a
reader will look for when asking why the run stopped.
