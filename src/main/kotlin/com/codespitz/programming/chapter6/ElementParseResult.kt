package com.codespitz.programming.chapter6

sealed class ElementParseResult {
    object Fail : ElementParseResult()
    data class Success(
        val elementString: String,
        val value: Any?
    ) : ElementParseResult()
}