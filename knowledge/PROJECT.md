# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins and the engine corrects this file.

## Toolchain (verified commands)

<!-- Only commands that have actually been run successfully. Record the command and what "success" looks like. -->

| Purpose | Command | Verified |
|---|---|---|
| Build (debug APK) | `./gradlew :app:assembleDebug` | **yes** — iteration 2. `BUILD SUCCESSFUL`; ~60s cold, ~10s warm |
| Compile only (faster) | `./gradlew :app:compileDebugKotlin` | **yes** — runs as part of the above |
| Unit tests (JVM) | `./gradlew :app:testDebugUnitTest` | **yes** — iteration 2. `BUILD SUCCESSFUL`; 38 tests as of iteration 5. Read the counts from `app/build/test-results/testDebugUnitTest/TEST-*.xml` — the console prints nothing when everything passes |
| Lint | `./gradlew :app:lintDebug` | **yes** — iteration 2. `BUILD SUCCESSFUL`, `0 errors, 56 warnings` (**it failed on the pristine baseline — see below**) |
| Screenshot tests — validate | `./gradlew :app:validateDebugScreenshotTest` | **yes** — iteration 3. `BUILD SUCCESSFUL`; ~15s. Read the count from `app/build/test-results/validateDebugScreenshotTest/TEST-preview-screenshot-test-engine.xml` |
| Screenshot tests — re-record | `./gradlew :app:updateDebugScreenshotTest` | **yes** — iteration 3. Writes PNGs under `app/src/screenshotTestDebug/reference/…` |

Success looks like a literal `BUILD SUCCESSFUL` line. Lint additionally writes
`app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`,
whose last line is the `N errors, M warnings` summary — read that rather than trusting the console
tail.

`updateDebugScreenshotTest` **overwrites** the committed reference images. It is never the fix for a
failing `validateDebugScreenshotTest` — that re-baselines the regression the test existed to catch.

### `updateDebugScreenshotTest` orphans reference images rather than replacing them

