# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins and the engine corrects this file.

## Toolchain (verified commands)

<!-- Only commands that have actually been run successfully. Record the command and what "success" looks like. -->

| Purpose | Command | Verified |
|---|---|---|
| Build (debug APK) | `./gradlew :app:assembleDebug` | **no** — granted, not yet run |
| Compile only (faster) | `./gradlew :app:compileDebugKotlin` | **no** — granted, not yet run |
| Unit tests (JVM) | `./gradlew :app:testDebugUnitTest` | **no** — granted, not yet run |
| Lint | `./gradlew :app:lintDebug` | **no** — granted, not yet run |
| Screenshot tests — validate | `./gradlew :app:validateDebugScreenshotTest` | **no** — granted, not yet run *here* (verified on `loop/todo-calendar-screens`) |
| Screenshot tests — re-record | `./gradlew :app:updateDebugScreenshotTest` | **no** — granted, not yet run *here* (verified on `loop/todo-calendar-screens`) |

**None of the above has been executed on this branch.** At bootstrap `./gradlew --version` was
refused by the permission layer; the capability was then granted through escalation D-001
(`knowledge/capabilities.json`) but only *after* that iteration's permissions had been compiled, so
the first run is still pending. Until a build has actually run, treat every command in this table as
inferred from `settings.gradle.kts` (single module `:app`) and the AGP task naming — except the two
screenshot tasks, which a prior run did execute successfully on `loop/todo-calendar-screens`.

`updateDebugScreenshotTest` **overwrites** the committed reference images. It is never the fix for a
failing `validateDebugScreenshotTest` — that re-baselines the regression the test existed to catch.

## Architecture Conventions

Distilled from `CLAUDE.md` + `.claude/*.md` (human-owned rule files, never edited by the engine) and
confirmed against the code that exists.

- **Single module** `:app`, package `com.example.skeleton`, `applicationId com.example.myapplication`.
- **Single Activity + Fragments + Compose.** `MainActivity` hosts a `NavHostFragment`
  (`res/layout/activity_main.xml`) driving `res/navigation/navigation_graph.xml`. Each screen is a
  `Fragment` whose UI is Compose, hosted by `CoreFragment`'s `ComposeView()` override. The PRD's
  "hybrid vs. RecyclerView" open question is therefore already answered by the codebase: hybrid.
- **Screen spine:** `XxxFragment : CoreFragment()` (owns ViewModel, navigation, side effects) +
  `private fun XxxLayout(uiState, on...: () -> Unit = {})` (pure UI, previewable, no navigation).
  Files live in `ui/fragment/<screen>/` with `XxxFragment.kt`, `XxxUiState.kt`, `XxxViewModel.kt`,
  and screen-local composables under `component/`.
- **Scaffolding:** use `core/CoreLayout.kt` (not raw `Scaffold`). It already paints a
  `Color.Black` background and takes `topBar` / `bottomBar` / `content`. `CoreTopBar` handles
  status-bar padding itself.
- **Typography:** `ui/theme/Type.kt` exposes `customizedTextStyle(fontSize, fontWeight, lineHeight,
  color)` backed by `InterFontFamily` (fonts present in `res/font/`). Default text colour is
  `Color.White`. House rule: use it for every `Text`, never `MaterialTheme.typography`.
- **Clean architecture layers:** `domain/model`, `domain/repository` (interfaces, Android-free),
  `data/database/local` (`AppDatabase`, `dao/`, `entity/`, `converter/`, `Migration.kt`),
  `data/repository/impl`, `data/mapper`, `data/remote`, `common/` (`Outcome<T>`, `Constant`),
  `core/extension/...`, `injection/` (Koin modules), `ui/`.
- **DI is Koin**, wired in `injection/AppModule.kt` from `databaseModule`, `datastoreModule`,
  `repositoryModule`, `viewModelModule`, `networkModule`, `localeModule`. Repositories are bound by
  interface with named arguments; ViewModels use `viewModel { }`.
- **Room:** `AppDatabase` is at `version = 2`, `exportSchema = false`, database file
  `"app_database"`, `@TypeConverters(DateConverter::class)` (`Date` ↔ `Long`). Migrations are
  explicit objects in `data/database/local/Migration.kt` and registered via `.addMigrations(...)` in
  `injection/DatabaseModule.kt`. Adding an entity means: entity + DAO + register in `@Database` +
  bump `version` + write the migration + expose the DAO in `databaseModule`.
- **ViewModel state:** one `data class XxxUiState` with defaults for every field; updates are always
  `_uiState.value = _uiState.value.copy(...)` (never `.update { }`); `TAG` is an instance
  `private val`, not a companion.
- **Repositories** never throw across the boundary — they return `null` / `emptyList()` /
  `common.Outcome<T>`, and re-throw `CancellationException`.
- **Strings:** every user-visible string goes in `res/values/strings.xml`, appended at the
  end, named for the words themselves (`<string name="download">`, not `<string
  name="feature_download">`).
- **`java.time` is safe on this minSdk.** `minSdk = 24`, but `isCoreLibraryDesugaringEnabled = true`
  with `libs.android.desugar.jdk.libs`, so `LocalDate`/`LocalDateTime` are usable. Existing
  extensions in `core/extension/date_and_time/` still carry `@RequiresApi(O)` annotations, which is
  a leftover, not a constraint.

