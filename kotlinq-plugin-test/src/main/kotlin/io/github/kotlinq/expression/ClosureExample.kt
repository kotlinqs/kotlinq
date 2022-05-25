package io.github.kotlinq.expression

import io.github.kotlinq.Kotlinq
import io.github.kotlinq.expression
import io.github.kotlinq.expression.node.Node

const val A = "top level const"
val B get() = "top level prop"
private val C = "top level const/prop"
var D = "top level mutable prop"
fun E(): String { return "top level fun"}

@Kotlinq
class ClosureExample {

    fun getExpression(): Node {
        val R = "local val"
        var S = "local var"
        fun T(): String = "local fun"

        return lambdaAsIs({
            "test" in listOf(A, B, C, D, E(), F, G, H, I, J(), K(), L, M(), N, O, P(), Q(), R, S, T(), ClosureExample2.U, ClosureExample3.V)
        }).expression
    }


    companion object L {
        val F = "companion object val"
        const val G = "companion object const"
        val H get() = "companion object prop"
        var I: String get() = "companion object mutable prop"
            set(_) { }
        fun J(): String = "companion object fun"
        @JvmStatic fun K(): String = "static fun"
        override fun toString(): String { return "companion object instance" }
    }

    private fun M() = "private class function"
    protected val N = "protected class val"
    internal val O get() = "internal class prop"
    inline fun P() = "inline class function"
    inner class Q {
        override fun toString(): String = "inner class instance"
    }


    fun <L> lambdaAsIs(l: L) = l



}

class ClosureExample2 {
    companion object {
        val U = "another class no-named companion object val"
    }
}

object ClosureExample3 {
    val V = "top-level object val"
}
