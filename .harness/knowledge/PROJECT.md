# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins
> and the engine corrects this file.
>
> **Provenance.** Written at the bootstrap of the `loop/music-player` run (2026-09-24), from `main` at
> `027d3ea` (the pristine skeleton). A much longer PROJECT.md exists on the sibling branch
> `loop/calendar-note-app`; most of it describes calendar/alarm code that is **not on main**. Only the
> entries re-verified against `main` were carried over. Read that file for history, never as a description
> of this tree.

---

## Constraints

> Traps. If a Worker ignored one, the result would be **wrong**. Carried verbatim into every Worker Brief.
> The Fresh-Context Review checks the diff against every entry; a violation is a blocking finding.
> A Constraint outranks a literal reading of an acceptance criterion.

- **C-01 — Never write a colour literal; take every colour from `MaterialTheme.colorScheme`.** Never
  `Color(0xFF…)`, `Color.White`, `Color.Black` in new code (`Color.Transparent` / `Color.Unspecified` are
  fine). Do **not** copy `ui/component/CoreBottomBar.kt`'s pattern (`Color(0xFF35A0F5)` :83, `Color.White`
  :92/:133/:146) nor `core/CoreLayout.kt:38` (`Color.Black`) — those are known defects, not conventions.
  Pass an explicit `color = MaterialTheme.colorScheme.onXxx` to `customizedTextStyle(...)`: its default
  (`ui/theme/Type.kt:56`) is `Color.White`. Evidence: `ui/theme/Theme.kt` (dynamic light/dark scheme on
  main) vs the always-black ground of `CoreLayout.kt:38`.
- **C-02 — A Worker never edits `res/values/strings.xml` or `res/values-de/strings.xml`.** `values-de` is a
  shipped locale and AGP lint rates `MissingTranslation` an **error** — verified at bootstrap: `main` itself
  fails `lintDebug` with 4 such errors (`home_refresh`, `home_no_posts`, `exact_alarm`,
  `allow_exact_alarm_for_prayer_time`). Reference `R.string.xxx` and **list every new key with its English
  and German text in your report**; the Iteration writes both files. Name a key for its own words
  (`no_songs_found`), never its feature (`music_empty_state`) — `CLAUDE.md` § String content.
- **C-03 — A Worker never edits Iteration-owned shared files:** `AndroidManifest.xml`,
  `res/navigation/navigation_graph.xml`, `gradle/libs.versions.toml`, `app/build.gradle.kts`,
  `injection/*.kt`, `domain/enums/BottomBarDestination.kt`, `ui/component/CoreBottomBar.kt`,
  `ui/util/PermissionUtil.kt`, `README.md`, both `strings.xml`. List what you need wired (DI binding,
  manifest entry, dependency) in your report.
- **C-04 — Unit tests run on a stub `android.jar`.** Any `android.*` call in a JVM test throws
  `RuntimeException("Method … not mocked.")` (there is no `isReturnDefaultValues` on main, no Robolectric, no mocking
  library). Keep every decision a criterion depends on in plain Kotlin behind an injected seam, and test
  that; never write a test whose subject is `Cursor`, `Uri`, `MediaController`, `Build`, `Log`.
- **C-05 — A Worker holds no git/build/test capability and cannot delete files.** Pick every file name
  before writing it.
- **C-06 — Single-line `Text` uses `maxLines = 1` + `Modifier.basicMarquee(iterations = Int.MAX_VALUE)`, never
  `TextOverflow.Ellipsis` alone** (`.claude/jetpack-compose-ui.md` § Text; an ellipsis leaves no way to read the
  rest). Evidence: iteration-1 review finding on `ui/fragment/music/component/SongRow.kt` (the brief asked for ellipsis).
- **C-07 — The app is always dark: never make a system bar or window follow the phone's light mode.**
  `core/CoreActivity.kt` forces `SystemBarStyle.dark(...)` and `res/values/themes.xml` sets
  `windowLightStatusBar=false`; do not reintroduce `enableEdgeToEdge()` defaults or `isSystemInDarkTheme()`
  branches. Evidence: iteration-1 review (dark status-bar icons on the black ground in system light mode).

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build | `./gradlew :app:assembleDebug` | **yes** (2026-09-24) — APK at `app/build/outputs/apk/debug/app-debug.apk` |
| Unit tests | `./gradlew :app:testDebugUnitTest` | **yes** — 25 tests after iteration 1, 0 failures |
| Lint | `./gradlew :app:lintDebug` | **green** since iteration 1: 0 errors, 59 warnings (main had 4 `MissingTranslation` errors, fixed per A-006) |
| Device | `~/AppData/Local/Android/Sdk/platform-tools/adb devices` | **yes** — one device `b56e2819` attached |

