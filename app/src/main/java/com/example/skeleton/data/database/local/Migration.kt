package com.example.skeleton.data.database.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS posts (
                id INTEGER PRIMARY KEY NOT NULL,
                userId INTEGER NOT NULL,
                title TEXT NOT NULL,
                body TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                isDone INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
