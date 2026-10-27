plugins {
    alias(libs.plugins.eifi.android.library)
    alias(libs.plugins.eifi.android.compose)
    alias(libs.plugins.eifi.android.hilt)
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
