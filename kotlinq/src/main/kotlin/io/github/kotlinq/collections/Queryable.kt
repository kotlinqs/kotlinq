package io.github.kotlinq.collections

/**
 * Provides lazy access to some data source.
 *
 * Implementations may convert passed lambda expressions into internal data query expressions (like SQL)
 * or evaluate directly, for in-memory source.
 *
 * Acts like [Sequence]. Actions are queued and executed only on terminal operations,
 * like [count], [toList] or [aggregate]
 */
interface Queryable<T> {

    /**
     * Returns [Queryable] data source which contains only elements which matched by provided [predicate]
     */
    fun filter(predicate: (T) -> Boolean): Queryable<T>

    /**
     * Returns data source with [R] element for every [T] element from original data source.
     * Convertion is performed by [mapper]
     */
    fun <R> map(mapper: (T) -> R): Queryable<R>

    /**
     * Returns data source with elements sorted in ascending order by [selector]
     */
    fun <C: Comparable<C>> sortedBy(selector: (T) -> C): Queryable<T>

    /**
     * Returns data source with elements sorted in descending order by [selector]
     */
    fun <C: Comparable<C>> sortedDescendingBy(selector: (T) -> C): Queryable<T>

    /**
     * Returns data source without first [offset] elements
     */
    fun drop(offset: Int): Queryable<T>

    /**
     * Returns data source with only [limit] elements (or less)
     */
    fun take(limit: Int): Queryable<T>

    /**
     * Obtains elements from data storage into memory and return them.
     *
     * Terminal operation (evaluates all previously used non-terminal operations)
     */
    fun toList(): List<T>

    /**
     * Applies [aggFn] for elements from data storage and returns result.
     *
     * Must return same result as `aggFn(toList())`, but may do it more efficient way
     *
     * Terminal operation (evaluates all previously used non-terminal operations)
     */
    fun <R> aggregate(aggFn: (Iterable<T>) -> R): R

    /**
     * Returns count of elements in data source
     *
     * Terminal operation (evaluates all previously used non-terminal operations)
     */
    fun count(): Long

}

