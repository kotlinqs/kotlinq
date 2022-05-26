allprojects {
    repositories {
        maven("https://jitpack.io")
        mavenCentral()
        mavenLocal()
    }
}

tasks.create("publishToMavenLocal") {
    dependsOn(
        gradle.includedBuild("kotlinq").task(":publishToMavenLocal")
    )
}