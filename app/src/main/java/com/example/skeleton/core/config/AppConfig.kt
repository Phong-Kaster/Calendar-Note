package com.example.skeleton.core.config

import com.example.skeleton.BuildConfig

/**
 * Central configuration for the application.
 * Update these values when copying this skeleton to a new project.
 *
 * [API_BASE_URL] comes from [BuildConfig]: **debug** uses HTTP for JSONPlaceholder to avoid
 * `CertPathValidatorException` on emulators or TLS-inspected networks; **release** uses HTTPS.
 */
object AppConfig {

    // API Configuration
    val API_BASE_URL: String = BuildConfig.API_BASE_URL
    const val API_TIMEOUT_MS = 30_000L

    // Third-party APIs
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/"
}
