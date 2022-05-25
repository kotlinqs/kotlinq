package io.github.kotlinq.expression.node

data class Options(
    val symbolTypes: Map<String, SymbolType> = ambiguousNames + definitelyConstructors,
    val upperCaseIsClassName: Boolean = true,
    val onlyIfAnnotationPresent: Boolean = false
)

val ambiguousNames = listOf("listOf", "setOf", "mapOf").associateWith { SymbolType.Identifier }
val definitelyConstructors = listOf("Pair").associateWith { SymbolType.Constructor }

enum class SymbolType {
    Identifier, Constructor
}