# OPEN ISSUES

> Engine-maintained list of **known-wrong things that are still wrong**. Survives every feature run
> because it lives in `knowledge/`, alongside `PROJECT.md`, and is read at Orient every iteration.
>
> **Its semantics are the opposite of `PROJECT.md`'s.** `PROJECT.md` records how this repository
> *is* — conventions to conform to. This file records what is *wrong* with it — things to **not**
> copy.
>
> Human-editable without approval, like `PROJECT.md`.
>
> Entries are self-contained and cite the **commit SHA** holding the full record, never a path into
> `.ai/` alone (the Cleanup Commit removes `.ai/`). Read a record back with `git show <sha>:<path>`.

## Entries

<!-- Newest first. Delete resolved entries outright rather than marking them done. -->

### A note with no title prints its first line twice in every list row

- **What is wrong:** `Note.displayTitle` falls back to the first non-blank line of the body when
  the title is blank, and the row then *also* draws the body underneath. For an untitled note
  short enough to fit in one line — which is most quick notes — the user reads the same sentence
  twice, once bold and once grey.
- **Where:** `ui/component/NoteSummaryRow.kt` (the heading at the top, the body at the bottom),
  reading `domain/model/Note.kt`'s `displayTitle`. Affects **both** lists: Home and the Calendar
  screen's day section.
- **Why it matters:** it is small, and it is on the app's two busiest surfaces. It also reads as a
  rendering fault rather than as a choice — a duplicated line looks like a component drawing the
  same field twice by mistake, which is very nearly what it is.
- **Visible in a committed reference image.** `CalendarScreenshotTest.DayNotesWithNotes`'s second
  card is exactly this case. **A passing validation of that image is the wart holding still, not
  the wart being fixed** — and the image is worth keeping either way, because the fallback heading
  itself is right and needs defending.
- **Why it is still open:** the obvious fix is wrong. Suppressing the body whenever the title is
  blank also hides lines 2–3 of a *long* untitled note, which is where the preview earns its keep;
  dropping just the first line of the body makes the row's text start mid-thought. Which of those
  a reader prefers is a design call, and no DoD criterion asks for any of them — criterion 4 asks
  only that the row show "the title, or — when the title is blank — the first line of its body",
  which it does.
- **What would resolve it:** a decision. Either (a) when the heading came from the body, draw the
  body starting after that first line, (b) suppress the body entirely for untitled notes and
  accept the shorter row, or (c) leave it and say so. Re-record `DayNotesWithNotes` with whichever
  it is and delete this entry.
- **Full record:** the fresh-context review of T-007 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-007)'`.
- **Do not:** fix it by making `displayTitle` return the title only. Its fallback is DoD criterion
  4's own wording, and an untitled note would then draw the `Untitled note` placeholder over a
  body that plainly says what the note is.

### The bottom bar's selected-tab label truncates in German

- **What is wrong:** a third tab (Calendar) halved every tab slot in `CoreBottomBar` — roughly
  132dp to roughly 66dp on a 360dp screen, less on a 320dp one. Only the **selected** tab shows
  its label, with `maxLines = 1, overflow = Ellipsis`. 14sp semi-bold fits the English words
  ("Home", "Calendar", "Setting") and does not fit the German ones: `setting` is "Einstellungen"
  and `home` is "Startseite". `values-de/` is a shipped locale here — lint rates a missing German
  string an **error** — so this is real, not hypothetical.
- **Where:** `ui/component/CoreBottomBar.kt`'s `BottomBarElement`, with the strings in
  `res/values-de/strings.xml`.
- **Why it matters:** it is the only place the bar says in words where the user is, and it
  regressed for German speakers on every screen, not just the new one.
- **A picture defends it now.** `CalendarScreenshotTest.BottomBarTabGermanLabel` renders the tab
  at the real slot size under `locale = "de"` and shows "Einstell…". `BottomBarElement` was made
  `internal` for it, because the whole-bar previews have no `NavController` — every tab there
  comes out unselected, so the one state that can overflow was the one state no picture covered.
  **A passing validation of that case is the defect holding still, not the defect being fixed.**
