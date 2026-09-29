# Definition of Done — music-player-v3

> Derived from `PRD.md` ("rebuild the music player with a notification controller, delete unneeded code; opening the app must show a real music player; source = music already on the device"). Human-owned after approval.

## Status

- [ ] APPROVED — approve via pending `D-001` in `.harness/run/ESCALATION.md` (answer in `.harness/run/DECISIONS.md`). Edit criteria freely first; approval covers each criterion's Verification Class.

Shorthands: `PKG` = `com.example.myapplication`, `ACT` = `com.example.skeleton.MainActivity`, `adb` targets the one attached phone (API 36, has real songs). **Fresh install** = `adb uninstall $PKG`, then `adb install app/build/outputs/apk/debug/app-debug.apk` (no `-g`, so no permission granted).

## Acceptance Criteria

1. [machine] `./gradlew.bat :app:assembleDebug` exits 0.
2. [machine] `./gradlew.bat :app:testDebugUnitTest :app:assembleDebugAndroidTest` exits 0, and the JUnit XML report lists test classes for: MediaStore-row → `Song` mapping, `mm:ss` duration formatting, `LibraryUiState` empty/loaded decision, `PlaybackState` derived values (progress fraction, has-next/previous), repeat-mode cycling. `ExampleUnitTest` alone does not satisfy this.
3. [machine] The merged debug manifest (`apkanalyzer manifest print app/build/outputs/apk/debug/app-debug.apk`) declares: `MainActivity` as LAUNCHER; a `service` with `android:foregroundServiceType="mediaPlayback"`, `android:exported="true"` and intent-filter action `androidx.media3.session.MediaSessionService`; permissions `READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`.
4. [machine] Driven from: fresh install. `am start -W -n $PKG/$ACT`, wait 5 s: `pidof $PKG` non-empty, `logcat -d -b crash` has no `$PKG` entry, and a `uiautomator dump` shows the system permission-controller dialog (package `com.android.permissioncontroller`, resource-id `…:id/permission_allow_button` present) — the app's own grant control alone does not satisfy this.
5. [machine] Driven from: fresh install, permission **denied** in the system dialog (uiautomator taps "Don't allow"). App still alive, no crash entry, (deny tapped by resource-id `…:id/permission_deny_button`, never by button text), and the dump contains the app's control to grant/open settings.
6. [machine] Driven from: fresh install then `adb shell pm grant $PKG android.permission.READ_MEDIA_AUDIO` and `… POST_NOTIFICATIONS` (this is the easy branch — 4, 5, 22–24 cover the ungranted paths), launch. The dump contains the title of at least one song returned by `adb shell content query --uri content://media/external/audio/media --projection title --where "is_music=1"` (device holds ≥ 3 songs).
7. [machine-then-human] Empty state: permission granted and no audio indexed → the library shows a friendly "no songs" message instead of a blank region. Driven from: AVD `astronex_test` (no media), permission granted via `pm grant`, if it can be started; otherwise reported "not driven".
8. [machine] Driven from 6: tap the first song row (uiautomator). `adb shell dumpsys media_session` lists a session for `$PKG` with `state=PlaybackState {state=3` or `state=PLAYING(3)` (regex `state=(PLAYING\()?3`) and metadata title equal to the tapped title.
9. [machine] After 8: `dumpsys notification --noredact` has a notification from `$PKG` with a media session token and ≥ 3 actions (previous, pause, next); `dumpsys activity services $PKG` shows the playback service `isForeground=true` with `types=0x00000002` (mediaPlayback).
10. [machine] After 8: `cmd media_session dispatch pause` → state 2; `dispatch play` → state 3; `dispatch next` → metadata title becomes the next song in the list that was tapped; `dispatch previous` issued right after `next` (position < 3 s) returns to the tapped song.
11. [machine] After 8: `input keyevent KEYCODE_HOME`, wait 10 s: session still state 3 and the extrapolated position (`position + (now − updated) × speed` from `dumpsys media_session`) advanced ≥ 8 s, corroborated by `dumpsys audio` showing the app's playback as started.
12. [machine] After 8, back on the library: the UI dump shows a mini player containing the playing title and a pause control; after `cmd media_session dispatch pause` the same control's content-description changes to the play label (mini player follows the session).
13. [machine] After 12: tap the mini player → Now Playing screen: dump shows title, artist, a slider/seek bar, elapsed and total time; two dumps 3 s apart show different elapsed text; an `input swipe` across the seek bar moves the `dumpsys media_session` position by ≥ 5 s.
14. [machine] `aapt2 dump badging app/build/outputs/apk/debug/app-debug.apk` reports application label `Music Player`; the DoD Removals R1–R8 all hold.
15. [machine-then-human] Notification tap: play a song, press Home, expand the shade (`cmd statusbar expand-notifications`), tap the media notification (uiautomator). Pre-check: `dumpsys activity activities` shows `$ACT` resumed. Expected: the app comes to the foreground with exactly ONE `MainActivity` in its task (`launchMode=singleTop`), showing the library with the mini player (or Now Playing if it was open) — not a second stacked instance, blank or other screen. Human confirms.
16. [machine] `README.md` exists at repo root, states purpose/features/tech stack, and contains a package tree in which every directory under `app/src/main/java/com/example/skeleton/` appears (compare with `find … -type d`).

