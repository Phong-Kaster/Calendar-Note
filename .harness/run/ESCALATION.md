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

- **Status:** pending
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

<!-- Answered in `.harness/run/DECISIONS.md`, under a heading `## D-001` - never here. -->
