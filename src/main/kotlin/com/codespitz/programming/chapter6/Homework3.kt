package com.codespitz.programming.chapter6

import java.util.Date
import kotlin.collections.Iterator

// 과제 3 - 기존 작성한 stringify 가 오브젝트인 경우 toJson 을 구현하고 있으면 그걸 이용해 stringify 가 되도록 보수
// 과제 4 - Date 의 json 인 경우 date 를 복원한다.

fun stringify(any: Any?): String {
    return Element.of(any).toJsonString()
}

private sealed class Element {
    abstract fun toJsonString(): String

    private class SingleElement(private val value: () -> String) : Element() {
        override fun toJsonString(): String = value()
    }

    private class StringElement(value: String) : Element() {
        private val internalValue: String by lazy { "\"${stringStringify(value)}\"" }
        override fun toJsonString(): String = internalValue
    }

    private class IteratorElement(private val iterator: Iterator<*>) : Element() {
        override fun toJsonString(): String =
            iteratorStringify(iterator) { node -> nodeStringify(node, "[", "]") }
    }

    private class EntryElement(private val fieldEntry: ElementEntry) : Element() {
        override fun toJsonString(): String = "${fieldEntry.key.toJsonString()}:${fieldEntry.value.toJsonString()}"
    }

    private class DictionaryElement(private val iterator: Iterator<ElementEntry>) : Element() {
        override fun toJsonString(): String =
            iteratorStringify(iterator) { node -> nodeStringify(node, "{", "}") }
    }

    companion object {
        fun of(any: Any?): Element = when (any) {
            null -> SingleElement { "null" }
            is Number, is Boolean -> SingleElement { any.toString() }
            is String -> StringElement(any)
            is Array<*> -> IteratorElement(any.iterator())
            is Iterable<*> -> IteratorElement(any.iterator())
            is Map<*, *> -> {
                DictionaryElement(iterator {
                    for (entry in any.entries) {
                        yield(ElementEntry(of(entry.key).toMapKeyElement(), of(entry.value)))
                    }
                })
            }
            is Date -> StringElement(any.toString())
            is JsonSerializable -> StringElement(any.toJsonString())
            is ElementEntry -> EntryElement(any)
            else -> {
                DictionaryElement(iterator {
                    for (field in any::class.java.declaredFields) {
                        val value = field.also { it.isAccessible = true }.get(any)
                        if (value !== any) {
                            yield(ElementEntry(StringElement(field.name), of(value)))
                        }
                    }
                })
            }
        }

        private fun Element.toMapKeyElement(): Element = when(this) {
            is StringElement -> this
            else -> SingleElement { "\"${toJsonString()}\"" }
        }
    }
}

private data class ElementEntry(
    val key: Element,
    val value: Element
)

private sealed class Node {
    abstract val element: Element?
    abstract fun next(): Node?
    abstract fun add(element: Element): Node

    private class Impl(
        override val element: Element,
        val next: Node?
    ) : Node() {
        override fun next(): Node? = next
        override fun add(element: Element): Node = Impl(element, next = this)
    }

    object Empty : Node() {
        override val element: Element? = null
        override fun next(): Node? = null
        override fun add(element: Element): Node = Impl(element, next = null)
    }
}

private val nodeStringify: (node: Node, prefix: String, postfix: String) -> String = run {
    tailrec fun recursive(node: Node, acc: String): String {
        val next: Node? = node.next()
        val nextAcc = "${node.element?.toJsonString() ?: ""},$acc"
        return if (next == null) nextAcc.substring(0, nextAcc.length - 1) else recursive(next, nextAcc)
    }

    ({ node, prefix, postfix -> "${prefix}${recursive(node, acc = "")}${postfix}" })
}

private val stringStringify: (value: String) -> String = run {
    val table = arrayOf(
        "\"".toRegex() to "\\\\\"",
        "\t".toRegex() to "\\\\t",
        "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
    )

    ({ str -> table.fold(str) { acc, (regex, replacement) -> acc.replace(regex, replacement) } })
}

private val iteratorStringify: (iterator: Iterator<*>, jsonFormatter: ((Node) -> String)) -> String = run {
    data class Context(
        val iterator: Iterator<*>,
        val acc: Node,
        val context: Context?
    )

    tailrec fun recursive(
        iterator: Iterator<*>,
        acc: Node,
        preContext: Context?,
        toJsonString: (Node).() -> String
    ): String {
        return if (iterator.hasNext()) {
            val value: Any? = iterator.next()
            val (nextIterator, nextAcc, nextContext) = if (value is Iterator<*>) {
                Context(value, Node.Empty, Context(iterator, acc, preContext))
            } else {
                Context(iterator, acc.add(Element.of(value)), preContext)
            }
            recursive(nextIterator, nextAcc, nextContext, toJsonString)
        } else {
            if (preContext != null) {
                val (preIterator, preAcc, prePreContext) = preContext
                recursive(preIterator, acc.add(Element.of(preAcc.toJsonString())), prePreContext, toJsonString)
            } else {
                acc.toJsonString()
            }
        }
    }

    ({ arr, jsonFormatter -> recursive(arr, Node.Empty, preContext = null, toJsonString = jsonFormatter) })
}

fun main() {
    println(stringify({ a: Int, b: Int -> a + b }))
    println(
        stringify(
            mapOf(
                Track(trackId = 1, title = "Butter", artistName = "BTS") to 2,
                "Hello" to 10,
                4 to 5,
                4L to 6,
            ),
        )
    )
    println(
        stringify(
            Album(
                albumId = 50,
                title = "BEST of K-pop",
                tracks = arrayOf(
                    Track(trackId = 1, title = "Butter\"", artistName = "BTS"),
                    Track(trackId = 2, title = "Dynamite", artistName = "BTS"),
                    Track(trackId = 3, title = "Brave", artistName = null),
                ),
            )
        )
    )
    println(stringify(arrayOf(Date())))
}