A reference filename ends in a hash of the preview's *parameters*: `PaletteAccents_Palette -
accents_479b9379_0.png`. Change the `@Preview` body only and the same file is overwritten in place.
Change `name`, `widthDp` or `heightDp` and the plugin writes a **new** file under a new hash and
leaves the old one sitting there. `validateDebugScreenshotTest` ignores the strays and stays green,
so nothing ever tells you. Iteration 3 produced three of them in two `update` runs.

**After any `update` run that changed a preview's parameters, list the reference folder and check
the surviving filenames against the case names** — otherwise dead images get committed and the
folder stops being readable. Note also that the filenames contain spaces (from `@Preview(name = …)`),
which breaks unquoted shell paths.

### The engine cannot delete files here

`rm` is not in the granted capability set (`POLICIES.md` rates deletion high-risk), so orphaned
references and any other stray file must be removed by a human or left untracked. Plan for it:
prefer not creating the stray in the first place — pick a preview's `name`/`widthDp`/`heightDp`
before recording it, not after.

### A hand-written Room migration has no test here — copy Room's own statement instead of writing one

Nothing in this repository can execute SQLite: there is no Robolectric, `MigrationTestHelper` needs
an instrumented device, and `fallbackToDestructiveMigration(false)` turns a migration that
disagrees with the entity into a **launch crash** rather than a degraded read. So a migration is
either right by construction or unverified.

It can be right by construction. After any build, Room's generated implementation sits at
`app/build/generated/ksp/debug/kotlin/com/example/skeleton/data/database/local/AppDatabase_Impl.kt`.
Its `createAllTables` holds the exact `CREATE TABLE` Room will use on a fresh install, and the
`_columnsXxx` map below it holds the `TableInfo` the first open is validated against. **Copy that
statement into the migration verbatim, backticks and all**, then read the column map to confirm
types, `NOT NULL` and primary key. Composing the SQL from the entity by eye is where the difference
creeps in — iteration 4 wrote a `DEFAULT ''` the entity does not declare (see amendment A-005).

### `git reset` and `git restore` are denied — `git rm --cached` is how you unstage

`git add -A <dir>` stages everything under it, including the orphaned reference PNGs that
`knowledge/ISSUES.md` says must stay untracked. Both obvious ways back are refused by the
permission layer; `Bash(git rm --cached*)` is in the baseline ledger and does exactly the job —
it drops a path from the index and leaves the file on disk.

**Name the individual files, never the folder.** `git rm --cached -r <dir>` on
`…/ThemeScreenshotTestKt/` would also untrack the six *committed* references living beside the
strays, and the next commit would delete them from the branch.

### `assembleDebug` + `testDebugUnitTest` green does **not** mean the tree compiles

`app/src/screenshotTest/` is a fourth source set that neither task touches. Change the signature of
a composable it renders and both report `BUILD SUCCESSFUL` while
`compileDebugScreenshotTestKotlin` is broken — found in iteration 5 by giving `CoreBottomBar` a
required parameter, which the two screenshot cases calling it did not pass.

**Run `:app:validateDebugScreenshotTest` in the same command as the build whenever a composable's
signature changes.** It is cheap (~15s) and it is the only task that compiles that source set. The
same applies to `app/src/androidTest/`, which nothing here can compile at all.

### Unit tests run against a *stub* `android.jar` — every framework call throws unless told not to

A JVM unit test compiles against a stub `android.jar` where every framework method **throws**
`"not mocked"` rather than doing nothing. One `Log.w` on an error path is therefore enough to make
that whole path untestable: the test crashes on the log line instead of failing on the behaviour.
Since iteration 5 `app/build.gradle.kts` carries
`testOptions { unitTests { isReturnDefaultValues = true } }`, which makes those stubs return
`0` / `false` / `null` instead. That is what lets `NoteRepositoryImpl`'s refusal path — DoD
criterion 10 — be tested at all (amendment A-008).

**The trap that comes with it: an unmocked framework call now returns a default silently.** So
`android.os.Bundle.putLong` is a no-op in a unit test, and
`NoteFragment.argumentsFor()` cannot be unit-tested — it returns an empty `Bundle` and every
assertion against it passes vacuously. A test that needs framework *behaviour* rather than
framework *silence* needs Robolectric, which is still not a dependency here. Do not write a test
whose subject is a `Bundle`, an `Intent`, a `Uri` or a `SharedPreferences`.

### Any new screen with a text field needs `imePadding()` — nothing here handles IME insets

`MainActivity` calls `enableEdgeToEdge()` and `SystemBarUtil` calls
`WindowCompat.setDecorFitsSystemWindows(window, false)`, so **the window is never resized when the
keyboard opens**, and `CoreLayout` passes `contentWindowInsets = WindowInsets(0,0,0,0)` — it
deliberately consumes nothing. Before iteration 5 there was no full-screen text input in the app,
so this had never bitten; the Note editor's first version scrolled correctly with the keyboard
closed and was unusable with it open. The viewport stayed full-screen height, the keyboard covered
the bottom 40% of it, and no amount of scrolling could lift the covered strip into view, because
the scroll range is `content − viewport` and the viewport was never shortened.

`Modifier.imePadding()` **before** `verticalScroll` in the chain — it has to shrink the viewport,
not the content inside it. `MainActivity` also carries
`android:windowSoftInputMode="adjustResize"`, which is what `WindowInsets.ime` needs below API 30.
**That half is unverified** — see `knowledge/ISSUES.md`; `values/themes.xml` sets
`windowTranslucentStatus`, which historically suppresses the back-ported inset, and nothing here
can run an API 24–29 image.

### A piped Gradle command reports the *pipe's* exit code, not Gradle's

`./gradlew … 2>&1 | tail -20` exits **0 even when the build failed**, because the exit status
belongs to `tail`. This is not theoretical: it is how a `BUILD FAILED` in this repository was first
mistaken for a pass. **Always read the `BUILD SUCCESSFUL` / `BUILD FAILED` line**, never the exit
code, whenever the command is piped. (`PIPESTATUS` and `set -o pipefail` are both rejected by the
permission layer here — the former as an expansion, the latter as a second operation.)

### Do not run Gradle in the background while editing sources

Gradle reads the working tree when it gets there, not when it was launched. A background
`testDebugUnitTest` started before a batch of edits compiled the half-written tree and reported a
`compileDebugKotlin` failure that had nothing to do with the baseline it was launched to measure.
Either finish the edits first, or run the command in the foreground.

### Lint fails the build on a missing German translation

`values-de/` is a shipped locale and AGP lint rates `MissingTranslation` as an **error**, so lint —
and therefore DoD criterion 13 — goes red the moment a string is added to `res/values/strings.xml`
without a matching entry in `res/values-de/strings.xml`. The baseline tree was already failing this
way on four strings before this run touched anything.

The failure message names `values/strings.xml`, i.e. the file you *did* edit, and never mentions the
German file you didn't — so it reads like the wrong problem. **Adding a user-visible string is a
two-file operation here.**

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
- **Scaffolding:** use `core/CoreLayout.kt` (not raw `Scaffold`). It takes `topBar` / `bottomBar` /
  `content` and paints `MaterialTheme.colorScheme.background` as the ground (iteration 2 — it used
  to hardcode `Color.Black`). `CoreTopBar` handles status-bar padding itself.
- **Theme:** one fixed dark scheme in `ui/theme/Theme.kt`; `MyApplicationTheme(content)` takes no
  `darkTheme` or `dynamicColor` parameter, and nothing reads the system light/dark setting. All 36
  Material 3 roles are assigned explicitly. Colour tokens live in `ui/theme/Color.kt`;
  `primary = ColorBlue2` = `#35A0F5`. **Read colours from `MaterialTheme.colorScheme.*`** —
  `.claude/figma-design-system.md` permits inline `Color(0xFF…)` literals, but DoD criterion 2 and
  `POLICIES.md` § User-Interface Defects override it for code this run writes.
  When adding or changing a token, **compute the contrast ratio against the black ground rather
  than judging the swatch** — this caught `outline` at 2.19:1 (under the 3:1 floor for a visible
  boundary) in iteration 2. On pure black every mid-grey looks plausible in isolation, and the
  failure shows up only as a border that silently is not there. `Color.kt` records the ratios in
  its KDoc where they were load-bearing.
