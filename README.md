# Calendar Note

A dark-only Android note-taking app organised around dates. You write notes against a day, read
them back newest-first on the Home screen, or jump to a specific day through a month calendar to see
just that day's notes.

Built on a reusable Kotlin + Jetpack Compose skeleton: single Activity, Fragments driven by the
Navigation Component, Compose inside each Fragment, Koin for dependency injection, Room for storage.

---

## Purpose

Two things at once:

1. **A working note app.** Capture a thought, tie it to a day, find it again.
2. **A skeleton worth copying.** Every layer is small, obvious, and named the same way, so a new
   project can lift whole packages out of it. Where a choice had to be made between clever and
   readable, this codebase picks readable.

## Features

| Feature | Status |
|---|---|
| Dark-only theme with a blue accent, fixed regardless of the system light/dark setting | ✅ built |
| Language picker in Settings — offers 7 languages, but only English and German have translations; the other 5 fall back to English | ⚠️ partial (inherited from the skeleton) |
| Rate-app and permission-request bottom sheets | ✅ built (inherited from the skeleton) |
| Home screen listing every note, most-recently-edited first, with an empty state | ✅ built |
| Create a note for today from the bottom bar's centre button | ✅ built |
| Tap a note on Home to open it, edit it, and see it move back to the top of the list | ✅ built |
| Delete a note, behind a confirmation step | ✅ built |
| Month calendar with today marked and previous/next month navigation | ✅ built |
| Future days on the calendar are visibly disabled and cannot be picked | ✅ built |
| A dot on every calendar day that already has notes on it | ✅ built |
| Tap a day to see that day's notes under the grid, with an explicit message when it has none | ✅ built |
| Tap one of that day's notes to open it in the editor | ✅ built |
| Add a note dated to the day picked on the calendar (rather than to today) | ✅ built |
| Refuse — and say why — when a note would be dated in the future | ✅ built |
| Alarms as a fourth bottom-bar tab, opening on an explicit empty state | ✅ built |
| Write an alarm — a message and a time — save it, and see it in the list | ✅ built |
| Alarms survive the app being killed, and are listed earliest time of day first | ✅ built |
| Refuse — and say why — when an alarm would be saved with nothing written on it | ✅ built |
| Re-open an alarm from its row, change it, and save it back to the same row | ✅ built |
| Switch an alarm off or on from its row, without opening it | ✅ built |
| Delete an alarm, behind a confirmation step | ✅ built |
| Have an alarm actually go off, as a heads-up notification, and repeat the next day | ✅ built |
| Alarms still fire after the phone is restarted | ✅ built |
| When the OS will not let an alarm fire, the Alarms screen says so and offers the fix | ✅ built |

> **Implementation status is deliberately explicit.** The note feature is complete: every row above
> is code that exists, and this table was updated in the same change that landed each piece — a
> README that claims a feature the code does not have is worse than no README.
>
> Two rows are honest about being less than finished. The **language picker** is inherited and only
> two of its seven languages have translations. The **German bottom-bar label** truncates
> ("Einstell…") — a known defect, filed in `knowledge/ISSUES.md` and pinned by a reference image so
> it cannot get quietly worse. **Alarms is complete**: an alarm can be written, saved, listed,
> reopened and changed, switched off or on, deleted behind a confirmation, and — at the time set —
> arrives as a heads-up notification carrying its message, then repeats the next day with no further
> action from the user; it survives a restart of the phone; and when the operating system will not
> let an alarm fire at all (notifications off, or exact alarms refused), the screen says so and offers
> the fix, clearing itself once the setting is corrected. What no command in this repository can
> check — whether a real phone actually pops the banner up, whether tapping it opens the Alarms list
> without stacking a second copy of the app, and whether an alarm set before a reboot really does
> survive one — is left for a person; see the Human Verification Request when this run finishes.

## Business rules

Both of these are implemented and enforced where the text below says they are. Both are covered by
JVM unit tests — with one gap worth naming: the `ORDER BY` half of the second rule is not, because
testing a SQL clause means running SQLite, which needs an emulator this project has no setup for.
The store's own sort is what carries that guarantee under test.

