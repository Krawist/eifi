plugins {
    alias(libs.plugins.eifi.android.feature)
}

android {
    namespace = "com.eifi.feature.pay"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.permission)
}
