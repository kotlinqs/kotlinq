package io.github.kotlinq.collections

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryableSequenceTest {

    data class User(val id: Int, val username: String)
    data class Post(val userId: Int, val text: String)

    @Test
    fun filter() {
        assertEquals(
            listOf(User(1, "a")),
            queryableSequenceOf(User(1, "a"), User(2, "b")).filter { it.id == 1 }.toList()
        )
    }

    @Test
    fun map() {
        assertEquals(
            listOf(User(1, "A")),
            queryableSequenceOf(User(1, "a")).map { User(it.id, it.username.uppercase()) }.toList()
        )
    }

    @Test
    fun join() {
        val sequence1 = queryableSequenceOf(User(1, "A"), User(2, "B"), User(3, "C"))
        val sequence2 = queryableSequenceOf(Post(1, "Post#1"), Post(3, "Post#2"), Post(3, "Post#3"))
        val result = sequence1.join(sequence2, { user, post -> user.id == post.userId}, { user, post -> "${post.text} of ${user.username}" })
        assertEquals(
            listOf("Post#1 of A", "Post#2 of C", "Post#3 of C"),
            result.toList()
        )
    }

    @Test
    fun max() {
        assertEquals(
            2,
            queryableSequenceOf(User(1, "a"), User(2, "b")).map { it.id }.aggregate { it.maxOrNull() }
        )
    }

    @Test
    fun sum() {
        assertEquals(
            3,
            queryableSequenceOf(User(1, "a"), User(2, "b")).map { it.id }.aggregate { it.sum() }
        )
    }

    @Test
    fun count() {
        assertEquals(
            4,
            queryableSequenceOf(User(1, "a"), User(2, "b"), User(3, "c"), User(4, "d")).count()
        )
    }
}