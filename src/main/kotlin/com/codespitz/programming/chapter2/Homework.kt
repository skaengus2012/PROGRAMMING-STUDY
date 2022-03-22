package com.codespitz.programming.chapter2

// 숙제 : Array 배열을 Json.stringify 처럼 구현
// 재귀, 꼬리 재귀 -> 번역한 for
fun main() {
    val a = arrayOf(1, true, "ab\"c", null, { 3 })

    println(arrayStringifyByRecursive(a))
    println(arrayStringifyByTailrec(a))
    println(arrayStringifyByIteration(a))
}

// Recursive
fun arrayStringifyByRecursive(array: Array<out Any?>, index: Int = -1): String {
    return getValueFromArray(array, index) +
            (if (index < array.size) arrayStringifyByRecursive(array, index + 1) else "")
}

// Tailrec
tailrec fun arrayStringifyByTailrec(array: Array<out Any?>, index: Int = -1, acc: String = ""): String {
    return if (index <= array.size) arrayStringifyByTailrec(array, index + 1, acc + getValueFromArray(array, index))
    else acc
}

// convert to loop from tailrec
fun arrayStringifyByIteration(array: Array<out Any?>): String {
    var acc = ""
    var index = -1
    while (index <= array.size) {
        acc = acc + getValueFromArray(array, index)
        index = index + 1
    }
    return acc
}

fun getValueFromArray(array: Array<out Any?>, index: Int): String = when (index) {
    -1 -> "\"["
    array.size -> "]\""
    in array.indices -> valueOf(array[index]) + if (index == array.size - 1) "" else ", "
    else -> throw IllegalArgumentException()
}

fun valueOf(any: Any?): String = when (any) {
    is Number, is Boolean -> "" + any
    is String -> addQuotes(any)
    else -> "null"
}

fun addQuotes(str: Any): String {
    return "\"" + str + "\""
}