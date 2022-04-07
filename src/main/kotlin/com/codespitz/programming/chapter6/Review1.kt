package com.codespitz.programming.chapter6

sealed class IteratorResult<out T> {
    data class Value<T>(val value: T) : IteratorResult<T>() {
        override fun toString(): String = "IteratorResult.Result(value=$value)"
    }
    object Done : IteratorResult<Nothing>() {
        override fun toString(): String = "IteratorResult.Done"
    }
}

fun <T> IteratorResult<T>.isDone(): Boolean =  this is IteratorResult.Done
fun <T> IteratorResult<T>.requireValue(): IteratorResult.Value<T> = this as IteratorResult.Value<T>

interface Iterator<T> {
    fun next(): IteratorResult<T>
}

// Iterator 는 확정된 자료구조가 아닌, next 를 호출할 때 마다 그 다음번 자료가 나갈지 말지 결정하는 자료 구조
// Iterable 은 Iterator 를 반환하는 개체, 실제 자료는 Iterator 로 얻음.
// 데코레이터 패턴 : 함수의 지연 리스트를 기반으로 만들어진 패턴. Iterator 패턴도 데코레이터 패턴으로 정의됨.
class ArrayIterator<T>(private val arr: Array<T>) : Iterator<T> {
    private var cursor: Int = 0
    override fun next(): IteratorResult<T> =
        if (cursor >= arr.size) IteratorResult.Done else IteratorResult.Value(arr[cursor++])
}

fun <T> arrayIterator(arr: Array<T>): Iterator<T> = ArrayIterator(arr)
fun <T> filter(iter: Iterator<T>, f: (T) -> Boolean): Iterator<T> = object : Iterator<T> {
    override fun next(): IteratorResult<T> {
        var result = iter.next()
        while (!result.isDone()) {
            val value = result.requireValue()
            if (f(value.value)) return value
            result = iter.next()
        }
        return result
    }
}

fun <T, U> map(iter: Iterator<T>, f: (T) -> U): Iterator<U> = object : Iterator<U> {
    override fun next(): IteratorResult<U> = when (val result = iter.next()) {
        is IteratorResult.Value -> IteratorResult.Value(f(result.value))
        else -> IteratorResult.Done
    }
}

fun main() {
    val iter1 = map(filter(arrayIterator(arrayOf(1, 2, 3, 4))) { num -> num % 2 == 0 }) { num -> num * 2 }
    println(iter1.next())
    println(iter1.next())
    println(iter1.next())
    println(iter1.next())
    println(iter1.next())
}