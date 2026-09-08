package com.example.skeleton.domain.model

/**
 * One note attached to a calendar date.
 *
 * @param id Room primary key; 0 for a note not yet persisted.
 * @param epochDay The note's date as [java.time.LocalDate.toEpochDay] — a plain day count from
 * epoch, so date-range queries (e.g. "every note this month") are simple integer comparisons.
 * @param title What the user wrote.
 * @param createdAt Wall-clock creation time in millis, used to order notes on the same day.
 * @author Phong-Kaster
 */
data class Note(
    val id: Long = 0,
    val epochDay: Long,
    val title: String,
    val createdAt: Long,
)