- **A note can never be dated in the future.** Today is allowed, tomorrow is not. Enforced in
  `NoteRepositoryImpl.save`, below the UI, so that no caller can walk past it — a screen that
  merely hides the affordance does not satisfy it. The refusal comes back as an
  `Outcome.Error`; the store never throws at the screen above it.

- **Notes are ordered most-recently-touched first** — by `updatedAt` descending, where "touched"
  means created *or* edited. Enforced twice on purpose: `NoteDao` orders in SQL, and
  `NoteRepositoryImpl` sorts the result again so the guarantee belongs to the store rather than to
  a query string, and so a plain JVM test can hold it to that.

Those two are the project's whole domain rulebook, and the authoritative copy is
`knowledge/DOMAIN.md`. The point below is **not** a third rule — it is the convention the code
follows in order to keep the second one true:

- **No screen stamps a note; the store does.** `NoteRepositoryImpl.save` sets `createdAt` on a note
  that has never been stored and moves `updatedAt` on every save, reading a `Clock` injected into
  it. Two screens reading two clocks would produce a list order nobody could explain. Which *day*
  a note belongs to is the opposite case: that is the caller's choice — today from Home's centre
  button, a selected day from the Calendar screen — and the store only checks it, by refusing a
  day in the future.

## Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3, hosted in Fragments via `ComposeView` |
| Navigation | Navigation Component (`navigation_graph.xml`), single Activity |
| Dependency injection | Koin |
| Local storage | Room (`app_database`) + DataStore (preferences) |
| Networking | Ktor client (OkHttp engine) + kotlinx.serialization, wrapped in `safeApiCallFlow` |
| Dates | `java.time` — safe on `minSdk 24` because core-library desugaring is enabled |
| SDK levels | `minSdk 24`, `targetSdk 36`, `compileSdk 36`, `jvmTarget 11` |

## Build and check

```bash
./gradlew :app:assembleDebug                 # build the debug APK
./gradlew :app:testDebugUnitTest             # JVM unit tests
./gradlew :app:lintDebug                     # Android Gradle Plugin lint
./gradlew :app:validateDebugScreenshotTest   # compare the UI against the committed reference images
./gradlew :app:updateDebugScreenshotTest     # re-record those images (see the warning below)
```

`local.properties` must exist with your `sdk.dir` — it is git-ignored, so a fresh clone will not
build until you create it.

> **Lint treats a missing German translation as an error.** Every string added to
> `res/values/strings.xml` needs a matching entry in `res/values-de/strings.xml`, or `lintDebug`
> fails. This is easy to forget and the failure message does not mention the file you actually
> edited.

> **`updateDebugScreenshotTest` overwrites the reference images, so it is never the fix for a
> failing validation.** If `validateDebugScreenshotTest` goes red, something about the rendering
> changed — decide whether that change was intended *first*. Re-recording to make the red go away
> silently erases the regression the test existed to catch. Run it only when you meant to change
> what the UI looks like, and eyeball the new images before committing them.

### Test source sets

| Path | What lives there | Needs a device? |
|---|---|---|
| `app/src/test/` | Plain JVM unit tests. Compose's non-`@Composable` API (`darkColorScheme()`, `Color`) works here. | no |
| `app/src/screenshotTest/` | Compose Preview Screenshot Tests — `@PreviewTest @Preview` functions rendered host-side by layoutlib. | no |
| `app/src/screenshotTestDebug/reference/` | The committed reference PNGs those tests are compared against. | — |
| `app/src/androidTest/` | Instrumented tests. | yes |

The screenshot suite is how this project checks things that only exist as pixels — that the theme
really is dark, that the accent really is a legible blue, that a border is actually visible. A build
and a unit test are equally happy with an unreadable palette, so those properties are pinned as
images a human approved once.

Two things worth knowing before you add a case:

- The reference filename ends in a hash of the preview's parameters. Change `name`, `widthDp` or
  `heightDp` and the old PNG is **orphaned** rather than replaced — delete it by hand, or dead
  images pile up in the reference folder.
- Anything that animates (`basicMarquee`, `AnimatedContent`, Lottie) renders differently each run
  and makes validation a coin flip. Use a static equivalent inside a screenshot case.

---

## Package tree

Rooted at `app/src/main/java/com/example/skeleton/` — the app's own code. The test source sets are
listed in the table above. Subfolders are listed before the `.kt` files that sit alongside them.

