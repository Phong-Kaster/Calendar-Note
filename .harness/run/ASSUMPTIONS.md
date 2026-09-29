# ASSUMPTIONS

> Autonomous mode. Each decision the engine made alone. To overturn: answer its id in `.harness/run/DECISIONS.md` and re-run, or `git revert` the named commit.

---

## A-001 - Declare and request POST_NOTIFICATIONS

- **Tier:** Missing information
- **Iteration:** 1
- **First dependent checkpoint:** b5f678a
- **Revert:** `git revert b5f678a`

### Question
Should the app declare/request `POST_NOTIFICATIONS` (media notifications may be exempt on API 33+)?

### Options Considered
1. Declare + request on first play - consequences: notification visible everywhere; one extra prompt.
2. Declare only / omit - consequences: risk the PRD's "notification controller" is invisible on some builds.

### Taken, and why
Option 1: the PRD's core deliverable is a visible notification controller; a permission the app actually exercises is not dead.

---

## A-002 - Screen set and feature scope of "a real music player"

- **Tier:** Missing information
- **Iteration:** 1
- **First dependent checkpoint:** b5f678a
- **Revert:** `git revert b5f678a`

### Question
What does "real music player" include, and is there a bottom bar / settings tab?

### Options Considered
1. Library + mini player + Now Playing (seek, prev/next, shuffle, repeat), no tabs.
2. Also search, playlists, favourites, sleep timer, resume-last-song.
3. Single screen.

### Taken, and why
Option 1: covers the PRD's "real player + notification controller" with minimal surface; option 2 is scope the PRD never asked for and would need Room/DataStore that the "delete unneeded code" goal argues against. No settings tab (nothing to set).

---

## A-003 - No new libraries beyond Media3; placeholder in-app artwork

- **Tier:** 2 (plan/architecture)
- **Iteration:** 1
- **First dependent checkpoint:** b5f678a
- **Revert:** `git revert b5f678a`

### Question
May we add Coil / coroutines-guava for artwork and controller futures?

### Options Considered
1. Media3 only; artwork via MediaStore album-art URI in the notification, placeholder icon in-app; await `MediaController` via `ListenableFuture.addListener`.
2. Add Coil + coroutines-guava.

### Taken, and why
Option 1: the PRD asks to remove code, not add dependencies; the notification already shows artwork from the URI.

---

## A-004 - Swipe-away behaviour

- **Tier:** Missing information
- **Iteration:** 1
- **First dependent checkpoint:** b5f678a
- **Revert:** `git revert b5f678a`

### Question
When the user removes the app from Recents: keep playing or stop?

### Options Considered
1. Keep playing if currently playing; stop the service if paused (Media3 default guidance).
2. Always stop.
3. Always keep.

### Taken, and why
Option 1: matches user expectation of a music player with a notification controller.

---

## A-005 - English UI, applicationId/namespace unchanged, label "Music Player"

- **Tier:** Missing information
- **Iteration:** 1
- **First dependent checkpoint:** b5f678a
- **Revert:** `git revert b5f678a`

### Question
UI language (PRD is Vietnamese), rename package/applicationId/label?

### Options Considered
1. English strings; keep `applicationId com.example.myapplication` and namespace; set `app_name` = "Music Player".
2. Vietnamese strings.
3. Full package rename.

### Taken, and why
Option 1: the skeleton's default language and convention docs are English; renaming applicationId/namespace touches every file for no PRD benefit; `values-de` is removed as a demo leftover.
