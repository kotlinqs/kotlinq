package io.github.kotlinq.expression

val QQQ = "\"\"\""

fun Any?.toCode(): String = when(this) {
    null -> "null"
    is SerializableToCode -> this.toCode()
    is String -> "$QQQ$this$QQQ"
    else -> this.toString()
}

interface SerializableToCode {
    val importString: String
    fun toCode(): String
}