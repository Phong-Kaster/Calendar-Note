package com.example.skeleton.data.remote.api

import com.example.skeleton.data.remote.dto.PostDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API service for Post endpoints.
 * Add new API services in data/remote/api/ when adding features.
 */
class PostApi(
    private val client: HttpClient
) {

    suspend fun getPosts(): List<PostDto> {
        return client
            .get(ApiPath.posts)
            .body()
    }

    suspend fun getPostById(id: Int): PostDto {
        return client
            .get(ApiPath.postById(id))
            .body()
    }
}
