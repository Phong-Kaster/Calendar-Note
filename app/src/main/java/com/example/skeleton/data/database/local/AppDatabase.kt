package com.example.skeleton.data.database.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.skeleton.data.database.local.converter.DateConverter
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.database.local.dao.PostDao
import com.example.skeleton.data.database.local.dao.UserActionDao
import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.data.database.local.entity.PostEntity
import com.example.skeleton.data.database.local.entity.UserActionEntity

/**
 * The app's single local database.
 *
 * Adding an entity here is always five steps, not one: list the entity below, raise [version] by
 * one, write the matching migration in `Migration.kt`, register that migration in
 * `injection/DatabaseModule.kt`, and expose the DAO both here and in that module. Skipping any of
 * them crashes the app at launch for anybody who already had the previous version installed.
 *
 * Version history: 1 = user actions only, 2 = the demo posts table, 3 = the notes table.
 *
 * @author Phong-Kaster
 */
@Database(
    entities = [
        UserActionEntity::class,
        PostEntity::class,
        NoteEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userActionDao(): UserActionDao
    abstract fun postDao(): PostDao
    abstract fun noteDao(): NoteDao
}
