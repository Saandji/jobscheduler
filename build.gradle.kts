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

    // ---- Logging ----
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")

    // ---- Testing ----
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.samshend.jobscheduler.Scheduler")
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

            groupId = "com.samshend"
            artifactId = "jobscheduler"
            version = "0.1.0"

            pom {
                name.set("JobScheduler")
                description.set("Lightweight coroutine-based job scheduler for Kotlin and Java applications.")
                url.set("https://github.com/Saandji/jobscheduler") // link to your repo

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("samshend")
                        name.set("Sam Shendyapin")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Saandji/jobscheduler.git")
                    developerConnection.set("scm:git:ssh://github.com:Saandji/jobscheduler.git")
                    url.set("https://github.com/Saandji/jobscheduler")
                }
            }
        }
    }
}
tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "JobScheduler"
        attributes["Implementation-Version"] = "0.1.0"
    }
}

// Optional but strongly recommended:
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }
}