### Human-perceived criteria

17. [machine-then-human] First launch on the phone from a fresh install: the permission prompt appears, and after allowing it the phone's own songs appear in a scrollable list with readable title, artist and duration (`m:ss`). Pre-checks: 4 and 6.
18. [machine-then-human] Playing: audio is audible; the mini player and the Now Playing screen show artwork (or a placeholder), title, artist; previous / play-pause / next / seek bar / shuffle / repeat controls are all visible, findable, and clearly indicate playing vs paused and shuffle/repeat on/off. Pre-checks: 8, 10, 12, 13.
19. [machine-then-human] Notification shade and lock screen show title, artist and previous/play-pause/next that each work; no duplicate or blank notification. Pre-checks: 9, 10. Drive from: playing, then screen locked.
20. [machine-then-human] Permission denied state: after denying, the explanation is understandable and the button leads to granting (system dialog or app settings). Pre-check: 5.
21. [human-only] Overall: opening the app feels like "a real music player" — no skeleton/demo leftovers (posts, "+" button, settings/language/rate screens), text readable on the background in both light and dark system themes, and the status-bar clock/icons visible in both.
22. [machine] Driven from: fresh install, launch, tap the system Allow button (resource-id `com.android.permissioncontroller:id/permission_allow_button`, via dump bounds + `input tap`): within 5 s, without restarting the app, the dump contains a title from the `content query` of criterion 6.
23. [machine] Driven from: fresh install, deny the system dialog twice (resource-id taps), then tap the app's grant button: the dump shows the Android app-settings screen for `$PKG` (permanently-denied path).
24. [machine] Driven from: fresh install, `pm grant` READ_MEDIA_AUDIO only, tap the first song: the POST_NOTIFICATIONS dialog appears; deny it (resource-id); playback still reaches state 3 (criterion 8 regex) — the prompt never blocks playback. (API ≤ 32 `READ_EXTERNAL_STORAGE` path is not drivable here and is reported "not driven".)

### Driving notes (apply to every adb-driven criterion)

Use `adb -s <serial>` (a phone and the AVD may both be attached; criterion 7 uses `-s emulator-5554`). Before any `uiautomator dump` run `adb shell settings put global animator_duration_scale 0` and restore `1` afterwards (marquee/ticker animations otherwise break idle detection). Tap permission buttons by resource-id, never by English text (the phone may be localized). Record the exact commands and outputs under `.harness/run/evidence/`.

## Removals

Repository class: **template** (`.harness/knowledge/PROJECT.md`). Everything the player does not use goes.

