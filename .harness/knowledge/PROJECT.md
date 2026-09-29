# PROJECT KNOWLEDGE

> Engine-maintained cache of verified operational truth. Codebase wins on conflict.

**Repository class: template.** Evidence: single commit `027d3ea initial: Android Compose skeleton…`; `app_name` = "Android Compose Skeleton" (`res/values/strings.xml:2`); demo feature fetches JSONPlaceholder posts (`HomeFragment.kt`, `PostApi.kt`); `HomeViewModel` inserts "John Doe"; placeholder URLs in `common/Constant.kt`; `.claude/android-skeleton-project.md` calls it "a reusable skeleton app". Removals in the DoD therefore cover every demo feature the PRD does not use.

## Constraints

- **Never** take a colour from `CoreLayout`'s or `CoreTopBar`'s pattern (hardcoded `Color.Black` ground `core/CoreLayout.kt:38`, hardcoded white `ui/component/CoreTopBar.kt:59`). Source every colour from the theme (`MaterialTheme.colorScheme.*`); the theme is forced dark with a FULL role set (T-001 fixes this). Text/icons on the ground must contrast in both system themes.
- **Never** import anything from `ui/fragment/home/**`, `ui/fragment/setting/**`, `ui/component/ratebottomsheet/**`, `ui/component/CoreBottomBar.kt`, `ui/component/CoreTopBar4.kt`, `domain/enums/BottomBarDestination.kt`, `data/remote/**`, `data/database/**`, `data/datastore/**` or `ui/util/{RateUtil,AppUtil,LocaleManager,NetworkUtil}.kt` from new code — all are deleted in T-005 and the build would break. Copy the idea (e.g. the permission flow in `HomeRequestPermission.kt`), not the dependency.
- **Never** put `android.*`, `androidx.*`, `Uri` or `MediaItem` types in `domain/` (repository-layer.md). Store URIs as `String`. (`domain/enums/BottomBarDestination.kt:6` already violates this and is being deleted; do not copy it.)
- **Never** keep the playback queue only in a ViewModel. The queue lives as `MediaItem`s inside the Media3 player owned by `PlaybackService`; otherwise Next/Previous from the notification or lock screen do nothing.
- **Never** hand-roll `NotificationCompat`/`MediaSessionCompat` for playback. Use Media3 `MediaSessionService` + `DefaultMediaNotificationProvider`.
- **Never** call `customizedTextStyle(...)` without an explicit `color = MaterialTheme.colorScheme.onXxx` — its default is hardcoded `Color.White` (`ui/theme/Type.kt:56`). `CoreTopBar.kt` also hardcodes `Color.Blue`/`Color.White` (`:33,35,59,73,93`); do not copy those.
- **Never** run player commands inside `viewModelScope.launch(Dispatchers.IO)` (overrides `.claude/viewmodel-layer.md` for `PlayerRepository` only): a Media3 `MediaController` must be used on its application-looper thread; `PlayerRepositoryImpl` posts every command to the main thread itself and ViewModels call the plain `fun`s directly.
- **Never** use `kotlin.OptIn` for Media3 `@UnstableApi`; use `@androidx.annotation.OptIn(UnstableApi::class)` (Media3's lint check errors otherwise and `lintDebug` must be green).
- **Never** rely on `MediaItem.localConfiguration` surviving a `MediaController` hop: set `RequestMetadata.mediaUri` and have `PlaybackService` rebuild items in `onAddMediaItems`.
- **Never** decide "permission permanently denied" from a `rememberSaveable` flag alone (lost on process death); decide it from the result callback (denied + no rationale) and fall back to opening app Settings.
- **Never** write `_uiState.update { … }`; use `_uiState.value = _uiState.value.copy(...)` (`.claude/android-skeleton-project.md`).
- **Never** add Compose to `MainActivity`; `CoreActivity.onCreate` calls `setContent` then `MainActivity` replaces it with `activity_main.xml` (the UI lives in Fragments).
- **Never** edit `strings.xml`, `AndroidManifest.xml`, `navigation_graph.xml`, `injection/*.kt`, `MainApplication.kt`, Gradle files or `README.md` from a Worker task — the Iteration wires them. Report needed strings as `name = "English value"` in the manifest.

- **Never** call `safeNavigate` without `import com.example.skeleton.ui.util.NavigationUtil.safeNavigate` (it is an extension in the `NavigationUtil` object, not a `CoreFragment` member). When a screen fills `CoreLayout(bottomBar=...)`, the bar owns the nav-bar inset: do not add nav-bar padding to the list too.

## Toolchain (verified commands)

Run from repo root in Git Bash; `2>&1 > file` then read the tail. JDK 24 on PATH, AGP 9.0.1, Kotlin 2.2.10, Gradle 9.1.

| Purpose | Command | Verified |
|---|---|---|
| Build | `./gradlew.bat :app:assembleDebug` (BUILD SUCCESSFUL, ~60 s cold) | yes (2026-09-29, baseline) |
| Unit tests | `./gradlew.bat :app:testDebugUnitTest` | yes (baseline, exit 0) |
| Lint | `./gradlew.bat :app:lintDebug` — baseline FAILS with 4 pre-existing errors (`MissingTranslation` for `values-de`, e.g. `home_refresh`); expected to pass once `values-de` and demo strings are removed (T-005). Text report: `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt` | yes (fails as described) |
| Compile androidTest | `./gradlew.bat :app:assembleDebugAndroidTest` | yes (baseline, exit 0) |
| Device | `adb devices` → a physical phone is attached (serial changes between sessions: earlier `b56e2819`, now `10AECY1ZXG003MQ`, model V2408A, API 36); it holds real songs (`adb shell content query --uri content://media/external/audio/media --projection title`). AVD `astronex_test` also exists. | yes |
| Manifest/APK inspection | `apkanalyzer` in `~/AppData/Local/Android/Sdk/cmdline-tools/*/bin` | present |

Package under test: `applicationId = com.example.myapplication` (namespace is `com.example.skeleton`). Launch: `adb shell am start -n com.example.myapplication/com.example.skeleton.MainActivity`.

## Architecture Conventions

- Layers per `.claude/android-skeleton-project.md`: `domain/` (models, repository interfaces), `data/` (mapper, repository/impl), `ui/fragment/<screen>/` (Fragment + ViewModel + UiState + component/), `core/` (skeleton base classes — keep), `injection/` (Koin modules listed in `AppModule.kt`).
- A screen = `XxxFragment : CoreFragment()` overriding `@Composable ComposeView()`, private `XxxLayout(uiState, onXxx = {})`, wrapped in `CoreLayout`; ViewModels get DI in `ViewModelModule`. Every `Text` uses `customizedTextStyle(...)` (`ui/theme/Type.kt:52`), never `MaterialTheme.typography`. Named lambda args, positive-condition if/else, KDoc ending `@author Phong-Kaster`, previews per state.
- Real convention docs on disk: `.claude/{android-skeleton-project,repository-layer,viewmodel-layer,jetpack-compose-ui,usecase-layer,figma-design-system}.md`. `CLAUDE.md` @-imports `view-model-layer.md`, `jetpack-compose-ui-layer.md`, `wiki-connection.md` which do NOT exist — use the real names.
- Repositories: interface in `domain/repository/`, `XxxRepositoryImpl` in `data/repository/impl/`, mappers as extension functions in `data/mapper/`, fail-soft (no throwing), inject `ioDispatcher`, `TAG` in companion. MediaStore = "System-API wrapping" recipe F. Player commands are plain `fun`, hot state is `val state: StateFlow<X>`.
- Existing runtime-permission pattern: accompanist `rememberPermissionState` in `ui/fragment/home/component/HomeRequestPermission.kt` (accompanist stays as a dependency).
- Removal-order traps for the Iteration (T-005): `ui/component/CoreTopBar.kt:44` uses `dynamicStatusBarPadding()` defined in `CoreTopBar4.kt:101` (move it before deleting); `MainApplication.kt` imports deleted modules and `ProviderInstaller`; `core/config/AppConfig.kt:15` reads `BuildConfig.API_BASE_URL`; `UiErrorMapper.kt` imports Ktor; Room cannot compile a `@Database` with zero entities (remove Room entirely, incl. `ksp` plugin); `kotlin("plugin.serialization")` is applied in `app/build.gradle.kts:6` and root `build.gradle.kts`; `activity_main.xml` is the only `ConstraintLayout` user; `ExampleInstrumentedTest` asserts the applicationId.

## Environmental Facts

- `enableEdgeToEdge()` in `core/CoreActivity.kt:18` sets status-bar icon colour from the system theme and overrides `themes.xml` `windowLightStatusBar`; the Iteration switches it to `SystemBarStyle.dark(TRANSPARENT)` (Phase 1 wiring).
- `uiautomator dump` fails on continuously animating screens (marquee/ticker): set `settings put global animator_duration_scale 0` first and restore after. With phone + AVD attached always use `adb -s <serial>`. Tap permission dialogs by resource-id (`com.android.permissioncontroller:id/permission_allow_button|permission_deny_button`), not text (vivo V2408A may be localized).
- Media3 adds `ACCESS_NETWORK_STATE`/`INTERNET`-style entries to the merged manifest: remove with `tools:node="remove"` (DoD R2). Navigation pulls `kotlinx-serialization-core` and Material pulls `constraintlayout` transitively (allowed).
- Workers cannot delete files or run anything. Deletions and all builds are done by the Iteration.
- `local.properties` and `build/` are git-ignored. `.harness/run/` is committed on the Loop Branch until the Cleanup Commit.

- The AVD `astronex_test` can be shut down externally mid-boot (Iteration 7: graceful-shutdown request ~2 min after start) and attached phones change serials/models between runs (b56e2819 Xiaomi 23021RAAEG, 3H164700ALT00000 CPH2895). Start the AVD first thing in the Verifier, check `adb devices` before every drive step, and never assume the phone serial.
- Verifier R5 check: `python .harness/run/evidence/V-sweep.py` (reference sweep; theme members are allow-listed). It matches by name, so also check overloads by hand.

## Sources Consulted

- `CLAUDE.md`, `.claude/*.md`, `app/src/main/java/com/example/skeleton/ui/CLAUDE.md`, `PRD.md`, `.harness/loop/POLICIES.md`, manifest, Gradle files, all sources under `app/src/main`.
