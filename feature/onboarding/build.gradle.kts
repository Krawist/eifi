plugins {
    alias(libs.plugins.eifi.android.feature)
}

android {
    namespace = "com.eifi.feature.onboarding"
}

dependencies {
    implementation(projects.core.data)
}
