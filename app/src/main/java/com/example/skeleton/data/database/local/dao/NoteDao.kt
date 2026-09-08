package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room access for the `notes` table.
 *
 * @author Phong-Kaster
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE epochDay = :epochDay ORDER BY createdAt ASC")
    fun observeByEpochDay(epochDay: Long): Flow<List<NoteEntity>>

    @Query("SELECT DISTINCT epochDay FROM notes WHERE epochDay BETWEEN :start AND :end")
    fun observeEpochDaysWithNotesBetween(start: Long, end: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity)

    @Query("UPDATE notes SET title = :title WHERE id = :id")
    suspend fun update(id: Long, title: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}
