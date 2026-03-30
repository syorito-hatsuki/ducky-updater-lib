import com.modrinth.minotaur.TaskModrinthUpload

val archivesBaseName: String by project
val modVersion: String by project
val mavenGroup: String by project

val javaVersion = JavaVersion.VERSION_25

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.minotaur)
}

base {
    archivesName.set(archivesBaseName)
}

group = mavenGroup
version = modVersion

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set(archivesBaseName)
    versionName.set("Ducky Updater Lib $modVersion")
    versionNumber.set(modVersion)
    versionType.set("release")
    uploadFile.set(tasks.jar)
    project.afterEvaluate {
        tasks.findByName("sourcesJar")?.let {
            additionalFiles.add(it)
        }
    }
    gameVersions.addAll(
        "26.1"
    )
    loaders.add("fabric")
    changelog.set(rootProject.file("CHANGELOG.md").readText())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    withSourcesJar()
}

tasks {
    named("modrinth").configure {
        @Suppress("UnstableApiUsage") doLast {
            (this@configure as TaskModrinthUpload).uploadInfo?.let {
                rootProject.file("build/modrinth_url.txt").writeText(
                    "https://modrinth.com/mod/${archivesBaseName}/version/${it.id}".apply(::println)
                )
            } ?: return@doLast
        }
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to modVersion))
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
}
