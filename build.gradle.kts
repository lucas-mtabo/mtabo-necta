plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("maven-publish")
}

group = "com.mtabo.necta"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Jsoup
    implementation("org.jsoup:jsoup:1.18.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

/**
 * Publishing configuration (required for JitPack/Maven usage)
 */
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])

            groupId = project.group.toString()
            artifactId = "mtabo-necta"
            version = project.version.toString()
        }
    }
}