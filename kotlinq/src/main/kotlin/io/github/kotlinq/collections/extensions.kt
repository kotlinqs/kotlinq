package io.github.kotlinq.collections

fun <T> Sequence<T>.asQueryable(): QueryableSequence<T> = QueryableSequence(this)

fun <T> QueryableSequence<T>.asSequence(): Sequence<T> = sequence

fun <T> Queryable<T>.first() = take(1).toList()[0]

fun <T> queryableSequenceOf(vararg items: T) = sequenceOf(*items).asQueryable()