package com.codespitz.programming.chapter6

fun ElementParseResult.isSuccess(): Boolean = this is ElementParseResult.Success
fun ElementParseResult.successOrNull(): ElementParseResult.Success? = this as? ElementParseResult.Success

inline fun ElementParseResult.requireSuccess(
    lazyMessage: () -> String = { "Require value was null" }
): ElementParseResult.Success = requireNotNull(successOrNull(), lazyMessage)