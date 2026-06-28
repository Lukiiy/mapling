plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "8.3.1"
}

group = "me.lukiiy"
description = "Minigame map/arena library to hold & store data."
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("dev.eav.tomlkt:tomlkt:0.6.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin.jvmToolchain(8)