- **Two things were already tried and are not the answer.** Shrinking the label to 11sp was done
  and reverted: it charged every screen and every language a legibility cost to fit one English
  word, and still did not fit "Einstellungen" — no size in the readable range does.
  `Modifier.basicMarquee`, which `.claude/jetpack-compose-ui.md` § Text gives as the house
  default for single-line overflow, was considered and rejected: this bar is on every screen for
  the life of the app, so a label scrolling sideways for ever is a permanent distraction. The
  same deviation, for the same reason, is already explained in `HomeNoteList`'s date line.
- **What would resolve it:** a product decision — shorter German copy, no tab labels at all, or
  two lines inside a taller bar. Re-record the reference image with it and delete this entry.
- **Full record:** the fresh-context review of T-006 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-006)'`.
- **Do not:** "fix" it by shrinking the font. That was the first attempt and it made things
  worse in two languages to help none.

### A calendar day is a smaller touch target than Android asks for

- **What is wrong:** `CalendarDayCell` fills its column, and seven columns inside 16dp screen
  padding with 4dp gaps gives `(360 − 32 − 24) / 7 ≈ 43.4dp` on a 360dp-wide device and
  `≈ 37.7dp` on a 320dp one. Android's accessibility minimum is 48dp.
- **Where:** `ui/fragment/calendar/component/CalendarDayCell.kt`, sized by
  `CalendarMonthGrid`'s row.
- **Why it matters:** smaller targets are missed more often, and a missed tap on a calendar looks
  like the grid ignoring you — which is the same thing a *disabled* day does, so the two failure
  modes are hard for a user to tell apart.
- **Accepted deliberately, with the arithmetic.** Seven columns cannot each be 48dp on a 360dp
  screen: `7 × 48 = 336dp` leaves 24dp for all padding and gaps combined, so reaching the minimum
  means a grid with no margins and no gutters. The mitigations that do not cost the layout are
  already applied — the `clickable` is on the **outer** box, so the whole square including the
  4dp gap between the selection ring and the fill is live, rather than just the visible tile.
- **What would resolve it:** a design decision about the grid's density, or accepting the trade
  explicitly in the DoD. This entry exists so the number is a decision on record rather than a
  side effect nobody measured.
- **Full record:** the fresh-context review of T-006 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-006)'`.
- **Do not:** shrink the inter-cell gap to buy a dp or two. It buys about 1.7dp, does not reach
  48, and the gap is what stops two adjacent selection rings reading as one shape.

### A bottom sheet dismissed by its own button vanishes instead of animating out

- **What is wrong:** `CoreBottomSheet` calls `sheetState.hide()` only on the `onDismissRequest`
  path — a scrim tap or a swipe down. Both of the delete confirmation's **buttons** work by
  dropping `enable` to false instead, so the `ModalBottomSheet` leaves composition immediately and
  the sheet disappears in one frame while the same sheet swiped away slides out politely. Two
  dismissals of one control, two different animations.
- **Where:** `ui/component/CoreBottomSheet.kt:38-51`, reached from
  `ui/fragment/note/component/NoteDeleteConfirmSheet.kt`.
- **Why it matters:** only polish, but it is the *confirmation* of a destructive action — the one
  moment the app most wants to look deliberate rather than glitchy. It is also the kind of thing
  nobody files later because nobody can name it.
- **Why it is still open:** `CoreBottomSheet` is a shared skeleton primitive with two other
  callers (`RateBottomSheet`, `HomePermissionBottomSheet`), and changing how it closes changes all
  three. That is not a change to make from inside "add delete to the Note screen" — it would be
  modifying unrelated files (ENGINE.md §14) and the other two have no reference image to catch a
  regression.
- **What would resolve it:** give `CoreBottomSheet` a `hideThenDismiss()` the button paths can call
  — `coroutineScope.launch { sheetState.hide(); onDismiss() }` — and repoint all three callers'
  buttons at it, verifying each. **This is the first sheet in the app whose primary dismissal is a
  button rather than a swipe**, which is why it surfaced now.
