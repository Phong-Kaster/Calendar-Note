# HISTORY

> Append-only audit log. Written every iteration, **never read during Orient** — it exists for a
> human auditing the run, not for the engine deciding what to do next. Keeping it out of the read
> path is what stops orientation cost from growing with the run.

<!-- Newest first. One entry per iteration. -->

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
