# DECISION QUEUE

> Questions the engine could not answer within its authority. This file is the engine's own log.
> **Answer in `.harness/run/DECISIONS.md`, never here** — copy an entry's id as a heading there and write
> your decision underneath it, then re-run.
>
> Run Mode is **Autonomous**: this DoD approval is the only question this run will ask. Every later
> decision is taken by the engine and written to `ASSUMPTIONS.md`, where any of them can be overturned.

---

## D-001 - Approve the Definition of Done for the music player

- **Status:** pending
- **Type:** DoD approval
- **Iteration:** 0 (bootstrap)
- **Timestamp:** 2026-09-24
- **Blocks tasks:** T-001, T-002, T-003, T-004

### Question

Approve `.harness/run/DoD.md` (edit any criterion or its `machine`/`human` class freely before approving).
Please also glance at `ASSUMPTIONS.md` A-001 … A-006 — the engine's readings of what the PRD leaves open —
since the DoD is written on top of them.

### Context

PRD: a simple music player with play/pause, next, previous, and a foreground service whose notification
toggles the player; music from the device. The DoD has 15 criteria: 8 `machine` (build/test/lint, launch
without crash on the attached phone, MediaStore query + mapping, permission per API level, screen state
logic, control wiring, foreground-service manifest, README) and 7 `human` (list legibility, permission/empty
states, in-app playback, notification controls, notification tap, swipe-away stop, theme contrast). The
attached phone refuses injected taps, so everything that needs a tap is `human`.

Found at bootstrap: `main` already fails `lintDebug` with 4 `MissingTranslation` errors; the engine will add
the German strings (A-006) so criterion 1 can be green.

### Options Considered

1. Approve as written — consequences: Phase 1 (theme + Music library) starts on the next run.
2. Edit criteria / classes, then approve — consequences: the edited DoD becomes the exam.
3. Overturn an assumption (answer its `A-00N` id too) — consequences: the plan is re-shaped before code is written.

### Engine Recommendation

Option 1. Most likely edits worth considering: A-002 (whether Music should replace Home rather than sit beside
it) and A-004 (repeat-all vs stop at end of list).

### Proposed Capabilities (if any)

None — Autonomous mode runs under the Deny List (ENGINE.md §14.3).

### Decision

<!-- Answered in `.harness/run/DECISIONS.md`, under a heading `## D-001` - never here. -->