- **Full record:** the fresh-context review of T-005, finding 8 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-005)'`.
- **Do not:** work around it by having `NoteViewModel` delay lowering `confirmingDelete`. Timing a
  state change to match an animation nobody told it about is how the double-tap-save defect got in.

### Leaving the Note screen after a failed open is a one-way door

- **What is wrong:** when `openNote` cannot produce a note — `NoteProblem.Gone` or
  `NoteProblem.Unreadable` — `NoteFragment` shows a message, calls `consumeProblem()`, and then
  calls `safeNavigateUp()`. `NavigationUtil.safeNavigateUp` catches and logs every exception, so if
  the pop does not happen there is **no second chance**: `problem` has already been cleared, so the
  `LaunchedEffect` will not re-fire, and `opened` is already true, so `openNote` will not run again.
  The user is left on a permanently blank editor whose Save correctly does nothing and whose only
  way out is the back gesture.
- **Where:** `ui/fragment/note/NoteFragment.kt:119-136`, with `ui/util/NavigationUtil.kt:37-43`.
- **Why it matters:** low probability, zero recovery. And the blankness is indistinguishable from
  the defect this whole task removed — a screen showing an empty note that is not a note.
- **Why it is still open:** the fix is a small ordering question with a real trade — consume the
  problem only once the pop is observed, which means the Fragment has to observe the pop, and
  nothing in this app does that yet. Reordering the two lines alone does not fix it: the pop can
  fail either way.
- **What would resolve it:** leave `problem` set until the destination change is observed (a
  `NavController.OnDestinationChangedListener`, or checking `currentDestination` after the call),
  and show the message from a state the screen can re-enter. Alternatively give the blank state an
  explicit "go back" control so there is always a way out that does not depend on the pop.
- **Full record:** the fresh-context review of T-005, finding 9 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-005)'`.
- **Do not:** conclude it is unreachable because `safeNavigateUp` "always works". It is wrapped in
  a `try`/`catch` precisely because it does not.

### The Note editor's keyboard handling is unverified below API 30

- **What is wrong:** possibly nothing, and that is the problem — it cannot be checked here.
  `NoteEditor` uses `Modifier.imePadding()` so the writing surface is not covered by the keyboard.
  That is correct on API 30+. Below 30, `WindowInsets.ime` is a back-port that needs
  `android:windowSoftInputMode="adjustResize"` (added to `MainActivity` in iteration 5) **and** is
  historically suppressed by `android:windowTranslucentStatus`, which `res/values/themes.xml` sets
  on `Core.Theme.JetpackCompose` (lines 17 and 26). `minSdk` is **24**.
- **Why it matters:** if the inset reports zero on an older device, the editor reverts to the
  Critical defect it was fixed for — the user types into a line the keyboard is covering, with no
  scroll range able to reach it. DoD criterion 12's editor clause would be false on every device
  below API 30 while all four Gradle tasks stay green.
- **Why it is still open:** nothing in this repository can run an API 24–29 image. There is no
  emulator, no `adb` capability, and `androidTest` needs a device. Removing
  `windowTranslucentStatus` is a theme change affecting every screen's status bar, which does not
  belong inside "create a note from Home" and would be guesswork without a device to compare
  against.
- **What would resolve it:** open a note on an API 29 emulator, tap the body, type past the
  keyboard's top edge, and confirm the text stays visible. If it does not, the likely fix is
  dropping `windowTranslucentStatus` (the edge-to-edge setup makes it redundant) rather than
  anything in `NoteEditor`. Delete this entry once it has been observed either way.
- **Full record:** the fresh-context review of T-003, finding 1 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-003)'`.
- **Do not:** assume it works because the code reads correctly, and do not assume it is broken
  either. Both would be inventing evidence.

### The Note *editor* has no reference image, so nothing defends how it looks

- **Narrowed in iteration 7.** T-005 added `NoteScreenshotTest.kt`, so the Note screen is no longer
  entirely unpinned — but what it pins is `NoteDeleteConfirmContent`, the confirmation's two
  controls. Everything below still stands for `NoteEditor` itself, which remains unrendered.
