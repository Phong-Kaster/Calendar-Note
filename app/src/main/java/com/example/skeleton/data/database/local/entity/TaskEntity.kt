package com.example.skeleton.data.database.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for one to-do task.
 *
 * @author Phong-Kaster
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean,
    val createdAt: Long,
)
