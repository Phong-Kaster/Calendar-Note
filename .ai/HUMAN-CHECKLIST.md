# Human inspection checklist — Calendar Note

> **What this is.** Every acceptance criterion in `.ai/DoD.md` that **no command can prove**, collected
> into one runnable walkthrough. The run reached this file with the build, 138 unit tests, lint and 20
> screenshot cases all green — none of which can see a colour, a gap, or whether a disabled day looks
> disabled or looks broken.
>
> **It is not a substitute for the machine evidence, and nothing has been moved onto it that a command
> could have carried.** That was checked against the DoD's (M) column while writing it.
>
> Collected from the ten task files in `.ai/TASKS/`, which accumulated these as they went. Source is
> named on every item so you can read the reasoning behind it.

---

## How to use this

There are **three** kinds of item here and they cost very different amounts of your time:

| Part | What it asks | Needs a device? | Time |
|---|---|---|---|
| **A — Approve the pictures** | Look at 20 PNGs already in the repo and say yes or no, once (19 need a judgement; #6 is listed only to explain itself) | no | ~15 min |
| **B — Walk the app** | Install the debug APK and follow 15 numbered steps | **yes** | ~30 min |
| **C — Overrule the product calls** | Five decisions the machine made that are yours to take back | no | ~10 min |

**Part A is the one that compounds.** Each image is already defended by
`./gradlew :app:validateDebugScreenshotTest`, which fails the day the rendering changes. Until a
person agrees an image is *right*, the command is defending a state nobody approved. Approving them
converts DoD criteria 1, 2, 7, 9 and 11's perceptual clauses from **"a person's opinion every
release"** into **"a person's opinion once, defended by a command thereafter"**. Part B does not
compound — it has to be redone by hand every time.

A failure in any item is a defect in the run, not a question. Write it on the item and it becomes the
next run's input.

---

# Part A — Approve the reference images

All 20 live under
`app/src/screenshotTestDebug/reference/com/example/skeleton/screenshot/`. Open them in any image
viewer. **The engine has looked at all 20 and believes they are right — that is not approval, and
recording that distinction is the point of this section** (`.ai/TASKS/T-010.md`, iteration 3's
lesson).

> There are also **8 untracked PNGs** in those folders that are **not** on this list. They are dead
> renderings that `updateDebugScreenshotTest` orphaned when a preview's size changed; the engine
> cannot delete files here. Confirmed dead against the validation report, not guessed from git — see
> `knowledge/PROJECT.md` § Toolchain. Delete them whenever you like; nothing reads them.

### A1 — The theme itself (`ThemeScreenshotTestKt/`) — DoD 1, 2

| # | Image | The question |
|---|---|---|
| 1 | `PaletteAccents_…_d16a2e86_0.png` | Does `#35A0F5` read as **blue**, and is each label legible on its own fill? This is criterion 2's perceptual clause. |
| 2 | `PaletteSurfaces_…_cb0dd2d4_0.png` | Are `background`, `surface` and `surfaceVariant` distinguishable from one another, or does the screen read as one flat black? |
| 3 | `OutlinesAndText_…_d5eb76a7_0.png` | Is `outline` `#55616E` visible without being loud? It is the lowest-contrast role in the scheme: **3.32:1 against the black ground**, but the panel in this image is filled with `surface` `#12171E`, against which the same border is only **2.85:1**. That inner edge is the one most likely to vanish on a cheap panel — judge it, not the outer edge. |
| 4 | `ScreenShell_…_4f5ede87_0.png` | Top bar, ground and bottom bar together — does the app read as deliberately dark rather than unstyled? |
| 5 | `BottomBar_…_7f47a575_0.png` | The centre **+** draws its icon in `onPrimary` `#03121F` (near-black) on the blue. T-001 changed this from `Color.White` — **6.75:1 against 2.80:1** — but it is a change to something a person chose, so a person should see it. |
| 6 | `BottomBarSystemNight_…_d7a4f6ad_0.png` | **Nothing to approve — listed only so you know why there are two.** Same bar rendered under `uiMode = night`; it must be byte-identical to #5, and **that is a command's job, not yours** (it is identical today, and `ThemeScreenshotTest.kt` says validation catches the regression on its own). Judging #5 covers both. |

### A2 — Calendar day cells (`CalendarScreenshotTestKt/`) — DoD 8, 9

Seven states of one square. The overlaps are the interesting part.

| # | Image | The question |
|---|---|---|
| 7 | `DayCellTodayWithNotes_…_60eca93a_0.png` | **The dot on today's filled square.** A prior run shipped this drawn in `primary` on a `primary` fill — invisible, on the one day a user looks at first. Now 6.75:1. Can you see it? |
| 8 | `DayCellTodaySelectedWithNotes_…_ff462092_0.png` | **The worst overlap, and the state the app opens on.** Is a day that is *both* today and picked distinguishable from one that is only today (#7)? |
| 9 | `DayCellSelectedWithNotes_…_edba67b1_0.png` | Does the selection ring read as *selected* rather than as an artifact? |
| 10 | `DayCellPlainWithNotes_…_bb8e5a4f_0.png` | A plain past day carrying notes — dot visible without the day looking marked-up? |
| 11 | `DayCellFuture_…_21d45a31_0.png` | **DoD criterion 9's perceptual clause.** Is it *legibly* disabled — quieter than a live day, without looking like a rendering fault? Only a person can answer this. |
| 12 | `DayCellFutureWithNotes_…_587ac2b5_0.png` | The dimmed dot at **3.16:1**. Dot, or smudge? |
| 13 | `MonthGrid_…_fdb336bd_0.png` | The assembly. Do the weekday labels sit over the right columns (1 September 2026 is a Tuesday, so the first row starts with two blanks), and does the past/future boundary partway through week two read as a change of weight rather than as damage? |

### A3 — A day's notes (`CalendarScreenshotTestKt/`) — DoD 11, 12

| # | Image | The question |
|---|---|---|
| 14 | `DayNotesEmptyDay_…_f675d4bb_0.png` | Does "Nothing written on this day yet" read as **"nothing here yet"** rather than as a **failure**? This is criterion 11's perceptual clause and the whole point of that task. |
| 15 | `DayNotesWithNotes_…_4877ea70_0.png` | Two things: are the two cards far enough apart to read as separate notes; and does **"Add a note to this day"** read as an *action* rather than as a third note? It is deliberately shaped like a note row — same radius, border and surface — set apart only by `primary` and a plus. |
| 16 | `DayNotesNothingPicked_…_4085722f_0.png` | Before any day is tapped. Does "Pick a day to see what is on it" explain the empty space, or is it noise? |
| 17 | `DayNotesGermanHeading_…_65c10232_0.png` | The longest German heading at 288dp. Comfortable, or cramped? A review argued this would truncate and gave the arithmetic; rendering it showed the German date fitting with room to spare. Your eyes settle it. |

### A4 — Home, the note editor, and the German tab — DoD 4, 7 (#20 maps to no criterion)

| # | Image | The question |
|---|---|---|
| 18 | `HomeScreenshotTestKt/NotesEmptyState_…_a18a7273_0.png` | "No notes yet", centred, muted grey on black. Criterion 4's empty state. Does it read as a fresh app rather than a broken one? |
| 19 | `NoteScreenshotTestKt/DeleteConfirmation_…_d72f8e07_0.png` | **DoD criterion 7's perceptual clause, and no command can ever prove it.** Cancel is unfilled `onSurfaceVariant` text at weight 400; Delete is a solid `error` fill with `onError` text at weight 600. They differ in **both** colour role and emphasis, not only in wording. Do they read as clearly different actions at a glance? Once you say yes, `validateDebugScreenshotTest` fails the day somebody makes them look alike. |
| 20 | `CalendarScreenshotTestKt/BottomBarTabGermanLabel_…_967f30ea_0.png` | **This one is a known defect, not a proposal.** "Einstell…" is what a German user sees. Approving the image means agreeing it is the right thing to have a *picture* of — not that the truncation is acceptable. Filed in `knowledge/ISSUES.md`. |

**What Part A still cannot prove:** a screenshot cannot tell a hardcoded `Color.White` from
`colorScheme.onBackground` — they render identical pixels. Criterion 2's *"every colour comes from
the theme"* half is carried by source review and by nothing else (amendment A-004). It was re-checked
at T-009: **no file added by this run contains a `Color(0x…)` literal.** The **8** pre-existing
skeleton files that do — 43 occurrences between them — are filed in `knowledge/ISSUES.md`, which
lists them individually.

---

# Part B — The device walkthrough

Build and install the debug APK — `./gradlew :app:assembleDebug` only *builds* it; install with
`./gradlew :app:installDebug` or `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
**Before you start, set the device's system theme to _light_** — step 1 depends on it.

### 1. The app is dark on a light-system device — **DoD 1**
Open the app with the system theme set to **light**.
**Failure looks like:** any white or light ground, anywhere. One light screen means the theme is
still reading the system setting, and criterion 1 is false.

### 2. The blue accent is legible on black — **DoD 2**
Look at the centre **+** button, the Save control, and a selected bottom-bar tab.
**Failure looks like:** the blue vibrating against the black, or an icon you have to hunt for. Note
that the **selected tab now tints in `primary`** — changed in T-003, and the reference images could
not see it (they render with no active destination, so both tabs draw inactive).

### 3. A new note survives the app being killed — **DoD 3**
Write a note. **Force-stop the app** (not just background it). Reopen it.
**Failure looks like:** the note is gone. This is the only real proof `MIGRATION_2_3` is right —
nothing in this repository executes SQLite, so the migration is verified by construction (copied
verbatim out of Room's own generated statement) and by this step. `fallbackToDestructiveMigration`
is off, so a wrong migration is a **launch crash**, not a silent data loss.

### 4. Home's list scrolls — **DoD 4**
Create enough notes to overflow the screen (10 is plenty). Scroll to the bottom.
**Failure looks like:** the list stops short, or the last row sits under the bottom bar.

### 5. Create from Home, end to end — **DoD 5**
From Home, tap the centre **+**. Type a title and a body. Save.
**Failure looks like:** not landing back on Home, or the new note not being the **first** row.
*Why this is here:* three unit tests cover the halves; nothing covers the `Bundle` between them,
because unit tests run against a stub `android.jar` where `Bundle.putLong` is a silent no-op — an
assertion about it would pass vacuously (`knowledge/PROJECT.md`, A-008).

### 6. The editor stays usable with the keyboard open — **DoD 12**
In the editor, type past where the keyboard's top edge would be.
**Failure looks like:** the caret disappearing under the keyboard. `imePadding()` is in place and is
correct on **API 30+**; **below API 30 it is unverified** and could still be the Critical defect it
was added to fix (`knowledge/ISSUES.md`). **If you have an API 24–29 device, use it for this step.**

### 7. Open, edit, and watch it move to the top — **DoD 6**
Tap a note on Home that is *not* the first row. Change the text. Save.
**Failure looks like:** the change not showing, or the note not jumping to the top of the list.

### 8. A note row responds immediately after a bottom-bar tap — **DoD 6**
Tap a bottom-bar tab, then **immediately** tap a note row.
**Failure looks like:** the first tap on the row doing nothing. This was a real defect — one 800 ms
navigation clock shared by the whole app left every row dead for 800 ms after any tab tap. The fix
asks the nav graph where it is instead, and **no test covers it** because it needs a `NavController`.
While you are there: does the ripple stay inside the row's rounded corners, and is the whole card the
tap target?

### 9. Delete takes two deliberate actions — **DoD 7**
Open a stored note. Tap Delete **once**.
**Failure looks like:** the note disappearing on that single tap. Then: dismiss the sheet with the
**back gesture**, and again by tapping the **scrim**. Neither may delete anything. Then confirm, and
check the note is gone, the toast is readable, and **the screen leaves once, not twice**.
*Why this is here:* the reference image renders `NoteDeleteConfirmContent` directly, not the sheet.
If `NoteFragment` stopped showing the sheet at all, image #19 would still pass.

### 10. Delete is absent on a brand-new note — **DoD 7**
Open a *new* note (centre **+**) and look at the top bar.
**Failure looks like:** a Delete control present, or present-but-greyed. It should be **absent**.
Then, on a stored note: can you hit Delete comfortably without catching Save? They sit side by side
and one of them is irreversible (48dp box, 8dp gap — geometry no command here can check).

### 11. The calendar shows the right month and moves — **DoD 8**
Open Calendar. Check today is marked. Go back a month, forward two.
**Failure looks like:** the weekday labels drifting out of line with the columns, or a month with the
wrong number of weeks.

### 12. A future day ignores taps — **DoD 9**
Tap tomorrow, and a day next month.
**Failure looks like:** the selection moving, or an add-note action appearing for that day.
**Then leave the app open across local midnight, or move the device clock forward a day**, and check
that the boundary moved with it — today must become "past" and tomorrow must become "today".

### 13. A day's notes, and the day heading agreeing with the rows — **DoD 11**
Tap a past day that has notes. Then tap another day quickly.
**Failure looks like:** the heading naming one day while the rows below it are another day's. This
was a real Major defect — a window between the tap and the query — and **every test passed through
it**, because a synchronous fake closes the window. The fix makes the mismatch unrepresentable, but
only a device has the real latency. Also: with a long list, does the grid **plus** its notes scroll,
and is the last note reachable?

### 14. Add a note to a picked past day — **DoD 11**
Pick a past day on the Calendar (any day with a lighter weight). Tap "Add a note to this day". Save.
Check it appears under that day **and** carries that date on Home — not today's.
**Failure looks like:** the note landing on today. The unit tests prove every link of that chain
individually; only a device proves the chain.

### 15. The refusal message — **no criterion owns this; it is a copy judgement**
The future-date **rule** is criterion 10 and is fully machine-proven (see Part D) — what needs your
eye is only its *wording*, which lives in a Fragment that **no test in this project reaches**.

**Making it appear takes a deliberate trick**, because the app is built so you cannot normally get
there: the calendar refuses to select a future day, and the editor has no date control. The one
route is to open the editor first and move the clock afterwards —

1. Pick **today** on the Calendar and tap "Add a note to this day".
2. With the editor open, move the device clock **back one day** (Settings → Date & time).
3. Return to the editor and tap Save. The note is now dated "tomorrow" and the store refuses it.

You should see, with the refused day named in place of `<day>`:

> "`<day>` has not arrived yet — a note can only be written on today or an earlier day"

**Failure looks like:** no feedback at all, or a generic "something went wrong, please try again" —
both were real states this run passed through, and the suite stays green for either.
The message names the day on purpose: the editor has no date control, so "try again" would be an
instruction you cannot act on. **But it is long for a toast.** Shorter wording is a one-line change
to `a_note_can_only_be_written_on_today_or_an_earlier_day` in both locales.

---

# Part C — Product calls the machine made, yours to take back

None of these is a defect. Each is a decision taken so the run could continue, each is reversible
cheaply, and each is recorded with its reasoning in `.ai/STATE.md` or `.ai/AMENDMENTS.md`.

### C1 — The engine wrote your German copy (amendment A-003)
Lint here treats a missing German translation as a build **error**, and the tree was already failing
that way on four strings before the run began. So the engine authored German for **every** string
this run added — **31 keys**, plus two written in T-001 to clear that pre-existing failure. Worst
case is clumsy German, not a wrong app, but **it is product copy nobody asked for**.
*The one-line alternative:* "German is a stale demo locale, drop `values-de/`." That deletes A-003 and
every German string with it, and costs one task. Suppressing the lint check was considered and
rejected — it would green-light criterion 13 over a real defect.

### C2 — The calendar week starts on **Sunday** (`CalendarMonth.WEEK_START`)
Matches the prior run. But `values-de/` is a shipped locale and the German convention is
**Monday-first**, so the grid reads as foreign to a German user. One line, plus a re-record of every
grid image.

### C3 — Saving a note created from **Settings** returns to Settings, not Home
Criterion 5 says "saving returns to **Home**". True on the Home path; from Settings you land back on
Settings without seeing what you wrote. Taken this way because the criterion is written about the
Home flow, because `safeNavigateUp()` is exactly what the Calendar path needs, and because the
alternative — leaving Settings' centre button dead — is the defect T-003 existed to remove.
*Reversible in one line:* `safePopBackstack(destination = R.id.homeFragment, …)`.

### C4 — On the Calendar screen, the centre **+** means **the picked day** (amendment A-010)
Assumption A11 only contemplated Home, where the button means today. On Calendar it follows the
selection. Defensible either way; a product call, not a machine-checkable one.

### C5 — An untitled note prints its first line twice
Once as the heading, once as the body — visible in image #15. Pre-existing since T-002 and filed
rather than fixed, because suppressing the body for untitled notes would also hide lines 2–3 of a
long one. **Which of those is worse is a design call**, which is why it is here.

---

# Part D — What you do *not* need to check

Recorded so this checklist does not quietly grow. All of it is defended by a command that runs in
about 90 seconds:

- **Criterion 10, the future-date rule, is fully machine-proven** — the DoD singles it out as the one
  criterion that must never depend on a human looking at a screen, and it does not. The boundary is
  defended **six ways**: six tests fail if `>` becomes `>=`. Both callers are covered, and the
  refusal travels as a *typed* value (`FutureDateRefusedException` inside `Outcome.Error.throwable`),
  so a screen can tell "I will not store this" from "I could not store this" without reading message
  text. Step 15 is about the **message**, not the rule.
- **Criterion 13** — build, 138 unit tests, lint (`0 errors, 64 warnings`) and 20 screenshot cases,
  all green at the final checkpoint.
- **Criterion 14** — the README package tree was compared against a directory listing: **107 tracked
  `.kt` files, 107 entries in the tree**, every path present and ordered subfolders-before-files.
  `app_name` reads "Calendar Note". Every string this run added is in `res/values/strings.xml` with a
  German counterpart, and named for its own words **with one documented exception**: the seven
  `weekday_short_*` keys carry a qualifier, because a key called `sun` reads as the star and `mon` as
  nothing at all. The reasoning is written inline in `strings.xml` above those keys.
- **The theme does not read the system setting** — images #5 and #6 are byte-identical today, and
  `validateDebugScreenshotTest` fails if they ever diverge. That is criterion 1's *machine* half;
  step 1 covers the half a command cannot see.
- **The empty states exist** (criteria 4, 11). Deleting the empty-state branch leaves **all 138 unit
  tests green** and fails only two pictures — which is exactly why Part A matters.

One line here is narrower than it looks, and is called out rather than rounded up:

- **`updatedAt`-descending ordering** (criteria 4, 6) is enforced **twice** — `NoteDao` orders in SQL
  and `NoteRepositoryImpl` sorts the result again — but only the **store's** sort is tested.
  Mutation-checking reaches the store and the screen; it cannot reach the `ORDER BY`, because
  checking a SQL clause means running SQLite, which needs an emulator this project has no setup for.
  The second sort is not redundancy for its own sake: it is what moves the guarantee off a query
  string and onto code a plain JVM test can hold. **If the SQL clause were silently wrong, every test
  would still pass and the app would still be correct** — so this is defence-in-depth working, not a
  gap in the app. It is named here so the next person does not read "enforced twice" as "tested
  twice".

---

*Generated by task T-009. Sources: `.ai/TASKS/T-001.md` … `T-010.md`, `.ai/DoD.md` § Verification
Evidence Required, `.ai/PLAN.md` § Verification approach.*
