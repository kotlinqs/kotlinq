package io.github.kotlinq.parser

import io.github.kotlinq.expression.QQQ
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LookupsKtTest {


    @Test
    fun mainSuccessScenario() {
        val fileContent = """
            package org.tilitili.tralivali
            
            @WithGarmoshka
            class Antoshka {

                @Kopat
                fun potato(): Boolean {
                    val zadavali = false
                    return getProhodili()
                        .filter { it && zadavali }
                        .sortedBy @Kotlinq(true) { it.id }
                        .any({ listOf(it) } with Expression)
                }

                fun dinner(): Boolean {
                    val otkazhus = visit(object: Visitor {
                       override fun edvaLi(): Boolean {
                          return true
                       }
                    })
                    val poSile = inventory.filter { Lozhka(it) }.size > 0
                    
                    if (poSile && !otkazhus) {
                       logger.debug(this) { "Bratsy" }
                       return true
                    }
                    return false
                }
            
            }
        """.trimIndent()
        val replaced = processFileContent(fileContent)
        assertEquals(
            """
            package org.tilitili.tralivali;import io.github.kotlinq.*;import io.github.kotlinq.expression.node.*;
            
            @WithGarmoshka
            class Antoshka {

                @Kopat
                fun potato(): Boolean {
                    val zadavali = false
                    return getProhodili()
                        .filter (withExpression1({ it && zadavali }, { And(LambdaArgument(0),Val("zadavali", zadavali)) }))
                        .sortedBy @Kotlinq(true) { it.id }
                        .any((withExpression1({ listOf(it) }, { Call(Identifier("listOf"),LambdaArgument(0)) })))
                }

                fun dinner(): Boolean {
                    val otkazhus = visit(object: Visitor {
                       override fun edvaLi(): Boolean {
                          return true
                       }
                    })
                    val poSile = inventory.filter (withExpression1({ Lozhka(it) }, { Call(Ref(Lozhka::class),LambdaArgument(0)) })).size > 0
                    
                    if (poSile && !otkazhus) {
                       logger.debug(this, withExpression0({ "Bratsy" }, { Value(${QQQ}Bratsy${QQQ}) }))
                       return true
                    }
                    return false
                }
            
            }
        """.trimIndent(), replaced
        )
    }

}

