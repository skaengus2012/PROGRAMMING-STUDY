package com.codespitz.programming.chapter7

import java.util.Stack

// 캡처, 비캡처
// (a|b) - 캡처
// (?:a|b) - 비캡처
// ex:
// (tom|pot)ato -> tom, pot
// (?:tom|pot)ato -> tomato, potato

// 정규식에 빠져서 정규식에 모두 다 쓰려고 하지 마라.
// 정규식은 느림 - trim 이나 substring 에 비하면 느림.
// 값만 찾거나 처리하는 경우는 정규식이 이득.

// 도메인 부분은 정규식으로 안하는 것이 좋음 -> 정규식을 빡빡하게 짜면, 관리 측면에서...
// 정규식에 도메인을 넣기로 했으면, 정규식에 모두 들어가야함.

// 귀납적 사고방식에 따르면 현재의 특성 상태는 일반화된 알고리즘을 반복하면 얻을 수 있다.
// 반복하는 전체 과정을 한단계만 잘라서 그 구조를 파악한다. 전체 과정을 전지적으로 파악하는 것은 어려움.

// 일반화란 하나의 유일한 인생의 답을 찾는 것이 아니다.
// 발생 가능한 모든 경우의 수를 빈틈없이 처리한 것이 그 국면에서의 일반화

// 그 국면에 발생가능한 모든 경우의 수(패턴)을 발견할 머리가 되나? ----->
// 훈련을 통해 (이번 7주간 코드스피츠 과정을 5일내로 압축할 정도로 부하를 건다.)

// 1단계 : 제어문, 함수 + 변수의 라이프사이클과 스코프.
// 2단계 : 객체지향 구조물 + 함수형 구조물
// 3단계 : 동시성에 대한 이해
// 4단계 : 디자인과 레이어구조에 대한 이해
// 5단계 : 시스템 설계

fun parse(str: String): JsonObject? = parseInternal(str)

private val parseInternal: (str: String) -> JsonObject? = run {
    val numRegex = "^\\s*([0-9]+)\\s*[,]?".toRegex()
    val rKeyRegex = "^\\s*\"((?:\\\\\"|[^\"])+)\"\\s*:\\s*".toRegex()

    tailrec fun parser(str: String, acc: JsonObject?, key: String?, stack: Stack<Context>): JsonObject? {
        var v = str.trim()
        return if (v.isEmpty()) acc
        else {
            when (val v1 = v.first()) {
                '[', '{' -> {
                    stack.push(Context(acc, key))
                    parser(
                        str = v.substring(startIndex = 1),
                        acc = if (v1 == '[') JsonObject.JsonArray() else JsonObject.JsonElement(),
                        key = null,
                        stack
                    )
                }

                ']', '}' -> {
                    require(stack.isNotEmpty()) { "invalid json $v" }

                    val (prevAcc, prevKey) = stack.pop() ?: Context()
                    if (prevAcc == null) return acc
                    else {
                        val nextAcc = when (prevAcc) {
                            is JsonObject.JsonArray -> {
                                JsonObject.JsonArray(prevAcc.arr.plusElement(acc))
                            }
                            is JsonObject.JsonElement -> {
                                requireNotNull(prevKey) { "key was null" }
                                JsonObject.JsonElement(prevAcc.map + (prevKey to acc))
                            }
                        }
                        v = v.substring(startIndex = 1).trim()
                        parser(
                            str = if (v.first() == ',') v.substring(startIndex = 1) else v,
                            acc = nextAcc,
                            key = null,
                            stack
                        )
                    }
                }

                else -> {
                    when (acc) {
                        is JsonObject.JsonArray -> {
                            val valueMatcher = checkNotNull(numRegex.find(v)) { "Invalid array value : $v" }
                            parser(
                                str = v.substring(startIndex = valueMatcher.value.length),
                                acc = JsonObject.JsonArray(
                                    acc.arr.plusElement(valueMatcher.groupValues[1].toFloat())
                                ),
                                key = null,
                                stack
                            )
                        }
                        is JsonObject.JsonElement -> {
                            if (key == null) {
                                val keyMatcher = checkNotNull(rKeyRegex.find(v)) { "Invalid key : $v"}
                                parser(
                                    str = v.substring(startIndex = keyMatcher.value.length),
                                    acc = acc,
                                    key = keyMatcher.groupValues[1],
                                    stack
                                )
                            } else {
                                val valueMatcher = checkNotNull(numRegex.find(v)) { "Invalid object value : $v" }
                                parser(
                                    str = v.substring(startIndex = valueMatcher.value.length),
                                    acc = JsonObject.JsonElement(map = acc.map + (key to valueMatcher.groupValues[1].toFloat())),
                                    key = null,
                                    stack
                                )
                            }
                        }

                        null -> acc
                    }
                }
            }
        }
    }

    ({ str -> parser(str, null, null, Stack()) })
}

