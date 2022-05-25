package org.gradle.kotlin.dsl

import io.github.kotlinq.plugin.KotlinqExtension
import org.gradle.api.Project

fun Project.kotlinq(init: (KotlinqExtension).() -> Unit) {
    extensions.configure(KotlinqExtension::class.java) {
        init(it)
    }
}
