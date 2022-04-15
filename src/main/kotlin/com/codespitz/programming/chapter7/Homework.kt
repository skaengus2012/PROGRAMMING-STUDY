package com.codespitz.programming.chapter7

import com.codespitz.programming.chapter7.HtmlObject.*
import com.codespitz.programming.chapter7.HtmlObject.HtmlElement.*

/**
 * @author Doohyun
 */

// 과제 - html parser 만들기

fun main() {
    // 모든 요소는 <*/> <*> </*> 평문 으로 처리..
    // <*> 가 등장 => Element 를 만들다. 이전의 Element 는 스택에 추가한다.
    // </*> 가 등장 => 현재 Element 를 마무리한다. 이전의 Element 에 현재 Element 를 추가한다.
    // 평문 : 공백을 제거한 평문이 들어오게 하자.

    val htmlObject: HtmlObject = parseHtml(
        """
        <html>
            <title>
                  Test Document
            </title>
            <body>
                <div> Hello <br/><img src="https://naver.com />
                </div>
                <script>
                    function hello() { console.log("Hello") }
                </script>
                <a href  = "https://hello.com" />
            </body>
        </html>
    """.trimIndent())
    println(htmlObject.rawString)
}

fun parseHtml(string: String): HtmlObject = htmlParser(string)

private val fieldsRegex: Regex = "(.+)(=)(.*)".toRegex()
private val startTagRegex: Regex = "^\\s*<([^(\\s|/)].*?)>\\s*".toRegex()
private val endTagRegex: Regex = "^\\s*</(\\S.*?)>\\s*".toRegex()
private val descriptionRegex: Regex = "^(\\s*.*?\\s*)</?(?:[^\\s]|.)+>".toRegex()
private fun String.splitByBlank(): List<String> = trim().split(" ").filter { it.isNotBlank() }

private val htmlParser: (String) -> HtmlObject = run {
    data class HtmlContext(
        val curElement: HtmlElement,
        val context: HtmlContext? = null
    )

    data class NextParameter(
        val text: String,
        val curElement: HtmlElement,
        val context: HtmlContext?
    )

    val parseChains = listOf(
        startTagRegex to { v: String, curElement: HtmlElement, context: HtmlContext? ->
            { startTagMatchResult: MatchResult ->
                val (rawStr: String, captureValue: String) = startTagMatchResult.groupValues
                val captureSplits: List<String> = captureValue.splitByBlank()
                val openObject = HtmlElement(
                    tagName = captureSplits.first().let { tagName ->
                        if (tagName.endsWith("/")) tagName.substring(0, tagName.length - 1) else tagName
                    },
                    rawString = rawStr,
                    fields = fieldParser(captureSplits.subList(fromIndex = 1, toIndex = captureSplits.size))
                )
                val (nextElement: HtmlElement, nextContext: HtmlContext?) =
                    if (captureValue.endsWith("/")) curElement.withChild(openObject) to context
                    else openObject to HtmlContext(curElement, context)

                NextParameter(v.substring(rawStr.length), nextElement, nextContext)
            }
        },

        endTagRegex to { v: String, curElement: HtmlElement, context: HtmlContext? ->
            { endTagMatchResult: MatchResult ->
                val (rawStr: String, captureValue: String) = endTagMatchResult.groupValues
                val closeName: String = captureValue.splitByBlank().first()
                val closedObject: HtmlObject =
                    if (curElement.tagName == closeName) curElement.copy(rawString = curElement.rawString + rawStr)
                    else HtmlText(curElement.rawString + rawStr)
                if (context == null) {
                    curElement.withChild(closedObject)
                } else {
                    val (beforeElement, beforeContext) = context
                    NextParameter(v.substring(rawStr.length), beforeElement.withChild(closedObject), beforeContext)
                }
            }
        },

        descriptionRegex to { v: String, curElement: HtmlElement, context: HtmlContext? ->
            { descriptionMatchResult: MatchResult ->
                val captureValue: String = descriptionMatchResult.groupValues[1]
                NextParameter(v.substring(captureValue.length), curElement.withChild(HtmlText(captureValue)), context)
            }
        }
    )

    tailrec fun parseHtml(
        text: String,
        curElement: HtmlElement,
        context: HtmlContext?,
    ): HtmlElement? {
        val v: String = text.trim()
        return if (v.isBlank()) {
            require(context == null) { "Document was end with not closed tag" }
            curElement
        } else {
            when (val next = parseChains
                .asSequence()
                .map { (regex, next) -> regex.find(v)?.let { matcher -> next(v, curElement, context)(matcher) } }
                .find { it != null }
            ) {
                is HtmlElement -> next
                is NextParameter -> parseHtml(next.text, next.curElement, next.context)
                else -> throw IllegalArgumentException("Parsing was failed by -> $v")
            }
        }
    }

    ({ str ->
        runCatching { parseHtml(str, curElement = HtmlElement(tagName = "", rawString = ""), context = null) }
            .getOrNull()
            ?.child
            ?.takeIf { it.size == 1 }
            ?.first()
            ?: HtmlText(str)
    })
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