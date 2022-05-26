package io.github.kotlinq.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.io.path.Path

class PackageMatcher(pattern: String) : Predicate<Path?> {

    private val asPath = Path(pattern.removeSuffix(".*").replace(".", "/"))
    private val startsWith = pattern.endsWith(".*")

    override fun test(t: Path?): Boolean {
        return when {
            t == null -> false
            t.endsWith(asPath) -> true
            startsWith -> test(t.parent)
            else -> false
        }
    }
}

class GradlePlugin: KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        super.apply(target)
        target.extensions.add("kotlinq", KotlinqExtension())
        target.dependencies.add("implementation", "com.github.kotlinqs.kotlinq:kotlinq:0.1-SNAPSHOT")
        target.repositories.maven { it.setUrl("https://jitpack.io") }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(KotlinqExtension::class.java)
        return project.provider {
            listOfNotNull(
                extension.packages.ifNotEmpty { SubpluginOption(KotlinqCommandLineProcessor.PACKAGES_OPT, extension.packages.joinToString(";")) },
                SubpluginOption(KotlinqCommandLineProcessor.DEBUG_OPT, extension.debug.toString()),
                SubpluginOption(KotlinqCommandLineProcessor.UPPERCASE_OPT, extension.upperCaseIsClassName.toString()),
                extension.symbolTypes.keys.ifNotEmpty {
                    SubpluginOption(KotlinqCommandLineProcessor.SYMBOLS_OPT, extension.symbolTypes.map { (k, v) -> "$k:$v" }.joinToString(";"))
                }
            )
        }
    }

    override fun getCompilerPluginId(): String {
        return KotlinqCommandLineProcessor.PLUGIN_ID
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact("com.github.kotlinqs.kotlinq", "kotlinq", "0.1-SNAPSHOT")
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

}