package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.TaskEntity
import com.example.skeleton.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    isDone = isDone,
    createdAt = createdAt,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    isDone = isDone,
    createdAt = createdAt,
)
