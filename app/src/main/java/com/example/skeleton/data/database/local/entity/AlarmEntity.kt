package com.example.skeleton.data.database.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How an alarm is stored in the `alarms` table.
 *
 * The one thing worth explaining is the time: it is **two plain `Int` columns**, not one time type
 * and not a formatted string. Same reasoning as [NoteEntity] keeping a calendar day as a raw
 * epoch-day `Long` — the database can then sort and compare a time of day with ordinary integer
 * comparisons, and the table needs no type converter. `AlarmMapper` is the only place that would
 * ever translate these numbers into anything else.
 *
 * [enabled] is here even though nothing reads it yet. Adding a column to a table that has already
 * shipped costs a second Room migration, and in this app a migration that disagrees with its entity
 * is a launch crash for every existing install (no `fallbackToDestructiveMigration` call). So the
 * column lands once, with the table.
 *
 * @param id row id, handed out by Room. Leave it `0` when inserting a new alarm.
 * @param message what the alarm says when it goes off. Never blank on a stored row — the repository
 *   refuses a blank one before it ever reaches here.
 * @param hourOfDay the hour, 0–23.
 * @param minute the minute past the hour, 0–59.
 * @param enabled whether the alarm is armed.
 * @param createdAt first save, epoch millis.
 * @author Phong-Kaster
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val message: String,
    val hourOfDay: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val createdAt: Long,
)
