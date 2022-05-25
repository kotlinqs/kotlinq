package io.github.kotlinq.collections

object QS : SpecificQueryable<QS>

/**
 * Wrapper over regular [Sequence] which implements [Queryable]
 */
class QueryableSequence<T>(
    val sequence: Sequence<T>
) : DataQueryable<T, QS> {

    override fun filter(predicate: (T) -> Boolean): QueryableSequence<T> {
        return QueryableSequence(sequence.filter(predicate))
    }

    override fun <R> map(mapper: (T) -> R): QueryableSequence<R> {
        return QueryableSequence(sequence.map(mapper))
    }

    override fun <C : Comparable<C>> sortedBy(selector: (T) -> C): QueryableSequence<T> {
        return QueryableSequence(sequence.sortedBy(selector))
    }

    override fun <C : Comparable<C>> sortedDescendingBy(selector: (T) -> C): QueryableSequence<T> {
        return QueryableSequence(sequence.sortedByDescending(selector))
    }

    override fun drop(offset: Int): QueryableSequence<T> {
        return QueryableSequence(sequence.drop(offset))
    }

    override fun take(limit: Int): QueryableSequence<T> {
        return QueryableSequence(sequence.take(limit))
    }

    override fun toList(): List<T> {
        return sequence.toList()
    }

    override fun <R, Result> join(
        another: DataQueryable<R, QS>,
        condition: (T, R) -> Boolean,
        construct: (T, R) -> Result
    ): DataQueryable<Result, QS> {
        val anotherList = (another as QueryableSequence<R>).sequence.toList()
        return QueryableSequence(sequence.flatMap { left ->
            anotherList
                .filter { right -> condition(left, right) }
                .map { right -> construct(left, right) }
        })
    }

    override fun <R> aggregate(aggFn: (Iterable<T>) -> R): R {
        return aggFn(sequence.toList())
    }

    override fun count(): Long {
        return sequence.count().toLong()
    }
}