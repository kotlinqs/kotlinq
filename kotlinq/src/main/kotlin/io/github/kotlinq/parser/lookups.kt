package io.github.kotlinq.parser

import io.github.kotlinq.Kotlinq
import io.github.kotlinq.parser.AstInspector.all
import io.github.kotlinq.parser.AstInspector.get
import io.github.kotlinq.parser.AstInspector.text
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.astInfoOrNull
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.generated.KotlinLexer

data class Parsed(val value: Any?)

fun constLiteral(ast: Ast): Parsed? {
    val literalConstant = ast["literalConstant"]
    if (literalConstant != null) {
        return Parsed(when (KotlinLexer.Rules.valueOf((literalConstant[".terminal"])!!.description)) {
            KotlinLexer.Rules.IntegerLiteral -> literalConstant.text.toInt()
            KotlinLexer.Rules.LongLiteral -> literalConstant.text.trimEnd('L', 'l') .toLong()
            KotlinLexer.Rules.FloatLiteral -> literalConstant.text.toFloat()
            KotlinLexer.Rules.RealLiteral -> {
                if (literalConstant.text.endsWith("f"))
                    literalConstant.text.toFloat()
                else
                    literalConstant.text.toDouble()
            }
            KotlinLexer.Rules.DoubleLiteral -> literalConstant.text.toDouble()
            KotlinLexer.Rules.BooleanLiteral -> literalConstant.text.toBoolean()
            KotlinLexer.Rules.NullLiteral -> null
            else -> return null
        })
    }
    val lineStringLiteral = ast["lineStringLiteral"]
    if (lineStringLiteral != null) {
        return Parsed(lineStringLiteral["lineStringContent"]!!.text)
    }
    return null
}

fun isOff(ast: Ast?): Boolean {
    if (ast == null) return false
    return ast["annotation simpleIdentifier"]?.text == Kotlinq::class.simpleName
            && ast["annotation valueArgument"]?.let { constLiteral(it) }?.value == true
}

fun isOn(ast: Ast?): Boolean {
    if (ast == null) return false
    return ast["annotation simpleIdentifier"]?.text == Kotlinq::class.simpleName
            && ast["annotation valueArgument"]?.let { constLiteral(it) }?.value != true
}

fun Ast.recursive(selector: String, onlyLeaves: Boolean = true): List<Ast> {
    return this.all("& $selector").flatMap {
        val leaves = it.recursive(selector, onlyLeaves)
        when {
            onlyLeaves && leaves.isEmpty() -> listOf(it)
            onlyLeaves -> leaves
            else -> listOf(it) + leaves
        }
    }
}

fun searchLambdas(ast: Ast, onlyIfAnnotationPresent: Boolean): List<ParsedLambda>  {
    val result = mutableListOf<ParsedLambda>()

    val lambdasOn = if (onlyIfAnnotationPresent)
        ast.all("classDeclaration")
            .filter { isOn(it["& > modifiers annotation"]) }
            .flatMap { it.recursive("lambdaLiteral") } +
                ast.all("topLevelObject > declaration > functionDeclaration")
                    .filter { isOn(it["& > modifiers annotation"]) }
                    .flatMap { it.recursive("lambdaLiteral") }
    else
        ast.recursive("lambdaLiteral")
    if (lambdasOn.isEmpty()) return result

    val lambdasAnnotatedWithKotlinqOff = ast.all("annotatedLambda")
        .filter { isOff(it) }
        .mapNotNull { it["& > lambdaLiteral"] }
        .toSet()
    val lambdasInClassesAnnotatedWithKotlinqOff = ast.all("classDeclaration")
        .filter { isOff(it["& > modifiers annotation"]) }
        .flatMap { it.recursive("lambdaLiteral") }
    val lambdasInTopLevelFnsAnnotatedWithKotlinqOff = ast.all("topLevelObject > declaration > functionDeclaration")
        .filter { isOff(it["& > modifiers annotation"]) }
        .flatMap { it.recursive("lambdaLiteral") }
    val lambdasOff = lambdasAnnotatedWithKotlinqOff +
            lambdasInClassesAnnotatedWithKotlinqOff +
            lambdasInTopLevelFnsAnnotatedWithKotlinqOff
    for (lambdaLiteral in lambdasOn - lambdasOff) {
        result += ParsedLambda(lambdaLiteral, lambdaLiteral.astInfoOrNull!!.start, lambdaLiteral.astInfoOrNull!!.stop)
    }

    return result
}
