# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins
> and the engine corrects this file.
>
> **Provenance.** Written at the bootstrap of `loop/music-player-v2` (2026-09-25), from `main` at `027d3ea`.
> Seeded from `.harness/knowledge/PROJECT.md` on the sibling branch `loop/music-player` (same feature, same
> base commit), keeping only entries re-checked against this tree. That branch's code is **not** here.

---

## Constraints

> Traps. If a Worker ignored one, the result would be **wrong**. Carried verbatim into every Worker Brief.
> The Fresh-Context Review checks the diff against every entry; a violation is a blocking finding.
> A Constraint outranks a literal reading of an acceptance criterion.

- **C-01 — Never write a colour literal; take every colour from `MaterialTheme.colorScheme`.** Never
  `Color(0xFF…)`, `Color.White`, `Color.Black` in new code (`Color.Transparent` / `Color.Unspecified` are
  fine). Do **not** copy the literals in `ui/component/CoreBottomBar.kt:85,93,137,147`,
  `ui/component/CoreTopBar.kt:59`, `ui/fragment/setting/component/SettingItem.kt`, `core/CoreLayout.kt:38`
  (`Color.Black`) or `.claude/figma-design-system.md` §4/§5 (white cards) — those are known defects on a
  black ground, not conventions. Always pass an explicit `color = MaterialTheme.colorScheme.onXxx` to
  `customizedTextStyle(...)`: its default (`ui/theme/Type.kt:56`) is `Color.White`.
- **C-02 — A Worker never edits `res/values/strings.xml` or `res/values-de/strings.xml`.** `values-de` is a
  shipped locale and AGP lint rates `MissingTranslation` an **error** (main already fails `lintDebug` with 4:
  `home_refresh`, `home_no_posts`, `exact_alarm`, `allow_exact_alarm_for_prayer_time` — recorded on
  `loop/music-player`, same base). Reference `R.string.xxx` and **list every new key with its English and
  German text in your report**; the Iteration writes both files. Name a key for its own words
  (`no_songs_found`), never its feature (`music_empty_state`) — `CLAUDE.md` § String content.
- **C-03 — A Worker never edits Iteration-owned shared files:** `AndroidManifest.xml`,
  `res/navigation/navigation_graph.xml`, `gradle/libs.versions.toml`, `app/build.gradle.kts`,
  `injection/*.kt`, `domain/enums/BottomBarDestination.kt`, `ui/component/CoreBottomBar.kt`, `README.md`,
  both `strings.xml`. List what you need wired (DI binding, manifest entry, dependency, nav destination) in
  your report.
- **C-04 — Unit tests run on a stub `android.jar` with only JUnit 4.** Any `android.*` call in a JVM test
  throws `RuntimeException("Method … not mocked.")`; `Build.VERSION.SDK_INT` is 0. No Robolectric, no mocking
  library, no coroutines-test. Put every decision a criterion depends on in plain Kotlin that takes its
  inputs as parameters (e.g. `sdkInt: Int`), and test that; never make `Cursor`, `Uri`, `MediaItem`,
  `MediaController`, `Build` or `Log` a test's subject. Domain models hold a `String` uri / `Long` id, never
  `android.net.Uri`.
- **C-05 — A Worker holds no git/build/test capability and cannot delete files.** Pick every file name
  before writing it. Code you write is compiled only after you return.
- **C-06 — Single-line `Text` uses `maxLines = 1` + `Modifier.basicMarquee(iterations = Int.MAX_VALUE)`, never
  `TextOverflow.Ellipsis` alone** (`.claude/jetpack-compose-ui.md` § Text). Evidence: `loop/music-player`
  iteration-1 review finding on its song row.
- **C-07 — A Media3 `MediaController` is used only on the main thread.** Build it with
  `MediaController.Builder(...).buildAsync()` and listen with `ContextCompat.getMainExecutor(context)`; never
  call `future.get()` on the main thread and never wrap controller calls in `withContext(ioDispatcher)` —
  this is an explicit exception to `repository-layer.md` § Dispatcher injection.
