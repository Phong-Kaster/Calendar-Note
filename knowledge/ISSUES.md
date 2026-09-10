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

### A note that fails to load opens as a blank editor, and saving it writes a *second* note

- **What is wrong:** `NoteRepositoryImpl.getNote` answers `null` for two different situations — the
  row is not there, and the read threw — and `NoteViewModel.openNote` treats `null` as "start a new
  note" (`val note = stored ?: Note.draft(date = date)`). So the editor opens **blank but correctly
  dated**, with no message. The user retypes and taps Save; `openedNote` is a draft carrying
  `Note.UNSAVED_ID`, so Room's `autoGenerate` hands out a fresh row. Home then shows **two** notes
  for that day: the original with its old text, untouched, plus the retyped copy.
- **Why it matters:** it is the shape of data loss the rest of this screen was carefully built to
  avoid — the note the user thought they were editing keeps its old text and its old list position,
  and nothing anywhere says so. `NoteViewModelTest`'s *a note that has since been deleted opens as
  a fresh one for the day asked for* pins the fall-through as intended, so no existing test will
  ever catch it.
- **When it became reachable:** T-004. Until the Home rows became clickable, every caller of
  `argumentsFor` left `noteId` at its default, so `getNote` had no in-app caller and this branch was
  dead code. Today it still needs a database failure to reach.
- **When it stops being rare:** **T-005.** That task adds delete, and "opened a note that was
  deleted" stops needing a disk fault — at which point a deleted note silently resurrects itself as
  a duplicate.
- **What would resolve it:** distinguish the two cases before the ViewModel has to guess — `getNote`
  returning `Outcome<Note?>`, or a separate signal — and then decide each one deliberately: a note
  that is *gone* should leave the screen (or say so), and a note that *failed to read* should
  report the failure rather than impersonate a new note. The one thing it must not keep doing is
  presenting a blank editor whose save inserts.
- **Full record:** the fresh-context review of T-004, finding 2 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-004)'`.
- **Do not:** "fix" it by keeping the requested id on the draft so the save updates in place. That
  silently re-creates a note the user deleted, which is a different wrong answer, not a smaller one.

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

### The Note screen has no reference image, so nothing defends how it looks

- **What is wrong:** T-003 added four `@Preview`s and **zero** `@PreviewTest`, so the app's only
  text-entry screen has no committed rendering. `HomeNoteList` got one in T-002; `NoteEditor` did
  not. Consequences, both concrete: DoD criterion 12's "the editor scrolls rather than clipping"
  has no artifact at all, and criterion 2's perceptual half ("the accent reads as blue and is
  legible on black") is unrecorded for the `primary`-filled Save control and the `primary` cursor —
  the two places this screen paints the accent.
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

### Leaving the Note editor either loses the user's text or traps them on the screen

- **What is wrong:** two gaps at the editor's exits, neither claimed to be handled anywhere.
  1. **Nothing is saved on the way out.** `onBack` is wired only to the top bar's arrow, and it
     pops without asking. The hardware/gesture back button bypasses the layout entirely, and
     process death takes the text with it — there is no `SavedStateHandle` and no
     `rememberSaveable`. "Typed 300 words, swiped back, lost them" is a one-gesture data loss.
     (Rotation *is* handled — `NoteViewModel.openNote` refuses to run twice — and tested.)
     **Since T-004 this also loses *edits*, which is worse in kind than losing a new note.** A
     discarded new note never existed, so "I never saved it" matches what the user sees; a
     discarded *edit* returns them to Home showing the note with its old text, its old position and
     its `updatedAt` unmoved — a screen indistinguishable from a save that quietly did nothing.
  2. **A refused save has no way forward.** Both a refusal and a genuine write failure surface as
     the same `we_are_sorry` toast ("something went wrong, please try again"), and retrying a
     *refusal* can never succeed. The editor exposes no date control, so the user cannot change the
     thing that was refused.
- **Why it is still open:** no DoD criterion asks for either. A discard confirmation and an autosave
  are different product decisions with different failure modes (autosave creates empty notes), and
  choosing one is not the engine's call. Part 2 is currently reachable only if the device clock
  moves backwards between opening the editor and saving — T-004 re-checked this and it did *not*
  change, because an existing note's date is always today or earlier, so re-saving it can never be
  refused. Part 1 was re-examined in T-004 and deliberately left as it is; see that task file.
- **What would resolve it:** for part 1, a decision — confirm-on-discard, or save-on-leave, or an
  explicit "notes are kept only when you tap Save". For part 2, distinguish refusal from failure in
  the outcome and give the refusal its own message naming the date.
- **When it stops being cosmetic:** **T-008.** That task opens this same editor on a day the user
  picked on the calendar, so a refusal becomes reachable by ordinary use rather than by a clock
  change, and part 2 must be resolved there.
- **Full record:** the fresh-context review of T-003, findings 5 and 6 — find the checkpoint with
  `git log --oneline --all --grep='loop(T-003)'`.
- **Do not:** add an autosave to make part 1 go away. A note created because somebody opened a
  screen and left is worse than one they lost, and it breaks the "newest first" list with rows
  nobody wrote.

### Screenshot references are exact-pixel and host-locked, and three orphaned ones are sitting in the tree

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
  2. **Three orphaned reference PNGs are untracked in
     `app/src/screenshotTestDebug/reference/…/ThemeScreenshotTestKt/`:**
     `PaletteAccents_Palette - accents_479b9379_0.png`,
     `PaletteSurfaces_Palette - surfaces_56c32381_0.png`, and
     `ScreenShell_Screen shell_4c69c6b2_0.png`. They are earlier renderings whose previews then
     changed `name`/`widthDp`/`heightDp`, which changes the filename hash — the plugin writes a new
     file and leaves the old one. They were deliberately **not** committed, so the tracked reference
     set is exactly the six live images, but the files are still on disk.
- **Why it is still open:** the engine cannot delete files. `rm` is denied by the permission layer
  (`POLICIES.md` rates deletion high-risk and no deletion capability has been granted), and
  ENGINE.md §13 forbids routing around a denied action. Pinning a JDK toolchain is a build-
  configuration change with its own blast radius and does not belong inside an unrelated task.
- **What would resolve it:** a human deletes the three files listed above; and either a
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