- **What is wrong:** T-003 added four `@Preview`s and **zero** `@PreviewTest`, so the app's only
  text-entry screen has no committed rendering. `HomeNoteList` got one in T-002; `NoteEditor` did
  not. Consequences, both concrete: DoD criterion 12's "the editor scrolls rather than clipping"
  has no artifact at all, and criterion 2's perceptual half ("the accent reads as blue and is
  legible on black") is unrecorded for the `primary`-filled Save control and the `primary` cursor —
  the two places this screen paints the accent. The delete confirmation paints `error`, not
  `primary`, so the new image does not cover this.
- **Why it is still open:** the reason is real, not laziness, and it is the same one that kept
  populated note rows unpinned in T-002 — `NoteEditor` prints its date through
  `DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)` against `LocalConfiguration`, so the
  rendering follows the **host's** locale. A reference image of it fails on a colleague's machine
  for a reason that has nothing to do with the app, and the only way to green it again is
  `updateDebugScreenshotTest` — the one command nobody is allowed to reach for. See the host-locked
  entry below.
- **What would resolve it:** either an `imageDifferenceThreshold` / pinned toolchain (which the
  entry below already asks for), or a `@PreviewTest` that renders `NoteEditor` through a
  locale-independent surface — the fields and the divider without the date line, or the date
  supplied as a plain pre-formatted `String` parameter so the test can pass a fixed one. The second
  is a small change to `NoteEditor`'s signature and is the cheaper of the two.
- **Full record:** the fresh-context review of T-003, finding 8 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-003)'`.
- **Do not:** record a reference image of the editor as it stands. It would be host-locked on the
  date line, and a case that fails for the wrong reason teaches the next person to re-record.

### Leaving the Note editor loses the user's text

> **This entry used to have a second half — a refused save and a failed write showing the same
> "please try again" toast. T-008 resolved that and it has been deleted from here.** A refusal now
> travels as a `FutureDateRefusedException` inside `Outcome.Error.throwable`, reaches
> `NoteUiState.saveRefusedDate`, and the editor names the day that was refused. What remains below
> is only the text-loss half, which no DoD criterion asks for.

- **What is wrong:** **nothing is saved on the way out.** `onBack` is wired only to the top bar's
  arrow, and it pops without asking. The hardware/gesture back button bypasses the layout entirely,
  and process death takes the text with it — there is no `SavedStateHandle` and no
  `rememberSaveable`. "Typed 300 words, swiped back, lost them" is a one-gesture data loss.
  (Rotation *is* handled — `NoteViewModel.openNote` refuses to run twice — and tested.)
  **Since T-004 this also loses *edits*, which is worse in kind than losing a new note.** A
  discarded new note never existed, so "I never saved it" matches what the user sees; a discarded
  *edit* returns them to Home showing the note with its old text, its old position and its
  `updatedAt` unmoved — a screen indistinguishable from a save that quietly did nothing.
- **Why it is still open:** no DoD criterion asks for it. A discard confirmation and an autosave are
  different product decisions with different failure modes (autosave creates empty notes), and
  choosing one is not the engine's call. It was re-examined in T-004 and deliberately left as it
  is; see that task file.
- **What would resolve it:** a decision — confirm-on-discard, or save-on-leave, or an explicit
  "notes are kept only when you tap Save".
- **Full record:** the fresh-context review of T-003, finding 5 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-003)'`. The half that was resolved is recorded in
  `.ai/TASKS/T-008.md`; find that checkpoint with
  `git log --oneline --all --grep='loop(T-008)'`.
- **Do not:** add an autosave to make this go away. A note created because somebody opened a screen
  and left is worse than one they lost, and it breaks the "newest first" list with rows nobody
  wrote.

### Screenshot references are exact-pixel and host-locked, and eight orphaned ones are sitting in the tree

