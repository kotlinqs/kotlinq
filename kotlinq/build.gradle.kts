import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    `maven-publish`
    `java-gradle-plugin`
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

group = "com.github.kotlinqs.kotlinq"
version = "0.1-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    implementation("com.github.kotlinqs:vidbirnyk:0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")
    implementation("com.github.kotlinx.ast:common:v0.1.0")
    implementation("com.github.kotlinx.ast:grammar-kotlin-parser-antlr-kotlin:v0.1.0")
    implementation(kotlin("gradle-plugin-api"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create("maven_public", MavenPublication::class) {
            groupId = "com.github.kotlinqs.kotlinq"
            artifactId = "kotlinq"
            version = "0.1-SNAPSHOT"

            from(components.getByName("java"))
        }
    }
}

gradlePlugin {
    plugins {
        create("kotlinq") {
            id = "com.github.kotlinqs"
            version = "0.1-SNAPSHOT"
            implementationClass = "io.github.kotlinq.plugin.GradlePlugin"
        }
    }
}