- **C-08 — A Koin `single` that holds a connection (a `MediaController`) is shared by every screen: one
  screen's teardown must never close it for the others.** Pair every `connect()` with one `release()`,
  reference-count inside the repository, and on `MediaController.Listener.onDisconnected` /
  `isConnected == false` rebuild on next use. Evidence: `loop/music-player` iteration-2 review B-1 (Music →
  Home → Music cleared the old ViewModel *after* the new one connected, silently dropping updates).
- **C-09 — Stopping a `MediaSessionService` uses `pauseAllPlayersAndStopSelf()`, never bare `stopSelf()`**,
  which cannot end a service the app's own `MediaController` still binds. Evidence: `loop/music-player`
  iteration 3.
- **C-10 — An API-gated theme attribute (`android:windowLightNavigationBar` = API 27, etc.) never goes in
  `res/values/themes.xml` bare: minSdk is 24 and lint rates `NewApi` an error.** Omit it, or add
  `tools:targetApi="NN"`. Evidence: iteration 2, T-001 attempt 1 failed `lintDebug` on
  `themes.xml:14`.
- **C-11 — A Worker can write only under `app/src/main/java/`; its Write is refused for `app/src/main/res/**`
  and `app/src/test/**` even when they are in its Declared File Scope.** For every such file, put its full
  content (or, for a vector drawable, its path data) in your report; the Iteration writes it. Code that
  references `R.drawable.x` still compiles once the Iteration adds the file. Evidence: iteration 3, T-003
  Worker refused on 4 drawables and `PlaybackQueuePolicyTest.kt`.
- **C-12 — "Is music still meant to play?" is `playWhenReady && playbackState !in {STATE_IDLE, STATE_ENDED}`,
  never `Player.isPlaying` and never `playWhenReady` alone.** `isPlaying` is false during a transient
  audio-focus loss (a phone call), so a swipe-away mid-call would stop the music; `playWhenReady` alone stays
  true after a playback error, keeping a dead service and a stale notification alive. Evidence: iteration 4
  review N-1 on `MusicPlaybackService.onTaskRemoved`.

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build | `./gradlew :app:assembleDebug` | 2026-09-25, run 2 iteration 3 (`BUILD SUCCESSFUL`); APK at `app/build/outputs/apk/debug/app-debug.apk` |
| Unit tests | `./gradlew :app:testDebugUnitTest` | 2026-09-25, run 2 iteration 3 (76 tests, 0 failures) |
| Lint | `./gradlew :app:lintDebug` | 2026-09-25, run 2 iteration 3 (0 errors, ~70 warnings; 4 are `UnusedResources` on the deliberate `media3_icon_*` overrides) |
| Install / launch | `./gradlew :app:installDebug`; `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity` | 2026-09-25, run 2 iteration 2 (device `3H164700ALT00000`, no on-device prompt) |
| Device | `adb devices` | 2026-09-25, iteration 2 |

- Success is the literal `BUILD SUCCESSFUL`. Never trust a piped exit code; run unpiped or with
  `set -o pipefail`, and read the verdict line. One failing task aborts the rest of a multi-task command.
- Test counts: `app/build/test-results/testDebugUnitTest/TEST-*.xml` (console prints nothing on pass).
- Fresh evidence (Verifier): a repeat run is UP-TO-DATE. `./gradlew :app:testDebugUnitTest --rerun` re-runs tests;
  for lint use `./gradlew :app:lintDebug --rerun-tasks` (a line with several `--rerun` tasks is refused). Check the
  report file's timestamp. Verified run 2 iteration 4.
