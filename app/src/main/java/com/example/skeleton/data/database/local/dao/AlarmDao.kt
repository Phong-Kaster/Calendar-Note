package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * Every way this app touches the `alarms` table.
 *
 * [observeAll] returns a [Flow] and is **not** `suspend`: Room keeps the flow alive and pushes a
 * fresh list every time the table changes, so a screen collecting it updates itself after an insert
 * with nobody having to ask.
 *
 * **There is deliberately no `ORDER BY` here**, unlike `NoteDao`. The alarm list's order — earliest
 * time of day first — is a promise `AlarmRepositoryImpl` makes and keeps for itself, so a sort in
 * this SQL string would be a second copy of that promise that a future query could silently drop.
 * See the comparator's own comment in the repository for the whole story.
 *
 * @author Phong-Kaster
 */
@Dao
interface AlarmDao {

    /** Every alarm in the app, in whatever order the table hands them over. */
    @Query("SELECT * FROM alarms")
    fun observeAll(): Flow<List<AlarmEntity>>

    /** One alarm by row id, or `null` when there is no such row. */
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AlarmEntity?

    /**
     * Inserts a new alarm, or replaces the existing row when [AlarmEntity.id] already exists.
     *
     * @return the row id of the stored alarm — the freshly generated one for an insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarm: AlarmEntity): Long

    /**
     * Removes one alarm, matched on [AlarmEntity.id].
     *
     * @return how many rows went — `1` normally, and **`0` when there was no such row**. Room is
     *   perfectly happy to delete nothing and say nothing about it, so without this count the
     *   repository above could not tell "removed" from "there was nothing there", and the screen
     *   would announce a deletion that never happened. Same shape, same reason, as `NoteDao.delete`.
     */
    @Delete
    suspend fun delete(alarm: AlarmEntity): Int
}
