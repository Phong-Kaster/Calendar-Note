package com.example.skeleton.injection

import androidx.room.Room
import com.example.skeleton.data.database.local.AppDatabase
import com.example.skeleton.data.database.local.MIGRATION_1_2
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().userActionDao() }
    single { get<AppDatabase>().postDao() }
}