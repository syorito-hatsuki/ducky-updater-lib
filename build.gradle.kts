import com.modrinth.minotaur.TaskModrinthUpload

val modVersion: String by project
val loaderVersion: String by project
val minecraftVersion: String by project
val javaVersion = JavaVersion.VERSION_21

plugins {
    id("fabric-loom")
    id("com.modrinth.minotaur")
}

base {
    val archivesBaseName: String by project
    archivesName.set("$archivesBaseName-$modVersion-$minecraftVersion")
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
 
    val yarnMappings: String by project
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")

    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("ducky-updater-lib")
    versionName.set("Ducky Updater Lib $modVersion")
    versionNumber.set(modVersion)
    versionType.set("release")
    uploadFile.set(tasks.remapJar)
    additionalFiles.add(tasks.remapSourcesJar)
    gameVersions.addAll("1.21.9", "1.21.10", "1.21.11")
    loaders.add("fabric")
    changelog.set(rootProject.file("CHANGELOG.md").readText())
}

tasks {

    named("modrinth").configure {
        @Suppress("UnstableApiUsage") doLast {
            (this@configure as TaskModrinthUpload).uploadInfo?.let {
                "https://modrinth.com/mod/ducky-updater-lib/version/${it.id}".apply {
                    println(this)
                    rootProject.file("build/modrinth_url.txt").writeText(this)
                }
            } ?: return@doLast
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to modVersion))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
