pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "eifi"

include(":app")
include(":feature:onboarding")
include(":feature:pay")
include(":feature:receive")
include(":core:design")
include(":core:data")
include(":core:permission")
include(":core:payload")
include(":core:nfc")
include(":core:ussd")
include(":core:sms")