sealed class JsonObject {
    class JsonArray(val arr: Array<Any?> = emptyArray()) : JsonObject()
    class JsonElement(val map: Map<String, Any?> = emptyMap()) : JsonObject() {
        val arr: Array<Map.Entry<String, Any?>> by lazy { map.entries.toTypedArray() }
    }
}

fun JsonObject.toObject(): Any? {
    class Context(
        val obj: JsonObject,
        val index: Int,
        val key: String?,
        val acc: Any?
    ) {
        operator fun component1(): JsonObject = obj
        operator fun component2(): Int = index
        operator fun component3(): String? = key
        operator fun component4(): Any? = acc
    }

    tailrec fun toObjectInternal(obj: JsonObject, index: Int, key: String?, acc: Any?, stack: Stack<Context>): Any? {
        val element: Any? = when (obj) {
            is JsonObject.JsonArray -> obj.arr.getOrNull(index)
            is JsonObject.JsonElement -> obj.arr.getOrNull(index)
        }
        return if (element == null) {
            if (stack.isEmpty()) {
                acc
            } else {
                val (prevObj, prevIndex, prevKey, prevAcc) = stack.pop()
                toObjectInternal(
                    prevObj,
                    prevIndex,
                    prevKey,
                    if (key == null) (prevAcc as Array<Any?>) + acc
                    else (prevAcc as Map<*, *>) + (key to acc),
                    stack
                )
            }
        } else {
            if (element is JsonObject
                || (element is Map.Entry<*, *> && element.value is JsonObject)) {
                stack.push(Context(obj, index = index + 1, key, acc))

                val (nextElement: JsonObject, nextKey: String?) =
                    if (element is JsonObject) element to null
                    else { val (k, v) = (element as Map.Entry<*, *>); v as JsonObject to k as String }
                toObjectInternal(
                    nextElement,
                    index = 0,
                    key = nextKey,
                    acc = when (nextElement) {
                        is JsonObject.JsonArray -> emptyArray<Any?>()
                        is JsonObject.JsonElement -> emptyMap<String, Any?>()
                    },
                    stack
                )
            } else {
                val addToAcc: (Any?, Any) -> Any = { a, v ->
                    when (v) {
                        is Map.Entry<*, *> -> {
                            a as Map<*, *> + (v.key as String to v.value)
                        }
                        else -> (a as Array<Any?>) + v
                    }
                }

                toObjectInternal(
                    obj,
                    index = index + 1,
                    key = key,
                    acc = addToAcc(acc, element),
                    stack
                )
            }
        }
    }

    return toObjectInternal(
        this,
        0,
        null,
        if (this is JsonObject.JsonArray) emptyArray<Any?>() else emptyMap<String, Any?>(),
        Stack()
    )
}

private data class Context(
    val jsonObject: JsonObject? = null,
    val key: String? = null
)

fun main() {
    val target = """
        {"a":[1,2,[35,4],5], "b":{"a":123, "b":456}}
        """.trimIndent()
    val jsonObject = parse(target)
    val element = jsonObject?.toObject()
    println(element)
}