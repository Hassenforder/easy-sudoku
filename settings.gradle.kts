pluginManagement {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven(url = "https://maven.pkg.github.com/hassenforder/easy-android") {
            credentials {
                username = providers.gradleProperty("github-user").get()
                password = providers.gradleProperty("github-pat").get()
            }
        }
        maven(
            url = "https://jitpack.io"
        )
    }
}

rootProject.name = "Easy Sudoku"
include(":app")
