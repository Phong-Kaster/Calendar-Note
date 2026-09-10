# AMENDMENTS

> Append-only log of plan mutations. Every Tier-1 amendment is logged here as it is applied; every
> Tier-2/3 change is logged here once the human has decided it.
>
> One entry per amendment: timestamp, tier, reason, affected tasks, decision, expected impact.

---

## A-001 — Notes gain a `title` field

- **Timestamp:** 2026-09-10 (iteration 1, bootstrap)
- **Tier:** 1 — task amendment within the approved shape. The *decision* itself was Tier 3 (it
  changed intent), but the human made it directly by editing `.ai/DoD.md` before approving; what is
  logged here is only the propagation of that decision into the task files.
- **Reason:** the human approved the DoD and overruled assumption **A5**. The engine had proposed
  body-only notes, following the inferred data model in `PRD.md` §4, which lists `content` and marks
  a title "unconfirmed". The human's rationale, recorded in `DoD.md`: a reverse-chronological list of
  every note in the app is the primary navigation surface, and it is far harder to scan without
  titles — and the engine's own point that this is expensive to retrofit is the reason to do it now.
- **Decision:** `Note` carries `title: String` alongside `content: String`. The title may be blank; a
  blank title falls back to the first line of the body for display. DoD criteria 3, 4 and 6 were
  edited by the human to match.
- **Affected tasks:** T-002 (entity, migration, domain model, mapper, Home row), T-003 (create
  screen), T-004 (edit screen). T-001 and T-005 through T-009 are unaffected.
- **Expected impact:** small and entirely front-loaded. `title` is one more `TEXT NOT NULL DEFAULT ''`
  column in the same `CREATE TABLE` that T-002 was already going to write, one more field through the
  mapper, and one more editor field on the Note screen. Because no `NoteEntity` exists yet, this
  costs nothing extra — which is precisely why the human made the call now rather than after T-002
  had shipped a table without it. No change to the task graph, the dependency order, or the evidence
  strategy.

---

## A-002 — Import the existing screenshot harness; the evidence strategy changes

- **Timestamp:** 2026-09-10 (iteration 1, bootstrap)
- **Tier:** 1 — adding a task and re-describing evidence, within a shape the human explicitly
  directed in escalation D-001. The underlying decision (Q4, Option B+) was Tier 2 and is the
  human's; this entry logs only the propagation.
- **Reason:** the engine reported that this repository "has no host-side test setup" and classified
  the perceptual clauses of DoD criteria 1, 2, 7 and 9 as *"no command can ever prove"*. **That was
  wrong, and the correction came from the human.** It is true of `main` and false of the repository:
  a prior Foreman run on the unmerged branch `loop/todo-calendar-screens` already built and verified
  a complete Compose Preview Screenshot Testing harness on this exact toolchain. The engine branched
  from `main` and never looked at sibling `loop/*` branches, so it re-derived a conclusion the
  repository's own history already refuted.
- **Decision:**
  1. **New task T-010 — import the harness**, placed between T-001 and T-002. Not re-derived:
     imported from `loop/todo-calendar-screens` (plugin, catalogue entries, `gradle.properties` flag,
     `ScreenshotScaffold.kt`). Reference PNGs are regenerated, not copied — our screens differ and
     now carry a title field.
  2. **The evidence class of criteria 1, 2, 7 and 9 changes** from "unprovable, human-inspection
     only" to **"the human approves the reference image once; the machine defends it thereafter"**.
     A committed reference image *is* a standard a command can fail against, which is exactly what
     the engine said could not exist.
  3. **Criterion 13 gains `:app:validateDebugScreenshotTest`** once the harness is in (human's
     instruction in D-001). ⚠️ `.ai/DoD.md` itself was **not** edited to say so — it is approved and
     therefore immutable to the engine (Invariant 2). The instruction is honoured from here and from
     the task files. If the human wants the DoD file to match, they paste that task name into
     criterion 13 themselves.
  4. **Every task's "needs human inspection" line is re-scoped** to "approve the reference image
     once" wherever a screenshot can pin the property.
- **Affected tasks:** T-010 (new); T-001, T-002, T-004, T-005, T-006, T-007, T-008, T-009 (evidence
  sections); `.ai/PLAN.md` (strategy, task graph, risks).
- **Expected impact:** substantially more of the DoD becomes machine-defended, and the T-009 human
  checklist shrinks to first-time reference-image approval rather than a full manual walkthrough on
  every criterion. Cost: one extra task, one Gradle plugin, and a set of reference PNGs in the
  repository. The prior run's own test file names the three regressions this catches — an invisible
  has-notes dot on today's fill, a selection that never reached the grid, and an unexplained blank
  gap for an empty day — all of which passed a green unit suite.
- **Standing lesson, recorded in `knowledge/PROJECT.md`:** before deriving that this repository
  cannot do something, check the sibling `loop/*` branches. `git branch --list 'loop/*'` was
  available under baseline capabilities the whole time.
