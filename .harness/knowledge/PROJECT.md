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

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build | `./gradlew :app:assembleDebug` | **not yet on this branch** — denied at bootstrap (no standing capability). Verified on `loop/music-player` 2026-09-24 from the same base; APK at `app/build/outputs/apk/debug/app-debug.apk` |
| Unit tests | `./gradlew :app:testDebugUnitTest` | not yet on this branch (main: 1 test, `ExampleUnitTest`) |
| Lint | `./gradlew :app:lintDebug` | not yet on this branch (main: **BUILD FAILED**, 4 `MissingTranslation` errors) |
| Device | `adb devices` | not yet on this branch |

- Success is the literal `BUILD SUCCESSFUL`. Never trust a piped exit code; run unpiped or with
  `set -o pipefail`, and read the verdict line. One failing task aborts the rest of a multi-task command.
- Test counts: `app/build/test-results/testDebugUnitTest/TEST-*.xml` (console prints nothing on pass).
- Lint text report: `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.
- A `FileSystemException … classes.jar … used by another process` is a Windows lock (stale daemon / IDE), not a
  code error: `./gradlew --stop`, then rerun.

## Architecture Conventions

- Single module `:app`, source package `com.example.skeleton` (`namespace`), installed as
  `applicationId com.example.myapplication` (`app/build.gradle.kts:16`) — adb commands use the latter.
- Single Activity + Fragments + Compose: `MainActivity` hosts a `NavHostFragment` for
  `res/navigation/navigation_graph.xml` (start destination `homeFragment`; global actions `toHome`
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
- `adb shell dumpsys activity top | grep MusicFragment` shows which fragment is on screen without input.
- On `loop/music-player`, the notification player controls did not appear on the device until
  `POST_NOTIFICATIONS` was requested at runtime (commit `4c88c2d`).
- Bash permission matcher: a `cd … && <command>` line counts as multiple operations and is refused; run `cd`
  in its own call (the working directory persists). `git ls-files` / `git ls-tree` are not in the baseline.
- `.kotlin/` appears untracked after a build (Kotlin daemon data) and is not in `.gitignore`; do not commit it.
- `git show <ref>:.harness/...` needs `MSYS_NO_PATHCONV=1` under Git Bash, which the matcher refuses; a
  commit SHA as the ref (`git show 4c88c2d:.harness/...`) works.

## Sources Consulted

- `PRD.md`, `CLAUDE.md` + `.claude/*.md`, `app/src/main/java/com/example/skeleton/ui/CLAUDE.md`,
  `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, `navigation_graph.xml`,
  `ui/theme/*`, `core/*`, `CoreBottomBar.kt`, `HomeFragment.kt`, `injection/*`,
  `.harness/knowledge/PROJECT.md` on `loop/music-player` (`4c88c2d`).
