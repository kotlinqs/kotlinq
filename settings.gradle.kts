pluginManagement {
    includeBuild("kotlinq")
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
includeBuild("kotlinq")
include("kotlinq-plugin-test")
