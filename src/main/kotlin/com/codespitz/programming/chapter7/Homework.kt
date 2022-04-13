package com.codespitz.programming.chapter7

import java.lang.IllegalStateException

/**
 * @author Doohyun
 */

// 과제 - html parser 만들기

fun main() {
    // 모든 요소는 <*> </*> 평문 으로 처리..
    // <*> 가 등장 => Element 를 만들다. 이전의 Element 는 스택에 추가한다.
    // </*> 가 등장 => 현재 Element 를 마무리한다. 이전의 Element 에 현재 Element 를 추가한다.
    // 평문 : 공백을 제거한 평문이 들어오게 하자.

    val target = """
        <html>
            <title>
                  Test Document
            </title>
            <body>
                <div> Hello
                </div>
                <a href  = "https://hello.com" />
            </body>
        </html>
    """.trimIndent()
    println(parseHtml(target).rawString)
}

fun parseHtml(string: String): HtmlObject = htmlParser(string)

private val fieldsRegex: Regex = "(.+)(=)(.*)".toRegex()
private val startTagRegex: Regex = "^\\s*<([^[\\s/]].*?)>\\s*".toRegex()
private val endTagRegex: Regex = "^\\s*</([^\\s].*?)>\\s*".toRegex()
private val descriptionRegex: Regex = "^(\\s*.*?\\s*)</?(?:[^\\s]|.)+>".toRegex()
private fun String.splitByBlank(): List<String> = trim().split(" ").filter { it.isNotBlank() }

private val htmlParser: (String) -> HtmlObject = run {
    data class HtmlContext(
        val curElement: HtmlElement,
        val context: HtmlContext? = null
    )

    tailrec fun parseHtml(text: String, curElement: HtmlElement?, context: HtmlContext?): HtmlObject? {
        val v: String = text.trim()
        if (v.isBlank()) return curElement

        val startTagMatchResult: MatchResult? = startTagRegex.find(v)
        if (startTagMatchResult != null) {
            val (rawStr: String, captureValue: String) = startTagMatchResult.groupValues
            val captureSplits: List<String> = captureValue.splitByBlank()
            val openObject = HtmlElement(
                tagName = requireNotNull(captureSplits.firstOrNull()) { "Cannot found open tag -> $v" },
                rawString = rawStr,
                fields = fieldParser(captureSplits.subList(fromIndex = 1, toIndex = captureSplits.size))
            )
            val (nextElement: HtmlElement?, nextContext: HtmlContext?) =
                if (captureValue.endsWith("/"))
                    curElement?.copy(child = curElement.child + openObject, rawString = openObject.rawString) to context
                else openObject to if (curElement == null) context else HtmlContext(curElement, context)
            return parseHtml(text = v.substring(rawStr.length), nextElement, nextContext)
        }

        val endTagMatchResult: MatchResult? = endTagRegex.find(v)
        if (endTagMatchResult != null) {
            val (rawStr: String, captureValue: String) = endTagMatchResult.groupValues
            val closeName: String =
                requireNotNull(captureValue.splitByBlank().firstOrNull()) { "Cannot found close tag -> $v" }
            val nextText = v.substring(rawStr.length)
            val closedObject: HtmlObject = when {
                curElement == null -> HtmlText(rawStr)
                curElement.tagName == closeName -> curElement.copy(rawString = curElement.rawString + rawStr)
                else -> HtmlText(text = curElement.rawString + rawStr)
            }
            return if (context == null) closedObject
            else {
                val (beforeElement, beforeContext) = context
                parseHtml(
                    nextText,
                    curElement = beforeElement.copy(
                        child = beforeElement.child + closedObject,
                        rawString = beforeElement.rawString + closedObject.rawString
                    ),
                    beforeContext
                )
            }
        }

        val descriptionMatchResult: MatchResult? = descriptionRegex.find(v)
        if (descriptionMatchResult != null) {
            val captureValue: String = descriptionMatchResult.groupValues[1]
            val nextText = v.substring(captureValue.length)
            val nextElement: HtmlElement? = curElement?.copy(
                child = curElement.child + HtmlText(captureValue),
                rawString = curElement.rawString + captureValue
            )

            return parseHtml(nextText, nextElement, context)
        }

        throw IllegalStateException()
    }

    ({ str -> parseHtml(str, curElement = null, context = null) ?: HtmlText(str) })
}

private val fieldParser: (strings: List<String>) -> List<HtmlField> = run {
    data class FieldContext(
        val fieldName: String,
        val hasEqualsChar: Boolean = false
    )

    tailrec fun parseFields(
        strings: List<String>,
        index: Int,
        acc: List<HtmlField>,
        fieldContext: FieldContext?
    ): List<HtmlField> {
        return if (index >= strings.size) {
            if (fieldContext == null) acc else acc + HtmlField(name = fieldContext.fieldName, value = "")
        } else {
            val v = strings[index]
            val (nextAcc: List<HtmlField>, nextFieldContext: FieldContext?) = if (v == "=") {
                if (fieldContext == null) {
                    acc to null
                } else {
                    acc to fieldContext.copy(hasEqualsChar = true)
                }
            } else {
                if (fieldContext == null) {
                    acc to FieldContext(fieldName = v)
                } else {
                    if (fieldContext.hasEqualsChar) {
                        acc + HtmlField(name = fieldContext.fieldName, value = v) to null
                    } else {
                        acc + HtmlField(name = fieldContext.fieldName, value = "") to FieldContext(v)
                    }
                }
            }

            parseFields(strings, index = index + 1, nextAcc, nextFieldContext)
        }
    }

    ({ strings ->
        parseFields(
            strings = strings
                .asSequence()
                .map { str -> fieldsRegex.find(str)?.groupValues?.let { listOf(it[1], it[2], it[3]) } ?: listOf(str) }
                .fold(mutableListOf()) { acc, v -> acc.apply { this += v } },
            index = 0,
            acc = emptyList(),
            fieldContext = null
        )
    })
}