- **What is wrong:** two sharp edges on the reference-image lifecycle, both found in iteration 3.
  1. **Validation is exact-pixel against images produced on one machine.** No
     `imageDifferenceThreshold` is configured and no JDK toolchain is pinned
     (`app/build.gradle.kts` sets only `compileOptions` source/target 11, and there is no
     `jvmToolchain`). The six committed PNGs were rendered by layoutlib on Windows with this host's
     JDK and Compose BOM `2024.09.00`. Text rasterisation is not guaranteed identical across OS,
     JDK or a Compose bump — so `validateDebugScreenshotTest`, which DoD criterion 13 requires, can
     go red on a different machine for reasons that have nothing to do with the palette. The only
     thing that makes it green again is `updateDebugScreenshotTest`, which is exactly the command
     nobody is allowed to reach for. That is the trap: a criterion whose only escape hatch is
     forbidden.
  2. **Eight orphaned reference PNGs are sitting in
     `app/src/screenshotTestDebug/reference/com/example/skeleton/screenshot/`.** They are earlier
     renderings whose previews then changed `name`/`widthDp`/`heightDp`, which changes the filename
     hash — the plugin writes a new file and leaves the old one. Under `ThemeScreenshotTestKt/`,
     untracked:
     `PaletteAccents_Palette - accents_479b9379_0.png`,
     `PaletteSurfaces_Palette - surfaces_56c32381_0.png`,
     `ScreenShell_Screen shell_4c69c6b2_0.png`.
     Under `CalendarScreenshotTestKt/`, untracked:
     `BottomBarTabGermanLabel_Bottom bar tab - longest German label_e7c93008_0.png`,
     `DayNotesGermanHeading_Day notes - longest German heading_c2297230_0.png`.
     Under `CalendarScreenshotTestKt/`, **tracked** — orphaned by T-008, which grew three previews
     to fit the new add-note row, and removed from the index with `git rm --cached` in that
     checkpoint so the tracked set stays exactly the live images:
     `DayNotesEmptyDay_Day notes - a day with nothing on it_aec4ce2e_0.png`,
     `DayNotesGermanHeading_Day notes - longest German heading_fa3cecc9_0.png`,
     `DayNotesWithNotes_Day notes - a day with notes on it_22d24b4b_0.png`.

     **The live set is intact — check before assuming otherwise.** T-008's review suspected the
     tracked `BottomBarTabGermanLabel_…_967f30ea_0.png` was itself a dead file with the live one
     untracked, which would have meant `validateDebugScreenshotTest` passing here and failing on a
     clean clone. It is not: the validation report names the reference it used, so
     `grep -o '[a-f0-9]\{8\}_0\.png'
     app/build/reports/screenshotTest/preview/debug/com.example.skeleton.screenshot.<Class>Kt.html`
     lists the live hashes for a class in one command. That is the cheap way to tell a stray from a
     reference, and it is how the eight above were separated from the twenty that are live.
- **Why it is still open:** the engine cannot delete files. `rm` is denied by the permission layer
  (`POLICIES.md` rates deletion high-risk and no deletion capability has been granted), and
  ENGINE.md §13 forbids routing around a denied action. Pinning a JDK toolchain is a build-
  configuration change with its own blast radius and does not belong inside an unrelated task.
- **What would resolve it:** a human deletes the eight files listed above; and either a
  `screenshotTests { imageDifferenceThreshold = … }` block is added with a justified number, or a
  `jvmToolchain` is pinned, or the references are documented as host-locked with the generating
  JDK/OS/AGP recorded next to them. Delete this entry when the strays are gone and one of those
  three is done.
- **Full record:** the fresh-context review of T-010; find the checkpoint with
  `git log --oneline --all --grep='loop(T-010)'`.
- **Do not:** run `updateDebugScreenshotTest` to clear a red validation. Work out *why* the pixels
  moved first — on this host a diff means a real rendering change, and re-recording erases exactly
  the regression the images exist to catch.

### `CLAUDE.md` imports three rule files that do not exist

- **What is wrong:** `CLAUDE.md` `@`-imports `.claude/view-model-layer.md`,
  `.claude/jetpack-compose-ui-layer.md` and `.claude/wiki-connection.md`. None of those files exist.
  The real files are `.claude/viewmodel-layer.md` (no hyphen) and `.claude/jetpack-compose-ui.md`
  (no `-layer` suffix); there is no wiki rule file at all. `.claude/usecase-layer.md` and
  `.claude/figma-design-system.md` exist and are referenced only indirectly.
