package com.codespitz.programming.chapter6

import java.text.SimpleDateFormat

class DateElementParser : ElementParser {
    override fun convert(value: String): ElementParseResult =
        value.takeIf { regex.matches(it) }
            ?.runCatching { dateFormat.parse(this) }
            ?.getOrNull()
            ?.let { date -> ElementParseResult.Success(value, date) }
            ?: ElementParseResult.Fail

    companion object {
        private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        private val regex: Regex =
            "\\s*([a-z]|[A-Z]){3}\\s([a-z]|[A-Z]){3}\\s(([0-2][0-9])|(3[0-1]))\\s(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9])\\s[A-Z]{3}\\s[0-9]{4,}".toRegex()
    }
}