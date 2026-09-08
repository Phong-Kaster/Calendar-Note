package com.example.skeleton.domain.model

data class UserAction(
    val id: Long,
    val name: String,
    val createdAt: Long // epoch millis (domain-friendly)
)
