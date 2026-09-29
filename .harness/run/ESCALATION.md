# DECISION QUEUE

> Answer in `.harness/run/DECISIONS.md` under a heading naming the entry id, never here.

---

## D-001 - Approve the Definition of Done

- **Status:** answered (2026-09-29, approved as written; Iteration 2)
- **Type:** DoD approval
- **Iteration:** 1
- **Timestamp:** 2026-09-29
- **Blocks tasks:** T-001, T-002, T-003, T-004, T-005, T-006

### Question

Approve `.harness/run/DoD.md` (edit freely before approving): acceptance criteria 1–24, Removals R1–R8, Constraints. No standing capabilities are proposed (Autonomous mode runs under the Deny List).

### Context

Template repository (demo Home/Setting/Language/rate-sheet, JSONPlaceholder, Room, Ktor, Lottie, location/alarm permissions). PRD: rebuild a music player with a notification controller, delete unneeded code, open the app as a real player, source = on-device music. Attached phone (API 36) holds real songs; baseline `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest` pass.

Choices made by the engine as assumptions (`ASSUMPTIONS.md` A-001…A-005): request POST_NOTIFICATIONS; screens = Library + mini player + Now Playing (seek, prev/next, shuffle, repeat), no tabs; Media3 is the only new dependency; keep playing on swipe-away if playing; English UI, keep applicationId, label "Music Player". Note: Removals contradict `.claude/*.md` rules that call AppConfig, safeApiCallFlow, Room, DataStore, CoreBottomBar 'skeleton-provided'; approving accepts that (the engine does not edit rule files). Removals (approve or edit in DoD): all demo screens, network/Room/DataStore/Lottie/Play/TLS stack, location/alarm/INTERNET permissions, `values-de`, demo resources.

### Options Considered

1. Approve as written - consequences: run proceeds T-001…T-006.
2. Edit DoD first (e.g. keep Room/DataStore/Settings, add search/playlists) - consequences: engine replans under the edited DoD.

### Engine Recommendation

Approve as written; edit any Removal you want kept.

### Proposed Capabilities (if any)

None.

### Decision

Answered in `.harness/run/DECISIONS.md` under `## D-001`.
