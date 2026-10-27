plugins {
    alias(libs.plugins.eifi.android.library)
    alias(libs.plugins.eifi.android.hilt)
    alias(libs.plugins.eifi.android.room)
}

android {
    namespace = "com.eifi.core.data"
}

dependencies {
    api(libs.androidx.datastore.preferences)

    testImplementation(libs.kotlinx.coroutines.test)
}
