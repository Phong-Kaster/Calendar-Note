package com.example.skeleton.injection

import androidx.room.Room
import com.example.skeleton.data.database.local.AppDatabase
import com.example.skeleton.data.database.local.MIGRATION_1_2
import com.example.skeleton.data.database.local.MIGRATION_2_3
import com.example.skeleton.data.database.local.MIGRATION_3_4
import com.example.skeleton.data.database.local.MIGRATION_4_5
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Builds the one database instance the whole app shares, and hands out its DAOs.
 *
 * Every migration ever written has to be listed in `addMigrations`. There is no
 * `fallbackToDestructiveMigration` call, which is Room's default: an upgrade with no matching
 * migration **throws `IllegalStateException` at launch** instead of silently dropping and recreating
 * tables. Loud in development, honest in production — a bad version bump is a crash the first
 * developer to open the app after it hits, not a user's alarms disappearing with no log line.
 *
 * @author Phong-Kaster
 */
val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    single { get<AppDatabase>().userActionDao() }
    single { get<AppDatabase>().postDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().alarmDao() }
}
