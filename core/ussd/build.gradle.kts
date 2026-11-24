plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.hilt)
}

android {
    namespace = "com.eifi.core.ussd"
}

dependencies {
    api(projects.core.payload)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
