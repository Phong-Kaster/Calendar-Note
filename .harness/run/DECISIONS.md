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

## D-002

**Decision:** Phone attached (CPH2895, Android 16). Results: 1 FAIL, 2 pass, 3 pass, 4 pass, 5 FAIL, 6 pass, 7 pass.

- **1 FAIL (#7) — no album art.** Every behaviour works, but the human sees no album cover for the songs on Now Playing.
- **5 FAIL (#11) — no album art.** Same report: the album cover does not show (treat the notification card as affected
  too unless the engine can prove otherwise on the device).

The human said "all behaviours work well but I don't see the songs' album art". Not yet confirmed whether the
placeholder shows or whether the songs on the phone carry embedded art — the fix task should check that on the
device first. Hint for the fix: on Android 10+ the legacy `content://media/external/audio/albumart/<id>` address is
often empty; `ContentResolver.loadThumbnail(<audio content uri>, Size, null)` (API 29+) or
`MediaMetadataRetriever.embeddedPicture` usually returns the embedded cover. No new image library (DoD constraint).

**Rationale:** Human verification on the phone; everything else signed off.

**Addendum (evidence gathered after the answer above):** the phone holds only 2 songs, both in `Download/`:
`Ahrix - Nova.mp3` (no ID3v2 tag at all) and `Alan_Walker_-_Faded_..._(mp3.pm).mp3` (ID3v2, but no `APIC` frame).
Neither file carries embedded cover art, and the default music app shows no cover either. So "no art" on this phone
is expected; the missing-art placeholder is the correct result for these songs. Keep the fix task (so songs that DO
have art show it on Android 10+), but prove it with a unit test / code evidence, and do not treat these two songs as
a failing device case. The human will re-check #7/#11 with a song that has a cover, or accept the placeholder.
