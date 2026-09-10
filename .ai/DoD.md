# Definition of Done

> Derived from `PRD.md` at bootstrap. Human-owned after approval: the engine may propose changes (Tier 3) but never apply them.
> Every criterion must be verifiable by evidence — a command that can be run, an observable behavior, a measurable property.

## Status

- [x] **APPROVED** — 2026-09-10, by the human operator, through escalation D-001.
      A5 overruled to **title + body**; A1–A4 and A6–A12 accepted as written.
      Immutable to the engine from this point: propose changes (Tier 3), never apply them.

---

## Assumptions baked into these criteria — strike or change any of them before approving

`PRD.md` §7 lists nine open questions. Criteria cannot be written around a question mark, so each one
below has been resolved into a concrete, testable decision. **Every one of these is the human's to
overrule.** Edit the criterion text directly; the engine will implement exactly what this file says.

| # | PRD open question | Decision encoded below | Why |
|---|---|---|---|
| A1 | Compose-in-Fragment hybrid vs. `RecyclerView` | **Hybrid** — Fragments + Navigation Component, Compose inside via `ComposeView` | Not really open: the repository already *is* this. `CoreFragment` exists solely to host `ComposeView`, and `HomeFragment` already renders a `LazyColumn`. Choosing `RecyclerView` would mean deleting the skeleton. |
| A2 | Exact blue | **`#35A0F5`** as `primary` (already `ColorBlue2` in `ui/theme/Color.kt`), `#2F70BC` (`ColorBlue`) as the darker container/secondary role | Both blues already exist in the skeleton; `#35A0F5` is the one already painted on the bottom-bar action button, and it is the lighter of the two, so it survives a black background. |
| A3 | Month vs. week calendar | **Month grid**, with previous/next month navigation | PRD §3.2 says "month view assumed". |
| A4 | How a day's notes are shown after tapping | **Inline, on the Calendar screen, in a scrolling list under the grid** | Keeps the selected day and its notes visible together; avoids a sheet that hides the grid. A bottom sheet is the obvious alternative — say so and it changes. |
| A5 | Note fields | **Title + body** (`title: String`, `content: String`). The title may be blank; a blank title falls back to the first line of the body for display. | **HUMAN OVERRULE, 2026-09-10.** The engine proposed body-only. A reverse-chronological list of every note in the app is the primary navigation surface here, and it is far harder to scan without titles. The engine is right that this is expensive to add later — entity, migration, both screens, tests — which is the reason to do it now rather than later. |
| A6 | Sort key for "latest" | **`updatedAt` descending** | PRD §3.1 says "most recently created/edited note first" — "edited" only has meaning against `updatedAt`. `createdAt` is still stored. |
| A7 | Edit / delete after creation | **Both allowed.** Edit is already implied by PRD §3.1 ("tapping a note opens the Note screen for editing"); delete is added, behind a confirmation step | A note app with no delete is a data trap. Delete is the one genuinely *added* scope here — remove criterion 7 if you don't want it. |
| A8 | Persistence | **Room** | Already wired: `AppDatabase`, Koin `databaseModule`, an existing migration pattern to copy. |
| A9 | min/target SDK | **Unchanged**: `minSdk 24`, `targetSdk 36`, `compileSdk 36` | Already set in `app/build.gradle.kts`. `java.time` is safe because core-library desugaring is on. |
| A10 | Future-day interaction (PRD §3.2, sub-question) | **Non-interactive entirely** — future days are rendered visibly disabled and do not respond to taps | PRD reasons to the same answer ("likely the former"): there is nothing to view on a future day. |
| A11 | Create-note entry point from Home (PRD §3.1, sub-question) | **Yes** — the existing centre action button in `CoreBottomBar` creates a note dated today | The button already exists and currently does nothing. |
| A12 | App display name (not in the PRD) | `app_name` becomes **"Calendar Note"**; `applicationId` / package stay `com.example.*` | The Gradle project is already named "Calendar Note". Renaming the package is a separate, riskier job and is out of scope. |

---

## Acceptance Criteria

1. **Dark only.** `MyApplicationTheme` applies one fixed dark `ColorScheme` regardless of the system
   setting. No `lightColorScheme`, no `isSystemInDarkTheme()`, and no dynamic colour remains anywhere
   in `app/src/main`. The dark scheme fills **every** role it defines (`primary`, `onPrimary`,
   `primaryContainer`, `onPrimaryContainer`, `secondary`, …, `background`, `onBackground`, `surface`,
   `onSurface`, `surfaceVariant`, `onSurfaceVariant`, `outline`, `error`, `onError`) — no role is
   left at its Material default.
