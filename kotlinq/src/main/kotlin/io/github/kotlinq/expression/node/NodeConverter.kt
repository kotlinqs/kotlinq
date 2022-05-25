package io.github.kotlinq.expression.node

import io.github.kotlinq.parser.*
import io.github.kotlinq.parser.AstInspector.text
import io.github.kotlinq.parser.AstInspector.get
import io.github.kotlinq.parser.AstInspector.all
import io.github.kotlinq.parser.AstInspector.children
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstTerminal
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.flattenTerminal
import kotlinx.ast.common.print
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.generated.KotlinLexer

class NodeConverter {

    fun convert(tree: Ast, options: Options = Options()): Node {
        val statements = tree.all("statement")
        return when {
            statements.isEmpty() -> Error("Lambda is empty")
            statements.size > 1 -> Error("Too complex lambda")
            else -> try {
                val lambdaArgumentNames = tree.all("lambdaParameter").map { it.text }.ifEmpty { listOf("it") }.toSet()
                //statements[0].collapse().print()
                return parseAst(statements[0].collapse()!!, Context(
                    identifiersAsTopLevelVals = true,
                    options = options.copy(
                        symbolTypes = Options().symbolTypes + options.symbolTypes
                    ),
                    lambdaArgumentNames = lambdaArgumentNames
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                return Error(e.message!!)
            }
        }
    }

    data class Context(
        val identifiersAsReferenceOf: String? = null,
        val identifiersAsTopLevelVals: Boolean = false,
        val lambdaArgumentNames: Set<String>,
        val options: Options
    )


    private fun parseAst(node: Ast, context: Context): Node {
        fun parse(node: Ast, newContext: Context = context) = parseAst(node, newContext)

        val error by lazy {
            println("error processing")
            node.print()
            println("---")
            Unknown(node)
        }

        return when {
            node is AlreadyConvertedASTNode -> node.node
            node.description == "lineStringLiteral" && node["lineStringExpression"] != null -> {
                Concat(
                    *node.children
                        .filter { it.description == "lineStringExpression" || it.description == "lineStringContent" }
                        .map { when(it.description) {
                            "lineStringContent" -> {
                                val text = it.text
                                if (text.removePrefix("$") in context.lambdaArgumentNames) {
                                    LambdaArgument(context.lambdaArgumentNames.indexOf(text.removePrefix("$")))
                                } else {
                                    Value(text)
                                }
                            }
                            else -> parse(it[1])
                        } }
                        .toTypedArray()
                )
            }
            node.description == "literalConstant" || node.description == "lineStringLiteral"-> {
                constLiteral(node)?.let { Value(it.value) } ?: error
            }
            node.description == "disjunction" -> {
                Or(parse(node[0]), parse(node[2]))
            }
            node.description == "conjunction" -> {
                And(parse(node[0]), parse(node[2]))
            }
            node.description == "comparison" -> {
                when (KotlinLexer.Rules.valueOf((node[1]["comparisonOperator .terminal"]!!).description)) {
                    KotlinLexer.Rules.LANGLE -> Less(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.RANGLE -> Greater(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.LE -> LessOrEqual(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.GE -> GreaterOrEqual(parse(node[0]), parse(node[2]))
                    else -> error
                }
            }
            node.description == "equality" -> {
                when (KotlinLexer.Rules.valueOf((node[1]["equalityOperator .terminal"]!!).description)) {
                    KotlinLexer.Rules.EQEQ -> Equal(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.EXCL_EQ -> NotEqual(parse(node[0]), parse(node[2]))
                    else -> error
                }
            }
            node.description == "postfixUnaryExpression" && node[1].description == "valueArguments" -> {
                val obj = parse(node[0])

                val arguments = ((node["valueArguments"])!!.children)
                    .filter { it !is AstTerminal }
                    .map { parse(it) }
                    .toTypedArray()

                return Call(obj, *arguments)
            }
            node.description == "postfixUnaryExpression" && node[1].description == "navigationSuffix" -> {
                val obj = parse(node[0])
                var optionsReplacement = context.copy(identifiersAsTopLevelVals = false)
                val objIdentifier = getInternalIdentifierFrom(obj)
                if (objIdentifier != null) {
                    optionsReplacement = optionsReplacement.copy(identifiersAsReferenceOf = "${objIdentifier.insertablePrefix}${objIdentifier.nameWithoutPrefix}")
                }

                val prop = parse(node[1]["simpleIdentifier"]!!, optionsReplacement)
                val firstPart = GetProperty(obj, prop)
                val children = node.children
                if (children.size > 2) {
                    val ref = if (objIdentifier != null && prop is InternalIdentifiedNode) "${objIdentifier.insertablePrefix}${objIdentifier.nameWithoutPrefix}.${prop.nameWithoutPrefix}" else null
                    parse(DefaultAstNode(
                        node.description,
                        listOf(AlreadyConvertedASTNode(firstPart)) + children.drop(2),
                    ), context.copy(identifiersAsTopLevelVals = false, identifiersAsReferenceOf = ref))
                } else {
                    firstPart
                }
            }
            node.description == "multiplicativeExpression" -> {
                val firstPair = when (KotlinLexer.Rules.valueOf((node[1]["multiplicativeOperator .terminal"]!!).description)) {
                    KotlinLexer.Rules.MULT -> Multiply(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.DIV -> Divide(parse(node[0]), parse(node[2]))
                    else -> error
                }
                val children = node.children
                if (children.size > 3) {
                    parse(DefaultAstNode(
                        node.description,
                        listOf(AlreadyConvertedASTNode(firstPair)) + children.drop(3))
                    )
                } else {
                    firstPair
                }
            }
            node.description == "additiveExpression" -> {
                val firstPair = when (KotlinLexer.Rules.valueOf((node[1]["additiveOperator .terminal"]!!).description)) {
                    KotlinLexer.Rules.ADD -> Plus(parse(node[0]), parse(node[2]))
                    KotlinLexer.Rules.SUB -> Minus(parse(node[0]), parse(node[2]))
                    else -> error
                }
                val children = node.children
                if (children.size > 3) {
                    parse(DefaultAstNode(
                        node.description,
                        listOf(AlreadyConvertedASTNode(firstPair)) + children.drop(3))
                    )
                } else {
                    firstPair
                }

            }
            node.description == "simpleIdentifier" -> {
                when  {
                    node.text in context.lambdaArgumentNames -> LambdaArgument(context.lambdaArgumentNames.indexOf(node.text))
                    context.options.symbolTypes[node.text] == SymbolType.Identifier -> Identifier(node.text)
                    context.identifiersAsTopLevelVals -> Val(node.text)
                    context.identifiersAsReferenceOf != null -> Ref("${context.identifiersAsReferenceOf}::${node.text}")
                    else -> Identifier(node.text)
                }

            }
            node.description == "infixOperation" -> {
                //also convert to call
                val methodName = node[1].text
                Call(Identifier(methodName), parse(node[0]), parse(node[2]))
            }
            node.description == "genericCallLikeComparison" -> {
                // it's a bit complex
                // so "valueArguments" contains arguments
                // and node before - last one "simpleIdentifier" is method name

                val methodName = (node[0] ).flattenTerminal().last().text

                val arguments = ((node["valueArguments"])!!.children)
                    .filter { it !is AstTerminal }
                    .map { parse(it) }
                    .toTypedArray()

                val methodNode = when {
                    context.options.upperCaseIsClassName && methodName[0].isUpperCase() -> Ref("$methodName::class")
                    context.options.symbolTypes[methodName] == SymbolType.Constructor -> Ref("$methodName::class")
                    context.options.symbolTypes[methodName] == SymbolType.Identifier -> Identifier(methodName)
                    context.identifiersAsTopLevelVals -> Ref("::$methodName")
                    context.identifiersAsReferenceOf != null -> Ref("${context.identifiersAsReferenceOf}::$methodName")
                    else -> Identifier(methodName)
                }

                val callNode = Call(methodNode, *arguments)
                if ((node[0]).description != "simpleIdentifier") {
                    parse(DefaultAstNode(
                        (node[0]).description,
                        (node[0].children).dropLast(1) + AlreadyConvertedASTNode(callNode)
                    ))
                } else {
                    callNode
                }

            }
            else -> error
        }
    }


    private data class AlreadyConvertedASTNode(val node: Node): Ast {
        override val description: String
            get() = "Already Converted"

    }
}