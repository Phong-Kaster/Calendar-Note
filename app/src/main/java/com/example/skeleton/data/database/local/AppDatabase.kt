package com.example.skeleton.data.database.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.skeleton.data.database.local.converter.DateConverter
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.database.local.dao.PostDao
import com.example.skeleton.data.database.local.dao.TaskDao
import com.example.skeleton.data.database.local.dao.UserActionDao
import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.data.database.local.entity.PostEntity
import com.example.skeleton.data.database.local.entity.TaskEntity
import com.example.skeleton.data.database.local.entity.UserActionEntity

@Database(
    entities = [
        UserActionEntity::class,
        PostEntity::class,
        TaskEntity::class,
        NoteEntity::class,
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userActionDao(): UserActionDao
    abstract fun postDao(): PostDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
}
