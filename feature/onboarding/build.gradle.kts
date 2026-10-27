plugins {
    alias(libs.plugins.eifi.android.feature)
}

android {
    namespace = "com.eifi.feature.onboarding"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.nfc)
    implementation(projects.core.permission)
    implementation(projects.core.payload)
}
