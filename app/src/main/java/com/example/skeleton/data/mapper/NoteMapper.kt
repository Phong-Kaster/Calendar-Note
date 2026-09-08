package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.domain.model.Note

fun NoteEntity.toDomain(): Note = Note(id = id, epochDay = epochDay, title = title, createdAt = createdAt)
fun Note.toEntity(): NoteEntity = NoteEntity(id = id, epochDay = epochDay, title = title, createdAt = createdAt)
