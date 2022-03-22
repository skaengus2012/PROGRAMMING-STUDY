package com.codespitz.programming.chapter2

// 오류와 예외
// 내결함성 vs 외결함성 : 코드의 결함을 내재화하지 말고 외부로 내보내는가?
// 내결함성을 유지하면, 안정성이 올라가나 신뢰성이 떨어짐
// 외결함성은 프로그램이 종료될 수 있어 안정성이 떨어지지만 신뢰성이 올라감

// 코드의 분리 또는 정리 - 수정되는 원인에 따라 :: 변화율(변화율이 같은 애들끼리 코드를 짜라), 변화율의 원인? 수정되는 이유

// 데이터와 데이터를 이용한 알고리즘이 이원화되면 관리가 불가능 -> 데이터를 소유한 쪽에서 데이터를 사용하는 알고리즘을 제어해라.

// lifecycle : 변수가 얼마나 살아있는가?
// scope : 변수를 어디에서 읽어들일 수 있나? - 권한
// 메모리와 연산은 상호교환할 수 있으며, 특히 라이프사이클이 관여함

// elementSum scope : arraySum 만 알게, lifecycle : arraySum 을 호출할 때 생성되어 리턴 시 제거
val arraySum: (IntArray) -> Int = { array ->
    fun elementSum(target: IntArray, index: Int = target.size - 1): Int = when {
        index > 0 -> target[index] + elementSum(target, index - 1)
        index == 0 -> target[index]
        else -> throw IllegalArgumentException()
    }
    elementSum(array, index = array.size - 1)
}

// elementSum scope : arraySum 만 알게, lifecycle : 영구적
val arraySum2: (IntArray) -> Int = run {
    fun elementSum(target: IntArray, index: Int = target.size - 1): Int = when {
        index > 0 -> target[index] + elementSum(target, index - 1)
        index == 0 -> target[index]
        else -> throw IllegalArgumentException()
    }
    ({ array -> elementSum(array, index = array.size - 1) })
}

// 꼬리재귀
val sumByTailRecursive: (target: IntArray) -> Int = run {
    tailrec fun sumByTailRecursive(
        target: IntArray, index: Int, acc: Int
    ): Int = if (index <= -1) acc else sumByTailRecursive(target, index - 1, acc + target[index])
    ({ array -> sumByTailRecursive(array.check { it.isNotEmpty() }, index = array.size - 1, acc = 0) })
}

// 번역한 for
fun sumByIteration(target: IntArray): Int {
    check(target.isNotEmpty()) // 기계적으로 번역해야 하기 때문에
    var acc = 0
    for (index in target.size - 1 downTo 0) {
        // 재귀와 루프의 차이 : 루프는 클로저에만 의존하는 함수를 반복시키고, 재귀함수는 인자에만 의존한다.
        // 따라서 for-loop 을 쓰게 되면, 공유변수의 상태의  lifecycle 이 길어짐

        acc = acc + target[index] // 기계적으로 번역해야 하기 때문에
    }

    return acc
}

// 문을 식으로..
fun <T> T.check(predicate: (T) -> Boolean): T {
    check(predicate(this))
    return this
}
