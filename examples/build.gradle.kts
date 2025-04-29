plugins {
    kotlin("jvm") version "2.1.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ---- Logging ----
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
}

application {
    mainClass.set("com.example.SampleApp")
}