2. **Blue primary.** `primary` is `#35A0F5`, declared once in `ui/theme/Color.kt` and consumed
   through the theme. No new composable introduced by this run hardcodes a colour literal; every
   colour a new screen paints comes from the theme or from `ui/theme/Color.kt`.
3. **Notes persist.** A `NoteEntity` table exists carrying at minimum `id`, `title`, `content`, `date`, `createdAt` and `updatedAt`. It is registered in `AppDatabase` with the version
   bumped and a migration written in `data/database/local/Migration.kt`, and its DAO is exposed
   through `injection/DatabaseModule.kt`. A `NoteRepository` interface lives in `domain/repository/`
   with `NoteRepositoryImpl` in `data/repository/impl/`, bound by interface in
   `injection/RepositoryModule.kt`. Notes written on one app run are still present on the next.
4. **Home lists every note, newest first.** The Home screen shows all notes across all days in a
   `LazyColumn`, ordered by `updatedAt` descending. The list scrolls. Each row shows the note's
   title, or — when the title is blank — the first line of its body. When there are no notes, Home
   shows an explicit empty-state message — never a blank region.
5. **Create from Home.** The centre action button in `CoreBottomBar` opens the Note screen for a new
   note dated today. Saving it returns to Home, where the new note is the first row.
6. **Open and edit.** Tapping a row on Home opens the Note screen showing that note's title and content, both editable.
   Saving a change persists it, sets `updatedAt` to now, and Home reflects both the new text and the
   new position at the top of the list.
7. **Delete with confirmation.** A note can be deleted from the Note screen. Deletion always passes
   through an explicit confirmation step, and the confirming control is visually distinct from the
   cancelling one. No single tap deletes a note.
8. **Calendar screen.** A Calendar screen is reachable from the app's navigation (nav-graph
   destination + action, `safeNavigate`). It shows a month grid for the current month, marks today,
   and lets the user move to the previous and next month.
9. **Future days are inert.** Every day after today is rendered visibly disabled and ignores taps.
   Today and every past day are selectable. No add-note affordance is offered for a future day.
10. **The future-date rule is enforced below the UI.** `NoteRepository` refuses to save a note whose
    date is after today and reports the refusal to its caller (it does not throw). A unit test proves
    both halves: a note dated tomorrow is rejected, a note dated today and a note dated yesterday are
    accepted.
11. **A day's notes.** Selecting today or a past day on the Calendar screen shows that day's notes in
    a scrolling list, with an explicit empty state when the day has none, plus an add-note action
    that creates a note dated to the **selected** day (not today).
12. **Text that overruns has defined behaviour.** Note text shown in a list row has an explicit
    overflow behaviour (wrap to a bounded number of lines with ellipsis, with the full text reachable
    by opening the note). The Note screen's editor itself scrolls rather than clipping.
13. **The build is green.** `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest` and
    `./gradlew :app:lintDebug` all succeed, with output recorded.
14. **Documentation matches the code.** `README.md` exists at the repository root and states the
    app's purpose and features, its tech stack, and a package tree for
    `app/src/main/java/com/example/skeleton/` with a purpose note per major package — as
    `CLAUDE.md` requires. `app_name` reads "Calendar Note". Every user-visible string added by this
    run is in `res/values/strings.xml` and named for its own words.

## Constraints

- Kotlin; Jetpack Compose + Material3 hosted in Fragments; Navigation Component for screen
  navigation; Koin for DI; Room for persistence. No new architecture layer, no new DI framework, no
  module split.
- `minSdk 24`, `targetSdk 36`, `compileSdk 36`, `jvmTarget 11` — unchanged.
- Existing skeleton primitives are used, not recreated: `CoreFragment`, `CoreLayout`, `CoreTopBar`,
  `CoreBottomBar`, `customizedTextStyle`, `safeNavigate`, `common.Outcome`.
- The house rules in `CLAUDE.md` and `.claude/*.md` apply to every file written: screen spine
  (`XxxFragment` + private `XxxLayout` with defaulted lambdas + `@Preview`), `_uiState.value =
  _uiState.value.copy(...)`, `customizedTextStyle` instead of `MaterialTheme.typography`, KDoc with
  `@author Phong-Kaster`, no trailing-lambda syntax for named callbacks.
