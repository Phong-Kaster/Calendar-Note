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

/*
 * --- Version 3: the notes table (simple story) ---
 *
 * A migration is the instructions for turning a database that is already on somebody's phone into
 * the shape the new code expects. It is not optional here: `DatabaseModule` builds the database
 * with `fallbackToDestructiveMigration(false)`, so an app that meets a database it does not know
 * how to upgrade **crashes at launch** rather than quietly wiping the user's notes.
 *
 * The statement below has to describe the same table Room would have created from `NoteEntity` on
 * a fresh install — same column names, same SQLite types, same NOT NULL-ness. Room checks exactly
 * that at the first open and refuses to start if the two disagree.
 *
 * It is not written from memory. It is a copy of the statement Room itself generates, which the
 * build leaves in `app/build/generated/ksp/debug/kotlin/.../AppDatabase_Impl.kt` inside
 * `createAllTables`. Copying it is the only way to be sure the migrated database and a freshly
 * installed one end up identical, because nothing in this project's test setup can run SQLite on
 * the JVM to check afterwards.
 *
 * `title` deliberately carries no SQL `DEFAULT`: Room's own statement has none, and a note without
 * a heading is already expressed by the Kotlin type being an empty `String`. Adding a default here
 * would make a migrated table differ from a fresh one for no benefit.
 */

/**
 * Adds the `notes` table when upgrading a database that was created at version 2.
 *
 * @author Phong-Kaster
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` INTEGER NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL)"
        )
    }
}
