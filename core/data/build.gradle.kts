plugins {
    alias(libs.plugins.eifi.android.library)
    alias(libs.plugins.eifi.android.hilt)
    alias(libs.plugins.eifi.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.eifi.core.data"
}

dependencies {
    api(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
    api(projects.core.payload)

    testImplementation(libs.kotlinx.coroutines.test)
}
