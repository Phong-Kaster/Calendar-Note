# ISSUES

- Unreachable-by-tooling: on-device drive for T-001 (DoD 4,5,6,17,20,22,23) and T-002 (DoD 3,8-11,15,19); phone `adb install` blocks on an on-device installer confirmation, so the Verifier must use AVD `astronex_test`. Earlier note: not yet done; phone was locked and `adb shell input` denied (INJECT_EVENTS). Retry with AVD `astronex_test` / unlocked phone.
- Awaiting a person (later): human criteria 7,15,17,18,19,20,21 unsigned.
- Assumptions A-001..A-005 recorded in `run/ASSUMPTIONS.md`.
- Minor: dismissing the system permission dialog by tapping outside is treated as "permanently denied" (button then opens Settings) - accepted.
- Review finding not fixed: `PlaybackState.hasNext/hasPrevious` are approximate under shuffle (T-002, minor).
