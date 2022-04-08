package com.codespitz.programming.chapter6

import com.codespitz.programming.chapter4.stringify

// 과제 1 - 모든 값 타입을 인식하여 파싱하는 중첩된 배열문자열 파서.

fun main() {
    val target = "[123, true, 1234, null,  \"\", \"sadsdaa\\\"sda\\nsddassad\",  [ null,  false, 44554, []]]"
    val arr: Array<out Any?> = parseArray(target)
    println(stringify(arr))
}

fun parseArray(target: String): Array<*> = parseArrayInternal(target)

private val parseArrayInternal: (target: String) -> Array<*> = run {
    class Context(
        val acc: Array<Any?>,
        val context: Context?
    )

    data class ParseResult(
        val isSuccess: Boolean,
        val elementString: String,
        val value: Any?
    )

    val failedParseResult = ParseResult(isSuccess = false, value = null, elementString = "")
    val elementParsers: List<Pair<Regex, (elementStr: String) -> Any?>> by lazy {
        listOf(
            "^\\s*([0-9])+\\s*,?".toRegex() to { elementStr -> elementStr.toLong() },
            "^\\s*(true|false)+\\s*,?".toRegex() to { elementStr -> elementStr.toBoolean() },
            "^\\s*(null)+\\s*,?".toRegex() to { null },
            "^\\s*(\"((\\\\\")|[^\"])*\")+\\s*,?".toRegex() to { elementStr ->
                stringParser(elementStr.substring(startIndex = 1, endIndex = elementStr.length - 1))
            }
        )
    }

    fun parseElement(target: String): ParseResult {
        return elementParsers
            .asSequence()
            .map { (regex, parse) ->
                when (val matchResult: MatchResult? = regex.find(target)) {
                    null -> failedParseResult
                    else -> {
                        ParseResult(
                            isSuccess = true,
                            elementString = matchResult.value,
                            value = matchResult.value
                                .let { str ->
                                    if (str.last() == ',') str.substring(startIndex = 0, endIndex = str.length - 1)
                                    else str
                                }
                                .let { parse(it) }
                        )
                    }
                }
            }
            .find { element -> element.isSuccess }
            ?: failedParseResult
    }

    fun parseArray(target: String, acc: Array<Any?>, context: Context?): Array<*> {
        val v: String = target.trim()
        return if (v.isEmpty()) acc
        else {
            when (v.first()) {
                '[' -> {
                    parseArray(target = v.substring(startIndex = 1), acc = emptyArray(), Context(acc, context))
                }

                ']' -> {
                    if (context == null) acc
                    else parseArray(
                        target = v.substring(startIndex = 1),
                        acc = context.acc.plusElement(acc),
                        context.context
                    )
                }

                else -> {
                    val elementParseResult: ParseResult = parseElement(v)
                        .also { result -> require(result.isSuccess) { "Invalid parameter was entered $v" } }
                    parseArray(
                        target = v.substring(elementParseResult.elementString.length),
                        acc = acc + elementParseResult.value,
                        context
                    )
                }
            }
        }
    }

    ({ target ->
        val head: Array<*> = parseArray(target, emptyArray(), context = null)
        check(head.size == 1) { "Parameter was not array string" }
        checkNotNull(head.first() as? Array<*>) { "Parameter was not array string" }
    })
}

private val stringParser: (value: String) -> String = run {
    val table = arrayOf(
        "\\\\\"".toRegex() to "\"",
        "(\\\\t|\\t)".toRegex() to "\t",
        "(\\\\n|\\n)".toRegex() to "\n",
    )

    ({ str -> table.fold(str) { acc, (regex, replacement) -> acc.replace(regex, replacement) } })
}