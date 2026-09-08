package com.example.skeleton.domain.model

/**
 * A single to-do item.
 *
 * @param id Unique identifier; 0 means "not yet persisted" (Room auto-generates it on insert).
 * @param title What the user needs to do.
 * @param isDone True once the user marks this task as complete.
 * @param createdAt Epoch millis when this task was created; used to order the list newest-first.
 * @author Phong-Kaster
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val isDone: Boolean = false,
    val createdAt: Long,
)
