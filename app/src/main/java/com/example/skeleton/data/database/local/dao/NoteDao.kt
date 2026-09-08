package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE epochDay = :epochDay ORDER BY createdAt ASC")
    fun observeByEpochDay(epochDay: Long): Flow<List<NoteEntity>>

    @Query("SELECT DISTINCT epochDay FROM notes WHERE epochDay BETWEEN :start AND :end")
    fun observeEpochDaysWithNotesBetween(start: Long, end: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity)
}