- **Typography:** `ui/theme/Type.kt` exposes `customizedTextStyle(fontSize, fontWeight, lineHeight,
  color)` backed by `InterFontFamily` (fonts present in `res/font/`). Default text colour is
  `Color.White`. House rule: use it for every `Text`, never `MaterialTheme.typography`. The scheme's
  `onBackground`/`onSurface` are deliberately **pure** white to match that default — two nearly
  identical whites in one app is a defect nobody spots until they land side by side.
- **Clean architecture layers:** `domain/model`, `domain/repository` (interfaces, Android-free),
  `data/database/local` (`AppDatabase`, `dao/`, `entity/`, `converter/`, `Migration.kt`),
  `data/repository/impl`, `data/mapper`, `data/remote`, `common/` (`Outcome<T>`, `Constant`),
  `core/extension/...`, `injection/` (Koin modules), `ui/`.
- **DI is Koin**, wired in `injection/AppModule.kt` from `databaseModule`, `datastoreModule`,
  `repositoryModule`, `viewModelModule`, `networkModule`, `localeModule`. Repositories are bound by
  interface with named arguments; ViewModels use `viewModel { }`.
- **Room:** `AppDatabase` is at **`version = 3`** (1 = user actions, 2 = the demo posts table,
  3 = `notes`, added in iteration 4), `exportSchema = false`, database file `"app_database"`,
  `@TypeConverters(DateConverter::class)` (`Date` ↔ `Long`). Migrations are explicit objects in
  `data/database/local/Migration.kt` and registered via `.addMigrations(...)` in
  `injection/DatabaseModule.kt`. Adding an entity means: entity + DAO + register in `@Database` +
  bump `version` + write the migration + expose the DAO in `databaseModule`. `NoteEntity` stores a
  calendar day as an **epoch-day `Long`**, not through a converter — `DateConverter` handles
  `java.util.Date`, which is an instant rather than a day.
- **ViewModel state:** one `data class XxxUiState` with defaults for every field; updates are always
  `_uiState.value = _uiState.value.copy(...)` (never `.update { }`); `TAG` is an instance
  `private val`, not a companion.
- **Repositories** never throw across the boundary — they return `null` / `emptyList()` /
  `common.Outcome<T>`, and re-throw `CancellationException`.
- **The notes store owns the clock, and it is injected.** `NoteRepositoryImpl` takes
  `clock: Clock = Clock.systemDefaultZone()` and is the only thing in the app that stamps
  `createdAt` / `updatedAt` or decides what "today" is for the future-date rule. No screen stamps a
  note. Which *day* a note belongs to is the caller's choice and does travel down from the UI —
  `LocalDate.now()` appears in `HomeFragment`, `SettingFragment`, `NoteFragment` (as a fallback for
  a missing nav argument) and `NoteUiState`'s default. Keep any *new* time-dependent rule behind
  the injected `Clock`: the four UI reads are invisible to every test in this project.
