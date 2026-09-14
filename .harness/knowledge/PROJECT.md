# PROJECT KNOWLEDGE

> Engine-maintained cache of **verified** operational truth about this repository. Survives every feature run.
> Human-editable without approval. It is a cache, never the source of truth: on conflict, the codebase wins
> and the engine corrects this file.
>
> **Provenance.** Carried forward from `knowledge/PROJECT.md` at the repository root, which a previous
> Foreman run (harness v2, `.ai/` + `knowledge/` layout) built over eleven iterations. The harness has since
> moved to `.harness/`, and the runtime now reads `.harness/knowledge/`. The old file is left in place
> untouched — the engine cannot delete files here — but **this** file is the live one. Where they differ,
> this one is current.

---

# CONSTRAINTS

> Traps. If a Worker ignored one of these, the result would be **wrong**, not merely untidy.
> Carried **verbatim into every Worker Brief**, never filtered. The Fresh-Context Review checks the diff
> against every entry, and a violation is a **blocking** finding rather than an opinion.
> A Constraint outranks a literal reading of an acceptance criterion: where they conflict, follow the
> Constraint and say so.

### C-01 — Colour comes from the theme. Never a colour literal.

Read every colour from `MaterialTheme.colorScheme.*`. Never write `Color(0xFF…)`, `Color.White` or
`Color.Black` in new code. `Color.Transparent` and `Color.Unspecified` are not colour choices and are fine.

**Nothing mechanical catches this here.** Verified in iteration 11: the lint report contains *no*
`HardcodedText` and no colour finding at all, because AGP reads `android:text` in layout XML and this app
has no layout XML for its screens. And a screenshot test cannot tell a literal from a theme lookup —
`Color.White` and `colorScheme.onBackground` render identical pixels. 43 such literals survive in the tree
today (`.harness/ISSUES.md`). The check that works is a grep.

Evidence: `ui/theme/Color.kt`, `ui/theme/Theme.kt` (all 36 Material roles assigned explicitly).
Note `.claude/figma-design-system.md` § 4(b) says the opposite; it is overridden here and is itself filed
as a rule-file defect.

### C-02 — Adding a user-visible string is a two-file operation, and a Worker does neither.

`res/values-de/` is a shipped locale and AGP lint rates `MissingTranslation` an **error**, so `lintDebug`
goes red the moment a key lands in `res/values/strings.xml` without a German counterpart. The failure
message names the English file you *did* edit and never mentions the German one you didn't, so it reads
like the wrong problem.

`res/values/strings.xml` and `res/values-de/strings.xml` are **Iteration-owned shared files**. A Worker
must **not** edit either. Reference the key you need (`R.string.xxx`) and **list every new key, its English
text and its German text in your report** — the Iteration writes both files.

Name a string for its own words, not its feature: `<string name="no_alarms_yet">`, never
`<string name="alarms_empty_state">`. Evidence: `CLAUDE.md` § String content.

### C-03 — A Room migration's SQL is copied, never composed.

After a build, Room's generated implementation sits at
`app/build/generated/ksp/debug/kotlin/com/example/skeleton/data/database/local/AppDatabase_Impl.kt`. Its
`createAllTables` holds the exact `CREATE TABLE` a fresh install uses. **Copy that statement into the
migration verbatim, backticks and all**, then read the `_columnsXxx` map below it to confirm types,
`NOT NULL` and primary key.

Nothing in this repository can execute SQLite — no Robolectric, and `MigrationTestHelper` needs a device —
so a migration is either right by construction or unverified. Iteration 4 composed one by eye and wrote a
`DEFAULT ''` the entity does not declare.

A migration that *runs* but builds the wrong schema still fails loudly: Room's `validateMigration` compares
the result against the entity and throws. That is the case this Constraint protects. The **missing**-path
case is the opposite and much quieter — see **C-13**, which corrects what this entry used to claim about
`fallbackToDestructiveMigration(false)`.

### C-04 — Any new screen with a text field needs `Modifier.imePadding()` before `verticalScroll`.

