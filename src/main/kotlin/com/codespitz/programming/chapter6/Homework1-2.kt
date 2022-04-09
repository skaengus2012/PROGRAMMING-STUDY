package com.codespitz.programming.chapter6

import java.util.Date

// 과제 1 - 모든 값 타입을 인식하여 파싱하는 중첩된 배열문자열 파서
// 과제 2 - 나만의 클래스타입을 인식하여 해당 클래스의 인스턴스를 만들어 넣어주는 기능 추가
// 과제 4 - Date 의 json 인 경우 date 를 복원한다.

private val primitiveElementParser: ElementParser = CompositeElementParser.of(
    RegexElementParser(regex = "^\\s*([0-9])+\\s*,?".toRegex(), onParsing = { it.toLong() }),
    RegexElementParser(regex = "^\\s*(true|false)+\\s*,?".toRegex(), onParsing = { it.toBoolean() }),
    RegexElementParser(regex = "^\\s*(null)+\\s*,?".toRegex(), onParsing = { null }),
    DateElementParser(),
    StringElementParser()
)

private val parseArrayInternal: (target: String, elementParser: ElementParser) -> Array<*> = run {
    class Context(
        val acc: Array<Any?>,
        val context: Context?
    )

    fun parseArray(target: String, acc: Array<Any?>, context: Context?, elementParser: ElementParser): Array<*> {
        val v: String = target.trim()
        return if (v.isEmpty()) acc
        else {
            when (v.first()) {
                '[' -> {
                    parseArray(
                        target = v.substring(startIndex = 1),
                        acc = emptyArray(),
                        Context(acc, context),
                        elementParser
                    )
                }

                ']' -> {
                    if (context == null) acc
                    else parseArray(
                        target = v.substring(startIndex = 1),
                        acc = context.acc.plusElement(acc),
                        context.context,
                        elementParser
                    )
                }

                ',' -> {
                    parseArray(
                        target = v.substring(startIndex = 1),
                        acc,
                        context,
                        elementParser
                    )
                }

                else -> {
                    val elementParseResult: ElementParseResult.Success =
                        elementParser.convert(v).requireSuccess { "Invalid parameter was entered $v" }
                    parseArray(
                        target = v.substring(elementParseResult.elementString.length),
                        acc = acc + elementParseResult.value,
                        context,
                        elementParser
                    )
                }
            }
        }
    }

    ({ target, elementParser ->
        val head: Array<*> = parseArray(target, emptyArray(), context = null, elementParser)
        check(head.size == 1) { "Parameter was not array string" }
        checkNotNull(head.first() as? Array<*>) { "Parameter was not array string" }
    })
}

fun parseArray(
    target: String,
    customElementParser: ElementParser = EmptyElementParser()
): Array<*> = parseArrayInternal(target, CompositeElementParser.of(customElementParser, primitiveElementParser))

fun main() {
    val target = arrayOf(
        1,
        2,
        true,
        4,
        Date(),
        arrayOf(
            "Request Array\"\n ggg ",
            Album(
                albumId = 50,
                title = "BEST of K-pop",
                tracks = arrayOf(
                    Track(trackId = 1, title = "Butter", artistName = "BTS"),
                    Track(trackId = 2, title = "Dynamite", artistName = "BTS"),
                    Track(trackId = 3, title = "Brave", artistName = null),
                ),
            ),
            null,
            arrayOf<Any>()
        ),
        arrayOf(
            true,
            false,
            true,
            null
        )
    )
    val arr: Array<out Any?> = parseArray(
        stringify(target),
        CompositeElementParser.of(Track.TrackElementParser(), Album.AlbumElementParser())
    )
    println(stringify(arr) == stringify(target))
}