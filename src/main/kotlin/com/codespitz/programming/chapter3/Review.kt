package com.codespitz.programming.chapter3

// if - optional, shield (부록, 단일적), 그 외에는 잘못 씀.
// if else - binary mandatory (이지선다), 병렬조건 - if else 각각의 처리가 의미상 똑같다.
// switch - multi mandatory (병렬조건일 때만) - switch 에 빈틈에 있으면 안 됨.
// while - recursive 사전에 계획되지 않은 반복 - 반복할때마다 다음 반복을 계산한다.
// for - iteration 사전에 계획된 반복 - 이미 반복을 어떻게 할지 계획을 세웠다.

fun main() {
    println(arrayStringify(arrayOf(1, true, "ab\"c\n\t", null, { 3 })))
    println(arrayStringifyWithWhile(arrayOf(1, true, "ab\"c", null, { 3 })))
}

// 👀 js 에서는 function keyword 는 더이상 사용안하다는데.. 더이상 fun 의 의미가 없어보임..
val arrayStringify: (Array<out Any?>) -> String = run {
    // 👀 내부 scope 를 갖는 필드를 갖는 함수.. 결국 객체
    val stringStringify: (String) -> String = run {
        val table = arrayOf(
            "\"".toRegex() to "\\\\\"",
            "\t".toRegex() to "\\\\t",
            "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
        )

        ({ str ->
            table.fold(str) { acc, (regex, replacement) -> acc.replace(regex = regex, replacement = replacement) }
        })
    }

    fun valueOf(any: Any?) = when (any) {
        is Number, is Boolean -> any.toString()
        is String -> "\"${stringStringify(any)}\""
        else -> "null"
    }

    tailrec fun recursive(
        arr: Array<out Any?>,
        acc: String,
        index: Int
    ): String =
        if (index < arr.size) recursive(arr, acc = "$acc,${valueOf(arr[index])}", index = index + 1)
        else "[${acc.substring(startIndex = 1)}]"

    // if else - binary mandatory
    ({ array -> if (array.isEmpty()) "[]" else recursive(array, acc = "", index = 0) })
}

val arrayStringifyWithWhile: (Array<out Any?>) -> String = run {
    val stringStringify: (String) -> String = run {
        val table = arrayOf(
            "\"".toRegex() to "\\\\\"",
            "\t".toRegex() to "\\\\t",
            "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
        )

        ({ str ->
            table.fold(str) { acc, (regex, replacement) -> acc.replace(regex = regex, replacement = replacement) }
        })
    }

    fun valueOf(any: Any?): String = when (any) {
        is Number, is Boolean -> any.toString()
        is String -> "\"${stringStringify(any)}\""
        else -> "null"
    }

    ({ arr ->
        // if else - binary mandatory
        if (arr.isEmpty()) "[]"
        else {
            var acc = ""
            var index = 0
            // 꼬리재귀는 while 로 번역해야함. for 는 iteration
            // for 로 번역은 for(; index < arr.size; index = index + 1) 이지만, 꼬리재귀의 body 에서 index 증가 동작이 있음
            // for 로 번역한다면, 잘못 번역한 것.
            while (index < arr.size) {
                acc = "$acc,${valueOf(arr[index])}"
                index = index + 1
            }
            "[${acc.substring(startIndex = 1)}]"
        }
    })
}