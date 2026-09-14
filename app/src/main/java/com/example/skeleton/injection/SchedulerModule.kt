package com.example.skeleton.injection

import com.example.skeleton.data.notification.AlarmNotifier
import com.example.skeleton.data.scheduler.AlarmManagerAlarmScheduler
import com.example.skeleton.domain.scheduler.AlarmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Binds the alarm-scheduling seam: [AlarmScheduler] to the one real implementation that talks to
 * `AlarmManager`, and [AlarmNotifier] for `AlarmReceiver` to post through.
 *
 * A module of its own rather than folded into `repositoryModule`: `AlarmScheduler` lives in
 * `domain/scheduler/`, a peer of the repository interfaces and not a dependency of any of them, so
 * wiring it alongside the repositories would blur that boundary.
 *
 * @author Phong-Kaster
 */
val schedulerModule = module {

    single<AlarmScheduler> { AlarmManagerAlarmScheduler(context = androidContext()) }

    single { AlarmNotifier(context = androidContext()) }
}
