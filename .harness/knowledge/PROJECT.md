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
test that instead — `NoteRepositoryImpl`'s injected `java.time.Clock` is the worked example, and the
`AlarmScheduler` seam is the large one: `nextFireTimeMillis`, `scheduleDecision`, `requestCodeFor` and
`AlarmNotifier`'s five exposed constants are all ordinary Kotlin, reachable with no framework stub in the
way at all. They are not the *only* alarms code under test, though — `AlarmRepositoryImpl`,
`AlarmsViewModel` and `AlarmEditorViewModel` are tested too, over hand-written fakes standing in for the DAO
and the scheduler, the same pattern the rest of this codebase already uses. The seam is what makes the
*scheduling decision itself* (not just the repository and ViewModel plumbing around it) testable at all.

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

### C-14 — Deep-linking to a system settings screen needs the *specific* intent, or the user lands one screen short of the switch that was named.

Found in the Phase-5 Fresh-Context Review: a permission-notice banner told the user exactly which switch was
wrong, then its own fix button opened a screen that did not contain that switch. Nothing here is mechanically
checkable — the intent resolves, an activity opens, the app does not crash, and every build/test/lint command
stays green. A human looking at the wrong screen is the only thing that would ever catch it, and this app has
no human in the loop until the end of a run.

- **Notifications**: `openAppSettings(context)` (`ui/fragment/home/component/HomeRequestPermission.kt`) opens
  `ACTION_APPLICATION_DETAILS_SETTINGS` — the app's general "App info" page, one tap short of the
  notification toggle. Use `Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)`
  instead when the fix is specifically "turn notifications back on". `openAppSettings` is still the right
  call when the ask is genuinely "open this app's settings" (e.g. a permanently-denied runtime permission
  with no dedicated screen).
- **Exact alarms**: `Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)` with **no `data`** opens the
  system-wide list of every app that can be granted the permission, and the user has to find this app in
  that list after already being told which switch to flip. Add
  `.setData(Uri.parse("package:${context.packageName}"))` so the intent names this app the way the platform
  expects — the same `package:` convention `ACTION_APPLICATION_DETAILS_SETTINGS` already uses.
- Both fields (`ACTION_APP_NOTIFICATION_SETTINGS`, `Settings.EXTRA_APP_PACKAGE`) are `InlinedApi` on this
  app's `minSdk 24` (added in API 26) — a real, accepted lint warning here, not an error; wrap the call in
  `runCatching` and log the failure, the same fail-soft shape every other settings deep-link in this app
  already uses.

### C-15 — `HomeRequestPermission.kt`'s `requestExactAlarm` is not what it looks like from the outside.

`canScheduleExactAlarm(context)`, `isNotificationGranted(context)` and `openAppSettings(context)` in that
file are all top-level functions and safe to import. `requestExactAlarm()` looks like a fourth one and is
not: it is declared *inside* the `HomeRequestPermission` composable's body, a local function with no
visibility outside that composable. A task reusing "the exact-alarm request helper" from a different screen
cannot import it and has to write the few lines itself (guard on `Build.VERSION_CODES.S`, build the
`ACTION_REQUEST_SCHEDULE_EXACT_ALARM` intent per C-14, `runCatching` + log the `startActivity`) rather than
extracting it from a file it does not own. Two independent Workers hit this in Phase 5 and both caught it
before writing code around the wrong assumption — recorded here so a third does not have to rediscover it.

### C-16 — An Activity's launch `Intent` is replayed on every recreation, so intent-driven navigation needs `savedInstanceState == null`.

`getIntent()` keeps answering the **same** intent for the whole life of an activity instance. A rotation
recreates the activity and re-runs `onCreate` with it; so does this app's in-app language picker, through
`AppLocalesMetadataHolderService`. Handling the intent unguarded therefore re-navigates on every one of
those, with no new tap from anybody — a notification tapped once can throw the user onto Alarms over an
open editor days later, and the user's own half-written alarm is what gets covered.

