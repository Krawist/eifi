plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.hilt)
    alias(libs.plugins.krawist.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.eifi.core.data"
}

dependencies {
    api(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
    api(projects.core.payload)
    implementation(projects.core.ussd)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqlite.bundled.jvm)
}
