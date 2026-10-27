plugins {
    alias(libs.plugins.eifi.android.library)
    alias(libs.plugins.eifi.android.hilt)
}

android {
    namespace = "com.eifi.core.ussd"
}

dependencies {
    api(projects.core.payload)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
