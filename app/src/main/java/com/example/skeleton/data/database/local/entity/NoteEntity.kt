package com.example.skeleton.data.database.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for one note attached to a calendar date.
 *
 * Indexed on [epochDay] since every read query filters or groups by date.
 *
 * @author Phong-Kaster
 */
@Entity(tableName = "notes", indices = [Index(value = ["epochDay"])])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val title: String,
    val createdAt: Long,
)
