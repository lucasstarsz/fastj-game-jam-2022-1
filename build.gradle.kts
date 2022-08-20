plugins {
    java
    application
    id("org.beryx.jlink") version "2.25.0"
}

group = "tech.lucasz"
version = "1.0.0"
description = "Rhythm game made for FastJ Game Jam 2022.1."

application.mainClass.set("tech.fastj.gj.FastJGameJam2022")
application.mainModule.set("fastj.jam.main")

repositories.maven {
    setUrl("https://jitpack.io/")
}
repositories.mavenCentral()

dependencies.implementation("io.github.lucasstarsz.fastj:fastj-library:1.7.0-SNAPSHOT-1")
dependencies.implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
dependencies.implementation("com.google.code.gson:gson:2.9.0")

dependencies.implementation("com.formdev:flatlaf:2.3")

java {
    modularity.inferModulePath.set(true)
}

/* To make Kotlin compile and run properly with Gradle, this adds your Kotlin code to the Java
 * source sets. */
sourceSets.main {
    java.srcDirs("src/main/java")
}

/* The Runtime plugin is used to configure the executables and other distributions for your
 * project. */
jlink {

    options.addAll(
        "--strip-debug",
        "--no-header-files",
        "--no-man-pages",
        "--compress", "1"
    )

    launcher {
        noConsole = false
    }

    forceMerge("slf4j-api", "slf4j-simple", "gson", "flatlaf")

    jpackage {
        /* Use this to define the path of the icons for your project. */
        val iconPath = "project-resources/fastj_icon"
        val currentOs = org.gradle.internal.os.OperatingSystem.current()

        addExtraDependencies("slf4j", "gson")

        installerOptions.addAll(
            listOf(
                "--name", "fastj-game-jam-2022-1",
                "--description", project.description as String,
                "--vendor", project.group as String,
                "--app-version", project.version as String,
                "--license-file", "$rootDir/LICENSE.md",
                "--copyright", "Copyright (c) 2022 Andrew Dey",
                "--vendor", "Andrew Dey"
            )
        )

        when {
            currentOs.isWindows -> {
                installerType = "msi"
                imageOptions = listOf("--icon", "${iconPath}.ico")
                installerOptions.addAll(
                    listOf(
                        "--win-per-user-install",
                        "--win-dir-chooser",
                        "--win-shortcut"
                    )
                )
            }
            currentOs.isLinux -> {
                installerType = "deb"
                imageOptions = listOf("--icon", "${iconPath}.png")
                installerOptions.add("--linux-shortcut")
            }
            currentOs.isMacOsX -> {
                installerType = "pkg"
                imageOptions = listOf("--icon", "${iconPath}.icns")
                installerOptions.addAll(
                    listOf(
                        "--mac-package-name", project.name
                    )
                )
            }
        }
    }
}

tasks.named("jpackageImage") {
    doLast {
        copy {
            from("audio").include("*.*")
            into("$buildDir/jpackage/fastj-game-jam-2022-1/audio")
        }
        copy {
            from("img").include("*.*")
            into("$buildDir/jpackage/fastj-game-jam-2022-1/img")
        }
        copy {
            from("json").include("*.*")
            into("$buildDir/jpackage/fastj-game-jam-2022-1/json")
        }
        delete(fileTree("$buildDir/jpackage/fastj-game-jam-2022-1/runtime") {
            include("release", "bin/api**.dll", "bin/fastj-game-jam-2022-1**", "lib/jrt-fs.jar")
        })
    }
}

