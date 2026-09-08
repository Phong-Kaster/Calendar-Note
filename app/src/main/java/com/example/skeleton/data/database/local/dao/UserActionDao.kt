package com.example.skeleton.data.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skeleton.data.database.local.entity.UserActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: UserActionEntity)

    @Query("SELECT * FROM user_actions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<UserActionEntity>>

    @Query("DELETE FROM user_actions")
    suspend fun clear()
}
