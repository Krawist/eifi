plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.hilt)
}

android {
    namespace = "com.eifi.core.nfc"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
