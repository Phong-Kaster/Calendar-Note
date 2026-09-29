package com.example.skeleton

import android.app.Application
import com.example.skeleton.injection.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point. Its only job is to start Koin so every screen can get its
 * repositories and view models.
 *
 * @author Phong-Kaster
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }
}