`MainActivity` calls `enableEdgeToEdge()`, `SystemBarUtil` calls `setDecorFitsSystemWindows(window, false)`,
and `CoreLayout` passes `contentWindowInsets = WindowInsets(0,0,0,0)` — it deliberately consumes nothing.
**The window is never resized when the keyboard opens.** The scroll range is `content − viewport`, so if the
viewport is never shortened no amount of scrolling lifts the covered strip into view.

`imePadding()` must come **before** `verticalScroll` in the modifier chain — it has to shrink the viewport,
not the content inside it. Found in iteration 5: the Note editor's first version scrolled correctly with the
keyboard closed and was unusable with it open.

### C-05 — A `@Preview` too small records its content as *missing*, not clipped.

Compose lays a preview out inside `widthDp` × `heightDp` exactly. Children that do not fit are measured to
**nothing** — the image records as if they were never written, with no crop mark and no warning, and then
passes validation forever while defending nothing.

**Size a preview as the component's real size plus `ScreenshotScaffold`'s 24dp in each direction**, and add
up the whole component's height, not the height of the part you care about. When the number is uncertain,
record generously: too tall costs black pixels, too short costs the evidence. This happened twice
(iterations 8 and 9), and both times the thing that vanished was the part the author was not thinking about.

### C-06 — Unit tests run against a stub `android.jar` that returns defaults silently.

`app/build.gradle.kts` carries `testOptions { unitTests { isReturnDefaultValues = true } }`. Without it a
single `Log.w` on an error path crashes the test instead of exercising the behaviour. With it, **every
unmocked framework call returns `0` / `false` / `null` with no signal.**

So `Bundle`, `Intent`, `Uri`, `PendingIntent`, `AlarmManager`, `NotificationManager` and
`NotificationChannel` are all untestable here — assertions against them pass vacuously. There is no
Robolectric, no MockK, no Mockito. **Do not write a test whose subject is one of those types.** Pull the
decision that carries an acceptance criterion out into a plain Kotlin function behind an injected seam and
test that instead — `NoteRepositoryImpl`'s injected `java.time.Clock` is the worked example.

### C-07 — Do not debounce a control with `NavigationUtil.canNavigate()`.

It is one process-wide `lastNavTime` guarding an 800 ms window, and `CoreBottomBar.BottomBarElement` arms it
*before* checking whether the tab tap navigates anywhere. A tap on the tab you are already on consumes the
window and every other navigating control in the app is dead for 800 ms, showing its ripple and doing
nothing. Found in the T-004 review on a Home note row.

For "this control must not navigate twice", ask the graph where you are:
`findNavController().currentDestination?.id == R.id.<thisScreen>`. `navigate` moves `currentDestination`
synchronously, so the second tap of a double tap is already standing on the destination and is refused.

### C-08 — A navigation-argument sentinel must be a value that cannot legally occur.

There is no Safe Args plugin; arguments are hand-rolled through a `companion object` factory on the
destination Fragment (`NoteFragment.argumentsFor(...)`) so the key strings exist in one place. Give every
`<argument>` in the graph a `defaultValue`, because `safeNavigate` catches and logs — a mistyped key or a
missing required argument produces **no crash and no compiler error**, just a control that appears to do
nothing.

