package com.example.skeleton.data.database.local.entity

import androidx.room.ColumnInfo
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
 * **[repeatMode] and [repeatDays] both carry a SQL `DEFAULT`, unlike every other column here.** They
 * were added to an already-shipped table (version 4 → 5), and `MIGRATION_4_5`'s `ALTER TABLE ADD
 * COLUMN` has to give every existing row a value for a column declared `NOT NULL` — SQLite refuses
 * the statement otherwise. `@ColumnInfo(defaultValue = …)` is what makes Room's own *fresh-install*
 * `CREATE TABLE` carry the identical default, so the two ways of arriving at this table produce
 * byte-for-byte the same schema, which is exactly what Room's runtime check compares at launch.
 * `repeatMode` defaults to `DAILY` rather than `ONE_TIME` because that is the repeat behaviour every
 * alarm already had before this column existed — an upgrade must not silently stop repeating alarms
 * that were armed under the old, always-daily code.
 *
 * @param id row id, handed out by Room. Leave it `0` when inserting a new alarm.
 * @param message what the alarm says when it goes off. Never blank on a stored row — the repository
 *   refuses a blank one before it ever reaches here.
 * @param hourOfDay the hour, 0–23.
 * @param minute the minute past the hour, 0–59.
 * @param enabled whether the alarm is armed.
 * @param repeatMode [com.example.skeleton.domain.enums.AlarmRepeatMode.name] — `AlarmMapper` is the
 *   only place that turns it back into the enum.
 * @param repeatDays which weekdays a `CUSTOM` alarm repeats on, packed into one `Int` by
 *   [com.example.skeleton.domain.scheduler.toRepeatDaysBitmask]. Meaningless for any other
 *   [repeatMode], and never read as anything else.
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
    @ColumnInfo(defaultValue = "'DAILY'") val repeatMode: String = "DAILY",
    @ColumnInfo(defaultValue = "0") val repeatDays: Int = 0,
    val createdAt: Long,
)
