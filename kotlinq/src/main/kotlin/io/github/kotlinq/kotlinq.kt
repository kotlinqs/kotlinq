package io.github.kotlinq

import NodeProvider
import io.github.kotlinq.expression.node.Node

/**
 * Use this annotation to:
 * - mark classes that must be processed by Kotlinq if no packages are specified in config
 * - mark classes that must NOT be processed by Kotlinq even they belong to packages specified in config
 */
annotation class Kotlinq(val off: Boolean = false)

/**
 * Returns expression associated with lambda (in case Kotlinq is configured properly)
 */
val <Lambda: Function<*>> Lambda.expression: Node get() =
    (this as? FunctionWithExpression<*>)?.expression ?: error("Expression is not defined! Did you forget to apply plugin 'io.github.kotlinq' ? ")

// Helper methods used by code generator

fun <R> withExpression0(lambda: () -> R, expression: NodeProvider) = FunctionWithExpression.A0(lambda, expression)
fun <T1, R> withExpression1(lambda: (T1) -> R, expression: NodeProvider) = FunctionWithExpression.A1(lambda, expression)
fun <T1, T2, R> withExpression2(lambda: (T1, T2) -> R, expression: NodeProvider) = FunctionWithExpression.A2(lambda, expression)
fun <T1, T2, T3, R> withExpression3(lambda: (T1, T2, T3) -> R, expression: NodeProvider) = FunctionWithExpression.A3(lambda, expression)


fun <R> withExpression(lambda: () -> R, expression: NodeProvider) = withExpression0(lambda, expression)
fun <T1, R> withExpression(lambda: (T1) -> R, expression: NodeProvider) = withExpression1(lambda, expression)
fun <T1, T2, R> withExpression(lambda: (T1, T2) -> R, expression: NodeProvider) = withExpression2(lambda, expression)
fun <T1, T2, T3, R> withExpression(lambda: (T1, T2, T3) -> R, expression: NodeProvider) = withExpression3(lambda, expression)


