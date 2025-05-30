plugins {
    id("de.rhm176.silk") version "1.2.1"
    id("maven-publish")
    id("com.diffplug.spotless") version "7.0.3"
}

group = project.property("maven_group")!!
version = project.property("mod_version")!!

base {
    archivesName.set(project.property("archives_base_name").toString())
}

dependencies {
    equilinox(files(silk.findEquilinoxGameJar()))

    implementation("com.github.SilkLoader:silk-loader:${project.property("loader_version")}")
}

spotless {
    java {
        importOrder()
        removeUnusedImports()

        palantirJavaFormat("2.66.0")
    }
}

tasks.processResources {
    val expandProps = mapOf(
        "version" to project.version
    )

    filesMatching("fabric.mod.json") {
        expand(expandProps)
    }
    inputs.properties(expandProps)
}

java {
    val javaLanguageVersion = JavaLanguageVersion.of(rootProject.findProperty("javaVersion").toString())
    val javaVersion = JavaVersion.toVersion(javaLanguageVersion.asInt())

    toolchain {
        languageVersion = javaLanguageVersion
    }

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}