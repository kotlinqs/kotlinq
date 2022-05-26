package io.github.kotlinq.parser

import io.github.kotlinq.expression.node.LambdaArgument
import io.github.kotlinq.expression.node.NodeConverter
import io.github.kotlinq.expression.node.Options
import io.github.kotlinq.expression.node.walk
import io.github.kotlinq.parser.AstInspector.all
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser


fun replaceAll(fileContent: String, lambdas: List<ParsedLambda>, astToString: (String, Ast, Boolean) -> String): String {
    return lambdas
        .sortedByDescending { it.insertFrom.index }
        .fold(fileContent) { fc, lambda ->
            var rangeToReplace = lambda.insertFrom.index until lambda.insertTo.index
            val original = fc.substring(rangeToReplace).trim()
            val beforeLambda = fc.substring(maxOf(0, lambda.insertFrom.index - 3), lambda.insertFrom.index).trim()
            val isTrailLambda = beforeLambda.isNotEmpty() && beforeLambda.last() == ')'
            if (isTrailLambda) {
                rangeToReplace = fc.lastIndexOf(")", lambda.insertFrom.index) until lambda.insertTo.index
            }
            val result = astToString(original, lambda.astTree, isTrailLambda)
            fc.replaceRange(rangeToReplace, result)
        }
}

//returns modified file content or null, if nothing is modified
fun processFileContent(fileContent: String, options: Options = Options()): String? {
    val fileContentWithoutStubs = fileContent
        .replace(" with Expression", "")
        .replace(Regex("(package .*)([\n\r])"), "$1;import io.github.kotlinq.*;import io.github.kotlinq.expression.node.*;$2")
    val ast = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.String("", fileContentWithoutStubs))
    val lambdas = searchLambdas(ast, options.onlyIfAnnotationPresent)
    if (lambdas.isEmpty()) return null
    val nodeConverter = NodeConverter()
    return replaceAll(fileContentWithoutStubs, lambdas) { original, node, tailLambda ->
        var argumentsCount = node.all("& > lambdaParameters > lambdaParameter").size
        val parsed = nodeConverter.convert(node, options)
        walk(parsed) {
            if (it is LambdaArgument) {
                argumentsCount = maxOf(argumentsCount, it.number + 1)
            }
        }
        val withMethodName = "withExpression$argumentsCount"
        "${if (tailLambda) ", " else "("}$withMethodName($original, { ${parsed.toCode()} }))"
    }

}