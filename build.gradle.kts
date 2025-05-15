plugins {
    id("de.rhm176.silk") version "v1.0.4"
    id("maven-publish")
}

group = project.property("maven_group")!!
version = project.property("mod_version")!!

base {
    archivesName.set(project.property("archives_base_name").toString())
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Silk adds the essential maven repositories to download silk loader and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

// Optionally configure the silk plugin
silk {
    // runDir.set(project.file("game"))
}

dependencies {
    equilinox(files(silk.findEquilinoxGameJar()))

    implementation("com.github.SilkLoader:silk-loader:${project.property("loader_version")}")
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

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name").toString()

            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information
    // on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}