R1. [machine] No `HomeFragment`, `SettingFragment`, `SettingLanguageFragment`, `RateBottomSheet`, `BottomBarDestination`, `CoreBottomBar` in `app/src` or `navigation_graph.xml`; `startDestination` is the library screen (`rg -n "HomeFragment|SettingFragment|SettingLanguageFragment|RateBottomSheet|BottomBarDestination|CoreBottomBar|CoreTopBar4" app/src` is empty).
R2. [machine] The merged manifest (library-contributed entries included; use `tools:node="remove"` for `ACCESS_NETWORK_STATE`/`INTERNET` that Media3 adds if they appear) declares none of: `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `SCHEDULE_EXACT_ALARM`, `INTERNET`, `ACCESS_NETWORK_STATE`, `AppLocalesMetadataHolderService`, `networkSecurityConfig`, `usesCleartextTraffic` (`apkanalyzer manifest print`).
R3. [machine] No source or build reference to removed stacks: `rg -n "io\.ktor|kotlinx\.serialization|com\.airbnb\.lottie|com\.google\.android\.play|com\.google\.android\.gms|androidx\.room|androidx\.datastore|androidx\.constraintlayout|androidx\.navigation\.compose|navigation-compose|buildConfig|ProviderInstaller|API_BASE_URL|jsonplaceholder|ksp|plugin\.serialization" app gradle build.gradle.kts` is empty; `./gradlew.bat :app:dependencies --configuration debugRuntimeClasspath` lists none of ktor, lottie, play review, play-services, room, datastore (transitive `kotlinx-serialization-core` via Navigation and `constraintlayout` via Material are allowed — only DIRECT declarations in `app/build.gradle.kts`/`libs.versions.toml` are forbidden). Also removed as direct dependencies: `navigation-compose` (only `CoreBottomBar` used it), `constraintlayout-compose`, `koin-androidx-compose`/`koin-compose-viewmodel` if nothing uses them, `buildFeatures.buildConfig`. `activity_main.xml` uses `FrameLayout`. Accompanist-permissions, Koin (core/android), Navigation fragment/runtime, Compose, appcompat, material and Media3 remain.
R4. [machine] These paths no longer exist: `data/remote/`, `data/database/`, `data/datastore/`, `data/mapper/PostMapper.kt`, `data/mapper/UserActionMapper.kt`, `domain/model/Post.kt`, `domain/model/UserAction.kt`, `domain/repository/{Post,UserAction,Setting}Repository.kt`, `data/repository/impl/{Post,UserAction,Setting}RepositoryImpl.kt`, `injection/{Database,Datastore,Network,Locale}Module.kt`, `core/config/`, `common/{Language,Constant}.kt`, `domain/enums/`, `ui/util/{RateUtil,AppUtil,LocaleManager,NetworkUtil}.kt`, `ui/util/error/`, `ui/fragment/home/`, `ui/fragment/setting/`, `ui/component/ratebottomsheet/`, `res/raw/`, `res/values-de/`, `res/xml/network_security_config.xml`, `res/drawable/ic_language_*.xml`, `ic_bottom_*.xml`, `ic_star.xml`, `img_star_unfill.webp`, `img_lock_permission.webp`, `res/font/font_sofia_pro_black.otf`, `Theme.Lockscreen` in `themes.xml`, the `libs.versions.toml` entries for removed libraries and `koin-ktor/koin-compose/koin-test`. `isInternetConnected()` is gone from `CoreActivity`/`CoreFragment`.
R5. [machine] Dead-code sweep: every public/internal top-level class/object/function/const and every public member of a Kotlin `object` under `app/src/main/java` is referenced from at least one other file (XML references count, e.g. Fragments in the nav graph, the service in the manifest; `private` and `@Preview` functions are exempt), except the skeleton base allow-list `CoreActivity`, `CoreFragment`, `CoreLayout`, theme files and `common/Outcome.kt` (this removes e.g. `PermissionUtil.isLocationGranted/isPermissionCameraGranted/isPermissionStorageGranted`, `NavigationUtil.canNavigate`, `CoreFragment.enableDarkMode/setupDarkMode/trackEvent/LocalLocale`, unused imports in `MainActivity`, purple/teal colours in `colors.xml`); `./gradlew.bat :app:lintDebug` reports zero `UnusedResources` issues (so no orphan strings, drawables, colours); `coreLibraryDesugaring` is declared only if `java.time` is used.
R6. [machine] The placeholder `ExampleUnitTest`/`ExampleInstrumentedTest` are replaced by real tests or removed (the instrumented test asserts the real applicationId if kept).
R7. [machine] `MainApplication.kt` contains only Koin start-up (no TLS `ProviderInstaller`).
R8. [machine] `res/values/strings.xml` `app_name` is `Music Player`; no string in `strings.xml` refers to posts, weather, language selection, rating/feedback, location or alarms.

Questions: none — this is a template repository.

## Constraints

- `minSdk 24`, `applicationId` (`com.example.myapplication`) and `namespace` (`com.example.skeleton`) unchanged.
- Playback via AndroidX Media3 (`media3-exoplayer` + `media3-session`); no other new library (no Coil). Album artwork in the notification comes from the MediaStore album-art URI; in-app artwork is a placeholder icon.
- Follow `CLAUDE.md` / `.claude/*.md` architecture, naming, documentation and README-tree rules.
- Music source is on-device audio only (MediaStore); no network.
- UI language English; app is dark-themed with a complete colour scheme.

## Verification Evidence Required

| Criterion | Class | Evidence / what to look at | Signed off |
|---|---|---|---|
| 1–6, 8–14, 16, 22–24 | machine | the command in the criterion, output recorded to a file under `.harness/run/evidence/` and quoted in the checkpoint | n/a |
| 7 | machine-then-human | AVD with no media, permission granted: open the app, expect a clear "no songs" message centered in the screen | ☐ |
| 15 | machine-then-human | play a song, press Home, pull down the shade, tap the notification; expect the app to open on the player | ☐ |
| 17 | machine-then-human | uninstall, install, open; allow the prompt; expect your songs in a scrollable list, text clearly legible | ☐ |
| 18 | machine-then-human | tap a song; expect sound, mini player at the bottom with title and pause button; tap it → Now Playing with artwork placeholder, title, artist, seek bar with times, previous/play/next/shuffle/repeat; toggle each, expect a visible on/off change | ☐ |
| 19 | machine-then-human | while playing, pull the shade and lock the phone; expect title, artist, prev/play-pause/next that respond | ☐ |
| 20 | machine-then-human | uninstall, install, open, tap "Don't allow"; expect an explanation and a button that takes you to allow it | ☐ |
| 21 | human-only | open the app cold; expect a music player, no demo leftovers, readable text in light and dark system themes | ☐ |
| R1–R8 | machine | the commands in each removal | n/a |
