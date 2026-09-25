# LOOP ISSUES REPORT

> Regenerated every iteration. Problems only. What succeeded is in the commit messages.

_Last updated: 2026-09-25 - branch `loop/music-player-v2` - iteration 1_

## Abandoned tasks

none

## Unreachable tasks

none

## Decisions awaiting an answer

| # | Question | Blocks |
|---|---|---|
| D-002 | The capabilities approved in D-001 are not in effect — create `.harness/knowledge/capabilities.json` and `.harness/run/capabilities.json` (exact content in `ESCALATION.md`) | T-001, T-002, T-003, T-004 |

## Human criteria unsigned

DoD #8–#16 (all 9 `human` criteria) — none can be checked until the feature is built.

## Review findings not fixed

none

## Assumptions recorded

- Branch name `loop/ music-player-v2` read as `loop/music-player-v2`.
- Playback service in a new `service/` package; raw MediaStore row holder in a new `data/mediastore/` package.
- Toolchain unverified on this branch: `./gradlew` still refused in iteration 1 (pending D-002).
