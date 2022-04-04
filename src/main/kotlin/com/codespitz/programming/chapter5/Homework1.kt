package com.codespitz.programming.chapter5

// 숙제 :
// [1, 2, 3, 4, 5, 6, 7].filter(it=>it%2).map(it=>it*2) 7회.

private inline fun <T> Iterable<T>.filterLazy(crossinline f: (T) -> Boolean): Iterable<T> = Iterable {
    val internalIterator = iterator()
    iterator {
        while (internalIterator.hasNext()) {
            val v = internalIterator.next()
            if (f(v)) yield(v)
        }
    }
}

private inline fun <T, U> Iterable<T>.mapLazy(crossinline f: (T) -> U): Iterable<U> = Iterable {
    val internalIterator = iterator()
    iterator {
        while (internalIterator.hasNext()) {
            yield(f(internalIterator.next()))
        }
    }
}

fun main() {
    listOf(1, 2, 3, 4, 5, 6, 7)
        .filterLazy { value -> println("filter 1 -> $value"); value % 2 != 0 }
        .mapLazy { value -> println("map 2 -> $value"); value * 2 }
        .iterator()
        .run { if (hasNext()) println(next()) }
}

