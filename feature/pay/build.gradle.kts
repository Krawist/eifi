plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.eifi.feature.pay"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.nfc)
    implementation(projects.core.payload)
    implementation(projects.core.permission)
    implementation(projects.core.ussd)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
}
