rootProject.name = "equilinox-mod-template"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            requested.apply {
                if ("$id" == "de.rhm176.silk") {
                    useModule("com.github.SilkLoader:silk-plugin:$version")
                }
            }
        }
    }

    repositories {
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}