pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SnapPet"

include(":app-ui")
include(":core-game")
include(":module-pets")
include(":module-media")
include(":module-store-local")
include(":module-persistence")
include(":module-utils")
