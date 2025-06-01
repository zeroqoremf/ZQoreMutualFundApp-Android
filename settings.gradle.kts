// settings.gradle.kts (Project Settings file - at the root of your project)

pluginManagement {
    repositories {
        gradlePluginPortal()
        // REMOVED the 'content' block from google()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ZQoreMutualFundApp"
include(":app")