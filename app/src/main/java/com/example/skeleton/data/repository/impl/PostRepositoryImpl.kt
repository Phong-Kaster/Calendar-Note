package com.example.skeleton.data.repository.impl

import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.PostDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.data.remote.api.PostApi
import com.example.skeleton.domain.model.Post
import com.example.skeleton.domain.repository.PostRepository
import com.example.skeleton.ui.util.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PostRepositoryImpl(
    private val api: PostApi,
    private val dao: PostDao,
) : PostRepository {
    private val TAG = "PostRepository"

    override fun observePosts(): Flow<List<Post>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshPosts(): Outcome<Unit> {
        return try {
            val posts = api.getPosts().map { it.toDomain() }
            LogUtil.logcat(message = "refresh posts have ${posts.size} posts", tag = TAG)
            val listOfPost = posts.map { it.toEntity() }
            dao.insertAll(listOfPost)
            Outcome.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.logcat(message = "${e.message}", tag = TAG)
            Outcome.Error(e.message ?: "Unknown error", e)
        }
    }
}
