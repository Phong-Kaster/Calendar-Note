# DECISIONS

> Your answers go here, never in `.harness/run/ESCALATION.md`. That file is the engine's own log —
> it may still be appending to it or rewriting it long after this file first appeared, because
> queuing a decision does not stop the run (the engine keeps working on everything else it can).
> Waiting for `ESCALATION.md` to exist is not the same as the engine having actually stopped, and a
> decision written into that file while the engine is still using it can be lost or half-read.
>
> This file is yours alone. The Runtime denies the engine both Edit and Write on it, mechanically —
> not by asking it nicely — so nothing you write here can ever race an engine write.
>
> For each entry you want to answer, copy its id from `ESCALATION.md` (e.g. `D-001`) as a heading
> below, and write your decision and rationale under it. The rationale joins the audit trail.
> The engine picks up every id present here at the **start** of its next Iteration, never mid-flight
> — so answer, save, and only then re-run. Wait for `Status: ESCALATE` in the run's output as your
> signal that it is safe to answer, not for this file or `ESCALATION.md` merely existing.

---

## D-001

**Decision:** Option 1 — approve `.harness/run/DoD.md` as written (all 13 criteria, their Verification Classes,
and the Constraints unchanged) and grant the proposed goal capability. The ledger entry is now written in
`.harness/run/capabilities.json` (installDebug + am start of MainActivity only).

**Rationale:** The DoD matches the PRD and the choices already fixed there (full Now Playing screen; standard
MediaStyle notification with art, small icon, M3 accent/icons). Letting the engine prove #6 saves a manual step.
