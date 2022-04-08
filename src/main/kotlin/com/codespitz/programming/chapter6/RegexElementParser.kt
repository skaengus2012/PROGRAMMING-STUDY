package com.codespitz.programming.chapter6

class RegexElementParser(
    private val regex: Regex,
    private val onParsing: (elementString: String) -> Any?
) : ElementParser {
    override fun convert(value: String): ElementParseResult = when (val matchResult: MatchResult? = regex.find(value)) {
        null -> ElementParseResult.Fail
        else -> ElementParseResult.Success(
            elementString = matchResult.value,
            value = matchResult.value
                .let { str ->
                    if (str.last() == ',') str.substring(startIndex = 0, endIndex = str.length - 1)
                    else str
                }
                .let { onParsing(it) }
        )
    }
}