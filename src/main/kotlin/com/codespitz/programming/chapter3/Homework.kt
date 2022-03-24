package com.codespitz.programming.chapter3

import java.util.Stack

// 과제 - [1, 2, ["a", false], 3, ["b", "c", [1, 2]]] 를 stringify 할 것
//       꼬리재귀, 루프만 만들면 됨.
// 1. ds stack
// 2. 최상위 배열의 원소를 돌다가 원소가 배열인 경우를 만난다.
// 3. 스택에 최상위 배열과 진행했던 인덱스를 담아둔다.
// 4. 자식배열을 다시 0번 인덱스부터 돈다.
// 5. 자식배열이 다 끝나면, 스택에서 마지막에 넣었던 정보가 자기 부모배열의 정보이므로 이를 복원해서 부모 배열과 그 인덱스를 이어서 진행한다.
// 6. 그 와중에 어짜피 문자열은 ACC 하나에 더해가면 됨.

// 등장하는 모든 원소의 공통점을 찾아 패턴화한다.
// 1. 모든 원소는 인덱스를 갖고 있다.
// 2. 모든 원소는 소속된 배열이 있다.
// 3. 모든 원소는 값이거나 배열이다.
// 4. 원소가 배열인 경우는 부모배열이 있는 경우와, 없는 경우로 나눌 수 있다.
// 5. 원소의 인덱스는 중간인 경우와 마지막인 경우가 있다.
// 어떤 원소가 와도 저 5가지 경우만 처리하면, 일반화된 귀납적 꼬리최적화로 짤 수 있고, 그렇다면 전지적 루프로 변경할 수 있다.
// 이건 [어떤걸 반복] 해서 만들어진거지? 각 반복은 [이전 결과]에 뭔짓을 한거지? -> 꼬리재귀 -> 기계식으로 루프 (추론 능력)

fun main() {
    val array = arrayOf(1, 2, arrayOf("a", false), 3, arrayOf("b", "c", arrayOf(1, 2)))
    println(arrayStringifyByTailrec(array))
    println(arrayStringifyByWhile(array))
}

val arrayStringifyByTailrec: (Array<out Any?>) -> String = run {
    tailrec fun recursive(arr: Array<out Any?>, acc: String, index: Int, stack: Stack<Context>): String {
        // binary mandatory -> if-else
        return if (index < arr.size) {
            val element = arr[index]
            // optional
            if (element is Array<*>) stack.push(Context(arr, acc, index))
            // binary mandatory : if 를 합쳐 사용하지 않는 이유 - 코드의 역할이 다름 SRP
            // using component-n - Effective kotlin
            val (nextArr, nextAcc, nextIndex) =
                if (element is Array<*>) Triple(element, "", 0)
                else Triple(arr, "$acc,${valueOf(element)}", index + 1)
            recursive(nextArr, nextAcc, nextIndex, stack)
        } else {
            // binary mandatory
            if (stack.isNotEmpty()) {
                val context: Context = stack.pop()
                recursive(
                    context.array,
                    acc = "${context.acc},${acc.closeArrayStringify()}",
                    index = context.index + 1,
                    stack
                )
            } else {
                acc.closeArrayStringify()
            }
        }
    }
    ({ arr -> if (arr.isEmpty()) "[]" else recursive(arr, acc = "", index = 0, Stack()) })
}

val arrayStringifyByWhile: (Array<out Any?>) -> String = run {
    fun byWhile(array: Array<out Any?>): String {
        var arr: Array<out Any?> = array
        var acc = ""
        var index = 0
        val stack: Stack<Context> = Stack()
        while (index < arr.size || stack.isNotEmpty()) {
            if (index < arr.size) {
                val element = arr[index]
                if (element is Array<*>) stack.push(Context(arr, acc, index))
                val (nextArr, nextAcc, nextIndex) =
                    if (element is Array<*>) Triple(element, "", 0)
                    else Triple(arr, "$acc,${valueOf(element)}", index + 1)
                arr = nextArr
                acc = nextAcc
                index = nextIndex
            } else {
                val context: Context = stack.pop()
                arr = context.array
                acc = "${context.acc},${acc.closeArrayStringify()}"
                index = context.index + 1
            }
        }
        return acc.closeArrayStringify()
    }

    ({ arr -> if (arr.isEmpty()) "[]" else byWhile(arr) })
}

private val stringStringify: (String) -> String = run {
    val table = arrayOf(
        "\"".toRegex() to "\\\\\"",
        "\t".toRegex() to "\\\\t",
        "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
    )
    ({ str ->
        table.fold(str) { acc, (regex, replacement) -> acc.replace(regex = regex, replacement = replacement) }
    })
}

private fun valueOf(any: Any?) = when (any) {
    is Number, is Boolean -> any.toString()
    is String -> "\"${stringStringify(any)}\""
    else -> "null"
}

private fun String.closeArrayStringify(): String = "[${substring(startIndex = 1)}]"

private class Context(
    val array: Array<out Any?>,
    val acc: String,
    val index: Int
)
