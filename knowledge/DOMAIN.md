# DOMAIN KNOWLEDGE

> **Human-owned.** Durable truth about this project's problem domain. Survives every feature run.
>
> The engine may **read** this file and may **propose** entries through an Escalation Request. It can
> never write here: runtime deny rules protect this path.
>
> **This file beats the codebase.** When they disagree, the code is wrong.

Created 2026-09-10 through escalation D-001, from `PRD.md` §5. Only the two entries that are genuine
behavioural invariants were taken. The PRD's other two §5 rules — dark-only, blue primary — are
product/design constraints already covered by DoD criteria 1 and 2, and are deliberately **not**
duplicated here.

## Rules and Invariants

### A note is never dated in the future

- **Rule:** A note's `date` may be the current local date or any date before it. It may never be
  after the current local date. Today is allowed; tomorrow is not. The boundary is inclusive of
  today — an off-by-one here (`>=` where `>` belongs) makes it impossible to write a note at all,
  which is why the boundary is stated explicitly rather than left to the implementation.
- **Enforcement point:** in the repository layer, below the UI, so that no caller can bypass it.
  A UI that merely hides the affordance does not satisfy this rule.
- **On violation:** the save is refused and the refusal is reported to the caller as a value
  (`common.Outcome`). It is not signalled by throwing.
- **Authority:** `PRD.md` §5.1 — "A user cannot create a note for a future date."
- **Applies to:** every write path — create and edit alike. Editing an existing note's date into
  the future is the same violation as creating one there.
- **Do not confuse with:** the calendar grid disabling future days. That is presentation
  (DoD criterion 9); this rule is about what the data layer will accept.

### Notes are ordered most-recently-touched first

- **Rule:** Any list of notes not otherwise specified is ordered by `updatedAt`, descending.
  "Touched" means created **or** edited: creating a note sets `createdAt` and `updatedAt` to the
  same instant; editing it sets `updatedAt` to the time of the edit and leaves `createdAt` alone.
- **Authority:** `PRD.md` §3.1 — "most recently created/edited note first" — and §5.2.
- **Applies to:** the Home screen list. A per-day list on the Calendar screen follows the same
  ordering unless a criterion says otherwise.
- **Do not confuse with:** ordering by `createdAt`. That looks correct on a fresh database and stays
  correct right up until the first note is edited, which is the failure this entry exists to prevent.

## Known Divergences

- None recorded. The codebase has no note feature yet as of 2026-09-10.
