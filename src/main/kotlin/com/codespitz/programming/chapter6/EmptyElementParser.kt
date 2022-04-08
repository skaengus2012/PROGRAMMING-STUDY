package com.codespitz.programming.chapter6

class EmptyElementParser : ElementParser {
    override fun convert(value: String): ElementParseResult = ElementParseResult.Fail
}