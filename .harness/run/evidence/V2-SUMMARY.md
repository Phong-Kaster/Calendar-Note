# Verifier Iteration 9 - evidence summary (2026-09-29)

Device: AVD `emulator-5554` (`astronex_test`, API 34) with Alpha/Bravo/Charlie_Tone.wav (90 s each), per A-006. The phone was not used for any recorded result.

| # | Result | Evidence |
|---|---|---|
| 1,2 | pass: clean build; 6 test classes / 45 tests / 0 fail; androidTest compiles | V2-gradle.txt |
| 3 | pass: LAUNCHER, PlaybackService exported, fgsType 0x2, MediaSessionService action; READ_MEDIA_AUDIO, READ_EXTERNAL_STORAGE max 32, FGS, FGS_MEDIA_PLAYBACK, POST_NOTIFICATIONS | V2-manifest.txt |
| 4 | pass: pid alive, 0 crash, dialog has `permission_allow_button` | V2-c4-dump.xml |
| 5 | pass: deny by id, alive, 0 crash, "Allow access to your music..." and Allow button | V2-c5-dump.xml, V2-c5.png |
| 6 | pass: all 3 titles in dump | V2-c6-dump.xml, V2-songs.txt |
| 7 (pre-check) | did not fail: "No songs found on this device", centred | V2-c7-dump.xml, V2-c7.png |
| 8 | pass: PLAYING(3), description=Alpha_Tone | V2-c8-media.txt |
| 9 | pass: MediaStyle, mediaSession token, actions=3; isForeground=true types=00000002 | V2-c9-notif.txt, V2-c9-svc.txt |
| 10 | pass: pause->2, play->3, next->Bravo_Tone, previous->Alpha_Tone | transcript in HISTORY |
| 11 | pass: 10.7 s uptime; position 9327 -> 17817 at updated +9.09 s (extrapolated to now at least 10 s); AudioTrack uid 10201 state:started | V2-c11-audio.txt |
| 12 | pass: mini player title + "Pause", then "Play" after dispatch pause | V2-c12a/b-dump.xml |
| 13 | pass: title, artist, SeekBar, 0:21->0:27 across 3 s, swipe moved position 38.2 s -> 55.5 s | V2-c13*-dump.xml, V2-c13-seek.png |
| 14 | pass: label Music Player; R1-R8 hold | V2-badging.txt, V2-deps.txt, V2-sweep.txt |
| 15 (pre-check) | did not fail: tap on the shade media card resumes the same `MainActivity` record, only 1 in the task, library + mini player | V2-c15-act.txt, V2-c15-dump.xml |
| 16 | pass: every package dir appears in the README tree | - |
| 19 (pre-check) | shade card shows title, prev/pause/next (V2-c15-shade.png); lock screen NOT driven (AVD has no keyguard) | - |
| 22 | pass: 3 titles within 5 s after Allow, same pid | V2-c22-dump.xml |
| 23 | pass: two denies (2nd `permission_deny_and_dont_ask_again_button`), "Open settings" -> com.android.settings App info "Music Player" | V2-c23-dump.xml |
| 24 | pass: POST_NOTIFICATIONS dialog on first tap; denied by id; PLAYING(3) | V2-c24-dump.xml |
| R5 | pass: sweep lists only allow-listed InterFontFamily; UnusedResources 0; lint 0 errors | V2-sweep.txt |

Observation (not a criterion failure): a swipe to 80 % of the seek bar landed near 62 % (0:57 of 1:30). Criterion 13 only needs a move of at least 5 s. This matches the open review note "seek knob may snap back".
