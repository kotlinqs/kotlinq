package io.github.kotlinq.parser

import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstInfoPosition

data class ParsedLambda(
    val astTree: Ast,
    val insertFrom: AstInfoPosition,
    val insertTo: AstInfoPosition,
)