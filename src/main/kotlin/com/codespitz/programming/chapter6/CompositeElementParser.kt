package com.codespitz.programming.chapter6

class CompositeElementParser(
    private val elementParsers: Iterable<ElementParser>
) : ElementParser {
    override fun convert(value: String): ElementParseResult = elementParsers.asSequence()
        .map { elementParser -> elementParser.convert(value) }
        .find { elementResult -> elementResult.isSuccess() }
        ?: ElementParseResult.Fail

    companion object {
        fun of(
            vararg elements: ElementParser
        ): CompositeElementParser = CompositeElementParser(elements.asIterable())
    }
}