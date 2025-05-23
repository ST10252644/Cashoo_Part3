pluginManagement {
    plugins{
        id("org.jetbrains.kotlin.android") version "2.1.0"
    }
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

        maven(url = "https://jitpack.io")

    }
}

rootProject.name = "cashoo"
include(":app")
 //(Cal, 2023), (College, 2025)