- Out of scope (PRD §6): reminders/notifications, categories, tags, search, multi-user, sync, cloud
  backup, rich text, attachments.
- No change to `applicationId` or the `com.example.skeleton` package.

---

## Verification Evidence Required

Three evidence classes are used, and the difference between them matters:

- **(M) Machine** — a command whose output proves the criterion. Available today (once the toolchain
  capability is granted).
- **(H) Host-side UI test** — a JVM Compose/Room test. **Not possible today**: this repository has no
  Robolectric and no host-side test setup, and `androidTest` needs a device. Proposed as a capability
  + dependency change in `.ai/ESCALATION.md`. Until that is approved, every (H) row falls back to (I).
- **(I) Human inspection** — no command can prove it. Perceptual properties: whether a colour reads
  as blue, whether a disabled day *looks* disabled, whether a marker contrasts with the fill it sits
  on, whether a layout is legible. `POLICIES.md` § Evidence Requirements forbids quietly reducing
  these to their machine-checkable subset, so they are named as human-inspection criteria here.

| Criterion | Class | Evidence |
|---|---|---|
| 1 | M + I | (M) `./gradlew :app:assembleDebug` plus a source check that `app/src/main` contains no `lightColorScheme`, `isSystemInDarkTheme`, or `dynamicDarkColorScheme`, and that every listed role is assigned in the dark scheme. (I) the running app is dark on a device whose system theme is set to **light**. |
| 2 | M + I | (M) source check: `primary = ColorBlue2 /* #35A0F5 */` in the scheme; no `Color(0x…)` literal in files added by this run. (I) the accent reads as blue and is legible on black. |
| 3 | M (H) | (M) build + `AppDatabase` version/migration/DAO registration visible in the diff; Koin module wiring present. (H) a Room in-memory test that inserts and reads back a note. Until (H) exists: (I) create a note, kill the app, reopen it, the note is still there. |
| 4 | M + (H) + I | (M) unit test on the repository: given notes with known `updatedAt`, the emitted list is descending. (H) Compose test asserting the empty-state text is shown for an empty list. (I) the list scrolls past the bottom of the screen with enough notes. |
| 5 | (H) → I | (H) Compose test: tapping the centre action navigates to the Note screen with today's date. Until then: (I) walkthrough on a device. |
| 6 | M + (H) → I | (M) unit test: updating a note sets `updatedAt` later than before and re-orders the list. (H)/(I) the UI walkthrough. |
| 7 | (H) → I | (H) Compose test: one tap on delete shows a confirmation and does **not** delete; confirming does. (I) the confirming control is visually distinct from the cancelling one — perceptual, human only. |
| 8 | M + (H) → I | (M) nav-graph destination + action exist and the fragment class resolves; build passes. (H)/(I) the grid shows the right month and moves between months. |
| 9 | M + (H) → I + **I** | (M) unit test on the date-state helper: a future date maps to a disabled/non-selectable state. (H) Compose test: a tap on a future day does not change the selection. **(I)** whether a disabled day is *legibly* disabled is perceptual and human-only. |
| 10 | **M** | Unit test with a fake DAO: `save(note dated tomorrow)` is refused and returns an error outcome; `save(today)` and `save(yesterday)` succeed. This is the criterion that must never depend on a human looking at a screen. |
| 11 | M + (H) → I | (M) unit test: notes-for-date filtering returns only that day's notes; a note added while day D is selected carries date D. (H)/(I) empty state and scrolling. |
| 12 | (H) → I | (H) Compose test asserting `maxLines`/overflow on the row. (I) whether the truncation still leaves the note identifiable, and that the editor scrolls. |
| 13 | **M** | The three Gradle commands, with their output recorded in the task files. |
| 14 | **M** | `README.md` exists and contains a package tree matching `app/src/main/java/com/example/skeleton/`; `app_name` string value; new strings present in `res/values/strings.xml`. |

**Known evidence gap, stated plainly.** Criteria 1, 2, 7 and 9 each contain a perceptual clause that
**no command can ever prove**. They are marked (I) above and will be reported as human-inspection
items at Final Verification, not silently reduced to their machine-checkable half. If (H) is not
approved, criteria 5, 7, 8, 11 and 12 also depend on a human walkthrough — and the run will say so
rather than report a verified `DONE` over an unproven requirement.
