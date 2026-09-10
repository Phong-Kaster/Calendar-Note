package com.example.skeleton.data.database.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How a note is stored in the `notes` table.
 *
 * One thing here is worth explaining, because it looks like a mistake and is not: [date] is a
 * `Long`, not a date type. It holds an **epoch day** — the number of days since 1970-01-01, which
 * is what `LocalDate.toEpochDay()` returns. Storing it that way means the database can sort and
 * filter days with plain integer comparisons (`WHERE date = :epochDay`), and it means this table
 * needs no type converter. `NoteMapper` is the only place that translates between the number and
 * the real `LocalDate`.
 *
 * The existing [com.example.skeleton.data.database.local.converter.DateConverter] is deliberately
 * not reused: it converts `java.util.Date`, which is a different type with a different meaning
 * (an instant, not a calendar day).
 *
 * @param id row id, handed out by Room. Leave it `0` when inserting a new note.
 * @param date the note's day as an epoch day.
 * @param title the heading; may be an empty string.
 * @param content the body text; may be an empty string.
 * @param createdAt first save, epoch millis.
 * @param updatedAt latest save, epoch millis. Every list in the app sorts on this.
 * @author Phong-Kaster
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
