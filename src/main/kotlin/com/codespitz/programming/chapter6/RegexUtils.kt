package com.codespitz.programming.chapter6

private val stringParser: (value: String) -> String = run {
    val table = arrayOf(
        "\\\\\"".toRegex() to "\"",
        "(\\\\t|\\t)".toRegex() to "\t",
        "(\\\\n|\\n)".toRegex() to "\n",
    )

    ({ str -> table.fold(str) { acc, (regex, replacement) -> acc.replace(regex, replacement) } })
}

val stringOrNullRegex: Regex = "((null)|(\".*?((\"\")|([^\\\\\"](\")))))".toRegex()

fun stringOrNull(value: String): String? = if (value == "null") null else parseString(value)
fun parseString(value: String): String = stringParser(value.substring(startIndex = 1, endIndex = value.length - 1))
