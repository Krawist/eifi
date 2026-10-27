import java.util.Properties

plugins {
    alias(libs.plugins.eifi.android.application)
    alias(libs.plugins.eifi.android.compose)
    alias(libs.plugins.eifi.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing: read from keystore.properties (not versioned). See README.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.eifi"

    defaultConfig {
        applicationId = "com.eifi"
        versionCode = 1
        versionName = "0.1.0"
    }

    // French is the default language (unqualified resources), English the only translation.
    androidResources {
        localeFilters += listOf("fr", "en")
        generateLocaleConfig = true
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Signed with Android Studio's local debug key.
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.core.design)
    implementation(projects.core.data)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.pay)
    implementation(projects.feature.receive)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.kotlinx.coroutines.test)
}
