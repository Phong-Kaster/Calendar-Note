# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries — fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.
>
> **Three entries are open.** D-001 is the run's only blocking gate and nothing moves until it is
> answered. D-002 and D-003 are answerable in the same pass and are separated only so that each can be
> refused on its own without taking the others down with it.

---

## D-001 — Approve the Definition of Done, and re-install the toolchain capabilities

- **Status:** decided
- **Type:** DoD approval + Capability grant
- **Iteration:** 1
- **Timestamp:** 2026-09-13
- **Blocks tasks:** A-001, A-002, A-003, A-004, A-005, A-006, A-007 — every task in the run.

### Question

Two things, in one answer:

1. **Approve `.harness/run/DoD.md`.** Edit it freely first. Approval covers the **Verification Class** of
   each criterion (`machine` vs `human`), not only its wording, and it covers the **Assumptions table**
   (AS-1 … AS-11) — each row is the engine's reading of something the three-sentence addendum does not say.
   AS-4 and AS-10 are the two whose reversal is expensive; both say so in the table.
2. **Grant the standing toolchain capabilities below**, so that the engine can build, test and lint.

### Context

The addendum is three sentences. The DoD turns them into 36 criteria: 19 `machine`, 17 `human`. The split
is not caution — this repository has **no emulator, no device, no `adb` and no Robolectric**, so "a popup
notification appears and grabs my attention" cannot be proved by any command that exists here. The
`AlarmScheduler` seam converts the arithmetic, the arming and the cancelling into JVM unit tests and leaves
only delivery and appearance for a person. Those residues are named at criteria 22, 31 and 32 rather than
quietly dropped.

**The engine currently holds no build capability at all.** The previous run's standing ledger is at
`knowledge/capabilities.json` (repository root). The runtime now reads `.harness/knowledge/capabilities.json`,
which does not exist. Until that file exists, `./gradlew` is denied, no task can be evidenced, and therefore
no task can ever be marked complete. The proposal below is the previous ledger's content — **including its
2026-09-11 correction withdrawing `updateDebugScreenshotTest`**, which stays withheld.

### Options Considered

1. **Approve as written.** — consequences: the run starts at A-001. 17 criteria will need a person with a
   phone at the end; the Verifier will hand you a numbered checklist built from the Evidence table.
2. **Approve, but reclassify some `human` criteria as `machine`.** — consequences: the run will report
   `DONE` on claims nothing here can check. The previous run on this branch declared its perceptual criteria
   unprovable while a working host-side screenshot harness sat on a sibling branch; the failure mode in the
   other direction is a delete button correctly wired and rendered invisible, with every test green.
3. **Cut scope — drop AS-4 (the on/off switch), which the addendum never asked for.** — consequences:
   removes criteria 29 and 30 and one schema column. **Decide this now or not at all:** the column ships in
   A-002's single migration, and adding it later costs a second hand-written migration, which is the most
   dangerous operation available in this repository (C-03).
4. **Grant a narrower capability set** (for example build and test but not lint). — consequences: criterion
   2 becomes unsatisfiable and every task's completion gate weakens with it.

### Engine Recommendation

**Option 1, with the capability proposal approved as written.** AS-4 is the one piece of engine-added scope
and it is recommended kept: a daily alarm with no way to silence it short of deleting it gets worse every
day the user ignores it, and the column is free today and expensive in three weeks.

### Proposed Capabilities (if any)

