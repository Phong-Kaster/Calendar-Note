package com.example.skeleton.calendar

import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * In-memory [NoteRepository] used by tests — no Room, no Android framework.
 *
 * @author Phong-Kaster
 */
class FakeNoteRepository : NoteRepository {

    private val backing = MutableStateFlow<List<Note>>(emptyList())

    private var nextId = 1L

    override fun observeByDate(date: LocalDate): Flow<List<Note>> =
        backing.asStateFlow().map { notes -> notes.filter { it.epochDay == date.toEpochDay() } }

    override fun observeDatesWithNotesBetween(start: LocalDate, end: LocalDate): Flow<Set<LocalDate>> =
        backing.asStateFlow().map { notes ->
            notes
                .map { it.epochDay }
                .filter { epochDay -> epochDay in start.toEpochDay()..end.toEpochDay() }
                .map { epochDay -> LocalDate.ofEpochDay(epochDay) }
                .toSet()
        }

    override suspend fun addNote(date: LocalDate, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val note = Note(
            id = nextId++,
            epochDay = date.toEpochDay(),
            title = trimmedTitle,
            createdAt = System.currentTimeMillis(),
        )
        backing.value = backing.value + note
    }
}
