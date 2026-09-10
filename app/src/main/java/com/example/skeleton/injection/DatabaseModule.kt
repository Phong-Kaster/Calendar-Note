package com.example.skeleton.injection

import androidx.room.Room
import com.example.skeleton.data.database.local.AppDatabase
import com.example.skeleton.data.database.local.MIGRATION_1_2
import com.example.skeleton.data.database.local.MIGRATION_2_3
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Builds the one database instance the whole app shares, and hands out its DAOs.
 *
 * Every migration ever written has to be listed in `addMigrations`. `fallbackToDestructiveMigration(false)`
 * means Room will **not** silently delete the user's data when an upgrade path is missing — it
 * throws instead, which is the loud failure you want while developing and the honest one in
 * production.
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().userActionDao() }
    single { get<AppDatabase>().postDao() }
    single { get<AppDatabase>().noteDao() }
}
