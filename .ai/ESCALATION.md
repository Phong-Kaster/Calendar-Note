# ESCALATION REQUEST

> The engine persists this before stopping whenever a decision exceeds its authority.
> Fill the **Decision** section (decision + rationale), then re-run the Runtime. At most one pending request at a time.

- **ID:** D-001
- **Type:** DoD approval + Capability grant + Tier 2 (evidence strategy)
- **Iteration:** 1 (Bootstrap)
- **Timestamp:** 2026-09-10

---

## Question

Four decisions, all of which block the first line of implementation. **Only Q1 is mandatory**; Q2 is
required for any evidence at all; Q3 and Q4 change how much of the DoD can be proven without you.

**Q1 — Approve `.ai/DoD.md`.** Edit it freely first. `PRD.md` §7 lists nine open questions and a
criterion cannot be written around a question mark, so each has been resolved into a concrete
decision, tabulated as **A1–A12** at the top of `DoD.md`. The ones most worth your eye:

- **A5 — note fields: body only, no title.** The PRD's inferred data model lists `content` and marks
  a title "unconfirmed". If you want a title, say so now; adding it later touches the entity, the
  migration, both screens and the tests.
- **A7 — delete is allowed, behind a confirmation.** This is the one capability *added* beyond what
  the PRD describes. Strike criterion 7 if you don't want it.
- **A4 — a day's notes appear inline under the calendar grid**, not in a bottom sheet.
- **A2 — primary blue is `#35A0F5`** (the lighter of the two blues already in the skeleton, and the
  one already painted on the bottom-bar button). The PRD says "blue, shade not defined".
- **A11 — the existing centre button in `CoreBottomBar` becomes the create-note action.** It is
  currently wired to an unused flag.

**Q2 — Grant the standing toolchain capability.** `./gradlew --version` was refused at bootstrap, so
**nothing in this repository has ever been built, tested or linted by this engine.** Without this
grant no task can ever be marked complete, because ENGINE.md §10 requires build/test/lint evidence.
Proposals C1 below.

**Q3 — Two test dependencies are needed for the *baseline* machine evidence.** Not a nice-to-have:
DoD criteria 4, 6, 10 and 11 are proven by unit tests over `suspend` functions and `Flow`s, and
`kotlinx-coroutines-test` is not in `gradle/libs.versions.toml`. Without it, criterion 10 — the one
explicit business rule in the PRD — has no machine evidence and falls back to you looking at a
screen. Proposal C2.

**Q4 — How should the UI criteria be proven?** `.ai/DoD.md` § *Verification Evidence Required* states
the gap plainly rather than hiding it: criteria **1, 2, 7 and 9 each contain a clause no command can
ever prove** (whether a colour reads as blue, whether a disabled day *looks* disabled, whether a
marker contrasts with the fill under it, whether a confirming control is visually distinct from a
cancelling one). Separately, criteria 5, 7, 8, 11 and 12 have behavioural halves that *could* be
machine-proven, but only with host-side UI tests this repository does not have. Choose Option A, B or
C below.

---

## Context

**What was found at bootstrap.**

This is not a greenfield repository. It is a working Fragment + Compose + Koin + Room skeleton with a
demo Home screen backed by a network `Post` list. Consequences:

- **PRD open question 1 is already answered by the code.** `CoreFragment` exists solely to host a
  `ComposeView`; `HomeFragment` already renders a `LazyColumn`. The "hybrid vs. `RecyclerView`"
  choice is not open — picking `RecyclerView` would mean deleting the skeleton. Recorded as A1, not
  escalated as a live choice. Overrule it in `DoD.md` if that reading is wrong.
- **The architecture the PRD leaves undecided is already decided**: Room (`AppDatabase` at
  `version = 2`, an existing migration to copy), Koin, Navigation Component. Recorded as A8.
- `java.time` is safe on `minSdk 24` because core-library desugaring is enabled — a build-config
  fact, not a language guarantee.

**What is missing.**

- **No toolchain has run.** `./gradlew --version` → *"requires approval"*. Every command in
  `knowledge/PROJECT.md` is recorded `Verified: no`, and the task names are inferred from Android
  Gradle Plugin defaults rather than observed. It is not yet known whether this project builds at
  all. The first iteration after approval treats that as its first finding.
- **No CI**, so there is no pipeline to inherit verified commands from.
- **No test infrastructure.** `app/src/test` and `app/src/androidTest` contain the two template
  stubs and nothing else. `androidTest` needs a device or emulator this engine cannot drive.
  `kotlinx-coroutines-test` and Robolectric are both absent.
