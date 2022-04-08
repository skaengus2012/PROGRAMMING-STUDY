package com.codespitz.programming.chapter6

interface ElementParser {
    fun convert(value: String): ElementParseResult
}