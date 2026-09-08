# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries - fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

---

## D-001 - Approve the Definition of Done and standing capabilities

- **Status:** pending
- **Type:** DoD approval
- **Iteration:** 0 (Bootstrap)
- **Timestamp:** 2026-09-08
- **Blocks tasks:** T-001, T-002, T-003, T-004, T-005, T-006, T-007
  <!-- Every task in this run — none can be verified without the build/test capability requested below. -->

### Question

1. Approve `.harness/run/DoD.md` (edit its Acceptance Criteria / Constraints freely before approving).
2. Approve the two capability grants proposed below, needed before any Phase can be built, tested, and
   checkpointed.

### Context

This is the Bootstrap iteration. Per `ENGINE.md` §5, the DoD approval is the only blocking gate in the
entire run — after this is answered, the loop will not stop for a question again until it genuinely runs
out of executable work. `PRD.md`'s "How completion will be judged" section is largely self-contained and
was carried into `DoD.md` with minimal interpretation; the two items below are the only points that needed
engine judgment rather than direct transcription, both flagged by the PRD itself:

- The PRD explicitly says: "If a test dependency is genuinely needed ... propose it as a capability/
  architecture decision rather than assuming it is available." The plan is designed to avoid needing one
  at all (tests exercise in-memory repository fakes directly via `runBlocking`, never a real Room DB and
  never a ViewModel instance, so no `Dispatchers.Main` stub is needed). The repo survey indicates
  `kotlinx-coroutines-core` (which provides `runBlocking`) is already reachable on the JVM test classpath
  transitively via `room-ktx`/`lifecycle-runtime-ktx`, since Android Gradle Plugin puts the module's
  `implementation` dependencies on the unit-test compile classpath — but `gradle/libs.versions.toml` has
  no explicit coroutines entry today, so this has not actually been compiled and verified yet. Requesting
  pre-authorization now, scoped narrowly, avoids a mid-run stall if the transitive path turns out not to
  resolve as expected.
- No standing capability exists yet to run this repository's own build/test/lint commands — every Phase's
  §ENGINE 6.7 step needs one before any task can be checkpointed as complete.

### Options Considered

1. Grant both capabilities now, narrowly scoped - consequences: Phase 1 can run to completion without a
   mid-run pause; the coroutines-test-dependency grant may simply go unused if the transitive path
   resolves, which the first test run will show either way.
2. Grant only the build/test/lint capability now, and require a fresh decision later if the coroutines
   dependency turns out to be needed - consequences: safer/narrower, but likely costs one extra
   Iteration's round-trip the first time `gradlew.bat test` actually runs, if the transitive dependency
   does not resolve.
3. Grant neither, and require per-command approval - consequences: no Phase in this run could ever
   complete a checkpoint; not viable given DoD criteria 1-2 require these exact commands.

### Engine Recommendation

Option 1. The coroutines grant is written narrowly enough (test source set only, one specific dependency,
explicit ban on `kotlinx-coroutines-test`/`Dispatchers.Main` usage) that pre-approving it costs little and
removes a likely one-Iteration stall.

### Proposed Capabilities

```json
{
  "intent": "Run this repository's verified build, unit-test, and lint commands so each Phase's build/test/lint step (ENGINE.md §6.7) and final DoD verification (§11) can execute and be checkpointed",
  "command": "gradlew.bat assembleDebug | gradlew.bat test | gradlew.bat lint",
  "scope": "this repository only; no other process, no network access beyond what Gradle itself needs for its own dependency cache",
  "lifetime": "goal",
  "allow": [
    "Bash(./gradlew.bat assembleDebug*)",
    "Bash(./gradlew.bat test*)",
    "Bash(./gradlew.bat lint*)",
    "Bash(./gradlew.bat testDebugUnitTest*)",
    "Bash(./gradlew.bat lintDebug*)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

```json
{
  "intent": "If Phase 1's first gradlew.bat test run shows kotlinx-coroutines-core's runBlocking/Flow APIs do not resolve on the JVM test classpath transitively (as the repo survey expects them to, via room-ktx/lifecycle-runtime-ktx), add one explicit testImplementation entry for kotlinx-coroutines-core (pinned to whatever version Room 2.7.2 already resolves) to app/build.gradle.kts and gradle/libs.versions.toml. Test source set only.",
  "command": "Edit app/build.gradle.kts and gradle/libs.versions.toml to add testImplementation(libs.kotlinx.coroutines.core)",
  "scope": "app/build.gradle.kts and gradle/libs.versions.toml, test source set dependency graph only; explicitly excludes kotlinx-coroutines-test, Robolectric, Turbine, MockK, and any androidTest dependency",
  "lifetime": "goal",
  "allow": [
    "Edit(app/build.gradle.kts)",
    "Edit(gradle/libs.versions.toml)"
  ],
  "target_ledger": ".harness/knowledge/capabilities.json"
}
```

### Decision

<!-- HUMAN WRITES HERE: the decision AND its rationale. The rationale becomes part of the audit trail. -->
