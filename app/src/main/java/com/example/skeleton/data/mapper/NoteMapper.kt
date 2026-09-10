package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.domain.model.Note
import java.time.LocalDate

/*
 * --- The only place a stored note becomes a real note (simple story) ---
 *
 * The database stores a note's day as a plain number (an epoch day: how many days since
 * 1970-01-01). The rest of the app wants a `LocalDate`, because that is the type you can ask
 * "which month is this?" and "is this after today?".
 *
 * Both translations live here, together, so they can never drift apart. Nothing else in the app —
 * not the DAO, not the repository, not a screen — is allowed to do this conversion by hand.
 */

/**
 * Turns a stored row into the note the app works with.
 *
 * @author Phong-Kaster
 */
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        date = LocalDate.ofEpochDay(date),
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

/**
 * Turns a note into the row the database stores.
 *
 * @author Phong-Kaster
 */
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        date = date.toEpochDay(),
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
