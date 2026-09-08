package com.example.skeleton.data.remote.api

import com.example.skeleton.core.config.AppConfig

/**
 * API endpoint paths.
 * Add new endpoints here when adding new features.
 */
object ApiPath {

    private val baseUrl: String get() = AppConfig.API_BASE_URL
    private val weatherBase get() = AppConfig.WEATHER_BASE_URL

    // Post endpoints
    val posts: String get() = "${baseUrl}posts"
    fun postById(id: Int) = "${baseUrl}posts/$id"

    // Weather API
    val weather get() = "${weatherBase}data/2.5/weather"
}
