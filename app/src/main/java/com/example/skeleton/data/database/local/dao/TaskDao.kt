package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room access for the `tasks` table.
 *
 * @author Phong-Kaster
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TaskEntity)
}
