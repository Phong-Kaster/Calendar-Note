package com.example.skeleton.domain.greeting

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate

/**
 * Greet the user once today — and write today down **only if the greeting actually went out**.
 *
 * This is the whole "say hello once a day" sequence, start to finish, with every Android-shaped piece
 * handed in as a lambda. Three steps and nothing else: ask [greetingDueOn] whether one is due, post
 * it, and remember the day.
 *
 * **Why the recording is conditional, and what went wrong when it was not.** The first version of this
 * feature wrote the day down the moment it had *tried* to post, on the reasoning that a post which
 * went nowhere was somebody else's problem. It is not somebody else's problem, because
 * `NotificationManagerCompat.notify(...)` on Android 13 and up **quietly does nothing at all** when the
 * app has never been granted `POST_NOTIFICATIONS` — no exception, no error code, nothing to notice. On
 * a fresh install the app comes to the front *before* the user has been asked for that permission, so
 * the very first foreground posted into a void, wrote today's date against it, and [greetingDueOn]
 * then answered "already greeted" for the rest of the day. The user granted the permission, opened the
 * app again, and was never greeted on the day they installed it. A human tester found exactly this.
 *
 * So: nothing delivered, nothing written down, and the next foreground that day simply tries again.
 * The cost is a handful of extra attempts on the one day somebody is refusing notifications, and each
 * attempt is a cheap question to the system that shows the user nothing.
 *
 * **What "delivered" means is the caller's business, not this function's.** [postGreeting] answers
 * `true` when the system accepted the greeting for display and `false` when it did not. Deciding which
 * is which needs `NotificationManagerCompat`, which is why it is a lambda and why this file is
 * Android-free: unit tests here run against a stub `android.jar` where every framework call silently
 * answers `0` / `false` / `null`, so a test aimed at the real notification code would go green whatever
 * the code did. Pulled out like this, the sequence that carries the promise is ordinary Kotlin and
 * `GreetOnceADayTest` can drive it. Same shape as `domain/scheduler/NextFireTime.kt` and
 * [greetingDueOn] next door; `GreetingNotifier` is then a thin arm that only has to fill in the
 * lambdas.
 *
 * **Why posting and recording are wrapped in `NonCancellable`.** This runs in the activity's own
 * scope, and that scope is cancelled when the activity stops — which a rotation does. Cancelled
 * *between* the greeting being shown and the day being written down, the app would have greeted the
 * user without remembering it, and the very next foreground would greet them all over again.
 * `NonCancellable` keeps the pair together: either the greeting is shown **and** recorded, or neither
 * happens.
 *
 * Example — what the Android side looks like at the call site:
 *
 * ```kotlin
 * greetOnceADay(
 *     lastGreetedDate = settingRepository.lastGreetedDateFlow.first(),
 *     clock = clock,
 *     postGreeting = { postGreeting() },                                   // true when it was shown
 *     recordGreetedOn = { date -> settingRepository.setLastGreetedDate(date = date) },
 * )
 * ```
 *
 * @param lastGreetedDate the day the user was last greeted, as it was read back out of storage, or
 *   `null` when they have never been greeted (a fresh install) or when what was stored could not be
 *   read.
 * @param clock where "today" comes from. A parameter and never `LocalDate.now()` read inside, because
 *   every promise above is only checkable if a test can decide what day it is.
 * @param postGreeting shows the greeting. Returns `true` when the system accepted it for display and
 *   `false` when it did not — and **only `true` gets the day written down**. It must not throw; a
 *   failure is a `false`.
 * @param recordGreetedOn remembers that the user has now been greeted on the given day, so that
 *   nothing greets them again until the calendar turns over. Called at most once, and never at all
 *   unless [postGreeting] said `true`.
 * @author Phong-Kaster
 */
suspend fun greetOnceADay(
    lastGreetedDate: LocalDate?,
    clock: Clock,
    postGreeting: suspend () -> Boolean,
    recordGreetedOn: suspend (LocalDate) -> Unit,
) {
    // The whole decision lives next door in `GreetingDecision.kt`. `null` means the user has already
    // heard from us today, and there is nothing to do — not a post, not a write, not a log line.
    val greetingDate = greetingDueOn(lastGreetedDate = lastGreetedDate, clock = clock) ?: return

    /* --- Why finishing is not optional once the decision is made (simple story) ---
     *
     * From here on there are two things to do and they only make sense as a pair: show the greeting,
     * then write down the day. Stopping halfway leaves the app having said hello without remembering
     * it, and the next foreground would say hello again. `NonCancellable` means "even if whoever
     * asked for this has walked away — the screen rotated, the activity stopped — finish these two."
     */
    withContext(NonCancellable) {
        val delivered = postGreeting()

        // The guard this whole function exists for. A greeting the system refused to show is a
        // greeting the user never saw, so nothing is written down and the next foreground today gets
        // to try again. Writing the day here regardless is what cost one user their install-day
        // hello — see the story above.
        if (!delivered) return@withContext

        // The date comes from the decision above and is never a fresh reading of the clock. One
        // reading, one answer: a foreground at 23:59:59.999 cannot be decided against one day and
        // recorded against the next.
        recordGreetedOn(greetingDate)
    }
}
