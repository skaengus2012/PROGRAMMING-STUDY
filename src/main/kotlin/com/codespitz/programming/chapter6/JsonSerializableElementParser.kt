package com.codespitz.programming.chapter6

class JsonSerializableElementParser(
    private val entireRegex: Regex,
    private val map: (ElementParseResult.Success) -> ElementParseResult
) : ElementParser {
    private val stringElementParser = StringElementParser()
    override fun convert(value: String): ElementParseResult {
        return stringElementParser.convert(value).successOrNull()
            ?.takeIf { entireRegex.matches(it.value.toString()) }
            ?.let(map)
            ?: ElementParseResult.Fail
    }
}