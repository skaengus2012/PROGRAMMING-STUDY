package com.codespitz.programming.chapter6

import com.codespitz.programming.chapter4.stringify
import java.util.Stack

// 오늘 할 것.
// "[1,2,[3,4,[5,[6,7]]]]" 중첩된 배열을 어찌 배열로 만들 것인가?
// 문자열을 다룰 때, 문자 하나 하나를 다루는 Tokenizer 를 만들기 힘듦 -> 정규식이 필요

// 패턴을 알아야함.
// "[ 1 ,      2,  [ 3,  4, [  5  , [  6, 7]  ]  ]  ]"
// 파서는 왼쪽에서 오른쪽으로 파싱
// 1. 대괄호 열기가 온다 => 새 배열을 만든다. 지금 배열은 스택으로 넣어버린다.
// 2. 대괄호 닫기가 온다. => 지금 배열을 종료하고, 스택을 이전으로 돌아간다.
// 3. 적합한 원소가 온다. 원소는 컴마를 포함하거나 하지 않는다. (마지막 원소인 경우) => 현재 배열에 값을 추가한다.
// 우리는 당면한 국면만 바라보고 처리해야함.

// 열린목록 => 해결 할 문제, 닫힌목록 => 해결된 문제
// 열린목록에서 결과를 추출한 뒤, 닫힌 목록으로 이동.
// stringify 쉬움 : 정형화된 데이터를 비정형으로 변경
// parser 어려움 : 비정형인 데이터를 정형화.

val arrayParse: (str: String) -> Array<*> = run {
    // ^ : 문자열의 시작
    // \s : 공백 문자
    // * : 0 개 이상 {0, }
    // + : 1 개 이상 {1, }
    // ? : 없거나 하나 있음 {0, 1}
    val elementRegex: Regex by lazy { "^\\s*([0-9]+)\\s*,?".toRegex() }
    tailrec fun parse(str: String, acc: Array<Any>, stack: Stack<Array<Any>>): Array<*> {
        val v: String = str.trim()
        if (v.isEmpty()) return acc
        return when (v[0]) {
            '[' -> {
                stack.push(acc)
                parse(str = v.substring(startIndex = 1), emptyArray(), stack)
            }
            ']' -> {
                if (stack.isEmpty()) acc
                else parse(str = v.substring(startIndex = 1), acc = stack.pop().plusElement(acc), stack)
            }
            else -> {
                val matchResult: MatchResult = checkNotNull(elementRegex.find(v)) { "Invalid value : $v" }
                parse(
                    str = v.substring(startIndex = matchResult.value.length),
                    acc = acc + matchResult.value
                        .first()
                        .toString()
                        .let { Integer.parseInt(it) },
                    stack
                )
            }
        }
    }

    ({ str ->
        checkNotNull(parse(str, emptyArray(), Stack()).firstOrNull()?.let { it as? Array<*> }) {
            "Input was not array. str is \"$str\""
        }
    })
}



fun main() {
    println(stringify(arrayParse("[1, 2,   [   3, 4] ]")))
}