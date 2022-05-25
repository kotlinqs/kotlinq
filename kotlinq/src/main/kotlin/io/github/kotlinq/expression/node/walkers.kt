package io.github.kotlinq.expression.node

fun walk(node: Node, fn: (Node) -> Unit) {
    fn(node)
    if (node is WithChildren) {
        node.children.forEach { walk(it, fn) }
    }
}