Found in the Phase-4 Fresh-Context Review (iteration 6, `AMENDMENTS.md` A-12 #1). The fix in
`MainActivity` is three parts and all three matter: `if (savedInstanceState == null) openAlarmsIfRequested(...)`
in `onCreate`, the same call from `onNewIntent` (which is the path when the app is already on top —
`AlarmNotifier`'s `PendingIntent` carries `CLEAR_TOP or SINGLE_TOP`), and navigating through the
**`R.id.toAlarms` tab-swap action** (`popUpTo="@id/homeFragment"` + `launchSingleTop`) rather than the raw
`alarmsFragment` destination, so the tap collapses to Home→Alarms instead of stacking on whatever was open.

Nothing mechanical catches the unguarded version: there is no device and no instrumented test here, and it
builds, tests, lints and validates green. Any future intent this activity learns to handle — a widget, a
deep link, a second kind of notification — carries the same trap.

### C-17 — Revoking the exact-alarm permission cancels every alarm already pending; granting it back arms nothing.

From Android 12, when the user withdraws `SCHEDULE_EXACT_ALARM` the system drops the exact alarms this app
has pending. Grant it again and the rows are still in Room, every switch on the Alarms screen still reads
ON, the warning banner correctly disappears — and **not one alarm is armed.** It is the same silent state a
reboot leaves (which is what `BootReceiver` exists for), reached by the very fix path the banner asks the
user to walk. Clearing the warning is not the repair; re-arming is.

`AlarmsViewModel.setPermissionState(...)` detects the **false→true transition** of `exactAlarmGranted` and
calls `alarmScheduler.rearmAll(alarms = ...)`. The transition, not the value: the Fragment calls this on
every `RESUMED` transition, so re-arming whenever the flag reads `true` would re-arm the whole list on every
resume of a perfectly healthy screen. A new screen, or a refactor that moves where permission state is read,
must carry that re-arm with it.

Found in the Phase-5 Fresh-Context Review (iteration 7, `AMENDMENTS.md` A-14 #1), covered by three
`AlarmsViewModelTest` cases (re-arms on the grant transition; not when already granted; not on a
notifications-only change). No command in this repository can observe the real behaviour — it needs a phone
and a trip through system settings.

---

# REFERENCE

> Conventions, commands and layout. Violating one of these is untidy, not wrong. Filtered into a Worker
> Brief as the task needs.

## Toolchain (verified commands)

| Purpose | Command | Verified |
|---|---|---|
| Build (debug APK) | `./gradlew :app:assembleDebug` | **yes** — `BUILD SUCCESSFUL`; ~60s cold, ~10s warm |
| Compile only (faster) | `./gradlew :app:compileDebugKotlin` | **yes** — runs as part of the above |
| Unit tests (JVM) | `./gradlew :app:testDebugUnitTest` | **yes** — 288 tests across 18 classes, 0 failures (greeting/permission-cleanup run, this iteration) |
| Lint | `./gradlew :app:lintDebug` | **yes** — `0 errors, 75 warnings` (greeting/permission-cleanup run, this iteration) |
| Screenshot tests — validate | `./gradlew :app:validateDebugScreenshotTest` | **yes** — ~15s, **23 of 23** cases green (alarms run, iteration 8) |
| Screenshot tests — re-record | `./gradlew :app:updateDebugScreenshotTest` | **yes**, but **not granted to the engine** — see below |

Success is a literal `BUILD SUCCESSFUL` line. Read test counts from
`app/build/test-results/testDebugUnitTest/TEST-*.xml` (the console prints nothing when everything passes)
and the lint summary from
`app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.

All three figures come from **one** invocation of
`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:validateDebugScreenshotTest` whose own
`BUILD SUCCESSFUL` was observed (the greeting/permission-cleanup run, this iteration). The tree agrees:
288 `@Test`s across 18 classes in `app/src/test/`, 23 `@PreviewTest`s in `app/src/screenshotTest/`.
**The previous "251 tests across 15 classes" figure recorded here (alarms run, iteration 8) was already
stale before this run started** — the tree at the start of this run already held 272 tests across 16
classes, 21 more than the cached figure, with no record of when they were added. Read this table's count
as a floor to compare a *future* run's own fresh measurement against, not as a diff base to reason from —
this cache is corrected opportunistically when a run happens to measure it, not on every commit that adds
a test.

**The standing capability ledger is installed** at `.harness/knowledge/capabilities.json` (re-installed by
the human on approving D-001, migrated verbatim from the previous run's `knowledge/capabilities.json`,
which the runtime no longer reads). It grants `assembleDebug` / `compileDebugKotlin` / `testDebugUnitTest` /
`lintDebug`, `validateDebugScreenshotTest`, and the `MSYS_NO_PATHCONV=1 git show|ls-tree` workaround.
`updateDebugScreenshotTest` is **deliberately absent** from it — see below.

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
  one sitting there. Validation ignores strays and stays green, so nothing ever tells you. The eight
  orphans the previous run recorded are **gone**: `app/src/screenshotTestDebug/reference/` holds exactly
  23 `.png` files today and each one's filename prefix is a distinct live `@PreviewTest` function, so
  every file maps to exactly one of the 23 live cases. Keep it that way.
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
  `colorScheme.background` as the ground. **`floatingActionButton` now has exactly one user:**
  `AlarmsFragment`'s `AlarmsLayout` (a `primaryContainer` / `onPrimaryContainer` FAB — the contrast-verified
  pair, taken together). A screen that fills that slot must also keep its own list clear of it:
  `AlarmsFragment` reserves `56.dp + 16.dp + 16.dp` of bottom `contentPadding`, because the button floats
  *over* the list and otherwise eats the last row's taps. `CoreTopBar` handles status-bar padding itself;
  secondary screens use `CoreTopBar4`.
- **Bottom bar: four tabs.** `domain/enums/BottomBarDestination.kt` — `Home`, `Calendar`, `Setting`,
  `Alarms`; declaration order is layout order, and adding an entry is the whole job of putting a new
  top-level screen in the bar. `CoreBottomBar` splits the entries around the centre round add button with
  `leftCount = (entries.size + 1) / 2`, so today it is Home/Calendar | + | Setting/Alarms — the
  deliberately-empty right slot the three-tab era left is now filled. A destination opts out of the shared
  centre "+" with `hidesCreateButton = true` on its own enum entry (Alarms does, because it has its own
  FAB); the decision lives on the destination, never as an id check inside `CoreBottomBar`.
  `CoreBottomBar(onCreateNote: () -> Unit)` still has **no default, deliberately** — a new top-level screen
  must decide what the centre button means there. `ThemeScreenshotTest` renders the whole bar, and its two
  references were re-recorded for the fourth tab under the goal-scoped D-003 grant and are green today, so
  they now pin the **four-tab** layout: a fifth entry changes those pixels from a file no task opens.
- **Theme:** one fixed dark scheme in `ui/theme/Theme.kt`; `MyApplicationTheme(content)` takes no
  parameters and nothing reads the system light/dark setting. All 36 Material 3 roles are assigned
  explicitly, and `DarkColorSchemeTest` fails if any still matches `darkColorScheme()`'s default.
  `primary = ColorBlue2 = #35A0F5`, `background` is pure black. When adding or changing a token, **compute
  the contrast ratio against the black ground rather than judging the swatch** — this caught `outline` at
  2.19:1 in iteration 2. `primaryContainer` (#123A5F) exists specifically because `onPrimaryContainer` on
  the lighter blue reaches only 3.94:1 while on this fill it reaches 9.12:1 — it is the contrast-verified
  choice for a FAB.
- **Clean architecture layers:** `domain/model`, `domain/repository` (interfaces, Android-free),
  `domain/enums`, `domain/scheduler` (the `AlarmScheduler` seam + `NextFireTime.kt`, also Android-free),
  `data/database/local` (`AppDatabase`, `dao/`, `entity/`, `converter/`, `Migration.kt`),
  `data/repository/impl`, `data/mapper`, `data/remote`, `data/scheduler`, `data/receiver`,
  `data/notification`, `common/` (`Outcome<T>`, `Constant`), `core/extension/...`, `injection/`
  (Koin modules), `ui/`.
- **DI is Koin**, wired in `injection/AppModule.kt` from `databaseModule`, `datastoreModule`,
  `repositoryModule`, `viewModelModule`, `networkModule`, `localeModule`, `schedulerModule` — a *new*
  module must be added to that `includes` list. Repositories are bound by interface with named arguments;
  ViewModels use `viewModel { }`. `MainApplication` starts Koin with `modules(appModule)` only, and reads
  anything it needs at startup back out of `startKoin { ... }`'s return value rather than constructing a
  second, DI-invisible copy of it (that is how the notification channel is created — see below).
- **Room:** `AppDatabase` is at **`version = 4`** (1 = user actions, 2 = posts, 3 = `notes`, 4 = `alarms`),
  `exportSchema = false`, file `"app_database"`, `@TypeConverters(DateConverter::class)` (`Date` ↔ `Long`).
  Adding an entity means: entity + DAO + register in `@Database` + bump `version` + write the migration +
  register it in `addMigrations` + expose the DAO both in `AppDatabase` and in `databaseModule`. `NoteEntity`
  stores a calendar day as a raw epoch-day `Long` rather than through a converter, so `WHERE date = :epochDay`
  is an integer compare — and `AlarmEntity` follows it, storing a time of day as plain `hourOfDay` / `minute`
  `Int`s (`alarms` table: `id`, `message`, `hourOfDay`, `minute`, `enabled`, `createdAt`), with any
  conversion confined to the mapper.
- **DAO shape:** `observeAll(): Flow<List<XEntity>>` non-suspend, `suspend fun getById(id): XEntity?`,
  `@Insert(onConflict = REPLACE) suspend fun upsert(…): Long`, `@Delete suspend fun delete(…): Int`.
- **ViewModel state:** one `data class XxxUiState` with defaults for every field and derived values as
  computed `val`s inside it; updates are always `_uiState.value = _uiState.value.copy(...)`, never
  `.update { }`; `TAG` is an instance `private val` (the **opposite** of the repository convention, where
  `TAG` is a `companion object` const).
- **A store owns its clock, and it is injected.** `NoteRepositoryImpl` takes
  `clock: Clock = Clock.systemDefaultZone()` — one `Clock` rather than two lambdas, so the millisecond it
  stamps and the day it compares against cannot come from two different readings. The parameter exists so a
  test can pass a fixed clock; DI leaves it at its default. **Keep any new time-dependent rule behind it.**
  `AlarmRepositoryImpl` and `AlarmManagerAlarmScheduler` each take one too, and the two are **not** the same
  job: the repository's clock only stamps `createdAt`, while the instant an alarm actually fires is decided
  solely by the scheduler's, through `nextFireTimeMillis`. A test comment that confused the two had to be
  corrected in iteration 6.
- **Strings:** every user-visible string in `res/values/strings.xml`, appended at the end, named for the
  words themselves. Reusable keys already present: `notification`, `exact_alarm`, `cancel`, `delete`,
  `save`, `title`. The alarms feature added, in both locales: `alarms`, `alarm`, `no_alarms_yet`,
  `add_alarm`, `delete_alarm`, `delete_this_alarm`, `the_alarm_will_be_removed_permanently`,
  `alarm_deleted`, `the_alarm_could_not_be_opened` / `_deleted` / `_changed`, `alarms_may_not_reach_you`,
  `notifications_are_turned_off`, `exact_alarms_are_not_allowed`, `turn_on_notifications`,
  `allow_exact_alarms`. Look here before inventing a key — and see C-02 about who writes the two files.
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
  Turbine, WorkManager. An alarms feature needs **no new dependency**, and the shipped one added none —
  `AlarmManager` is framework and `NotificationCompat` already ships; `libs.versions.toml` still has no
  alarm-, notification- or WorkManager-related entry. A task proposing to edit `libs.versions.toml` or
  `app/build.gradle.kts` has silently become an architecture decision.
- **The HTTP client is Ktor, not Retrofit** — the reflex guess for an Android repo of this shape, and a
  README written on that reflex had to be corrected in iteration 2.
- **Notification and alarm infrastructure now exists — it is the alarms feature, shipped over this run.**
  Where each piece lives, because the next task will need to find it:
  - **Channel:** `data/notification/AlarmNotifier.kt`. One channel, id `"alarms"`, `IMPORTANCE_HIGH`, name
    read from the existing `exact_alarm` string, sound + `VIBRATION_PATTERN`, `PRIORITY_HIGH` and
    `CATEGORY_ALARM` on the builder (both halves of C-10). Created in `MainApplication.onCreate` through
    **Koin's own** `AlarmNotifier` instance, and again before each post (`createNotificationChannel` is
    idempotent). Notification id is the alarm's row id, so an alarm replaces its own banner and never
    another alarm's. Small icon `res/drawable/ic_notification_alarm.xml`, solid white — never the launcher
    mipmap. Tapping it opens `MainActivity` with the action `AlarmNotifier.ACTION_OPEN_ALARMS`.
  - **Receivers, both in `data/receiver/`:** `AlarmReceiver` (**not** exported; hears the app's own
    `ACTION_ALARM_FIRED`, shows the notification and then arms tomorrow's — `setExactAndAllowWhileIdle`
    fires once, so that second step *is* the daily repeat) and `BootReceiver` (**exported**, with a
    `BOOT_COMPLETED` intent-filter and the `RECEIVE_BOOT_COMPLETED` permission, because the boot broadcast
    comes from outside the app; `goAsync()` + `withTimeoutOrNull(8_000L)` around a `Dispatchers.IO`
    coroutine that reads `alarmsFlow.first()` and calls `rearmAll`, with `pendingResult.finish()` in a
    `finally`). Neither has a constructor to inject into — the system builds receivers itself — so both
    reach Koin through `KoinComponent` + `by inject()`.
  - **The scheduler seam:** interface `domain/scheduler/AlarmScheduler.kt` (`schedule`, `cancel`,
    `rearmAll`) with `data/scheduler/AlarmManagerAlarmScheduler.kt` behind it, bound in
    `injection/SchedulerModule.kt`. **It is a seam because of C-06**, not for style: `AlarmManager`,
    `PendingIntent` and `NotificationManager` are silent stubs under this toolchain, so every decision is
    pulled to the plain-Kotlin side of the line and unit-tested there — `nextFireTimeMillis`
    (`domain/scheduler/NextFireTime.kt`, behind an injected `Clock`), and `scheduleDecision` /
    `requestCodeFor` (`internal`, in the scheduler file; the request code is the alarm's row id).
    `rearmAll`'s loop lives in the **interface default** and nothing overrides it, deliberately: an
    override would mean the fakes in `RearmAllTest` exercise a path production never runs. `AlarmRepositoryImpl`
    mirrors every mutation (save / edit / delete / enable / disable) through the interface, so no caller can
    arm or cancel behind the store's back.
  - **Permission notice:** `ui/fragment/alarms/component/AlarmsPermissionNotice.kt` — a banner above the
    list, drawing nothing at all when both permissions are in order, one sentence and one fix button per
    problem (the two fixes are two different system screens — C-14). `AlarmsFragment` reads both permissions
    on **every** `RESUMED` transition and hands them to `AlarmsViewModel.setPermissionState(...)` — see
    C-17 for what has to happen on the false→true transition.
  - **Manifest:** `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED`, plus
    both `<receiver>` elements. `MainActivity` still has **no `launchMode`** — the notification's
    `PendingIntent` carries `CLEAR_TOP or SINGLE_TOP` instead, and the activity handles the intent in both
    `onCreate` and `onNewIntent` (C-16).
  - Still **absent**, and still true: no `WorkManager`, no `setFullScreenIntent`, no do-not-disturb
    override, no `USE_EXACT_ALARM`. And `ui/fragment/home/component/HomeRequestPermission.kt` remains a
    *Home-screen* Accompanist flow wired into `HomeFragment` with all three callbacks empty — read C-15
    before reusing anything out of it.
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
- The alarms run's own record: `.harness/run/TASKS/A-001.md` … `A-007.md` (§ Evidence),
  `.harness/run/AMENDMENTS.md` (A-1 … A-14), `.harness/run/STATE.md`
- For the numbers and the current shape, the tree itself: `app/src/test/`, `app/src/screenshotTest/`,
  `app/src/screenshotTestDebug/reference/`, `AndroidManifest.xml`, `.harness/knowledge/capabilities.json`