- **Navigation arguments are hand-rolled — there is no Safe Args plugin.** Build the `Bundle` with
  a `companion object` factory on the destination Fragment (`NoteFragment.argumentsFor(...)`) so the
  key names exist in one place, and navigate with `safeNavigate(destination, bundle)`. Note the
  sharp edge: `safeNavigate` catches and logs, so a mistyped key or a missing required argument
  produces **no crash and no compiler error** — just a button that appears to do nothing. Give every
  `<argument>` a `defaultValue` and pick sentinels that cannot be real values (`NoteFragment` uses
  `-1L` for "no note yet" and `Long.MIN_VALUE` for "no day given"; `-1` as an epoch day is a real
  date, 1969-12-31, so it is *not* usable as the day sentinel).
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
- **Test surface — three source sets, two of which need no device:**
  - `app/src/test/` — plain JVM unit tests: the `ExampleUnitTest` stub,
    `ui/theme/DarkColorSchemeTest.kt`, `domain/model/NoteTest.kt`,
    `data/repository/NoteRepositoryImplTest.kt` (a `runTest` coroutine against a hand-written fake
    DAO) and, from iteration 5, `ui/fragment/note/NoteViewModelTest.kt`. **These can touch
    Compose's non-`@Composable` API** — `darkColorScheme()`, `Color` and Java reflection over
    `ColorScheme` all work with no Android context and no extra dependency. Worth knowing before
    assuming a Compose-related property needs an emulator. There is no Robolectric.
    **A ViewModel is testable here too**, which iteration 5 established: `Dispatchers.setMain(...)`
    from `kotlinx-coroutines-test` replaces the `Dispatchers.Main` that `viewModelScope` posts to
    and that does not exist off a device. With an `UnconfinedTestDispatcher` every `launch` runs to
    completion where it is started, so a test can call a ViewModel function and assert on
    `uiState.value` on the next line. Reach for this before declaring a screen's logic unprovable.
  - `app/src/screenshotTest/kotlin/…/screenshot/` — Compose Preview Screenshot Tests
    (`@PreviewTest @Preview`), rendered host-side by layoutlib. Imported in iteration 3.
    `ScreenshotScaffold.kt` is the wrapper every case goes through: it renders the subject inside
    `MyApplicationTheme` on `colorScheme.background`, because a bare `@Preview` renders against
    Studio's white with Material's baseline colours — which is how hardcoded colours survive review.
    References live in `app/src/screenshotTestDebug/reference/…` and are committed.
  - `app/src/androidTest/` — instrumented; needs a device or emulator.
- **What a screenshot test cannot do:** tell a hardcoded literal from a theme lookup. `Color.White`
  and `colorScheme.onBackground` render the same pixels. Do not treat a green
  `validateDebugScreenshotTest` as evidence that colour is coming from the theme — that needs a
  static check or a reader. See `knowledge/ISSUES.md`.
- **The HTTP client is Ktor, not Retrofit.** `injection/NetworkModule.kt` builds
  `io.ktor.client.HttpClient(OkHttp)` with `HttpTimeout` + `ContentNegotiation`/kotlinx-serialization;
  the catalogue declares `ktor-client-core`, `ktor-client-okhttp`, `ktor-serialization-kotlinx-json`
  and **no Retrofit artifact**. `*Api` classes take an `HttpClient` constructor parameter. Worth
  stating explicitly: Retrofit is the reflex guess for an Android repository of this shape, and a
  README written on that reflex had to be corrected in iteration 2.
- **`README.md` exists** at the repository root as of iteration 2, with the package tree
  `CLAUDE.md` requires. It carries a feature table marking each feature built vs. planned — keep
  that table honest in the same change that lands a feature.
- The dependency catalogue is `gradle/libs.versions.toml`; all dependencies are declared through it.
  As of iteration 3 it carries `kotlinx-coroutines-test` (for `runTest`), `room-testing` (in-memory
  database) and `screenshot-validation-api`. Robolectric is still **not** there.
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
whole time. `git branch --list 'loop/*'` costs nothing. The harness was imported in iteration 3 and
is now part of this branch; what remains useful on `loop/todo-calendar-screens` (tip `969f278`):

- **`CalendarScreenshotTest.kt` and `TodoScreenshotTest.kt`** — patterns to copy from when T-006 and
  T-007 write calendar cases. Read them; do not import them, as they reference composables that do
  not exist here.
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
