package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.PostEntity
import com.example.skeleton.data.remote.dto.PostDto
import com.example.skeleton.domain.model.Post

fun PostDto.toDomain(): Post =
    Post(id = id, userId = userId, title = title, body = body)

fun PostEntity.toDomain(): Post =
    Post(id = id, userId = userId, title = title, body = body)

fun Post.toEntity(): PostEntity =
    PostEntity(id = id, userId = userId, title = title, body = body)
