
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.kotlinqs.kotlinq:kotlinq:0.1-SNAPSHOT")
    }
}

plugins {
    kotlin("jvm") version "1.6.21"
}

apply(plugin="com.github.kotlinqs")

kotlinq {
    debug = true
    ignore("T")
    upperCaseIsClassName = false
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("com.github.kotlinqs.kotlinq:kotlinq:0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}