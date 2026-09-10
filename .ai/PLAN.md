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
   day. ~~The future-date rule lands in the repository in T-008~~ — **corrected in iteration 5
   (amendment A-006): it landed in T-003.** The original reasoning was right about *where* the rule
   belongs (the repository, not a grid cell) and wrong about *when*: it timed the rule to the task
   where a future date first becomes **selectable**, which is a fact about the UI, while the rule is
   about what the data layer accepts. The moment `save` existed and took any date it was handed, the
   code contradicted `knowledge/DOMAIN.md`. **The lesson worth carrying: schedule a domain rule to
   the task that creates its first write path, not to the task that creates its first tempting
   caller.** T-006 still disables the cell; T-008 is now the second caller reaching a guard that
   already holds.
4. **Documentation and the human-inspection package last** (T-009), when the package tree is final.

**Integration approach.** The demo `Post` / network stack (`PostApi`, `PostRepository`, `PostDao`,
`WeatherApi`, `networkModule`) stays wired but stops being used by Home. Deleting it is not asked
for by the PRD, and this repository is explicitly a reusable skeleton whose network layer is part of
its value. `HomeViewModel` is repointed from posts to notes; the post plumbing is left intact.

**Verification approach — revised by escalation D-001 (amendment A-002).** Four layers now, not
three. The bootstrap iteration concluded this repository had no host-side test setup and that the
perceptual halves of DoD criteria 1, 2, 7 and 9 were unprovable by any command. **That conclusion was
wrong.** A prior run on the unmerged branch `loop/todo-calendar-screens` already built a working
Compose Preview Screenshot Testing harness on this toolchain; the engine simply never looked at
sibling `loop/*` branches. T-010 imports it.

The consequence is not cosmetic: with a committed reference image, a perceptual property *does* have
a standard a command can fail against. Those four criteria move from "human inspection forever" to
**"the human approves the reference image once; `validateDebugScreenshotTest` defends it
thereafter"**. Say exactly that at Final Verification — neither "proven" nor "unprovable".

The layers, in increasing cost:

- **Unit tests (JVM, no device)** carry everything that can be expressed without a screen: the
  future-date rule, `updatedAt` ordering, notes-for-a-date filtering, and the day-cell state
  mapping (past / today / future). These are written against a fake DAO so they need no Room and no
  Android context. This is where DoD criterion 10 lives, and it must never depend on a human looking
  at a screen. **Iteration 5 widened this layer twice, and both are worth reusing:** a ViewModel is
  testable here (`Dispatchers.setMain` + an unconfined test dispatcher — see
  `knowledge/PROJECT.md`), so screen *logic* need not wait for a host-side UI test; and error paths
  are testable now that framework stubs return defaults instead of throwing (A-008).

  **A test is not evidence until it has been made to fail.** Iteration 5 mutated the
  implementation three times and recorded which tests broke, because a green suite against correct
  code is indistinguishable from a green suite against a tautology — and iteration 4 had already
  been bitten by a fake DAO that would have agreed with the code instead of checking it. Do this for
  any test that carries a DoD criterion on its own, criterion 10 above all.
- **Build + lint** on every checkpoint. Criterion 13's command set now also includes
  `:app:validateDebugScreenshotTest` (D-001; `DoD.md` itself is immutable to the engine and was not
  edited to say so — see A-002).
- **Screenshot tests** (`app/src/screenshotTest/`, host-side layoutlib, no device) — **live since
  iteration 3**, six cases, references committed. For everything that is judged by looking: marker
  contrast in *combined* states, disabled-day styling, empty states, the delete confirmation's two
  controls, the dark-and-blue theme itself. The prior run's suite names the three regressions it
  caught, all of which passed a green unit suite — write the equivalent cases rather than
  rediscovering them.

  **But a screenshot cannot see criterion 2** (amendment A-004). `Color.White` and
  `colorScheme.onBackground` render identical pixels, so "colour comes from the theme, never
  hardcoded" gets **no** help from this layer — the six cases pass today with 65 hardcoded literals
  still in the tree. Criterion 2 is carried by the fresh-context review of each diff and by nothing
  else. Every task from here states that honestly rather than pointing at a green screenshot run.
- **Human inspection, once per image.** What remains for a person is approving each reference image
  the first time it is generated. T-009 collects those approvals rather than a full manual
  walkthrough.

`updateDebugScreenshotTest` overwrites references. It is never the fix for a failing validation —
that silently re-baselines the regression the test existed to catch.

