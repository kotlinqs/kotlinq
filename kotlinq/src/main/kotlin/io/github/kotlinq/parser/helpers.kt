package io.github.kotlinq.parser

import io.github.vidbirnyk.Vidbirnyk
import kotlinx.ast.common.ast.*
import kotlinx.ast.common.flattenTerminal
import kotlinx.ast.common.print
import kotlinx.ast.grammar.kotlin.common.KotlinGrammarAstChannels

object AstInspector: Vidbirnyk<Ast>() {

    override fun getName(node: Ast): String {
        return node.description
    }

    override fun getChildrenList(node: Ast): List<Ast> {
        return when(node) {
            is AstNode -> node.children
            else -> emptyList()
        }
    }

    override fun hasClass(node: Ast, name: String): Boolean {
        return when(name) {
            "terminal" -> node is AstTerminal
            else -> false
        }
    }

    override fun <T: Any> getValue(node: Ast): T? {
        return node.flattenTerminal().firstOrNull()?.text as? T
    }


}


fun Ast?.print() {
    this?.print() ?: println("-- empty ast --")
}

fun Ast.collapse(): Ast? {
    return when {
        this is DefaultAstTerminal && this.channel != KotlinGrammarAstChannels.default -> null
        this is AstTerminal -> this
        this is AstNode && this.children.size == 1 && this.children.first() is AstTerminal -> this
        this is AstNode && this.children.size == 1 -> this.children.first().collapse()
        this is AstNode && this.children.size > 1 -> DefaultAstNode(
            description, this.children.mapNotNull { it.collapse() }, this.astAttachmentsOrNull ?: AstAttachments()
        )
        else -> null
    }
}