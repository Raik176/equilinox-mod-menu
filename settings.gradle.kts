rootProject.name = "modmenu"

pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.rhm176.de/releases")
            name = "RHM's Maven"
        }
        gradlePluginPortal()
    }
}