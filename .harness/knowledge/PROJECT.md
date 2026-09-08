# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins and the engine corrects this file.

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build | `gradlew.bat assembleDebug` | no — proposed in D-001, not yet run (no standing capability granted yet) |
| Unit tests | `gradlew.bat test` (or `gradlew.bat testDebugUnitTest`) | no — same as above |
| Lint | `gradlew.bat lint` (or `gradlew.bat lintDebug`) | no — same as above |

## Architecture Conventions

- Clean architecture: `domain/` (model + repository interfaces, Android-free) → `data/` (`database/local/
  {entity,dao}`, `mapper/`, `repository/impl/`, `remote/`) → `ui/fragment/<feature>/` (Fragment + UiState +
  ViewModel + `component/`) → `injection/` (Koin modules). See `.claude/repository-layer.md`,
  `.claude/android-skeleton-project.md`, `.claude/viewmodel-layer.md`, `.claude/jetpack-compose-ui.md`,
  `.claude/usecase-layer.md` for the authoritative detail.
- Every Fragment extends `core/CoreFragment.kt`, which owns the `ComposeView()` entry point and wraps
  content in `MyApplicationTheme()`. Screen shape: thin `XxxFragment` + private `XxxLayout(uiState, onX)`
  composable + `@Preview` at file end (see `HomeFragment.kt`, `SettingFragment.kt`).
- Room: `data/database/local/AppDatabase.kt` (currently `version = 2`), migrations in
  `data/database/local/Migration.kt` as top-level `val MIGRATION_x_y = object : Migration(x, y) { ... }`
  with hand-written `CREATE TABLE IF NOT EXISTS` SQL — no Room schema-export JSON is checked in
  (`exportSchema = false`).
- DI (Koin): `injection/DatabaseModule.kt` (DB + DAO singles), `injection/RepositoryModule.kt`
  (`single<XxxRepository> { XxxRepositoryImpl(...) }`), `injection/ViewModelModule.kt`
  (`viewModel { XxxViewModel(...) }`), all included by `injection/AppModule.kt`.
- Navigation: Fragment-based via `app/src/main/res/navigation/navigation_graph.xml`; entries reached from
  `ui/component/CoreBottomBar.kt` + `domain/enums/BottomBarDestination.kt`, which today hard-codes exactly
  two destinations (Home, Setting) around an unused center "+" button.
- Theming: `ui/theme/Theme.kt`'s `MyApplicationTheme` currently defaults `dynamicColor = true`, which
  overrides any custom `LightColorScheme`/`DarkColorScheme` on API 31+ — a fact worth knowing before
  touching theme colors on any future feature. Dark mode is driven by
  `darkTheme = isSystemInDarkTheme()`, not by `CoreFragment`'s (dead) `enableDarkMode` field.
- Text style: use `customizedTextStyle(...)` from `ui/theme/Type.kt`, not `MaterialTheme.typography`
  (`SettingFragment`/`SettingLanguageFragment` follow this; `HomeFragment` is a pre-existing exception,
  not a pattern to copy).

## Environmental Facts

- Windows repository; use `gradlew.bat`, not `./gradlew`.
- `minSdk = 24`, `isCoreLibraryDesugaringEnabled = true` — `java.time` is safe both on-device and in JVM
  unit tests.
- Test source set (`app/build.gradle.kts`) declares only `testImplementation(libs.junit)` (JUnit 4.13.2).
  No `kotlinx-coroutines-test`, Room-testing artifact, Turbine, MockK, or Robolectric. `koin-test` is
  cataloged in `gradle/libs.versions.toml` but unused.
- `kotlinx-coroutines-core` (providing `runBlocking`) is expected to already be reachable on the JVM test
  classpath transitively via `room-ktx`/`lifecycle-runtime-ktx`, since AGP puts a module's `implementation`
  dependencies on its unit-test compile classpath — but this has not yet been empirically verified by an
  actual `gradlew.bat test` run (no standing build/test capability granted yet). See `ESCALATION.md` D-001.
- No root `README.md` exists yet, though `.claude/android-skeleton-project.md` requires one with a package
  tree, to be added/updated whenever top-level packages change.
- `CLAUDE.md`'s `@`-imports reference `.claude/view-model-layer.md`, `.claude/jetpack-compose-ui-layer.md`,
  `.claude/wiki-connection.md` — none exist on disk. Real files are `.claude/viewmodel-layer.md` and
  `.claude/jetpack-compose-ui.md`; no wiki-connection file exists at all. Pre-existing, out of this run's
  scope.

## Sources Consulted

- `PRD.md`, `CLAUDE.md`, `.claude/android-skeleton-project.md`, `.claude/repository-layer.md`,
  `.claude/viewmodel-layer.md`, `.claude/jetpack-compose-ui.md`, `.claude/usecase-layer.md`.
- `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/res/navigation/navigation_graph.xml`.
- `app/src/main/java/com/example/skeleton/{core,ui,domain,data,injection}/**` (Home/Setting/Post/UserAction
  verticals as the reference pattern).
