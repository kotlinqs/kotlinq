package io.github.kotlinq.plugin

import io.github.kotlinq.expression.node.Options
import io.github.kotlinq.expression.node.SymbolType
import io.github.kotlinq.parser.processFileContent
import io.github.kotlinq.plugin.KotlinqCommandLineProcessor.Companion.DEBUG_KEY
import io.github.kotlinq.plugin.KotlinqCommandLineProcessor.Companion.PACKAGES_KEY
import io.github.kotlinq.plugin.KotlinqCommandLineProcessor.Companion.SYMBOLS_KEY
import io.github.kotlinq.plugin.KotlinqCommandLineProcessor.Companion.UPPERCASE_KEY
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.extensions.PreprocessedVirtualFileFactoryExtension
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class KotlinqCommandLineProcessor: CommandLineProcessor {

    companion object {
        const val PACKAGES_OPT = "packages"
        val PACKAGES_KEY = CompilerConfigurationKey<List<PackageMatcher>>(PACKAGES_OPT)

        const val DEBUG_OPT = "debug"
        val DEBUG_KEY = CompilerConfigurationKey<Boolean>(DEBUG_OPT)

        const val SYMBOLS_OPT = "symbols"
        val SYMBOLS_KEY = CompilerConfigurationKey<Map<String, SymbolType>>(SYMBOLS_OPT)

        const val UPPERCASE_OPT = "uppercaseIsClass"
        val UPPERCASE_KEY = CompilerConfigurationKey<Boolean>(UPPERCASE_OPT)

        const val PLUGIN_ID = "com.github.kotlinqs"

        val PACKAGES_CLI_OPTION = CliOption(
            optionName = PACKAGES_OPT,
            valueDescription = "pac.kage1;pac.kage2;pac.kage3.*;",
            description = "packages list separated with ;",
            required = false
        )
        val PACKAGE_DEBUG_OPTION = CliOption(
            optionName = DEBUG_OPT,
            valueDescription = "(true/false)",
            description = "prints processed sources to temp",
            required = false
        )
        val PACKAGE_SYMBOLS_OPTION = CliOption(
            optionName = SYMBOLS_OPT,
            valueDescription = "class1:Identifier;class2:Constructor",
            description = "",
            required = false
        )
        val PACKAGE_UPPERCASE_OPTION = CliOption(
            optionName = UPPERCASE_OPT,
            valueDescription = "(true/false)",
            description = "if symbol started with uppercase is it class or not",
            required = false
        )
    }

    override val pluginId: String get() = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf(PACKAGES_CLI_OPTION, PACKAGE_DEBUG_OPTION, PACKAGE_SYMBOLS_OPTION, PACKAGE_UPPERCASE_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            PACKAGES_OPT -> configuration.put(PACKAGES_KEY, value.split(";").map { PackageMatcher(it) })
            DEBUG_OPT -> configuration.put(DEBUG_KEY, value.toBoolean())
            SYMBOLS_OPT -> configuration.put(SYMBOLS_KEY, value.split(";").map { it.split(":") }.associate { it[0] to SymbolType.valueOf(it[1]) })
            UPPERCASE_OPT -> configuration.put(UPPERCASE_KEY, value.toBoolean())
        }
    }
}

class KotlinqRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        requireNotNull(configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY))
        PreprocessedVirtualFileFactoryExtension.registerExtension(project, KotlinqFileExtension(
            configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)!!,
            configuration.get(PACKAGES_KEY),
            configuration.get(DEBUG_KEY, false),
            configuration.get(SYMBOLS_KEY, emptyMap()),
            configuration.get(UPPERCASE_KEY, true)
        ))
    }

}

class KotlinqFileExtension(
    private val logger: MessageCollector,
    private val packageMatchers: List<PackageMatcher>?,
    private val debug: Boolean,
    private val symbolsMap: Map<String, SymbolType>,
    private val upperCaseAsClass: Boolean
): PreprocessedVirtualFileFactoryExtension {
    var counter = 0
    override fun createPreprocessedFile(file: VirtualFile?): VirtualFile? {
        if (file == null) return null
        val noPackageMatchers = packageMatchers == null || packageMatchers.isEmpty()
        if (packageMatchers != null && packageMatchers.all { !it.test(Path(file.path).parent) }) return null
        counter++
        return object : PatchedFile(file) {
            override fun patchBody(original: String): String {
                return processFileContent(original, Options(symbolsMap, upperCaseAsClass, onlyIfAnnotationPresent = noPackageMatchers))?.also {
                    logger.report(CompilerMessageSeverity.INFO, " [ KOTLINQ ] ${file.path}")
                    if (debug) {
                        logger.report(CompilerMessageSeverity.INFO, "[KOTLINQ] Write debug info to: ${Path("build/temp").toAbsolutePath().toString()}")
                        Path("build/temp/").createDirectories()
                        Path("build/temp/${file.nameWithoutExtension}_$counter.kt").writeText(it)
                    }
                } ?: original
            }
        }
    }

    override fun createPreprocessedLightFile(file: LightVirtualFile?): LightVirtualFile? {
        return file
    }

    override fun isPassThrough(): Boolean {
        return false
    }

}

