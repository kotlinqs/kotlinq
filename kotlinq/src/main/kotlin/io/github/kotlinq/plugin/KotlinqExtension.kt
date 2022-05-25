package io.github.kotlinq.plugin

/**
 * Allows to provide configuration for Kotlinq
 */
class KotlinqExtension {
    val packages = mutableListOf<String>()
    val symbolTypes = mutableMapOf<String, String>()

    /**
     * If true then modified sources are put to temp folder
     */
    var debug: Boolean = false

    /**
     * If true (by default) then if symbol is started from upper letter it is considered to be
     * class name (constructor), otherwise - method/function name
     */
    var upperCaseIsClassName: Boolean = true

    /**
     * Adds package to processing. All classes under provided package (but not subpackages) will be processed
     * by Kotlinq.
     */
    fun `package`(pkg: String) {
        packages += pkg
    }

    /**
     * Marks provided [symbols] to be constructors, not just function calls
     */
    fun constructors(vararg symbols: String) {
        for (s in symbols) symbolTypes[s] = "Constructor"
    }

    /**
     * Does not try to resolve provided [symbols] and insert references to them.
     */
    fun ignore(vararg symbols: String) {
        for (s in symbols) symbolTypes[s] = "Identifier"
    }


}