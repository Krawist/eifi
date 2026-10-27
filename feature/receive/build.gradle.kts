plugins {
    alias(libs.plugins.eifi.android.feature)
}

android {
    namespace = "com.eifi.feature.receive"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.payload)
    implementation(projects.core.permission)
    implementation(libs.libphonenumber)
}
