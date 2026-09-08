# DECISION QUEUE

> Questions the engine could not answer within its authority. Queueing one does **not** stop the run:
> the engine marks the tasks that entry blocks and keeps working on everything else. The Runtime
> stops only when no executable task remains.
>
> Answer any number of entries - fill each `## Decision` section, then re-run. Unanswered entries
> stay queued and the tasks they name stay unselectable.

---

## D-001 - Approve the Definition of Done and standing capabilities

- **Status:** consumed (Iteration 1)
- **Full question, context, options, and decision:** archived in `HISTORY.md` under "Archived Decisions".

---

## D-002 - CoreLayout's hardcoded black background makes DoD criterion 30 architecturally unsatisfiable without touching Home/Setting

- **Status:** pending
- **Type:** Tier-2 (architecture-touching, cross-cutting blast radius)
- **Iteration:** 1
- **Timestamp:** 2026-09-08
- **Blocks tasks:** none directly (no remaining task's Declared File Scope needs to touch this to meet
  its own acceptance) — blocks only the final DoD sign-off at Verification (§ENGINE 11) on criterion 30.

### Question

DoD criterion 30 reads: "new screens read colors from `MaterialTheme.colorScheme`, never hardcoded."
Phase 1's Fresh-Context Review found that `TodoTaskItem.kt`, `CalendarMonthHeader.kt`, and
`CalendarDayCell.kt` use hardcoded `Color.White` for all text/icons, literally violating this criterion.
How should this be resolved?

### Context

I did not fix this inline because the reviewer traced *why* it's hardcoded: `core/CoreLayout.kt:38`
unconditionally paints the screen background `Color.Black`, regardless of `darkTheme`. T-001 only
overrode `primary`/`secondary`/`tertiary` in `Theme.kt` — `onBackground`/`onSurface` still fall back to
Material3's baseline defaults, which are **dark** in the light color scheme (e.g. `onBackground =
0xFF1C1B1F`). Swapping the flagged `Color.White` literals for `MaterialTheme.colorScheme.onBackground`
(the literal-compliance fix) would render near-invisible dark-on-black text for any user whose system is
in light mode — trading a DoD-checklist violation for a real, worse, visible bug.

The only way to make DoD 30 true in both letter and spirit is for `CoreLayout`'s background to itself
come from `MaterialTheme.colorScheme.background` instead of a hardcoded constant. But `CoreLayout.kt` is
a skeleton-provided core file outside every task's Declared File Scope in this run, and every existing
screen (`HomeFragment`, `SettingFragment`, `SettingItem`, `LanguageItem`) has the identical
hardcoded-`Color.White`-on-hardcoded-black pattern already — changing `CoreLayout`'s background without
also touching those pre-existing files would make Home/Setting's own hardcoded-white text invisible in
light mode, which is a real regression even though DoD criterion 31 only requires build+reachability
(not visual appearance) for Home/Setting, so it would not be *caught* by this run's own acceptance bar
while still being obviously wrong to ship.

### Options Considered

1. Leave Todo/Calendar's hardcoded `Color.White` as-is, consistent with the app's existing pattern —
   document DoD 30 as met "in spirit, not literally" for this run, and treat full theme-awareness
   (`CoreLayout` + Home/Setting + Todo/Calendar together) as a separate follow-up PRD. Consequence:
   ships working, readable screens today; DoD 30 remains formally unmet at final Verification unless its
   wording is amended (Tier 3, human-only).
2. Approve a new cross-cutting task, outside the original seven, that changes `CoreLayout.kt`'s
   background to `MaterialTheme.colorScheme.background` AND fixes every existing hardcoded-white call
   site app-wide (`HomeFragment`, `SettingFragment`, `SettingItem`, `LanguageItem`, plus this run's own
   Todo/Calendar files) so the whole app is genuinely theme-aware. Consequence: DoD 30 fully satisfied,
   but this is materially larger than anything in the original PRD/plan, touches files no task ever
   declared, and carries real visual-regression risk across the whole app that the original DoD's
   Fresh-Context Review process was never scoped to catch (single-Phase diffs, not whole-app sweeps).
3. Amend DoD criterion 30's wording (Tier 3 — human-owned intent, I cannot do this myself) to describe
   the existing, working, consistent pattern instead of demanding `MaterialTheme.colorScheme` literally.

### Engine Recommendation

Option 1 for this run, with Option 2 proposed as a separate follow-up PRD/goal. The blast radius and
regression risk of Option 2 inside this run is disproportionate to what T-001/T-002/T-005 were scoped to
change, and the existing pattern is not a new defect introduced by this run — it predates it and already
ships in Home/Setting today.

### Decision

<!-- HUMAN WRITES HERE: the decision AND its rationale. The rationale becomes part of the audit trail. -->
