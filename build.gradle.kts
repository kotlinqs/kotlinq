import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    `maven-publish`
}

group = "io.github.ko-linq"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create("maven_public", MavenPublication::class) {
            groupId = "io.github.ko-linq"
            artifactId = "kotlinq-core"
            version = "0.1-SNAPSHOT"

            from(components.getByName("java"))
        }
   }
}