package io.github.kotlinq.expression

import io.github.kotlinq.expression.node.Identifier
import io.github.kotlinq.expression.node.Ref
import io.github.kotlinq.expression.node.Val
import io.github.kotlinq.expression.node.walk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClosureTest {

    @ParameterizedTest
    @CsvSource(
        "A,top level const",
        "B,top level prop",
        "C,top level const/prop",
        "D,top level mutable prop",
        "E(),top level fun",
        "F,companion object val",
        "G,companion object const",
        "H,companion object prop",
        "I,companion object mutable prop",
        "J(),companion object fun",
        "K(),static fun",
        "L,companion object instance",
        //TODO: this case does not work, it is impossible to access private function outside
        //"M(),private class function",
        "N,protected class val",
        "O,internal class prop",
        "P(),inline class function",
        "Q(),inner class instance",
        "R,local val",
        "S,local var",
        "T(),identifier without value",
        "U,another class no-named companion object val",
        "V,top-level object val"
    )
    fun checkClosure(key: String, value: String) {
        var resolved: Pair<String?, String?> = null to null
        walk(ClosureExample().getExpression()) {
            if (it is Ref && it.isConstructor && it.returnClass.simpleName!!.split("$").last() + "()" == key) {
                resolved = key to it.callable.call()?.toString()
            }
            if (it is Ref && (it.name == key || "${it.name}()" == key)) {
                resolved = key to it.callable.call()?.toString()
            }
            if (it is Val && (it.name == key)) {
                resolved = key to it.value?.toString()
            }
            if (it is Identifier && (it.name == key || "${it.name}()" == key)) {
                resolved = key to "identifier without value"
            }
        }
        assertNotNull(resolved.first, "Reference ${key} not found!")
        assertEquals(key to value, resolved)
    }

}