- **Where:** `CLAUDE.md:1-5`
- **Why it is still open:** `CLAUDE.md` and `.claude/*.md` are human-owned rule sources. ENGINE.md §5
  says these are inspected, never edited. Fixing the import names is the human's call.
- **What would resolve it:** the human renames the imports to the real filenames (or renames the
  files), and decides whether `wiki-connection.md` should exist.
- **Full record:** `git show 9b25307:knowledge/PROJECT.md` — the *Sources Consulted* section lists
  which rule files were actually read.
- **Do not:** assume the ViewModel and Compose rules are unavailable because the import is broken.
  They were read directly from `.claude/viewmodel-layer.md` and `.claude/jetpack-compose-ui.md` at
  bootstrap and their conventions are summarised in `knowledge/PROJECT.md`. Also do not "fix" this by
  creating the missing files — that invents human intent.

### `.claude/figma-design-system.md` tells you to hardcode colours, and names four tokens that do not exist

- **What is wrong:** two separate defects in one human-owned rule file.
  1. **It contradicts the DoD.** § 4(b) *"Inline `Color(0xFF...)` for one-off literals"* and the DO
     bullet *"Inline `Color(0xFF...)` … when they're literals from a node"* directly oppose DoD
     criterion 2 and `POLICIES.md` § User-Interface Defects, which forbid hardcoded colour.
  2. **Its token table references symbols that are not in the codebase.** § 4(a) documents
     `ColorTextPrimary`, `ColorTextSecond`, `ColorBorderSubtle` and `ColorIconMuted`.
     **None of the four is declared in `ui/theme/Color.kt`.** Code written to that table's letter
     does not compile.
- **Where:** `.claude/figma-design-system.md` § 4 "Colors: two systems, used on purpose", and its
  Quick DO/DON'T list.
- **Why it is still open:** `CLAUDE.md` and `.claude/*.md` are human-owned rule sources. ENGINE.md
  §5 says they are inspected, never edited.
- **How to behave until it is fixed:** follow the DoD, not the rule file — source-of-truth order
  (ENGINE.md §3) puts `POLICIES.md` and the approved DoD above repository rule files. Read colours
  from `MaterialTheme.colorScheme.*`; where a semantic name is genuinely wanted, add it to
  `ui/theme/Color.kt` rather than assuming one of the four above exists.
- **What would resolve it:** the human either relaxes DoD criterion 2 or edits the rule file — and
  decides whether the four semantic names should be added to `Color.kt` or dropped from the table.
- **Full record:** the fresh-context review of T-001; find the checkpoint with
  `git log --oneline --all --grep='loop(T-001)'`.
- **Do not:** create the four missing tokens to make the rule file true. That invents human intent,
  and the skeleton's own `Color.kt` already covers these roles under different names.

### One pre-existing string key names a feature this app has never had

