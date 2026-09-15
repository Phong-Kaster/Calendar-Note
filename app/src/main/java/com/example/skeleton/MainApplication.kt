package com.example.skeleton

import android.app.Application
import android.util.Log
import com.example.skeleton.data.notification.AlarmNotifier
import com.example.skeleton.data.notification.GreetingNotifier
import com.example.skeleton.injection.appModule
import com.example.skeleton.injection.databaseModule
import com.example.skeleton.injection.datastoreModule
import com.example.skeleton.injection.repositoryModule
import com.example.skeleton.injection.viewModelModule
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * # [1. Skills For Real Engineers](https://github.com/mattpocock/skills)
 * # [2. 5 Agent Skills I Use Every Day](https://www.aihero.dev/5-agent-skills-i-use-every-day)
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        installUpdatedSecurityProvider()

        val koinApplication = startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }

        // Through Koin's own instance, not a second `AlarmNotifier(this)` — `AlarmReceiver` reaches
        // the one `schedulerModule` registers via `by inject()`, and a second instance built here
        // would only coincidentally be interchangeable with it today; the moment either one starts
        // caching anything, the two would silently stop agreeing.
        //
        // Created here, not only lazily on first alarm: a channel's importance is frozen at creation
        // (see AlarmNotifier's own KDoc), and creating it at launch is what lets the user find it in
        // system settings and choose its sound or vibration before any alarm has ever gone off.
        koinApplication.koin.get<AlarmNotifier>().createChannelIfNeeded()

        // Same reasoning as AlarmNotifier above: created here, through Koin's own instance, so the
        // greeting channel — and the user's choices about it in system settings — exists from first
        // launch, on its own channel the user can silence without also silencing their alarms.
        koinApplication.koin.get<GreetingNotifier>().createChannelIfNeeded()
    }

    /**
     * Pulls an updated TLS provider from Google Play services when available.
     * Reduces certificate validation failures on older devices with stale system trust stores.
     */
    private fun installUpdatedSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.w(TAG, "Google Play services can be updated to improve TLS", e)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.w(TAG, "Google Play services not available; using platform TLS only", e)
        }
    }

    private companion object {
        private const val TAG = "MainApplication"
    }
}