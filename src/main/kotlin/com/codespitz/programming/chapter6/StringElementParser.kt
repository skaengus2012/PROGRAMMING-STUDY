package com.codespitz.programming.chapter6

class StringElementParser : ElementParser by RegexElementParser(
    regex = "^\\s*(\"((\\\\\")|[^\"])*\")+\\s*,?".toRegex(),
    onParsing = { parseString(it) }
)