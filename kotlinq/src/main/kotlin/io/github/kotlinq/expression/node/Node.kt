package io.github.kotlinq.expression.node

import io.github.kotlinq.expression.SerializableToCode
import io.github.kotlinq.expression.toCode
import io.github.kotlinq.parser.AstInspector.children
import kotlinx.ast.common.ast.Ast
import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

sealed interface Node : SerializableToCode {
    override val importString: String
        get() = "import io.github.kotlinq.expression.node.*"
}

data class Unknown(val unknown: Ast) : Node {

    override fun toCode(): String {
        return """Unknown(${unknown.description})"""
    }

    private fun Ast.toCode(): String {
        return """DefaultAstNode("$description", ${(this.children).toCode()})"""
    }

    private fun List<Ast>.toCode(): String {
        return "listOf(${joinToString(", ") { it.toCode() }})"
    }

}

sealed class WithChildren(open val children: List<Node>) : Node {
    constructor(vararg children: Node) : this(children.toList())

    override fun toCode(): String {
        return """${this::class.simpleName}(${children.joinToString(",") { it.toCode() }})"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WithChildren) return false
        if (other::class != this::class) return false

        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        return children.hashCode()
    }

    override fun toString(): String {
        return "${this::class.simpleName}($children)"
    }

}
sealed class OneChildren(val child: Node) : WithChildren(listOf(child)) {
}

sealed class TwoChildren(val left: Node, val right: Node) : WithChildren(listOf(left, right))

data class Identifier(override val name: String) : Node, IdentifiedNode {
    override fun toCode(): String {
        return """Identifier("$name")"""
    }
}

data class Value(override val value: Any?) : Node, ValueNode {
    override fun toCode(): String {
        return """Value(${value.toCode()})"""
    }

}

data class LambdaArgument(val number: Int) : Node {
    override fun toCode(): String {
        return """LambdaArgument($number)"""
    }
}

val It = LambdaArgument(0)


class UnaryMinus(child: Node) : OneChildren(child)
class UnaryPlus(child: Node) : OneChildren(child)
class UnaryBang(child: Node) : OneChildren(child)

class Plus(left: Node, right: Node) : TwoChildren(left, right)
class Minus(left: Node, right: Node) : TwoChildren(left, right)
class Multiply(left: Node, right: Node) : TwoChildren(left, right)
class Divide(left: Node, right: Node) : TwoChildren(left, right)

class And(left: Node, right: Node) : TwoChildren(left, right)
class Or(left: Node, right: Node) : TwoChildren(left, right)

class Equal(left: Node, right: Node) : TwoChildren(left, right)
class NotEqual(left: Node, right: Node) : TwoChildren(left, right)
class Less(left: Node, right: Node) : TwoChildren(left, right)
class Greater(left: Node, right: Node) : TwoChildren(left, right)
class LessOrEqual(left: Node, right: Node) : TwoChildren(left, right)
class GreaterOrEqual(left: Node, right: Node) : TwoChildren(left, right)

data class Val(override val name: String, override val value: Any? = null): Node, ValueNode, InternalIdentifiedNode {
    override fun toCode(): String {
        return """Val("$name", $name)"""
    }

}

interface IdentifiedNode {
    val name: String
}

internal interface InternalIdentifiedNode: IdentifiedNode {
    val prefix: String? get() = name.split("::").dropLast(1).firstOrNull()
    val insertablePrefix: String get() = prefix?.let { "$it." } ?: ""
    val nameWithoutPrefix: String get() = name.split("::").last()
}

interface ValueNode {
    val value: Any?
}

data class PseudoCallable(val referenceStr: String): KCallable<Any> {
    override val annotations: List<Annotation> get() = error("Must not be called")
    override val isAbstract: Boolean get() = error("Must not be called")
    override val isFinal: Boolean get() = error("Must not be called")
    override val isOpen: Boolean get() = error("Must not be called")
    override val isSuspend: Boolean get() = error("Must not be called")
    override val name: String get() = referenceStr
    override val parameters: List<KParameter> get() = error("Must not be called")
    override val returnType: KType get() = error("Must not be called")
    override val typeParameters: List<KTypeParameter> get() = error("Must not be called")
    override val visibility: KVisibility? get() = error("Must not be called")
    override fun call(vararg args: Any?): Any { error("Must not be called") }
    override fun callBy(args: Map<KParameter, Any?>): Any { error("Must not be called") }
}

data class Ref(val callable: KCallable<*>): Node, InternalIdentifiedNode {

    constructor(pseudoName: String): this(PseudoCallable(pseudoName))

    constructor(kclass: KClass<*>): this(kclass.primaryConstructor ?: kclass.constructors.first())

    override val name = (callable as? KFunction<*>)?.javaConstructor?.name ?: callable.name

    val returnClass get() = callable.returnType.classifier as KClass<*>

    val isConstructor get() = (callable as? KFunction<*>)?.javaConstructor != null

    override fun toCode(): String {
        val pseudo = (callable as? PseudoCallable) ?: error("Cannot serialize random callable")
        return """Ref(${pseudo.referenceStr})"""
    }
}

internal fun getInternalIdentifierFrom(obj: Node): InternalIdentifiedNode? {
    return when {
        obj is InternalIdentifiedNode -> obj
        obj is GetProperty && obj.internalIdentifier != null -> obj.internalIdentifier
        else -> null
    }
}

class GetProperty(obj: Node, prop: Node) : TwoChildren(obj, prop) {

    internal val internalIdentifier: InternalIdentifiedNode? get() {
        val objId = getInternalIdentifierFrom(children[0]) ?: return null
        val propId = getInternalIdentifierFrom(children[1]) ?: return null
        return object: InternalIdentifiedNode {
            override val name: String
                get() = "${objId.insertablePrefix}${objId.nameWithoutPrefix}::${propId.nameWithoutPrefix}"
        }
    }

}

class Call(val method: Node, vararg args: Node) : WithChildren(listOf(method) + args)

class Concat(vararg args: Node): WithChildren(*args)

data class Error(val errorMessage: String) : Node {

    override fun toCode(): String {
        return """error(${errorMessage.toCode()})"""
    }

}

