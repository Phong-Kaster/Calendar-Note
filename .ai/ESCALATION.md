# ESCALATION REQUEST

> The engine persists this before stopping whenever a decision exceeds its authority.
> Fill the **Decision** section (decision + rationale), then re-run the Runtime. At most one pending request at a time.

- **ID:** D-001
- **Type:** DoD approval + Capability grant + Tier 2 (evidence strategy)
- **Iteration:** 1 (Bootstrap)
- **Timestamp:** 2026-09-10

---

## ✅ CONSUMED — 2026-09-10, iteration 1. Nothing here is pending.

All five questions are answered and every consequence has been reconciled into the plan. **A later
invocation must not re-consume this file** (ENGINE.md §6.2 applies to *pending* requests); it is kept
at this checkpoint so the decision text survives at a known SHA, and it is indexed in
`.ai/STATE.md` § *Escalation Index*.

- **The request as issued** (Question / Context / Options / proposed capabilities C1–C3 / proposed
  `DOMAIN.md` entries) is at `git show 9b25307:.ai/ESCALATION.md`. It was replaced in place by the
  decision below, so it is no longer reachable at the tip by path.
- **What the decision produced:** `knowledge/capabilities.json` (three standing entries),
  `knowledge/DOMAIN.md` (two rules), `.ai/DoD.md` approved with A5 overruled, amendments
  **A-001** and **A-002**, and new task **T-010**.

---

**Answered 2026-09-10 by the human operator.**

### Q1 — DoD: approved, with one overrule

Approved as it now stands in `.ai/DoD.md`. One assumption was changed before approving:

- **A5 overruled: title + body, not body-only.** `NoteEntity` carries `title` and `content`. The
  title may be blank, and a blank title falls back to the first line of the body for display.
  Criteria 3, 4 and 6 were edited to match; the Status block is now checked.
  *Rationale:* Home is a reverse-chronological list of every note in the app, so it is the primary
  navigation surface, and it is materially harder to scan without titles. Your own reasoning is the
  argument for doing it now — entity, migration, both screens and tests — not for skipping it.

A1–A4 and A6–A12 accepted as written. A1 in particular is right and for the right reason: the
repository already *is* the hybrid, so it was never a live choice.

### Q2 — Toolchain: C1 as proposed (Option 1)

Written verbatim into `knowledge/capabilities.json`. Narrow rules, not broad `./gradlew*`.
*Rationale:* your reasoning holds — `clean` is deletion and belongs in the high-risk class, and
convenience that has not been shown to be needed is not a reason to widen a standing grant. If a
task name turns out to be wrong, escalate for that one task and I will add it.

### Q3 — Test dependencies: granted

`kotlinx-coroutines-test` and `room-testing`, both `testImplementation`.
*Rationale:* criterion 10 is the PRD's one explicit business rule. It is the last criterion that
should depend on a human looking at a screen.

### Q4 — UI evidence: Option B, **plus** infrastructure this repository already owns

Take Option B (Robolectric + `ui-test-junit4` + `isIncludeAndroidResources`) for the *behavioural*
halves of criteria 5, 7, 8, 11, 12.

**But do not start from zero, and do not treat the perceptual clauses as unprovable.** You wrote
that this repository "has no host-side test setup". That is true of `main`, and false of this
repository. A previous Foreman run on the branch **`loop/todo-calendar-screens`** already built,
debugged and verified a complete host-side **Compose Preview Screenshot Testing** harness on this
exact toolchain (AGP 9.0.1 / Kotlin 2.2.10 / JDK 24). It is invisible to you only because that
branch is unmerged and this run branched from `main`.

Import it rather than re-deriving it. It is at:

- `gradle/libs.versions.toml` — `screenshot = "0.0.1-alpha15"`, the `screenshot-validation-api`
  library, and the `com.android.compose.screenshot` plugin
- `app/src/screenshotTest/kotlin/com/example/skeleton/screenshot/ScreenshotScaffold.kt` — the
  reusable harness. Read this one first; it is the part that took the work.
- `CalendarScreenshotTest.kt`, `TodoScreenshotTest.kt` — patterns to copy. The To-do one is not
  relevant to this PRD; the Calendar one largely is.
- `app/src/screenshotTestDebug/reference/**` — 15 committed reference PNGs. Ours will differ
  (different screens, and now a title field), so regenerate rather than reuse the images.

**Read them with `MSYS_NO_PATHCONV=1 git show <ref>:<path>`, not plain `git show`.** On this machine
Git Bash rewrites `ref:path` into `ref;path` and the command fails with *"unknown revision or path
not in the working tree"* — which reads like the file is missing when it is not. Verified: the
mangling hits `knowledge/**` and `.ai/**` paths but not `gradle/**` or `app/**`. I have added
`Bash(MSYS_NO_PATHCONV=1 git show*)` and `ls-tree` to the standing ledger, because the baseline
`Bash(git show*)` rule does not match a command with a leading environment assignment. **This also
affects you elsewhere:** every `ISSUES.md` entry and every compacted `STATE.md` row cites a SHA to
be read back this way. Use the prefixed form everywhere, not just here.

This changes the evidence picture for criteria 1, 2, 7 and 9. You classified their perceptual
clauses as *"no command can ever prove"*. With reference-image screenshot tests, a committed
reference image **is** the standard, and `validateDebugScreenshotTest` fails when rendering drifts
from it. That does not make a human's first judgement unnecessary — someone still has to look at the
first generated image and agree it is right — but after that the property is pinned and regressions
are caught. Treat those four as: **human approves the reference image once, machine defends it
thereafter.** Say exactly that at Final Verification rather than either claiming they are proven or
claiming they are unprovable.

Add `:app:validateDebugScreenshotTest` to the green-build criterion (13) once the harness is in.

### `knowledge/DOMAIN.md` — created, rules 1 and 2 only

Created by me at `knowledge/DOMAIN.md` with the future-date rule and the `updatedAt` ordering rule.
Rules 3 and 4 (dark-only, blue) were **not** included.
*Rationale:* your split is correct — 1 and 2 constrain behaviour a plausible-looking implementation
gets subtly wrong, and they outlive this run. 3 and 4 are product/design constraints already binding
as DoD criteria 1 and 2, and duplicating them buys nothing while adding two more things that can
drift. I wrote the boundary conditions explicitly (today allowed, tomorrow not; `createdAt`
unchanged on edit) so the rule is implementable from that file alone.

### Also noted, no action needed from you

`CLAUDE.md`'s broken `@`-imports: correct catch, correct handling. It is my file, filing it in
`knowledge/ISSUES.md` rather than editing it was the right call. I will fix it separately.

### One instruction for the rest of the run

Before you re-derive any infrastructure, check whether a prior run in this repository already built
it: `git branch --list 'loop/*'`, then `MSYS_NO_PATHCONV=1 git ls-tree -r --name-only <branch>`.
You had `Bash(git show*)` all along and the answer to Q4 was sitting in this repository's own
history. That is a gap in the harness, not a mistake by you — but for this run, look first.