`NoteFragment` uses `-1L` for "no note yet" (`0` is Room's autogenerate marker) and `Long.MIN_VALUE` for
"no day given" (`-1` as an epoch day is a real date, 1969-12-31, so it is not usable as a sentinel).

### C-09 — A repository never throws across its boundary, and a write that matched nothing is an error.

Return `null` / `emptyList()` / `common.Outcome<T>`; always re-throw `CancellationException` inside a catch.
A read with three answers returns `Outcome<T?>` — `Success(value)` / `Success(null)` / `Error` — not a bare
`T?`, which collapses "it is not there" into "I could not look" and made a failed read open a blank editor
whose save wrote a second note (fixed iteration 7). Tag a refusal by **exception type** inside
`Outcome.Error.throwable`, never by message text; `FutureDateRefusedException` is the precedent.

Room's `@Delete` is perfectly content to match no row and report nothing wrong, so `NoteDao.delete` returns
`Int` and the impl turns `0` into an `Outcome.Error`. Without the count the screen announces a deletion that
never happened. The same applies to any `@Update` or `@Query("DELETE …")`.

### C-10 — A notification channel's importance is immutable once created (API 26+).

Creating a channel at `IMPORTANCE_DEFAULT` and later "raising" it does nothing on any device that already
ran the app — re-creating a channel with the same id is a silent no-op. Get `IMPORTANCE_HIGH` right the
first time or ship a new channel id. **A heads-up notification is decided by the channel, not the builder.**

And `minSdk = 24`: on API 24–25 there are no channels at all, and a heads-up requires
`NotificationCompat.PRIORITY_HIGH` **plus** a sound or vibration. Both paths are live in this app; setting
only one leaves the requirement unmet on two API levels with a fully green build.

### C-11 — Use `customizedTextStyle(...)` for every `Text`, and the app's own locale for formatting.

`ui/theme/Type.kt` exposes
`customizedTextStyle(fontSize, fontWeight, lineHeight, color, textDecoration, fontFamily)` backed by
`InterFontFamily`. Never `MaterialTheme.typography`. Default text colour is `Color.White`, which is why the
scheme's `onBackground`/`onSurface` are deliberately pure white — two nearly identical whites in one app is
a defect nobody spots until they land side by side.

Format dates and times against `LocalConfiguration.current.locales[0]`, **never `Locale.getDefault()`**:
the app has its own language picker. Wrap formatters in `remember(locale) { … }`.

### C-12 — A Worker holds no git, no build and no test capability, and cannot delete files.

Report what you wrote; the Iteration reads the diff from git. `rm` is not in the granted capability set, so
a stray file you create cannot be removed by the engine — **choose a file's name and a preview's
`name`/`widthDp`/`heightDp` before writing it, not after.**

### C-13 — `fallbackToDestructiveMigration` opts into destructive migration; calling it at all is the trap.

**Resolved (D-005, iteration 5): the call is removed.** `injection/DatabaseModule.kt` no longer calls
`fallbackToDestructiveMigration` at all, which is Room's default — an upgrade with no matching migration now
throws `IllegalStateException` at launch instead of silently dropping and recreating tables. The four KDoc
blocks that used to claim this behaviour while the call still enabled the opposite
(`DatabaseModule.kt`, `Migration.kt` ×2, `AlarmEntity.kt`, `Alarm.kt`) were corrected in the same checkpoint.

The trap for future work: Room `2.7.2` deprecated the no-arg `fallbackToDestructiveMigration()` in favour of
`fallbackToDestructiveMigration(dropAllTables: Boolean)`. The boolean is **not** an on/off switch — *calling
the method at all*, with either `true` or `false`, opts into destructive migration; the parameter only
chooses whether every table is dropped (`true`) or only the ones Room owns (`false`). If a future task adds
this call back — for a debug build type, for example — it must not read the boolean as "false = safe": the
only way to keep Room's throw-on-missing-migration behaviour is to not call the method at all.

A task that bumps `AppDatabase.version` and forgets to write or register the migration now crashes at launch
instead of silently wiping `alarms`, `notes`, `posts` and `user_actions` — the loud failure D-005 chose over
the quiet one, since nothing in a build, test, lint or fresh-install QA pass could ever have seen the quiet
version.

---

# REFERENCE

> Conventions, commands and layout. Violating one of these is untidy, not wrong. Filtered into a Worker
> Brief as the task needs.

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build (debug APK) | `./gradlew :app:assembleDebug` | **yes** — `BUILD SUCCESSFUL`; ~60s cold, ~10s warm |
| Compile only (faster) | `./gradlew :app:compileDebugKotlin` | **yes** — runs as part of the above |
| Unit tests (JVM) | `./gradlew :app:testDebugUnitTest` | **yes** — 138 tests across 8 classes as of the previous run's iteration 11 |
| Lint | `./gradlew :app:lintDebug` | **yes** — `0 errors, 64 warnings` as of iteration 11 |
| Screenshot tests — validate | `./gradlew :app:validateDebugScreenshotTest` | **yes** — ~15s, 20 cases as of iteration 11 |
| Screenshot tests — re-record | `./gradlew :app:updateDebugScreenshotTest` | **yes**, but **not granted to the engine** — see below |

Success is a literal `BUILD SUCCESSFUL` line. Read test counts from
`app/build/test-results/testDebugUnitTest/TEST-*.xml` (the console prints nothing when everything passes)
and the lint summary from
`app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.

**These commands require a standing capability grant that is not currently installed at
`.harness/knowledge/capabilities.json`.** The previous run's ledger sits at `knowledge/capabilities.json`,
which the runtime no longer reads. Until it is moved, the engine cannot build, test or lint anything.

### `updateDebugScreenshotTest` is withheld on purpose

It overwrites the committed reference images, which gives an engine facing a red screenshot test a
one-command route to re-recording wrong output as correct — the same class of act as editing an approved
DoD. It is never the fix for a failing `validateDebugScreenshotTest`. When a baseline genuinely must move
(an intentional UI change, or a new state with no reference yet), the engine raises an Escalation Request
and the human adds a **goal-scoped** entry to `.harness/run/capabilities.json`, which expires with the run.

Three further traps around it:

- **It orphans references rather than replacing them.** A reference filename ends in a hash of the
  preview's *parameters*. Change the `@Preview` body only and the file is overwritten in place; change
  `name`, `widthDp` or `heightDp` and the plugin writes a **new** file under a new hash and leaves the old
  one sitting there. Validation ignores strays and stays green, so nothing ever tells you. Eight orphans
  are in the tree today.
- **Telling a stray from a live reference takes one command.** After a green validation run,
  `grep -o '[a-f0-9]\{8\}_0\.png' app/build/reports/screenshotTest/preview/debug/com.example.skeleton.screenshot.<Class>Kt.html`
  lists exactly the live hashes for that class. Anything on disk and not in that list is dead. Iteration 10
  reasoned from the tracked-vs-untracked split instead and concluded a *live* image was a stray.
- **References are exact-pixel and host-locked.** Anything rendering a localised date or time follows the
  *host's* locale and cannot be pinned — pass a pre-formatted `String` in as a parameter if it is to be
  photographed. This is why the Note editor has no reference image.

### `assembleDebug` + `testDebugUnitTest` green does **not** mean the tree compiles

`app/src/screenshotTest/` is a fourth source set neither task touches. Change the signature of a composable
it renders and both report `BUILD SUCCESSFUL` while `compileDebugScreenshotTestKotlin` is broken — found in
iteration 5. **Run `:app:validateDebugScreenshotTest` in the same command as the build whenever a
composable's signature changes.** The same applies to `app/src/androidTest/`, which nothing here can compile.

### One failing Gradle task aborts the others, and the leftover XML reads exactly like a pass

Asking for four tasks in one command does not mean all four run. In iteration 7 a failure aborted
`testDebugUnitTest`, but its result XML was still sitting there from the previous invocation with old counts
and `failures="0"` — producing a confident, wrong "the new tests pass". **Read counts only from a run whose
own `BUILD SUCCESSFUL` you saw**, and treat a test count that did not move after adding tests as proof the
task did not run.

### A piped Gradle command reports the *pipe's* exit code, not Gradle's

`./gradlew … 2>&1 | tail -20` exits 0 even when the build failed, because the status belongs to `tail`.
Always read the `BUILD SUCCESSFUL` / `BUILD FAILED` line. (`PIPESTATUS` and `set -o pipefail` are both
rejected by the permission layer here.) And do not run Gradle in the background while editing sources —
Gradle reads the working tree when it gets there, not when it was launched.

### `git reset` and `git restore` are denied — `git rm --cached` is how you unstage

`Bash(git rm --cached*)` is in the baseline ledger and drops a path from the index while leaving the file on
disk. **Name individual files, never a folder**: `-r` on a reference directory would also untrack the
committed images living beside the strays, and the next commit would delete them from the branch.

### Reading files out of other commits — Git Bash mangles the path

For `.harness/**` and `knowledge/**` paths, MSYS rewrites `ref:path` into `ref;path` and `git show` fails
with *"unknown revision or path not in the working tree"*, which reads exactly like the file is missing.
Prefix with `MSYS_NO_PATHCONV=1` (a granted capability in the previous run's ledger). Distinguish the two
failures: *"path 'x' exists on disk, but not in '<ref>'"* is Git telling you the truth.

## Architecture Conventions

Distilled from `CLAUDE.md` + `.claude/*.md` (human-owned rule files, never edited by the engine) and
confirmed against the code that exists.

- **Single module** `:app`, package `com.example.skeleton`, `applicationId com.example.myapplication`.
- **Single Activity + Fragments + Compose.** `MainActivity` hosts a `NavHostFragment`
  (`res/layout/activity_main.xml`) driving `res/navigation/navigation_graph.xml`. Each screen is a
  `Fragment` whose UI is Compose, hosted by `CoreFragment`'s `ComposeView()` override.
- **Screen spine:** `XxxFragment : CoreFragment()` (owns ViewModel, navigation, side effects) +
  `private fun XxxLayout(uiState, on…: () -> Unit = {})` (pure UI, previewable, navigates nowhere).
  Files live in `ui/fragment/<screen>/` with `XxxFragment.kt`, `XxxUiState.kt`, `XxxViewModel.kt`, and
  screen-local composables under `component/`. `@Preview`s at the file end, each wrapped in
  `MyApplicationTheme { }`.
- **Scaffolding:** use `core/CoreLayout.kt`, not a raw `Scaffold`. It takes `topBar` / `bottomBar` /
  `floatingActionButton` / `snackbarHost` / `showLoading` / `content` and paints
  `colorScheme.background` as the ground. **`floatingActionButton` already exists and nothing in the app
  uses it yet.** `CoreTopBar` handles status-bar padding itself; secondary screens use `CoreTopBar4`.
- **Bottom bar:** `domain/enums/BottomBarDestination.kt` — declaration order is layout order, and adding an
  entry is the whole job of putting a new top-level screen in the bar. `CoreBottomBar` splits the entries
  down the middle around a centre round add button; it is written for an even split and its KDoc states a
  **fourth** screen fills the deliberately-empty right slot and needs no change to that file. But
  `CoreBottomBar(onCreateNote: () -> Unit)` has **no default, deliberately** — a new top-level screen must
  decide what the centre button means there. Note that `ThemeScreenshotTest` renders the whole bar and its
  two references pin a **three-tab** layout: a fourth entry changes those pixels from a file no task opens.
- **Theme:** one fixed dark scheme in `ui/theme/Theme.kt`; `MyApplicationTheme(content)` takes no
  parameters and nothing reads the system light/dark setting. All 36 Material 3 roles are assigned
  explicitly, and `DarkColorSchemeTest` fails if any still matches `darkColorScheme()`'s default.
  `primary = ColorBlue2 = #35A0F5`, `background` is pure black. When adding or changing a token, **compute
  the contrast ratio against the black ground rather than judging the swatch** — this caught `outline` at
  2.19:1 in iteration 2. `primaryContainer` (#123A5F) exists specifically because `onPrimaryContainer` on
  the lighter blue reaches only 3.94:1 while on this fill it reaches 9.12:1 — it is the contrast-verified
  choice for a FAB.
- **Clean architecture layers:** `domain/model`, `domain/repository` (interfaces, Android-free),
  `domain/enums`, `data/database/local` (`AppDatabase`, `dao/`, `entity/`, `converter/`, `Migration.kt`),
  `data/repository/impl`, `data/mapper`, `data/remote`, `common/` (`Outcome<T>`, `Constant`),
  `core/extension/...`, `injection/` (Koin modules), `ui/`.
- **DI is Koin**, wired in `injection/AppModule.kt` from `databaseModule`, `datastoreModule`,
  `repositoryModule`, `viewModelModule`, `networkModule`, `localeModule` — a *new* module must be added to
  that `includes` list. Repositories are bound by interface with named arguments; ViewModels use
  `viewModel { }`. `MainApplication` starts Koin with `modules(appModule)` only.
- **Room:** `AppDatabase` is at **`version = 4`** (1 = user actions, 2 = posts, 3 = `notes`, 4 = `alarms`),
  `exportSchema = false`, file `"app_database"`, `@TypeConverters(DateConverter::class)` (`Date` ↔ `Long`).
  Adding an entity means: entity + DAO + register in `@Database` + bump `version` + write the migration +
  register it in `addMigrations` + expose the DAO both in `AppDatabase` and in `databaseModule`. `NoteEntity`
  stores a calendar day as a raw epoch-day `Long` rather than through a converter, so `WHERE date = :epochDay`
  is an integer compare — the same reasoning points a time-of-day at minute-of-day or hour+minute `Int`s,
  with the conversion confined to the mapper.
- **DAO shape:** `observeAll(): Flow<List<XEntity>>` non-suspend, `suspend fun getById(id): XEntity?`,
  `@Insert(onConflict = REPLACE) suspend fun upsert(…): Long`, `@Delete suspend fun delete(…): Int`.
- **ViewModel state:** one `data class XxxUiState` with defaults for every field and derived values as
  computed `val`s inside it; updates are always `_uiState.value = _uiState.value.copy(...)`, never
  `.update { }`; `TAG` is an instance `private val` (the **opposite** of the repository convention, where
  `TAG` is a `companion object` const).
- **The notes store owns the clock, and it is injected.** `NoteRepositoryImpl` takes
  `clock: Clock = Clock.systemDefaultZone()` — one `Clock` rather than two lambdas, so the millisecond it
  stamps and the day it compares against cannot come from two different readings. The parameter exists so a
  test can pass a fixed clock; DI leaves it at its default. **Keep any new time-dependent rule behind it.**
- **Strings:** every user-visible string in `res/values/strings.xml`, appended at the end, named for the
  words themselves. Reusable keys already present: `notification`, `exact_alarm`, `cancel`, `delete`,
  `save`, `title`.
- **List row recipe:** `ui/component/NoteSummaryRow.kt` — `Column` → `fillMaxWidth` →
  `clip(RoundedCornerShape(16.dp))` → `border(1.dp, colorScheme.outlineVariant)` →
  `background(colorScheme.surfaceContainer)` → `clickable(onClickLabel, interactionSource, ripple)` →
  `padding(16.dp)`, `verticalArrangement = spacedBy(8.dp)`.
- **`java.time` is safe on this minSdk.** `minSdk = 24` but `isCoreLibraryDesugaringEnabled = true`, so
  `LocalDate` / `LocalTime` / `LocalDateTime` are usable. The `@RequiresApi(O)` annotations on the existing
  `core/extension/date_and_time/` helpers are a leftover, not a constraint.

## Environmental Facts

- **Platform:** Windows 11, PowerShell primary shell, Gradle wrapper, Gradle 9.1.0.
- **`gradle.properties` no longer pins `org.gradle.java.home`, and the build works anyway.** The pin to a
  personal Android Studio JBR path was added by a human between iterations 2 and 3, escaped by iteration 3
  to satisfy `lintDebug`'s `PropertyEscape` check, and **removed by a human in commit `ee7b5c9`** ("drop
  machine-specific org.gradle.java.home pin"). Iteration 4 re-verified from scratch: all four commands run
  to `BUILD SUCCESSFUL` with no pin, so Gradle is finding a JDK from the environment (`JAVA_HOME` / PATH)
  without help. The earlier "no JDK reachable at all" of iteration 2 was an environment problem that has
  since been fixed outside the repository, **not** something the pin was load-bearing for. This is now
  closed rather than flagged — nothing is left for a human to relocate.
- **Android SDK** path comes from git-ignored `local.properties`; a fresh clone will not build without it.
- `compileSdk 36`, `targetSdk 36`, `minSdk 24`, `jvmTarget 11`.
- **No CI configuration exists.** No lint baseline, no ktlint, no detekt — "lint" means AGP lint.
- **No emulator, no device, no `adb`, no Robolectric.** Anything requiring the app to actually run —
  a notification appearing, an alarm firing, a reboot surviving, a screen rendering on a phone — is a
  `human` verification item, not a machine one. There is no command here that can prove it.
- **Test surface — three source sets, two of which need no device:**
  - `app/src/test/` — plain JVM unit tests. These **can** touch Compose's non-`@Composable` API
    (`darkColorScheme()`, `Color`, reflection over `ColorScheme`) with no Android context. **A ViewModel is
    testable here**: `Dispatchers.setMain(UnconfinedTestDispatcher())` from `kotlinx-coroutines-test`
    replaces the `Dispatchers.Main` that `viewModelScope` posts to, and every `launch` then runs to
    completion where it is started, so a test can call a function and assert on `uiState.value` on the next
    line. Fakes are hand-written classes in the same file — there is no mocking framework. The established
    pattern includes a *second*, deliberately-failing fake (`BrokenNoteDao`) as the only way to reach a
    repository's catch blocks, and a fake that returns a **jumbled** list so the repository's own sort is
    what is under test rather than SQL.
  - `app/src/screenshotTest/kotlin/…/screenshot/` — Compose Preview Screenshot Tests (`@PreviewTest`
    `@Preview`), rendered host-side by layoutlib. Every case goes through `ScreenshotScaffold`, which
    applies `MyApplicationTheme` **itself** and resolves the ground colour inside the theme — which is why
    its parameter is a `ScreenshotGround` enum and not a `Color`. A caller's `MaterialTheme.colorScheme.…`
    would be evaluated *outside* the theme against Material's baseline **light** scheme. Match the ground to
    what is actually behind the component. References live in `app/src/screenshotTestDebug/reference/…` and
    are committed.
  - `app/src/androidTest/` — instrumented; needs a device. Nothing here can compile it.
- **Dependencies** are declared through `gradle/libs.versions.toml`. Present: `junit 4.13.2`,
  `kotlinx-coroutines-test`, `room-testing` (unused — needs a device), `screenshot-validation-api`,
  `androidx-core-ktx` (which is where `NotificationCompat` lives). **Absent:** Robolectric, MockK, Mockito,
  Turbine, WorkManager. An alarms feature needs **no new dependency**: `AlarmManager` is framework and
  `NotificationCompat` already ships. A task proposing to edit `libs.versions.toml` or `app/build.gradle.kts`
  has silently become an architecture decision.
- **The HTTP client is Ktor, not Retrofit** — the reflex guess for an Android repo of this shape, and a
  README written on that reflex had to be corrected in iteration 2.
- **Notification and alarm infrastructure is 100% absent, not 90%.** `POST_NOTIFICATIONS` and
  `SCHEDULE_EXACT_ALARM` are declared in the manifest, and `ui/fragment/home/component/HomeRequestPermission.kt`
  is a complete Accompanist permission flow with `canScheduleExactAlarm(context)`,
  `isNotificationGranted(context)`, `requestExactAlarm()` and `openAppSettings(context)` — wired into
  `HomeFragment` with **all three callbacks empty**. That makes it *look* like the groundwork is done. It is
  not: grep returns **zero** hits for `NotificationChannel`, `NotificationCompat`, `BroadcastReceiver` and
  `WorkManager` across `app/src/main/java`. There is no channel, no notification icon drawable, no
  `<receiver>` of any kind, no `RECEIVE_BOOT_COMPLETED`, no `PendingIntent`, and `MainActivity` (27 lines)
  has no intent handling or `launchMode`.
- **`README.md`** exists at the repository root with the package tree `CLAUDE.md` requires and a feature
  table marking each feature built vs. planned — keep that table honest in the same change that lands a
  feature.
- **`.harness/`, `.claude/skills/`, `.agents/skills/` and the root `knowledge/` directory** are
  human-installed tooling and prior-harness residue. They show up in `git status` and are not the debris of
  a crashed run.
- **A prior Foreman run's work lives on `loop/todo-calendar-screens`** (tip `969f278`). Check sibling
  `loop/*` branches before concluding this repository cannot do something — the previous run declared its
  perceptual criteria unprovable while a verified host-side screenshot harness sat on that branch, reachable
  the whole time. `git branch --list 'loop/*'` costs nothing.

## Sources Consulted

- `PRD.md` (including the Alarms addendum), `CLAUDE.md` and the rule files it imports
- `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`, `res/navigation/navigation_graph.xml`, `res/values*/strings.xml`
- Existing sources under `core/`, `injection/`, `data/`, `domain/`, `ui/`, and all three test source sets
- `knowledge/PROJECT.md` and `knowledge/ISSUES.md` from the previous run
