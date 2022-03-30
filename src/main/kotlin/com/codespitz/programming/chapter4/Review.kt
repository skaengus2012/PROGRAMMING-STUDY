package com.codespitz.programming.chapter4

// 함수본질 : 문을 담아 식으로 사용할 수 있는 그릇
// 일단 문을 식으로 만들면...
// 1. 반복적으로 그 제어문을 사용할 수 있고
// 2. 일반화만 시키면 인자에 따라 여러 문제를 하나의 로직으로 해결할 수 있고
// 3. 필요할때까지 실행을 안시킬 수 잇고
// 4. 여러개를 만들어 필요시마다 다른 제어문을 사용할 수 있다.

// 변수의 scope = 권한 x 범위 x, 이 변수를 인식할 수 있는 공간, 그 변수를 인식할 수 있는 범위

// 컬렉션의 책임은 단일 값보다 크다.
// 반드시 필요한 경우가 아니면 컬렉션을 사용하지 않는다.
// 1. parent.children = [child, child] // 책임이 훨씬 더 무거움.
// 2. child1.parent = p1, child2.parent = p2 // 자식이 부모만 알면되기 때문에 가벼움. 2번이 더 좋은 설계
// 결합도의 무게를 줄일려면, 컬렉션 사용을 지양해야함. 가벼운 의존성.


fun main() {
    val array = arrayOf(arrayOf(1, 2, 3,), 4, arrayOf("HELLO", "HI"), emptyArray<Any>())
    println(arrayStringifyByTailrec(array))
    println(arrayStringifyWhile(array))
}

// JSON 으로 변경하는 작업을 한 곳으로 응집
private fun arrToString(array: Array<out Any?>): String = "[%s]".format(
    array.fold("") { accStr, v -> "${accStr},${v}" }
        .takeIf { it.isNotBlank() }
        ?.substring(startIndex = 1)
        ?: ""
)

private fun elementToNextParameter(
    v: Any?,
    arr: Array<out Any?>,
    acc: Array<String>,
    index: Int,
    prevContext: Context?
): Context = when (v) {
    is Array<*> -> {
        // 로직의 응집성을 위해 index 를 더해야함.
        // 이곳에서 더하지 않으면, 스택에서 꺼내서 1을 더하는 로직이 다른 곳에 존재.
        Context(v, emptyArray(), 0, Context(arr, acc, index = index + 1, prevContext))
    }

    else -> {
        Context(arr, acc + arrayOf(v.toString()), index + 1, prevContext)
    }
}


val arrayStringifyByTailrec: (Array<out Any?>) -> String = run {
    // 1.변수의 lifecycle 은 코드의 형태와 일치하는 것은 아니다. 2.설계에 일치한다. -> 3.원하는 의도에 맞게 변수를 설정한다.

    tailrec fun recursive(
        arr: Array<out Any?>,
        acc: Array<String>,
        index: Int,
        prevContext: Context?
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

            val (nextArr, nextAcc, nextIndex, nextContext) = elementToNextParameter(
                arr[index],
                arr,
                acc,
                index,
                prevContext
            )
            recursive(nextArr, nextAcc, nextIndex, nextContext)
        } else {
            // 원소별 문자열로 환원된 배열을 이용해서 통합 문자열을 만든다.

            // LinkedList 의 Stack 을 사용한다면, 원래 length 에 의존적이지 않다.
            // Kotlin 의 경우 pop 을 할 경우 예외를 던지기 때문에 다음과 같이 작성.
            if (prevContext != null) {
                val (prevArr, prevAcc, prevIndex, prevPrevContext) = prevContext
                recursive(prevArr, acc = prevAcc + arrToString(acc), prevIndex, prevPrevContext)
            } else {
                arrToString(acc)
            }
        }
    }

    ({ arr ->
        // 일반화 = 모든 경우의 수를 처리하는 알고리즘
        // 별도의 케이스를 처리 (배열이 빈 경우) 하는 것은 일반화가 깨진 것.
        // stack 을 사용할 필요가 없음. 직전의 context 만 알고 있으면 됨. -> 자료구조를 잘 모르기 때문에, 컬렉션을 이용하려 듦.
        // acc 도 엄밀히 배열이면 안됨.
        recursive(arr, acc = emptyArray(), index = 0, prevContext = null)
    })
}

fun arrayStringifyWhile(array: Array<out Any?>): String {
    var arr: Array<out Any?> = array
    var acc: Array<String> = emptyArray()
    var index = 0
    var prevContext: Context? = null

    // 꼬리 재귀 최적화가 제대로 만들어졌다면, while 로 변경했을 때 자연스럽게 변경됨.
    while (true) {
        if (index < arr.size) {
            val (nextArr, nextAcc, nextIndex, nextContext) = elementToNextParameter(
                arr[index],
                arr,
                acc,
                index,
                prevContext
            )
            arr = nextArr
            acc = nextAcc
            index = nextIndex
            prevContext = nextContext
        } else {
            if (prevContext != null) {
                val (prevArr, prevAcc, prevIndex, prevPrevContext) = prevContext
                arr = prevArr
                acc = prevAcc + arrToString(acc)
                index = prevIndex
                prevContext = prevPrevContext
            } else {
                return arrToString(acc)
            }
        }
    }
}

private class Context(
    val array: Array<out Any?>,
    val acc: Array<String>,
    val index: Int,
    val prevContext: Context?
) {
    operator fun component1(): Array<out Any?> = array
    operator fun component2(): Array<String> = acc
    operator fun component3(): Int = index
    operator fun component4(): Context? = prevContext
}