```
com/example/skeleton/
├── common/                                 # Types every layer may use. No Android, no framework.
│   ├── Constant.kt                         #   App URLs, DataStore name
│   ├── Language.kt                         #   Supported UI languages
│   ├── Outcome.kt                          #   Loading / Success / Error — the project's own result type
│   └── Typealias.kt
├── core/                                   # Skeleton-provided base classes. Use them; don't recreate them.
│   ├── config/
│   │   └── AppConfig.kt                    #   API base URL, timeouts
│   ├── extension/                          #   Small, dependency-free Kotlin extensions
│   │   ├── collection/
│   │   │   ├── AdvancedCollectionExtension.kt
│   │   │   └── BasicCollectionExtension.kt
│   │   ├── date_and_time/
│   │   │   ├── DateExtension.kt
│   │   │   └── LocalDateExtension.kt
│   │   ├── flow/
│   │   │   └── FlowExtension.kt
│   │   ├── null_safety/
│   │   │   ├── CollectionNullSafetyExtension.kt
│   │   │   └── NullSafetyExtension.kt
│   │   ├── number/
│   │   │   ├── NumberFormattingExtension.kt
│   │   │   └── RangeAndBoundaryExtensions.kt
│   │   └── string_validation/
│   │       ├── AdvancedStringValidationExtension.kt
│   │       └── BasicStringValidationExtension.kt
│   ├── CoreActivity.kt                     #   Base Activity
│   ├── CoreFragment.kt                     #   Base Fragment: owns the ComposeView() entry point and the theme
│   └── CoreLayout.kt                       #   Scaffold wrapper: ground colour, insets, tap-to-dismiss-keyboard
├── data/                                   # How data is actually stored and fetched. Android types allowed here.
│   ├── database/
│   │   └── local/
│   │       ├── converter/
│   │       │   └── DateConverter.kt        #     Room TypeConverter: Date <-> Long
│   │       ├── dao/
│   │       │   ├── AlarmDao.kt             #     The alarms table: observe all, get one, upsert, delete
│   │       │   ├── NoteDao.kt              #     The notes table: observe all, observe one day, upsert, delete
│   │       │   ├── PostDao.kt
│   │       │   └── UserActionDao.kt
│   │       ├── entity/
│   │       │   ├── AlarmEntity.kt          #     A stored alarm; its time is two Int columns, not a time type
│   │       │   ├── NoteEntity.kt           #     A stored note; its day is an epoch-day Long, not a date type
│   │       │   ├── PostEntity.kt
│   │       │   └── UserActionEntity.kt
│   │       ├── AppDatabase.kt              #     The Room database; every @Entity is registered here
│   │       └── Migration.kt                #     Hand-written migrations, registered in DatabaseModule
│   ├── datastore/
│   │   └── SettingDatastore.kt             #   Typed Flows over Preferences DataStore
│   ├── mapper/                             #   toDomain() / toEntity() extensions. Repositories never map inline.
│   │   ├── AlarmMapper.kt                  #     Entity <-> domain for alarms, field for field
│   │   ├── NoteMapper.kt                   #     The only place an epoch day becomes a LocalDate
│   │   ├── PostMapper.kt
│   │   └── UserActionMapper.kt
│   ├── notification/
│   │   └── AlarmNotifier.kt                #   Builds and posts the alarm's heads-up notification; owns the channel
│   ├── receiver/
│   │   ├── AlarmReceiver.kt                #   Fires the notification, then re-arms tomorrow's occurrence
│   │   └── BootReceiver.kt                 #   Re-arms every stored alarm after a restart; the one exported receiver
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ApiPath.kt                  #     Every endpoint path lives here
│   │   │   ├── PostApi.kt
│   │   │   └── WeatherApi.kt
│   │   ├── dto/
│   │   │   └── PostDto.kt                  #     Wire format only — never leaves the data layer
│   │   └── util/
│   │       └── safeApiCallFlow.kt          #     Wraps a call as Flow<Outcome<T>>; repositories never throw
│   ├── repository/
│   │   └── impl/                           #   The implementations behind the domain interfaces
│   │       ├── AlarmRepositoryImpl.kt      #     Room-only store; owns the earliest-first order and the blank-message refusal
│   │       ├── NoteRepositoryImpl.kt       #     Room-only store; guarantees the newest-first order itself
│   │       ├── PostRepositoryImpl.kt
│   │       ├── SettingRepositoryImpl.kt
│   │       └── UserActionRepositoryImpl.kt
│   └── scheduler/
│       └── AlarmManagerAlarmScheduler.kt   #   The one real AlarmScheduler; talks to AlarmManager, never tested directly
├── domain/                                 # What the app is about, in plain Kotlin. Android-free by rule.
│   ├── enums/
│   │   └── BottomBarDestination.kt         #   Home, Calendar, Setting, Alarms — declaration order is tab order
│   ├── model/                              #   Models the UI and repositories agree on
│   │   ├── Alarm.kt                        #     An alarm: a message and a time of day
│   │   ├── BlankAlarmMessageException.kt   #     The "an alarm must say something" rule saying no, carried as a value
│   │   ├── CalendarMonth.kt                #     One month laid out as a Sunday-first grid of squares
│   │   ├── FutureDateRefusedException.kt   #     The no-future-dates rule saying no, carried as a value
│   │   ├── Note.kt                         #     A note, plus the displayTitle fallback a row draws
│   │   ├── Post.kt
│   │   └── UserAction.kt
│   ├── repository/                         #   Interfaces only. Implementations live in data/.
│   │   ├── AlarmRepository.kt
│   │   ├── NoteRepository.kt
│   │   ├── PostRepository.kt
│   │   ├── SettingRepository.kt
│   │   └── UserActionRepository.kt
│   └── scheduler/                          #   The seam that makes "when does this go off" and "arm/cancel it" testable
│       ├── AlarmScheduler.kt               #     Interface: schedule, cancel, rearmAll (default: schedule in a loop)
│       └── NextFireTime.kt                 #     Pure arithmetic: next occurrence of hour:minute against a Clock
├── injection/                              # Koin modules. AppModule pulls the rest together.
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── DatastoreModule.kt
│   ├── LocaleModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   ├── SchedulerModule.kt                   #   Binds AlarmScheduler -> AlarmManagerAlarmScheduler, and AlarmNotifier
│   └── ViewModelModule.kt
├── ui/                                     # Everything the user sees.
│   ├── component/                          #   Widgets shared by two or more unrelated screens
│   │   ├── ratebottomsheet/
│   │   │   ├── RateBottomSheet.kt
│   │   │   └── RateOption.kt
│   │   ├── CoreBottomBar.kt                #     Bottom nav + the centre action button
│   │   ├── CoreBottomSheet.kt
│   │   ├── CoreTopBar.kt
│   │   ├── CoreTopBar4.kt
│   │   ├── LifecycleComposable.kt
│   │   └── NoteSummaryRow.kt               #     One note as a card — used by Home and by the Calendar day list
│   ├── fragment/                           #   One folder per screen: Fragment + UiState + ViewModel + component/
│   │   ├── alarm_editor/                   #     Write one alarm: a message, a time, and Save in the top bar
│   │   │   ├── component/
│   │   │   │   └── AlarmEditor.kt          #       The message field and the time control
│   │   │   ├── AlarmEditorFragment.kt      #       Owns argumentsFor(alarmId) — the only place that key name lives
│   │   │   ├── AlarmEditorUiState.kt
│   │   │   └── AlarmEditorViewModel.kt
│   │   ├── alarms/                         #     The fourth tab: every alarm, earliest first, plus the button that adds one
│   │   │   ├── component/
│   │   │   │   ├── AlarmDeleteConfirmSheet.kt #    The step between "delete" and an alarm actually going away
│   │   │   │   ├── AlarmRow.kt             #       One alarm as a card — its time, what it says, its switch, its bin
│   │   │   │   ├── AlarmsEmptyState.kt     #       "No alarms yet", centred in the content area
│   │   │   │   └── AlarmsPermissionNotice.kt #     Banner: tells the two OS-level reasons an armed alarm might not fire
│   │   │   ├── AlarmsFragment.kt
│   │   │   ├── AlarmsUiState.kt
│   │   │   └── AlarmsViewModel.kt
│   │   ├── calendar/                       #     The month page: a grid, today marked, the future switched off
│   │   │   ├── component/
│   │   │   │   ├── CalendarDayCell.kt      #       One square, and every combination of its three markers
│   │   │   │   ├── CalendarDayNotes.kt     #       The picked day's notes, or why there are none
│   │   │   │   ├── CalendarMonthGrid.kt    #       Weekday labels plus the squares, seven to a row
│   │   │   │   └── CalendarMonthHeader.kt  #       Back a month, the month's name, forward a month
│   │   │   ├── model/                      #       Screen-local types, not domain ones
│   │   │   │   └── DayCellState.kt         #         Past / Today / Future / OutsideMonth — the tappable rule
│   │   │   ├── CalendarFragment.kt
│   │   │   ├── CalendarUiState.kt
│   │   │   └── CalendarViewModel.kt
│   │   ├── home/
│   │   │   ├── component/
│   │   │   │   ├── HomeNoteList.kt         #       The scrolling note list and its empty state
│   │   │   │   ├── HomePermissionBottomSheet.kt
│   │   │   │   └── HomeRequestPermission.kt
│   │   │   ├── HomeFragment.kt
│   │   │   ├── HomeUiState.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── note/                           #     The note editor: one new or existing note
│   │   │   ├── component/
│   │   │   │   ├── NoteDeleteConfirmSheet.kt #     The question between "delete" and the note going away
│   │   │   │   └── NoteEditor.kt           #       The scrolling title + body writing surface
│   │   │   ├── model/                      #       Screen-local types, not domain ones
│   │   │   │   └── NoteProblem.kt          #         Why the screen cannot go on: gone, unreadable, or would not delete
│   │   │   ├── NoteFragment.kt             #       Owns the nav arguments; argumentsFor() builds them
│   │   │   ├── NoteUiState.kt
│   │   │   └── NoteViewModel.kt
│   │   └── setting/
│   │       ├── component/
│   │       │   └── SettingItem.kt
│   │       ├── language/
│   │       │   ├── component/
│   │       │   │   └── LanguageItem.kt
│   │       │   └── SettingLanguageFragment.kt
│   │       ├── SettingFragment.kt
│   │       ├── SettingUiState.kt
│   │       └── SettingViewModel.kt
│   ├── modifier/
│   │   └── Shadow.kt                       #   Custom drop-shadow modifier — cards draw their own
│   ├── theme/                              #   The single source of every colour and text style
│   │   ├── Color.kt                        #     All colour tokens, declared once
│   │   ├── Theme.kt                        #     The one fixed dark ColorScheme
│   │   └── Type.kt                         #     customizedTextStyle(...) — used instead of MaterialTheme.typography
│   └── util/                               #   UI-only helpers
│       ├── error/
│       │   └── UiErrorMapper.kt            #     Throwable -> a message a human can read
│       ├── AppUtil.kt
│       ├── LocaleManager.kt
│       ├── LogUtil.kt
│       ├── NavigationUtil.kt               #     safeNavigate — always use it, never raw navigate()
│       ├── NetworkUtil.kt
│       ├── PermissionUtil.kt
│       ├── RateUtil.kt
│       └── SystemBarUtil.kt
├── MainActivity.kt                         # The only Activity; hosts the NavHostFragment
└── MainApplication.kt                      # Application class; starts Koin
```

### How a screen is put together

Every screen is exactly two composable layers, and the split is what keeps it testable:

- **`XxxFragment : CoreFragment()`** — owns the ViewModel, navigation and side effects. Anything
  that needs a `Context`, a `NavController` or a permission lives here.
- **`private fun XxxLayout(uiState, onSomething: () -> Unit = {})`** — pure UI. It renders state and
  forwards events, nothing else. Because every callback has a `= {}` default, it can be previewed
  with a single line.

### Colour rule

Colours come from `MaterialTheme.colorScheme.*`, which is fed by the one dark scheme in `Theme.kt`,
which is built from the tokens in `Color.kt`. New composables do not write `Color(0xFF...)`.

A hardcoded colour is right only by coincidence with whatever background happens to sit under it
today, and it breaks silently when that changes — with the build still green. `Color.kt` is also
why `CoreLayout` reads its background from the theme rather than painting black itself: `CoreLayout`
is what actually covers the screen, so a theme change that stopped short of it would change nothing
a user could see.

---

## Author

Phong-Kaster
