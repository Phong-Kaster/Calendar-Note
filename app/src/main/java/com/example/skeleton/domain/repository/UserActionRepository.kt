package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.UserAction
import kotlinx.coroutines.flow.Flow

interface UserActionRepository {

    fun observeActions(): Flow<List<UserAction>>

    suspend fun saveAction(action: UserAction)

    suspend fun clear()
}

