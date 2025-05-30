rootProject.name = "equilinox-mod-menu"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            requested.apply {
                if ("$id" == "de.rhm176.silk") {
                    useModule("com.github.SilkLoader:silk-plugin:v$version")
                }
            }
        }
    }

    repositories {
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}