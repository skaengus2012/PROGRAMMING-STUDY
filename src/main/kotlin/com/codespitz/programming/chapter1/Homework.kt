package com.codespitz.programming.chapter1

// 숙제 : 1차원 배열의 합
// 재귀, 꼬리 재귀 -> 번역한 for
fun main() {
    val target = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    // 재귀
    fun sumByRecursive(target: IntArray, index: Int = target.size - 1): Int = when {
        index > 0 -> target[index] + sumByRecursive(target, index - 1)
        index == 0 -> target[index]
        else -> throw IllegalArgumentException()
    }

    // 꼬리재귀
    tailrec fun sumByTailRecursive(target: IntArray, index: Int = target.size - 1, acc: Int = 0): Int = when {
        index > 0 -> sumByTailRecursive(target, index - 1, acc + target[index])
        index == 0 -> acc + target[index]
        else -> throw IllegalArgumentException()
    }

    // 번역한 for
    fun sumByIteration(target: IntArray): Int {
        var acc = 0
        for (index in target.size -1 downTo 0) acc += target[index]
        return acc
    }

    println(sumByRecursive(target))
    println(sumByTailRecursive(target))
    println(sumByIteration(target))
}
