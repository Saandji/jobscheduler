plugins {
    kotlin("jvm") version "2.1.20"
    `maven-publish`
    application
}

group = "com.samshend.jobscheduler"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // ---- Core libraries ----
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.micrometer:micrometer-core:1.13.13")
    implementation("org.slf4j:slf4j-api:2.0.17")

    // ---- Logging backend for tests/run ----
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // ---- Testing ----
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.samshend.jobscheduler.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// ---- Publishing ----
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Job Scheduler Library")
                description.set("A coroutine-based job scheduler for Kotlin & Java")
                url.set("https://github.com/yourorg/jobscheduler")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("you")
                        name.set("Your Name")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/yourorg/jobscheduler.git")
                    developerConnection.set("scm:git:ssh://github.com/yourorg/jobscheduler.git")
                    url.set("https://github.com/yourorg/jobscheduler")
                }
            }
        }
    }
    // add your deployment repositories here when ready
}