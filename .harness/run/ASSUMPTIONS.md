# ASSUMPTIONS

> Autonomous mode only (ENGINE.md §14.1, ADR-027). Every decision the engine made alone, where a
> Collaborative run would have stopped to ask. The Runtime renders this file into `RUN-REPORT.html` at
> the end of the run, so each entry must make sense to someone who has read nothing else.
>
> To overturn one: answer its id in `.harness/run/DECISIONS.md` and re-run. A human answer always
> outranks an assumption — the engine reverts what depended on it. Or run the revert command below
> yourself, on the Loop Branch, after the run has stopped.
>
> All entries below were recorded at bootstrap (iteration 0). No code depends on them yet; the first
> dependent checkpoint is filled in by the iteration after the one that first builds on each.

---

## A-001 - Playback engine: Media3 (ExoPlayer + MediaSessionService)

- **Tier:** 2 (plan/architecture — new external dependency)
- **Iteration:** 0 (bootstrap)
- **First dependent checkpoint:** `e3d77de` (iteration 2, Phase 2)
- **Revert:** `git revert e3d77de` (the Phase 2 checkpoint carries T-003 as one commit; T-004 at the Phase 3 checkpoint builds on it and must be reverted first)

### Question

Which library plays the music and runs the foreground service that the notification controls?

### Options Considered

1. `androidx.media3:media3-exoplayer` + `media3-session` (`MediaSessionService`) — Android's current official
   recommendation; gives a foreground service of type `mediaPlayback`, a media-style notification with
   play/pause/next/previous and lock-screen/Bluetooth controls for free. Consequence: two new dependencies.
2. Framework `MediaPlayer` + hand-written foreground `Service` + `NotificationCompat` with custom actions —
   no new library for playback, but the notification/foreground/media-button plumbing is written and
   maintained by hand (and `MediaStyle` still needs `androidx.media`).

### Taken, and why

Option 1. It is the documented Android approach, needs the least custom lifecycle code, and matches the
skeleton's "follow official recommendations" rule. Version pinned in `gradle/libs.versions.toml` when T-003 lands.

## A-002 - Where the music player lives in the app

- **Tier:** Missing information
- **Iteration:** 0
- **First dependent checkpoint:** `ccf22b3` (iteration 1, Phase 1)
- **Revert:** `git revert ccf22b3` (the Phase 1 checkpoint also carries T-001/T-002 as one commit)

### Question

Does the music list replace the skeleton's Home screen or sit beside it?

### Options Considered

1. New **Music** screen as the start destination and first bottom-bar tab; Home and Setting stay reachable. — consequence: skeleton screens untouched.
2. Rewrite Home into the music list. — consequence: skeleton Home/posts demo removed.

### Taken, and why

Option 1: the PRD asks for a music player, nothing about removing existing screens; option 1 is additive and reversible.

## A-003 - Meaning of "bật tắt music player từ notification"

- **Tier:** Missing information
- **Iteration:** 0
- **First dependent checkpoint:** `e3d77de` (iteration 2, Phase 2)
- **Revert:** `git revert e3d77de` (the Phase 2 checkpoint carries T-003 as one commit; T-004 at the Phase 3 checkpoint builds on it and must be reverted first)

### Question

What does "turn the player on/off from the notification" mean?

### Options Considered

1. Play/pause toggle (plus next/previous) in the media notification; swiping the app away while paused stops the service and removes the notification.
2. Additionally a dedicated "close/stop" button in the notification that kills the service.

### Taken, and why

Option 1 — it is the standard media-notification behaviour and the literal reading of "toggle". A stop
button is easy to add later if wanted.

## A-004 - Queue and next/previous behaviour

- **Tier:** Missing information
- **Iteration:** 0
- **First dependent checkpoint:** `e3d77de` (iteration 2, Phase 2)
- **Revert:** `git revert e3d77de` (the Phase 2 checkpoint carries T-003 as one commit; T-004 at the Phase 3 checkpoint builds on it and must be reverted first)

### Question

What plays after the last song, and what does "previous" do mid-song?

### Options Considered

1. Queue = every song in the list (title order), repeat-all: next on the last song goes to the first, previous on the first goes to the last; previous always goes to the previous song.
2. Stop at the end of the list; previous restarts the current song if more than 3 s in (ExoPlayer default).

### Taken, and why

Option 1 — every control always does something visible, which matches "next/previous song" literally.

## A-005 - Fixed dark theme

- **Tier:** 2 (plan — changes the skeleton theme)
- **Iteration:** 0
- **First dependent checkpoint:** `ccf22b3` (iteration 1, Phase 1)
- **Revert:** `git revert ccf22b3` (the Phase 1 checkpoint also carries T-001/T-002 as one commit)

### Question

`core/CoreLayout.kt` always paints black and default text is white, but `ui/theme/Theme.kt` follows the
system light/dark mode and Android 12 dynamic colours — so in light mode theme-coloured text would be dark on black.

### Options Considered

1. Pin one explicit dark colour scheme (no dynamic colour, every role assigned) so the theme matches the black ground.
2. Make the ground follow the theme (light mode = light background) and fix every hard-coded white in the skeleton.

### Taken, and why

Option 1: smallest change, no skeleton screen breaks, and dark is conventional for a music player.

## A-006 - Pre-existing lint errors

- **Tier:** Missing information
- **Iteration:** 0
- **First dependent checkpoint:** `ccf22b3` (iteration 1, Phase 1)
- **Revert:** `git revert ccf22b3` (the Phase 1 checkpoint also carries T-001/T-002 as one commit)

### Question

`main` already fails `lintDebug` (4 `MissingTranslation` errors). Fix them, or accept a red lint?

### Options Considered

1. Add the 4 missing German strings so lint is green and stays a usable gate.
2. Add a lint baseline — hides future errors of the same kind.

### Taken, and why

Option 1: a baseline would hide exactly the class of error new music strings could introduce.
