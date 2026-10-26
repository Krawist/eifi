plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
}

// ktlint s'applique à chaque module ; detekt est configuré une seule fois pour tout le dépôt.
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(
        files(
            subprojects.flatMap { listOf("${it.projectDir}/src/main/kotlin", "${it.projectDir}/src/test/kotlin") },
        ),
    )
    parallel = true
}

// detekt 1.23.x embarque son propre compilateur Kotlin 2.0.21 : on empêche Gradle de l'aligner sur la version du projet.
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.0.21")
        }
    }
}
