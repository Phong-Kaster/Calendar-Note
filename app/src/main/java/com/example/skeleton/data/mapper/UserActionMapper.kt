package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.UserActionEntity
import com.example.skeleton.domain.model.UserAction
import java.util.Date

fun UserActionEntity.toDomain(): UserAction {
    return UserAction(
        id = id,
        name = name,
        createdAt = createdAt.time
    )
}

fun UserAction.toEntity(): UserActionEntity {
    return UserActionEntity(
        id = id,
        name = name,
        createdAt = Date(createdAt)
    )
}