- **`CLAUDE.md` `@`-imports three rule files that do not exist** (`view-model-layer.md`,
  `jetpack-compose-ui-layer.md`, `wiki-connection.md`). The real files are `viewmodel-layer.md` and
  `jetpack-compose-ui.md`; there is no wiki file at all. The rules were read directly from the real
  files, so nothing was lost this iteration — but the import block is silently dropping rules for
  every other agent that reads it. `CLAUDE.md` is yours, so it is filed in `knowledge/ISSUES.md`
  rather than edited. **No decision needed here** — noted so you can fix it when convenient.

**Why Q4 exists at all.** `POLICIES.md` § Evidence Requirements forbids the tempting third route:
quietly treating the machine-checkable subset of a requirement as the whole requirement. Under that
route criterion 1 would silently become "the source contains no `lightColorScheme`" — which passes
while the app renders unreadable text on a black background, and the fresh-context reviewer has no
standard to judge the rest against. So the gap is named instead, and you choose how to close it.

---

## Options Considered

### Q2 — toolchain capability

1. **Grant C1 as proposed (four narrow `./gradlew` task rules).** — Consequences: the engine can
   build, unit-test and lint, and nothing else. It cannot run `clean`, `installDebug`, or any task
   that touches a device. Evidence becomes possible.
2. **Grant a single broad `Bash(./gradlew*)`.** — Consequences: fewer future escalations if a new
   task name is needed; also permits every Gradle task including `clean` (deletes build outputs) and
   anything a future plugin adds. Broader than this run needs.
3. **Grant nothing.** — Consequences: the run cannot proceed past T-001. No task can be marked
   complete without evidence, so every iteration would escalate. Not viable.

### Q4 — how the UI criteria get proven

- **Option A — human walkthrough only (no new dependencies).**
  The engine proves everything a JVM unit test can reach (ordering, the future-date rule, date
  filtering, day-cell state mapping), and T-009 produces `.ai/HUMAN-CHECKLIST.md`: one numbered
  walkthrough for every criterion it could not prove. You run it once on a device at the end.
  *Consequences:* zero new dependencies and zero new risk. Roughly nine criteria depend on your eyes,
  not four. A UI regression in a later iteration will not be caught by any automated check.

- **Option B — add host-side (Robolectric) UI tests. Recommended.**
  Add Robolectric + `androidx.compose.ui:ui-test-junit4` to `testImplementation` and set
  `testOptions.unitTests.isIncludeAndroidResources = true`. Compose UI tests and Room in-memory tests
  then run on the JVM inside `./gradlew :app:testDebugUnitTest` — **no device, no emulator, no new
  permission beyond C1**. This converts the behavioural halves of criteria 5, 7, 8, 11 and 12 into
  machine evidence, and makes them regression-proof for the rest of the run.
  *Consequences:* three test-only dependencies and a `testOptions` block — a Tier-2 change because it
  is a technology choice, which is why it is here rather than done silently. Robolectric's first run
  downloads Android jars, so the first test run is slow. It still cannot prove anything perceptual:
  criteria 1, 2, 7 and 9 keep their human-inspection clause under every option.

- **Option C — drive a real device/emulator over `adb`.**
  Instrumented tests and real screenshots.
  *Consequences:* `adb` is a high-risk capability (process/system management, outside the
  repository), needs a device attached for the whole run, and is far slower per iteration. It would
  prove the most, at the highest cost and risk. Not recommended unless you already have an emulator
  running and want screenshots in the audit trail.

### Q1 — the DoD itself

The alternative to approving a DoD with A1–A12 baked in is to answer `PRD.md` §7 yourself and have
the engine regenerate it. That is strictly better if any assumption is wrong — but it costs a round
trip, and the table is laid out precisely so you can edit `DoD.md` in place instead.

---

## Engine Recommendation

- **Q1:** approve `.ai/DoD.md`, editing A5 (title field) and A7 (delete) first if either is wrong.
  Those two are the ones that get expensive to change later — A5 touches the entity, the migration,
  both screens and the tests; the rest are localised.
- **Q2:** **Option 1** — the four narrow rules in C1. They cover build, compile, test and lint, which
  is exactly what ENGINE.md §10 requires and nothing more. A broad `./gradlew*` buys convenience that
  has not been shown to be needed, and `POLICIES.md` puts deletion in the high-risk class.
- **Q3:** grant C2. Without `kotlinx-coroutines-test` the PRD's one explicit business rule — no
  future-dated notes — has no automated proof, and DoD criterion 10 is the criterion that most
  deserves one.
- **Q4:** **Option B.** It is the only option that moves criteria out of the human-inspection column
  without attaching hardware, it runs inside the capability already being granted in Q2, and it is
  test-scoped so it cannot affect the shipped APK. Option A remains honest and safe — the run will
  simply lean much harder on your walkthrough, and later iterations can silently break UI behaviour
  that no check covers.

If you grant Q2 but decline Q3 and Q4, the run still works. It will report more human-inspection
items and fewer verified criteria, and it will say so at Final Verification rather than reporting a
verified `DONE` over an unproven requirement.

