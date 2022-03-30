package com.codespitz.programming.chapter4

// stringify({...})
// Object 가 포함된 stringify 를 구현할 것.

fun main() {
    println(stringify({ a: Int, b: Int -> a + b}))
    println(stringify(Album(
        albumId = 50,
        title = "BEST of K-pop",
        tracks = arrayOf(
            Track(trackId = 1, title = "Butter", artistName = "BTS"),
            Track(trackId = 2, title = "Dynamite", artistName = "BTS"),
            Track(trackId = 3, title = "Brave", artistName = null),
        ),
    )))
}

fun stringify(any: Any?): String {
    return Element.of(any).value()
}

private sealed class Element {
    abstract fun value(): String
    private class SingleElement(private val data: String) : Element() {
        override fun value(): String = data
    }
    private class StringElement(private val data: String) : Element() {
        override fun value(): String = "\"${stringStringify(data)}\""
    }
    private class ArrayElement(private val array: Array<*>) : Element() {
        override fun value(): String = arrayStringify(array) { elements ->
            SingleElement("[%s]".format(
                elements.fold("") { accStr, v -> "${accStr},${v.value()}" }
                    .takeIf { it.isNotBlank() }
                    ?.substring(startIndex = 1)
                    ?: ""
            ))
        }
    }
    private class FieldPairElement(private val fieldPair: FieldPair) : Element() {
        override fun value(): String {
            return "\"${fieldPair.filedName}\":${fieldPair.value.value()}"
        }
    }

    private class DefaultElement(private val any: Any) : Element() {
        override fun value(): String = arrayStringify(any::class.java.declaredFields
            .onEach { it.isAccessible = true }
            .map { it.name to it.get(any) }
            .filter { (_, v) -> v !== any }
            .map { (name, v) -> FieldPair(name, of(v)) }
            .toTypedArray()) { elements ->
            SingleElement("{%s}".format(
                elements.fold("") { accStr, v -> "${accStr},${v.value()}" }
                    .takeIf { it.isNotBlank() }
                    ?.substring(startIndex = 1)
                    ?: ""
            ))
        }
    }

    private data class FieldPair(
        val filedName: String,
        val value: Element
    )

    companion object {
        fun of(target: Any?): Element = when(target) {
            null -> SingleElement(data = "null")
            is Number, is Boolean -> SingleElement(data = target.toString())
            is String -> StringElement(target)
            is FieldPair -> FieldPairElement(target)
            is Array<*> -> ArrayElement(target)
            else -> DefaultElement(target)
        }
    }
}

private val stringStringify: (String) -> String = run {
    val table = arrayOf(
        "\"".toRegex() to "\\\\\"",
        "\t".toRegex() to "\\\\t",
        "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
    )
    ({ str ->
        table.fold(str) { acc, (regex, replacement) -> acc.replace(regex = regex, replacement = replacement) }
    })
}

private val arrayStringify: (Array<*>, (Array<Element>) -> Element) -> String = run {
    class Context(
        val array: Array<*>,
        val acc: Array<Element>,
        val index: Int,
        val prevContext: Context? = null
    ) {
        operator fun component1(): Array<*> = array
        operator fun component2(): Array<Element> = acc
        operator fun component3(): Int = index
        operator fun component4(): Context? = prevContext
    }

    fun nextContext(
        curEl: Any?,
        array: Array<*>,
        acc: Array<Element>,
        index: Int,
        prevContext: Context? = null
    ): Context = when (curEl) {
        is Array<*> -> Context(curEl, emptyArray(), index = 0, Context(array, acc, index = index + 1, prevContext))
        else -> Context(array, acc = acc + arrayOf(Element.of(curEl)), index = index + 1, prevContext)
    }

    tailrec fun recursive(
        array: Array<*>,
        acc: Array<Element>,
        index: Int,
        prevContext: Context? = null,
        formatter: (Array<Element>) -> Element
    ): String {
        return if (index < array.size) {
            val (nextArray, nextAcc, nextIndex, nextContext) = nextContext(
                curEl = array[index],
                array,
                acc,
                index,
                prevContext
            )
            recursive(nextArray, nextAcc, nextIndex, nextContext, formatter)
        } else {
            if (prevContext != null) {
                val (prevArray, prevAcc, prevIndex, prevPrevContext) = prevContext
                recursive(prevArray, acc = prevAcc + arrayOf(formatter(acc)), prevIndex, prevPrevContext, formatter)
            } else {
                formatter(acc).value()
            }
        }
    }

    { arr, formatter -> recursive(arr, emptyArray(), index = 0, prevContext = null, formatter) }
}