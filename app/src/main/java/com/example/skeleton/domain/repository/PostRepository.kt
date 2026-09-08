package com.example.skeleton.domain.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Repository for posts with offline-first support.
 * - [observePosts]: stream from local cache (Room); use for UI.
 * - [refreshPosts]: fetch from API and save to local cache; call on init or pull-to-refresh.
 */
interface PostRepository {

    /** Observes posts from local database. Emits whenever cache is updated. */
    fun observePosts(): Flow<List<Post>>

    /** Fetches posts from API and saves to local database. Returns [Outcome] for error handling. */
    suspend fun refreshPosts(): Outcome<Unit>
}