```json
{
  "intent": "Build, unit-test and lint the app so task completion can be evidenced (ENGINE.md §6.7, §6.10). The engine currently holds no build capability in this repository; without this, no task can ever be marked complete. Includes the dependency resolution Gradle performs as part of these tasks.",
  "command": "./gradlew <task> (assemble / compile / unit test / lint only)",
  "scope": "consumer repository, module :app",
  "lifetime": "permanent",
  "allow": [
    "Bash(./gradlew --version)",
    "Bash(./gradlew :app:assembleDebug*)",
    "Bash(./gradlew :app:compileDebugKotlin*)",
    "Bash(./gradlew :app:testDebugUnitTest*)",
    "Bash(./gradlew :app:lintDebug*)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

```json
{
  "intent": "Verify rendered UI against the recorded reference images, so acceptance criteria about appearance, contrast and layout are provable by command output instead of being narrowed away. Host-side (layoutlib on the JVM) - no emulator, no device, no adb. Read-only with respect to the references: it compares, it never overwrites.",
  "command": "./gradlew :app:validateDebugScreenshotTest",
  "scope": "consumer repository, module :app",
  "lifetime": "permanent",
  "allow": [
    "Bash(./gradlew :app:validateDebugScreenshotTest*)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

```json
{
  "intent": "Read files out of earlier commits and out of a prior run's Loop Branch. Bash(git show*) is already baseline, but on Windows/Git Bash MSYS rewrites 'ref:path' into 'ref;path' and the command fails for exactly the paths that matter (.harness/**, knowledge/**), with an error that reads as if the file is missing. MSYS_NO_PATHCONV=1 is the documented workaround, and the leading assignment means the baseline rule does not match it.",
  "command": "MSYS_NO_PATHCONV=1 git show <ref>:<path>",
  "scope": "consumer repository, read-only",
  "lifetime": "permanent",
  "allow": [
    "Bash(MSYS_NO_PATHCONV=1 git show*)",
    "Bash(MSYS_NO_PATHCONV=1 git ls-tree*)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

**`updateDebugScreenshotTest` is deliberately absent from this proposal.** It overwrites the committed
reference images, which hands an engine facing a red screenshot test a one-command route to re-recording
wrong output as correct — the same class of act as editing an approved DoD. It was granted standing on
2026-09-10 and withdrawn on 2026-09-11; it is not being asked for again here. The narrow, expiring version
of it is D-003.

### Decision

**Approved as written — Option 1, with the capability proposal approved as written.** Keep AS-4 (the
per-alarm on/off switch): it's cheap to include now in the single A-002 migration and expensive to add
later via a second hand-written migration (C-03), and a daily alarm with no way to silence it short of
deletion gets worse the longer it's ignored. Grant all three proposed capability blocks (gradlew
assemble/compile/test/lint, validateDebugScreenshotTest, and the MSYS_NO_PATHCONV git-show/ls-tree
workaround) to `.harness/knowledge/capabilities.json` as specified. `updateDebugScreenshotTest` stays
withheld from the standing ledger, per the 2026-09-11 withdrawal — it is granted separately and narrowly
in D-003 only.

---

## D-002 — Where does the Alarms screen live, and what does the bottom bar's centre "+" do there?

- **Status:** decided
- **Type:** Tier 2 (plan/architecture)
- **Iteration:** 1
- **Timestamp:** 2026-09-13
- **Blocks tasks:** A-001, A-002, A-006

### Question

AS-5 assumes Alarms is the **fourth bottom-bar tab**, and that `CoreBottomBar`'s centre "+" — which today
always means "new note" — is **hidden on the Alarms screen only**, because that screen has its own floating
action button for creating an alarm. Is that right?

### Context

This is the one assumption in the DoD the engine is genuinely unsure of, which is why it is asked rather
than recorded.

`CoreBottomBar` splits its entries down the middle around a centre round add button. Its KDoc says a
**fourth** screen fills a deliberately-empty right slot and needs no change to that file — so the tab itself
is cheap. But `CoreBottomBar(onCreateNote: () -> Unit)` has **no default, deliberately**: the component's
author decided that a new top-level screen must state what the centre button means there, and would not
guess on the author's behalf. Neither will the engine.

Two consequences either way: a fourth tab makes the existing German truncation worse ("Einstellungen"
already ellipsizes at three), and it moves every tab slot, which invalidates two committed reference images
— that is D-003.

### Options Considered

1. **Fourth tab; centre "+" hidden on Alarms; a FAB creates the alarm.** (AS-5 as written) — consequences:
   two controls that both mean "create" never appear together. Costs an edit to the shared `CoreBottomBar`
   and the re-record in D-003.
2. **Fourth tab; centre "+" becomes "new alarm" on the Alarms screen; no FAB.** — consequences: no shared
   FAB, one create control everywhere. But the addendum asks for a floating action button in as many words,
   so this contradicts the requirement text and would need DoD criterion 13 rewritten.
3. **Fourth tab; centre "+" keeps meaning "new note" everywhere, and the FAB sits beside it.** —
   consequences: no `CoreBottomBar` change and no cross-screen contract. But two round buttons sit inches
   apart on one screen meaning different things, which is the reading the engine likes least.
4. **Not a tab at all — reach Alarms from Settings or from an overflow.** — consequences: the reference
   images survive untouched and D-003 disappears entirely. But a daily-alarm feature buried two taps deep is
   a feature nobody uses, and criterion 8 ("the screen can be found") becomes hard to pass honestly.

### Engine Recommendation

**Option 1.** It is the only one that satisfies the addendum's words (a floating action button) without
putting two create controls on one screen. Option 4 is the cheapest and the engine would understand it
being chosen, but it trades the feature's discoverability for two reference images.

### Proposed Capabilities (if any)

None.

### Decision

**Option 1 — fourth tab; centre "+" hidden on Alarms; a dedicated FAB creates the alarm.** This is the
only reading that honors the addendum's explicit ask for a floating action button without putting two
"create" controls on one screen. Accept the consequences: `CoreBottomBar` needs the small change to hide
the centre button on the Alarms screen, and the two bottom-bar reference images need re-recording — see
D-003.

---

## D-003 — A goal-scoped grant to re-record the two bottom-bar reference images

- **Status:** decided
- **Type:** Capability grant
- **Iteration:** 1
- **Timestamp:** 2026-09-13
- **Blocks tasks:** A-001 — **its completion only.** A-001's implementation can proceed the moment D-001
  and D-002 are answered; what it cannot do is be marked complete, because its acceptance includes DoD
  criterion 2 and criterion 2 will be red from the moment the fourth tab lands.

### Question

Grant `./gradlew :app:updateDebugScreenshotTest` as a **goal-scoped** capability that expires with this
run, so the engine can re-record `ThemeScreenshotTestKt/BottomBar_*` and `BottomBarSystemNight_*` after the
fourth tab changes the bar's layout?

### Context

`CoreBottomBar` renders `BottomBarDestination.entries`, and `ThemeScreenshotTest` photographs the whole
bar. A fourth entry moves every tab slot, so `BottomBar_Bottom bar_7f47a575_0.png` and
`BottomBarSystemNight_…_d7a4f6ad_0.png` mismatch — **in a file no task opens**. This is an intentional UI
change invalidating a correct baseline, which is the legitimate case for a re-record, and it is also the
exact situation in which a re-record is most easily abused.

`updateDebugScreenshotTest` is withheld from the standing ledger on purpose (see D-001). A goal-scoped
entry expires when `.harness/run/` is removed at completion, so it cannot silently become the next run's
default.

Two traps that come with it, both recorded in `PROJECT.md`: it **orphans** references rather than replacing
them when a preview's `name`/`widthDp`/`heightDp` changes, and validation ignores strays and stays green, so
nothing tells you. Eight orphans are in the tree today. And references are exact-pixel and **host-locked** —
anything rendering a localised date or time follows the host's locale and cannot be pinned.

### Options Considered

1. **Grant it, goal-scoped.** — consequences: criterion 2 is satisfiable from Phase 1. The engine is
   required to re-record **only** the two named bottom-bar cases and to report the before/after hash list
   for each, so an unexpected third file appearing is visible in the checkpoint rather than lost.
2. **Refuse; you re-record the two images yourself when asked.** — consequences: safest, and the engine has
   no route to hiding a failure. Costs one human round-trip during Phase 1, and criterion 2 stays red until
   then, which means A-001 stays incomplete and the run reports `ESCALATE` at the end of its first Phase
   rather than continuing to A-002.
3. **Refuse outright, and accept criterion 2 as permanently failing.** — consequences: the run can never
   report `DONE`, and the regression gate that protects the already-verified Calendar Note feature is gone
   for the rest of the run. This would want criterion 2 amended in D-001 rather than left standing as a
   gate nothing can pass — that is a Tier-3 change and yours to make, not the engine's.
4. **Grant it standing** (in `.harness/knowledge/capabilities.json`). — consequences: it survives this run
   and every future one. This is the 2026-09-10 grant that was withdrawn on 2026-09-11; the engine is not
   asking for it.

### Engine Recommendation

**Option 1 if you want the run to proceed unattended past Phase 1; option 2 if you would rather sit at that
gate.** The engine does not think option 1 is clearly correct — the asymmetry the withholding protects is
real, and this request is exactly the shape a wrongly-motivated one would take. What distinguishes it is
that the mismatch is predicted **before** any code is written, and the two filenames are named in advance,
so a re-record producing anything else is visibly not this.

### Proposed Capabilities (if any)

```json
{
  "intent": "Re-record the two ThemeScreenshotTest bottom-bar references after the Alarms tab changes the bar's layout - an intentional UI change invalidating a correct baseline. Scoped to this goal and expiring with it. The engine must re-record only BottomBar_* and BottomBarSystemNight_*, and must report the live-hash list before and after so a stray or an unexpected third case is visible in the checkpoint.",
  "command": "./gradlew :app:updateDebugScreenshotTest",
  "scope": "consumer repository, module :app, references for ThemeScreenshotTest only",
  "lifetime": "goal",
  "allow": [
    "Bash(./gradlew :app:updateDebugScreenshotTest*)"
  ],
  "target_ledger": ".harness/run/capabilities.json"
}
```

### Decision

**Grant it, goal-scoped — Option 1.** The mismatch is predicted before any code is written and the two
filenames are named in advance, so a re-record producing anything else is visibly not this and would be
caught by the required before/after hash report. Re-record only `BottomBar_*` and `BottomBarSystemNight_*`;
report the live-hash list both before and after so any unexpected third file is visible in the checkpoint.
This grant expires with the run per its `"lifetime": "goal"` and lives only in `.harness/run/capabilities.json` —
it must not be copied into the standing `.harness/knowledge/capabilities.json` ledger.
