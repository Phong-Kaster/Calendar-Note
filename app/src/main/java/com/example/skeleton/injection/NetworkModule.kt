package com.example.skeleton.injection

import com.example.skeleton.core.config.AppConfig
import com.example.skeleton.data.remote.api.PostApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {

    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = AppConfig.API_TIMEOUT_MS
                connectTimeoutMillis = AppConfig.API_TIMEOUT_MS
                socketTimeoutMillis = AppConfig.API_TIMEOUT_MS
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                )
            }
        }
    }

    single { PostApi(get()) }
}
