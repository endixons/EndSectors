rootProject.name = "EndSectors"

include("common")
include("paper")
include("proxy")


pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }

}
