plugins {
    alias(libs.plugins.eifi.android.library)
    alias(libs.plugins.eifi.android.hilt)
}

android {
    namespace = "com.eifi.core.nfc"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
