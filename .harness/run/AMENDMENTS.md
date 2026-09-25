# AMENDMENTS

> Every plan mutation (Tier 1, logged) and every applied decision, with the human's rationale.

<!-- Newest first. -->

### A-006 (decision) - iteration 9 - D-003 applied
- **Decision:** option 3 — no song with an embedded cover exists on the phone; the music-note placeholder is accepted
  for #7 and #11 ("chấp nhận hình nốt nhạc cho mục 1 và 3"); #9 accepted from its D-002 PASS plus the placeholder
  acceptance (recorded by the supervisor in `DECISIONS.md`).
- **Human rationale:** "Both songs on the phone carry no embedded art (checked: no `APIC` frame). T-006's cover loading
  is proven by unit tests (AlbumArtUriTest 10/10) and code evidence; the human accepts the placeholder on device."
- **Effect:** #7, #9, #11 signed; all 13 criteria now signed or machine-proved → completion unblocked.

### A-005 (decision addendum) - iteration 7 - D-002 addendum applied
- **Addendum:** the phone's only songs (`Ahrix - Nova.mp3`, no ID3v2; `Alan_Walker_-_Faded_…mp3`, ID3v2 without
  `APIC`) carry no embedded cover, and the stock music app shows none either — the placeholder is correct for them.
  "Keep the fix task … but prove it with a unit test / code evidence, and do not treat these two songs as a failing
  device case. The human will re-check #7/#11 with a song that has a cover, or accept the placeholder."
- **Effect:** T-006 proved by `AlbumArtUriTest` + build/lint/review; no device art check attempted. The next Human
  Verification Request asks #7/#9/#11 with a song that has a cover, or acceptance of the placeholder.

### A-004 (decision) - iteration 6 - D-002 applied: two failed on-phone checks become T-006
- **Decision:** #8, #9, #10, #12, #13 pass; #7 FAIL and #11 FAIL — "all behaviours work well but I don't see the
  songs' album art" (notification treated as affected too). Hint: legacy `albumart/<id>` is often empty on Android
  10+; use `ContentResolver.loadThumbnail` or `MediaMetadataRetriever.embeddedPicture`; no new image library.
- **Human rationale:** "Human verification on the phone; everything else signed off."
- **Effect:** new task T-006 (Capable, Phase 3, scope in its task file) depends on T-001/T-002/T-004; DONE-candidate
  cleared. Signed off: #8, #10, #12, #13. #9 passed but T-006 changes the picture it checked, so it is re-asked with
  #7 and #11. PRD, DoD and architecture unchanged (the fix stays inside "album art via `ContentResolver`").

### A-003 (Tier 1) - iteration 3 - widen T-005's scope by one new file
- **Change:** T-005's Declared File Scope gains the new file `ui/fragment/nowplaying/model/NowPlayingCloseRule.kt`
  (plain-Kotlin close decision, per C-04). Disjoint from T-004.
- **Why:** the task asks for a unit-tested decision rule; the original scope had no plain-Kotlin home for it.
  PRD, DoD and architecture unchanged.

### A-002 (Tier 1) - iteration 2 - add T-005 from the Phase 1 review
- **Change:** new task T-005 "Now Playing stays in step while paused, off-screen, or restored empty" (review findings
  2, 3, 4, 5 on T-003's code). Scope `domain/model/NowPlaying.kt`, `domain/repository/MusicPlayerRepository.kt`,
  `data/repository/impl/MusicPlayerRepositoryImpl.kt`, `ui/fragment/nowplaying/NowPlayingViewModel.kt`,
  `ui/fragment/nowplaying/NowPlayingFragment.kt`, `ui/fragment/nowplaying/NowPlayingUiState.kt`. Depends on T-003.
  Phase 2 = T-004 + T-005 (disjoint: T-004 writes only `component/NowPlayingArtwork.kt`).
- **Why:** PRD, DoD and architecture unchanged; the findings are correctness gaps against DoD #8 (time stays right)
  and #9 (back / screen behaviour), cheaper as a focused task than a re-dispatch of the whole T-003 scope.

### A-001 (decision) - iteration 2 - D-001 applied
- **Decision:** option 1 — DoD approved as written (13 criteria, Verification Classes and Constraints unchanged);
  goal capability for `./gradlew :app:installDebug*` + `adb shell am start -n …MainActivity*` written by the human
  into `.harness/run/capabilities.json`.
- **Human rationale:** "The DoD matches the PRD and the choices already fixed there (full Now Playing screen;
  standard MediaStyle notification with art, small icon, M3 accent/icons). Letting the engine prove #6 saves a
  manual step."
- **Effect:** `DoD.md` immutable from now on; T-001, T-002, T-003 unblocked; DoD #6 stays `machine`.
