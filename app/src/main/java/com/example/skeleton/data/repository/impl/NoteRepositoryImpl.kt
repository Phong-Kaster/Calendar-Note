package com.example.skeleton.data.repository.impl

import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Room-backed [NoteRepository]. Dates cross the domain boundary as [LocalDate]; only this
 * class translates them to/from the `epochDay` [Long] that Room actually stores.
 *
 * @author Phong-Kaster
 */
class NoteRepositoryImpl(
    private val dao: NoteDao,
) : NoteRepository {

    override fun observeByDate(date: LocalDate): Flow<List<Note>> =
        dao.observeByEpochDay(date.toEpochDay()).map { entities -> entities.map { it.toDomain() } }

    override fun observeDatesWithNotesBetween(start: LocalDate, end: LocalDate): Flow<Set<LocalDate>> =
        dao.observeEpochDaysWithNotesBetween(start.toEpochDay(), end.toEpochDay())
            .map { epochDays -> epochDays.map { LocalDate.ofEpochDay(it) }.toSet() }

    override suspend fun addNote(date: LocalDate, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val note = Note(
            id = 0,
            epochDay = date.toEpochDay(),
            title = trimmedTitle,
            createdAt = System.currentTimeMillis(),
        )
        dao.insert(note.toEntity())
    }
}
