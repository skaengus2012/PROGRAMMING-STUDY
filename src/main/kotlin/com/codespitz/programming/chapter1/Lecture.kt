package com.codespitz.programming.chapter1

/**
 * 모든 언어의 element
 * val a = 5
 * val(문, 명령) a(식별자) = 5(식)
 */
fun main() {
    println(sum1())
    println(sum2())
    println(sum3())
    println(sum4())
}

private fun sum1(): Int {
    var acc = 0
    // for vs while
    // for -> iteration, 계약 조건이 정확히 명시될 때
    // while -> recursive, 얼마나 반복할지 모름.
    // 현재 : control-freak & side-effect
    for (i in 1..10) {
        acc += i
    }

    // Q. 구구단 이상의 코드를 for-loop 으로 짤 수 있나? (Json 파서 등)
    return acc
}

private fun sum2(): Int {
    // 현재 시점을 쪼개서, 패턴을 찾아야 함
    // 귀납적 방법
    // 문제를 한칸만 바라보는 훈련이 필요, 전체를 통제하려 하지 말자.
    // 떠넘기다. 어려운 문제를 떠넘겨라.
    // 1~10 = sum(9) + 10
    fun sum(v: Int): Int = if (v <= 1) 1 else v + sum(v - 1)

    // Q. 스택 오버플로우 문제.
    // 실행기가 잘못된 것을 예상하고 죽임. OS 가 보다 자원을 더 확보하기 위해서
    return sum(10)
}

private fun sum3(): Int {
    // Stack clear (문을 없앴기 때문에 식만 존재, 함수형)
    // 꼬리물기 최적화 :  재귀함수나 루틴이 반복될 때 루틴 위치를 그 다음번 함수에게 인계하는 기능
    // 조건 : return 하는 시점에 어떤 것도 남기면 안됨, 함수를 호출하고 복귀할 때 할 일이 있다면 함수 안에 인자로 넘겨야 함.
    // 기계적으로 만든 꼬리물기 재귀는 언제나 for 로 변경 가능.
    tailrec fun sum(v: Int, acc: Int = 0): Int = if (v <= 1) acc + 1 else sum(v - 1, acc + v)
    // Q. 구구단 이상의 코드를 for-loop 으로 짤 수 있나? (Json 파서 등)
    // A. 꼬리물기 최적화를 이용해 재귀적으로 굉장히 어려운 문제를 풀 수 있고, 이 코드는 for-loop 으로 변경 가능
    return sum(10)
}

private fun sum4(): Int {
    // 변수를 목적에 따라 정확히 이해해야함
    val v = 10      // 상수 - 특정 컨텍스트(해결하려는 문제) 하에서 미리 주어진, 함수 입장에서는 인자가 상수일 확률이 높음.
    var acc = 0     // 저장소, 스토리지
    for (i in v downTo 2) acc += i  // i 제어변수, 카운터
    acc += 1

    // 모든 변수는 Lifecycle, scope(어휘공간, 일반적인 scope, ex. 중괄호, 함수), 권한(예외적인 scope, ex. 객체의 멤버) 를 갖음
    // Lifecycle : 변수가 얼마나 생존하는가, 되도록이면 짧게 유지한다. (why, 인지부조화)
    // scope : 되도록이면 좁게, (why, 인지부조화, 다른 곳에서 접근방지)
    // 꼬리재귀는 Lifecycle 이 짧음, 저장소가 필요 없음.
    return acc
}