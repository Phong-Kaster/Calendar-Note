package com.example.skeleton.injection

import com.example.skeleton.data.notification.GreetingNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Binds the notifications the app posts on its own account — the ones nobody scheduled.
 *
 * A module of its own rather than folded into `schedulerModule`: that module's job is the
 * alarm-scheduling seam, and a greeting has nothing to do with `AlarmManager`. `AlarmNotifier` stays
 * where it is, next to the scheduler it serves.
 *
 * `GreetingNotifier` is a `single` because the promise it makes — one greeting per calendar day — is
 * kept by a `Mutex` **inside** it. Two instances would each hold their own lock, and two foregrounds
 * arriving together could then both decide a greeting was due.
 *
 * `clock` is left at its default on purpose; it is a constructor parameter only so that the decision
 * it delegates to can be tested against a day of the test's choosing.
 *
 * @author Phong-Kaster
 */
val notificationModule = module {

    single {
        GreetingNotifier(
            context = androidContext(),
            settingRepository = get(),
        )
    }
}