- Success is the literal `BUILD SUCCESSFUL`. Never trust a piped exit code; run unpiped or with
  `set -o pipefail`, and read the verdict line. One failing task aborts the rest of a multi-task command.
- Test counts: `app/build/test-results/testDebugUnitTest/TEST-*.xml` (console prints nothing on pass).
- Lint text report: `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.
- Cold build of all three ≈ 2m20s.

## Architecture Conventions

- Single module `:app`, source package `com.example.skeleton` (`namespace`), installed as
  `applicationId com.example.myapplication` — adb commands use the latter.
- Single Activity + Fragments + Compose: `MainActivity` hosts a `NavHostFragment` for
  `res/navigation/navigation_graph.xml`; each screen `XxxFragment : CoreFragment()` overrides
  `@Composable ComposeView()`, which `CoreFragment` wraps in `MyApplicationTheme`. Screen files in
  `ui/fragment/<screen>/` (`XxxFragment`, `XxxUiState`, `XxxViewModel`, `component/`). Use `core/CoreLayout`
  (topBar / bottomBar / content), `CoreTopBar(title)`.
- Bottom bar: `CoreBottomBar()` hard-codes the tab lists (`listOf(Music, Home)` left since iteration 1,
  `listOf(...Setting)` right); adding an enum entry alone shows nothing.
- DI is Koin: `injection/AppModule.kt` includes database/datastore/repository/viewModel/network/locale
  modules; a new module must be added to that `includes`. Bind repositories by interface with named args;
  `viewModel { XxxViewModel(dep = get()) }`.
- ViewModel state: one `data class XxxUiState` with defaults; update only via
  `_uiState.value = _uiState.value.copy(...)`.
- Repositories never throw across the boundary; re-throw `CancellationException`; inject
  `ioDispatcher: CoroutineDispatcher = Dispatchers.IO`.
- KDoc on every class/function ending `@author Phong-Kaster`; named lambda arguments for `onXxx` callbacks
  (no trailing-lambda syntax); `customizedTextStyle(...)` for every `Text`.
- `java.time` is usable (`isCoreLibraryDesugaringEnabled = true`, minSdk 24).
- Existing permission helpers: `ui/fragment/home/component/HomeRequestPermission.kt` (Accompanist,
  notification/location/exact alarm), `ui/util/PermissionUtil.kt`.

## Environmental Facts

- Windows 11, Git Bash + PowerShell, Gradle wrapper, AGP 9.0.1, Kotlin 2.2.10, compileSdk/targetSdk 36,
  minSdk 24, jvmTarget 11. Android SDK path from git-ignored `local.properties`.
- Dependencies via `gradle/libs.versions.toml`. Test deps: `junit 4.13.2`, `kotlinx-coroutines-test 1.10.2`
  (added iteration 1). No Media3, no screenshot-test plugin on main.
- The attached device (per the calendar run's record) is MIUI and refuses injected input
  (`adb shell input` → `SecurityException: INJECT_EVENTS`): installable and inspectable (`dumpsys`,
  `logcat -b crash`), not tappable. Re-run `adb devices` before relying on it.
- The device **refuses `adb shell pm grant/revoke`** (`SecurityException: … GRANT_RUNTIME_PERMISSIONS`) and
  `adb install -g` grants nothing (verified iteration 1, API 35). Only the "permission not granted" launch path
  can be exercised by command; a granted path needs a person (or MIUI "USB debugging (Security settings)").
- `adb shell dumpsys activity top | grep MusicFragment` shows which fragment is on screen without input injection.
- `.kotlin/` appears untracked after a build (Kotlin daemon data) and is not in `.gitignore`; do not commit it.
- `git show <ref>:.harness/...` needs `MSYS_NO_PATHCONV=1` under Git Bash.

## Sources Consulted

- `PRD.md`, `CLAUDE.md` + `.claude/*.md`, `app/build.gradle.kts`, `gradle/libs.versions.toml`,
  `AndroidManifest.xml`, `navigation_graph.xml`, `ui/theme/*`, `core/CoreLayout.kt`, `CoreBottomBar.kt`,
  `HomeFragment.kt`, `injection/*`, `.harness/knowledge/PROJECT.md` on `loop/calendar-note-app`.
