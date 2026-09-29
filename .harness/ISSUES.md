# ISSUES

Run `loop/music-player-v3` ended **DONE_PARTIAL** (Iteration 9 Verifier, 2026-09-29). Every machine criterion was re-proved; nothing is abandoned or deferred. The one thing stopping DONE is the list below, which needs a person.

## Awaiting a person

Tick these in `.harness/run/DECISIONS.md` under a new heading (e.g. `## V-001`). An unticked item stays unsigned. Use the phone for 17-19 and 21; the machine pre-checks ran on the AVD (assumption A-006).

- [ ] **7 - Empty library.** Open: the app on a device with no music, permission allowed. Expect: a music-note icon and "No songs found on this device" centred on screen, not a blank area. *Driven on AVD (no media, permission granted): did not fail. Screenshot: `run/evidence/V2-c7.png`. Still unlooked-at: how it reads to a person.*
- [ ] **15 - Notification tap.** Do: play a song, press Home, pull down the shade, tap the media card's title (not a button). Expect: the app comes back on the library with the mini player (or Now Playing if that was open). Back should leave the app, not reveal a second copy. *Driven on AVD from: playing, Home, shade expanded. Did not fail: the same MainActivity was resumed, 1 in the task. Still unlooked-at: whether the screen it lands on feels right.*
- [ ] **17 - First launch on the phone.** Do: uninstall, install, open, tap Allow. Expect: your own songs in a scrollable list, each with a readable title, artist and `m:ss` duration. *Pre-checks 4, 6, 22 pass on AVD. The phone itself was NOT driven (it was in use).*
- [ ] **18 - Playing.** Do: tap a song. Expect: sound you can hear, and a mini player at the bottom with a title and a pause button. Tap it to open Now Playing: placeholder artwork, title, artist, a seek bar with times, and previous/play-pause/next/shuffle/repeat. Toggle shuffle and repeat and expect a visible on/off change. Drag the seek bar to about 80 %: it should land there. *Pre-checks 8, 10, 12, 13 pass on AVD. Seen while driving: a drag to 80 % landed near 62 %. Check whether the knob follows your finger.*
- [ ] **19 - Shade and lock screen.** Do: while playing, pull down the shade, then lock the phone and wake it. Expect: title, artist and previous/play-pause/next that each work, with no duplicate or blank notification. *Shade card driven on AVD: it showed title + prev/pause/next (`run/evidence/V2-c15-shade.png`). Lock screen NOT driven (the AVD has no keyguard).*
- [ ] **20 - Permission denied.** Do: uninstall, install, open, tap "Don't allow". Expect: "Allow access to your music so the app can show the songs on this phone." and an Allow button that brings the prompt back. After a second deny, the text becomes "Music access is turned off…" and "Open settings" goes to the app's settings. *Driven on AVD (5, 23): did not fail. Still unlooked-at: whether the wording is clear.*
- [ ] **21 - Overall (human-only).** Open the app cold, in both light and dark system theme. Expect: it looks like a music player, with no posts, "+" button, settings, language or rate screens. Text and the status-bar clock and icons should be readable in both themes.

## Assumptions

- A-001 … A-006 in `run/ASSUMPTIONS.md` (none Tier 3). A-006: device criteria were driven on the AVD with generated tones, not on the phone.

## Review findings recorded, not fixed (minor)

- `PlaybackState.hasNext/hasPrevious` are approximate under shuffle (T-002); `hasPrevious` is unused by the UI.
- Seek knob may snap back after release, or land short of the drag target (see 18); `dragFraction` is not reset when the song changes mid-drag.
- The "unknown artist" rule is duplicated in `NowPlayingUiState` and `SongItem.kt`.
- Now Playing content is not vertically centred on tall screens; CoreTopBar buttons are 32dp (smaller than the 48dp touch target).
- Dismissing the system permission dialog by tapping outside it counts as "permanently denied" (the button then opens Settings). Accepted.
