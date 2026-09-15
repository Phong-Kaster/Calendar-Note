# Daily greeting notification, and a permission clean-up

Two independent pieces of work. Neither depends on the other, and neither should
change how alarms behave.

---

## 1. Greet me once a day, the first time I open the app

The first time I open Calendar Note on any given day, it posts a notification
saying hello — something like "Good morning". Every later open on the same day
posts nothing.

The rule, precisely:

- **Once per calendar day**, using the **device's local date**, not a 24-hour
  timer. Opening at 23:58 and again at 00:02 is two different days, so that is
  two notifications.
- **"Open" means the app is brought to the foreground**, whether it was launched
  cold or resumed from the background. If resuming counts as an open in your
  design, say so in the Definition of Done and keep it consistent.
- The "already greeted today" fact must **survive the app being killed and
  relaunched**. Holding it in memory is not enough: force-stopping the app and
  reopening it on the same day must still post nothing.
- A new day **re-arms** it, with no user action.

### Where it fits

- `SettingDatastore` (`data/datastore/SettingDatastore.kt`) already persists
  preferences with `stringPreferencesKey` / `booleanPreferencesKey`. The stored
  date belongs there unless you find a reason it does not.
- `AlarmNotifier` (`data/notification/AlarmNotifier.kt`) already posts
  notifications for alarms. Reuse what it establishes — channel creation,
  permission handling — rather than building a second, parallel way to notify.
  The greeting is not an alarm, so it may want its own channel; that is your
  call, but say which you chose and why.
- `MainActivity` / `MainApplication` are the entry points.

### What must not happen

- No notification on the second open of the same day. This is the whole point,
  so it needs a test that actually exercises the second open, not just the first.
- Alarms keep working exactly as they do now. The 251 existing unit tests and the
  23 screenshot references stay green.

### What I do not care about

The exact wording, the icon, and whether it is "Hello" or "Good morning". Pick
something sensible and put it in `strings.xml` like everything else — do not
hardcode a literal.

---

## 2. Remove permissions the app does not use

`app/src/main/AndroidManifest.xml` declares eight permissions. Three of them are
dead weight and should go:

| Remove | Why |
|---|---|
| `VIBRATE` | Nothing references it. There is no `Vibrator` usage anywhere in the app. |
| `ACCESS_COARSE_LOCATION` | The app asks for location and never uses it — no `LocationManager`, no fused location client, nothing that reads a position. |
| `ACCESS_FINE_LOCATION` | Same. |

**Keep these. Do not remove them:**

| Keep | Why |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | `injection/NetworkModule.kt` and `ui/util/NetworkUtil.kt` exist and appear to use them. If you find they are genuinely dead too, **say so and leave them alone** — that is a separate decision for me, not part of this run. |
| `POST_NOTIFICATIONS` | Needed by alarms, and by the greeting above. |
| `SCHEDULE_EXACT_ALARM` | Needed by alarms. |
| `RECEIVE_BOOT_COMPLETED` | Needed so alarms survive a reboot. |

Removing the two location permissions also means removing the code that requests
them — `ui/fragment/home/component/HomePermissionBottomSheet.kt` references them.
A manifest with the permission gone and UI still asking for it is worse than
either state on its own. If that bottom sheet also handles a permission we are
keeping, strip only the location part and leave the rest working.

After the change, the app must still build, install and open, and the permission
notice the alarms screen shows must behave exactly as it does today.

---

## Scope

This is a small run on purpose. Two tasks, no new libraries, no new modules, no
architecture changes. If you find yourself proposing more than a handful of
tasks, the decomposition is wrong — say so rather than building it.

I already know the answers to the questions this most obviously raises — which
permissions to drop, and where the date should live — and they are written above
precisely so this run does not stop to ask me.
