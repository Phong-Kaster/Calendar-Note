plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.screenshot)
    kotlin("plugin.serialization")
}

android {
    namespace = "com.example.skeleton"

    // Compose Preview Screenshot Testing renders previews with layoutlib on the JVM. No emulator,
    // no connected device, so it runs anywhere `gradlew` runs.
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // JSONPlaceholder allows HTTP; avoids TLS trust failures on some emulators / inspected networks during dev.
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://jsonplaceholder.typicode.com/\"",
            )
        }
        release {
            isMinifyEnabled = false
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://jsonplaceholder.typicode.com/\"",
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            // Why this is here (simple story): a JVM unit test runs against a stub `android.jar`
            // where every framework method throws "not mocked" instead of doing nothing. So a
            // single `Log.w(...)` on an error path is enough to make that path untestable — the
            // test does not fail on the behaviour being wrong, it crashes on the log line.
            //
            // That is not hypothetical here. `NoteRepositoryImpl.save` logs when it refuses a
            // future-dated note, and DoD criterion 10 requires a unit test proving exactly that
            // refusal. With this flag the framework stubs return their default (0 / false / null)
            // and the code under test runs.
            //
            // The cost, stated plainly: any *other* unmocked framework call in a unit test now
            // returns a default quietly instead of announcing itself. Nothing under test in this
            // project relies on a framework return value — the repositories use `android.util.Log`
            // and nothing else — but a future test that does needs Robolectric, not this flag.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)

    // `runTest` for suspend/Flow unit tests, and an in-memory Room database for DAO tests.
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)

    // Screenshot tests live in src/screenshotTest/ and are recorded/validated by the
    // update|validateDebugScreenshotTest tasks.
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Constraint Layout
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.constraint.layout.compose)

    // Preferences DataStore (SharedPreferences like APIs)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // Material (for Fragment/AppCompat compatibility)
    implementation(libs.google.material)

    // Accompanist Permissions (Compose permission handling, e.g. notification/location)
    implementation(libs.accompanist.permissions)

    // Koin for Dependency Injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.compose)

    // Lottie
    implementation(libs.lottie.compose)

    // Google Play in-app review - https://developer.android.com/guide/playcore/in-app-review/kotlin-java
    implementation(libs.google.play.review)
    implementation(libs.google.play.review.ktx)
    implementation(libs.google.play.services.base)

    // Save data in a local database using Room - https://developer.android.com/training/data-storage/room#setup
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
}