- **What is wrong:** `CLAUDE.md` § "String content" requires a string be named for the words it
  contains, not for the feature that shows it. **`allow_exact_alarm_for_prayer_time`** names a
  *prayer-time* feature. This app has no such feature and never did; the key is a leftover from
  whatever project the skeleton was cut from, and its own text ("Allow exact alarms so
  notifications can trigger on time") says nothing about prayer.
- **Where:** `app/src/main/res/values/strings.xml` and the matching `values-de/strings.xml` entry,
  used by `ui/fragment/home/component/HomePermissionBottomSheet.kt`.
- **Why it is still open:** renaming a string key means touching `values/`, `values-de/` and every
  Kotlin call site together, which belongs to no task in this run.
- **What would resolve it:** rename to `allow_exact_alarm` across those three places.
- **Two sibling keys resolved themselves in iteration 4**, as this entry predicted: `home_refresh`
  and `home_no_posts` belonged to the demo `Post` list, and T-002 deleted both locales' copies
  along with the posts UI on Home. Nothing references either key any more.
- **Full record:** the fresh-context review of T-001; find the checkpoint with
  `git log --oneline --all --grep='loop(T-001)'`.
- **Do not:** copy the `<feature>_<word>` shape when adding new strings. New copy follows the rule —
  T-002 added `no_notes_yet` and `untitled_note`, not `home_no_notes`.

### 14 pre-existing UI files hardcode colour literals instead of reading the theme

- **What is wrong:** 65 occurrences of `Color.Black`, `Color.White` or `Color(0xFF…)` across 14
  files under `app/src/main/java/com/example/skeleton/`. `POLICIES.md` § User-Interface Defects
  rates hardcoded colour a **Critical** defect: it is right only by coincidence with whatever
  background happens to sit underneath, and it breaks silently — with the build still green — as
  soon as that background changes.
- **Where** (as of the T-001 checkpoint, which cleared five of these files):
  `ui/component/ratebottomsheet/RateBottomSheet.kt` (14),
  `ui/fragment/home/component/HomePermissionBottomSheet.kt` (9),
  `ui/fragment/setting/language/component/LanguageItem.kt` (5), `ui/component/CoreTopBar4.kt` (5),
  `ui/fragment/setting/component/SettingItem.kt` (4), `ui/component/CoreTopBar.kt` (3),
  `ui/fragment/setting/language/SettingLanguageFragment.kt` (2),
  `ui/fragment/setting/SettingFragment.kt` (1).
  Re-derive the live list with:
  `grep -rE 'Color\.Black|Color\.White|Color\(0x' app/src/main/java/com/example/skeleton`
- **Already fixed, do not re-file:** `ui/theme/Theme.kt`, `ui/theme/Color.kt` (the declarations
  themselves — that is the point of the file), `core/CoreLayout.kt`,
  `ui/component/CoreBottomBar.kt`, `ui/component/CoreBottomSheet.kt` (its `containerColor` default
  was `Color.White` in a dark-only app), and `ui/theme/Type.kt` (`customizedTextStyle`'s default is
  now `ColorTextPrimaryDark`, so an uncoloured `Text` is painted from the palette). The surviving
  `Color.Transparent` and `Color.Unspecified` uses are not colour choices and are fine.
- **Why it is still open:** all of it predates this run. T-001's scope was the theme itself plus the
  two components it explicitly named (`CoreLayout`, `CoreBottomBar`), and DoD criterion 2 binds only
  code **this run introduces** — so rewriting eleven unrelated pre-existing screens would be
  modifying unrelated files (ENGINE.md §14), not completing the task.
- **The screenshot harness does not catch this, and it never will.** T-010 landed
  `validateDebugScreenshotTest` in iteration 3 and it passes with every one of these 65 literals
  still in place — because `Color.White` and `colorScheme.onBackground` render the same pixels.
  DoD criterion 2 therefore has **no automated gate of any kind**: it is checked by a reader, or it
  is not checked. Say so plainly whenever criterion 2 is reported on; a green screenshot run is not
  evidence for it.
- **What would resolve it:** repoint each literal at `MaterialTheme.colorScheme.*`, one file per
  change, verifying appearance against a screenshot reference. Delete this entry when the only
  survivors are the three deliberate ones noted above (`Color.Transparent`, `Color.Unspecified`,
  and `customizedTextStyle`'s default). A custom lint rule — or a `grep` for colour literals under
  `ui/` outside `ui/theme/`, wired into the build — would turn criterion 2 into something a command
  can fail, and is the only way it stops depending on someone remembering to look.
- **Full record:** the checkpoint that established the theme convention — find it with
  `git log --oneline --all --grep='loop(T-001)'`, then read `.ai/TASKS/T-001.md` at that commit
  (`MSYS_NO_PATHCONV=1 git show <sha>:.ai/TASKS/T-001.md`). Cited this way on purpose: the Cleanup
  Commit deletes `.ai/`, so a bare path stops resolving at the branch tip.
- **Do not:** copy the pattern into new files, and do not read
  `.claude/figma-design-system.md` § "Colors: two systems, used on purpose" as permission to. That
  rule file does allow inline literals, but DoD criterion 2 and `POLICIES.md` outrank it for code
  this run writes. Nor should you bulk-rewrite these files opportunistically inside an unrelated
  task — each one changes what a user sees and needs its own verification.
