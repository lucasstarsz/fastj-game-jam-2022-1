plugins {
    java
    application
    id("org.beryx.jlink") version "2.25.0"
}

group = "tech.lucasz"
version = "1.0-SNAPSHOT"
description = "Game made for FastJ Game Jam 2022.1."

application.mainClass.set("tech.fastj.gj.Test")
application.mainModule.set("fastj.jam.main")

repositories.maven {
    setUrl("https://jitpack.io/")
}
repositories.mavenCentral()

dependencies.implementation("com.github.lucasstarsz:FastJ:98b89d6e23")
dependencies.implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
