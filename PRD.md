# Product Requirements Document: Calendar Note App

**Status:** Draft — generated from initial description, not yet reviewed
**Purpose:** Input artifact for the "foreman" harness experiment

---

## 1. Overview

A note-taking Android app organized around dates. Users capture notes tied to
the current day, browse them in a reverse-chronological list on the home
screen, or jump to a specific day via a calendar view to see that day's notes.

## 2. Tech Stack (as specified)

- **Language:** Kotlin
- **Navigation:** Android Navigation Component with Fragments (multi-fragment,
  single-Activity architecture)
- **Theme:** Dark mode only — no light theme, no system-theme switching
- **Primary color:** Blue (exact hex/token not specified — needs design input)

> **Assumption flagged, not a confirmed spec:** the description also asks for
> a `LazyColumn` for the home screen list. `LazyColumn` is a Jetpack Compose
> API, while Navigation Fragment is the classic View-based navigation system.
> These aren't mutually exclusive — Compose can be hosted inside a Fragment
> via `ComposeView` — but this PRD does not know which of the following you
> intend, and that choice changes the architecture meaningfully:
> 1. **Hybrid:** Fragments + Navigation Component for screen navigation, with
>    individual screens (like the home list) built in Compose (`ComposeView`
>    hosting a `LazyColumn`).
> 2. **Pure View system:** the term "LazyColumn" was used loosely to mean "a
>    lazily-rendered scrolling list," and the actual implementation should be
>    a `RecyclerView` (the View-system equivalent).
>
> This should be confirmed before implementation starts. The rest of this
> document assumes **Option 1 (hybrid)** since it's the closest literal match
> to both stated requirements, but this is an inference, not something you
> confirmed.

## 3. Screens

### 3.1 Home Screen
- Displays all notes across all days.
- List implementation: `LazyColumn` (see assumption above), hosted inside a
  Fragment reached via the Navigation graph.
- Sort order: latest → oldest (most recently created/edited note first).
- Tapping a note opens the Note screen for editing.
- Needs clarification (not specified in the original description):
  - Is there an explicit "create note" entry point from Home (e.g., a FAB),
    or is note creation only reachable via the Calendar screen?
  - What "latest" is sorted by — creation timestamp or last-edited timestamp
    — is not stated.

### 3.2 Calendar Screen
- Displays a calendar (month view assumed — the description doesn't specify
  week vs. month view).
- Tapping a day shows all notes belonging to that day (in some list/detail
  form — not specified whether this is a bottom sheet, a separate fragment,
  or an inline expansion).
- **Business rule (explicitly stated):** users cannot add a note to a future
  day. This implies:
  - Days after "today" should be visually disabled or otherwise blocked from
    the "add note" action.
  - Today and past days remain selectable and support adding notes.
  - Not specified: what happens if a user taps a future day at all — is it
    non-interactive entirely, or interactive-but-read-only (e.g., can view
    but not add)? Since there's nothing to view on a future day yet, likely
    the former, but this should be confirmed.

### 3.3 Note Screen
- Create/edit a single note.
- Fields, note structure (title? body only? attachments?), and validation
  rules are not specified in the original description and need clarification.
- Not specified: can a note be deleted? Edited after creation? Is a note tied
  to exactly one day, set automatically to "today" or the day it was created
  under from the Calendar screen?

## 4. Data Model (inferred, not confirmed)

This is a best-effort inference from the requirements, not a confirmed spec:

| Field       | Type          | Notes                                   |
|-------------|---------------|------------------------------------------|
| id          | Long / UUID   | Primary key                              |
| date        | LocalDate     | The day the note belongs to              |
| content     | String        | Note body — title field unconfirmed      |
| createdAt   | Timestamp     | For sort order, if "latest" = creation    |
| updatedAt   | Timestamp     | For sort order, if "latest" = last edit   |

Persistence layer (Room, DataStore, plain file storage, etc.) is not
specified and should be decided separately.

## 5. Explicit Business Rules

1. A user cannot create a note for a future date.
2. Notes on the Home screen are sorted latest → oldest.
3. The app is dark-mode only; no light theme should be built or exposed.
4. Primary color is blue (specific shade not defined).

## 6. Non-Goals / Out of Scope (not stated, assumed absent unless added)

- No reminders/notifications mentioned.
- No note categories, tags, or search mentioned.
- No multi-user, sync, or cloud backup mentioned.
- No rich text/attachments mentioned.

## 7. Open Questions Before Implementation

- [ ] Confirm Compose-in-Fragment hybrid vs. pure RecyclerView for the list.
- [ ] Exact blue color token/hex for the primary color.
- [ ] Month vs. week calendar view.
- [ ] How a day's notes are displayed after tapping (sheet, screen, inline).
- [ ] Note fields: title + body, or body only?
- [ ] Sort key for "latest": createdAt vs. updatedAt.
- [ ] Whether notes can be edited/deleted after creation.
- [ ] Persistence choice (Room is the typical default for this app shape, but
      not stated).
- [ ] Minimum SDK / target SDK versions.

---

*This document reflects only what was explicitly described plus clearly
labeled assumptions and open questions. Anything not marked as stated or
inferred above was not part of the original description and should be
treated as undecided.*
