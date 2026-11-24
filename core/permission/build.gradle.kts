plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
    alias(libs.plugins.krawist.android.hilt)
}

android {
    namespace = "com.eifi.core.permission"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.design)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
