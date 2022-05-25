package io.github.kotlinq.collections


/**
 * Marker to distinguish different [DataQueryable] implementations
 */
interface SpecificQueryable<X>

/**
 * Extension for [Queryable]
 *
 * Adds methods which perform operations over multiple [Queryable]-s. It is important
 * to have same implementations in this case, i.e. to not mix in-memory and sql storage. So you need
 * to create object implementing [SpecificQueryable] interface and use that object
 * for your [DataQueryable] implementations. After that compiler will not allow you to mix
 * [DataQueryable]-s with different [SpecificQueryable]
 */
interface DataQueryable<T, X>: Queryable<T>, SpecificQueryable<X> {

    /**
     * Returns data source with elements constructed by [construct] lambda
     * from pairs of elements from [this] data source and [another] one
     * which match [condition]
     */
    fun <R, Result> join(
        another: DataQueryable<R, X>,
        condition: (T, R) -> Boolean,
        construct: (T, R) -> Result
    ): DataQueryable<Result, X>

}