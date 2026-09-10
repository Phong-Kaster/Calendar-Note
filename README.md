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
| Home screen listing every note, most-recently-edited first | 🚧 planned |
| Create a note for today from the bottom bar's centre button | 🚧 planned |
| Open, edit and delete a note (deletion behind a confirmation step) | 🚧 planned |
| Month calendar with today marked and previous/next month navigation | 🚧 planned |
| Tap a day to see that day's notes, and add a note to that day | 🚧 planned |
| Future days are visibly disabled and cannot hold a note | 🚧 planned |

> **Implementation status is deliberately explicit.** The theme, navigation shell, DI, database and
> networking layers are real and building. The note feature itself is still being added, and this
> table is updated in the same change that lands each piece — a README that claims a feature the
> code does not have is worse than no README.

## Business rules

These are the rules the note feature is being built to, not a description of code that exists yet —
the Features table above is the source of truth for what is actually implemented.

- **A note can never be dated in the future.** Today is allowed, tomorrow is not. The rule will be
  enforced in the repository layer, below the UI, so that no caller can walk past it — a screen
  that merely hides the affordance does not satisfy it.
- **Notes are ordered most-recently-touched first** — by `updatedAt` descending, where "touched"
  means created *or* edited.

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
./gradlew :app:assembleDebug        # build the debug APK
./gradlew :app:testDebugUnitTest    # JVM unit tests
./gradlew :app:lintDebug            # Android Gradle Plugin lint
```

`local.properties` must exist with your `sdk.dir` — it is git-ignored, so a fresh clone will not
build until you create it.

> **Lint treats a missing German translation as an error.** Every string added to
> `res/values/strings.xml` needs a matching entry in `res/values-de/strings.xml`, or `lintDebug`
> fails. This is easy to forget and the failure message does not mention the file you actually
> edited.

---

## Package tree

Rooted at `app/src/main/java/com/example/skeleton/`. Subfolders are listed before the `.kt` files
that sit alongside them.

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
│   │       │   ├── PostDao.kt
│   │       │   └── UserActionDao.kt
│   │       ├── entity/
│   │       │   ├── PostEntity.kt
│   │       │   └── UserActionEntity.kt
│   │       ├── AppDatabase.kt              #     The Room database; every @Entity is registered here
│   │       └── Migration.kt                #     Hand-written migrations, registered in DatabaseModule
│   ├── datastore/
│   │   └── SettingDatastore.kt             #   Typed Flows over Preferences DataStore
│   ├── mapper/                             #   toDomain() / toEntity() extensions. Repositories never map inline.
│   │   ├── PostMapper.kt
│   │   └── UserActionMapper.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ApiPath.kt                  #     Every endpoint path lives here
│   │   │   ├── PostApi.kt
│   │   │   └── WeatherApi.kt
│   │   ├── dto/
│   │   │   └── PostDto.kt                  #     Wire format only — never leaves the data layer
│   │   └── util/
│   │       └── safeApiCallFlow.kt          #     Wraps a call as Flow<Outcome<T>>; repositories never throw
│   └── repository/
│       └── impl/                           #   The implementations behind the domain interfaces
│           ├── PostRepositoryImpl.kt
│           ├── SettingRepositoryImpl.kt
│           └── UserActionRepositoryImpl.kt
├── domain/                                 # What the app is about, in plain Kotlin. Android-free by rule.
│   ├── enums/
│   │   └── BottomBarDestination.kt
│   ├── model/                              #   Models the UI and repositories agree on
│   │   ├── Post.kt
│   │   └── UserAction.kt
│   └── repository/                         #   Interfaces only. Implementations live in data/.
│       ├── PostRepository.kt
│       ├── SettingRepository.kt
│       └── UserActionRepository.kt
├── injection/                              # Koin modules. AppModule pulls the rest together.
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── DatastoreModule.kt
│   ├── LocaleModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
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
│   │   └── LifecycleComposable.kt
│   ├── fragment/                           #   One folder per screen: Fragment + UiState + ViewModel + component/
│   │   ├── home/
│   │   │   ├── component/
│   │   │   │   ├── HomePermissionBottomSheet.kt
│   │   │   │   └── HomeRequestPermission.kt
│   │   │   ├── HomeFragment.kt
│   │   │   ├── HomeUiState.kt
│   │   │   └── HomeViewModel.kt
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