---

## Proposed Capabilities

### C1 — Gradle toolchain (standing, per repository)

```json
{
  "intent": "Build, unit-test and lint the app so task completion can be evidenced (ENGINE.md §10). Nothing in this repository has been built by the engine yet; without this, no task can ever be marked complete. Includes the dependency resolution Gradle performs as part of these tasks.",
  "command": "./gradlew <task> (assemble / compile / unit test / lint only)",
  "scope": "consumer repository, module :app",
  "lifetime": "standing (per repository, survives this goal)",
  "allow": [
    "Bash(./gradlew --version)",
    "Bash(./gradlew :app:assembleDebug*)",
    "Bash(./gradlew :app:compileDebugKotlin*)",
    "Bash(./gradlew :app:testDebugUnitTest*)",
    "Bash(./gradlew :app:lintDebug*)"
  ],
  "target_ledger": "knowledge/capabilities.json"
}
```

Deliberately excluded, and left for a separate request if they ever prove necessary: `clean`
(deletion), `installDebug` / `connectedAndroidTest` (device), `assembleRelease` (signing).

### C2 — Test dependencies required for the baseline machine evidence (Q3)

Not a permission — a change to `gradle/libs.versions.toml` and `app/build.gradle.kts`, listed here
because it is a dependency/technology decision:

- `org.jetbrains.kotlinx:kotlinx-coroutines-test` → `testImplementation`. **Required** by DoD
  criteria 4, 6, 10 and 11: their tests call `suspend` functions and collect `Flow`s.
- `androidx.room:room-testing` → `testImplementation` (only if Q4 Option B is taken; it needs
  Robolectric to run on the JVM). Covers DoD criterion 3's persistence half.

### C3 — Host-side UI test infrastructure (Q4, Option B only)

- `org.robolectric:robolectric` → `testImplementation`
- `androidx.test:core-ktx` → `testImplementation`
- `androidx.compose.ui:ui-test-junit4` → `testImplementation` (already declared for `androidTest`;
  the catalogue alias `androidx-compose-ui-test-junit4` exists and can be reused)
- `androidx.compose.ui:ui-test-manifest` → `debugImplementation` (already present)
- `android { testOptions { unitTests { isIncludeAndroidResources = true } } }` in
  `app/build.gradle.kts`

All test-scoped: nothing here reaches the shipped APK. Runs inside the C1 grant — no additional
permission.

---

## Proposed `knowledge/DOMAIN.md` entries

`knowledge/DOMAIN.md` does not exist, and the engine is forbidden from creating it (ENGINE.md §5.3 —
it is human-owned and deny-listed). `PRD.md` §5 does carry durable business rules, so they are
proposed here for you to decide whether the file should exist at all. **If you create it, its rules
outrank the codebase forever**: a difference between code and `DOMAIN.md` becomes a defect in the
code, and the fresh-context reviewer checks every diff against it.

Candidate entries, verbatim from `PRD.md` §5:

1. **A note can never be dated later than the current local date.** Today is allowed; tomorrow is
   not. The rule is enforced in `NoteRepository`, below the UI, so that no caller can bypass it.
2. **Notes are ordered most-recently-touched first**, by `updatedAt` descending — "touched" means
   created or edited.
3. **The app is dark-only.** No light theme is built or exposed, and the theme does not follow the
   system setting.
4. **The primary colour is blue.**

Rules 1 and 2 are genuine domain invariants and are the strongest candidates — they constrain
behaviour that a plausible-looking implementation can get subtly wrong (a `>=` where a `>` belongs
makes today's notes impossible; an `ORDER BY createdAt` looks correct until a note is edited). Rules
3 and 4 are product/design constraints rather than domain truth; they are already DoD criteria 1 and
2, and duplicating them into `DOMAIN.md` buys little.

**Recommendation:** create `knowledge/DOMAIN.md` with rules 1 and 2 only. Declining entirely is also
fine — both are already DoD criteria 10 and 4, and the DoD is equally binding for this run. The
difference is lifetime: `DOMAIN.md` survives after `.ai/` is deleted at completion and governs every
future run.

---

## Decision

<!-- HUMAN WRITES HERE: the decision AND its rationale. The rationale becomes part of the audit trail.

Suggested shape — answer all four, in any words you like:

Q1 (DoD):        approved as written | approved with the edits I made in .ai/DoD.md | rejected because …
Q2 (toolchain):  C1 as proposed | broad ./gradlew* | other …
Q3 (test deps):  granted | declined because …
Q4 (UI evidence): Option A (human walkthrough) | Option B (Robolectric) | Option C (adb/device)
DOMAIN.md:       created with rules 1+2 | created with … | not creating it because …

Rationale:
-->
