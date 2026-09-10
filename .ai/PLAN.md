# PLAN

> Machine-owned execution strategy. The human never reviews this file — only the Definition of Done.
> Evolves through Tier-1 amendments (logged in AMENDMENTS.md) and Tier-2 proposals (human-approved).

## Strategy

**Shape of the work.** This is not a greenfield build. The repository is already a working
Fragment + Compose + Koin + Room skeleton with a demo Home screen backed by a network `Post` list.
The job is to (a) repoint the theme, (b) replace the demo feature with the Note feature, and
(c) add the Calendar screen. Nothing about the architecture changes — every new file has a sibling
in the repository to copy the shape from.

**Order of attack.** Theme first, then the note lifecycle end-to-end on Home, then Calendar.

1. **Theme before screens** (T-001). Every later task paints UI. If the fixed dark scheme with the
   blue primary does not exist yet, those tasks reach for hardcoded colours — which
   `POLICIES.md` § User-Interface Defects calls a Critical defect, and which is exactly the failure
   that survives a green build. Doing the theme first means there is always a correct thing to reach
   for. It also fills every Material role up front, so no later component falls through to a default
   nobody chose.
2. **The note lifecycle on Home** (T-002 → T-005), one verb per checkpoint: read, create, edit,
   delete. Each leaves the app usable at a higher level than the one before. Persistence lands in
   T-002 because a list with nothing behind it is not observable behaviour.
3. **Calendar** (T-006 → T-008), likewise: show the grid, then show a day's notes, then add to a
   day. The future-date rule lands in the repository in T-008 rather than in the calendar UI in
   T-006, because a rule enforced only in a grid cell is a rule that a second caller can walk past.
   T-006 disables the cell; T-008 makes the disabling redundant.
4. **Documentation and the human-inspection package last** (T-009), when the package tree is final.

**Integration approach.** The demo `Post` / network stack (`PostApi`, `PostRepository`, `PostDao`,
`WeatherApi`, `networkModule`) stays wired but stops being used by Home. Deleting it is not asked
for by the PRD, and this repository is explicitly a reusable skeleton whose network layer is part of
its value. `HomeViewModel` is repointed from posts to notes; the post plumbing is left intact.

**Verification approach.** Three layers, in increasing cost:

- **Unit tests (JVM, no device)** carry everything that can be expressed without a screen: the
  future-date rule, `updatedAt` ordering, notes-for-a-date filtering, and the day-cell state
  mapping (past / today / future). These are written against a fake DAO so they need no Room and no
  Android context. This is where DoD criterion 10 lives, and it must never depend on a human looking
  at a screen.
- **Build + lint** on every checkpoint.
- **Human inspection** for the perceptual criteria the DoD names explicitly (1, 2, 7, 9). T-009
  assembles these into one walkthrough checklist so the human runs the device once, at the end,
  instead of once per iteration.

If the host-side UI-test capability proposed in the bootstrap escalation is granted, add a test task
after T-005 and after T-008 (Tier-1 amendment) and move the (H) rows of the DoD evidence table out of
the human checklist.

**Pure logic goes somewhere testable.** The date-state mapping and the ordering rule are put in
plain Kotlin (domain model + repository), never inside a `@Composable`. A rule inside a composable
can only be checked by rendering it, which is precisely the capability this repository does not have.

## Task Graph

- T-001 — Dark-only theme with blue primary, app named "Calendar Note", README seeded (depends on: —)
- T-002 — Home shows persisted notes newest-first, with an empty state (depends on: T-001)
- T-003 — User can create a note for today from Home (depends on: T-002)
- T-004 — User can open and edit an existing note; Home re-sorts (depends on: T-003)
- T-005 — User can delete a note behind a confirmation step (depends on: T-004)
- T-006 — Calendar screen: month grid, today marked, month navigation, future days inert (depends on: T-002)
- T-007 — Selecting today or a past day shows that day's notes, with an empty state (depends on: T-006)
- T-008 — Add a note to the selected day; the repository refuses future-dated notes (depends on: T-007, T-003)
- T-009 — README, package tree, strings audit, and the human-inspection checklist (depends on: T-005, T-008)

## Known Risks

- **No toolchain has ever run here.** Every command in `knowledge/PROJECT.md` is unverified and
  `./gradlew --version` was refused at bootstrap. The first iteration after approval must treat
  "does this project build at all?" as its first finding, before treating any code as correct.
  If it does not build out of the box, that is a discovery to reconcile, not a defect introduced by
  this run.
- **No host-side test infrastructure.** `androidTest` needs a device this engine cannot drive, and
  there is no Robolectric. Anything that can only be proven by rendering is currently unprovable —
  see the DoD's stated evidence gap. The mitigation is to push logic out of composables, not to
  pretend the gap is smaller than it is.
- **Room migration.** `AppDatabase` is at `version = 2` with `fallbackToDestructiveMigration(false)`,
  so a wrong or missing migration crashes the app at launch rather than degrading quietly. Adding
  `NoteEntity` means version 3 **and** a hand-written `MIGRATION_2_3`.
- **`CoreLayout` hardcodes `Color.Black`** as its background and `customizedTextStyle` defaults to
  white text. This happens to be correct for a dark-only app, but it means the theme's `background`
  role is not what actually paints the screen. Changing the theme alone will not change what the user
  sees; T-001 has to check both.
- **Perceptual criteria cannot regress-test themselves.** Criteria 1, 2, 7 and 9 pass or fail on a
  human's eyes. A later iteration can break them and every automated check will stay green.
- **`java.time` on `minSdk 24`** works only through core-library desugaring, which is enabled. It is
  a build-configuration dependency, not a language guarantee — if desugaring is ever turned off,
  every date type in this feature breaks at once.