**Adding a string is a two-file operation (amendment A-003).** Lint here treats
`MissingTranslation` as an *error*, and this repository ships a `values-de/` locale — so a string
appended to `res/values/strings.xml` alone turns criterion 13 red. Every task that adds user-visible
copy appends the German equivalent to `res/values-de/strings.xml` in the same checkpoint. This was
found by lint failing on the untouched baseline, not inferred.

**Pure logic goes somewhere testable.** The date-state mapping and the ordering rule are put in
plain Kotlin (domain model + repository), never inside a `@Composable`. A rule inside a composable
can only be checked by rendering it, which is precisely the capability this repository does not have.

## Task Graph

- ✅ T-001 — Dark-only theme with blue primary, app named "Calendar Note", README seeded (depends on: —)
- ✅ T-010 — Import the host-side screenshot harness from `loop/todo-calendar-screens` (depends on: T-001)
- ✅ T-002 — Home shows persisted notes newest-first, with an empty state (depends on: T-001)
- ✅ T-003 — User can create a note for today from Home (depends on: T-002) — **also carried the
  future-date rule (A-006) and the load-existing-note path (A-007), so DoD criterion 10 is done**
- T-004 — User can open and edit an existing note; Home re-sorts (depends on: T-003) — **reduced by
  A-007 to the Home-row route, the re-sort evidence and the blanked-title case**
- T-005 — User can delete a note behind a confirmation step (depends on: T-004, T-010)
- T-006 — Calendar screen: month grid, today marked, month navigation, future days inert (depends on: T-002, T-010)
- T-007 — Selecting today or a past day shows that day's notes, with an empty state (depends on: T-006)
- T-008 — Add a note to the selected day (depends on: T-007, T-003) — **reduced by A-006: the
  refusal is implemented and tested; this task is the second caller reaching it, plus surfacing a
  refusal distinguishably**
- T-009 — README, package tree, strings audit, and the human-inspection checklist (depends on: T-005, T-008)

## Known Risks

- ~~**No toolchain has ever run here.**~~ **Resolved in iteration 2.** The skeleton builds, its unit
  test stub passes, and lint runs — see `knowledge/PROJECT.md` § Toolchain, now `Verified: yes`.
  The risk paid off exactly as written: running the baseline *before* editing anything is what
  revealed that **lint was already failing on the untouched tree** (four `MissingTranslation`
  errors). Had T-001's changes gone in first, a pre-existing red would have looked like this run's
  regression. Keep doing that: measure before you touch.
- **Look in the repository's own history before concluding it cannot do something.** This risk is
  written from a mistake already made: the bootstrap iteration declared the perceptual criteria
  unprovable while a verified screenshot harness sat on a sibling `loop/*` branch, reachable with
  baseline capabilities the whole time. `git branch --list 'loop/*'` costs nothing.
- **Pushing logic out of composables is still right**, and is now belt *and* braces rather than the
  only mitigation. A rule in plain Kotlin is cheaper to test than a rule that must be rendered, even
  when rendering is available.
- ~~**Room migration.**~~ **Landed in iteration 4, and the risk was real.** `AppDatabase` is now at
  `version = 3` with `MIGRATION_2_3`. `fallbackToDestructiveMigration(false)` means a migration
  that disagrees with the entity is a **launch crash**, and nothing in this repository can catch
  one: no Robolectric, no device, no unit test that executes SQLite. The mitigation that worked is
  in `knowledge/PROJECT.md` — copy the `CREATE TABLE` out of Room's generated `AppDatabase_Impl`
  instead of composing one by eye. Writing it by eye produced a `DEFAULT ''` the entity does not
  declare (amendment A-005). **Any future migration takes the same route.**
- **`CoreLayout` hardcodes `Color.Black`** as its background and `customizedTextStyle` defaults to
  white text. This happens to be correct for a dark-only app, but it means the theme's `background`
  role is not what actually paints the screen. Changing the theme alone will not change what the user
  sees; T-001 has to check both.
- **Perceptual criteria cannot regress-test themselves.** Criteria 1, 2, 7 and 9 pass or fail on a
  human's eyes. A later iteration can break them and every automated check will stay green.
- **`java.time` on `minSdk 24`** works only through core-library desugaring, which is enabled. It is
  a build-configuration dependency, not a language guarantee — if desugaring is ever turned off,
  every date type in this feature breaks at once.
