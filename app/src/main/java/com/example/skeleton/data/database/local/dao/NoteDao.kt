package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Every way this app touches the `notes` table.
 *
 * Both `observe…` functions return a [Flow] and are **not** `suspend`: Room keeps the flow alive
 * and pushes a fresh list every time the table changes, so a screen collecting one of them updates
 * itself after an insert or a delete with nobody having to ask.
 *
 * Both order by `updatedAt DESC` — most recently touched first — with `id DESC` behind it. That
 * second key is not decoration: two notes saved inside the same millisecond have the same
 * `updatedAt`, and without a tiebreaker SQLite is free to return them in either order, which is
 * the kind of bug that reproduces once a week and never in a test.
 *
 * @author Phong-Kaster
 */
@Dao
interface NoteDao {

    /** Every note in the app, newest first. */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC, id DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    /**
     * The notes belonging to one day, newest first.
     *
     * @param epochDay the day, as `LocalDate.toEpochDay()`.
     */
    @Query("SELECT * FROM notes WHERE date = :epochDay ORDER BY updatedAt DESC, id DESC")
    fun observeByDate(epochDay: Long): Flow<List<NoteEntity>>

    /** One note by row id, or `null` when there is no such row. */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    /**
     * Inserts a new note, or replaces the existing row when [NoteEntity.id] already exists.
     *
     * @return the row id of the stored note — the freshly generated one for an insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    /** Removes one note. */
    @Delete
    suspend fun delete(note: NoteEntity)
}
