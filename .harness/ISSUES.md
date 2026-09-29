# ISSUES

- Open task T-007: R5 fails — `NavigationUtil.safePopBackstack` (2 overloads) and `safeNavigate(NavDirections)` unreferenced (Verifier, Iteration 7).
- Device drive not yet done for DoD 4-13,15,17-20,22-24: Iteration 7 AVD `astronex_test` was shut down externally before boot completed; attached phone changed (b56e2819 → CPH2895). Next Verifier drives on the AVD.
- Awaiting a person (later): criteria 7,15,17,18,19,20,21 unsigned.
- Assumptions A-001..A-005 recorded in `run/ASSUMPTIONS.md` (none Tier 3).
- Minor: dismissing the system permission dialog by tapping outside is treated as "permanently denied" (button then opens Settings) - accepted.
- Review finding not fixed: `PlaybackState.hasNext/hasPrevious` are approximate under shuffle (T-002, minor).
- Review notes not fixed (T-003/T-004, minor): seek knob may briefly snap back after release; `dragFraction` not reset when the song changes mid-drag; "unknown artist" rule duplicated in `NowPlayingUiState` and `SongItem.kt`; `PlaybackState.hasPrevious` unused by UI; Now Playing content not vertically centred on tall screens; CoreTopBar buttons 32dp (<48dp touch target).
