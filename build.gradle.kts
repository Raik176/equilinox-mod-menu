plugins {
    id("de.rhm176.silk.silk-plugin") version "2.1.1"
    id("maven-publish")
    id("com.diffplug.spotless") version "7.0.3"
}

group = project.property("maven_group")!!
version = project.property("mod_version")!!

base {
    archivesName.set(project.property("archives_base_name").toString())
}

silk {
    silkLoaderCoordinates = "de.rhm176.silk:silk-loader:${project.property("loaderVersion")}"

    generateFabricModJson = true

    fabric {
        id = "modmenu"
        name = "Mod Menu"
        version = project.version.toString()
        description = "Adds a mod menu to view the list of mods you have installed."

        author("Right Hand Man")
        contact = mapOf(
            "homepage" to "https://rhm176.de/",
            "sources" to "https://github.com/Raik176/equilinox-mod-menu",
            "issues" to "https://github.com/Raik176/equilinox-mod-menu/issues",
        )
        license("MIT")
        icon("assets/modmenu/icon.png")

        entrypoints {
            type("main") {
                entry("de.rhm176.modmenu.ModMenu")
            }
            type(silk.fabric.id.get()) {
                entry("de.rhm176.modmenu.compat.ModMenuModMenuCompat")
            }
        }
        mixins.add("modmenu.mixins.json")

        depends = mapOf(
            "fabricloader" to ">=0.16.14",
            "silk-api" to "~1.0.0",
            "equilinox" to "~${project.property("equilinoxVersion")}",
            "java" to ">=${project.property("javaVersion")}"
        )

        customData.put("modmenu", mapOf(
            "links" to mapOf(
                "modmenu.sources" to "https://github.com/Raik176/equilinox-mod-menu",
                "modmenu.discord" to "https://discord.gg/ukeD58mngP"
            )
        ))
    }
}

repositories {
    maven {
        url = uri("https://maven.rhm176.de/releases")
        name = "RHM's Maven (Release)"
    }

    if (System.getenv("CI") == "true") {
        repositories {
            maven {
                name = "RHM's Maven (Private)"
                url = uri("https://maven.rhm176.de/private")

                credentials(PasswordCredentials::class.java) {
                    username = System.getenv("REPOSILITE_USERNAME") ?: project.findProperty("reposiliteUsername") as String?
                    password = System.getenv("REPOSILITE_PASSWORD") ?: project.findProperty("reposilitePassword") as String?
                }

                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

dependencies {
    if (System.getenv("CI") == "true") { // this is probably legally questionable, but at least it's a private repo only accessible using the token
        equilinox("com.equilinox:game:${project.property("equilinoxVersion")}")
    } else {
        equilinox(silk.findEquilinoxGameJar())
    }

    compileOnly("org.jetbrains:annotations:${project.property("annotationsVersion")}")

    implementation("de.rhm176.silk.silk-api:silk-api:${project.property("silkApiVersion")}")
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
    val javaVersion = JavaVersion.toVersion(rootProject.findProperty("javaVersion").toString())

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = silk.fabric.name
                description = silk.fabric.description
                url = silk.fabric.contact.map { it["sources"] }

                licenses {
                    license {
                        name = silk.fabric.licenses.map { it[0] }
                        url = silk.fabric.contact.map { it["sources"]!!.removeSuffix("/") + "/blob/main/LICENSE" }
                    }
                }

                developers {
                    // this .get should be fine
                    silk.fabric.authors.get().forEach { authorPerson ->
                        developer {
                            this.name = authorPerson.name
                        }
                    }
                }

                scm {
                    connection = silk.fabric.contact.map { "scm:git:${it["sources"]}.git" }
                    val repoPath = silk.fabric.contact.map { it["sources"]!!.substringAfter("https://github.com/")}
                    developerConnection = repoPath.map { "scm:git:git@github.com:$it.git" }
                    this.url = silk.fabric.contact.map { it["sources"] }
                }
            }
        }
    }

    repositories {
        val reposiliteBaseUrl = System.getenv("REPOSILITE_URL")
            ?: project.findProperty("reposiliteUrl") as String?

        if (!reposiliteBaseUrl.isNullOrBlank()) {
            maven {
                name = "Reposilite"

                val repoPath = if (project.version.toString().endsWith("-SNAPSHOT")) {
                    "/snapshots"
                } else {
                    "/releases"
                }
                url = uri("$reposiliteBaseUrl$repoPath")

                credentials(PasswordCredentials::class.java) {
                    username = System.getenv("REPOSILITE_USERNAME") ?: project.findProperty("reposiliteUsername") as String?
                    password = System.getenv("REPOSILITE_PASSWORD") ?: project.findProperty("reposilitePassword") as String?
                }

                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}