## Environmental Facts

- **Platform:** Windows 11, PowerShell primary shell. Gradle wrapper `gradlew` /`gradlew.bat`,
  Gradle 9.1.0.
- **Android SDK** path comes from `local.properties` (`sdk.dir=C:\Users\MAC_PC\AppData\Local\Android\Sdk`),
  which is git-ignored — a fresh clone will not build until that file exists.
- `compileSdk 36`, `targetSdk 36`, `minSdk 24`, `jvmTarget 11`.
- **No CI configuration exists** in this repository — there is no pipeline to inherit commands from.
- **No lint baseline / no ktlint / no detekt** is configured. "Lint" means Android Gradle Plugin lint.
- **Test surface is a stub only:** `app/src/test/.../ExampleUnitTest.kt` and
  `app/src/androidTest/.../ExampleInstrumentedTest.kt`. There is no test that exercises app code, and
  no host-side (Robolectric) test infrastructure. `androidTest` requires a device or emulator.
- **No `README.md` exists** even though `CLAUDE.md` requires one with a package tree.
- The dependency catalogue is `gradle/libs.versions.toml`; all dependencies are declared through it.
  `kotlinx-coroutines-test` and Robolectric are **not** in it — any `suspend`/`Flow` unit test needs
  a dependency added first.
- **`.loop/`, `.claude/skills/` and `.agents/skills/` are permanently untracked** human-installed
  tooling. They show up in every `git status` and are not the debris of a crashed run.

### Reading files out of other commits and branches — Git Bash mangles the path

`git show <ref>:<path>` works normally for `app/**` and `gradle/**`. For **`knowledge/**` and
`.ai/**`** paths, MSYS rewrites `ref:path` into `ref;path` and the command fails with *"unknown
revision or path not in the working tree"* — which reads exactly like the file is missing when it is
not. Prefix those with the documented workaround, granted in `knowledge/capabilities.json`:

```
MSYS_NO_PATHCONV=1 git show <ref>:knowledge/PROJECT.md
MSYS_NO_PATHCONV=1 git ls-tree -r --name-only <ref>
```

This matters more than it looks: every `knowledge/ISSUES.md` entry and every compacted `STATE.md`
row cites a SHA meant to be read back this way. Use the prefixed form for those paths as a habit.

Distinguish the two failures — *"path 'x' exists on disk, but not in '<ref>'"* is Git telling you the
truth (the file genuinely is not on that branch), not the mangling.

### A prior Foreman run's work lives on `loop/todo-calendar-screens`

**Check sibling `loop/*` branches before concluding this repository cannot do something.** The
bootstrap iteration of the current run declared the perceptual DoD criteria unprovable while a
verified host-side screenshot harness sat on that branch, reachable with baseline capabilities the
whole time. `git branch --list 'loop/*'` costs nothing.

What is there (tip `969f278`), verified working on AGP 9.0.1 / Kotlin 2.2.10:

- **Compose Preview Screenshot Testing**, host-side via layoutlib — no emulator, no device, no `adb`.
  Four pieces, all required: the `com.android.compose.screenshot` plugin +
  `screenshot-validation-api` (version `0.0.1-alpha15`) in `gradle/libs.versions.toml`;
  `alias(libs.plugins.screenshot)` and
  `experimentalProperties["android.experimental.enableScreenshotTest"] = true` and the two
  `screenshotTestImplementation` lines in `app/build.gradle.kts`;
  `android.experimental.enableScreenshotTest=true` in `gradle.properties`; and the test sources in
  `app/src/screenshotTest/kotlin/...`, with references in `app/src/screenshotTestDebug/reference/`.
- **`ScreenshotScaffold.kt`** — wraps content in `MyApplicationTheme` on `colorScheme.background`.
  This is the part that took the work, and the reason it exists is recorded in its own KDoc: a bare
  `@Preview` renders against Studio's white with Material's baseline colours, which is how a screen
  full of hardcoded `Color.White` looked fine in the preview pane and was unreadable in the app.
- **That run's calendar grid is Sunday-first**, and its screenshot suite pins the weekday header
  against the grid for exactly that reason.
- Its `Note` model differs from this run's (`epochDay`/`title`/`createdAt` there;
  `date`/`title`/`content`/`createdAt`/`updatedAt` here), so its **reference PNGs are not reusable** —
  regenerate rather than copy.

## Sources Consulted

- `PRD.md`
- `CLAUDE.md` and the rule files it imports: `.claude/android-skeleton-project.md`,
  `.claude/repository-layer.md`, `.claude/view-model-layer.md` (present as `viewmodel-layer.md`),
  `.claude/jetpack-compose-ui-layer.md` (present as `jetpack-compose-ui.md`),
  `.claude/figma-design-system.md`, `.claude/usecase-layer.md`
- `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`,
  `gradle/wrapper/gradle-wrapper.properties`, `local.properties`, `.gitignore`
- `app/src/main/AndroidManifest.xml`, `res/navigation/navigation_graph.xml`, `res/values/themes.xml`,
  `res/values-v30/themes.xml`, `res/values/strings.xml`, `res/values/colors.xml`
- Existing sources under `core/`, `injection/`, `data/`, `domain/`, `ui/`
