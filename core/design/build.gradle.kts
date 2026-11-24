plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
}

android {
    namespace = "com.eifi.core.design"
}

dependencies {
    implementation(libs.zxing.core)
}
