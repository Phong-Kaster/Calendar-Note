# LOOP ISSUES REPORT

_Last updated: 2026-09-24 - branch `loop/music-player` - iteration 2 (Phase 2)_

## Abandoned tasks

None.

## Unreachable tasks

None.

## Decisions awaiting an answer

None (D-001 approved 2026-09-24).

## Review findings not fixed

| Sev | Where | Finding |
|---|---|---|
| Minor | `data/mapper/SongMapper.kt:41` | Sort `title ASC` may be case-sensitive in MediaStore's SQLite (lowercase titles after uppercase). Unverified — no music-file check possible by command. |
| Minor | `ui/fragment/music/component/SongRow.kt` | UI imports `formatDuration` from `data.mapper` (UI → data dependency). |
| Minor | `data/mapper/SongMapper.kt:54` | Data class `SongRow` shares its simple name with the `SongRow` composable. |
| Minor | `data/service/MusicPlaybackService.kt` | Exported session service (required by DoD 9) accepts any controller — any installed app can control playback. Normal for a media app; add an `onConnect` allow-list if unwanted. |
| Info | `data/repository/impl/PlayerRepositoryImpl.kt` | Reference counting / reconnect logic has no unit test (it sits on `MediaController` + `Log`, C-04); covered only by the ViewModel-level fake and by DoD 10/11 on a device. |

## Awaiting a person

Not yet — the checklist is written by the Verifier once every `machine` criterion holds. `human` criteria 6, 7, 10, 11, 12, 13, 14 are all unsigned.

## Known verification limits

- DoD 2 (launch with permission granted *and* revoked): the device refuses `pm grant` / `pm revoke` and `install -g`, so only the not-granted launch was proved by command (MainActivity resumed, 0 crashes).
- Playback (DoD 10/11, incl. "previous goes to the previous song") is unverified by command: the device refuses injected taps.

## Assumptions recorded

See `.harness/run/ASSUMPTIONS.md` (A-001 … A-006). Tier-1 amendments in `AMENDMENTS.md` (iteration 1).