- Lint text report: `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.
- A `FileSystemException … classes.jar … used by another process` is a Windows lock (stale daemon / IDE), not a
  code error: `./gradlew --stop`, then rerun.

## Architecture Conventions

- Single module `:app`, source package `com.example.skeleton` (`namespace`), installed as
  `applicationId com.example.myapplication` (`app/build.gradle.kts:16`) — adb commands use the latter.
- Single Activity + Fragments + Compose: `MainActivity` hosts a `NavHostFragment` for
  `res/navigation/navigation_graph.xml` (start destination `musicFragment` since run 1, `navigation_graph.xml:6`; global actions `toHome`
  (pops to graph), `toSetting` (pops to `homeFragment`), `toSettingLanguage`). Each screen
  `XxxFragment : CoreFragment()` overrides `@Composable ComposeView()`, which `CoreFragment` wraps in
  `MyApplicationTheme`. Screen files in `ui/fragment/<screen>/` (`XxxFragment`, `XxxUiState`, `XxxViewModel`,
  `component/`, `model/`). Use `core/CoreLayout` (topBar / bottomBar / content), `CoreTopBar(title)`.
- `safeNavigate` lives in `ui/util/NavigationUtil.kt`, not on `CoreFragment` (the `.claude` rule text is stale).
- Bottom bar: `CoreBottomBar()` hard-codes `listOf(Home)` | centre "+" | `listOf(Setting)`; adding a
  `BottomBarDestination` entry alone shows nothing.
- DI is Koin: `injection/AppModule.kt` includes database/datastore/repository/viewModel/network/locale
  modules. Bind repositories by interface with named args; `viewModel { XxxViewModel(dep = get()) }`. A
  ViewModel/repository constructor change must land in the same checkpoint as its `injection/` edit (the
  build breaks, or Koin fails at runtime, otherwise).
- ViewModel state: one `data class XxxUiState` with defaults; update only via
  `_uiState.value = _uiState.value.copy(...)`.
- Repositories never throw across the boundary; re-throw `CancellationException`; inject
  `ioDispatcher: CoroutineDispatcher = Dispatchers.IO` (except controller calls, C-07).
- KDoc on every class/function ending `@author Phong-Kaster`; named lambda arguments for `onXxx` callbacks
  (no trailing-lambda syntax); `customizedTextStyle(...)` for every `Text`, never `MaterialTheme.typography`.
- Permission pattern: Accompanist `rememberPermissionState` in a screen-local `XxxPermissionRequest`
  composable (see `ui/fragment/home/component/HomeRequestPermission.kt`). `HomeFragment` is **not** a good
  template otherwise (uses `MaterialTheme.typography`, Material `Card`).
- Icons: vector drawables `res/drawable/ic_<name>.xml`; there is no `material-icons-extended` dependency.
- `java.time` is usable (`isCoreLibraryDesugaringEnabled = true`, minSdk 24).

## Environmental Facts

- Windows 11, Git Bash + PowerShell, Gradle wrapper, AGP 9.0.1, Kotlin 2.2.10, compileSdk/targetSdk 36,
  minSdk 24. Android SDK path from git-ignored `local.properties`.
- No Media3 on main. On `loop/music-player`, Media3 **1.8.0** (`media3-exoplayer`, `media3-session`,
  `media3-common`) compiled; `ForwardingPlayer` is `@UnstableApi` → `@androidx.annotation.OptIn(UnstableApi::class)`.
  Media3 jars are zipped `.aar`s in the Gradle cache: a Worker cannot inspect the API, only a build can.
- The device used on earlier runs (MIUI) refuses injected input (`adb shell input` → `INJECT_EVENTS`) and
  `pm grant/revoke`; installable and inspectable (`dumpsys`, `logcat -b crash`), not tappable. Serial changed
  between runs; always re-run `adb devices`.
- A capability approved in `DECISIONS.md` is **not** in effect until the human writes it into
  `.harness/knowledge/capabilities.json` / `.harness/run/capabilities.json`
  (`{"entries":[{…,"allow":["<rule>"]}]}`); the Runtime compiles only those files (observed iteration 1, D-002).
- `adb shell dumpsys activity top | grep MusicFragment` shows which fragment is on screen without input.
- On `loop/music-player`, the notification player controls did not appear on the device until
  `POST_NOTIFICATIONS` was requested at runtime (commit `4c88c2d`).
- `python` is not an allowed command; make multi-file edits with the Edit tool. `adb shell dumpsys activity top`
  output is ~150 KB — grep the persisted result for `MusicFragment{`.
- Iteration 3 device: `10AECY1ZXG003MQ` — `installDebug` succeeded without an on-device prompt. `adb shell
  getprop` and `rm` are not allowed commands; write dumps inside the repo (`> file` outside it is refused).
- Media3 1.8.0 notification (verified run 2 iteration 2): `DefaultMediaNotificationProvider.Builder(ctx).build()` +
  `setSmallIcon(@DrawableRes)` compile; there is no accent-colour setter, so `service/MusicNotificationProvider.kt`
  wraps it as a `MediaNotification.Provider` and sets `Notification.color` on the returned and every callback
  notification. Notification buttons use library drawables `media3_icon_play|pause|next|previous` (present in
  `app/build/intermediates/runtime_symbol_list/debug/processDebugResources/R.txt`); same-named app drawables override them.
  That `R.txt` is the way to check a library resource name without opening the aar.
- `Cursor.getLong` returns 0 for a NULL cell; read nullable columns with `cursor.isNull(index)` first
  (`SongRepositoryImpl.readNullableLong`).
- `CoreTopBar` hard-codes colours (C-01); a new screen builds its own top bar from theme colours
  (see `ui/fragment/nowplaying/component/NowPlayingTopBar.kt`). `safeNavigate` / `safeNavigateUp` are imported as
  `com.example.skeleton.ui.util.NavigationUtil.safeNavigate`.
- Bash matcher (run 2 iteration 2): `cd <repo> && cat …` lines were accepted; lines chaining with `;`, `sed -i`,
  `ls`/`find` outside the repo, and gradle with `> file` redirects were refused — run gradle bare, and capture adb
  output with a single `adb … > file-in-repo.txt`.
- Media3 1.8.0 compiled with `Util.handlePlayPauseButtonAction` / `Util.shouldShowPlayButton` (`@UnstableApi`)
  and Guava `ListenableFuture`/`Futures` arriving transitively — no extra dependency needed.
- `adb logcat -d -b crash` on device `b56e2819` always holds `init` SIGABRT lines from boot; only lines naming
  `com.example.myapplication` count against DoD #6.
- Bash permission matcher: a `cd … && <command>` line counts as multiple operations and is refused; run `cd`
  in its own call (the working directory persists). `git ls-files` / `git ls-tree` are not in the baseline.
- The §11 Cleanup Commit is covered by the baseline: `Bash(git rm -r .harness/run*)` (checked run 2 bootstrap;
  in run 1 iteration 6 a different form was refused).
- `git -C <path> …` and `cd <path>; git …` lines are refused by the matcher; run git from the default working
  directory with plain `git …` (run 2 bootstrap).
- Android 12+ colours a MediaStyle notification from its artwork and Android 13+ draws its own media-control icons,
  so an app's accent colour / action icons show only on older Androids (run 2 bootstrap analysis; DoD #11).
- `.kotlin/` appears untracked after a build (Kotlin daemon data) and is not in `.gitignore`; do not commit it.
- `produceState(key1 = x)` keeps its previous value when the key changes (the holder is not keyed); a loader keyed
  on a song must set `value = null` first or the old song's picture lingers (run 2 iteration 3, T-004).
- A `StateFlow<NowPlaying>` drops an event that changes no field (e.g. a seek while paused); publish such events as a
  changing field — `NowPlaying.positionChangeCount` (run 2 iteration 3, T-005).
- `CoreFragment` is a plain `Fragment`; screens may override `onStart` / `onStop` (NowPlayingFragment gates its
  position poll that way).
- `git show <ref>:.harness/...` needs `MSYS_NO_PATHCONV=1` under Git Bash, which the matcher refuses; a
  commit SHA as the ref (`git show 4c88c2d:.harness/...`) works.

## Sources Consulted

- `PRD.md`, `CLAUDE.md` + `.claude/*.md`, `app/src/main/java/com/example/skeleton/ui/CLAUDE.md`,
  `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, `navigation_graph.xml`,
  `ui/theme/*`, `core/*`, `CoreBottomBar.kt`, `HomeFragment.kt`, `injection/*`,
  `.harness/knowledge/PROJECT.md` on `loop/music-player` (`4c88c2d`).
