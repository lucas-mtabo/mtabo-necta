plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.0.0"
    `maven-publish`
}

group = "org.example"
version = "1.0-SNAPSHOT"

//group = "com.github.YOUR_GITHUB_USERNAME"
//version = "1.0.0"


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Jsoup HTML Parser
    implementation("org.jsoup:jsoup:1.18.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
//
//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            from(components["kotlin"])
//        }
//    }
//}