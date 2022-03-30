package com.codespitz.programming.chapter4

import java.util.*

// 함수본질 : 문을 담아 식으로 사용할 수 있는 그릇
// 일단 문을 식으로 만들면...
// 1. 반복적으로 그 제어문을 사용할 수 있고
// 2. 일반화만 시키면 인자에 따라 여러 문제를 하나의 로직으로 해결할 수 있고
// 3. 필요할때까지 실행을 안시킬 수 잇고
// 4. 여러개를 만들어 필요시마다 다른 제어문을 사용할 수 있다.

// 변수의 scope = 권한 x 범위 x, 이 변수를 인식할 수 있는 공간, 그 변수를 인식할 수 있는 범위


fun main() {
    val array = arrayOf(arrayOf(arrayOf(8)))
    println(arrayStringifyByTailrec(array))
}

val arrayStringifyByTailrec: (Array<out Any?>) -> String = run {
    // 1.변수의 lifecycle 은 코드의 형태와 일치하는 것은 아니다. 2.설계에 일치한다. -> 3.원하는 의도에 맞게 변수를 설정한다.

    // JSON 으로 변경하는 작업을 한 곳으로 응집
    fun arrToString(array: Array<out Any?>): String = "[%s]".format(
        array.fold("") { accStr, v -> "${accStr},${v}" }
            .takeIf { it.isNotBlank() }
            ?.substring(startIndex = 1)
            ?: ""
    )

    fun elementToNextParameter(
        v: Any?,
        arr: Array<out Any?>,
        acc: Array<String>,
        index: Int,
        stack: Stack<Context>
    ): Triple<Array<out Any?>, Array<String>, Int> = when (v) {
        is Array<*> -> {
            // 로직의 응집성을 위해 index 를 더해야함.
            // 이곳에서 더하지 않으면, 스택에서 꺼내서 1을 더하는 로직이 다른 곳에 존재.
            stack.push(Context(arr, acc, index = index + 1))
            Triple(v, emptyArray(), 0)
        }

        else -> {
            Triple(arr, acc + arrayOf(v.toString()), index + 1)
        }
    }


    tailrec fun recursive(
        arr: Array<out Any?>,
        acc: Array<String>,
        index: Int,
        stack: Stack<Context>
    ): String {
        // 문자열로 다루는 코드
        // 1. 주머니에 500원. 오늘만 산다. 현재 합쳐진 문자열이면 충분해, 그 문자열을 중간에 어떻게든 잘 만들었겠지. 과거에 나를 믿는다.
        // 2. +100, -100, 1000, -500 == 500
        //    가계부라도 써서 미래의 나는 대비한다. 데이터에는 내가 가공할 재료들이 들어있군, 가공법은 arrToString 에 있네.
        //    무조건 2번이 유리함. (데이터와 로직을 분리하는 코드)

        return if (index < arr.size) {
            // 각 원소 문자열로 환원하여 다른 배열에 담아둔다.

            // if 를 제거하는 방법
            // 선행해서 모든 전략객체가 같은 인터페이스(인자의 모양과 반환값의 모양)을 갖도록 조정
            // why?
            // 1.OCP,
            // 2.IOC,
            // 3.복잡성 정복 - 격리를 통해 한번에 다룰 복잡성 줄이기. -> 응집도, 결합도가 낮은 독립적인 모듈로 만들어 정복.
            //    문을 식(함수값, 전략객체, 커맨드 객체)으로 변경
            //    원래 제어문이었던 것을, 함수라는 그릇에 담아 값으로 변경한 뒤 원하는 함수값을 필요시마다 선택해서 사용
            //    장점 : 문은 코드 작성시마다 확정됨으로 변경하려면 코드를 변경하고 확인해야 하나, 함수화된 값은 코드 실행 시 원하는 함수를 선택할 수 있음으로 필요한 코드를 대입할 때 사용하는 측의 코드는 변경할 필요가 없다.

            val (nextArr, nextAcc, nextIndex) = elementToNextParameter(
                arr[index],
                arr,
                acc,
                index,
                stack
            )
            recursive(nextArr, nextAcc, nextIndex, stack)
        } else {
            // 원소별 문자열로 환원된 배열을 이용해서 통합 문자열을 만든다.

            // LinkedList 의 Stack 을 사용한다면, 원래 length 에 의존적이지 않다.
            // Kotlin 의 경우 pop 을 할 경우 예외를 던지기 때문에 다음과 같이 작성.
            val pre = stack.runCatching { pop() }.getOrNull()
            if (pre != null) {
                val (prevArr, prevAcc, prevIndex) = pre
                recursive(prevArr, acc = prevAcc + arrToString(acc), index = prevIndex, stack)
            } else {
                arrToString(acc)
            }
        }
    }

    ({ arr ->
        // 일반화 = 모든 경우의 수를 처리하는 알고리즘
        // 별도의 케이스를 처리 (배열이 빈 경우) 하는 것은 일반화가 깨진 것.
        recursive(arr, acc = emptyArray(), index = 0, Stack())
    })
}

private class Context(
    val array: Array<out Any?>,
    val acc: Array<String>,
    val index: Int
) {
    operator fun component1(): Array<out Any?> = array
    operator fun component2(): Array<String> = acc
    operator fun component3(): Int = index
}