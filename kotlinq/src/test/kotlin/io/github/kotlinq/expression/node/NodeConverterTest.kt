package io.github.kotlinq.expression.node

import kotlinx.ast.common.AstSource
import kotlinx.ast.common.flatten
import kotlinx.ast.grammar.kotlin.common.KotlinGrammarParserType
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NodeConverterTest {

    private val converter = NodeConverter()

    private fun lambdaToAst(str: String) = KotlinGrammarAntlrKotlinParser
        .parse(AstSource.String("", "val _ = $str"), KotlinGrammarParserType.kotlinFile)
        .summary(false)
        .get().last().flatten("lambdaLiteral").first()

    private fun convert(str: String): Node = converter.convert(lambdaToAst(str))

    @Test
    fun constLambda() {
        assertEquals(
            Value("str"),
            convert("""{ "str" } """)
        )
        assertEquals(
            Value(5),
            convert("""{ 5 } """)
        )
        assertEquals(
            Value(null),
            convert("""{ null }""")
        )
    }

    @Test
    fun stringExpressionLambda() {
        assertEquals(
            Concat(
                Value("3 * "),
                LambdaArgument(0),
                Value(" = "),
                Multiply(LambdaArgument(0), Value(3))
            ),
            convert("""{ "3 * ${"$"}it = ${"$"}{it * 3}" }""")
        )
    }

    @Test
    fun constFirstArg_It() {
        assertEquals(
            It,
            convert("""{ it }""")
        )
    }

    @Test
    fun constFirstArg_Explicit() {
        assertEquals(
            It,
            convert("""{ a -> a }""")
        )
    }

    @Test
    fun multipleArgs_Explicit() {
        assertEquals(
            Plus(LambdaArgument(0), LambdaArgument(1)),
            convert("""{ a, b -> a + b }""")
        )
    }

    @Test
    fun getField() {
        assertEquals(
            GetProperty(It, Identifier("date")),
            convert(""" { it.date }""")
        )
    }

    @Test
    fun minus() {
        assertEquals(
            Minus(Value(10), It),
            convert(""" { 10 - it }""")
        )
    }

    @Test
    fun comparison() {
        assertEquals(
            Less(It, Value(5)),
            convert(""" { it < 5 } """)
        )
        assertEquals(
            Greater(It, Value(5.5)),
            convert(""" { it > 5.5 } """)
        )
        assertEquals(
            LessOrEqual(It, Value(44.4f)),
            convert(""" { it <= 44.4f} """)
        )
        assertEquals(
            GreaterOrEqual(It, Value(90000000L)),
            convert(""" { it >= 90000000L} """)
        )
    }

    @Test
    fun equality() {
        assertEquals(
            Equal(It, Value("test")),
            convert(""" { it == "test" }""")
        )
    }

    @Test
    fun methodCall() {
        assertEquals(
            Call(Ref("::sin"), It),
            convert(""" { sin(it) } """)
        )
    }

    @Test
    fun references() {
        assertEquals(
            Multiply(It, Val("modifier")),
            convert(""" { it * modifier }""")
        )
    }

    @Test
    fun simpleCall() {
        assertEquals(
            Call(Ref("::println")),
            convert(""" { println() } """)
        )
        assertEquals(
            "Call(Ref(::println))",
            convert(""" { println() } """).toCode()
        )
    }

    @Test
    fun simpleVal() {
        assertEquals(
            Val("a"),
            convert(""" { a } """)
        )
        assertEquals(
            """Val("a", a)""",
            convert(""" { a } """).toCode()
        )
    }

    @Test
    fun simpleValProp() {
        assertEquals(
            GetProperty(Val("a"), Ref("a::b")),
            convert(""" { a.b } """)
        )
        assertEquals(
            """GetProperty(Val("a", a),Ref(a::b))""",
            convert(""" { a.b } """).toCode()
        )
    }

    @Test
    fun nestedValProp() {
        assertEquals(
            GetProperty(GetProperty(Val("a"), Ref("a::b")), Ref("a.b::c")),
            convert(""" { a.b.c } """)
        )
        assertEquals(
            """GetProperty(GetProperty(Val("a", a),Ref(a::b)),Ref(a.b::c))""",
            convert(""" { a.b.c } """).toCode()
        )

        assertEquals(
            GetProperty(GetProperty(GetProperty(Val("a"), Ref("a::b")), Ref("a.b::c")), Ref("a.b.c::d")),
            convert(""" { a.b.c.d } """)
        )
        assertEquals(
            """GetProperty(GetProperty(GetProperty(Val("a", a),Ref(a::b)),Ref(a.b::c)),Ref(a.b.c::d))""",
            convert(""" { a.b.c.d } """).toCode()
        )
    }

    @Test
    fun simpleLambdaArgProp() {
        assertEquals(
            GetProperty(LambdaArgument(0), Identifier("b")),
            convert(""" { it.b } """)
        )
        assertEquals(
            """GetProperty(LambdaArgument(0),Identifier("b"))""",
            convert(""" { it.b } """).toCode()
        )
    }

    @Test
    fun simpleValMethod() {
        assertEquals(
            Call(GetProperty(Val("a"), Ref("a::b"))),
            convert(""" { a.b() } """)
        )
        assertEquals(
            """Call(GetProperty(Val("a", a),Ref(a::b)))""",
            convert(""" { a.b() } """).toCode()
        )
    }

    @Test
    fun simpleLambdaArgMethod() {
        assertEquals(
            Call(GetProperty(LambdaArgument(0), Identifier("b"))),
            convert(""" { it.b() } """)
        )
        assertEquals(
            """Call(GetProperty(LambdaArgument(0),Identifier("b")))""",
            convert(""" { it.b() } """).toCode()
        )

        assertEquals(
            Call(GetProperty(LambdaArgument(0), Identifier("b")), Value(5)),
            convert(""" { it.b(5) } """)
        )
        assertEquals(
            """Call(GetProperty(LambdaArgument(0),Identifier("b")),Value(5))""",
            convert(""" { it.b(5) } """).toCode()
        )
    }

    @Test
    fun nestedReferencesAndCalls() {
        assertEquals(
            Call(
                Ref("::max"),
                GetProperty(It, Identifier("a")),
                Call(
                    GetProperty(
                        GetProperty(It, Identifier("a")),
                        Identifier("b")
                    )
                ),
                GetProperty(Val("global"), Ref("global::c")),
                Call(GetProperty(GetProperty(Val("global"), Ref("global::c")), Ref("global.c::d"))),
            ),
            convert(""" { max(it.a, it.a.b(), global.c, global.c.d()) } """)
        )
        println(convert(""" { max(it.a, it.a.b(), global.c, global.c.d()) } """).toCode())
    }

    @Test
    fun referencesAndArgs() {
        assertEquals(
            Plus(
                Plus(
                    Plus(
                        LambdaArgument(1),
                        Val("it"),
                    ),
                    LambdaArgument(0),
                ),
                Val("that")
            ),
            convert(""" { a, b -> b + it + a + that }""")
        )
    }

    @Test
    fun and() {
        assertEquals(
            And(
                LambdaArgument(0),
                LambdaArgument(1)
            ),
            convert(""" {a, b -> a && b }""")
        )
    }

    @Test
    fun or() {
        assertEquals(
            Or(
                LambdaArgument(0),
                LambdaArgument(1)
            ),
            convert(""" {a, b -> a || b }""")
        )
    }

    @Test
    fun inList() {
        assertEquals(
            Call(
                Identifier("in"),
                LambdaArgument(0),
                Call(
                    Identifier("listOf"),
                    Val("b")
                )
            ),
            convert(""" { it in listOf(b)} """)
        )
    }


}