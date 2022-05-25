package io.github.kotlinq

import NodeProvider
import io.github.kotlinq.expression.node.Node

/**
 * Holds both lambda and corresponding expression.
 *
 * Subclasses also implement lambda's interface, so their instances could be passed in methods
 * which receive just lambdas.
 */
open class FunctionWithExpression<Lambda>(
    val lambda: Lambda,
    private val expressionProvider : NodeProvider
) {
    val expression: Node by lazy { expressionProvider() }

    class A0<R>(lambda: () -> R, expression: NodeProvider): () -> R by lambda, FunctionWithExpression<() -> R>(lambda, expression)
    class A1<T1, R>(lambda: (T1) -> R, expression: NodeProvider): (T1) -> R by lambda, FunctionWithExpression<(T1) -> R>(lambda, expression)
    class A2<T1, T2, R>(lambda: (T1, T2) -> R, expression: NodeProvider): (T1, T2) -> R by lambda, FunctionWithExpression<(T1, T2) -> R>(lambda, expression)
    class A3<T1, T2, T3, R>(lambda: (T1, T2, T3) -> R, expression: NodeProvider): (T1, T2, T3) -> R by lambda, FunctionWithExpression<(T1, T2, T3) -> R>(lambda, expression)

}